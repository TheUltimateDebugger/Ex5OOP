package ex5.validation;

import ex5.exceptions.ValidationException;

/**
 * An interface for validating lines of code in s-Java.
 * @author Tomer Zilberman
 */
public interface Validator {
    /**
     * Validates a given line of code.
     *
     * @param line The line to validate.
     * @throws ValidationException If the line is invalid.
     */
    void validate(String line) throws ValidationException;
}
