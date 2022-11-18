import org.w3c.dom.*;
import org.wikipedia.Wiki;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

     public static String getAttribute(String string,String attributeName){
         String res="";
         //Matcher matcher = Pattern.compile(attributeName+"=\"(.*?)\"(\s/>)")
         Matcher matcher = Pattern.compile(attributeName+"=\"(.*?)\"(\s|/>)")
                 .matcher(string);
         if (matcher.find()) {
             res= matcher.group(1);
         }
         return res;
     }
    public static ArrayList<Typo> extractTags(String text){
        StringBuilder sb=new StringBuilder();
        ArrayList<Typo> typos=new ArrayList<>();
        Matcher matcher = Pattern.compile("(?:<Typo.*?(\s|\")/>)")
                .matcher(text);
        String tag;
        while (matcher.find()) {
            tag=matcher.group();
            String find = Helper.getAttribute(tag,"find");
            String replace = Helper.getAttribute(tag,"replace");
            String word=Helper.getAttribute(tag,"word");
            String scope=Helper.getAttribute(tag,"scope");
            Pattern pattern = Typo.getPatternFrom(find);
            if (pattern != null)
                typos.add(new Typo(word, find, replace,scope, pattern));
        }
        return typos;
    }

    public static Map<String, String> parseCredentials(String s){

        HashMap<String,String> map=new HashMap<>();
        InputSource is;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            is = new InputSource(new FileInputStream(s));
            is.setEncoding("UTF-8");
            // builderFactory.setValidating(false);
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(is);
            NodeList list; //= document.getDocumentElement().getElementsByTagName("Typo");
             list=document.getDocumentElement().getChildNodes();
            int size= list.getLength();
            Node node;
            Element element;
            for(int i=0;i<size;i++){
                node=list.item(i);
                if(node instanceof Element){
                    element=(Element) node;
                    map.put(element.getTagName(),element.getTextContent());
                }

            }

        }
        catch (Exception e){
            System.out.println("Helper Parse Cred Error "+e);
        }
        return map;

    }

    public static ArrayList<Section> getLinks(String text)
    {
        ArrayList<String> al = new ArrayList<String>();
        ArrayList<Section> sections =new ArrayList<>();
        // text = removeCommentsAndNoWikiText(text);
        Pattern p = Pattern.compile("(\\[\\[(.*?)\\]\\])");
        Matcher m = p.matcher(text);
        int prev=0;
        int start=0;
        int end=0;
        int len=text.length();
        while (m.find()){
            start =m.start(1);
            end=m.end(1);
            if(start-prev>0)
                sections.add(new Section(text.substring(prev,start),false));
            sections.add( new Section(text.substring(start,end),true) );
             prev=end;
        }
        if(end<len)
             sections.add(new Section(text.substring(end,len),false));
        return sections;
    }




    public static ArrayList<Typo> parseStringToTypos(String s){
        ArrayList<Typo> typos=new ArrayList<>();
        InputSource is = new InputSource(new StringReader(s));
        is.setEncoding("UTF-8");
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
           // builderFactory.setValidating(false);
            builder = builderFactory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource( new ByteArrayInputStream( "".getBytes() ) );
                }
            });
            Document document = builder.parse(is);
            NodeList list= document.getDocumentElement().getElementsByTagName("Typo");
            int size= list.getLength();
            Node node;
            Element element;
            for(int i=0;i<size;i++){
                node=list.item(i);
                if(node instanceof Element){
                    element=(Element) node;
                    if(element.hasAttribute("find") && element.hasAttribute("replace")) {
                        String find = element.getAttribute("find");
                        String replace = element.getAttribute("replace");
                        String word=element.getAttribute("word");
                        Pattern pattern = Typo.getPatternFrom(find);
                        if (pattern != null)
                            typos.add(new Typo(word, find, replace,"", pattern));
                    }

                }

            }

        }
        catch (Exception e){
            System.out.println("Helper Error "+e);
        }
        return typos;

    }

    public static String listToString(List<String> list){
        StringBuilder sb=new StringBuilder();
        for(String s:list)
            sb.append(s);
        return sb.toString();

    }
    public static void printRevs(List<Wiki.Revision> list){
        for(Wiki.Revision r:list) {
            System.out.println(r.getTitle()+" "+r.getID());
        }
    }
    public static void printTypos(List<Typo> list){
        for(Typo t:list) {
            System.out.println(t.toString());
        }
    }
}
