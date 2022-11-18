public class Section {
    public String text;
    public boolean ISLINK;
    public int type;
    public final static  int
            TYPE_TEXT=0
            ,TYPE_LINK=1
            ,TYPE_TEMPLATE =2
            ,TYPE_COMMENT=3
            ;
    public Section (String text,boolean ISLINK){
        this.text=text;
        this.ISLINK=ISLINK;
    }
    public Section (String text,int type){
        this.text=text;
        this.type=type;
    }

    @Override
    public String toString() {
        return "Section{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
