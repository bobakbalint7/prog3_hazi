package bme.prog.torpedo;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

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
        
        loadGameBtn.addActionListener(e -> {
            // 1. Bekérjük a kódot a játékostól
            String inputCode = JOptionPane.showInputDialog(MainMenu.this, "Add meg a mentési kódot (pl. K8F2):");
            
            // Ha beírt valamit és nem nyomott Cancel-t
            if (inputCode != null && !inputCode.trim().isEmpty()) {
                String filename = "save_" + inputCode.trim().toUpperCase() + ".dat";
                
                // 2. Visszaolvassuk a fájlból a teljes 'game' objektumot
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
                    Game loadedGame = (Game) ois.readObject();
                    
                    // 3. Bezárjuk a menüt és átadjuk a betöltött játékot a csatatérnek
                    JOptionPane.showMessageDialog(MainMenu.this, "Játék sikeresen betöltve!");
                    dispose();
                    new GameWindow(loadedGame).setVisible(true);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainMenu.this, 
                        "Nem található mentés ezzel a kóddal, vagy sérült a fájl!\n(" + filename + ")", 
                        "Hiba", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(newGameBtn);
        panel.add(loadGameBtn);
        add(panel);
    }
}
