import java.util.Random;
import javax.swing.JFrame;

public class Ch8_Emulator {
    private final int[] memory = new int[4096]; // 4k RAM
    private final int[] V = new int[16];        // 8-bit registers, V0-F
    private int I = 0;                          // 16-bit address register
    private int DT = 0;                         // 8-bit delay timer
    private int ST = 0;                         // 8-bit sound timer
    private int PC = 0;                         // 16-bit program counter
    private int SP = 0;                         // 8-bit stack pointer
    private final int[] stack = new int[32];    // 16-bit stack
    private final Ch8_reader fr = new Ch8_reader();
    private final Ch8_display display = new Ch8_display();
    private final Ch8_input keypad = new Ch8_input(display);
    JFrame frame = new JFrame("Chapter 8 Display");
    Random rand = new Random();
    String filepath;
    boolean play = true;                        // should the emulator run?

    public Ch8_Emulator() { }

    public void exec_ins() {    // TODO: 0x00CN, FB, FC; 0xFX0A
        int ins = read_program();
        int msb = (ins >> 12) & 0x0F;
        int lsb = ins & 0x0F;
        int hi_byte = (ins >> 8) & 0xFF;
        int hb_lsb = hi_byte & 0x0F;
        int lo_byte = ins & 0xFF;
        if (hi_byte == 0x00) {
            if ((ins >> 4 & 0x0F) == 0x0C) {    // 0x00CN: scroll down N pixels (SCHIP)
                System.out.printf("SCROLL %x \n", ins & 0x0F);
            } else if (lo_byte == 0xE0) {       // 0x00E0: clear screen
                System.out.println("CLR");
            } else if (lo_byte == 0xEE) {       // 0x00EE: return from subroutine
                SP--;
                PC = stack[SP];
                System.out.println("RTN");
            } else if (lo_byte == 0xFB) {       // 0x00FB: scroll right 4 pixels (SCHIP)
                System.out.println("SCROLL R 4");
            } else if (lo_byte == 0xFC) {       // 0x00FC: scroll left 4 pixels (SCHIP)
                System.out.println("SCROLL L 4");
            } else if (lo_byte == 0xFD) {       // 0x00FD: "exit" program (SCHIP)
                init_emu(filepath);  // just restarts instead
                System.out.println("EXIT");
            } else if (lo_byte == 0xFE) {       // 0x00FE: enter 64x32 mode (SCHIP)
                display.set_scale(1);
                System.out.println("LORES");
            } else if (lo_byte == 0xFF) {       // 0x00FF: enter 128x64 mode (SCHIP)
                display.set_scale(2);
                System.out.println("HIRES");
            } else {
                System.out.printf("ERR unknown 0 cmd: %x \n", ins);
            }
        } else if (msb == 0x01) {               // 0x1NNN: jump to 0x0NNN
            PC = (short) (ins & 0x0FFF);
            System.out.printf("JMP %x \n", ins & 0x0FFF);
        } else if (msb == 0x02) {               // 0x2NNN: jump to subroutine at 0x0NNN
            stack[SP++] = PC;
            PC = (short) (ins & 0x0FFF);
            System.out.printf("JSR %x \n", ins & 0x0FFF);
        } else if (msb == 0x03) {               // 0x3XNN: if (V[X] != NN) ...
            if (V[hb_lsb] == (lo_byte)) {
                PC += 2;
            }
            System.out.printf("IF V%x != %x \n", hb_lsb, lo_byte);
        } else if (msb == 0x04) {               // 0x4XNN: if (V[X] == NN) ...
            if (V[hb_lsb] != (lo_byte)) {
                PC += 2;
            }
            System.out.printf("IF V%x == %x \n", hb_lsb, lo_byte);
        } else if (msb == 0x05) {               // 0x5XY0: if (V[X] != V[Y]) ...
            if (V[hb_lsb] == V[(ins >> 4) & 0x0F]) {
                PC += 2;
            }
            System.out.printf("IF V%x != V%x \n", hb_lsb, (ins >> 4) & 0x0F);
        } else if (msb == 0x06) {               // 0x6XNN: V[X] = NN
            V[hb_lsb] = (byte) (lo_byte & 0xFF);
            System.out.printf("LDX %x %x \n", hb_lsb, lo_byte);
        } else if (msb == 0x07) {               // 0x7XNN: V[X] += NN
            V[hb_lsb] += lo_byte;
            System.out.printf("ADX %x %x \n", hb_lsb, lo_byte);
        } else if (msb == 0x08) {
            if (lsb == 0x00) {                  // 0x8XY0: V[X] = V[Y]
                V[hb_lsb] = (byte) ((ins >> 4) & 0x0F);
                System.out.printf("LDX %x V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x01) {           // 0x8XY1: V[X] |= V[Y]
                V[hb_lsb] |= V[(ins >> 4) & 0x0F];
                System.out.printf("V%x |= V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x02) {           // 0x8XY2: V[X] &= V[Y]
                V[hb_lsb] &= V[(ins >> 4) & 0x0F];
                System.out.printf("V%x &= V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x03) {           // 0x8XY3: V[X] ^= V[Y]
                V[hb_lsb] ^= V[(ins >> 4) & 0x0F];
                System.out.printf("V%x ^= V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x04) {           // 0x8XY4: V[x] += V[Y] with carry
                byte carry = 0;
                V[hb_lsb] += V[(ins >> 4) & 0x0F];
                int v1, v2;
                v1 = V[hb_lsb];
                v2 = V[(ins >> 4) & 0x0F];
                if (v1 > (0xFF - v2)) {  // if a > (255 - b) then a + b > 255
                    carry = 1;
                }
                V[0x0F] = carry;
                System.out.printf("V%x += V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x05) {           // 0x8XY5: V[X] -= V[Y] with borrow
                V[hb_lsb] -= V[(ins >> 4) & 0x0F];
                if (V[hb_lsb] > V[(ins >> 4) & 0x0F]) {
                    V[0x0F] = 0;
                }
                System.out.printf("V%x -= V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x06) {           // 0x8XY6: V[X] >>= V[Y] with carry
                int vy = V[(ins >> 4) & 0x0F];
                int carry = vy & 0x01;
                V[0x0F] = carry;
                V[hb_lsb] >>= vy;
                System.out.printf("V%x >>= V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x07) {           // 0x8XY7: V[X] = V[Y] - V[X] with borrow
                System.out.printf("V%x = V%x - V%x \n", hb_lsb, hb_lsb, (ins >> 4) & 0x0F);
            } else if (lsb == 0x0E) {           // 0x8XYE: V[X] <<= V[Y] with carry
                int vy = V[(ins >> 4) & 0x0F];
                int carry = (vy >> 15) & 0x01;
                V[0x0F] = carry;
                V[hb_lsb] <<= vy;
                System.out.printf("V%x <<= V%x \n", hb_lsb, (ins >> 4) & 0x0F);
            } else {
                System.out.printf("ERR unknown 8 cmd: %x \n", ins);
            }
        } else if (msb == 0x09) {               // 0x9XY0: if (V[X] != V[Y]) then skip
            if (V[hb_lsb] != V[(ins >> 4) & 0x0F]) {
                PC += 2;
            }
            System.out.printf("IF V%x == V%x \n", hb_lsb, (ins >> 4) & 0x0F);
        } else if (msb == 0x0A) {               // 0xANNN: I = NNN
            I = ins & 0x0FFF;
            System.out.printf("LDI %x \n", ins & 0x0FFF);
        } else if (msb == 0x0B) {               // 0xBNNN: jump to NNN + V[0]
            PC = (short) ((ins & 0x0FFF) + V[0]);
            System.out.printf("JMP %x + V0 \n", ins & 0x0FFF);
        } else if (msb == 0x0C) {               // 0xCXNN: V[X] = random(255) & 0xNN
            V[hb_lsb] = (byte) (rand.nextInt(256) & lo_byte);
            System.out.printf("LDX %x (RND 255 & %x) \n", hb_lsb, lo_byte);
        } else if (msb == 0x0D) {               // 0xDXYN: draw sprite at V[X], V[Y] with N bytes data starting at I
            byte xpos = (byte) (hb_lsb);
            byte ypos = (byte) ((ins >> 4) & 0x0F);
            display.draw_sprite(V[xpos], V[ypos], lsb, memory, I);
            System.out.printf("DRW %x %x %x \n", xpos, ypos, lsb);
        } else if (msb == 0x0E) {
            if (lo_byte == 0x9E) {              // 0xEX9E: if (key = V[X]) is pressed, skip
                if (display.get_keyp() == V[hb_lsb]) {
                    PC += 2;
                }
                System.out.printf("SKIP IF key == V%x \n", hb_lsb);
            } else if (lo_byte == 0xA1) {       // 0xEXA1: if (key = V[X]) not pressed, skip
                if (display.get_keyp() != V[hb_lsb]) {
                    PC += 2;
                }
                System.out.printf("SKIP IF key != V%x \n", hb_lsb);
            } else {
                System.out.printf("ERR unknown E cmd: %x \n", ins);
            }
        } else if (msb == 0x0F) {
            if (lo_byte == 0x07) {              // 0xFX07: V[X] = DT
                V[hb_lsb] = DT;
                System.out.printf("LDX %x DT \n", hb_lsb);
            } else if (lo_byte == 0x0A) {       // 0xFX0A: wait for key press, store it in V[X]
                if (display.get_keyp() != 0xFF) {
                    V[hb_lsb] = display.get_keyp();
                } else {
                    PC -= 2;
                }
                System.out.printf("WAIT %x key \n", hb_lsb);
            } else if (lo_byte == 0x15) {       // 0xFX15: DT = V[X]
                DT = V[hb_lsb];
                System.out.printf("LDX DT V%x \n", hb_lsb);
            } else if (lo_byte == 0x18) {       // 0xFX18: ST = V[X]
                ST = V[hb_lsb];
                System.out.printf("LDX ST V%x \n", hb_lsb);
            } else if (lo_byte == 0x1E) {       // 0xFX1E: I += V[X]
                I += V[hb_lsb];
                System.out.printf("ADI V%x \n", hb_lsb);
            } else if (lo_byte == 0x29) {       // 0xFX29: I = mem. addr. of sprite of hex digit in V[X]
                I = font_lookup(V[hb_lsb]);
                System.out.printf("LDI $V%x (0xFX29)\n", hb_lsb);
            } else if (lo_byte == 0x33) {       // 0xFX33: I, I+1, and I+2 = BCD V[X]
                int bite = V[hb_lsb] & 0xFF;
                int high = (int) (Math.floor((bite / 100)) % 10); // abc.0 -> a.bc -> a
                int mid = (int) (Math.floor((bite / 10)) % 10); // abc.0 -> ab.c -> ab -> b
                int low = (bite % 10); // abc -> c
                memory[I] = high & 0xFF;
                memory[I+1] = mid & 0xFF;
                memory[I+2] = low & 0xFF;
                System.out.printf("BCD V%x -> %d, %d, %d \n", hb_lsb, high, mid, low);
            } else if (lo_byte == 0x55) {       // 0xFX55: I, I+1, ... I+X = V[0], V[1], ... V[X]; I += X + 1
                for (int i = 0; i <= (hb_lsb); i++) {
                    memory[I+i] = V[i];
                }
                I += (hb_lsb) + 1;
                System.out.printf("SAVE V%x \n", hb_lsb);
            } else if (lo_byte == 0x65) {       // 0xFX65: V[0], V[1], ... V[X] = I, I+1, ... I+X; I += X + 1
                for (int i = 0; i <= (hb_lsb); i++) {
                    V[i] = memory[I+i];
                }
                I += (hb_lsb) + 1;
                System.out.printf("LOAD V%x \n", hb_lsb);
            } else {
                System.out.printf("ERR unknown F cmd: %x \n", ins);
            }
        } else {
            System.out.printf("ERR unknown cmd: %x \n", ins);
        }
    }

    public int font_lookup(int digit) {
        int base = 0x0050;
        return switch (digit) {
            case 0x01 -> base + 5;
            case 0x02 -> base + 10;
            case 0x03 -> base + 15;
            case 0x04 -> base + 20;
            case 0x05 -> base + 25;
            case 0x06 -> base + 30;
            case 0x07 -> base + 35;
            case 0x08 -> base + 40;
            case 0x09 -> base + 45;
            case 0x0A -> base + 50;
            case 0x0B -> base + 55;
            case 0x0C -> base + 60;
            case 0x0D -> base + 65;
            case 0x0E -> base + 70;
            case 0x0F -> base + 75;
            default -> base;
        };
    }

    public void init_emu(String file) {
        // reset all registers and memory
        I = 0;
        DT = 0;
        ST = 0;
        PC = 512;  // start of PRG-RAM
        SP = 0;
        for (int i = 0; i < V.length; i++) {
            V[i] = 0;
        }
        for (int i = PC; i < memory.length; i++) {
            memory[i] = 0;
        }
        if (display.getKeyListeners().length == 0) {
            display.addKeyListener(keypad);
        }
        display.init();
        start_display();

        fr.read_file(memory, file);  // load PRG-RAM
        filepath = file;
        load_font("C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\chp8-font-hex.ch8");
    }

    public boolean is_playing() {
        return play;
    }

    public void load_font(String font_file) {
        fr.read_file(memory, font_file, 0x50);
    }

    public int read_program() {
        int ins_1 = memory[PC++];
        int ins_2 = memory[PC++];
        return ((ins_1 << 8) | (ins_2 & 0x00FF)) & 0xFFFF;
    }

    public void start_display() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(display);
        frame.setSize(512, 256);
        frame.setVisible(true);
    }

    public void step_emu() {
        exec_ins();
        if (DT > 0) {
            DT--;
        }
        if (ST > 0) {
            //play_sound()  // not implemented - java audio system is garbo
            ST--;
        }
    }

    public void toggle_display() {
        frame.setVisible(!frame.isVisible());
    }

    public int[] get_memory() { return memory.clone(); }

    public int get_pc() { return PC; }

    public Ch8_display get_display() { return display; }

    public Ch8_reader get_reader() { return fr; }
}
