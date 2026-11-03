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
                throw new IllegalArgumentException("You placed the ship outside of boundaries!");
            }
        }
        
        
        for(Ship ship : ships){
            for(Coordinate c: newShip.getCoordinates()){
                if(ship.getCoordinates().contains(c)){
                    throw new IllegalArgumentException("You placed the new ship on top of an already placed one!");
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
}