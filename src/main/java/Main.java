
import org.w3c.dom.*;
import org.wikipedia.Wiki;
import org.wikipedia.tools.CommandLineParser;
import org.wikiutils.LoginUtils;
import org.wikiutils.ParseUtils;
import org.wikiutils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main implements Runnable{

public static ArrayList<Typo> typos;

    int MAX_EDITS=5;
    int EDITS=0;
    int MAX_EDITS_PER_RUN=1;
    boolean DEBUG=true;
    boolean DEEP_SEARCH=false;
    Properties props=null;
    String username=null;
    String password=null;
    String credPath= System.getProperty("user.home")+"\\desktop\\cred.cnf";

    boolean RUN_FLAG=true;
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
        // String pth= System.getProperty("user.home")+"\\desktop\\cd.cnf";
        props=new Properties(new File(credPath));
        props.load();
        username = props.getProperty("username");
        password =props.getProperty("password");
        String val=props.getProperty("max_edits");
        if(val!=null)
            MAX_EDITS=Integer.parseInt(val);
        val=props.getProperty("edits");
        if(val!=null)
            EDITS=Integer.parseInt(val);
        val=props.getProperty("max_edits_per_run");
        if(val!=null)
            MAX_EDITS_PER_RUN=Integer.parseInt(val);
        val=props.getProperty("debug");
        if(val!=null)
            DEBUG=Boolean.parseBoolean(val);
        val=props.getProperty("deep_search");
        if(val!=null)
            DEEP_SEARCH=Boolean.parseBoolean(val);
          props.store();
    }
    catch (Exception e){
        RUN_FLAG=false;
        log("Exception in parsing cred file "+e);
        System.exit(0);
    }

}

