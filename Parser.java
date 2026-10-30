import java.util.ArrayList;
import java.util.List;

//Recursive descent parser for our dialect.
//There is one method for each nonterminal in our grammar (see the Phase 2 grammar
//and the abstract machine drawing). Each method checks the current token (input)
//against the selection sets to pick a production, and also builds the atoms.
public class Parser {

    //Used for the end of the input (the "↵" in our grammar)
    private static final String END = "END";

    private final List<Token> tokens;
    private int position = 0;
    private String input;

    //The atoms we have made so far
    private List<Atom> atoms = new ArrayList<>();

    //Counters for making new temporary variables (T1, T2, ...) and labels (L1, L2, ...)
    private int tempCount = 0;
    private int labelCount = 0;

    public Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    //main() from the abstract machine
    public List<Atom> parse()
    {
        getInput();
        program();

        //Every token has to be used up for the program to be accepted
        if (!input.equals(END))
        {
            reject("end of input");
        }
        return atoms;
    }

    // ---------------- machine helpers ----------------

    //Turns a token from the scanner into the terminal name used in our grammar
    private String terminal(Token token)
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
    private void getInput()
    {
        if (position < tokens.size())
        {
            input = terminal(tokens.get(position));
        }
        else
        {
            input = END;
        }
        position++;
    }

    //The text of the current token (like "x" or "5" instead of "identifier")
    private String currentValue()
    {
        if (position - 1 < tokens.size())
        {
            return tokens.get(position - 1).getValue();
        }
        return END;
    }

    //Checks that the current token is the one we expect and moves on
    private void match(String expected)
    {
        if (input.equals(expected))
        {
            getInput();
        }
        else
        {
            reject(expected);
        }
    }

    //Stops the parser when a token is not allowed here
    private void reject(String expected)
    {
        throw new RuntimeException("Reject: expected " + expected + " but found '" + currentValue()
                + "' (token " + position + ")");
    }

    //Checks if the current token is one of the tokens in a selection set
    private boolean inputIn(String[] set)
    {
        for (String token : set)
        {
            if (input.equals(token))
            {
                return true;
            }
        }
        return false;
    }

    private String newTemp()
    {
        tempCount++;
        return "T" + tempCount;
    }

    private String newLabel()
    {
        labelCount++;
        return "L" + labelCount;
    }

    private void emit(Atom atom)
    {
        atoms.add(atom);
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
        if (inputIn(new String[]{"int", "float", "identifier", "if", "while", "for", "{"}))
        {
            stmt();
            stmtList();
        }
        else if (inputIn(new String[]{"}", END}))
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
        if (inputIn(new String[]{"int", "float"}))
        {
            declaration();
        }
        else if (input.equals("identifier"))
        {
            assignment();
            match(";");
        }
        else if (input.equals("if"))
        {
            ifStmt();
        }
        else if (input.equals("while"))
        {
            whileStmt();
        }
        else if (input.equals("for"))
        {
            forStmt();
        }
        else if (input.equals("{"))
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
        String name = currentValue();
        match("identifier");
        init(name);
        match(";");
    }

    //11  Init -> = Expr        atom: (MOV, value, , name)
    //12  Init -> ε             no atom, the variable just gets declared
    private void init(String name)
    {
        if (input.equals("="))
        {
            getInput();
            String value = expr();
            emit(Atom.move(value, name));
        }
        else if (input.equals(";"))
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
        if (inputIn(new String[]{"int", "float"}))
        {
            getInput();
        }
        else
        {
            reject("int or float");
        }
    }

