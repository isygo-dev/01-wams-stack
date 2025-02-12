package eu.isygoit.factory;

/**
 * The type Abstract factory.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractFactory<T> implements Factory<T> {

    private T instance;

    @Override
    public T instance() {
        if (this.instance == null) {
            this.instance = newInstance();
        }
        return this.instance;
    }
}
