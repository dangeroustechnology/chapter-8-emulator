import java.awt.*;

public class Ch8_display extends Canvas{
    // https://docs.oracle.com/javase/7/docs/api/java/awt/Canvas.html
    // https://cs.lmu.edu/~ray/notes/javagraphics/
    Color WHITE = Color.WHITE;
    Color BLACK = Color.BLACK;
    private int w = 64;
    private int h = 32;
    private int res_scale = 1;
    private byte[][] vram = new byte[w * 2][h * 2];  // essentially a bitmap

    public Ch8_display() { }

    public void draw_sprite(int x, int y, int n, int[] mem, int addr) {
        int xpos = x;
        int ypos = y;
        xpos &= 0xFF;
        xpos %= w;
        ypos &= 0xFF;
        ypos %= h;
        for (int i = 0; i < n; i++) {
            byte bmp = (byte) (mem[addr + i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                int xp = xpos + j;
                int yp = ypos + i;
                if (xp < w * res_scale && yp < h * res_scale) {
                    vram[xp][yp] ^= (bmp >> (7 - j)) & 0x01;
                }
            }
        }
        super.repaint();  // update screen whenever a sprite is drawn
    }

    public void init() {
        for (int i = 0; i < w * 2; i++) {
            for (int j = 0; j < h * 2; j++) {
                vram[i][j] = 0;
            }
        }
    }

    public void paint(Graphics g) {
        super.setBackground(WHITE);
        g.setColor(BLACK);
        for (int i = 0; i < w * res_scale; i++) {
            for (int j = 0; j < h * res_scale; j++) {
                if (vram[i][j] == 1) {
                    g.fillRect(i * (8/res_scale), j * (8/res_scale), 8/res_scale, 8/res_scale);
                }
            }
        }
    }

    public int get_scale() {
        return res_scale;
    }

    public byte[][] get_vram() {
        return vram;
    }

    public void set_scale(int n) {
        res_scale = n;
        super.repaint();  // update screen whenever resolution is changed
    }

    public void set_vram(byte[][] very_ram) {
        vram = very_ram;
    }
}
