package eu.isygoit.factory;

/**
 * The type Abstract factory.
 *
 * @param <E> the type parameter
 */
public abstract class AbstractFactory<E> implements Factory<E> {

    private E instance;

    @Override
    public E instance() {
        if (this.instance == null) {
            this.instance = newInstance();
        }
        return this.instance;
    }
}
