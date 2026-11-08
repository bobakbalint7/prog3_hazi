package bme.prog.torpedo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.List; 

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

    // Fegyvergombok (Osztályszintű változók lettek, hogy lövés után frissíteni tudjuk rajtuk a lőszert!)
    private JButton btnSzoenyeg;
    private JButton btnCelzott;

    /**
     * Létrehozza a főablakot, beállítja a méreteit, és elhelyezi rajta a két játéktáblát.
     */
    public GameWindow(Game game) {
        this.game = game;

        setTitle("BME Torpedó");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        setLayout(new BorderLayout()); 

        // --- FEGYVERVÁLASZTÓ GOMBOK LÉTREHOZÁSA (FELSŐ SÁV - NORTH) ---
        JPanel topPanel = new JPanel();
        btnSzoenyeg = new JButton("Szőnyegbomba (" + game.getPlayerSpec1() + ")");
        btnCelzott = new JButton("Célzott lövés (" + game.getPlayerSpec2() + ")");

        btnSzoenyeg.addActionListener(e -> {
            if (game.getPlayerSpec1() > 0) {
                game.setActivePlayerShot(1);
                btnSzoenyeg.setBackground(Color.YELLOW); 
                btnCelzott.setBackground(null);
            }
        });

        btnCelzott.addActionListener(e -> {
            if (game.getPlayerSpec2() > 0) {
                game.setActivePlayerShot(2);
                btnCelzott.setBackground(Color.YELLOW);
                btnSzoenyeg.setBackground(null);
            }
        });

        topPanel.add(btnSzoenyeg);
        topPanel.add(btnCelzott);
        add(topPanel, BorderLayout.NORTH);
        
        // --- TÁBLÁK LÉTREHOZÁSA ÉS ELHELYEZÉSE (KÖZÉPRE - CENTER) ---
        JPanel playerPanel = createBoardPanel(playerButtons, "Saját táblád", false);
        JPanel enemyPanel = createBoardPanel(enemyButtons, "Ellenfél táblája", true);

        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.add(playerPanel); 
        boardsPanel.add(enemyPanel);
        add(boardsPanel, BorderLayout.CENTER); 

        // --- A JÁTÉKOS SAJÁT HAJÓINAK KISZÍNEZÉSE ZÖLDRE INDULÁSKOR ---
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Coordinate currentPos = new Coordinate(r, c);
                if (game.hasPlayerShipAt(currentPos)) {
                    playerButtons[r][c].setBackground(Color.GREEN);
                }
            }
        }
    }

    /**
     * Létrehoz és visszaad egy komplett játéktábla panelt.
     */
    private JPanel createBoardPanel(CellButton[][] buttonArray, String title, boolean isClickable) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(10, 10));

        // EGYETLEN közös figyelőt hozunk létre az egész táblához!
        ShootListener sharedListener = new ShootListener(); 

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setBackground(Color.LIGHT_GRAY); 
                btn.setFocusPainted(false); 
                
                if (isClickable) {
                    btn.addActionListener(sharedListener);
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
        
        @Override
        public void actionPerformed(ActionEvent e) {
            CellButton clickedBtn = (CellButton) e.getSource();
            Coordinate target = new Coordinate(clickedBtn.getRow(), clickedBtn.getCol());

            if (game.isPlayerTurn() && !game.isGameOver()) {
                try {
                    // Lövünk a Modellben, és megkapjuk a CSOMAGOT (ShotResult)
                    Game.ShotResult result = game.playTurn(target);
                    
                    // Színezzük a PIROS (találat) gombokat a listából
                    for (Coordinate hitC : result.hitCells) {
                        CellButton b = enemyButtons[hitC.getRow()][hitC.getCol()];
                        b.setBackground(Color.RED);
                        b.removeActionListener(this); // Lezárjuk a gombot
                    }
                    
                    // Színezzük a KÉK (víz) gombokat a listából
                    for (Coordinate missC : result.missedCells) {
                        CellButton b = enemyButtons[missC.getRow()][missC.getCol()];
                        b.setBackground(Color.BLUE);
                        b.removeActionListener(this); // Lezárjuk a gombot
                    }

                    // --- FEGYVERGOMBOK FRISSÍTÉSE ---
                    btnSzoenyeg.setText("Szőnyegbomba (" + game.getPlayerSpec1() + ")");
                    btnCelzott.setText("Célzott lövés (" + game.getPlayerSpec2() + ")");
                    btnSzoenyeg.setBackground(null);
                    btnCelzott.setBackground(null);

                    // Ellenőrizzük, vége van-e a játéknak
                    if (game.isGameOver()) {
                        JOptionPane.showMessageDialog(GameWindow.this, "Győztél!");
                        dispose(); 
                        new MainMenu().setVisible(true); 
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
     * Elindítja a gép válaszlépését, és frissíti a játékos grafikus tábláját.
     */
    private void playBotTurn() {
        // 1. A gép lő, és megkapjuk a CSOMAGOT (ShotResult)
        Game.ShotResult result = game.playBotTurn();

        // 2. Kiszínezzük a gép találatait pirosra
        for (Coordinate hitC : result.hitCells) {
            CellButton btn = playerButtons[hitC.getRow()][hitC.getCol()];
            btn.setBackground(Color.RED);
        }

        // 3. Kiszínezzük a gép mellélövéseit kékre
        for (Coordinate missC : result.missedCells) {
            CellButton btn = playerButtons[missC.getRow()][missC.getCol()];
            btn.setBackground(Color.BLUE);
        }

        // 4. Ellenőrizzük a gép győzelmét
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "Vesztettél! A gép elsüllyesztette az összes hajódat!");
            dispose(); 
            new MainMenu().setVisible(true);
        }
    }
}