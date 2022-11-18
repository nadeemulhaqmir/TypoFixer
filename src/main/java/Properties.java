import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Properties extends java.util.Properties {
    private  File file;
    private FileInputStream fis;
    private FileOutputStream fos;
    public Properties(File f) throws IOException{
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

}
