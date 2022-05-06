package enigma;

import java.util.Collection;

import static enigma.EnigmaException.*;

/**
 * Class that represents a complete enigma machine.
 *
 * @author taiga
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _rotors = new Rotor[_numRotors];
        rotorArray = allRotors.toArray();

    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _numRotors;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _pawls;
    }

    /**
     * Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     * #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     * undefined results.
     */
    Rotor getRotor(int k) {
        if (k < 0 || k >= numRotors()) {
            throw new EnigmaException("The index 0 to numRotors()-1");
        }
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) {
        for (int k = 0; k < rotors.length; k++) {
            for (int i = 0; i < rotorArray.length; i++) {
                if (((Rotor) rotorArray[i]).name().equals(rotors[k])) {
                    _rotors[k] = (Rotor) rotorArray[i];
                }
            }
        }
        if (rotors.length != numRotors()) {
            throw new EnigmaException("There is no rotor for the name");
        }
        if (!_rotors[0].reflecting()) {
            throw new EnigmaException("The first rotor is not reflector");
        }
    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector) and OPTIONAL.
     */
    void setRotors(String setting, String optional) {

        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("#setting doesn't match # of rotors");
        }

        for (int i = 0; i < numRotors() - 1; i++) {
            if (!_alphabet.contains(setting.charAt(i))) {
                throw new EnigmaException("setting not in the alphabet");
            }

            if (_rotors[i + 1] != null) {
                _rotors[i + 1].set(setting.charAt(i));
                if (optional != "") {
                    _rotors[i + 1].setOptional(optional.charAt(i));
                }
            } else {
                throw new EnigmaException("rotor at the index is null");
            }

        }
    }

    /**
     * Return the current plugboard's permutation.
     */
    Permutation plugboard() {
        return _plugboard;
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /**
     * Advance all rotors to their next position.
     */
    private void advanceRotors() {
        int firstIndex = numRotors() - 1;
        boolean check = false;
        for (int i = firstIndex; i > 0; i--) {
            if (i == firstIndex) {
                if (_rotors[i].atNotch()) {
                    check = true;
                }
                _rotors[i].advance();
            } else if (check) {
                if (!_rotors[i].atNotch()) {
                    _rotors[i].advance();
                    check = false;
                } else {
                    if (_rotors[i - 1].rotates()) {
                        _rotors[i].advance();
                    } else if (_rotors[i - 1].reflecting()) {
                        _rotors[i].advance();
                        check = false;
                    }
                }
            } else {
                if (_rotors[i].atNotch()) {
                    if (_rotors[i - 1].rotates()) {
                        _rotors[i].advance();
                        check = true;
                    }
                }
            }
        }
    }


    /**
     * Return the result of applying the rotors to the character C (as an
     * index in the range 0..alphabet size - 1).
     */
    private int applyRotors(int c) {
        for (int f = numRotors() - 1; f >= 0; f--) {
            c = _rotors[f].convertForward(c);
        }
        for (int b = 1; b < numRotors(); b++) {
            c = _rotors[b].convertBackward(c);
        }
        return c;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) {
        String result = "";
        for (int k = 0; k < msg.length(); k++) {
            if ((Character.toString(msg.charAt(k))).equals(" ")) {
                result += " ";
            }
            if (!alphabet().contains(msg.charAt(k))) {
                throw new EnigmaException("char not in the alphabet");
            }
            int integer = convert(alphabet().toInt(msg.charAt(k)));
            result += alphabet().toChar(integer);
        }
        return result;
    }

    /**
     * Common alphabet of my rotors.
     */
    private final Alphabet _alphabet;

    /**
     * # of rotors.
     */
    private int _numRotors;
    /**
     * # of pawls.
     */
    private int _pawls;
    /**
     * rotor array.
     */
    private Rotor[] _rotors;
    /**
     * object array.
     */
    private Object[] rotorArray;
    /**
     * plugboard.
     */
    private Permutation _plugboard;

}
