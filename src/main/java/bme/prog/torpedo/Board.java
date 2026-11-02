package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {
    private final Set<Coordinate> firedShots;
    private final List<Ship> ships;
    public Board() {
        this.ships = new ArrayList<>();
        this.firedShots = new HashSet<>();
    }

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

    
}
