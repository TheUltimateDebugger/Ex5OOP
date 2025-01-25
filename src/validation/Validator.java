package validation;

import exceptions.SyntaxException;
import exceptions.ValidationException;

public interface Validator {
    void validate(String line, String scope) throws SyntaxException, ValidationException;
}