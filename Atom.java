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
    private final String leftOperand;
    private final String rightOperand;
    private final String resultName;
    private final String comparisonCode;
    private final String destinationLabel;

    public Atom(String operation, String leftOperand, String rightOperand, String resultName,
                String comparisonCode, String destinationLabel)
    {
        this.operation = operation;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.resultName = resultName;
        this.comparisonCode = comparisonCode;
        this.destinationLabel = destinationLabel;
    }

    //Atoms for ADD, SUB, MUL, DIV
    public static Atom arithmetic(String operationName, String leftOperand, String rightOperand, String resultName)
    {
        return new Atom(operationName, leftOperand, rightOperand, resultName, "", "");
    }

    //Atom for NEG
    public static Atom negate(String leftOperand, String resultName)
    {
        return new Atom("NEG", leftOperand, "", resultName, "", "");
    }

    //Atom for MOV
    public static Atom move(String valueName, String resultName)
    {
        return new Atom("MOV", valueName, "", resultName, "", "");
    }

    //Atom for JMP
    public static Atom jump(String destinationLabel)
    {
        return new Atom("JMP", "", "", "", "", destinationLabel);
    }

    //Atom for LBL
    public static Atom label(String destinationLabel)
    {
        return new Atom("LBL", "", "", "", "", destinationLabel);
    }

    //Atom for TST
    public static Atom test(String leftOperand, String rightOperand, int comparisonNumber,
                            String destinationLabel)
    {
        return new Atom("TST", leftOperand, rightOperand, "", String.valueOf(comparisonNumber), destinationLabel);
    }

    //Turns a comparison operator into its comparison code
    public static int getComparisonCode(String operator)
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
    public static int getOppositeComparison(int comparisonNumber)
    {
        return 7 - comparisonNumber;
    }

    public String getOperation() { return operation; }
    public String getLeftOperand() { return leftOperand; }
    public String getRightOperand() { return rightOperand; }
    public String getResultName() { return resultName; }
    public String getComparisonCode() { return comparisonCode; }
    public String getDestinationLabel() { return destinationLabel; }

    //Prints the atom like (ADD, a, b, T1) and leaves off empty spots at the end
    @Override
    public String toString()
    {
        String[] parts = {operation, leftOperand, rightOperand, resultName, comparisonCode, destinationLabel};

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
