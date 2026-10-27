import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    //Usage: java Main [input file] [output file]
    //Input defaults to text.txt. Atoms always print to the screen,
    //and also get written to the output file if one is given.
    public static void main(String[] args) throws Exception {
        String inputFileName = args.length > 0 ? args[0] : "text.txt";

        //Read source code from a plaintext file
        String sourceCode = Files.readString(Path.of(inputFileName));

        //Phase 1: turn the source code into tokens
        Scanner scanner = new Scanner();
        List<Token> tokenList = scanner.scan(sourceCode);

        //Phase 2: parse the tokens and turn them into atoms
        Parser parser = new Parser(tokenList);
        List<Atom> atomList;
        try
        {
            atomList = parser.parse();
        }
        catch (RuntimeException e)
        {
            System.out.println(e.getMessage());
            return;
        }

        //Print the atoms one per line
        List<String> lines = new ArrayList<>();
        for (Atom atom : atomList)
        {
            System.out.println(atom);
            lines.add(atom.toString());
        }
        System.out.println("Accept");

        if (args.length > 1)
        {
            Files.write(Path.of(args[1]), lines);
        }
    }
}