    //15  Assignment -> identifier = Expr        atom: (MOV, value, , name)
    private void assignment()
    {
        String name = currentValue();
        match("identifier");
        match("=");
        String value = expr();
        emit(Atom.move(value, name));
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
        String falseLabel = newLabel();

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
        if (input.equals("else"))
        {
            String endLabel = newLabel();
            emit(Atom.jump(endLabel));
            emit(Atom.label(falseLabel));

            getInput();
            elseBody();

            emit(Atom.label(endLabel));
        }
        else if (inputIn(new String[]{"int", "float", "identifier", "if", "while", "for", "{", "}", END}))
        {
            emit(Atom.label(falseLabel));
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
        if (input.equals("{"))
        {
            block();
        }
        else if (input.equals("if"))
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
        String topLabel = newLabel();
        String endLabel = newLabel();

        match("while");
        emit(Atom.label(topLabel));
        match("(");
        condition(endLabel);
        match(")");
        block();
        emit(Atom.jump(topLabel));
        emit(Atom.label(endLabel));
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
        String topLabel = newLabel();
        String endLabel = newLabel();

        match("for");
        match("(");
        assignment();
        match(";");
        emit(Atom.label(topLabel));
        condition(endLabel);
        match(";");

        //Save the atoms for the update part in a separate list so they can go after the block
        List<Atom> saved = atoms;
        atoms = new ArrayList<>();
        assignment();
        List<Atom> updateAtoms = atoms;
        atoms = saved;

        match(")");
        block();
        atoms.addAll(updateAtoms);
        emit(Atom.jump(topLabel));
        emit(Atom.label(endLabel));
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
        String left = expr();
        int comparison = compare();
        String right = expr();
        emit(Atom.test(left, right, Atom.opposite(comparison), falseLabel));
    }

    //25-30  Compare -> == | != | < | > | <= | >=
    private int compare()
    {
        if (inputIn(new String[]{"==", "!=", "<", ">", "<=", ">="}))
        {
            int comparison = Atom.comparisonCode(input);
            getInput();
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
        String left = term();
        return exprTail(left);
    }

    //32  ExprTail -> + Term ExprTail        atom: (ADD, left, right, T)
    //33  ExprTail -> - Term ExprTail        atom: (SUB, left, right, T)
    //34  ExprTail -> ε
    //The left side gets passed in so 1 - 2 - 3 is done as (1 - 2) - 3
    private String exprTail(String left)
    {
        if (inputIn(new String[]{"+", "-"}))
        {
            String operation = input.equals("+") ? "ADD" : "SUB";
            getInput();
            String right = term();
            String result = newTemp();
            emit(Atom.arithmetic(operation, left, right, result));
            return exprTail(result);
        }
        else if (inputIn(new String[]{"==", "!=", "<", ">", "<=", ">=", ")", ";"}))
        {
            return left;
        }
        reject("an operator, ) or ;");
        return left;
    }

    //35  Term -> Factor TermTail
    private String term()
    {
        String left = factor();
        return termTail(left);
    }

    //36  TermTail -> * Factor TermTail      atom: (MUL, left, right, T)
    //37  TermTail -> / Factor TermTail      atom: (DIV, left, right, T)
    //38  TermTail -> ε
    private String termTail(String left)
    {
        if (inputIn(new String[]{"*", "/"}))
        {
            String operation = input.equals("*") ? "MUL" : "DIV";
            getInput();
            String right = factor();
            String result = newTemp();
            emit(Atom.arithmetic(operation, left, right, result));
            return termTail(result);
        }
        else if (inputIn(new String[]{"==", "!=", "<", ">", "<=", ">=", "+", "-", ")", ";"}))
        {
            return left;
        }
        reject("an operator, ) or ;");
        return left;
    }

    //39  Factor -> ( Expr )
    //40  Factor -> - Factor                 atom: (NEG, value, , T)
    //41-43  Factor -> identifier | intliteral | floatliteral
    private String factor()
    {
        if (input.equals("("))
        {
            getInput();
            String value = expr();
            match(")");
            return value;
        }
        else if (input.equals("-"))
        {
            getInput();
            String value = factor();
            String result = newTemp();
            emit(Atom.negate(value, result));
            return result;
        }
        else if (inputIn(new String[]{"identifier", "intliteral", "floatliteral"}))
        {
            String value = currentValue();
            getInput();
            return value;
        }
        reject("a number, variable, - or (");
        return "";
    }
}
