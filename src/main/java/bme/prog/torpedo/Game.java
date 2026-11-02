package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private final Board playerBoard;
    private final Board enemyBoard;
    private boolean isPlayerTurn;

    //For the bot's random hits
    private final Random random;
    //For the bot's smart hit
    private final List<Coordinate> smartTargets;
    public Game(){
        playerBoard=new Board();
        enemyBoard=new Board();
        isPlayerTurn=true;

        this.random = new Random();
        this.smartTargets = new ArrayList<>();
    }
    //places the ship on player's ship
    public void placePlayerShip(Ship playerShip){
        playerBoard.placeShip(playerShip);
    }
    //same with the enemy
    public void placeEnemyShip(Ship ship) {
        enemyBoard.placeShip(ship);
    }
    //a complete turn 
    public boolean playTurn(Coordinate target) {
        if (isGameOver()) {
            throw new IllegalStateException("A játéknak már vége, nem lehet többet lőni!");
        }

        boolean wasHit;
        //shooting the enemy board
        if (isPlayerTurn) {
            wasHit = enemyBoard.hit(target);
        } else {
            wasHit = playerBoard.hit(target);
        }
        //changing turns
        isPlayerTurn = !isPlayerTurn;

        return wasHit;
    }


    //One of the boards is sunk
    public boolean isGameOver() {
        return playerBoard.areAllShipsSunk() || enemyBoard.areAllShipsSunk();
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }
    //The bots random, but also smart shot
    public void playBotTurn() {
        Coordinate target = null;
        //If the previous shot was a hit
        //If it's a hit for the third time, the shot isn't random,
        //but it goes in the same direction
        //e.g: 1. random shot is a hit 2. randomly shots one of the 4 directions
        //3. It isn't random, it goes further in the previous direction,
        //or else the shots could be in an 'L' shape which doesn't make sense
        while (!smartTargets.isEmpty() && target == null) {
            //Gets the last target, it's easy to remove
            Coordinate potentialTarget = smartTargets.remove(smartTargets.size() - 1);
            
            if (isValidCoordinate(potentialTarget) && !playerBoard.hasBeenShot(potentialTarget)) {
                target = potentialTarget;
            }
        }

       //The random shot
        while (target == null) {
            int r = random.nextInt(10); 
            int c = random.nextInt(10);
            Coordinate randomTarget = new Coordinate(r, c);
            
           
            if (!playerBoard.hasBeenShot(randomTarget)) {
                target = randomTarget;
            }
        }

       
        boolean wasHit = playTurn(target);

        //If it's a hit, we put the potential targets in the list
        if (wasHit) {
            int r = target.getRow();
            int c = target.getCol();
            smartTargets.add(new Coordinate(r - 1, c));
            smartTargets.add(new Coordinate(r + 1, c));
            smartTargets.add(new Coordinate(r, c - 1));
            smartTargets.add(new Coordinate(r, c + 1));
            java.util.Collections.shuffle(smartTargets);
        }
    }

    private boolean isValidCoordinate(Coordinate c) {
        return c.getRow() >= 0 && c.getRow() <= 9 && c.getCol() >= 0 && c.getCol() <= 9;
    }
}
