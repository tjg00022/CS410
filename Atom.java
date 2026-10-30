public class Atom {

    //Comparison codes used by TST atoms
    public static final int EQUAL = 1;
    public static final int LESSER = 2;
    public static final int GREATER = 3;
    public static final int LESSER_OR_EQUAL = 4;
    public static final int GREATER_OR_EQUAL = 5;
    public static final int UNEQUAL = 6;

    //(operation, left, right, result, comparison, destination)
    private final String operation;
    private final String left;
    private final String right;
    private final String result;
    private final String comparison;
    private final String destination;

    public Atom(String operation, String left, String right, String result, String comparison, String destination)
    {
        this.operation = operation;
        this.left = left;
        this.right = right;
        this.result = result;
        this.comparison = comparison;
        this.destination = destination;
    }

    //Atoms for ADD, SUB, MUL, DIV
    public static Atom arithmetic(String operation, String left, String right, String result)
    {
        return new Atom(operation, left, right, result, "", "");
    }

    //Atom for NEG
    public static Atom negate(String left, String result)
    {
        return new Atom("NEG", left, "", result, "", "");
    }

    //Atom for MOV
    public static Atom move(String left, String result)
    {
        return new Atom("MOV", left, "", result, "", "");
    }

    //Atom for JMP
    public static Atom jump(String destination)
    {
        return new Atom("JMP", "", "", "", "", destination);
    }

    //Atom for LBL
    public static Atom label(String destination)
    {
        return new Atom("LBL", "", "", "", "", destination);
    }

    //Atom for TST
    public static Atom test(String left, String right, int comparison, String destination)
    {
        return new Atom("TST", left, right, "", String.valueOf(comparison), destination);
    }

    //Turns a comparison operator into its comparison code
    public static int comparisonCode(String operator)
    {
        switch (operator)
        {
            case "==": return EQUAL;
            case "<": return LESSER;
            case ">": return GREATER;
            case "<=": return LESSER_OR_EQUAL;
            case ">=": return GREATER_OR_EQUAL;
            case "!=": return UNEQUAL;
            default: return 0;
        }
    }

    //Gives the opposite comparison (== and != , < and >= , > and <=)
    //The codes are set up so the opposite of c is always 7 - c
    public static int opposite(int comparison)
    {
        return 7 - comparison;
    }

    public String getOperation() {
        return operation; 
    }
    public String getLeft() {
        return left; 
        }
    public String getRight() {
        return right;
    }
    public String getResult() {
        return result;
    }
    public String getComparison() {
        return comparison;
    }
    public String getDestination() {
        return destination;
    }

    //Prints the atom like (ADD, a, b, T1) and leaves off empty spots at the end
    @Override
    public String toString()
    {
        String[] parts = {operation, left, right, result, comparison, destination};

        //Find the last spot that is not empty
        int last = parts.length - 1;
        while (last > 0 && parts[last].isEmpty())
        {
            last--;
        }

        StringBuilder text = new StringBuilder("(");
        for (int i = 0; i <= last; i++)
        {
            if (i > 0)
            {
                text.append(", ");
            }
            text.append(parts[i]);
        }
        text.append(")");
        return text.toString();
    }
}
