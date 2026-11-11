package bme.prog.torpedo;

import java.io.Serializable;
import java.util.Objects;
/**
 * Egy kétdimenziós koordinátát (sor és oszlop) reprezentáló osztály a játéktáblán.
 * A koordináták azonosítják a hajók elhelyezkedését és a lövések célpontját.
 * Az osztály megvalósítja a Serializable interfészt a játékállás menthetősége érdekében,
 * valamint felülírja az equals és hashCode metódusokat a gyűjteményekben 
 * (pl. List, HashSet) való helyes azonosításhoz.
 */
public class Coordinate implements Serializable {
    
    /** A koordináta sorának (Y tengely) indexe. */
    private final int row; 
    
    /** A koordináta oszlopának (X tengely) indexe. */
    private final int col; 

    /**
     * Létrehoz egy új koordinátát a megadott sor és oszlop értékekkel.
     * 
     * @param row A sor indexe (Y koordináta).
     * @param col Az oszlop indexe (X koordináta).
     */
    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Visszaadja a koordináta sorának (Y tengely) indexét.
     * 
     * @return A sor indexe.
     */
    public int getRow() {
        return row;
    }

    /**
     * Visszaadja a koordináta oszlopának (X tengely) indexét.
     * 
     * @return Az oszlop indexe.
     */
    public int getCol() {
        return col;
    }

    /**
     * Összehasonlítja ezt a koordinátát egy másik objektummal.
     * Két koordináta akkor egyenlő, ha mind a soruk, mind az oszlopuk megegyezik.
     * Ez kritikus fontosságú a találatok regisztrálásánál (a koordináták listából való törlésénél).
     * 
     * @param o Az összehasonlítandó objektum.
     * @return true, ha az objektum egy azonos sor és oszlop értékekkel rendelkező Coordinate példány; egyébként false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate u = (Coordinate) o;
        return row == u.row && col == u.col;
    }

    /**
     * Kiszámítja a koordináta hash kódját.
     * Szükséges ahhoz, hogy a koordináták helyesen működjenek a hash-alapú gyűjteményekben (pl. HashMap, HashSet).
     * 
     * @return A koordináta hash kódja.
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}