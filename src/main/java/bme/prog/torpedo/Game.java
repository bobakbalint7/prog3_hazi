package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bme.prog.torpedo.Ship.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bme.prog.torpedo.Ship.Direction;

/**
 * A Torpedó játék központi vezérlője (Modell / Controller).
 * Felelős a táblák kezeléséért, a különleges lövésekért, a körök váltásáért,
 * valamint a számítógépes ellenfél (AI) vadászó és véletlenszerű logikájáért.
 */
public class Game {

    private final Board playerBoard;
    private final Board enemyBoard;
    private boolean isPlayerTurn;

    // --- BOT INTELLIGENS MEMÓRIÁJA (Vadászat) ---
    private final Random random;
    /** Tárolja, hova lőtt már a gép, hogy soha ne lőjön kétszer ugyanoda. */
    private final List<Coordinate> botFiredShots;
    /** Igaz, ha a gép épp egy megsebzett hajót próbál elsüllyeszteni. */
    private boolean isHunting;
    /** A hajó legelső eltalált koordinátája (ide tér vissza irányváltáskor). */
    private Coordinate firstHit;
    /** A legutóbbi sikeres találat koordinátája (innen lép tovább). */
    private Coordinate lastHit;
    /** A jelenlegi lövési irány (0: Jobbra, 1: Le, 2: Balra, 3: Fel). */
    private int huntDirection;
    /** Számon tartja, hány irányt próbált már ki a gép a 4-ből. */
    private int directionsTried;

    // --- KÜLÖNLEGES LÖVÉSEK KÉSZLETE (Lőszer) ---
    private int playerSpec1; // Játékos 1. extrája (Szőnyegbomba - egész hajó kilövése)
    private int playerSpec2; // Játékos 2. extrája (Célzott lövés - 3x3-as keresés)
    private int botSpec1;    // Gép 1. extrája
    private int botSpec2;    // Gép 2. extrája

    /** Milyen fegyverrel lő a játékos a következő kattintásnál? (0=Sima, 1=Szőnyeg, 2=Célzott) */
    private int activePlayerShot;

    /**
     * Létrehoz egy új játékot, inicializálja a táblákat és a gép memóriáját.
     */
    public Game() {
        this.playerBoard = new Board();
        this.enemyBoard = new Board();
        this.isPlayerTurn = true;

        this.random = new Random();
        this.botFiredShots = new ArrayList<>();
        this.isHunting = false;
        this.firstHit = null;
        this.lastHit = null;
        this.huntDirection = 0;
        this.directionsTried = 0;

        this.playerSpec1 = 0;
        this.playerSpec2 = 0;
        this.botSpec1 = 0;
        this.botSpec2 = 0;
        this.activePlayerShot = 0;
    }

    // --- SETTEREK ÉS GETTEREK A KÜLÖNLEGES LÖVÉSEKHEZ ---

    /** A SetupWindow hívja meg: beállítja az 1. különleges lövés darabszámát mindkét félnek. */
    public void setSpecialShot1Count(int count) {
        this.playerSpec1 = count;
        this.botSpec1 = count;
    }

    /** A SetupWindow hívja meg: beállítja a 2. különleges lövés darabszámát mindkét félnek. */
    public void setSpecialShot2Count(int count) {
        this.playerSpec2 = count;
        this.botSpec2 = count;
    }

    /** Beállítja, hogy a következő kattintásnál milyen fegyvert használjon a játékos. */
    public void setActivePlayerShot(int type) {
        this.activePlayerShot = type;
    }

    public int getPlayerSpec1() {
        return playerSpec1;
    }

    public int getPlayerSpec2() {
        return playerSpec2;
    }

    // --- TÁBLA ÉS ÁLLAPOT KEZELÉS ---

    public void placePlayerShip(Ship playerShip) {
        playerBoard.placeShip(playerShip);
    }

    public void placeEnemyShip(Ship ship) {
        enemyBoard.placeShip(ship);
    }

