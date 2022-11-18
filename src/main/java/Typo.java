import org.wikipedia.Wiki;

import java.util.List;
import java.util.regex.Pattern;

public   class Typo{
    public  String
            word
            ,find
            ,replace
            ,scope
            ;
    public Pattern pattern;
    public final static String
                SCOPE_TEXT="TEXT"
                ,SCOPE_TEMPLATE="TEMPLATE"
                ,SCOPE_LINK="LINK"
                ;
    public Typo(String word, String find, String replace) {
        this.word = word;
        this.find = find;
        this.replace = replace;
        pattern=Pattern.compile(find);
    }
    public Typo(String word, String find, String replace,String scope,Pattern pattern){
        if(scope==null || scope.length()==0)
            scope=SCOPE_TEXT;
         else
             scope=scope.toUpperCase();
        this.word = word;
        this.find = find;
        this.replace = replace;
        this.scope=scope;
        this.pattern=pattern; //Pattern.compile(find);
    }

    @Override
    public String toString() {
        return "Typo{" +
                "word='" + word + '\'' +
                ", find='" + find + '\'' +
                ", replace='" + replace + '\'' +
                '}';
    }

    public static Pattern getPatternFrom(String regex){
        Pattern pattern=null;
         try{
            pattern= Pattern.compile(regex);
         }
         catch (Exception e){

         }
         return  pattern;

    }
}

