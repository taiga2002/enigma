package enigma;

import static enigma.EnigmaException.*;

/**
 * An alphabet of encodable characters.  Provides a mapping from characters
 * to and from indices into the alphabet.
 *
 * @author taiga
 */
class Alphabet {

    /**
     * A new alphabet containing CHARS. The K-th character has index
     * K (numbering from 0). No character may be duplicated.
     */
    Alphabet(String chars) {
        if (!uniqueChar(chars)) {
            throw new EnigmaException("The characters have to be all unique");
        }
        _chars = chars;
    }

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return _chars.length();
    }

    /**
     * Returns true if CH is in this alphabet.
     */
    boolean contains(char ch) {
        return _chars.contains(Character.toString(ch));
    }

    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        return _chars.charAt(wrap(index));
    }
    /** Returns index P wrap method. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the index of character CH which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        for (int i = 0; i < size(); i++) {
            if (_chars.charAt(i) == ch) {
                return i;
            }
        }
        throw new EnigmaException("No character match.");
    }

    /**
     * string characters passed in.
     */
    private String _chars;

    public boolean uniqueChar(String str) {
        for (int k = 0; k < str.length(); k++) {
            for (int l = k + 1; l < str.length(); l++) {
                if (str.charAt(k) == str.charAt(l)) {
                    return false;
                }
            }
        }
        return true;
    }
}
