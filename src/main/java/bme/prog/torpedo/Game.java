package bme.prog.torpedo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bme.prog.torpedo.Ship.Direction;

import java.io.Serializable;

/**
 * A Torpedó játék központi vezérlője (Modell / Controller).
 * Felelős a táblák kezeléséért, a különleges lövésekért, a körök váltásáért,
 * valamint a számítógépes ellenfél (AI) intelligens vadászó és véletlenszerű logikájáért.
 * Megvalósítja a Serializable interfészt, így a teljes játékállapot egyetlen lépésben fájlba menthető.
 */
public class Game implements Serializable {

    /** A játékos (felhasználó) saját játéktáblája. */
    private final Board playerBoard;
    
    /** A számítógépes ellenfél (AI) játéktáblája. */
    private final Board enemyBoard;
    
    /** Igaz, ha jelenleg a játékos köre van, hamis, ha a gépé. */
    private boolean isPlayerTurn;

    // --- VIZUÁLIS TÖRTÉNELEM A BETÖLTÉSHEZ ---
    
    /** A játékos sikeres (talált) lövéseinek koordinátái az ellenséges táblán. */
    private final List<Coordinate> playerHitCells = new ArrayList<>();
    
    /** A játékos sikertelen (vízbe esett) lövéseinek koordinátái az ellenséges táblán. */
    private final List<Coordinate> playerMissedCells = new ArrayList<>();
    
    /** A gép sikeres (talált) lövéseinek koordinátái a játékos tábláján. */
    private final List<Coordinate> botHitCells = new ArrayList<>();
    
    /** A gép sikertelen (vízbe esett) lövéseinek koordinátái a játékos tábláján. */
    private final List<Coordinate> botMissedCells = new ArrayList<>();

    public List<Coordinate> getPlayerHitCells() { return playerHitCells; }
    public List<Coordinate> getPlayerMissedCells() { return playerMissedCells; }
    public List<Coordinate> getBotHitCells() { return botHitCells; }
    public List<Coordinate> getBotMissedCells() { return botMissedCells; }

    // --- BOT INTELLIGENS MEMÓRIÁJA (Vadászat) ---
    private final Random random;
    
    /** Tárolja, hova lőtt már a gép, hogy elkerülje ugyanannak a mezőnek a kétszeri támadását. */
    private final List<Coordinate> botFiredShots;
    
    /** Igaz, ha a gép épp egy megsebzett hajót próbál szisztematikusan elsüllyeszteni. */
    private boolean isHunting;
    
    /** A hajó legelső eltalált koordinátája (ide tér vissza a gép irányváltáskor). */
    private Coordinate firstHit;
    
    /** A hajó legutóbbi sikeres találatának koordinátája (innen lép tovább az adott irányba). */
    private Coordinate lastHit;
    
    /** A jelenlegi lövési irány a vadászat során (0: Jobbra, 1: Le, 2: Balra, 3: Fel). */
    private int huntDirection;
    
    /** Számon tartja, hány irányt próbált már ki a gép a 4 lehetségesből. */
    private int directionsTried;

    // --- KÜLÖNLEGES LÖVÉSEK KÉSZLETE (Lőszer) ---
    
    /** A játékos 1. típusú különleges képességének (Szőnyegbomba) megmaradt lőszere. */
    private int playerSpec1; 
    
    /** A játékos 2. típusú különleges képességének (Célzott lövés) megmaradt lőszere. */
    private int playerSpec2; 
    
    /** A gép 1. típusú különleges képességének (Szőnyegbomba) megmaradt lőszere. */
    private int botSpec1;    
    
    /** A gép 2. típusú különleges képességének (Célzott lövés) megmaradt lőszere. */
    private int botSpec2;    

    /** Milyen fegyverrel lő a játékos a következő kattintásnál? (0=Sima, 1=Szőnyeg, 2=Célzott) */
    private int activePlayerShot;

