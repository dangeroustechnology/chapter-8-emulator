import java.awt.event.*;

public class Ch8_input extends KeyAdapter {
    // https://docs.oracle.com/javase/tutorial/uiswing/events/keylistener.html
    // https://docs.oracle.com/en/java/javase/19/docs/api/java.desktop/java/awt/event/KeyAdapter.html
    Ch8_display ch8d;

    
    Ch8_input(Ch8_display d) {
        ch8d = d;
    }

    public void keyPressed(KeyEvent e) {
        ch8d.set_keyp(decode_keyEvent(e));
    }

    public void keyReleased(KeyEvent e) {
        ch8d.set_keyp(0xFF);
    }

    private int decode_keyEvent(KeyEvent e) {  // TODO: do actual key mapping (1-4, Q-R, etc.)
        int keycode = e.getKeyCode();
        return switch (keycode) {
            case 48 -> 0x00;  // 0
            case 49 -> 0x01;
            case 50 -> 0x02;
            case 51 -> 0x03;
            case 52 -> 0x04;
            case 53 -> 0x05;
            case 54 -> 0x06;
            case 55 -> 0x07;
            case 56 -> 0x08;
            case 57 -> 0x09;  // 9
            case 65 -> 0x0A;  // A
            case 66 -> 0x0B;
            case 67 -> 0x0C;
            case 68 -> 0x0D;
            case 69 -> 0x0E;
            case 70 -> 0x0F;  // F
            default -> 0xFF;  // invalid key
        };
    }
}