    public boolean isGameOver() {
        return playerBoard.areAllShipsSunk() || enemyBoard.areAllShipsSunk();
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public boolean hasPlayerShipAt(Coordinate c) {
        return playerBoard.hasShipAt(c);
    }

    // --- JÁTÉKOS LÖVÉSE ---

    /**
     * Végrehajtja a játékos lövését az ellenséges táblán az aktív fegyverrel.
     * 
     * @param target A megkattintott célkoordináta.
     * @return A találatokat és mellélövéseket tartalmazó ShotResult csomag.
     */
    public ShotResult playTurn(Coordinate target) {
        if (isGameOver()) {
            throw new IllegalStateException("A játéknak már vége van!");
        }

        ShotResult result = executeShot(enemyBoard, target, activePlayerShot);

        // Lőszer levonása és fegyver visszaállítása alaphelyzetbe (0)
        if (activePlayerShot == 1) playerSpec1--;
        if (activePlayerShot == 2) playerSpec2--;
        activePlayerShot = 0;

        isPlayerTurn = false;
        return result;
    }

    // --- GÉP (BOT) LÖVÉSE: VADÁSZAT + RANDOM + 20% SPECIÁLIS ---

    /**
     * Végrehajtja a gép lépését:
     * 1. A kör elején 20% eséllyel kiválaszt egy elérhető különleges lövést.
     * 2. Ha vadászik (isHunting), a megsebzett hajó körül próbálkozik 4 irányban;
     *    különben véletlenszerűen lő egy még érintetlen mezőre.
     * 3. A találat eredménye alapján frissíti a vadász-memóriát.
     * 
     * @return A gép lövésének eredményét tartalmazó ShotResult csomag.
     */
    public ShotResult playBotTurn() {
        int botShotType = 0;

        // 1. 20% esély különleges lövésre a kör elején, ha van még lőszere
        if (random.nextInt(100) < 20) {
            if (botSpec1 > 0 && botSpec2 > 0) {
                botShotType = random.nextBoolean() ? 1 : 2;
            } else if (botSpec1 > 0) {
                botShotType = 1;
            } else if (botSpec2 > 0) {
                botShotType = 2;
            }
        }

        Coordinate target = null;

        // 2. Célpont kiválasztása: Vadászat VAGY Véletlen lövés
        if (isHunting) {
            boolean validTargetFound = false;

            while (!validTargetFound && directionsTried < 4) {
                int r = lastHit.getRow();
                int c = lastHit.getCol();

                if (huntDirection == 0) c++;      // 0: Jobbra
                else if (huntDirection == 1) r++; // 1: Le
                else if (huntDirection == 2) c--; // 2: Balra
                else if (huntDirection == 3) r--; // 3: Fel

                Coordinate candidate = new Coordinate(r, c);

                // Csak akkor jó a célpont, ha pályán belül van ÉS még nem lőtt oda a gép
                if (isValidCoordinate(candidate) && !hasBotShotAt(candidate)) {
                    target = candidate;
                    validTargetFound = true;
                } else {
                    // Falnak ütközött vagy már lőtt ide -> irányváltás az első találattól!
                    changeHuntDirection();
                }
            }

            // Ha mind a 4 irány elakadt, kilépünk a vadászatból és random lövünk
            if (!validTargetFound) {
                resetHuntingMemory();
                target = getRandomUnshotCoordinate();
            }
        } else {
            // Alapállapot: véletlenszerű lövés egy még nem lőtt mezőre
            target = getRandomUnshotCoordinate();
        }

        // 3. Lövés végrehajtása és lőszer levonása
        ShotResult result = executeShot(playerBoard, target, botShotType);
        if (botShotType == 1) botSpec1--;
        if (botShotType == 2) botSpec2--;

        // Eltároljuk a kilőtt mezőket a gép memóriájában, hogy ne lőjön ide újra
        for (Coordinate hitC : result.hitCells) {
            recordBotShot(hitC);
        }
        for (Coordinate missC : result.missedCells) {
            recordBotShot(missC);
        }

        // 4. Vadász-memória frissítése a lövés eredménye alapján
        if (!result.hitCells.isEmpty()) {
            // Tényleges találati pont (Célzott lövésnél ez lehet a szomszédos mező is!)
            Coordinate actualHit = result.hitCells.get(0);
            Ship hitShip = playerBoard.getShipAt(actualHit);

            if (hitShip != null && hitShip.isSunken()) {
                // Ha a hajó elsüllyedt (pl. utolsó találat vagy Szőnyegbomba), vége a vadászatnak
                resetHuntingMemory();
            } else {
                // A hajó megsérült, de még nem süllyedt el -> Vadászat indul vagy folytatódik!
                if (!isHunting) {
                    isHunting = true;
                    firstHit = actualHit;
                    lastHit = actualHit;
                    huntDirection = 0;
                    directionsTried = 0;
                } else {
                    lastHit = actualHit;
                }
            }
        } else {
            // Mellélövés (Víz): ha épp vadászott, akkor rossz irányba ment, forduljon meg!
            if (isHunting) {
                changeHuntDirection();
            }
        }

        isPlayerTurn = true;
        return result;
    }

    // --- UNIVERZÁLIS LÖVÉS-VÉGREHAJTÓ LOGIKA ---

    /**
     * Végrehajtja a megadott típusú lövést a céltáblán.
     * 
     * @param targetBoard A tábla, amire a lövés érkezik.
     * @param target A célzott koordináta.
     * @param shotType 0 = Sima lövés, 1 = Szőnyegbomba, 2 = Célzott (3x3) lövés.
     * @return A eltalált és mellélőtt mezők listája.
     */
    private ShotResult executeShot(Board targetBoard, Coordinate target, int shotType) {
        ShotResult result = new ShotResult();

        if (shotType == 1) { // 1. SZŐNYEGBOMBÁZÁS
            Ship ship = targetBoard.getShipAt(target);
            if (ship != null) {
                // Készítünk egy másolatot a listáról, 
                // miközben a hitTaken() egyesével törölgeti őket belőle!
                List<Coordinate> coordsCopy = new ArrayList<>(ship.getCoordinates());
                
                for (Coordinate c : coordsCopy) {
                    result.hitCells.add(c);
                    registerHitOnBoard(targetBoard, ship, c);
                }
            } else {
                result.missedCells.add(target);
            }
        }
        else if (shotType == 2) { // 2. CÉLZOTT LÖVÉS: Mellélövéskor 3x3-as környezetben keres 1 hajócellát
            Ship ship = targetBoard.getShipAt(target);
            if (ship != null) {
                result.hitCells.add(target);
                registerHitOnBoard(targetBoard, ship, target);
            } else {
                result.missedCells.add(target);
                boolean found = false;

                for (int r = target.getRow() - 1; r <= target.getRow() + 1; r++) {
                    for (int c = target.getCol() - 1; c <= target.getCol() + 1; c++) {
                        Coordinate adj = new Coordinate(r, c);
                        if (isValidCoordinate(adj)) {
                            Ship adjShip = targetBoard.getShipAt(adj);
                            if (adjShip != null) {
                                result.hitCells.add(adj);
                                registerHitOnBoard(targetBoard, adjShip, adj);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) break; // Csak az első megtalált hajócellát lövi ki
                }
            }
        } 
        else { // 0. SIMA LÖVÉS
            Ship ship = targetBoard.getShipAt(target);
            if (ship != null) {
                result.hitCells.add(target);
                registerHitOnBoard(targetBoard, ship, target);
            } else {
                result.missedCells.add(target);
            }
        }

        return result;
    }

    /**
     * Központi segédmetódus a találat elkönyvelésére a Modellben.
     * (Ha a te Ship vagy Board osztályodban máshogy hívják a sebzés metódust,
     * csak ebben az egy metódusban kell átírnod!)
     */
    private void registerHitOnBoard(Board board, Ship ship, Coordinate hitCoord) {
        ship.hitTaken(hitCoord); // VAGY: board.shoot(hitCoord); / ship.hit(hitCoord);
    }

    // --- BOT VADÁSZAT ÉS KOORDINÁTA SEGÉDMETÓDUSOK ---

    private void changeHuntDirection() {
        huntDirection = (huntDirection + 1) % 4;
        directionsTried++;
        lastHit = firstHit; // Visszaugrunk az első találatra, és onnan indulunk az új irányba
    }

    private void resetHuntingMemory() {
        isHunting = false;
        firstHit = null;
        lastHit = null;
        huntDirection = 0;
        directionsTried = 0;
    }

    private Coordinate getRandomUnshotCoordinate() {
        for (int attempts = 0; attempts < 1000; attempts++) {
            Coordinate c = new Coordinate(random.nextInt(10), random.nextInt(10));
            if (!hasBotShotAt(c)) {
                return c;
            }
        }
        // Tartalék ág, ha szinte minden mezőre lőtt már
        for (int r = 0; r < 10; r++) {
            for (int col = 0; col < 10; col++) {
                Coordinate c = new Coordinate(r, col);
                if (!hasBotShotAt(c)) {
                    return c;
                }
            }
        }
        return new Coordinate(0, 0);
    }

    private boolean hasBotShotAt(Coordinate target) {
        for (Coordinate c : botFiredShots) {
            if (c.getRow() == target.getRow() && c.getCol() == target.getCol()) {
                return true;
            }
        }
        return false;
    }

    private void recordBotShot(Coordinate target) {
        if (!hasBotShotAt(target)) {
            botFiredShots.add(target);
        }
    }

    private boolean isValidCoordinate(Coordinate c) {
        return c.getRow() >= 0 && c.getRow() <= 9 && c.getCol() >= 0 && c.getCol() <= 9;
    }

    // --- A BOT HAJÓINAK AUTOMATIKUS LERAKÁSA ---

    public void placeBotShipsRandomly(int count5, int count4, int count3, int count2) {
        placeRandomShipsOfLength(5, count5);
        placeRandomShipsOfLength(4, count4);
        placeRandomShipsOfLength(3, count3);
        placeRandomShipsOfLength(2, count2);
    }

    private void placeRandomShipsOfLength(int length, int count) {
        for (int i = 0; i < count; i++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 1000) {
                attempts++;
                int r = random.nextInt(10);
                int c = random.nextInt(10);
                Direction dir = random.nextBoolean() ? Direction.HORIZONTAL : Direction.VERTICAL;

                try {
                    Ship botShip = Ship.createShip(r, c, length, dir);
                    placeEnemyShip(botShip);
                    placed = true;
                } catch (IllegalArgumentException e) {
                    // Ütközés vagy pályáról lelógás esetén újrapróbálkozik
                }
            }

            if (!placed) {
                System.out.println("Figyelmeztetés: A gép kihagyott egy " + length +
                                   " hosszú hajót, mert 1000 próbából sem fért el a pályán!");
            }
        }
    }

    // --- DTO ADATCSOMAG A GUI SZÁMÁRA ---

    public static class ShotResult {
        public List<Coordinate> hitCells = new ArrayList<>();
        public List<Coordinate> missedCells = new ArrayList<>();
    }
}

