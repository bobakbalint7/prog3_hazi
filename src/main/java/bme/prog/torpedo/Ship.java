package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final List<Coordinate> coordinates;

    public enum Direction{
        HORIZONTAL,
        VERTICAL
    }
    public Ship(List<Coordinate> startingCoordinates) {
        this.coordinates = new ArrayList<>(startingCoordinates);
    }
    //Builds the ship, starts building in the given direction, from the startpoint
    public static Ship createShip(int startRow, int startCol, int length, Direction dir){
        List<Coordinate> generatedCoords = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (dir == Direction.HORIZONTAL) {
                generatedCoords.add(new Coordinate(startRow, startCol + i));
            } else {
                generatedCoords.add(new Coordinate(startRow + i, startCol));
            }
        }
        
        return new Ship(generatedCoords);
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
