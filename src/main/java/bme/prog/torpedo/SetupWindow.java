package bme.prog.torpedo;

import javax.swing.*;

import bme.prog.torpedo.Ship.Direction;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Queue;

public class SetupWindow extends JFrame {

    private final Game pendingGame; // A játék, amit épp felépítünk
    private final CellButton[][] setupButtons = new CellButton[10][10];
    
    // UI Elemek a beállításhoz
    private JSpinner spin5, spin4, spin3, spin2, spinSpec1, spinSpec2;
    private JButton startPlacementBtn;
    private JLabel statusLabel;

    // Lerakási logika változói
    private Queue<Integer> shipsToPlace; // Itt tároljuk, milyen hosszú hajókat kell még lerakni
    private Direction currentDir = Direction.HORIZONTAL; // Alapértelmezett irány (Jobbra)

    public SetupWindow() {
        this.pendingGame = new Game();
        this.shipsToPlace = new LinkedList<>();

        setTitle("Torpedó - Játék Előkészítése");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Bal opldalon vannak a beállítások
        JPanel settingsPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        settingsPanel.add(new JLabel("5 hosszú hajók száma:"));
        spin5 = new JSpinner(new SpinnerNumberModel(1, 0, 5, 1)); // Alap: 1 db
        settingsPanel.add(spin5);

        settingsPanel.add(new JLabel("4 hosszú hajók száma:"));
        spin4 = new JSpinner(new SpinnerNumberModel(2, 0, 5, 1)); // Alap: 2 db
        settingsPanel.add(spin4);

        settingsPanel.add(new JLabel("3 hosszú hajók száma:"));
        spin3 = new JSpinner(new SpinnerNumberModel(3, 0, 5, 1)); // Alap: 3 db
        settingsPanel.add(spin3);

        settingsPanel.add(new JLabel("2 hosszú hajók száma:"));
        spin2 = new JSpinner(new SpinnerNumberModel(4, 0, 5, 1)); // Alap: 4 db
        settingsPanel.add(spin2);

        settingsPanel.add(new JLabel("Szőnyegbomba (db):"));
        spinSpec1 = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
        settingsPanel.add(spinSpec1);

        settingsPanel.add(new JLabel("Célzott lövés (db):"));
        spinSpec2 = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
        settingsPanel.add(spinSpec2);

        startPlacementBtn = new JButton("Hajók Lerakása");
        startPlacementBtn.addActionListener(e -> startPlacementPhase());
        settingsPanel.add(startPlacementBtn);

        statusLabel = new JLabel("Állítsd be a darabszámokat!");
        statusLabel.setForeground(Color.RED);
        settingsPanel.add(statusLabel);

        add(settingsPanel, BorderLayout.WEST);

        // Jobb oldali 10x10-es rács
        JPanel gridPanel = new JPanel(new GridLayout(10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setBackground(Color.LIGHT_GRAY);
                btn.setOpaque(true);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                // Egér figyelése (Bal klikk: lerak, Jobb klikk: forgat)
                btn.addMouseListener(new PlacementMouseListener());
                
                setupButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    /**
     * Lezárja a pörgettyűket, beállítja a Modell értékeit, és feltölti a lerakandó hajók listáját.
     */
    private void startPlacementPhase() {
        startPlacementBtn.setEnabled(false);
        // Pörgettyűk kikapcsolása
        spin5.setEnabled(false); spin4.setEnabled(false); spin3.setEnabled(false); 
        spin2.setEnabled(false); spinSpec1.setEnabled(false); spinSpec2.setEnabled(false);

        // Különleges lövések mentése a Game-be
        pendingGame.setSpecialShot1Count((Integer) spinSpec1.getValue());
        pendingGame.setSpecialShot2Count((Integer) spinSpec2.getValue());

        // Lista feltöltése (pl. berakunk 1 db 5-öst, 2 db 4-est, stb.)
        addShipsToQueue(5, (Integer) spin5.getValue());
        addShipsToQueue(4, (Integer) spin4.getValue());
        addShipsToQueue(3, (Integer) spin3.getValue());
        addShipsToQueue(2, (Integer) spin2.getValue());

        updateStatusText();
    }

    private void addShipsToQueue(int length, int count) {
        for (int i = 0; i < count; i++) {
            shipsToPlace.add(length);
        }
    }

    private void updateStatusText() {
        if (shipsToPlace.isEmpty()) {
            statusLabel.setText("Minden hajó lent van!");
            statusLabel.setForeground(Color.GREEN);
            
            // Játék indítása
            JOptionPane.showMessageDialog(this, "A játék indul!");
            
            
            
            dispose(); // Beállító ablak bezárása
            new GameWindow(pendingGame).setVisible(true); // Fő játékablak megnyitása
        } else {
            String dirText = (currentDir == Direction.HORIZONTAL) ? "Vízszintes (Jobbra)" : "Függőleges (Le)";
            statusLabel.setText(shipsToPlace.peek() + " hosszú hajó. (Jobb klikk: " + dirText + ")");
            statusLabel.setForeground(Color.BLUE);
        }
    }

    /**
     * Belső osztály az egérkattintások figyelésére.
     */
    private class PlacementMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            // Ha még nem nyomtuk meg a "Lerakás" gombot, vagy elfogytak a hajók, ne csináljon semmit
            if (startPlacementBtn.isEnabled() || shipsToPlace.isEmpty()) return;

            CellButton btn = (CellButton) e.getSource();

            // JOBB KLIKK: Irány megfordítása
            if (SwingUtilities.isRightMouseButton(e)) {
                currentDir = (currentDir == Direction.HORIZONTAL) ? Direction.VERTICAL : Direction.HORIZONTAL;
                updateStatusText();
                return;
            }

            // BAL KLIKK: Hajó lerakásának megkísérlése
            
            if (SwingUtilities.isLeftMouseButton(e)) {
                int length = shipsToPlace.peek(); // Megnézzük mekkora a következő hajó
                
                // Kiszedjük külön a sort és az oszlopot a megnyomott gombból
                int r = btn.getRow();
                int c = btn.getCol();
                
                try {
                    
                    Ship newShip = Ship.createShip(r, c, length, currentDir);
                    pendingGame.placePlayerShip(newShip);
                    
                    // Ha nem dobott Exceptiont, a hajó lekerült
                    shipsToPlace.poll(); // Kivesszük a listából
                    colorPlacedShip(newShip); // Kiszínezzük a gombokat feketére
                    updateStatusText();
                    
                } catch (IllegalArgumentException ex) {
                    // Ha ütközik vagy lelóg, kiírjuk a hibaüzenetedet!
                    JOptionPane.showMessageDialog(SetupWindow.this, ex.getMessage(), "Szabálytalan lépés!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Kifeketíti azokat a gombokat a rácson, ahova sikeresen letettünk egy hajót.
     */
    private void colorPlacedShip(Ship ship) {
        for (Coordinate c : ship.getCoordinates()) {
            setupButtons[c.getRow()][c.getCol()].setBackground(Color.DARK_GRAY);
        }
    }
}
