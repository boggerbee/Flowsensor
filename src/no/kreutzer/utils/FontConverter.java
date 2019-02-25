package no.kreutzer.utils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FontConverter {

    public void whenReadWithScanner_thenCorrect() throws IOException {
        String file = "D:/work/gaugette/py-gaugette/gaugette/fonts/arial_32.py";
        Scanner scanner = new Scanner(new File(file));
        scanner.useDelimiter(" ");
/*
        assertTrue(scanner.hasNext());
        assertEquals("Hello", scanner.next());
        assertEquals("world", scanner.next());
        assertEquals(1, scanner.nextInt());
*/
        scanner.close();
    }
    
    public static void main(String args[]) {
        
    }
}
