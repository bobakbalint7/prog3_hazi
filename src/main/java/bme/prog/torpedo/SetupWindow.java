package bme.prog.torpedo;

import javax.swing.*;

import bme.prog.torpedo.Ship.Direction;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A Torpedó játék előkészítő (Setup) ablakának grafikus felülete.
 * Ebben az ablakban állíthatja be a felhasználó a játék paramétereit (hajók és extra lőszerek számát),
 * valamint itt helyezheti el a saját hajóit a 10x10-es rácson egérkattintások segítségével.
 * A lerakás befejeztével ez az osztály indítja el a tényleges csatát (GameWindow).
 */
public class SetupWindow extends JFrame {

    /** A játék Modellje, amelyet az ablakban megadott beállításokkal fokozatosan építünk fel. */
    private final Game pendingGame; 
    
    /** A hajók vizuális lerakására szolgáló 10x10-es gombmátrix. */
    private final CellButton[][] setupButtons = new CellButton[10][10];
    
    // --- UI Elemek a beállításhoz ---
    
    /** Pörgettyű az 5 egység hosszú hajók darabszámának beállításához. */
    private JSpinner spin5;
    
    /** Pörgettyű a 4 egység hosszú hajók darabszámának beállításához. */
    private JSpinner spin4;
    
    /** Pörgettyű a 3 egység hosszú hajók darabszámának beállításához. */
    private JSpinner spin3;
    
    /** Pörgettyű a 2 egység hosszú hajók darabszámának beállításához. */
    private JSpinner spin2;
    
    /** Pörgettyű a Szőnyegbomba (1. speciális fegyver) darabszámának beállításához. */
    private JSpinner spinSpec1;
    
    /** Pörgettyű a Célzott lövés (2. speciális fegyver) darabszámának beállításához. */
    private JSpinner spinSpec2;
    
    /** A gomb, amellyel a játékos rögzíti a darabszámokat és megkezdi a hajók rácsra helyezését. */
    private JButton startPlacementBtn;
    
    /** Szöveges címke a felhasználó tájékoztatására (pl. hibaüzenetek, aktuálisan lerakandó hajó mérete). */
    private JLabel statusLabel;

    // --- Lerakási logika változói ---
    
    /** Egy sor (Queue), amelyben a még lerakásra váró hajók hosszát tároljuk a beállított darabszámok alapján. */
    private Queue<Integer> shipsToPlace; 
    
    /** Az aktuálisan lerakandó hajó iránya (Jobb klikkel változtatható). Alapértelmezetten vízszintes (jobbra). */
    private Direction currentDir = Direction.HORIZONTAL; 

