/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-service
 *
 * work-presentation-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.service;

import java.util.function.Supplier;

/**
 * Wrapper to raise exceptions from nested exceptions
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 * @param <T> result type
 */
public abstract class ExceptionSafe<T> {

    /**
     * Value producer like {@link Supplier}, that can throw exceptions
     *
     * @param <T> result type
     */
    public interface Executable<T> {

        T execute() throws Exception;
    }

    /**
     * Wrap a value in a exception helper
     *
     * @param <T>  result type
     * @param exec value producer
     * @return value or exception wrapper
     */
    public static <T> ExceptionSafe<T> wrap(Executable<T> exec) {
        try {
            return new Value<>(exec.execute());
        } catch (Exception ex) {
            return new Error<>(ex);
        }
    }

    /**
     * Try to raise a (nested?) exception from the exception caused by the value
     * producer
     *
     * @param <E> exception type
     * @param e   exception class reference
     * @return self for chaining
     * @throws E exception found in the nested causes
     */
    public abstract <E extends Exception> ExceptionSafe<T> raise(Class<E> e) throws E;

    /**
     * Get the value from the value producer
     *
     * @return the value
     * @throws RuntimeException if no value was produced
     */
    public abstract T get();

    /**
     * Implementation for values
     *
     * @param <T> result type
     */
    private static class Value<T> extends ExceptionSafe<T> {

        private final T value;

        public Value(T value) {
            this.value = value;
        }

        @Override
        public <E extends Exception> ExceptionSafe<T> raise(Class<E> e) throws E {
            return this;
        }

        @Override
        public T get() {
            return value;
        }
    }

    /**
     * Implementation for exceptions
     *
     * @param <T> result type
     */
    private static class Error<T> extends ExceptionSafe<T> {

        private final Exception ex;

        public Error(Exception ex) {
            this.ex = ex;
        }

        @Override
        public <E extends Exception> ExceptionSafe<T> raise(Class<E> e) throws E {
            for (Throwable t = ex ; t != null ; t = t.getCause()) {
                if (e.isAssignableFrom(t.getClass()))
                    throw (E) t;
            }
            return this;
        }

        @Override
        public T get() {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}
