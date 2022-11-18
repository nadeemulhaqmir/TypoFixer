import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Page {
    private ArrayList<Section> sectionsList;
    private ArrayList<Section> textsList;
    private ArrayList<Section> templatesList;
    private ArrayList<Section> linksList;

    public Page(String text) throws Exception{
        sectionsList=new ArrayList<>();
        textsList=new ArrayList<>();
        templatesList =new ArrayList<>();
        linksList =new ArrayList<>();

        String [] regexs= new String[]{
                "(\\[\\[(.*|\s.*)\\]\\])"
                ,"(\\{\\{.*|\s|.*\\}\\})"
               // ,"(?s)(\\{\\{.*\\}\\})"
                };
        int[] types=new int[]{
                Section.TYPE_LINK
                ,Section.TYPE_TEMPLATE
                };
        sectionsList.add(new Section(text,Section.TYPE_TEXT));
        Section section;
        ArrayList<Section> _sections;
        int size;
        for(int x=0;x< regexs.length;x++){
            for(int i=0;i< sectionsList.size();i++){
                section=sectionsList.get(i);
                if(section.type==Section.TYPE_TEXT){
                 _sections=parseSections(section.text,regexs[x],types[x],Section.TYPE_TEXT);
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

    private ArrayList<Section> parseSections(String text, String regex, int type, int defaultType) {
        ArrayList<Section> _sections = new ArrayList<>();
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
                _sections.add(new Section(text.substring(prev, start), defaultType));
            _sections.add(new Section(text.substring(start, end), type));
            prev = end;
        }
        if (end < len)  // Default
            _sections.add(new Section(text.substring(end, len), defaultType));

        return _sections;
    }

    public void replaceAllInTexts(String regex,String replacement){
        for (Section s:textsList)
            s.text= s.text.replaceAll(regex,replacement);
    }
    public void replaceAllInTemplates(String regex,String replacement){
        for (Section s:templatesList)
            s.text= s.text.replaceAll(regex,replacement);
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
