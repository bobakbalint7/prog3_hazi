package bme.prog.torpedo;
import javax.swing.*;
import java.awt.*;


public class Main {
    public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            GameWindow window = new GameWindow(game);
            window.setVisible(true);
        });
    }
}