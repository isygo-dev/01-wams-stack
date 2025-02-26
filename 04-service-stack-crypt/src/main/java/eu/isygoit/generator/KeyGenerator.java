package eu.isygoit.generator;

/**
 * The type Key generator.
 */
public class KeyGenerator extends AbstractKeyGenerator {
    /**
     * Instantiates a new Key generator.
     *
     * @param size the size
     */
    public KeyGenerator(int size) {
        this.setBufferlength(size);
    }
}
