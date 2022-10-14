import java.io.*;

public class Ch8_reader {
    int[] memory;

    public Ch8_reader() {

    }

    public void read_file(int[] array, String path) {
        memory = array;
        try {
            InputStream in = new FileInputStream(path);
            int read;
            int mem = 512;  // program ram starts at 0x200 = 512
            while ((read = in.read()) != -1) {
                memory[mem] = read & 0xFF;
                mem++;
            }
            in.close();
        } catch (IOException e) {
            System.out.println("404 or unexpected EOF");
        }
    }

    public void read_file(int[] array, String path, int index) {
        memory = array;
        try {
            InputStream in = new FileInputStream(path);
            int read;
            int mem = index;  // program ram starts at 0x200 = 512
            while ((read = in.read()) != -1) {
                memory[mem] = read & 0xFF;
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
            int read;
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
