package bme.prog.torpedo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.List; 

/**
 * A játék grafikus főablaka (Nézet és Vezérlő egyben az MVC mintában).
 * Felelős a teljes felhasználói felület (ablak, játéktáblák, fegyvergombok, mentés gomb) 
 * megjelenítéséért és vizuális frissítéséért, valamint a játékos egérkattintásainak 
 * (lövések és fegyverváltások) lekezeléséért és a Modell (Game) felé történő továbbításáért.
 */
public class GameWindow extends JFrame {
    
    /** A játék belső logikáját (Modell) összefogó és irányító motor példánya. */
    private final Game game;
    
    /** A játékos saját táblájának gombjait tároló 10x10-es mátrix a grafikus memóriában. */
    private final CellButton[][] playerButtons = new CellButton[10][10];
    
    /** Az ellenfél táblájának gombjait tároló 10x10-es mátrix a grafikus memóriában. */
    private final CellButton[][] enemyButtons = new CellButton[10][10];

    /** Grafikus gomb a Szőnyegbombázás (1. speciális fegyver) kiválasztására. Osztályszintű, hogy lövés után frissíthető legyen rajta a maradék lőszer. */
    private JButton btnSzoenyeg;
    
    /** Grafikus gomb a Célzott lövés (2. speciális fegyver) kiválasztására. Osztályszintű, hogy lövés után frissíthető legyen rajta a maradék lőszer. */
    private JButton btnCelzott;

    /**
     * Létrehozza a főablakot, beállítja a méreteit, és felépíti a teljes játékkörnyezetet.
     * Magában foglalja a fegyvergombok és a mentés gomb inicializálását, a két játéktábla 
     * legenerálását, valamint a játékállás vizuális rekonstruálását (zöld saját hajók, 
     * és betöltés esetén a már meglőtt piros/kék cellák visszaállítását).
     * 
     * @param game A játék logikáját vezérlő {@code Game} objektum (akár új, akár betöltött).
     */
    public GameWindow(Game game) {
        this.game = game;

        setTitle("BME Torpedó");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        setLayout(new BorderLayout()); 

        // --- FEGYVERVÁLASZTÓ ÉS MENTÉS GOMBOK LÉTREHOZÁSA (FELSŐ SÁV - NORTH) ---
        JPanel topPanel = new JPanel();
        btnSzoenyeg = new JButton("Szőnyegbomba (" + game.getPlayerSpec1() + ")");
        btnCelzott = new JButton("Célzott lövés (" + game.getPlayerSpec2() + ")");

        // Szőnyegbomba kiválasztása
        btnSzoenyeg.addActionListener(e -> {
            if (game.getPlayerSpec1() > 0) {
                game.setActivePlayerShot(1);
                btnSzoenyeg.setBackground(Color.YELLOW); 
                btnCelzott.setBackground(null);
            }
        });

        // Célzott lövés kiválasztása
        btnCelzott.addActionListener(e -> {
            if (game.getPlayerSpec2() > 0) {
                game.setActivePlayerShot(2);
                btnCelzott.setBackground(Color.YELLOW);
                btnSzoenyeg.setBackground(null);
            }
        });

        topPanel.add(btnSzoenyeg);
        topPanel.add(btnCelzott);

        // Mentés és Kilépés gomb (Szerializációs logika)
        JButton btnSave = new JButton("Mentés és Kilépés");
        btnSave.addActionListener(e -> {
            // 1. Véletlen 4 karakteres alfanumerikus kód generálása a mentés azonosításához
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            StringBuilder codeBuilder = new StringBuilder();
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < 4; i++) {
                codeBuilder.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            String saveCode = codeBuilder.toString();
            String filename = "save_" + saveCode + ".dat";

            // 2. A teljes 'game' objektum (Modell) bináris fájlba írása szerializációval
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(filename))) {
                oos.writeObject(game);
                
                JOptionPane.showMessageDialog(GameWindow.this, 
    "A játék sikeresen elmentve!\n\n" +
    "A kódod: " + saveCode + "\n\n" + 
    "Ezt jegyezd meg a betöltéshez!");
                
                dispose(); // Bezárjuk az aktuális csatateret
                new MainMenu().setVisible(true); // Visszatérés a Főmenübe
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(GameWindow.this, "Hiba a mentés során: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        topPanel.add(btnSave);
        add(topPanel, BorderLayout.NORTH);
        
        // --- JÁTÉKTÁBLÁK LÉTREHOZÁSA ÉS ELHELYEZÉSE (KÖZÉPRE - CENTER) ---
        JPanel playerPanel = createBoardPanel(playerButtons, "Saját táblád", false);
        JPanel enemyPanel = createBoardPanel(enemyButtons, "Ellenfél táblája", true);

        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.add(playerPanel); 
        boardsPanel.add(enemyPanel);
        add(boardsPanel, BorderLayout.CENTER); 

        // --- A JÁTÉKÁLLÁS VIZUÁLIS REKONSTRUÁLÁSA ---
        
        // 1. A játékos saját, még ép hajóinak kiszínezése zöldre az induláskor
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Coordinate currentPos = new Coordinate(r, c);
                if (game.hasPlayerShipAt(currentPos)) {
                    playerButtons[r][c].setBackground(Color.GREEN);
                }
            }
        }
        
        // 2. A játékos már meglőtt, sikeres (Piros) célpontjainak visszaállítása és a gombok lezárása (Betöltés esetén)
        for (Coordinate c : game.getPlayerHitCells()) {
            CellButton btn = enemyButtons[c.getRow()][c.getCol()];
            btn.setBackground(Color.RED);
            for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
        }
        
        // 3. A játékos már meglőtt, vízbe esett (Kék) célpontjainak visszaállítása és a gombok lezárása
        for (Coordinate c : game.getPlayerMissedCells()) {
            CellButton btn = enemyButtons[c.getRow()][c.getCol()];
            btn.setBackground(Color.BLUE);
            for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
        }
        
        // 4. A gép sikeres (Piros) találatainak visszaállítása a saját tábládon
        for (Coordinate c : game.getBotHitCells()) {
            playerButtons[c.getRow()][c.getCol()].setBackground(Color.RED);
        }
        
        // 5. A gép vízbe esett (Kék) mellélövéseinek visszaállítása a saját tábládon
        for (Coordinate c : game.getBotMissedCells()) {
            playerButtons[c.getRow()][c.getCol()].setBackground(Color.BLUE);
        }
    }

