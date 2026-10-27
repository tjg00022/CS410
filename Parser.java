import java.util.ArrayList;
import java.util.List;

//Recursive descent parser for our dialect.
//There is one method for each nonterminal in our grammar (see the Phase 2 grammar
//and the abstract machine drawing). Each method checks the current terminal
//against the selection sets to pick a production, and also builds the atoms.
public class Parser {

    //Used for the end of the input (the "↵" in our grammar)
    private static final String END = "END";

    private final List<Token> tokenList;
    private int currentTokenIndex = 0;
    private String currentTerminal;

    //The atoms we have made so far
    private List<Atom> atomList = new ArrayList<>();

    //Counters for making new temporary variables (T1, T2, ...) and labels (L1, L2, ...)
    private int temporaryCount = 0;
    private int labelCount = 0;

    public Parser(List<Token> inputTokens)
    {
        this.tokenList = inputTokens;
    }

    //main() from the abstract machine
    public List<Atom> parse()
    {
        getNextToken();
        program();

        //Every token has to be used up for the program to be accepted
        if (!currentTerminal.equals(END))
        {
            reject("end of input");
        }
        return atomList;
    }

    // ---------------- machine helpers ----------------

    //Turns a token from the scanner into the terminal name used in our grammar
    private String getTerminal(Token token)
    {
        switch (token.getType())
        {
            case "IDENTIFIER": return "identifier";
            case "INTEGER_LITERAL": return "intliteral";
            case "FLOAT_LITERAL": return "floatliteral";
            default: return token.getValue(); //keywords, operators and punctuation
        }
    }

    //Moves to the next token
    private void getNextToken()
    {
        if (currentTokenIndex < tokenList.size())
        {
            currentTerminal = getTerminal(tokenList.get(currentTokenIndex));
        }
        else
        {
            currentTerminal = END;
        }
        currentTokenIndex++;
    }

    //The text of the current token (like "x" or "5" instead of "identifier")
    private String getCurrentValue()
    {
        if (currentTokenIndex - 1 < tokenList.size())
        {
            return tokenList.get(currentTokenIndex - 1).getValue();
        }
        return END;
    }

    //Checks that the current token is the one we expect and moves on
    private void match(String expected)
    {
        if (currentTerminal.equals(expected))
        {
            getNextToken();
        }
        else
        {
            reject(expected);
        }
    }

    //Stops the parser when a token is not allowed here
    private void reject(String expected)
    {
        throw new RuntimeException("Reject: expected " + expected + " but found '" + getCurrentValue()
                + "' (token " + currentTokenIndex + ")");
    }

    //Checks if the current token is one of the tokens in a selection set
    private boolean isOneOf(String... selectionSet)
    {
        for (String selectionToken : selectionSet)
        {
            if (currentTerminal.equals(selectionToken))
            {
                return true;
            }
        }
        return false;
    }

    private String createTemporaryName()
    {
        temporaryCount++;
        return "T" + temporaryCount;
    }

    private String createLabelName()
    {
        labelCount++;
        return "L" + labelCount;
    }

    private void addAtom(Atom atom)
    {
        atomList.add(atom);
    }

    // ---------------- statements ----------------

    //1  Program -> StmtList
    private void program()
    {
        stmtList();
    }

    //2  StmtList -> Stmt StmtList
    //3  StmtList -> ε
    private void stmtList()
    {
        if (isOneOf("int", "float", "identifier", "if", "while", "for", "{"))
        {
            stmt();
            stmtList();
        }
        else if (isOneOf("}", END))
        {
            return;
        }
        else
        {
            reject("a statement");
        }
    }

    //4-9  Stmt -> Declaration | Assignment ; | IfStmt | WhileStmt | ForStmt | Block
    private void stmt()
    {
        if (isOneOf("int", "float"))
        {
            declaration();
        }
        else if (currentTerminal.equals("identifier"))
        {
            assignment();
            match(";");
        }
        else if (currentTerminal.equals("if"))
        {
            ifStmt();
        }
        else if (currentTerminal.equals("while"))
        {
            whileStmt();
        }
        else if (currentTerminal.equals("for"))
        {
            forStmt();
        }
        else if (currentTerminal.equals("{"))
        {
            block();
        }
        else
        {
            reject("a statement");
        }
    }

