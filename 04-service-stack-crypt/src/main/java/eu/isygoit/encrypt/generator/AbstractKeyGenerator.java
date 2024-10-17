package eu.isygoit.encrypt.generator;

import eu.isygoit.enums.IEnumCharSet;

import java.util.Arrays;
import java.util.Random;

/**
 * The type Abstract key generator.
 */
public abstract class AbstractKeyGenerator implements IKeyGenerator {

    private static final char[] allsymbols;
    private static final char[] alphanumsymbols;
    private static final char[] alphasymbols;
    private static final char[] numsymbols;

    static {
        //numeric symbols
        StringBuilder numsymbolsSB = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch) {
            numsymbolsSB.append(ch);
        }
        numsymbols = numsymbolsSB.toString().toCharArray();

        //alphabet symbols
        StringBuilder alphasymbolsSB = new StringBuilder();
        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            alphasymbolsSB.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            alphasymbolsSB.append(ch);
        }
        alphasymbols = alphasymbolsSB.toString().toCharArray();

        //alphanumeric symbols
        StringBuilder alphanumsymbolsSB = new StringBuilder();
        alphanumsymbolsSB.append(Arrays.toString(numsymbols))
                .append(alphasymbols);
        alphanumsymbols = alphanumsymbolsSB.toString().toCharArray();

        //all symbols
        StringBuilder allsymbolsSB = new StringBuilder();
        allsymbolsSB.append(Arrays.toString(alphanumsymbols));
        allsymbolsSB.append('$');
        allsymbolsSB.append('#');
        allsymbolsSB.append('&');
        allsymbolsSB.append('@');
        allsymbolsSB.append('/');
        allsymbolsSB.append('-');
        allsymbolsSB.append('+');
        allsymbolsSB.append('=');
        allsymbolsSB.append('{');
        allsymbolsSB.append('}');
        allsymbolsSB.append(']');
        allsymbolsSB.append('[');
        allsymbolsSB.append('(');
        allsymbolsSB.append(')');
        allsymbols = allsymbolsSB.toString().toCharArray();
    }

    private final Random random = new Random();
    private char[] buf;

    /**
     * Sets bufferlength.
     *
     * @param length the length
     */
    public void setBufferlength(int length) {
        if (length < 1)
            throw new IllegalArgumentException("textLength < 1: " + length);
        if (buf == null || buf.length != length) {
            buf = new char[length];
        }
    }

    @Override
    public String currentGuid() {
        return new String(buf);
    }

    @Override
    public String nextGuid() {
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = allsymbols[random.nextInt(allsymbols.length)];
        }
        return new String(buf);
    }

    @Override
    public String nextGuid(IEnumCharSet.Types charSetType) {
        for (int idx = 0; idx < buf.length; ++idx) {
            switch (charSetType) {
                case NUMERIC:
                    buf[idx] = numsymbols[random.nextInt(numsymbols.length)];
                    break;
                case ALPHA:
                    buf[idx] = alphasymbols[random.nextInt(alphasymbols.length)];
                    break;
                case ALPHANUM:
                    buf[idx] = alphanumsymbols[random.nextInt(alphanumsymbols.length)];
                    break;
                case ALL:
                    buf[idx] = allsymbols[random.nextInt(allsymbols.length)];
                    break;
                default:
                    buf[idx] = allsymbols[random.nextInt(allsymbols.length)];
            }
        }
        return new String(buf);
    }
}
