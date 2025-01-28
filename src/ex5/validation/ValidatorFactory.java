package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

/**
 * Factory class for creating and managing different types of validators.
 * Handles validation for various lines of code, including conditions, methods, and variables.
 */
public class ValidatorFactory {

    /** Validator for condition blocks (if/while statements). */
    private ConditionValidator conditionValidator;

    /** Validator for method declarations and calls. */
    private MethodValidator methodValidator;

    /** Validator for variable declarations and modifications. */
    private VariableValidator variableValidator;

    /** The symbol table used to track variable and method scopes. */
    private SymbolTable symbolTable;

    /** Flag indicating whether the current context is inside a method body. */
    private boolean isInMethodBody = false;

    /** Flag indicating whether the previous line was a return statement. */
    private boolean wasPreviousLineReturn = false;

    /**
     * Constructor for the ValidatorFactory.
     *
     * @param symbolTable The symbol table used for managing variable and method scopes.
     */
    public ValidatorFactory(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        conditionValidator = new ConditionValidator(symbolTable);
        methodValidator = new MethodValidator(symbolTable);
        variableValidator = new VariableValidator(symbolTable);
    }

    /**
     * Returns the appropriate validator for a given line of code based on its type.
     *
     * @param line The line of code to be validated.
     * @return The appropriate Validator instance, or null if no validation is needed.
     * @throws ValidationException If the line is invalid or violates certain rules.
     */
    public Validator getValidator(String line) throws ValidationException {
        final String DECLARE_IN_METHOD = "Cannot declare method inside method";
        final String INVALID_LINE = "Invalid line in global scope: " + line;
        line = line.trim();
        Validator returnValue = null;

        if (RegexUtils.matches(line, RegexUtils.RETURN_STATEMENT)) {
            // Handle return statement validation
            wasPreviousLineReturn = true;
            returnValue = methodValidator;
        } else {
            if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
                // Handle scope closure
                symbolTable.exitScope();
                if (wasPreviousLineReturn) {
                    isInMethodBody = false;
                    symbolTable.resetGlobalsToGlobalInitializationState();
                }
                returnValue = null;
            } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
                // Handle variable declarations (only allowed within a non-global scope)
                if (symbolTable.getScope() == 0) {
                    return null;
                }
                returnValue = variableValidator;
            } else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION_ONLY)) {
                // Handle method declarations
                if (isInMethodBody) {
                    throw new ValidationException(DECLARE_IN_METHOD);
                }
                isInMethodBody = true;
                if (symbolTable.getScope() == 1) {
                    return null;
                }
                returnValue = methodValidator;
            } else if (RegexUtils.matches(line, RegexUtils.IF_WHILE_BLOCK)) {
                // Handle condition blocks (if/while)
                returnValue = conditionValidator;
            } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_VALUE_CHANGE)) {
                // Handle variable value modifications
                returnValue = variableValidator;
            } else if (RegexUtils.matches(line, RegexUtils.METHOD_CALL_ONLY)) {
                // Handle method calls
                returnValue = methodValidator;
            } else {
                // Invalid line in the global scope
                throw new ValidationException(INVALID_LINE);
            }
            wasPreviousLineReturn = false;
        }
        return returnValue;
    }

    /**
     * Returns the appropriate validator for the sweep phase of code analysis.
     *
     * @param line The line of code to be validated during the sweep phase.
     * @return The appropriate Validator instance, or null if no validation is needed.
     * @throws ValidationException If the line is invalid or violates certain rules.
     */
    public Validator getValidatorForSweep(String line) throws ValidationException {
        final String INVALID_LINE = "Invalid line in global scope: " + line;

        if (RegexUtils.matches(line, RegexUtils.IF_WHILE_BLOCK)) {
            // Enter a new scope for condition blocks
            symbolTable.enterScope();
            return null;
        }

        if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
            // Exit the current scope
            symbolTable.exitScope();
            if (symbolTable.getScope() == 0) {
                isInMethodBody = false;
            }
            return null;
        }

        if (isInMethodBody) {
            // Skip validation inside method body during sweep
            return null;
        }

        if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
            // Handle variable declarations
            return variableValidator;
        } else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION_ONLY)) {
            // Validate method declarations during the sweep phase
            methodValidator.validateMethodDeclarationForSweep(line);
            isInMethodBody = true;
            return null;
        } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_VALUE_CHANGE)) {
            // Handle variable value modifications
            return variableValidator;
        } else {
            // Invalid line in the global scope
            throw new ValidationException(INVALID_LINE);
        }
    }
}