    //10  Declaration -> Type identifier Init ;
    private void declaration()
    {
        type();
        String variableName = getCurrentValue();
        match("identifier");
        init(variableName);
        match(";");
    }

    //11  Init -> = Expr        atom: (MOV, value, , name)
    //12  Init -> ε             no atom, the variable just gets declared
    private void init(String variableName)
    {
        if (currentTerminal.equals("="))
        {
            getNextToken();
            String expressionValue = expr();
            addAtom(Atom.move(expressionValue, variableName));
        }
        else if (currentTerminal.equals(";"))
        {
            return;
        }
        else
        {
            reject("= or ;");
        }
    }

    //13  Type -> int
    //14  Type -> float
    private void type()
    {
        if (isOneOf("int", "float"))
        {
            getNextToken();
        }
        else
        {
            reject("int or float");
        }
    }

    //15  Assignment -> identifier = Expr        atom: (MOV, value, , name)
    private void assignment()
    {
        String variableName = getCurrentValue();
        match("identifier");
        match("=");
        String expressionValue = expr();
        addAtom(Atom.move(expressionValue, variableName));
    }

    // ---------------- control flow ----------------

    //16  IfStmt -> if ( Condition ) Block ElsePart
    //
    //    TST (opposite comparison) to Lfalse     skip the block if the condition is false
    //    ...block...
    //    JMP Lend                                 only if there is an else
    //    LBL Lfalse
    //    ...else part...
    //    LBL Lend                                 only if there is an else
    private void ifStmt()
    {
        String falseLabel = createLabelName();

        match("if");
        match("(");
        condition(falseLabel);
        match(")");
        block();
        elsePart(falseLabel);
    }

    //17  ElsePart -> else ElseBody
    //18  ElsePart -> ε
    private void elsePart(String falseLabel)
    {
        if (currentTerminal.equals("else"))
        {
            String endLabel = createLabelName();
            addAtom(Atom.jump(endLabel));
            addAtom(Atom.label(falseLabel));

            getNextToken();
            elseBody();

            addAtom(Atom.label(endLabel));
        }
        else if (isOneOf("int", "float", "identifier", "if", "while", "for", "{", "}", END))
        {
            addAtom(Atom.label(falseLabel));
        }
        else
        {
            reject("else or a statement");
        }
    }

    //19  ElseBody -> Block
    //20  ElseBody -> IfStmt         (this is how else if works)
    private void elseBody()
    {
        if (currentTerminal.equals("{"))
        {
            block();
        }
        else if (currentTerminal.equals("if"))
        {
            ifStmt();
        }
        else
        {
            reject("{ or if");
        }
    }

    //21  WhileStmt -> while ( Condition ) Block
    //
    //    LBL Ltop
    //    TST (opposite comparison) to Lend
    //    ...block...
    //    JMP Ltop
    //    LBL Lend
    private void whileStmt()
    {
        String topLabel = createLabelName();
        String endLabel = createLabelName();

        match("while");
        addAtom(Atom.label(topLabel));
        match("(");
        condition(endLabel);
        match(")");
        block();
        addAtom(Atom.jump(topLabel));
        addAtom(Atom.label(endLabel));
    }

    //22  ForStmt -> for ( Assignment ; Condition ; Assignment ) Block
    //
    //    ...first assignment...
    //    LBL Ltop
    //    TST (opposite comparison) to Lend
    //    ...block...
    //    ...second assignment...       (read before the block but has to run after it)
    //    JMP Ltop
    //    LBL Lend
    private void forStmt()
    {
        String topLabel = createLabelName();
        String endLabel = createLabelName();

        match("for");
        match("(");
        assignment();
        match(";");
        addAtom(Atom.label(topLabel));
        condition(endLabel);
        match(";");

        //Save the atoms for the update part in a separate list so they can go after the block
        List<Atom> savedAtoms = atomList;
        atomList = new ArrayList<>();
        assignment();
        List<Atom> updateAtoms = atomList;
        atomList = savedAtoms;

        match(")");
        block();
        atomList.addAll(updateAtoms);
        addAtom(Atom.jump(topLabel));
        addAtom(Atom.label(endLabel));
    }

