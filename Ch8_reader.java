import java.io.*;

public class Ch8_reader {
    byte[] memory;

    public Ch8_reader() {

    }

    public void read_file(byte[] array, String path) {
        memory = array;
        try {
            InputStream in = new FileInputStream(path);
            int read = -1;
            int mem = 512;  // program ram starts at 0x200 = 512
            while ((read = in.read()) != -1) {
                memory[mem] = (byte) read;
                mem++;
            }
            in.close();
        } catch (IOException e) {
            System.out.println("404 or unexpected EOF");
        }
    }

    public void dump_file(String inpath, String outpath) {
        try {
            InputStream in = new FileInputStream(inpath);
            OutputStream out = new FileOutputStream(outpath);
            int read = -1;
            int i = 0;
            while ((read = in.read()) != -1) {
                out.write(read);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("404 or unexpected EOF");
        }
    }
}
