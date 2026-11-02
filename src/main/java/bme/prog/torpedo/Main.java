package bme.prog.torpedo;
import javax.swing.*;
import java.awt.*;


public class Main {
    public static void main(String[] args) {
        // Two objects with same coord.
        Coordinate c1 = new Coordinate(5, 5);
        Coordinate c2 = new Coordinate(5, 5);
        
        // true if equals works
        System.out.println("Do the coordinates match? " + c1.equals(c2));
    }
}