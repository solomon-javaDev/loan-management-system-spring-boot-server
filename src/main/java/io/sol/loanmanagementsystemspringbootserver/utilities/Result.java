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
        Logger.logInfo(message);
        return new Ok<>(message, value);
    }

    static <T> Result<T> notFound(String message, T value) {
        Logger.logError("Not Found: " + message);
        return new NotFound<>(message, value);
    }

    static <T> Result<T> invalid(String message, T value) {
        Logger.logError("Invalid: " + message);
        return new Invalid<>(message, value);
    }

    static <T> Result<T> unauthorized(String message, T value) {
        Logger.logError("Unauthorized: " + message);
        return new Unauthorized<>(message, value);
    }
}

/**
 * Represents a successful result of an operation. This class is part of the Result sealed interface and
 * specifically models the success scenario where an operation completes as expected. The success outcome is
 * associated with a message and a value.
 *
 * @param <T> the type of the value associated with the result
 * @param message descriptive message explaining the result of the operation
 * @param value the associated value representing the result
 */
record Ok<T>(String message, T value) implements Result<T> {}

/**
 * Represents a "not found" result of an operation. This class is part of the Result sealed interface
 * and models the scenario where an expected resource or piece of data could not be found.
 *
 * @param <T> the type of the value associated with the result
 * @param message a descriptive message providing details about the "not found" outcome
 * @param value the associated value, typically representing contextual information or a null if unavailable
 */
record NotFound<T>(String message, T value) implements Result<T> {}

/**
 * Represents an invalid result of an operation. This class is part of the Result sealed interface
 * and is used to model the scenario where an operation fails due to invalid inputs or states.
 *
 * @param <T> the type of the value associated with the result
 * @param message a descriptive message providing details about the invalid outcome
 * @param value the associated value, typically representing the invalid input or contextual information
 */
record Invalid<T>(String message, T value) implements Result<T> {}

/**
 * Represents an "unauthorized" result of an operation. This class is part of the Result sealed interface
 * and models the scenario where an operation fails due to lack of proper authorization or permissions.
 *
 * @param <T> the type of the value associated with the result
 * @param message a descriptive message providing details about the unauthorized outcome
 * @param value the associated value, which can provide additional context or details about the failure
 */
record Unauthorized<T>(String message, T value) implements Result<T> {}