    /**
     * Létrehoz és visszaad egy komplett grafikus játéktábla panelt (címsorral és egy 10x10-es rácsba rendezett gombtömbbel).
     * 
     * @param buttonArray A 2D tömb, amibe a generált {@code CellButton} gombokat mentjük a későbbi hivatkozáshoz.
     * @param title A tábla fölött megjelenő szöveges felirat (pl. "Saját táblád").
     * @param isClickable {@code true}, ha a tábla gombjainak reagálniuk kell a kattintásokra (ez csak az ellenség táblájánál igaz).
     * @return A felépített {@code JPanel}, ami tartalmazza a címet és a gombokat.
     */
    private JPanel createBoardPanel(CellButton[][] buttonArray, String title, boolean isClickable) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(10, 10));

        // Egyetlen, közös figyelőt hozunk létre az egész táblához a hatékony memóriakezelés, 
        // és a több cellát érintő lövések (Szőnyegbomba) biztonságos gomb-lezárása érdekében.
        ShootListener sharedListener = new ShootListener(); 

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setBackground(Color.LIGHT_GRAY); 
                btn.setFocusPainted(false); 
                
                // Csak akkor adunk hozzá eseménykezelőt, ha a tábla interaktív (ellenség)
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
     * Belső eseménykezelő osztály az ellenséges táblára leadott játékosi kattintások (lövések) feldolgozására.
     * Lekéri a megkattintott gomb koordinátáját, továbbítja a lövést a Modell (Game) felé,
     * majd a visszakapott eredmény (ShotResult) alapján kiszínezi és lezárja a megfelelő gombokat.
     */
    private class ShootListener implements ActionListener {
        
        /**
         * Akkor hívódik meg, amikor a játékos rákattint egy érvényes, még meg nem lőtt gombra az ellenség tábláján.
         * 
         * @param e Az esemény objektum, amely tartalmazza a megnyomott gomb referenciáját.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            CellButton clickedBtn = (CellButton) e.getSource();
            Coordinate target = new Coordinate(clickedBtn.getRow(), clickedBtn.getCol());

            if (game.isPlayerTurn() && !game.isGameOver()) {
                try {
                    // Lövünk a belső Modellben, és visszakapjuk a feldolgozott eredménycsomagot (DTO)
                    Game.ShotResult result = game.playTurn(target);
                    
                    // Színezzük a találatot (Piros) elszenvedő gombokat, és letiltjuk a további kattintást róluk
                    for (Coordinate hitC : result.hitCells) {
                        CellButton b = enemyButtons[hitC.getRow()][hitC.getCol()];
                        b.setBackground(Color.RED);
                        b.removeActionListener(this); 
                    }
                    
                    // Színezzük a mellélövést (Kék) regisztráló gombokat, és letiltjuk róluk a kattintást
                    for (Coordinate missC : result.missedCells) {
                        CellButton b = enemyButtons[missC.getRow()][missC.getCol()];
                        b.setBackground(Color.BLUE);
                        b.removeActionListener(this); 
                    }

                    // --- FEGYVERGOMBOK FRISSÍTÉSE ---
                    // Visszaállítjuk az alapértelmezett színt, és frissítjük a gombokon a maradék lőszerek számát
                    btnSzoenyeg.setText("Szőnyegbomba (" + game.getPlayerSpec1() + ")");
                    btnCelzott.setText("Célzott lövés (" + game.getPlayerSpec2() + ")");
                    btnSzoenyeg.setBackground(null);
                    btnCelzott.setBackground(null);

                    // Ellenőrizzük, hogy a játékos a mostani lövésével megnyerte-e a mérkőzést
                    if (game.isGameOver()) {
                        JOptionPane.showMessageDialog(GameWindow.this, "Győztél!");
                        dispose(); 
                        new MainMenu().setVisible(true); 
                        return; // Kilépünk a metódusból, hogy a gép már ne lőjön vissza
                    }

                    // Ha nincs vége a játéknak, átadjuk a kört a számítógépes ellenfélnek
                    playBotTurn();

                } catch (Exception ex) {
                    System.out.println("Érvénytelen lépés: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Elindítja a gép válaszlépését a belső Modellben, majd az eredmények alapján
     * frissíti (kiszínezi pirosra vagy kékre) a játékos saját grafikus tábláját.
     * A kör végén ellenőrzi a gép esetleges győzelmét.
     */
    private void playBotTurn() {
        // 1. A gép lő a Modellben, és visszakapjuk a feldolgozott eredménycsomagot
        Game.ShotResult result = game.playBotTurn();

        // 2. Kiszínezzük a gép sikeres találatait (Piros) a játékos tábláján
        for (Coordinate hitC : result.hitCells) {
            CellButton btn = playerButtons[hitC.getRow()][hitC.getCol()];
            btn.setBackground(Color.RED);
        }

        // 3. Kiszínezzük a gép mellélövéseit (Kék) a játékos tábláján
        for (Coordinate missC : result.missedCells) {
            CellButton btn = playerButtons[missC.getRow()][missC.getCol()];
            btn.setBackground(Color.BLUE);
        }

        // 4. Ellenőrizzük, hogy a gép elsüllyesztette-e a játékos összes hajóját
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "Vesztettél! A gép elsüllyesztette az összes hajódat!");
            dispose(); 
            new MainMenu().setVisible(true); // Visszatérés a Főmenübe
        }
    }
}
