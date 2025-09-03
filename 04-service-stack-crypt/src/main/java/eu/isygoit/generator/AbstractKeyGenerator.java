package eu.isygoit.generator;

import eu.isygoit.enums.IEnumCharSet;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Abstract key generator.
 */
public abstract class AbstractKeyGenerator implements IKeyGenerator {

    private static final char[] NUM_SYMBOLS = "0123456789" .toCharArray();
    private static final char[] ALPHA_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" .toCharArray();
    private static final char[] specialSymbols = "$#&@/-+={}[]()" .toCharArray();

    private static final char[] ALPHANUM_SYMBOLS;
    private static final char[] ALL_SYMBOLS;

    static {
        // Merge NUMERIC + ALPHA symbols
        ALPHANUM_SYMBOLS = new char[NUM_SYMBOLS.length + ALPHA_SYMBOLS.length];
        System.arraycopy(NUM_SYMBOLS, 0, ALPHANUM_SYMBOLS, 0, NUM_SYMBOLS.length);
        System.arraycopy(ALPHA_SYMBOLS, 0, ALPHANUM_SYMBOLS, NUM_SYMBOLS.length, ALPHA_SYMBOLS.length);

        // Merge ALPHANUM + SPECIAL symbols
        ALL_SYMBOLS = new char[ALPHANUM_SYMBOLS.length + specialSymbols.length];
        System.arraycopy(ALPHANUM_SYMBOLS, 0, ALL_SYMBOLS, 0, ALPHANUM_SYMBOLS.length);
        System.arraycopy(specialSymbols, 0, ALL_SYMBOLS, ALPHANUM_SYMBOLS.length, specialSymbols.length);
    }

    private char[] buf;

    /**
     * Sets buffer length.
     *
     * @param length the length
     */
    public void setBufferLength(int length) {
        if (length < 1) throw new IllegalArgumentException("Buffer length must be >= 1: " + length);
        buf = new char[length];
    }

    @Override
    public String currentGuid() {
        return new String(buf);
    }

    @Override
    public String nextGuid() {
        return generateRandomString(ALL_SYMBOLS);
    }

    @Override
    public String nextGuid(IEnumCharSet.Types charSetType) {
        char[] charPool = switch (charSetType) {
            case NUMERIC -> NUM_SYMBOLS;
            case ALPHA -> ALPHA_SYMBOLS;
            case ALPHANUM -> ALPHANUM_SYMBOLS;
            case ALL -> ALL_SYMBOLS;
        };
        return generateRandomString(charPool);
    }

    private String generateRandomString(char[] charPool) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < buf.length; i++) {
            buf[i] = charPool[random.nextInt(charPool.length)];
        }
        return new String(buf);
    }
}