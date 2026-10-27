import java.util.Arrays;

public class StateTable {
    //States needed by the scanner
    public static final int START = 0;
    public static final int WORD = 1;
    public static final int INTEGER = 2;
    public static final int DECIMAL = 3;
    public static final int FLOAT = 4;
    public static final int OPERATOR = 5;
    public static final int ASSIGN = 6;
    public static final int EQUAL = 7;
    public static final int NOT = 8;
    public static final int NOT_EQUAL = 9;
    public static final int LESS = 10;
    public static final int LESS_EQUAL = 11;
    public static final int GREATER = 12;
    public static final int GREATER_EQUAL = 13;
    public static final int PUNCTUATION = 14;

    //Input numbers are the columns of the transition table
    public static final int LETTER = 0;
    public static final int DIGIT = 1;
    public static final int DOT = 2;
    public static final int PLUS_INPUT = 3;
    public static final int MINUS_INPUT = 4;
    public static final int STAR = 5;
    public static final int SLASH = 6;
    public static final int EQUAL_INPUT = 7;
    public static final int EXCLAMATION = 8;
    public static final int LESS_INPUT = 9;
    public static final int GREATER_INPUT = 10;
    public static final int LEFT_PAREN_INPUT = 11;
    public static final int RIGHT_PAREN_INPUT = 12;
    public static final int LEFT_BRACE_INPUT = 13;
    public static final int RIGHT_BRACE_INPUT = 14;
    public static final int SEMICOLON_INPUT = 15;
    public static final int WHITESPACE = 16;
    public static final int OTHER = 17;

    public static final int ERROR = -1;

    //Each row is a state and each column is an input type
    public static final int[][] transitions = createTransitions();

    private static int[][] createTransitions() {
        int[][] table = new int[15][18];

        //Start with every transition invalid
        for (int[] row : table) {
            Arrays.fill(row, ERROR);
        }
        //Tokens that can begin from start
        table[START][LETTER] = WORD;
        table[START][DIGIT] = INTEGER;
        table[START][PLUS_INPUT] = OPERATOR;
        table[START][MINUS_INPUT] = OPERATOR;
        table[START][STAR] = OPERATOR;
        table[START][SLASH] = OPERATOR;
        table[START][EQUAL_INPUT] = ASSIGN;
        table[START][EXCLAMATION] = NOT;
        table[START][LESS_INPUT] = LESS;
        table[START][GREATER_INPUT] = GREATER;
        table[START][LEFT_PAREN_INPUT] = PUNCTUATION;
        table[START][RIGHT_PAREN_INPUT] = PUNCTUATION;
        table[START][LEFT_BRACE_INPUT] = PUNCTUATION;
        table[START][RIGHT_BRACE_INPUT] = PUNCTUATION;
        table[START][SEMICOLON_INPUT] = PUNCTUATION;

        //Words contain letters and digits after the first letter
        table[WORD][LETTER] = WORD;
        table[WORD][DIGIT] = WORD;

        //Numbers can be integers or floats
        table[INTEGER][DIGIT] = INTEGER;
        table[INTEGER][DOT] = DECIMAL;
        table[DECIMAL][DIGIT] = FLOAT;
        table[FLOAT][DIGIT] = FLOAT;

        //These states handle operators that may have a second character
        table[ASSIGN][EQUAL_INPUT] = EQUAL;
        table[NOT][EQUAL_INPUT] = NOT_EQUAL;
        table[LESS][EQUAL_INPUT] = LESS_EQUAL;
        table[GREATER][EQUAL_INPUT] = GREATER_EQUAL;

        return table;
    }

    //True means the state contains a complete token
    public static final boolean[] accepting = {
        false, true, true, false, true,
        true, true, true, false, true,
        true, true, true, true, true
    };

    //Converts an actual character into an input-column number
    public static int getCharacterType(char character) {
        if (Character.isLetter(character)) return LETTER;
        if (Character.isDigit(character)) return DIGIT;
        if (Character.isWhitespace(character)) return WHITESPACE;

        switch (character)
        {
            case '.': return DOT;
            case '+': return PLUS_INPUT;
            case '-': return MINUS_INPUT;
            case '*': return STAR;
            case '/': return SLASH;
            case '=': return EQUAL_INPUT;
            case '!': return EXCLAMATION;
            case '<': return LESS_INPUT;
            case '>': return GREATER_INPUT;
            case '(': return LEFT_PAREN_INPUT;
            case ')': return RIGHT_PAREN_INPUT;
            case '{': return LEFT_BRACE_INPUT;
            case '}': return RIGHT_BRACE_INPUT;
            case ';': return SEMICOLON_INPUT;
            default: return OTHER;
        }
    }

    //Checks whether a word is a keyword
    public static boolean isKeyword(String word) {
        return word.equals("int") || word.equals("float") || word.equals("if") || word.equals("else") || word.equals("for") || word.equals("while");
    }
}
