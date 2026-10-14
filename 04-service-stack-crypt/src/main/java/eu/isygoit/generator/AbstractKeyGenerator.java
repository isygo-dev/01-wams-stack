package eu.isygoit.generator;

import eu.isygoit.enums.IEnumCharSet;
import eu.isygoit.exception.BadArgumentException;

import java.security.SecureRandom;

/**
 * The type Abstract key generator.
 * <p>
 * Randomness comes from a {@link SecureRandom} (the output is used for passwords, one-time codes and keys), and no
 * per-call state is shared between threads: each call fills its own local buffer.
 */
public abstract class AbstractKeyGenerator implements IKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final char[] NUM_SYMBOLS = "0123456789".toCharArray();
    private static final char[] ALPHA_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] specialSymbols = "$#&@/-+={}[]()".toCharArray();

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

    private volatile int length = 1;
    private volatile String current = "";

    private static char[] poolFor(IEnumCharSet.Types charSetType) {
        return switch (charSetType) {
            case NUMERIC -> NUM_SYMBOLS;
            case ALPHA -> ALPHA_SYMBOLS;
            case ALPHANUM -> ALPHANUM_SYMBOLS;
            case ALL -> ALL_SYMBOLS;
        };
    }

    /**
     * Sets the default length used by {@link #nextGuid()} and {@link #nextGuid(IEnumCharSet.Types)}.
     *
     * @param length the length
     */
    public void setBufferLength(int length) {
        if (length < 1) throw new BadArgumentException("Buffer length must be >= 1: " + length);
        this.length = length;
    }

    /**
     * Last generated value (informational only; with concurrent callers it is simply "a recent value").
     */
    @Override
    public String currentGuid() {
        return current;
    }

    @Override
    public String nextGuid() {
        return generate(length, ALL_SYMBOLS);
    }

    @Override
    public String nextGuid(IEnumCharSet.Types charSetType) {
        return generate(length, poolFor(charSetType));
    }

    /**
     * Generates a value of an explicit length without touching the generator's shared default length, so it is
     * safe to call concurrently from a singleton.
     *
     * @param length      the length, at least 1
     * @param charSetType the char set
     * @return the generated value
     */
    protected String generate(int length, IEnumCharSet.Types charSetType) {
        if (length < 1) throw new BadArgumentException("Length must be >= 1: " + length);
        return generate(length, poolFor(charSetType));
    }

    private String generate(int length, char[] charPool) {
        char[] out = new char[length];
        for (int i = 0; i < out.length; i++) {
            out[i] = charPool[RANDOM.nextInt(charPool.length)];
        }
        String value = new String(out);
        this.current = value;
        return value;
    }
}
