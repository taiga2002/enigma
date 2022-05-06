package enigma;

import java.util.HashMap;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author taiga
 */
class Permutation {
    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        alpabetSize = alphabet.size();
        String cyclecopy = "";
        for (int i = 0; i < cycles.length() - 1; i++) {
            if (Character.toString(cycles.charAt(i)).equals(")")) {
                if (!Character.toString(cycles.charAt(i + 1)).equals(" ")) {
                    cyclecopy += cycles.charAt(i) + " ";
                }
            }
            cyclecopy += cycles.charAt(i);
        }
        String cyclesWithoutP = cyclecopy.replaceAll("[()]", "");
        String[] cycleArray = (String[]) cyclesWithoutP.split(" ");
        for (String e : cycleArray) {
            for (int i = 0; i < e.length() - 1; i++) {
                if (e.charAt(i) == e.charAt(i + 1)) {
                    throw new EnigmaException("duplicated");
                }
            }
        }
        numberCycle = cycleArray.length;
        orderedList = new HashMap[numberCycle];
        reversedList = new HashMap[numberCycle];
        for (int i = 0; i < numberCycle; i++) {
            if (cycleArray[i].length() == 0) {
                continue;
            } else if (cycleArray[i].length() == 1) {
                orderedList[i] = new HashMap<Integer, Integer>();
                int one = _alphabet.toInt(cycleArray[i].charAt(0));
                orderedList[i].put(one, one);
                reversedList[i] = new HashMap<Integer, Integer>();
                reversedList[i].put(one, one);
            } else {
                orderedList[i] = new HashMap<Integer, Integer>();
                reversedList[i] = new HashMap<Integer, Integer>();
                for (int k = 0; k < cycleArray[i].length() - 1; k++) {
                    int first = _alphabet.toInt(cycleArray[i].charAt(k));
                    int second = _alphabet.toInt(cycleArray[i].charAt(k + 1));
                    orderedList[i].put(first, second);
                }
                int num = cycleArray[i].length() - 1;
                int third = _alphabet.toInt(cycleArray[i].charAt(num));
                int fourth = _alphabet.toInt(cycleArray[i].charAt(0));
                orderedList[i].put(third, fourth);
                for (int k = cycleArray[i].length() - 1; k > 0; k--) {
                    int first = _alphabet.toInt(cycleArray[i].charAt(k));
                    int second = _alphabet.toInt(cycleArray[i].charAt(k - 1));
                    reversedList[i].put(first, second);
                }
                int fif = _alphabet.toInt(cycleArray[i].charAt(0));
                int numTwo = cycleArray[i].length() - 1;
                int six = _alphabet.toInt(cycleArray[i].charAt(numTwo));
                reversedList[i].put(fif, six);
            }
        }
    }

    /**
     * Return the value of P modulo the size of this permutation.
     */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    int size() {
        return alpabetSize;
    }

    /**
     * Return the result of applying this permutation to P modulo the
     * alphabet size.
     */
    int permute(int p) {

        for (HashMap hash : orderedList) {
            if (hash != null && hash.get(wrap(p)) != null) {
                return (int) hash.get(wrap(p));
            }
        }
        return wrap(p);
    }

    /**
     * Return the result of applying the inverse of this permutation
     * to  C modulo the alphabet size.
     */
    int invert(int c) {
        if (reversedList != null) {
            for (HashMap hash : reversedList) {
                if (hash != null && hash.get(wrap(c)) != null) {
                    return (int) hash.get(wrap(c));
                }
            }
        }
        return wrap(c);
    }

    /**
     * Return the result of applying this permutation to the index of P
     * in ALPHABET, and converting the result to a character of ALPHABET.
     */
    char permute(char p) {
        return _alphabet.toChar(permute(_alphabet.toInt(p)));
    }

    /**
     * Return the result of applying the inverse of this permutation to C.
     */
    char invert(char c) {
        return _alphabet.toChar(invert(_alphabet.toInt(c)));
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Return true iff this permutation is a derangement (i.e., a
     * permutation for which no value maps to itself).
     */
    boolean derangement() {
        for (int i = 0; i < size(); i++) {
            if (!check(i)) {
                return false;
            }
        }
        return true;
    }

    boolean check(int i) {
        if (orderedList.length <= 1) {
            return false;
        }
        for (HashMap hash : orderedList) {
            if (hash != null) {
                if (hash.get(i) == null) {
                    continue;
                } else if ((int) hash.get(i) != i) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Alphabet of this permutation.
     */
    private Alphabet _alphabet;
    /**
     * alphabet size.
     */
    private int alpabetSize;
    /**
     * # cycle.
     */
    private int numberCycle;
    /**
     * sorted hashmap.
     */
    private HashMap[] orderedList;
    /**
     * inverse hashmap.
     */
    private HashMap[] reversedList;
}
