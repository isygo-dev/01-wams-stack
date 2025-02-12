package eu.isygoit.encrypt.generator;

import eu.isygoit.enums.IEnumCharSet;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract base class for key generation.
 * Provides methods to generate random keys using different character sets.
 */
@Slf4j
public abstract class AbstractKeyGenerator implements IKeyGenerator {

    // Character sets used for key generation
    private static final char[] NUMERIC_CHARACTERS = IntStream.rangeClosed('0', '9')
            .mapToObj(c -> (char) c)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString().toCharArray();

    private static final char[] ALPHABETIC_CHARACTERS = (IntStream.rangeClosed('A', 'Z')
            .mapToObj(c -> String.valueOf((char) c))
            .collect(Collectors.joining()) +
            IntStream.rangeClosed('a', 'z')
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining()))
            .toCharArray();

    private static final char[] ALPHANUMERIC_CHARACTERS = (new String(NUMERIC_CHARACTERS) + new String(ALPHABETIC_CHARACTERS)).toCharArray();

    private static final char[] ALL_CHARACTERS = (new String(ALPHANUMERIC_CHARACTERS) + "$#&@/-+={}[]()")
            .toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();
    private char[] keyBuffer;

    /**
     * Sets the length of the key buffer used for key generation.
     *
     * @param length The desired buffer length.
     * @throws IllegalArgumentException if length is less than 1.
     */
    public void setKeyBufferLength(int length) {
        if (length < 1) {
            log.error("Invalid key buffer length: {}", length);
            throw new IllegalArgumentException("keyBufferLength < 1: " + length);
        }
        log.info("Key buffer length set to: {}", length);
        keyBuffer = new char[length];
    }

    /**
     * Retrieves the current generated key.
     *
     * @return The current key stored in the buffer.
     */
    @Override
    public String getCurrentKey() {
        return new String(keyBuffer);
    }

    /**
     * Generates a new key using all available characters.
     *
     * @return A newly generated key.
     */
    @Override
    public String generateKey() {
        log.info("Generating new key using all available characters.");
        return populateKeyBuffer(ALL_CHARACTERS);
    }

    /**
     * Generates a new key based on the specified character set type.
     *
     * @param charSetType The type of character set to use.
     * @return A newly generated key using the specified character set.
     */
    @Override
    public String generateKey(IEnumCharSet.Types charSetType) {
        log.info("Generating new key with character set: {}", charSetType);
        char[] symbols;
        switch (charSetType) {
            case NUMERIC:
                symbols = NUMERIC_CHARACTERS;
                break;
            case ALPHA:
                symbols = ALPHABETIC_CHARACTERS;
                break;
            case ALPHANUM:
                symbols = ALPHANUMERIC_CHARACTERS;
                break;
            case ALL:
            default:
                symbols = ALL_CHARACTERS;
                break;
        }
        return populateKeyBuffer(symbols);
    }

    /**
     * Populates the key buffer with randomly selected characters from the given character set.
     *
     * @param symbols The array of characters to randomly select from.
     * @return The newly generated key as a string.
     */
    private String populateKeyBuffer(char[] symbols) {
        log.debug("Populating key buffer with random characters.");
        IntStream.range(0, keyBuffer.length)
                .forEach(idx -> keyBuffer[idx] = symbols[secureRandom.nextInt(symbols.length)]);
        log.debug("Key buffer populated successfully.");
        return new String(keyBuffer);
    }
}