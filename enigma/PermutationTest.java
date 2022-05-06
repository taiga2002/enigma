package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class.
 *
 * @author taiga
 */
public class PermutationTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /**
     * Check that perm has an alphabet whose size is that of
     * FROMALPHA and TOALPHA and that maps each character of
     * FROMALPHA to the corresponding character of FROMALPHA, and
     * vice-versa. TESTID is used in error messages.
     */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testOne() {
        Alphabet alphabet = new Alphabet();

        String cycles = "(BCDEFGA) (HJIK) (MNL) (RPQO) (TUS) (W) (V) (YX)";
        perm = new Permutation(cycles, alphabet);
        assertEquals(26, perm.size());
        assertEquals(1, perm.permute(0));
        assertEquals(22, perm.permute(22));
        assertEquals(9, perm.permute(7));
        assertEquals(25, perm.permute(25));
        assertEquals(19, perm.permute(18));
        assertEquals(19, perm.permute(44));
        assertEquals(6, perm.invert(0));
        assertEquals(0, perm.invert(1));
        assertEquals(0, perm.invert(27));
        assertEquals(22, perm.invert(22));
        assertEquals(25, perm.invert(25));
        assertEquals(25, perm.permute(-1));
        assertEquals(3, perm.permute(-24));
        assertEquals(1, perm.invert(-24));
        assertEquals(25, perm.invert(-1));
        assertEquals(23, perm.invert(-2));

    }

    @Test
    public void testDeranagement() {
        Alphabet alphabet = new Alphabet();
        String cycles = "(BCDEFGA) (HJIK) (MNL) (RPQO) (TUS) (W) (V) (YX)";
        perm = new Permutation(cycles, alphabet);
        assertFalse(perm.derangement());
        String cyclesTwo = "(ABCDEFGHIJKLMN) (OPQRSTUVWXYZ)";
        Permutation permTwo = new Permutation(cyclesTwo, alphabet);
        assertTrue(permTwo.derangement());

        String cyclesFour = "(ABCDEFGHIJKLMNOPQRSTUVWXY)";
        Permutation permFour = new Permutation(cyclesFour, alphabet);
        assertFalse(permFour.derangement());
    }

    @Test
    public void testWithAlphabet() {
        Alphabet alphabet = new Alphabet();
        String cycles = "(BCDEFGA) (HJIK) (MNL) (RPQO) (TUS) (W) (V) (YX)";
        perm = new Permutation(cycles, alphabet);
        assertEquals('B', perm.permute('A'));
        assertEquals('W', perm.permute('W'));
        assertEquals('J', perm.permute('H'));
        assertEquals('Z', perm.permute('Z'));
        assertEquals('T', perm.permute('S'));
        assertEquals('G', perm.invert('A'));
        assertEquals('A', perm.invert('B'));
        assertEquals('W', perm.invert('W'));
        assertEquals('Z', perm.invert('Z'));
    }

    @Test
    public void emptyCycle() {
        Alphabet alphabet = new Alphabet();
        String cycles = "";
        perm = new Permutation(cycles, alphabet);
        assertEquals('B', perm.permute('B'));
        assertEquals('W', perm.permute('W'));
        assertEquals('W', perm.invert('W'));
        assertEquals('Z', perm.invert('Z'));
        assertFalse(perm.derangement());
    }
}

