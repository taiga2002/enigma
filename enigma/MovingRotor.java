package enigma;

/**
 * Class that represents a rotating rotor in the enigma machine.
 *
 * @author taiga
 */
class MovingRotor extends Rotor {

    /**
     * A rotor named NAME whose permutation in its default setting is
     * PERM, and whose notches are at the positions indicated in NOTCHES.
     * The Rotor is initally in its 0 setting (first character of its
     * alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }


    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        if (_notches != "") {
            if (_notches.indexOf(alphabet().toChar(this.setting())) != -1) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {

        int currentSetting = permutation().wrap(setting() + 1);
        set(currentSetting);
    }

    @Override
    String notches() {
        return _notches;
    }

    /**
     * notches.
     */
    private String _notches;

}
