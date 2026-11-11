package bme.prog.torpedo;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * A Torpedó játék indító grafikus felülete (Főmenü).
 * Ez az osztály felelős az alkalmazás belépési pontjának megjelenítéséért,
 * ahol a felhasználó választhat egy teljesen új játék indítása, vagy
 * egy korábban elmentett játékállás betöltése között.
 */
public class MainMenu extends JFrame {

    /**
     * Létrehozza a Főmenü ablakát, beállítja a méreteit és elhelyezi rajta a vezérlőgombokat.
     * Két fő funkciót biztosít:
     * 1. Új Játék: Megnyitja a beállításokat (SetupWindow) a hajók lerakásához.
     * 2. Játék Betöltése: Bekér egy egyedi azonosító kódot, és megpróbálja
     *    deszerializálni a hozzá tartozó bináris .dat fájlt a korábbi állapot visszaállításához.
     */
    public MainMenu() {
        setTitle("Főmenü");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Az ablakot a képernyő közepére igazítja
        setLayout(new GridBagLayout()); // A GridBagLayout biztosítja, hogy a tartalom középre kerüljön

        // A gombokat egy függőleges rácsba (GridLayout) rendezzük, 20 pixel térközzel (vgap)
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));

        JButton newGameBtn = new JButton("Új Játék");
        newGameBtn.setFont(new Font("Arial", Font.BOLD, 20));
        
        JButton loadGameBtn = new JButton("Játék Betöltése");
        loadGameBtn.setFont(new Font("Arial", Font.BOLD, 20));

        // --- ÚJ JÁTÉK ESEMÉNYKEZELŐ ---
        // Ha a játékos rányom az Új Játékra, bezárjuk a menüt és megnyitjuk az előkészítő ablakot
        newGameBtn.addActionListener(e -> {
            dispose(); // Megsemmisíti a jelenlegi Főmenü ablakot
            new SetupWindow().setVisible(true); // Megnyitja a hajólerakó és beállító ablakot
        });
        
        // --- JÁTÉK BETÖLTÉSE ESEMÉNYKEZELŐ ---
        loadGameBtn.addActionListener(e -> {
            // 1. Bekérjük a 4 karakteres egyedi mentési kódot a játékostól egy felugró ablakban
            String inputCode = JOptionPane.showInputDialog(MainMenu.this, "Add meg a mentési kódot (pl. K8F2):");
            
            // Ellenőrizzük, hogy a játékos beírt-e valamit (nem null, és nem csak szóközök)
            if (inputCode != null && !inputCode.trim().isEmpty()) {
                // Összeállítjuk a fájlnevet: eltávolítjuk a felesleges szóközöket és nagybetűssé alakítjuk
                String filename = "save_" + inputCode.trim().toUpperCase() + ".dat";
                
                // 2. A Java ObjectInputStream segítségével visszaolvassuk a fájlból a teljes 'Game' objektumot
                // A try-with-resources blokk garantálja, hogy a fájlolvasó a művelet végén biztonságosan lezárul
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
                    Game loadedGame = (Game) ois.readObject();
                    
                    // 3. Ha a beolvasás sikeres volt, jelezzük a felhasználónak, bezárjuk a menüt,
                    // és átadjuk a visszatöltött Modell objektumot az új játéktérnek (Nézetnek)
                    JOptionPane.showMessageDialog(MainMenu.this, "Játék sikeresen betöltve!");
                    dispose();
                    new GameWindow(loadedGame).setVisible(true);
                    
                } catch (Exception ex) {
                    // Ha a kódhoz nem tartozik fájl, vagy a fájl szerkezete sérült, hibaüzenetet adunk
                   JOptionPane.showMessageDialog(MainMenu.this, 
    "Nem található mentés ezzel a kóddal, vagy sérült a fájl!\n(" + filename + ")", 
    "Hiba", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // A gombok hozzáadása a panelhez, majd a panel hozzáadása a főablakhoz
        panel.add(newGameBtn);
        panel.add(loadGameBtn);
        add(panel);
    }
}
