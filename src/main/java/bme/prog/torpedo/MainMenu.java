package bme.prog.torpedo;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Főmenü");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout()); // Középre igazítás

        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));

        JButton newGameBtn = new JButton("Új Játék");
        newGameBtn.setFont(new Font("Arial", Font.BOLD, 20));
        
        JButton loadGameBtn = new JButton("Játék Betöltése");
        loadGameBtn.setFont(new Font("Arial", Font.BOLD, 20));

        // Ha rányom az Új Játékra, bezárjuk a menüt és megnyitjuk a beállítót
        newGameBtn.addActionListener(e -> {
            dispose(); // Bezárja a főmenüt
            new SetupWindow().setVisible(true); // Megnyitja az előkészítő ablakot
        });

        panel.add(newGameBtn);
        panel.add(loadGameBtn);
        add(panel);
    }
}
