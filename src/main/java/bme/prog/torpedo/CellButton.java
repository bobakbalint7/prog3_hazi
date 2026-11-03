package bme.prog.torpedo;

import javax.swing.JButton;

/**
 * Saját gomb osztály a Torpedó rácsához.
 * Egy sima JButton, ami extraként megjegyzi, hogy a rács melyik sorában és oszlopában van.
 */
public class CellButton extends JButton {
    private final int row;
    private final int col;

    public CellButton(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