    /**
     * Létrehoz egy új játékot, inicializálja a táblákat és a gép intelligens memóriáját.
     * Alapértelmezés szerint a játékos kezdi a mérkőzést, és az extra lőszerek száma 0.
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

    /** 
     * Beállítja az 1. különleges lövés (Szőnyegbomba) darabszámát mindkét félnek.
     * @param count Az induló lőszermennyiség.
     */
    public void setSpecialShot1Count(int count) {
        this.playerSpec1 = count;
        this.botSpec1 = count;
    }

    /** 
     * Beállítja a 2. különleges lövés (Célzott lövés) darabszámát mindkét félnek.
     * @param count Az induló lőszermennyiség.
     */
    public void setSpecialShot2Count(int count) {
        this.playerSpec2 = count;
        this.botSpec2 = count;
    }

    /** 
     * Beállítja, hogy a következő kattintásnál milyen fegyvert használjon a játékos.
     * @param type A fegyver típusa (0: Sima, 1: Szőnyegbomba, 2: Célzott lövés).
     */
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

    /**
     * Elhelyez egy megadott hajót a játékos tábláján.
     * @param playerShip A lerakandó hajó.
     */
    public void placePlayerShip(Ship playerShip) {
        playerBoard.placeShip(playerShip);
    }

    /**
     * Elhelyez egy megadott hajót a gép (ellenség) tábláján.
     * @param ship A lerakandó hajó.
     */
    public void placeEnemyShip(Ship ship) {
        enemyBoard.placeShip(ship);
    }

    /**
     * Ellenőrzi, hogy a mérkőzés véget ért-e.
     * @return {@code true}, ha valamelyik játékos összes hajója elsüllyedt, egyébként {@code false}.
     */
    public boolean isGameOver() {
        return playerBoard.areAllShipsSunk() || enemyBoard.areAllShipsSunk();
    }

    /**
     * Lekérdezi, hogy jelenleg a játékos köre van-e.
     * @return {@code true}, ha a játékos jön, {@code false}, ha a gép.
     */
    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    /**
     * Ellenőrzi, hogy a játékos tábláján az adott koordinátán van-e hajó.
     * Szükséges a grafikus felület felépítéséhez és színezéséhez betöltéskor.
     * @param c A vizsgálandó koordináta.
     * @return {@code true}, ha van ott hajó, egyébként {@code false}.
     */
    public boolean hasPlayerShipAt(Coordinate c) {
        return playerBoard.hasShipAt(c);
    }

    // --- JÁTÉKOS LÖVÉSE ---

    /**
     * Végrehajtja a játékos lövését az ellenséges táblán az aktuálisan kiválasztott fegyverrel.
     * Kiszámítja az eltalált és mellélőtt mezőket, levonja az extra lőszert (ha használt),
     * elmenti a lövés eredményét a történelembe, majd átadja a kört a gépnek.
     * 
     * @param target A megkattintott célkoordináta a grafikus felületen.
     * @return A találatokat és mellélövéseket tartalmazó {@code ShotResult} DTO csomag.
     * @throws IllegalStateException Ha a játéknak már vége van, de újabb lövés történik.
     */
    public ShotResult playTurn(Coordinate target) {
        if (isGameOver()) {
            throw new IllegalStateException("A játéknak már vége van!");
        }

        ShotResult result = executeShot(enemyBoard, target, activePlayerShot);

        // Eltároljuk a történelmet a jövőbeli mentés/betöltéshez
        playerHitCells.addAll(result.hitCells);
        playerMissedCells.addAll(result.missedCells);

        // Lőszer levonása és fegyver visszaállítása alaphelyzetbe (0)
        if (activePlayerShot == 1) playerSpec1--;
        if (activePlayerShot == 2) playerSpec2--;
        activePlayerShot = 0;

        isPlayerTurn = false;
        return result;
    }

    // --- GÉP (BOT) LÖVÉSE: VADÁSZAT + RANDOM + 20% SPECIÁLIS ---

