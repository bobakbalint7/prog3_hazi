package bme.prog.torpedo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A játék grafikus főablaka (Nézet és Vezérlő).
 * Felelős a felhasználói felület (ablak, táblák, gombok) megjelenítéséért, 
 * valamint a játékos egérkattintásainak lekezeléséért.
 */
public class GameWindow extends JFrame {
    
    /** A játék belső logikáját (Modell) összefogó motor. */
    private final Game game;
    
    /** A játékos saját táblájának gombjait tároló 10x10-es mátrix a memóriában. */
    private final CellButton[][] playerButtons = new CellButton[10][10];
    
    /** Az ellenfél táblájának gombjait tároló 10x10-es mátrix a memóriában. */
    private final CellButton[][] enemyButtons = new CellButton[10][10];

    /**
     * Létrehozza a főablakot, beállítja a méreteit, és elhelyezi rajta a két játéktáblát.
     * 
     * @param game A játék logikáját vezérlő Game objektum.
     */
    public GameWindow(Game game) {
        this.game = game;

        setTitle("BME Torpedó");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridLayout(1, 2, 20, 0)); 

        // 1. Létrehozzuk a játékos tábláját (Ide NEM teszünk kattintás figyelőt, mert saját magára nem lőhet)
        JPanel playerPanel = createBoardPanel(playerButtons, "Saját táblád", false);
        
        // 2. Létrehozzuk az ellenfél tábláját (Ide TESZÜNK kattintás figyelőt)
        JPanel enemyPanel = createBoardPanel(enemyButtons, "Ellenfél táblája", true);

        add(playerPanel);
        add(enemyPanel);
    }

    /**
     * Létrehoz és visszaad egy komplett játéktábla panelt (címsorral és 10x10-es rácsba rendezett gombokkal).
     * 
     * @param buttonArray A 2D tömb, amibe a generált okos gombokat (CellButton) mentjük.
     * @param title A tábla fölött megjelenő szöveges címke.
     * @param isClickable Igaz, ha akarjuk, hogy a gombok reagáljanak a kattintásra (ellenség táblája).
     * @return A felépített JPanel, ami tartalmazza a rácsot és a címet.
     */
    private JPanel createBoardPanel(CellButton[][] buttonArray, String title, boolean isClickable) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(10, 10));

       for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setBackground(Color.LIGHT_GRAY); 
                btn.setFocusPainted(false); 
                
                // HA az ellenség tábláját generáljuk éppen, akkor ráakasztjuk az eseménykezelőt
                if (isClickable) {
                    btn.addActionListener(new ShootListener());
                }
                
                buttonArray[r][c] = btn; 
                gridPanel.add(btn);      
            }
        }

        panel.add(gridPanel, BorderLayout.CENTER); 
        return panel;
    }

    /**
     * Belső eseménykezelő osztály az ellenséges táblára leadott kattintások (lövések) feldolgozására.
     */
    private class ShootListener implements ActionListener {
        
        /**
         * Akkor hívódik meg, amikor a játékos rákattint egy gombra.
         * Lekéri a kattintás koordinátáját, lő a belső Modellben, frissíti a GUI-t, 
         * végül átadja a kört a gépnek.
         * 
         * @param e A kattintás esemény (tartalmazza a megnyomott gombot).
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            CellButton clickedBtn = (CellButton) e.getSource();
            Coordinate target = new Coordinate(clickedBtn.getRow(), clickedBtn.getCol());

            if (game.isPlayerTurn() && !game.isGameOver()) {
                try {
                    // Lövünk a Modellben
                    boolean wasHit = game.playTurn(target);
                    
                    // Színezzük a gombot a grafikus felületen
                    if (wasHit) {
                        clickedBtn.setBackground(Color.RED);   // Találat
                    } else {
                        clickedBtn.setBackground(Color.BLUE);  // Mellé (Víz)
                    }
                    clickedBtn.removeActionListener(this);

                    // Ha a játéknak ezzel a lövéssel vége lett
                    if (game.isGameOver()) {
                        JOptionPane.showMessageDialog(GameWindow.this, "Győztél!");
                        return;
                    }

                    // Átadjuk a kört a Botnak
                    playBotTurn();

                } catch (Exception ex) {
                    System.out.println("Érvénytelen lépés: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Elindítja a gép válaszlépését, és frissíti a játékos grafikus tábláját 
     * a gépi lövés eredménye alapján.
     */
    private void playBotTurn() {
        // 1. A gép lő, és megkapjuk a pontos koordinátát, ahova a lövés ment
        Coordinate target = game.playBotTurn();
        // 2. Kikeressük a saját grafikus rácsunkból (playerButtons) azt az 1 db gombot
        CellButton btn = playerButtons[target.getRow()][target.getCol()];

        if (game.hasPlayerShipAt(target)) {
            btn.setBackground(Color.RED);  
        } else {
            btn.setBackground(Color.BLUE); 
        }
        //ha ezzel elsüllyedt az összes hajó
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "Vesztettél! A gép elsüllyesztette az összes hajódat!");
        }
    }
}