    /**
     * Létrehozza a beállító ablakot, felépíti a bal oldali vezérlőpanelt (spinnerek) 
     * és a jobb oldali 10x10-es interaktív rácsot a hajók lerakásához.
     */
    public SetupWindow() {
        this.pendingGame = new Game();
        this.shipsToPlace = new LinkedList<>();

        setTitle("Torpedó - Játék Előkészítése");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Bal oldali panel a beállításokhoz ---
        JPanel settingsPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        settingsPanel.add(new JLabel("5 hosszú hajók száma:"));
        spin5 = new JSpinner(new SpinnerNumberModel(1, 0, 5, 1)); // Alap: 1 db, Min: 0, Max: 5
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

        // Újrakezdés gomb: bezárja az aktuális ablakot és nyit egy teljesen tiszta újat
        JButton resetBtn = new JButton("Tábla Törlése");
        resetBtn.addActionListener(e -> {
            dispose(); 
            new SetupWindow().setVisible(true); 
        });
        settingsPanel.add(resetBtn);

        statusLabel = new JLabel("Állítsd be a darabszámokat!");
        statusLabel.setForeground(Color.RED);
        
        add(settingsPanel, BorderLayout.WEST);

        // --- Jobb oldali 10x10-es rács a hajók vizuális lerakásához ---
        JPanel gridPanel = new JPanel(new GridLayout(10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setBackground(Color.LIGHT_GRAY);
                btn.setOpaque(true);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                // Egér figyelése (Bal klikk: lerakás megkísérlése, Jobb klikk: irány forgatása)
                btn.addMouseListener(new PlacementMouseListener());
                
                setupButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); 
        add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Elindítja a hajólerakási fázist, miután a felhasználó megnyomta a "Hajók Lerakása" gombot.
     * Lezárja a beviteli mezőket (letiltja a spinnereket, amik emiatt kiszürkülnek),
     * beállítja a Modellbe a megadott speciális fegyverek számát, és a megadott 
     * hajódarabszámok alapján feltölti a lerakásra váró hajók listáját.
     */
    private void startPlacementPhase() {
        startPlacementBtn.setEnabled(false); // Gomb kiszürkítése
        
        // Pörgettyűk letiltása (automatikusan szürkévé és inaktívvá válnak)
        spin5.setEnabled(false); 
        spin4.setEnabled(false); 
        spin3.setEnabled(false); 
        spin2.setEnabled(false); 
        spinSpec1.setEnabled(false); 
        spinSpec2.setEnabled(false);

        // Különleges lövések darabszámának mentése a Game Modellbe
        pendingGame.setSpecialShot1Count((Integer) spinSpec1.getValue());
        pendingGame.setSpecialShot2Count((Integer) spinSpec2.getValue());

        // A lerakandó hajók listájának feltöltése csökkenő hosszak szerint
        addShipsToQueue(5, (Integer) spin5.getValue());
        addShipsToQueue(4, (Integer) spin4.getValue());
        addShipsToQueue(3, (Integer) spin3.getValue());
        addShipsToQueue(2, (Integer) spin2.getValue());

        updateStatusText();
    }

    /**
     * Segédmetódus, amely egy adott hosszúságú hajót a megadott darabszor 
     * betesz a lerakásra váró hajók várakozási sorába (Queue).
     * 
     * @param length A hozzáadandó hajó hossza.
     * @param count A hajóból hozzáadandó darabszám.
     */
    private void addShipsToQueue(int length, int count) {
        for (int i = 0; i < count; i++) {
            shipsToPlace.add(length);
        }
    }

    /**
     * Frissíti az ablak alján lévő státuszszöveget.
     * Ha még vannak lerakandó hajók, kiírja a következő hajó méretét és aktuális irányát.
     * Ha a lista kiürült, automatikusan legenerálja az ellenség hajóit, bezárja a 
     * SetupWindow-t, és megnyitja a tényleges csatateret (GameWindow).
     */
    private void updateStatusText() {
        if (shipsToPlace.isEmpty()) {
            statusLabel.setText("Minden hajó lent van!");
            statusLabel.setForeground(Color.GREEN);
            
            // A számítógépes ellenfél hajóinak automatikus legenerálása ugyanazokkal a darabszámokkal
            pendingGame.placeBotShipsRandomly(
                (Integer) spin5.getValue(), 
                (Integer) spin4.getValue(), 
                (Integer) spin3.getValue(), 
                (Integer) spin2.getValue()
            );
            
            JOptionPane.showMessageDialog(this, "A játék indul!");
            
            dispose(); // Beállító ablak bezárása
            new GameWindow(pendingGame).setVisible(true); // Fő játékablak megnyitása a kész Modellel
            
        } else {
            String dirText = (currentDir == Direction.HORIZONTAL) ? "Vízszintes (Jobbra)" : "Függőleges (Le)";
            statusLabel.setText(shipsToPlace.peek() + " hosszú hajó. (Jobb klikk: " + dirText + ")");
            statusLabel.setForeground(Color.BLUE);
        }
    }

    /**
     * Belső eseménykezelő osztály a grafikus rácson (táblán) történő egérkattintások figyelésére.
     * Bal klikkre megpróbálja letenni a hajót, Jobb klikkre megváltoztatja a hajó irányát.
     */
    private class PlacementMouseListener extends MouseAdapter {
        
        /**
         * Akkor hívódik meg, amikor a játékos kattint a rács egyik mezőjére.
         * 
         * @param e Az egéresemény, amely tartalmazza a kattintás típusát és a megkattintott gombot.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            // Ha még nem nyomtuk meg a "Hajók Lerakása" gombot, vagy elfogytak a hajók, ignoráljuk a kattintást
            if (startPlacementBtn.isEnabled() || shipsToPlace.isEmpty()) return;

            CellButton btn = (CellButton) e.getSource();

            // JOBB KLIKK: Az aktuálisan lerakandó hajó irányának megfordítása
            if (SwingUtilities.isRightMouseButton(e)) {
                currentDir = (currentDir == Direction.HORIZONTAL) ? Direction.VERTICAL : Direction.HORIZONTAL;
                updateStatusText();
                return;
            }

            // BAL KLIKK: Hajó lerakásának megkísérlése a rácsra
            if (SwingUtilities.isLeftMouseButton(e)) {
                int length = shipsToPlace.peek(); // Megnézzük mekkora a soron következő hajó
                
                // Kiszedjük a sort és az oszlopot a megnyomott gombból
                int r = btn.getRow();
                int c = btn.getCol();
                
                try {
                    // Megpróbáljuk létrehozni és letenni a hajót a belső Modellben (Game/Board)
                    Ship newShip = Ship.createShip(r, c, length, currentDir);
                    pendingGame.placePlayerShip(newShip);
                    
                    // Ha a Modell nem dobott Exception-t (nincs ütközés vagy lelógás), a hajó sikeresen lekerült
                    shipsToPlace.poll(); // Eltávolítjuk a sikeresen lerakott hajót a várakozási sorból
                    colorPlacedShip(newShip); // Kiszínezzük a gombokat feketére a vizuális visszajelzéshez
                    updateStatusText(); // Frissítjük a címkét, vagy elindítjuk a játékot, ha ez volt az utolsó
                    
                } catch (IllegalArgumentException ex) {
                    // Ha a hajó kilóg a pályáról vagy rákerülne egy másik hajóra, 
                    // elkapjuk a kivételt és hibaüzenetet jelenítünk meg
                    JOptionPane.showMessageDialog(SetupWindow.this, ex.getMessage(), "Szabálytalan lépés!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Vizuálisan kiszínezi sötétszürkére azokat a gombokat a grafikus rácson, 
     * ahova a játékos sikeresen letett egy hajót.
     * 
     * @param ship A sikeresen létrehozott és a Modellbe bekerült hajó objektum.
     */
    private void colorPlacedShip(Ship ship) {
        for (Coordinate c : ship.getCoordinates()) {
            setupButtons[c.getRow()][c.getCol()].setBackground(Color.DARK_GRAY);
        }
    }
}
