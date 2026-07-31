package io.sol.loanmanagementsystemspringbootserver.utilities;

/**
 * Represents a sealed interface to model the result of an operation. The interface can be used to
 * convey both success and failure outcomes with an associated message and value.
 *
 * @param <T> the type of the value associated with the result
 */
public sealed interface Result<T> permits Ok, NotFound, Invalid, Unauthorized {
    String message();

    T value();

    default boolean isSuccess() {
        return this instanceof Ok<?>;
    }

    default boolean isFailure() {
        return !isSuccess();
    }

    static <T> Result<T> success(String message, T value) {
        return new Ok<>(message, value);
    }

    static <T> Result<T> notFound(String message, T value) {
        return new NotFound<>(message, value);
    }

    static <T> Result<T> invalid(String message, T value) {
        return new Invalid<>(message, value);
    }

    static <T> Result<T> unauthorized(String message, T value) {
        return new Unauthorized<>(message, value);
    }
}

record Ok<T>(String message, T value) implements Result<T> {}
record NotFound<T>(String message, T value) implements Result<T> {}
record Invalid<T>(String message, T value) implements Result<T> {}
record Unauthorized<T>(String message, T value) implements Result<T> {}

