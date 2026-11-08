package bme.prog.torpedo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A játéktáblát reprezentáló osztály. 
 * Felelős a hajók tárolásáért, valamint a beérkező lövések kiértékeléséért.
 */
public class Board {
    
    /** A már leadott lövések koordinátáinak halmaza a duplikációk elkerülésére. */
    private final Set<Coordinate> firedShots;
    
    /** A táblán szabályosan elhelyezett hajók listája. */
    private final List<Ship> ships;

    /**
     * Létrehoz egy új, üres játéktáblát.
     * Inicializálja a hajók listáját és a leadott lövések halmazát.
     */
    public Board() {
        this.ships = new ArrayList<>();
        this.firedShots = new HashSet<>();
    }

    /**
     * Feldolgoz egy beérkező lövést a játéktáblán.
     * Ellenőrzi, hogy lőttek-e már ide, majd továbbadja a lövést a hajóknak.
     * 
     * @param c A lövés célkoordinátája.
     * @return true, ha a lövés eltalált egy hajót, egyébként false.
     */
    public boolean hit(Coordinate c){
        if(firedShots.contains(c)) return false;
        firedShots.add(c);

        for(Ship ship: ships){
            if(ship.hitTaken(c)){
                return true;
            }
        }

        return false;
    }

    /**
     * Elhelyez egy új hajót a játéktáblán.
     * Ellenőrzi, hogy a hajó a 10x10-es pályán belül van-e, 
     * és hogy nem ütközik-e egy már lent lévő hajóval.
     * 
     * @param newShip Az elhelyezni kívánt hajó.
     * @throws IllegalArgumentException Ha a hajó kilóg a pályáról, vagy másik hajóra kerülne.
     */
    public void placeShip(Ship newShip){
        
        for(Coordinate c: newShip.getCoordinates()){
            if(c.getRow()<0 || c.getRow()>9 || c.getCol()<0 ||c.getCol()>9){
                throw new IllegalArgumentException("A határokon kívülre helyeztél el hajót!");
            }
        }
        
        
        // Ellenőrizzük, hogy nem ütközik-e, vagy nem ér-e hozzá egy már lent lévő hajóhoz
        for (Ship existingShip : ships) {
            for (Coordinate newCoord : newShip.getCoordinates()) {
                for (Coordinate existingCoord : existingShip.getCoordinates()) {
                    
                    // Kiszámoljuk a sorok és az oszlopok távolságát
                    int rowDiff = Math.abs(newCoord.getRow() - existingCoord.getRow());
                    int colDiff = Math.abs(newCoord.getCol() - existingCoord.getCol());
                    
                    // Ha mindkét távolság legfeljebb 1, akkor a két koordináta vagy fedi egymást, vagy szomszédos
                    if (rowDiff <= 1 && colDiff <= 1) {
                        throw new IllegalArgumentException("A hajók nem érhetnek egymáshoz, és egymáson sem lehetnek!");
                    }
                    
                }
            }
        }

        ships.add(newShip);
    }

    /**
     * Ellenőrzi, hogy a táblán lévő összes hajó elsüllyedt-e már.
     * 
     * @return true, ha minden hajó elsüllyedt (tehát vége a játéknak), egyébként false.
     */
    public boolean areAllShipsSunk() {
        //ha még nincs hajó a táblán, nem lehet vége. Ez csak az első kipróbálásnál volt hiba.
        if (ships.isEmpty()) {
            return false; 
        }
        for (Ship ship : ships) {
            if (!ship.isSunken()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Lekérdezi, hogy egy adott koordinátára adtak-e már le lövést.
     * 
     * @param c A vizsgálandó koordináta.
     * @return true, ha a mezőt már lőtték, egyébként false.
     */
    public boolean hasBeenShot(Coordinate c) {
        return firedShots.contains(c);
    }

    /**
     * Megvizsgálja, hogy az adott koordinátán lévő hajó elsüllyedt-e.
     * @param c A vizsgálandó koordináta.
     * @return true, ha van ott hajó és az elsüllyedt.
     */
    public boolean isShipSunkAt(Coordinate c) {
        for (Ship ship : ships) {
            // Ha ez a hajó tartalmazza a koordinátát, megkérdezzük, hogy elsüllyedt-e
            if (ship.getCoordinates().contains(c)) {
                return ship.isSunken();
            }
        }
        return false;
    }
    /**
     * Lekérdezi, hogy a megadott koordinátán van-e hajó.
     * @param c A vizsgálandó koordináta.
     * @return true, ha van ott hajó (akár ép, akár sérült), egyébként false.
     */
    public boolean hasShipAt(Coordinate c) {
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(c)) {
                return true;
            }
        }
        return false;
    }

    public Ship getShipAt(Coordinate target) {
        for (Ship ship : ships) {
            for (Coordinate c : ship.getCoordinates()) {
                if (c.getRow() == target.getRow() && c.getCol() == target.getCol()) {
                    return ship;
                }
            }
        }
        return null;
    }
}