public static  void main(String[] args){
    Main main=new Main(args);
    new Thread(main)
            .start();

 }





  public void run(){


          if(!RUN_FLAG)
              return;
        log("DEBUG MODE: "+String.valueOf(DEBUG));
        log("DEEP SEARCH: "+String.valueOf(DEEP_SEARCH));
        try{
            Wiki wiki= Wiki.newSession("ks.wikipedia.org");
            if(username!=null && password!=null && !DEBUG){
                log("Login with Credentials");
                wiki.login(username,password);
                wiki.setMarkBot(true);
            }
            else {
                log("Login as Anonymous");
            }

            //   String txt= wiki.getPageText(List.of("Elizabeth_II")).toString();
            //   Page p=new Page(txt);
            //   System.out.println(p.getContent().equals(txt));
            //   System.out.println("Text \n"+ p.getLinks());
            //  System.out.println("Text \n"+ p.getText());
            //  System.out.println("Temp \n"+ p.getTemplates());
            //  System.out.println("Text \n"+ p.getContent());

            //  List<String> c=  wiki.longPages(50); //wiki.getPageText(List.of("Kashmir"));
            // wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE);
            List<String> c=  wiki.getPageText(List.of(wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE)+":AutoWikiBrowser/Typos"));
            // List<Map<String,String>> p=  wiki.getPageProperties(List.of(wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE)+":AutoWikiBrowser/Typos"));

           // List<String> t=  wiki.getPageText(List.of(wiki.namespaceIdentifier(Wiki.USER_NAMESPACE)+":Nadeemulhaqmir-bot"));

           // List<String> t=   wiki.getPageText(List.of("نِتاشا_کول"));
           // String str= t.toString();
            Script script=new Script();

            typos=Helper.extractTags(Helper.listToString(c));
            HashMap<String,Boolean> options=new HashMap<>();
            options.put("top",Boolean.TRUE);
            options.put("bot",Boolean.FALSE);
            // options.put("new",Boolean.TRUE);
            // options.put("patrolled",Boolean.TRUE);
            Wiki.RequestHelper rq=wiki.new RequestHelper()
                    .inNamespaces(Wiki.MAIN_NAMESPACE )
                    .withinDateRange(OffsetDateTime.now(ZoneOffset.UTC).minusDays(12), null)
                    .filterBy(options)
                    .limitedTo(2000)  // revisions count
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
                ArrayList<Typo> matchedTypos=new ArrayList<>();
                int EDITS_COUNT=0;
                int VISITS=0;
                long pTime;
                long currentTime;
                long elapsedTime;
                boolean first;
                Typo laggingTypo=null;
                for(Wiki.Revision r:revs){
                    if(r.getID()<1L || r.getSize()==0)  // No valid content
                        continue;
                    if(EDITS>=MAX_EDITS){
                        log("MAX_EDITS ("+EDITS+") reached");
                        break;
                    }
                    if(EDITS_COUNT>=MAX_EDITS_PER_RUN){
                        log("MAX_EDITS_PER_RUN ("+MAX_EDITS_PER_RUN+") reached");
                        break;
                    }
                    String title=r.getTitle();
                    String text=DEEP_SEARCH?Helper.listToString( wiki.getPageText(List.of(title))):r.getText();
                    matchedTypos.clear();
                    script.reset();
                    script.read(text);
                    if(script.arabic < (script.devanagri+script.sharda)){ // Skip Devanagri or Sharda
                        log("Skipped Devanagri/Sharda script. "+script.toString()+"\n Url: "+r.permanentUrl());
                        continue;
                    }
                    sb.setLength(0);
                    sb.append("Title: ").append(title);
                    first=true;
                    for(Typo typo:typos){
                        Pattern pattern=Pattern.compile(typo.find);
                        Matcher matcher = pattern.matcher(text);  //typo.pattern.matcher(text);
                        pTime=System.currentTimeMillis();
                        if(matcher.find() ){
                            if(first){
                                sb.append("\nTypo: ").append(typo.find).append(" -> ").append(typo.replace);
                                first=false;
                            }
                            String group=matcher.group();
                            String nGroup=group.replaceAll(typo.find,typo.replace);
                            sb.append("\n").append(matcher.group()).append(" -> ").append(nGroup);
                            matchedTypos.add(typo);
                        }
                        currentTime=System.currentTimeMillis();
                        elapsedTime= currentTime-pTime;

                        if(elapsedTime>3000){
                            log("Lagging ("+elapsedTime+")ms Typo: "+typo.toString());
                            laggingTypo=typo;
                        }
                        // pTime=currentTime;


                    }
                    if(laggingTypo!=null){
                        typos.remove(laggingTypo);
                        laggingTypo=null;
                    }

                    if(!matchedTypos.isEmpty()) {
                        String content= DEEP_SEARCH?text:Helper.listToString( wiki.getPageText(List.of(title)));
                        String newContent;
                        Page page=new Page(content);
                        for(Typo typo:matchedTypos){
                            if(Typo.SCOPE_TEXT.equals(typo.scope))
                              page.replaceAllInTexts(typo.find,typo.replace);
                            if(Typo.SCOPE_TEMPLATE.equals(typo.scope))
                              page.replaceAllInTemplates(typo.find, typo.replace);
                        }
                        newContent=page.getContent();
                        if(!newContent.equals(content)) {
                            try{
                            if(!DEBUG){
                                wiki.edit(title,newContent,"Fixed Typo Error ");
                            }
                            EDITS++;
                            EDITS_COUNT++;
                            props.setProperty("edits",String.valueOf(EDITS));
                            sb.append("\n").append("Edited revision  ").append(EDITS);
                            sb.append("\n").append("Url: ").append(r.permanentUrl());
                            log(sb.toString());
                            }
                            catch(Exception editEx){
                                log("Edit exception-> Page Title: "+title + "Url "+r.permanentUrl());
                            }
                        }

                    }
                    //  else
                    //      log("Nothing to Replace "+title);
                    VISITS++;
                }
                // END of revisions loop
               log("Iterated ("+VISITS+") Revisions out of "+revs.size());
                if(props!=null)
                  props.store();
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
              if(r.getID()<1L){  // Invalid rev
                  continue;
              }
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

}
