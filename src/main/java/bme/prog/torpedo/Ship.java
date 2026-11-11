package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * A Torpedó játék egyetlen hajóját reprezentáló osztály.
 * A hajó testét a játéktéren elfoglalt koordinátáinak ({@code Coordinate}) listája alkotja.
 * Amikor a hajót találat éri, a megadott koordináta kikerül a listából.
 * Ha a lista kiürül, a hajó elsüllyedtnek minősül.
 * Az osztály megvalósítja a {@code Serializable} interfészt a játékállás menthetősége érdekében.
 */
public class Ship implements Serializable {
    
    /** A hajó testét alkotó, még ép (nem eltalált) koordináták listája. */
    private final List<Coordinate> coordinates;

    /**
     * A hajó elhelyezkedésének lehetséges irányai a táblán.
     */
    public enum Direction {
        /** Vízszintes elhelyezkedés (balról jobbra haladva). */
        HORIZONTAL,
        
        /** Függőleges elhelyezkedés (fentről lefelé haladva). */
        VERTICAL
    }

    /**
     * Létrehoz egy új hajót a megadott induló koordinátákkal.
     * Privát jellegű vagy gyár által használt konstruktor, a hajó biztonságos létrehozásához
     * a {@code createShip} statikus metódus használata javasolt.
     * 
     * @param startingCoordinates A hajó testét alkotó koordináták listája.
     */
    public Ship(List<Coordinate> startingCoordinates) {
        // Létrehozunk egy új ArrayList-et a bejövő adatokból, hogy elkerüljük
        // a külső referencia módosítása okozta belső adatvesztést.
        this.coordinates = new ArrayList<>(startingCoordinates);
    }

    /**
     * Gyártó (Factory) metódus egy új hajó létrehozására a kezdőpont, a hossz és az irány alapján.
     * Ez a metódus legenerálja a hajó testét alkotó összes koordinátát, majd visszaad
     * egy kész {@code Ship} objektumot. 
     * (Megjegyzés: Ez a metódus még nem vizsgálja a pálya széléről való lelógást, 
     * azt a {@code Board} osztály ellenőrzi a lerakáskor).
     * 
     * @param startRow A hajó kezdőpontjának sor-indexe (Y koordináta).
     * @param startCol A hajó kezdőpontjának oszlop-indexe (X koordináta).
     * @param length A generálandó hajó hossza (cellák száma).
     * @param dir A hajó iránya (HORIZONTAL vagy VERTICAL).
     * @return Egy új {@code Ship} objektum a legenerált koordinátákkal.
     */
    public static Ship createShip(int startRow, int startCol, int length, Direction dir) {
        List<Coordinate> generatedCoords = new ArrayList<>();
        
        for (int i = 0; i < length; i++) {
            if (dir == Direction.HORIZONTAL) {
                // Vízszintes esetben az oszlop (X) indexet növeljük
                generatedCoords.add(new Coordinate(startRow, startCol + i));
            } else {
                // Függőleges esetben a sor (Y) indexet növeljük
                generatedCoords.add(new Coordinate(startRow + i, startCol));
            }
        }
        
        return new Ship(generatedCoords);
    }

    /**
     * Visszaadja a hajó aktuálisan még ép (nem eltalált) koordinátáinak listáját.
     * 
     * @return A hajó még sértetlen részeinek listája.
     */
    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    /**
     * Egy találat regisztrálása a hajón.
     * A metódus megpróbálja eltávolítani a kapott koordinátát a hajó testéből.
     * (A helyes működéshez elengedhetetlen, hogy a {@code Coordinate} osztályban
     * felül legyen írva az {@code equals} és {@code hashCode} metódus!)
     * 
     * @param c A találatot regisztráló koordináta.
     * @return {@code true}, ha a koordináta a hajó része volt és sikeresen törlődött (találat), 
     *         {@code false}, ha a koordináta nem tartozott a hajóhoz (mellé).
     */
    public boolean hitTaken(Coordinate c) {
        return coordinates.remove(c);
    }

    /**
     * Ellenőrzi, hogy a hajó elsüllyedt-e.
     * A hajó akkor minősül elsüllyedtnek, ha az összes koordinátáját eltávolították (kilőtték).
     * 
     * @return {@code true}, ha a hajó elsüllyedt (a koordináták listája üres), egyébként {@code false}.
     */
    public boolean isSunken() {
        return coordinates.isEmpty();
    }
}
