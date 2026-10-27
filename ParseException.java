//Thrown by the parser when the tokens are not a valid sentence of our dialect
public class ParseException extends RuntimeException {

    public ParseException(String message)
    {
        super(message);
    }
}
