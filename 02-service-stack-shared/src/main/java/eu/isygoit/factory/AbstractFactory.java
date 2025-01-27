package eu.isygoit.factory;

import java.util.Objects;

/**
 * The type Abstract factory.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractFactory<T> implements Factory<T> {

    private T instance;

    @Override
    public T instance() {
        if (Objects.isNull(this.instance)) {
            this.instance = newInstance();
        }
        return this.instance;
    }
}
