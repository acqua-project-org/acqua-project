package mjmisc;

import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.OutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

public class GZip{
    public static void ungzip(String inFilename, String outFilename) throws IOException{
        
        GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inFilename));

        OutputStream out = new FileOutputStream(outFilename);

        byte[] buf = new byte[102400]; //size can be changed according to programmer's need.
        int len;
        String ret = "";
        while ((len = gzipInputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
            //ret = ret + new String(buf, 0, len);
        }
        //return ret;

    }
}

