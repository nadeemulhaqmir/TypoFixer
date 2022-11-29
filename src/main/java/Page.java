import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Page {
    private ArrayList<Section> sectionsList;
    private ArrayList<Section> textsList;
    private ArrayList<Section> templatesList;
    private ArrayList<Section> linksList;
    private String title;
    private StringBuilder log;
    public Page(String text) throws Exception{
        sectionsList=new ArrayList<>();
        textsList=new ArrayList<>();
        templatesList =new ArrayList<>();
        linksList =new ArrayList<>();
        log=new StringBuilder();
        String [][] regexs= new String[][]{
            {"{{","}}" }                 // (\[\[(.*|s.*)\]\])
                ,{"[[","]]"}              // (\{\{.*|s|.*\}\})
               // ,"(?s)(\\{\\{.*\\}\\})"
                };
        int[] types=new int[]{
                Section.TYPE_TEMPLATE
                ,Section.TYPE_LINK
                };
        sectionsList.add(new Section(text,Section.TYPE_TEXT));
        Section section;
        ArrayList<Section> _sections;
        int size;
        for(int x=0;x< regexs.length;x++){
            for(int i=0;i< sectionsList.size();i++){
                section=sectionsList.get(i);
                if(section.type==Section.TYPE_TEXT){
                 _sections=parseSections(section.text,regexs[x][0],regexs[x][1],types[x]);
                 size= _sections.size();
                 // if(size==1 && _sections.get(0).type==Section.TYPE_TEXT) // NO change

                 if(size>0){
                      sectionsList.remove(i);
                      sectionsList.addAll(i,_sections);
                     // i-- ;//= _sections.size()-1;
                 }
                }
            }
        }
        for (Section s:sectionsList) {
            switch (s.type){
                case  Section.TYPE_TEXT:
                    textsList.add(s);
                    break;
                case  Section.TYPE_TEMPLATE:
                    templatesList.add(s);
                    break;
                case  Section.TYPE_LINK:
                    linksList.add(s);
                    break;

            }

        }
         if(text!=null && !text.equals(getContent())){
             throw new Exception("Page parsing error");
         }

    }
   public void setTitle(String title){
        this.title=title;
   }
   public String getTitle(){
       return this.title;
    }

   /* private ArrayList<Section> parseSections(String text, String regex, int type) {
        ArrayList<Section> _sections = new ArrayList<>();
        if(text==null)
            return _sections;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        int group=1;
        int prev = 0;
        int start = 0;
        int end = 0;
        int len = text.length();
        while (m.find()) {
            start = m.start(group);
            end = m.end(group);
            if (start - prev > 0)  // Default type
                _sections.add(new Section(text.substring(prev, start), Section.TYPE_TEXT));
            _sections.add(new Section(text.substring(start, end), type));
            prev = end;
        }
        if (end < len)  // Default
            _sections.add(new Section(text.substring(end, len), Section.TYPE_TEXT));

        return _sections;
    }
    */



    private ArrayList<Section> parseSections(String text, String startTag,String endTag, int type) {
        ArrayList<Section> _sections = new ArrayList<>();
             if(text==null)
              return _sections;
            int a=0,b=0; //m=0;
            int prevTextIndex=0;
           // String startTag="{{";
           // String endTag="}}";
            int startTagLen=startTag.length();
            int endTagLen=endTag.length();
            int len=text.length();
            while(a<len){
                a= text.indexOf(startTag,a);
                b=a;
                do{
                    b+=startTagLen;
                    b = text.indexOf(endTag, b );
                    // if(b<0) // mismatch tags
                    //    break;
                }while(a>=0 && countTags(text,startTag,a,b)!=countTags(text,endTag,a,b));

                if(a>=0 && b>a){
                    if(a-prevTextIndex>0)
                        _sections.add(new Section(text.substring(prevTextIndex, a), Section.TYPE_TEXT));
                   // template+= "|" + text.substring(a,b+endTagLen) +"|";
                    _sections.add(new Section(text.substring(a,b+endTagLen), type));
                    a=b+endTagLen;
                    prevTextIndex=a;
                }
                else{
                    _sections.add(new Section(text.substring(prevTextIndex, len), Section.TYPE_TEXT));
                  //  template+=  "|" +  text.substring(prevTextIndex,len) + "|" ;
                    break;
                }

            }
        return _sections;
    }

    private int countTags(String text,String tag,int index,int limit){
        int in=0;
        int tagLength=tag.length();
        do{
            index=text.indexOf(tag,index);
            if(index>limit)
                break;
            if(index>=0){
                in++;
                index= index+tagLength;
            }
        }while(index>0);
        return in;
    }

    public void fixTypos(ArrayList<Typo> typoList){
       // StringBuilder _log=new StringBuilder();
        if(getContent()!=null){
         for (Typo typo:typoList)
              fixTypos(typo,log);
         // if(!log.isEmpty())


        }

    }
    public void fixTypos(Typo typo,StringBuilder _log){
        if(Typo.SCOPE_TEXT.equals(typo.scope)){
            String text= getText();
            _pushLog(typo,text,_log);
            replaceAllInTexts(typo.find,typo.replace);
        }
        if(Typo.SCOPE_TEMPLATE.equals(typo.scope)){
            String templates= getTemplates();
            _pushLog(typo,templates,_log);
            replaceAllInTemplates(typo.find, typo.replace);
        }

    }
    public void pushTitleLog(String revNo){
         String revStr="([[Special:Diff/revNo|فَرَق]])".replaceAll("revNo",revNo);
        log.insert(0,"\n==== [["+getTitle()+"]] - "+revStr+" ====")
        ;
    }
    private void _pushLog(Typo typo,String text, StringBuilder _log){
       Matcher matcher= typo.getMatcher(text);
        if (matcher.find()){
           String group= matcher.group();
            _log.append("\n* <nowiki> ")
                    .append(group)
                    .append(" </nowiki><b> -> </b><nowiki>")
                    .append(group.replaceAll(typo.find,typo.replace))
                    .append("</nowiki>")
            ;
        }
    }
   public void setLogObject(StringBuilder log){
        this.log=log;
   }
   public  StringBuilder getLogObject(){
        return log;
   }
    public String getLog(){
        return log.toString();
    }
    public void clearLog(){
        log.setLength(0);
    }
    public void replaceAllInTexts(String regex,String replacement){
        replaceAllInSections(textsList,regex,replacement);
    }
    public void replaceAllInTemplates(String regex,String replacement){
        replaceAllInSections(templatesList,regex,replacement);
    }
   public void replaceAllInSections(ArrayList<Section> sections,String regex,String replacement){
       for (Section s:sections){
           if(s.text!=null)
            s.text= s.text.replaceAll(regex,replacement);
       }
   }
    public ArrayList<Section> getSectionsList() {
        return sectionsList;
    }

    public String getContent(){
        return  listToString(sectionsList);
    }
    public String getText(){
       return  listToString(textsList);
    }
    public String getLinks(){
        return  listToString(linksList);
    }
    public String getTemplates(){
        return  listToString(templatesList);
    }

    private String listToString(ArrayList<Section> list){
        StringBuilder sb=new StringBuilder();
        for (Section s: list) {
                sb.append(s.text);
               // sb.append("\n");
        }
        return  sb.toString();
    }


}
