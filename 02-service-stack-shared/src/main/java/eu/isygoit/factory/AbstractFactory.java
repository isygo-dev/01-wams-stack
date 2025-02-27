package eu.isygoit.factory;

import java.lang.reflect.ParameterizedType;

/**
 * The type Abstract factory.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractFactory<T> implements Factory<T> {
    private T instance;
    protected final Class<T> type;

    @SuppressWarnings("unchecked")
    public AbstractFactory() {
        // Extract generic type parameter once during construction
        type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public T instance() {
        // Double-checked locking pattern for thread safety and performance
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = newInstance();
                }
            }
        }
        return instance;
    }

    @Override
    public T newInstance() {
        try {
            // Direct call to newInstance() is simpler in most cases than using getDeclaredConstructor()
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + type.getName(), e);
        }
    }
}
