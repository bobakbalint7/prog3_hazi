# Specifikáció

1. A feladat ismertetése
A program a torpedó nevű játékot valósítja meg, ami az alábbiak szerint működik:
•	Kétszemélyes táblajáték, ebben a programban a felhasználó a gép ellen van. Mindkét játékosnak van 2 darab, egyenként 10x10-es táblája.
•	Az egyik táblán a saját hajói vannak, a másikon jelöli ki, hogy az ellenfél melyik cellájába lő.
•	1, 2, 3 és 4 cellát elfoglaló hajók vannak, melyek szélessége mindig 1 cella, tehát mindegyik egy vonalban helyezkedik el, valamint átlós sem lehet. A darabszámukat a játék kezdete előtt lehet kiválasztani.
•	Vagy a felhasználó maga helyezi el a hajókat, vagy a program, véletlenszerűen.
•	A lövéskor jelzi a program, hogy vízbe lőttünk-e, vagy hajót találtunk. Az utóbbi esetben a cél a hajó maradék részének a kilövése. Ha ez megtörtént, jelzi a program, hogy az adott hajó elsüllyedt.
•	A felhasználó és a gép felváltva lőnek, amíg valaki ki nem lövi a másik összes hajóját.
•	A gép véletlenszerűen generálja le a hajók felállását.
•	A hajók nem lóghatnak le a pályáról.
A hagyományos játékon felül két funkcióval egészül ki a program, melyekre igaz, hogy egy lövést egészítenek ki, és az adott lövés előtt kell jelezni a felhasználónak, hogy használni kívánja a funkciót: 
•	Azonnali süllyesztés: Ha a felhasználó eltalál egy hajót, akkor az egész hajót elsüllyeszti a lövés
•	Hajó-lokátor: Ha a felhasználó lő valahova, de nem talált, akkor, ha a közvetlen környezetében(3x3-mas négyzet) van hajó, azt lövi ki(legfeljebb 1 részét).
Mindkét funkció véges számú, mennyiségét a játék kezdete előtt választjuk ki, és a gép is használja, véletlenszerűen.




2. Use-case-ek kifejtése
-A felhasználó a főmenüből az ’’Új játék’’ opciót választja:
•	Beállító menü jelenik meg, ahol ki lehet választani legördülő menükből az egyes hajótípus darabszámát, valamint az Azonnali süllyesztés és a Hajó-lokátor számát.
-Flotta elhelyezése:
•	Manuális: A játéktér betöltése után a manuális opciót választva az egérrel helyezhetjük le a hajókat. A grafikus felületen ki lehet választani a lehelyezendő hajó típusát és irányát, valamint ellenőrzi, hogy szabályos-e a lerakás.
•	Automatikus: a program véletlenszerűen generál egy felállást, a szabályok szerint.
-Normál lövés:
•	Az ellenfél tábláján az egérrel kiválasztunk egy cellát, majd a program kiértékeli a lövést, amiből 3 féle van. Ha nem talált hajót, a Mellé jelzőt kapja a cella, ha talált, akkor a Találtat, ha a lövéssel elsüllyed a hajó, akkor az összes cellája a Süllyedtet. Már kilőtt mezőre szimplán nem lehet kattintani, nem csinál semmit a program.
-Speciális lövés:
•	Mindkettőnek külön gombja van, a lövés előtt a felhasználó, ha kijelöli valamelyiket, akkor ilyen típusú lesz a lövés. A kiválasztott lövés alapján kiértékeli a mezőket a program.
-Játékállás mentése
•	A Mentés opció választásakor.
•	Egy fájlba menti a jelenlegi játék minden adatát, megadhatjuk a mentés helyét egy fájlválasztó ablakban.
-Játékállás betöltése:
•	A felhasználó a főmenüből a Betöltés opciót választja
•	Fájlválasztó ablakban kiválasztjuk a betöltendő fájlt, majd a legutóbb félbehagyott állásból folytatódik a játék.



3. Megoldási ötlet:
-GUI: szabványos Java Swing keretrendszer. A játéktáblák egyéni rajzolással jelennek meg(Graphics osztály) egy-egy JPanel-en. Az interakciókat egéresemény-vezérlők (MouseListener) biztosítják. A beállításokhoz legördülő menüket (JComboBox) használ.
-Adatszerkezetek és Gyűjtemények: A játéklogika a Java Collections Framework elemeire épül. A játéklogika a Java Collections Framework elemeire épül. A flottában lévő hajókat, a meglőtt koordinátákat, valamint az ellenfél hátralévő lehetséges célpontjait különböző listák (ArrayList, LinkedList) és halmazok (HashSet) tárolják.
- Kivételkezelés: A felhasználói bemenetek (pl. helytelen fájl betöltése) és a belső logikai hibák (pl. hajó pályán kívülre helyezése) kezelése saját kivételosztályok és try-catch blokkok alkalmazásával történik.
- Tesztelés: A játék belső logikáját JUnit keretrendszerrel ellenőrizzük. A tesztek garantálják a találatok, az elsüllyedés és az elhelyezési korlátok (például ütközés vagy lelógás) helyes működését.
-Fájlok: A mentés és betöltés megvalósítása a Java szabványos karakteres adatfolyam-kezelő osztályaival történik (BufferedReader / BufferedWriter + InputStreamReader / OutputStreamWriter osztályok egy .txt fájlba. A kimentett fájl szerkezete: elsőként a játékosok hátralévő speciális képességeinek számait tárolja, ezt követően pedig a két játéktábla (10x10-es cella) állapotát tartalmazza (például soronként kiírva a mezők azonosítóit, vesszővel elválasztva).
-Fájlvalidáció: hogy ellenőrizzük, tényleg egy játékállás van a fájlban, a fájl első sorába egy egyedi azonosítót generál a program, betöltéskor először ezt ellenőrzi. Ha nem egy létező játék, megszakad a beolvasás.

