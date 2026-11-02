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

    public void placeShip(Ship newShip){
        //Check if it's within boundaries
        for(Coordinate c: newShip.getCoordinates()){
            if(c.getRow()<0 || c.getRow()>9 || c.getCol()<0 ||c.getCol()>9){
                throw new IllegalArgumentException("You placed the ship outside of boundaries!");
            }
        }
        //Check if it was placed on another ship
        for(Ship ship : ships){
            for(Coordinate c: newShip.getCoordinates()){
                if(ship.getCoordinates().contains(c)){
                    throw new IllegalArgumentException("You placed the new ship on top of an already placed one!");
                }
            }
        }

        ships.add(newShip);
    }
    //If there's even a single ship unsunk, it returns false
    public boolean areAllShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunken()) {
                return false;
            }
        }
        return true;
    }
}
