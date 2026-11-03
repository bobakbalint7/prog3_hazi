package bme.prog.torpedo;

import javax.swing.*;
import java.awt.*;

/**
 * A játék főablaka (Nézet).
 * Megjeleníti a játékos és az ellenfél tábláját.
 */
public class GameWindow extends JFrame {
    
    private final Game game;
    
    // A gombokat tároló 2D tömbök, hogy később könnyen át tudjuk színezni őket
    private final CellButton[][] playerButtons = new CellButton[10][10];
    private final CellButton[][] enemyButtons = new CellButton[10][10];

    public GameWindow(Game game) {
        this.game = game;

        // Ablak alapbeállításai
        setTitle("Torpedó");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Középre teszi az ablakot
        setLayout(new GridLayout(1, 2, 20, 0)); // 1 sor, 2 oszlop, 20px rés a két tábla között

        // Bal oldali panel (Játékos táblája)
        JPanel playerPanel = createBoardPanel(playerButtons, "Saját táblád");
        
        // Jobb oldali panel (Ellenfél táblája)
        JPanel enemyPanel = createBoardPanel(enemyButtons, "Ellenfél táblája");

        
        add(playerPanel);
        add(enemyPanel);
    }

    /**
     * Segédmetódus, ami legenerál egy 10x10-es gomb-rácsot.
     */
    private JPanel createBoardPanel(CellButton[][] buttonArray, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Címke a tábla fölé
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Maga a 10x10-es rács
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(10, 10));

        // Feltöltjük a rácsot a saját okos gombjainkkal
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                CellButton btn = new CellButton(r, c);
                btn.setBackground(Color.LIGHT_GRAY); // Alapból szürke a víz
                
                // Később ide jön a kattintás-figyelő (ActionListener)
                
                buttonArray[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }
}
