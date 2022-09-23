public class Main {

    public static void main(String[] args) {
        String octo1 = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\octojam1title.ch8";
        String ibm = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\IBM_logo.ch8";
        String testop = "C:\\Users\\itbra\\Documents\\Bard\\Academic\\003-SPROJ\\chp8-java\\test_opcode.ch8";
        Ch8_Emulator emulator = new Ch8_Emulator();
        emulator.init_emu(octo1);
        for (int i = 0; i < 66; i++) {
            String s = i + ": ";
            emulator.step_emu();
        }

//        byte be1 = 0x43;
//        byte le1 = (byte) 0xE0;
//        short be = be1;
//        short le = le1;
////        short le = 0xE0;
//        be  = (short) (be << 8);
//        le &= 0x00FF;
//        short bele = (short) (be | le);
////        if (be == 0x00) { bele ^= 0xFF00; }
//        System.out.printf("%x\n", be);
//        System.out.printf("%x\n", le); // IT FILLS ALL HIGHER BITS WITH 1'S ON CASTING??????
//        System.out.printf("%x", bele);
    }
}