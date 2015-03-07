/**
 * Interface for parsing single digits in an arbitrary radix
 */
public interface DigitParser {
    public int parseDigit(char c);
    public boolean isReadable(char c);
}
