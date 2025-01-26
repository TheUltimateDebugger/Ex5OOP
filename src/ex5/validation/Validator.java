package ex5.validation;

import ex5.exceptions.SyntaxException;
import ex5.exceptions.ValidationException;

public interface Validator {
    void validate(String line, int scope) throws SyntaxException, ValidationException;
}