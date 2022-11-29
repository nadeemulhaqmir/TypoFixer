import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Config extends java.util.Properties {
    private  File file;
    private FileInputStream fis;
    private FileOutputStream fos;

    //DEFAULT VALUES

    private final  String
            username=null
            ,password=null
            ;
    private final static int

            MAX_EDITS_PER_RUN=1
            ;

    private  final  boolean
             DEBUG=true
            ;



    public Config(File f) throws IOException{
        this.file=f;
    }
    public void load() throws IOException{
        this.fis=new FileInputStream(file);
        super.load(fis);
        fis.close();
    }
    public void store() throws IOException {
        this.fos=new FileOutputStream(file);
        super.store(fos,"");
        fos.close();
    }


    public int getMAX_EDITS_PER_RUN() {
        String val=getProperty("max_edits_per_run",String.valueOf(MAX_EDITS_PER_RUN));
        return Integer.parseInt(val);
    }

    public void setMAX_EDITS_PER_RUN(int MAX_EDITS_PER_RUN) {
       setProperty("max_edits_per_run",String.valueOf(MAX_EDITS_PER_RUN));
    }

    public boolean isDEBUG() {
        String val=getProperty("debug",String.valueOf(DEBUG));
        return Boolean.parseBoolean(val);
    }

    public void setDEBUG(boolean DEBUG) {
        setProperty("debug",String.valueOf(DEBUG));
    }



    public void setDEEP_SEARCH(boolean DEEP_SEARCH) {
       setProperty("deep_search",String.valueOf(DEEP_SEARCH));
    }

    public String getUsername() {
       return getProperty("username",username);
    }

    public void setUsername(String username) {
        setProperty("username",username);
    }

    public String getPassword() {
        return getProperty("password",password);
    }

    public void setPassword(String password) {
        setProperty("password",password);
    }
}
