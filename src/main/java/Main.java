/* Created By Nadeem Ul Haq Mir */

import org.wikipedia.Wiki;
import org.wikipedia.tools.CommandLineParser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;

public class Main implements Runnable{

public static ArrayList<Typo> typos;

   // int MAX_EDITS;
    //int EDITS;
    int MAX_EDITS_PER_RUN;
    boolean DEBUG=true;
   // boolean DEEP_SEARCH=false;
    Config config =null;

    String credPath= System.getProperty("user.home")+"\\desktop\\cred.cnf";

public Main(String[] args){
    CommandLineParser cmd=new CommandLineParser();
    cmd.addSingleArgumentFlag("--credentials","cred","");
    Map<String,String> ars= cmd.parse(args);
   // String credPath= System.getProperty("user.home")+"\\desktop\\cd.cnf";
    if(ars.containsKey("--credentials"))
        credPath=ars.get("--credentials");
    loadProperties();

}

private void loadProperties(){
    try {
        config =new Config(new File(credPath));
        config.load();
        //MAX_EDITS= config.getMAX_EDITS();
        MAX_EDITS_PER_RUN= config.getMAX_EDITS_PER_RUN();
        DEBUG= config.isDEBUG();
    }
    catch (Exception e){
        log("Exception in parsing credentials file "+e);
        System.exit(0);
    }

}

public static  void main(String[] args){
    Main main=new Main(args);
    new Thread(main)
            .start();

 }





  public void run(){

        log("DEBUG MODE: "+String.valueOf(DEBUG));

        try{
            String username= config.getUsername();
            String password= config.getPassword();
            Wiki wiki= Wiki.newSession("ks.wikipedia.org");
            wiki.setLogLevel(Level.OFF);
            wiki.setUsingCompressedRequests(false);
            wiki.setUserAgent("TypoFixer/1.0 (https://ks.wikipedia.org/wiki/User:Nadeemulhaqmir-bot; nadeemulhaqmir@gmail.com) WikiJava");
            if(username!=null && password!=null && !DEBUG){
                log("Login with Credentials");
                wiki.login(username,password);
                wiki.setMarkBot(true);
                wiki.setThrottle(5000); // 5 sec
            }
            else {
                log("Login as Anonymous");
            }



            // wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE);
            List<String> c=  wiki.getPageText(List.of(wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE)+":AutoWikiBrowser/Typos"));
            // List<Map<String,String>> p=  wiki.getPageProperties(List.of(wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE)+":AutoWikiBrowser/Typos"));

            String logUrl=wiki.namespaceIdentifier(Wiki.USER_NAMESPACE)+":Nadeemulhaqmir-bot/log/"+getYear()+"/"+getMonth();

            Script script=new Script();

            typos=Helper.extractTags(Helper.listToString(c));
            HashMap<String,Boolean> options=new HashMap<>();
            options.put("top",Boolean.TRUE);
            options.put("bot",Boolean.FALSE);
            // options.put("new",Boolean.TRUE);
            // options.put("patrolled",Boolean.TRUE);
            Wiki.RequestHelper rq=wiki.new RequestHelper()
                    .inNamespaces(Wiki.MAIN_NAMESPACE )
                    .withinDateRange(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2), null)
                    .filterBy(options)
                    .limitedTo(200)  // revisions count
                    .notByUser(username)    // Skip self
                    .reverse(false);

            List<Wiki.Revision> revs= wiki.recentChanges(rq);
            // int edits=0;

            if(revs.isEmpty()){
                log("No Revision found");
            }
            else {
                revs=removeDuplicateRevs(revs);
                log("Fetched "+revs.size()+" revisions.");

                StringBuilder sb=new StringBuilder();
                StringBuilder log=new StringBuilder();
                int EDITS_COUNT=0;
                int VISITS=0;
                long pTime;
                long currentTime;
                long elapsedTime;
                boolean first;
                Typo laggingTypo=null;
                PagesQueue pagesQueue=new PagesQueue(wiki);
                pagesQueue.setPageNames(revs);
                Page page;
               // log.append("==").append(getDate()).append("==\n");
                for(Wiki.Revision r:revs){
                       page= pagesQueue.removePage();

                    if(EDITS_COUNT>=MAX_EDITS_PER_RUN){
                        log("MAX_EDITS_PER_RUN ("+MAX_EDITS_PER_RUN+") reached");
                        break;
                    }
                    String title=r.getTitle();
                    String text= page.getContent();

                    script.reset();
                    script.read(text);
                    if(script.arabic < (script.devanagri+script.sharda)){ // Skip Devanagri or Sharda
                        log("Skipped Devanagri/Sharda script. "+script.toString()+"\n Url: "+r.permanentUrl());
                        continue;
                    }
                    sb.setLength(0);
                    sb.append("Title: ").append(title);
                    page.fixTypos(typos);
                    String newContent=page.getContent();
                    if(!newContent.equals(text)) {
                        try{
                            if(!DEBUG)
                                wiki.edit(title,newContent,"Fixed Typo Error ");
                            long revNo=(long) ((Map)wiki.getPageInfo(List.of(title)).get(0)).get("lastrevid");
                            page.pushTitleLog(String.valueOf(revNo));
                            log.append(page.getLog());
                            EDITS_COUNT++;
                           // sb.append("\n").append("Edited revision  ").append(EDITS);
                           // sb.append("\n").append("Url: ").append(r.permanentUrl());
                           // log(sb.toString());
                        }
                        catch(Exception editEx){
                            log("Edit exception-> Page Title: "+title + "Url "+r.permanentUrl()+"\n"+editEx);
                        }
                       // break;
                    }


                    VISITS++;

                }

               log("Iterated ("+VISITS+") Revisions out of "+revs.size());
                if(EDITS_COUNT>0){
                   String _log= wiki.getPageText(List.of(logUrl)).get(0);
                   String date=getDate();
                      if(_log==null)
                           _log=date;
                     if(!_log.contains(date))
                         log.insert(0,date);
                     _log= _log+log.toString();
                     if(!DEBUG)
                      wiki.edit(logUrl,_log,"TypoFixer Log");
                   // log("Log"+_log);
                }
                if(config !=null)
                  config.store();
            }

        }
        catch (Exception e){
            log("Exception: "+ e.toString());

        }




    }

    public static List<Wiki.Revision> removeDuplicateRevs(List<Wiki.Revision> list){
        List<Wiki.Revision> nList=new ArrayList<Wiki.Revision>();
        String t;
        boolean exists;
        for(Wiki.Revision r:list) {
            if(r.getID()<1L || r.getSize()==0)  // No valid content
                continue;
            exists=false;
            for(Wiki.Revision nr:nList){
                t=nr.getTitle();
                if(t.equals(r.getTitle())){
                    exists=true;
                    break;
                }
            }
            if(!exists)
                nList.add(r);

        }
          return nList;
    }



    public static void log(String text){
        Date dt=Calendar.getInstance().getTime();
        System.out.println();
        System.out.println(dt.toString());
        System.out.println(text);

    }
    public static int getYear(){
       return LocalDate.now().getYear();
    }
    public static int getMonth(){
        return LocalDate.now().getMonthValue();
    }
    public static  String getDate(){
       LocalDateTime ld=LocalDateTime.now();
      return String.format("\n==%d-%d-%d==\n",ld.getDayOfMonth(),ld.getMonthValue(),getYear());
    }

}
