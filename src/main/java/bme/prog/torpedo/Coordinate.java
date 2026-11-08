package bme.prog.torpedo;

import java.io.Serializable;
import java.util.Objects;
import java.io.Serializable;
public class Coordinate implements Serializable{
    private final int row; // Y coordinate
    private final int col; // X coordinate

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // Needed for HashSet
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate u = (Coordinate) o;
        return row == u.row && col == u.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