    /**
     * Végrehajtja a számítógépes ellenfél (AI) lépését.
     * A gép intelligens logikája alapján lő:
     * 1. 20% eséllyel kiválaszt egy elérhető különleges képességet (ha nem vadászik éppen).
     * 2. Ha a gép "vadászik" (talált egy hajót, de még nem süllyesztette el), megpróbálja 
     *    4 irányba letapogatni a hajó testét.
     * 3. Ha nem vadászik, egy teljesen véletlenszerű, még nem lőtt mezőre tüzel.
     * 4. A lövés eredménye alapján frissíti a vadász-memóriát és a játékállapotot.
     * 
     * @return A gép lövésének eredményét tartalmazó {@code ShotResult} DTO csomag.
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

        // Gép lövéseinek vizuális eltárolása (betöltéshez)
        botHitCells.addAll(result.hitCells);
        botMissedCells.addAll(result.missedCells);

        // Eltároljuk a kilőtt mezőket a gép memóriájában, hogy logikailag ne lőjön ide újra
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
     * Végrehajtja a megadott típusú lövést a cél táblán.
     * Tartalmazza a Szőnyegbombázás, a Célzott lövés és a Sima lövés szabályrendszerét.
     * 
     * @param targetBoard A tábla (Board), amire a lövés érkezik.
     * @param target A lövés célkoordinátája.
     * @param shotType A fegyver típusa (0 = Sima lövés, 1 = Szőnyegbomba, 2 = Célzott lövés).
     * @return Az eltalált (hitCells) és mellélőtt (missedCells) mezők listáját tartalmazó ShotResult.
     */
    private ShotResult executeShot(Board targetBoard, Coordinate target, int shotType) {
        ShotResult result = new ShotResult();

        if (shotType == 1) { // 1. SZŐNYEGBOMBÁZÁS
            Ship ship = targetBoard.getShipAt(target);
            if (ship != null) {
                // Készítünk egy másolatot a listáról, hogy elkerüljük a ConcurrentModificationException hibát,
                // miközben a hitTaken() egyesével törölgeti a koordinátákat az eredeti listából.
                List<Coordinate> coordsCopy = new ArrayList<>(ship.getCoordinates());
                
                for (Coordinate c : coordsCopy) {
                    result.hitCells.add(c);
                    registerHitOnBoard(targetBoard, ship, c);
                }
            } else {
                result.missedCells.add(target);
            }
        }
        else if (shotType == 2) { // 2. CÉLZOTT LÖVÉS
            Ship ship = targetBoard.getShipAt(target);
            if (ship != null) {
                // Sima telitalálat
                result.hitCells.add(target);
                registerHitOnBoard(targetBoard, ship, target);
            } else {
                result.missedCells.add(target);
                boolean found = false;

                // Mellélövéskor a lövés 3x3-as környezetében keres 1 darab hajócellát
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
     * Meghívja az adott hajó {@code hitTaken} metódusát a koordináta törléséhez.
     * 
     * @param board A tábla, amelyen a találat történt.
     * @param ship A találatot elszenvedő hajó.
     * @param hitCoord A találat pontos koordinátája.
     */
    private void registerHitOnBoard(Board board, Ship ship, Coordinate hitCoord) {
        ship.hitTaken(hitCoord);
    }

    // --- BOT VADÁSZAT ÉS KOORDINÁTA SEGÉDMETÓDUSOK ---

    /**
     * Irányt vált a gép vadászatánál (Jobbra -> Le -> Balra -> Fel).
     * Ha a gép vizet ér vagy eléri a pálya szélét, meghívja ezt a metódust,
     * hogy visszatérjen az első találathoz és új irányban kezdjen keresni.
     */
    private void changeHuntDirection() {
        huntDirection = (huntDirection + 1) % 4;
        directionsTried++;
        lastHit = firstHit; // Visszaugrunk az első találatra, és onnan indulunk az új irányba
    }

    /**
     * Alaphelyzetbe állítja a gép vadász-memóriáját, miután sikeresen elsüllyesztett egy hajót,
     * vagy ha az algoritmus teljesen beragadt.
     */
    private void resetHuntingMemory() {
        isHunting = false;
        firstHit = null;
        lastHit = null;
        huntDirection = 0;
        directionsTried = 0;
    }

    /**
     * Keres egy véletlenszerű, 10x10-es pályán lévő koordinátát, amire a gép még nem lőtt korábban.
     * 
     * @return Egy még érintetlen célkoordináta.
     */
    private Coordinate getRandomUnshotCoordinate() {
        for (int attempts = 0; attempts < 1000; attempts++) {
            Coordinate c = new Coordinate(random.nextInt(10), random.nextInt(10));
            if (!hasBotShotAt(c)) {
                return c;
            }
        }
        // Tartalék ág, ha szinte minden mezőre lőtt már a gép
        for (int r = 0; r < 10; r++) {
            for (int col = 0; col < 10; col++) {
                Coordinate c = new Coordinate(r, col);
                if (!hasBotShotAt(c)) {
                    return c;
                }
            }
        }
        return new Coordinate(0, 0); // Biztonsági visszatérés, ami gyakorlatban sosem fut le
    }

    /**
     * Ellenőrzi, hogy a gép lőtt-e már korábban az adott koordinátára.
     * 
     * @param target A vizsgálandó koordináta.
     * @return {@code true}, ha már lőtt ide, egyébként {@code false}.
     */
    private boolean hasBotShotAt(Coordinate target) {
        for (Coordinate c : botFiredShots) {
            if (c.getRow() == target.getRow() && c.getCol() == target.getCol()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Feljegyzi az adott koordinátát a gép lőtt mezőinek memóriájába (ha még nem szerepel benne).
     * 
     * @param target Az eltárolandó koordináta.
     */
    private void recordBotShot(Coordinate target) {
        if (!hasBotShotAt(target)) {
            botFiredShots.add(target);
        }
    }

    /**
     * Ellenőrzi, hogy egy adott koordináta a 10x10-es játéktábla határain belül esik-e.
     * 
     * @param c A vizsgálandó koordináta.
     * @return {@code true}, ha a koordináta érvényes, egyébként {@code false}.
     */
    private boolean isValidCoordinate(Coordinate c) {
        return c.getRow() >= 0 && c.getRow() <= 9 && c.getCol() >= 0 && c.getCol() <= 9;
    }

    // --- A BOT HAJÓINAK AUTOMATIKUS LERAKÁSA ---

    /**
     * Véletlenszerűen elhelyezi a számítógépes ellenfél hajóit a táblán 
     * a megadott darabszámok alapján.
     * 
     * @param count5 Az 5 hosszú hajók száma.
     * @param count4 A 4 hosszú hajók száma.
     * @param count3 A 3 hosszú hajók száma.
     * @param count2 A 2 hosszú hajók száma.
     */
    public void placeBotShipsRandomly(int count5, int count4, int count3, int count2) {
        placeRandomShipsOfLength(5, count5);
        placeRandomShipsOfLength(4, count4);
        placeRandomShipsOfLength(3, count3);
        placeRandomShipsOfLength(2, count2);
    }

    /**
     * Megpróbál lerakni egy megadott mennyiségű, adott hosszúságú hajót a gép táblájára.
     * Ha a hajó lelóg a pályáról vagy ütközik egy másikkal, automatikusan újrapróbálja (maximum 1000 alkalommal).
     * 
     * @param length A generálandó hajók hossza.
     * @param count A generálandó hajók darabszáma.
     */
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
                    // Ütközés vagy pályáról lelógás esetén némán újrapróbálkozik
                }
            }

            if (!placed) {
                System.out.println("Figyelmeztetés: A gép kihagyott egy " + length +
                                   " hosszú hajót, mert 1000 próbából sem fért el a pályán!");
            }
        }
    }

    // --- DTO ADATCSOMAG A GUI SZÁMÁRA ---

    /**
     * Egy egyszerű adatszállító objektum (DTO), amely egyetlen lövés (kör) kimenetelét tartalmazza.
     * Segítségével a Modell (Game) egyszerre több mező átszínezésére is utasíthatja a Nézetet (GUI).
     */
    public static class ShotResult {
        
        /** A lövés által sikeresen eltalált hajócellák (koordináták) listája. */
        public List<Coordinate> hitCells = new ArrayList<>();
        
        /** A lövés által érintett, vízbe esett (mellélőtt) cellák listája. */
        public List<Coordinate> missedCells = new ArrayList<>();
    }
}
