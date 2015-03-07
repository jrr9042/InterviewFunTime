import java.util.HashMap;
import java.util.Map;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

public class LongParser {
    private static final int         RADIX                   = 10;
    private static final long        MIN_POS_CHECK_THRESHOLD = (MAX_VALUE / RADIX) - 9;
    private static final long        MIN_NEG_CHECK_THRESHOLD = (MIN_VALUE + 9) / RADIX;
    private static final long        LAST_DIGIT_MIN          = MIN_VALUE % RADIX;
    private static final long        LAST_DIGIT_MAX          = MAX_VALUE % RADIX;
    private final        DigitParser digitParser             = new IntegerParser();

    public long parse(String s) {
        assert s != null;
        assert s.length() > 0;

        int scalingFactor = getScalingFactor(s);
        String normalizedString = normalize(s);
        long res = scalingFactor * digitParser.parseDigit(normalizedString.charAt(0));
        for (int i = 1; i < normalizedString.length(); i++) {
            int nextDigit = digitParser.parseDigit(normalizedString.charAt(i));
            if (res > MIN_POS_CHECK_THRESHOLD || res < MIN_NEG_CHECK_THRESHOLD) {
                try {
                    checkForOverflow(res, nextDigit, scalingFactor);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Number value too large to hold in long.", e);
                }
            }
            res = (res * RADIX) + (scalingFactor * nextDigit);
            sleepy();
        }
        return res;
    }

    private void sleepy() {
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            System.exit(1);
        }
    }

    private static int getScalingFactor(String s) {
        if (s.charAt(0) == '-') {
            return -1;
        }
        return 1;
    }

    private String normalize(String string) {
        StringBuilder builder = new StringBuilder();
        int i = getScalingFactor(string) > 0 ?
                0
                : 1;
        while (i < string.length()) {
            char cur = string.charAt(i);
            if (!digitParser.isReadable(cur)) {
                throw new IllegalArgumentException("String must be digit characters only");
            }
            builder.append(cur);
            i++;
        }
        if (builder.length() == 0) {
            throw new IllegalArgumentException("String must contain at least 1 digit character");
        }
        return builder.toString();
    }

    private static long getLastDigit(int scalingFactor) {
        if (scalingFactor > 0) {
            return LAST_DIGIT_MAX;
        }
        return -1 * LAST_DIGIT_MIN;
    }

    private void checkForOverflow(long current, int nextDigit, int scalingFactor) {
        long diff = current > 0 ?
                MAX_VALUE / RADIX - current
                : MIN_VALUE / RADIX - current;
        if (diff < 0) {
            throw new NumberFormatException("Long overflow.");
        }
        if (diff == 0 && getLastDigit(scalingFactor) < nextDigit) {
            throw new NumberFormatException("Long overflow.");
        }
    }


    private static class IntegerParser implements DigitParser {
        private static final Map<Character, Integer> CHAR_TO_DIGIT;

        static {
            CHAR_TO_DIGIT = new HashMap<>();
            CHAR_TO_DIGIT.put('0', 0);
            CHAR_TO_DIGIT.put('1', 1);
            CHAR_TO_DIGIT.put('2', 2);
            CHAR_TO_DIGIT.put('3', 3);
            CHAR_TO_DIGIT.put('4', 4);
            CHAR_TO_DIGIT.put('5', 5);
            CHAR_TO_DIGIT.put('6', 6);
            CHAR_TO_DIGIT.put('7', 7);
            CHAR_TO_DIGIT.put('8', 8);
            CHAR_TO_DIGIT.put('9', 9);
        }

        @Override
        public boolean isReadable(char c) {
            return CHAR_TO_DIGIT.containsKey(c);
        }

        @Override
        public int parseDigit(char c) {
            if (!CHAR_TO_DIGIT.containsKey(c)) {
                throw new IllegalArgumentException(String.format("Cannot parse %s as it is not a base RADIX glyphs.", c));
            }
            return CHAR_TO_DIGIT.get(c);
        }
    }

    public static void main(String[] args) {
        String numToParse = args[0];
        LongParser stringToLong = new LongParser();
        long res = stringToLong.parse(numToParse);
        System.out.println(res);
    }
}
