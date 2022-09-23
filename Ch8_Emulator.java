import java.util.Random;

public class Ch8_Emulator {
    private byte[] memory = new byte[4096]; // 4k RAM
    private byte[] V = new byte[16];        // 8-bit registers, V0-F
    private short I = 0;                    // 16-bit address register
    private byte DT = 0;                    // 8-bit delay timer
    private byte ST = 0;                    // 8-bit sound timer
    private short PC = 0;                   // 16-bit program counter
    private byte SP = 0;                    // 8-bit stack pointer
    private short[] stack = new short[32];  // 16-bit stack
    private Ch8_reader fr = new Ch8_reader();
    Random rand = new Random();

    public Ch8_Emulator() { }

    public void init_emu(String file) {
        // reset all registers
        I = 0;
        DT = 0;
        ST = 0;
        PC = 512;   // start of PRG-RAM
        SP = 0;
        for (byte b : V) {
            b = 0;
        }
        for (int i = PC; i < memory.length; i++) {
            memory[i] = 0;
        }

        // load PRG-RAM
        fr.read_file(memory, file);
        memory[0] = 1;
    }

    public short read_program() {
        short ins_1 = memory[PC++];
        short ins_2 = memory[PC++];
        short ins = (short) ((ins_1 << 8) | (ins_2 & 0x00FF));
        return ins;
    }