    //23  Block -> { StmtList }
    private void block()
    {
        match("{");
        stmtList();
        match("}");
    }

    // ---------------- conditions ----------------

    //24  Condition -> Expr Compare Expr
    //    Jumps to falseLabel when the condition is false, so the TST uses the opposite comparison
    private void condition(String falseLabel)
    {
        String leftValue = expr();
        int comparison = compare();
        String rightValue = expr();
        addAtom(Atom.test(leftValue, rightValue, Atom.getOppositeComparison(comparison), falseLabel));
    }

    //25-30  Compare -> == | != | < | > | <= | >=
    private int compare()
    {
        if (isOneOf("==", "!=", "<", ">", "<=", ">="))
        {
            int comparison = Atom.getComparisonCode(currentTerminal);
            getNextToken();
            return comparison;
        }
        reject("a comparison");
        return 0;
    }

    // ---------------- arithmetic ----------------
    //These methods return the name of where the value is (a variable, a number, or a temp like T1)

    //31  Expr -> Term ExprTail
    private String expr()
    {
        String firstTermValue = term();
        return exprTail(firstTermValue);
    }

    //32  ExprTail -> + Term ExprTail        atom: (ADD, left, right, T)
    //33  ExprTail -> - Term ExprTail        atom: (SUB, left, right, T)
    //34  ExprTail -> ε
    //The left side gets passed in so 1 - 2 - 3 is done as (1 - 2) - 3
    private String exprTail(String leftValue)
    {
        if (isOneOf("+", "-"))
        {
            String operationName = currentTerminal.equals("+") ? "ADD" : "SUB";
            getNextToken();
            String rightValue = term();
            String resultName = createTemporaryName();
            addAtom(Atom.arithmetic(operationName, leftValue, rightValue, resultName));
            return exprTail(resultName);
        }
        else if (isOneOf("==", "!=", "<", ">", "<=", ">=", ")", ";"))
        {
            return leftValue;
        }
        reject("an operator, ) or ;");
        return leftValue;
    }

    //35  Term -> Factor TermTail
    private String term()
    {
        String firstFactorValue = factor();
        return termTail(firstFactorValue);
    }

    //36  TermTail -> * Factor TermTail      atom: (MUL, left, right, T)
    //37  TermTail -> / Factor TermTail      atom: (DIV, left, right, T)
    //38  TermTail -> ε
    private String termTail(String leftValue)
    {
        if (isOneOf("*", "/"))
        {
            String operationName = currentTerminal.equals("*") ? "MUL" : "DIV";
            getNextToken();
            String rightValue = factor();
            String resultName = createTemporaryName();
            addAtom(Atom.arithmetic(operationName, leftValue, rightValue, resultName));
            return termTail(resultName);
        }
        else if (isOneOf("==", "!=", "<", ">", "<=", ">=", "+", "-", ")", ";"))
        {
            return leftValue;
        }
        reject("an operator, ) or ;");
        return leftValue;
    }

    //39  Factor -> ( Expr )
    //40  Factor -> - Factor                 atom: (NEG, value, , T)
    //41-43  Factor -> identifier | intliteral | floatliteral
    private String factor()
    {
        if (currentTerminal.equals("("))
        {
            getNextToken();
            String expressionValue = expr();
            match(")");
            return expressionValue;
        }
        else if (currentTerminal.equals("-"))
        {
            getNextToken();
            String factorValue = factor();
            String resultName = createTemporaryName();
            addAtom(Atom.negate(factorValue, resultName));
            return resultName;
        }
        else if (isOneOf("identifier", "intliteral", "floatliteral"))
        {
            String tokenValue = getCurrentValue();
            getNextToken();
            return tokenValue;
        }
        reject("a number, variable, - or (");
        return "";
    }
}
