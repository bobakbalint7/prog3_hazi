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
}