    public void step_emu() {    // TODO: all SCHIP; 0xD; 0xE; 0xF0A, 29, 33
        short ins = read_program();
        if (ins >>> 8 == 0x00) {
            if ((ins >> 4 & 0x0F) == 0x0C) {        // 0x00CN: scroll down N pixels (SCHIP)
                System.out.printf("SCROLL %x \n", ins & 0x0F);
            } else if ((ins & 0xFF) == 0xE0) {      // 0x00E0: clear screen
                System.out.println("CLR");
            } else if ((ins & 0xFF) == 0xEE) {      // 0x00EE: return from subroutine
                SP--;
                PC = stack[SP];
                System.out.println("RTN");
            } else if ((ins & 0xFF) == 0xFB) {      // 0x00FB: scroll right 4 pixels (SCHIP)
                System.out.println("SCROLL R 4");
            } else if ((ins & 0xFF) == 0xFC) {      // 0x00FC: scroll left 4 pixels (SCHIP)
                System.out.println("SCROLL L 4");
            } else if ((ins & 0xFF) == 0xFD) {      // 0x00FD: exit program (SCHIP)
                System.out.println("EXIT");
            } else if ((ins & 0xFF) == 0xFE) {      // 0x00FE: enter 64x32 mode (SCHIP)
                System.out.println("LORES");
            } else if ((ins & 0xFF) == 0xFF) {      // 0x00FF: enter 128x64 mode (SCHIP)
                System.out.println("HIRES");
            } else {
                System.out.printf("ERR unknown 0 cmd: %x \n", ins);
            }
        } else if (ins >>> 12 == 0x01) {            // 0x1NNN: jump to 0x0NNN
            PC = (short) (ins & 0x0FFF);
            System.out.printf("JMP %x \n", ins & 0x0FFF);
        } else if (ins >>> 12 == 0x02) {            // 0x2NNN: jump to subroutine at 0x0NNN
            stack[SP++] = PC;
            PC = (short) (ins & 0x0FFF);
            System.out.printf("JSR %x \n", ins & 0x0FFF);
        } else if (ins >>> 12 == 0x03) {            // 0x3XNN: if (V[X] != NN) ...
            if (V[(ins >> 8) & 0x0F] == (ins & 0x00FF)) {
                PC += 2;
            }
            System.out.printf("IF V%x != %x \n", (ins >> 8) & 0x0F, ins & 0x00FF);
        } else if (ins >>> 12 == 0x04) {            // 0x4XNN: if (V[X] == NN) ...
            if (V[(ins >> 8) & 0x0F] != (ins & 0x00FF)) {
                PC += 2;
            }
            System.out.printf("IF V%x == %x \n", (ins >> 8) & 0x0F, ins & 0x00FF);
        } else if (ins >>> 12 == 0x05) {            // 0x5XY0: if (V[X] != V[Y]) ...
            if (V[(ins >> 8) & 0x0F] == V[(ins >> 4) & 0x0F]) {
                PC += 2;
            }
            System.out.printf("IF V%x != V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
        } else if (ins >>> 12 == 0x06) {            // 0x6XNN: V[X] = NN
            V[(ins >> 8) & 0x0F] = (byte) (ins & 0x00FF);
            System.out.printf("LDX %x %x \n", (ins >> 8) & 0x0F, ins & 0x00FF);
        } else if (ins >>> 12 == 0x07) {            // 0x7XNN: V[X] += NN
            V[(ins >> 8) & 0x0F] += ins & 0x00FF;
            System.out.printf("ADX %x %x \n", (ins >> 8) & 0x0F, ins & 0x00FF);
        } else if (ins >>> 12 == 0x08) {
            if ((ins & 0x0F) == 0x00) {             // 0x8XY0: V[X] = V[Y]
                V[(ins >> 8) & 0x0F] = (byte) ((ins >> 4) & 0x0F);
                System.out.printf("LDX %x V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x01) {      // 0x8XY1: V[X] |= V[Y]
                V[(ins >> 8) & 0x0F] |= V[(ins >> 4) & 0x0F];
                System.out.printf("V%x |= V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x02) {      // 0x8XY2: V[X] &= V[Y]
                V[(ins >> 8) & 0x0F] &= V[(ins >> 4) & 0x0F];
                System.out.printf("V%x &= V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x03) {      // 0x8XY3: V[X] ^= V[Y]
                V[(ins >> 8) & 0x0F] ^= V[(ins >> 4) & 0x0F];
                System.out.printf("V%x ^= V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x04) {      // 0x8XY4: V[x] += V[Y] with carry
                byte carry = 0;
                V[(ins >> 8) & 0x0F] += V[(ins >> 4) & 0x0F];
                byte v1, v2;
                v1 = V[(ins >> 8) & 0x0F];
                v2 = V[(ins >> 4) & 0x0F];
                if (v1 > 0xFF - v2) {  // if a > (255 - b) then a + b > 255
                    carry = 1;
                }
                V[0x0F] = carry;
                System.out.printf("V%x += V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x05) {      // 0x8XY5: V[X] -= V[Y] with borrow
                V[(ins >> 8) & 0x0F] -= V[(ins >> 4) & 0x0F];
                if (V[(ins >> 8) & 0x0F] > V[(ins >> 4) & 0x0F]) {
                    V[0x0F] = 0;
                }
                System.out.printf("V%x -= V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x06) {      // 0x8XY6: V[X] >>= V[Y] with carry
                short vy = V[(ins >> 4) & 0x0F];
                byte carry = (byte) (vy & 0x01);
                V[0x0F] = carry;
                V[(ins >> 8) & 0x0F] >>= vy;
                System.out.printf("V%x >>= V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x07) {      // 0x8XY7: V[X] = V[Y] - V[X] with borrow
                System.out.printf("V%x = V%x - V%x \n", (ins >> 8) & 0x0F, (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else if ((ins & 0x0F) == 0x0E) {      // 0x8XYE: V[X] <<= V[Y] with carry
                short vy = V[(ins >> 4) & 0x0F];
                byte carry = (byte) (vy >> 15 & 0x01);
                V[0x0F] = carry;
                V[(ins >> 8) & 0x0F] <<= vy;
                System.out.printf("V%x <<= V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
            } else {
                System.out.printf("ERR unknown 8 cmd: %x \n", ins);
            }
        } else if (ins >>> 12 == 0x09) {            // 0x9XY0: if (V[X] != V[Y]) then skip
            if (V[(ins >> 8) & 0x0F] != V[(ins >> 4) & 0x0F]) {
                PC += 2;
            }
            System.out.printf("IF V%x == V%x \n", (ins >> 8) & 0x0F, (ins >> 4) & 0x0F);
        } else if (((ins >> 12) & 0x0F) == 0x0A) {  // 0xANNN: I = NNN
            I = (short) (ins & 0x0FFF);
            System.out.printf("LDI %x \n", ins & 0x0FFF);
        } else if (((ins >> 12) & 0x0F) == 0x0B) {  // 0xBNNN: jump to NNN + V[0]
            PC = (short) ((ins & 0x0FFF) + V[0]);
            System.out.printf("JMP %x + V0 \n", ins & 0x0FFF);
        } else if (((ins >> 12) & 0x0F) == 0x0C) {  // 0xCXNN: V[X] = random(255) & 0xNN
            V[(ins >> 8) & 0x0F] = (byte) (rand.nextInt(256) & (ins & 0xFF));
            System.out.printf("LDX %x (RND 255 & %x) \n", (ins >> 8) & 0x0F, ins & 0xFF);
        } else if (((ins >> 12) & 0x0F) == 0x0D) {  // 0xDXYN: draw sprite at V[X], V[Y] with N bytes sprite starting at I
            System.out.printf("DRW %x %x %x \n", (ins >> 8) & 0xFF, (ins >> 4) & 0x0F, ins & 0x0F);
        } else if (((ins >> 12) & 0x0F) == 0x0E) {
            if ((ins & 0xFF) == 0x9E) {             // 0xEX9E: if (key = V[X]) is pressed, skip
                System.out.printf("SKIP IF key == V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0xA1) {      // 0xEXA1: if (key = V[X]) not pressed, skip
                System.out.printf("SKIP IF key != V%x \n", (ins >> 8) & 0x0F);
            } else {
                System.out.printf("ERR unknown E cmd: %x \n", ins);
            }
        } else if (((ins >> 12) & 0x0F) == 0x0F) {
            if ((ins & 0xFF) == 0x07) {             // 0xFX07: V[X] = DT
                V[(ins >> 8) & 0x0F] = DT;
                System.out.printf("LDX %x DT \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x0A) {      // 0xFX0A: wait for key press, store it in V[X]
                System.out.printf("WAIT %x key \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x15) {      // 0xFX15: DT = V[X]
                DT = V[(ins >> 8) & 0x0F];
                System.out.printf("LDX DT V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x18) {      // 0xFX18: ST = V[X]
                ST = V[(ins >> 8) & 0x0F];
                System.out.printf("LDX ST V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x1E) {      // 0xFX1E: I += V[X]
                I += V[(ins >> 8) & 0x0F];
                System.out.printf("ADI V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x29) {      // 0xFX29: I = mem. addr. of sprite of hex digit in V[X]
                System.out.printf("LDI $V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x33) {      // 0xFX33: I, I+1, and I+2 = BCD V[X]
                byte bite = V[(ins >> 8) & 0x0F];
                byte high = (byte) (Math.floor((bite / 100)) % 10); // abc.0 -> a.bc -> a.0 -> a
                byte mid = (byte) (Math.floor((bite / 10)) % 10); // abc.0 -> ab.c -> ab.0 -> b
                byte low = (byte) (bite % 10); // abc -> c
                memory[I] = high;
                memory[I+1] = mid;
                memory[I+2] = low;
                System.out.printf("BCD V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x55) {      // 0xFX55: I, I+1, ... I+X = V[0], V[1], ... V[X]; I += X + 1
                for (int i = 0; i <= ((ins >> 8) & 0x0F); i++) {
                    memory[I+i] = V[i];
                }
                I += ((ins >> 8) & 0x0F) + 1;
                System.out.printf("SAVE V%x \n", (ins >> 8) & 0x0F);
            } else if ((ins & 0xFF) == 0x65) {      // 0xFX65: V[0], V[1], ... V[X] = I, I+1, ... I+X; I += X + 1
                for (int i = 0; i <= ((ins >> 8) & 0x0F); i++) {
                    V[i] = memory[I+i];
                }
                I += ((ins >> 8) & 0x0F) + 1;
                System.out.printf("LOAD V%x \n", (ins >> 8) & 0x0F);
            } else {
                System.out.printf("ERR unknown F cmd: %x \n", ins);
            }
        } else {
            System.out.printf("ERR unknown cmd: %x \n", ins);
        }
    }

    public byte[] get_memory() { return memory.clone(); }

    public short get_pc() { return PC; }

    public Ch8_reader get_reader() { return fr; }
}
