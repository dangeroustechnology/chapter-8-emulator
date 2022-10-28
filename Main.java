public class Main {  // TODO: make animation good, implement sound?; TEST!

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        double hz = 120;
        double framerate = 1000/hz;
        String octo1 = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\octojam1title.ch8";
        String ibm = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\IBM_logo.ch8";
        String testop = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\test_opcode.ch8";
        String game = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\outlaw.ch8";
        String file;
        if (args.length != 0) {
            file = args[0];
        } else {
            file = octo1;
        }
        Ch8_Emulator emulator = new Ch8_Emulator();
        emulator.init_emu(file);
        while (emulator.is_playing()) {
//            emulator.step_emu();
            if ((System.currentTimeMillis() - start) >= framerate) {
                emulator.step_emu();
//                emulator.get_display().repaint();
                start = System.currentTimeMillis();
            }
        }
    }
}