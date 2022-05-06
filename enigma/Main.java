package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/**
 * Enigma simulator.
 *
 * @author taiga
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        Machine mc = readConfig();
        while (_input.hasNext()) {
            String set = _input.nextLine();
            while (set.equals("")) {
                set = _input.nextLine();
                printMessageLine("");
            }

            if (!set.matches("^\\*.*")) {
                throw new EnigmaException("The input file has to start with *");
            }
            if (set.matches("\\*\\s.*")) {
                set = set.substring(2);
            } else {
                set = set.substring(1);
            }
            setUp(mc, set);

            while (_input.hasNextLine() && !_input.hasNext("\\*")) {
                String replaced = _input.nextLine().replaceAll(" ", "");
                String message = mc.convert(replaced);
                printMessageLine(message);
            }
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            String alphabet = _config.next();
            if (alphabet.matches("[()*]")) {
                throw new EnigmaException("no (), spaces, or *");
            }
            _alphabet = new Alphabet(alphabet);
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            if (!(numRotors > numPawls && numPawls > 0)) {
                throw new EnigmaException("0 < num_pawls < num_rotors");
            }
            ArrayList<Rotor> rotorlist = new ArrayList<Rotor>();
            ArrayList<String> keycheck = new ArrayList<String>();
            while (_config.hasNext()) {
                Rotor rotor = readRotor();
                if (keycheck.contains(rotor.name())) {
                    throw new EnigmaException("not unique key for rotors");
                }
                keycheck.add(rotor.name());
                rotorlist.add(rotor);
            }
            if (rotorlist.size() == 0) {
                throw new EnigmaException("(0 rotors stored)");
            }
            return new Machine(_alphabet, numRotors, numPawls, rotorlist);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /**
     * Return a rotor, reading its description from _config.
     */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            if (name.matches("[()]")) {
                throw new EnigmaException("Name cannot contain ()");
            }
            String typeandnotch = _config.next();
            char type = typeandnotch.charAt(0);
            String notches = "";
            if (typeandnotch.length() > 1) {
                notches = typeandnotch.substring(1);
            }

            String cycleString = "";
            while (_config.hasNext("\\s*\\(.*")) {
                cycleString += _config.nextLine();
            }
            cycleString = cycleString.replace(" ", "");
            if (!cycleString.matches("(\\(.*\\))+")) {
                throw new EnigmaException("format is incorrect");
            }
            if (type == 'R' && !cycleString.matches("(\\(.{2}\\))+")) {
                throw new EnigmaException("(??) form");
            }

            Permutation permutation = new Permutation(cycleString, _alphabet);
            if (type == 'R') {
                if (notches.length() != 0) {
                    throw new EnigmaException("No reflector no notch");
                }
                return new Reflector(name, permutation);
            } else if (type == 'N') {
                if (notches.length() > 0) {
                    throw new EnigmaException("No fixedrotor no notch");
                }
                return new FixedRotor(name, permutation);
            } else if (type == 'M') {
                if (notches.length() == 0) {
                    throw new EnigmaException("at least one notch for moving");
                }
                for (int i = 0; i < notches.length(); i++) {
                    if (!_alphabet.contains(notches.charAt(i))) {
                        throw new EnigmaException("The notch not in alphabet");
                    }
                }
                return new MovingRotor(name, permutation, notches);
            } else {
                throw new EnigmaException("The type not R, N, or M");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        String[] setting = settings.split("\\s+");
        if (setting.length < M.numRotors() + 1) {
            throw new EnigmaException("setting wrong");
        }
        String[] rotorsNames = Arrays.copyOfRange(setting, 0, M.numRotors());
        Set<String> newSet = new HashSet<String>();
        for (String name : rotorsNames) {
            if (!newSet.add(name)) {
                throw new EnigmaException("duplicate rotor setting");
            }
        }
        M.insertRotors(rotorsNames);
        String settingLetter = setting[M.numRotors()];
        int num = M.numRotors() + 1;
        int setLength = setting.length;
        String[] plugboard;
        if (setting.length == M.numRotors() + 2) {
            String optional = setting[M.numRotors() + 1];
            M.setRotors(settingLetter, optional);
            plugboard = Arrays.copyOfRange(setting, num + 1, setLength);
        } else {
            M.setRotors(settingLetter, "");
            plugboard = Arrays.copyOfRange(setting, num, setLength);
        }


        String plugboardString = "";
        for (int i = 0; i < plugboard.length; i++) {
            if (!plugboard[i].matches("^\\(.{2}\\)$")) {
                throw new EnigmaException("not (?,?)");
            }
            plugboardString = plugboardString + plugboard[i] + " ";
        }
        M.setPlugboard(new Permutation(plugboardString, _alphabet));
    }

    /**
     * Return true iff verbose option specified.
     */
    static boolean verbose() {
        return _verbose;
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */

    private void printMessageLine(String msg) {
        if (msg.isEmpty()) {
            System.out.println();
            return;
        }
        String message = "";
        for (int i = 0; i < msg.length(); i++) {
            if ((i + 1) % 5 == 0) {
                message = message + msg.charAt(i) + " ";
                System.out.print(message);
                message = "";
            } else {
                message += msg.charAt(i);
            }
        }
        if (message.length() != 0) {
            System.out.println(message);
        } else {
            System.out.println();
        }
    }


    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;

    /**
     * True if --verbose specified.
     */
    private static boolean _verbose;
}
