package enigma;

import org.junit.Test;


import static org.junit.Assert.*;


public class AlphabetTest {
    @Test
    public void test1() {
        Alphabet test = new Alphabet("ABCDEFG");

        assertEquals(7, test.size());

        assertTrue(test.contains('A'));
        assertFalse(test.contains('Q'));

        assertEquals(0, test.toInt('A'));
        assertEquals(4, test.toInt('E'));

        assertEquals('D', test.toChar(3));
        assertEquals('F', test.toChar(5));
    }
}
