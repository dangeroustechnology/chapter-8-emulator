import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int hz = 60;
        int framerate = 1000/hz;
        String octo1 = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\octojam1title.ch8";
        String ibm = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\IBM_logo.ch8";
        String testop = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\test_opcode.ch8";
        String file = "";
        if (args.length != 0) {
            file = args[0];
        } else {
            file = ibm;
        }
        Ch8_Emulator emulator = new Ch8_Emulator();
        emulator.init_emu(file);
        while (emulator.is_playing()) {
            if ((System.currentTimeMillis() - start) % framerate < 5) {
                emulator.step_emu();
            }
        }
    }
}