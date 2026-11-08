package bme.prog.torpedo;
import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Most már a Főmenüvel indítunk, nem egyből a játékkal
            new MainMenu().setVisible(true);
        });
    }
}