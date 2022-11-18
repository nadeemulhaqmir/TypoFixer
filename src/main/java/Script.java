public class Script {
    public  long
            arabic
            ,devanagri
            ,sharda
            ,latin
            ,other
            ;
    public Script(){
       reset();
    }
    public void read(String text){  // https://www.unicode.org/charts/
        int code;
        if(text!=null){
        char[] chars=text.toCharArray();
          for (int i=0;i<chars.length;i++){
              code=chars[i];
              if(code >=0X0600 && code<=0X06FF) // ARABIC
                  arabic++;
             else if(code >=0X0900 && code<=0X097F) // DEVANAGRI
                  devanagri++;
             else if(code >=0X0000 && code<=0X007F) // Basic Latin (ASCII)
                  latin++;
             else
                 other++;
          }
        }
        long sum=arabic+sharda+devanagri+latin+other;
        arabic= (100*arabic/sum);
        sharda= (100*sharda/sum);
        devanagri= (100*devanagri/sum);
        latin= (100*latin/sum);
        other= (100*other/sum);
    }
    public void reset(){
        arabic=devanagri=sharda=latin=other=0;  //percentage
    }

    @Override
    public String toString() {
        return "Script{" +
                "arabic=" + arabic +
                ", devanagri=" + devanagri +
                ", sharda=" + sharda +
                ", latin=" + latin +
                ", other=" + other +
                '}';
    }
}
