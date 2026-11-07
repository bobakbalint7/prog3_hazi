package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * A Torpedó játék központi vezérlője.
 * Felelős a táblák kezeléséért, a körök váltásáért, valamint a számítógépes ellenfél logikájáért.
 */
public class Game {
    private final Board playerBoard;
    private final Board enemyBoard;
    private boolean isPlayerTurn;

    //Bot memória
    private final Random random;
    /** Igaz, ha a gép épp egy megsebzett hajót próbál elsüllyeszteni (vadászik). */
    private boolean isHunting;
    /** A hajó legelső eltalált koordinátája (ide térünk vissza irányváltáskor). */
    private Coordinate firstHit;
    /** A legutóbbi sikeres találat koordinátája (innen lépünk tovább). */
    private Coordinate lastHit;
    /** A jelenlegi lövési irány (0: Jobbra, 1: Le, 2: Balra, 3: Fel). */
    private int huntDirection;
    /** Számon tartja, hány irányt próbált már ki a gép a 4-ből. */
    private int directionsTried;

    /**
     * Létrehoz egy új játékot, inicializálja a táblákat és a gép memóriáját.
     */
    public Game() {
        this.playerBoard = new Board();
        this.enemyBoard = new Board();
        this.isPlayerTurn = true;

        this.random = new Random();
        this.isHunting = false;
        this.firstHit = null;
        this.lastHit = null;
        this.huntDirection = 0;
        this.directionsTried = 0;
    }

    /**
     * Elhelyez egy hajót a játékos tábláján.
     * @param playerShip A lerakandó hajó.
     */
    public void placePlayerShip(Ship playerShip) {
        playerBoard.placeShip(playerShip);
    }

    /**
     * Elhelyez egy hajót az ellenfél (gép) tábláján.
     * @param ship A lerakandó hajó.
     */
    public void placeEnemyShip(Ship ship) {
        enemyBoard.placeShip(ship);
    }

    /**
     * Feldolgoz egy teljes kört: leadja a lövést az aktuális célpontra, majd átadja a kört.
     * @param target A célkoordináta, ahova a lövés érkezik.
     * @return true, ha a lövés talált, egyébként false.
     */
    public boolean playTurn(Coordinate target) {
        if (isGameOver()) {
            throw new IllegalStateException("A játéknak már vége, nem lehet többet lőni!");
        }

        boolean wasHit;
        if (isPlayerTurn) {
            wasHit = enemyBoard.hit(target);
        } else {
            wasHit = playerBoard.hit(target);
        }
        
        isPlayerTurn = !isPlayerTurn;
        return wasHit;
    }

    /**
     * Ellenőrzi, hogy véget ért-e a mérkőzés.
     * @return true, ha valamelyik játékos összes hajója elsüllyedt.
     */
    public boolean isGameOver() {
        return playerBoard.areAllShipsSunk() || enemyBoard.areAllShipsSunk();
    }

    /**
     * Lekérdezi, hogy a játékos köre van-e.
     * @return true, ha a játékos jön.
     */
    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    /**
     * A Bot  intelligens lövési logikája.
     * Ha találatot ér el, célirányosan követi a hajót, amíg az el nem süllyed.
     */
    public Coordinate playBotTurn() {
        Coordinate target = null;

        // 1. FÁZIS: Vadászat (ha korábban megsebeztünk egy hajót)
        if (isHunting) {
            // Eltolási vektorok: Jobbra(0, 1), Le(1, 0), Balra(0, -1), Fel(-1, 0)
            int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

            // Addig próbáljuk a jelenlegi irányt, amíg valid mezőt nem találunk, 
            // vagy amíg ki nem fogytunk a 4 irányból
            while (target == null && directionsTried < 4) {
                int r = lastHit.getRow() + dirs[huntDirection][0];
                int c = lastHit.getCol() + dirs[huntDirection][1];
                Coordinate potentialTarget = new Coordinate(r, c);

                if (isValidCoordinate(potentialTarget) && !playerBoard.hasBeenShot(potentialTarget)) {
                    target = potentialTarget; // Megvan a jó célpont!
                } else {
                    // Ha falba ütköztünk, vagy ide már lőttünk: irányváltás és visszaugrás az eredeti találathoz
                    huntDirection = (huntDirection + 1) % 4;
                    lastHit = firstHit;
                    directionsTried++;
                }
            }

            // Ha mind a 4 irány zsákutca (a hajó valószínűleg elsüllyedt), visszatérünk vak lövésre
            if (directionsTried >= 4) {
                isHunting = false;
            }
        }

        // 2. FÁZIS: Vak lövés (ha nem vadászunk)
        while (target == null) {
            int r = random.nextInt(10);
            int c = random.nextInt(10);
            Coordinate randomTarget = new Coordinate(r, c);

            if (!playerBoard.hasBeenShot(randomTarget)) {
                target = randomTarget;
            }
        }

        // 3. Lövés leadása (ez automatikusan át is adja a kört a playTurn-ben)
        boolean wasHit = playTurn(target);

        // 4. FÁZIS: A memória frissítése az eredmény alapján
        if (wasHit) {
            //Megkérdezzük, hogy elsüllyedt-e az a hajó, amit most eltaláltunk
            if (playerBoard.isShipSunkAt(target)) {
                isHunting = false; //Elsüllyedt,  vadászat véget ér, legközelebb random lő.
            } 
            else {
                // Ha még nem süllyedt el, folytatjuk a vadászatot
                if (!isHunting) {
                    isHunting = true;
                    firstHit = target;
                    lastHit = target;
                    huntDirection = 0; 
                    directionsTried = 0;
                } else {
                    lastHit = target;
                    directionsTried = 0; 
                }
            }
        } else {
            // Ha mellélőttünk
            if (isHunting) {
                huntDirection = (huntDirection + 1) % 4;
                lastHit = firstHit; 
                directionsTried++;
                
            }
        }
        return target;
    }
    public boolean hasPlayerShipAt(Coordinate c) {
        return playerBoard.hasShipAt(c);
    }
    /**
     * Belső segédmetódus annak ellenőrzésére, hogy a koordináta a 10x10-es pályán belül van-e.
     */
    private boolean isValidCoordinate(Coordinate c) {
        return c.getRow() >= 0 && c.getRow() <= 9 && c.getCol() >= 0 && c.getCol() <= 9;
    }
}
