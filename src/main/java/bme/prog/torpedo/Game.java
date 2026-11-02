package bme.prog.torpedo;

public class Game {
    private final Board playerBoard;
    private final Board enemyBoard;
    private boolean isPlayerTurn;

    public Game(Board p, Board e){
        playerBoard=p;
        enemyBoard=e;
        isPlayerTurn=true;
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
}
