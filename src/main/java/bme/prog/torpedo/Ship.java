package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final List<Coordinate> coordinates;

    
    public Ship(List<Coordinate> startingCoordinates) {
        this.coordinates = new ArrayList<>(startingCoordinates);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
    public boolean hitTaken(Coordinate c){
        return coordinates.remove(c);
    }
    public boolean isSunken(){
        return coordinates.isEmpty();
    }
}
