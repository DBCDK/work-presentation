/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along call this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.function.Supplier;
import javax.annotation.CheckReturnValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use a functional approach to leasing from a pool
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 * @param <T> Type of the pool leases
 */
public class QuickPool<T> extends GenericObjectPool<T> {

    private static final Logger log = LoggerFactory.getLogger(QuickPool.class);

    /**
     * Constructor for supplier
     *
     *
     * @param supplier Method that generates a T object
     */
    public QuickPool(Supplier<T> supplier) {
        super(new BasePooledObjectFactory<T>() {
            @Override
            public T create() throws Exception {
                log.debug("Creating object");
                T get = supplier.get();
                log.debug("Created object {}", get);
                return get;
            }

            @Override
            public PooledObject<T> wrap(T t) {
                log.debug("Wrapping object {}", t);
                return new DefaultPooledObject<>(t);
            }
        });
    }

    @Override
    public void returnObject(T obj) {
        log.debug("Returning object: {}", obj);
        super.returnObject(obj);
    }



    /**
     * Don't use this
     * <p>
     * Use:
     * {@link #exec(dk.dbc.search.work.presentation.worker.pool.QuickPool.ScopeWithExceptionAndValue)};
     *
     * @return N/A
     * @throws Exception N/A
     */
    @Deprecated
    @Override
    public T borrowObject() throws Exception {
        return super.borrowObject();
    }

    /**
     * Don't use this
     * <p>
     * Use:
     * {@link #exec(dk.dbc.search.work.presentation.worker.pool.QuickPool.ScopeWithExceptionAndValue)};
     *
     * @param ms N/A
     * @return N/A
     * @throws Exception N/A
     */
    @Deprecated
    @Override
    public T borrowObject(long ms) throws Exception {
        return super.borrowObject(ms);
    }

    /**
     * Lease an object from the pool, and call a method upon it
     *
     * @param <R>   Return type
     * @param scope Code block to run with the leased object
     * @return An exception wrapper or code block value
     */
    @CheckReturnValue
    public <R> ExceptionResult<R> exec(ScopeWithExceptionAndValue<T, R> scope) {
        try {
            T t = super.borrowObject();
            R value;
            try {
                value = scope.accept(t);
                returnObject(t);
                return new ExceptionResultValue<>(value);
            } catch (BadObjectException ex) {
                invalidateObject(t);
                return new ExceptionResultError<>(ex);
            } catch (Exception ex) {
                returnObject(t);
                return new ExceptionResultError<>(ex);
            }
        } catch (Exception ex) {
            return new ExceptionResultError<>(ex);
        }
    }

    @FunctionalInterface
    public interface ScopeWithExceptionAndValue<T, V> {

        V accept(T t) throws Exception;
    }

    /**
     * Data carrier for a value or an exception
     *
     * @param <T> The type of the value
     */
    public interface ExceptionResult<T> {

        /**
         * If an exception is carried, and of the given type, throw it
         *
         * @param <E>  Exception type
         * @param type The exception type
         * @return self for chaining
         * @throws E if an exception is (or is inherited from) E
         */
        @CheckReturnValue
        <E extends Exception> ExceptionResult<T> raise(Class<E> type) throws E;

        /**
         * If an exception is carried, and of the given type, throw it or any
         * cause if of the type, throw that
         *
         * @param <E>  Exception type
         * @param type The exception type
         * @return self for chaining
         * @throws E if an exception is (or is inherited from) E
         */
        @CheckReturnValue
        <E extends Exception> ExceptionResult<T> raiseNested(Class<E> type) throws E;

        /**
         * Get the value if a value is carried
         * <p>
         * If an exception is carried, throw that, optionally wrapped in a
         * {@link RuntimeException}
         *
         * @return value carried through
         * @throws RuntimeException if no value was carried
         */
        T value() throws RuntimeException;
    }

    private static class ExceptionResultValue<T> implements ExceptionResult<T> {

        private final T v;

        private ExceptionResultValue(T v) {
            this.v = v;
        }

        @Override
        public <E extends Exception> ExceptionResult<T> raise(Class<E> type) throws E {
            return this;
        }

        @Override
        public <E extends Exception> ExceptionResult<T> raiseNested(Class<E> type) throws E {
            return this;
        }

        @Override
        public T value() {
            return v;
        }
    }

    private static class ExceptionResultError<T> implements ExceptionResult<T> {

        private final Exception e;

        private ExceptionResultError(Exception e) {
            this.e = e;
        }

        @Override
        public <E extends Exception> ExceptionResult<T> raise(Class<E> type) throws E {
            if (type.isAssignableFrom(e.getClass()))
                throw (E) e;
            return this;
        }

        @Override
        public <E extends Exception> ExceptionResult<T> raiseNested(Class<E> type) throws E {
            Throwable t = e;
            while (t != null) {
                if (type.isAssignableFrom(t.getClass()))
                    throw (E) t;
            }
            return this;
        }

        @Override
        public T value() {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }
}
