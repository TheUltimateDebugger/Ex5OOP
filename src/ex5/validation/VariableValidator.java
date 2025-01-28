package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

/**
 * The VariableValidator class is responsible for validating variable declarations,
 * assignments, and ensuring proper syntax and type compatibility within a given symbol table.
 * @author Tomer Zilberman
 */
public class VariableValidator implements Validator {

    /** The symbol table used for managing variable scopes and definitions. */
    private SymbolTable symbolTable;

    /** Predefined constants used in variable validation. */
    private static final String DEFINING_VALUE_CHAR = "=",
            VAR_DELIMITER = ",", END_LINE = ";", EMPTY_STRING = "";

    /**
     * Error value constant used for identifying invalid scenarios.
     */
    private static final int ERROR_VALUE = -1;

    /**
     * Constructor for VariableValidator.
     *
     * @param symbolTable The symbol table instance used for variable management.
     */
    public VariableValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Validates a given line of code for proper variable declaration or assignment syntax.
     *
     * @param line The line of code to validate.
     * @throws ValidationException if the validation fails.
     */
    public void validate(String line) throws ValidationException {
        // Constants used for splitting and error messages
        final int SPLITTING_LIMIT = 2;
        final int VALUE_INDEX = 0, NAME_INDEX = 1;
        final String VAR_NOT_INITIALIZED = "Cannot assign value from null variable '<>'.",
                FINAL_VAR_NULL = "Final variable '<>' cannot be null",
                INVALID_ASSIGNMENT_LINE = "Invalid assignment syntax: " + line,
                INVALID_VAR_LINE = "Invalid variable syntax: " + line;
        final String PLACEHOLDER = "<>";

        // Validate variable declaration
        if (line.matches(RegexUtils.VARIABLE_DECLARATION)) {
            boolean isFinal = line.startsWith(RegexUtils.FINAL);
            if (isFinal) {
                line = line.substring(RegexUtils.FINAL.length()).trim();
            }
            String[] typeAndNames = line.split(RegexUtils.SPACES, SPLITTING_LIMIT);
            String[] names = typeAndNames[NAME_INDEX].split(VAR_DELIMITER);
            names[names.length - 1] = names[names.length - 1].replace(END_LINE, EMPTY_STRING);

            for (String name : names) {
                String value = null;
                if (name.contains(DEFINING_VALUE_CHAR)) {
                    value = name.split(DEFINING_VALUE_CHAR, SPLITTING_LIMIT)[NAME_INDEX].trim();
                    name = name.split(DEFINING_VALUE_CHAR, SPLITTING_LIMIT)[VALUE_INDEX].trim();
                }
                if (value != null) {
                    int valueScope = symbolTable.findVariableScope(value);
                    if ((valueScope >= 0 && !symbolTable.isVariableInitialized(valueScope, value)) ||
                            (value.equals(name) && valueScope == -1)) {
                        throw new ValidationException(VAR_NOT_INITIALIZED.replace(PLACEHOLDER, name));
                    }
                }
                validateDeclaration(name, typeAndNames[0], isFinal, value != null);
                if (value == null && isFinal) {
                    throw new ValidationException(FINAL_VAR_NULL.replace(PLACEHOLDER, name));
                }
                if (value != null) {
                    validateAssignment(name, value, true);
                }
            }
        }
        // Validate variable assignment
        else if (line.matches(RegexUtils.VARIABLE_VALUE_CHANGE)) {
            String[] variables = line.split(VAR_DELIMITER);
            variables[variables.length - 1] = variables[variables.length - 1]
                    .replace(END_LINE, EMPTY_STRING);
            for (String variable : variables) {
                String[] tokens = variable.split(DEFINING_VALUE_CHAR, SPLITTING_LIMIT);
                if (tokens.length != 2) {
                    throw new ValidationException(INVALID_ASSIGNMENT_LINE);
                }
                validateAssignment(tokens[0].trim(), tokens[1].trim(), false);
            }
        } else {
            throw new ValidationException(INVALID_VAR_LINE);
        }
    }

    /**
     * Validates a variable declaration.
     *
     * @param name         The variable name.
     * @param type         The variable type.
     * @param isFinal      Whether the variable is final.
     * @param isInitialized Whether the variable is initialized.
     * @throws ValidationException if the declaration is invalid.
     */
    private void validateDeclaration(String name, String type, boolean isFinal, boolean isInitialized)
            throws ValidationException {
        final String ILLEGAL_VAR_NAME = "Variable '" + name + "' cannot have '__' in it or be only '_'";
        final String ALREADY_DECLARED = "Variable '" + name + "' already declared in the current scope.";
        final String INVALID_TYPE = "Invalid type '" + type + "' for variable '" + name + "'.";

        if (name.matches(RegexUtils.ILLEGAL_VARIABLE_NAME)) {
            throw new ValidationException(ILLEGAL_VAR_NAME);
        }
        if (symbolTable.variableExists(symbolTable.getScope(), name)) {
            throw new ValidationException(ALREADY_DECLARED);
        }
        if (!isValidType(type)) {
            throw new ValidationException(INVALID_TYPE);
        }

        if (symbolTable.getScope() == 0) {
            symbolTable.addGlobalVariable(name, type, isInitialized, isFinal);
        } else {
            symbolTable.addLocalVariable(name, type, isInitialized, isFinal);
        }
    }

    /**
     * Validates an assignment operation.
     *
     * @param name          The variable name being assigned.
     * @param value         The value being assigned.
     * @param isDeclaration Whether this is part of a declaration.
     * @throws ValidationException if the assignment is invalid.
     */
    private void validateAssignment(String name, String value, boolean isDeclaration)
            throws ValidationException {
        final String VAR_NOT_EXIST = "Variable '" + name + "' does not exist in the current or parent scopes.";
        final String FINAL_VAR_ASSIGNMENT = "Cannot assign to final variable '" + name + "'.";
        final String MISMATCH_TYPES = "Type mismatch: Cannot assign value '" + value +
                "' to variable '" + name + "' of type '<>' or variable is uninitialized.";

        int scope = symbolTable.findVariableScope(name);
        if (scope == -1) {
            throw new ValidationException(VAR_NOT_EXIST);
        }
        if (!isDeclaration && symbolTable.isVariableFinal(scope, name)) {
            throw new ValidationException(FINAL_VAR_ASSIGNMENT);
        }

        String variableType = symbolTable.getVariableType(scope, name);
        if (!isTypeCompatible(variableType, value)) {
            throw new ValidationException(MISMATCH_TYPES.replace("<>", variableType));
        }
        symbolTable.initializeVariable(scope, name);
    }

    /**
     * Checks if a value is compatible with a given variable type.
     *
     * @param variableType The type of the variable.
     * @param value        The value to check.
     * @return True if compatible, false otherwise.
     */
    private boolean isTypeCompatible(String variableType, String value) {
        if (symbolTable.findVariableScope(value) != -1) {
            return variableType.equals(
                    symbolTable.getVariableType(symbolTable.findVariableScope(value), value)
            ) && symbolTable.isVariableInitialized(symbolTable.findVariableScope(value), value);
        }
        value = value.trim();

        switch (variableType) {
            case RegexUtils.INTEGER:
                return value.matches(RegexUtils.INTEGER_ONLY);
            case RegexUtils.DOUBLE:
                return value.matches(RegexUtils.DOUBLE_ONLY);
            case RegexUtils.BOOLEAN:
                return value.matches(RegexUtils.BOOLEAN_ONLY) || value.matches(RegexUtils.DOUBLE_ONLY);
            case RegexUtils.CHAR:
                return value.matches(RegexUtils.CHAR_ONLY);
            case RegexUtils.STRING:
                return value.matches(RegexUtils.STRING_ONLY);
            default:
                return false;
        }
    }

    /**
     * Checks if a given type is valid.
     *
     * @param type The type to check.
     * @return True if the type is valid, false otherwise.
     */
    private boolean isValidType(String type) {
        return type.equals(RegexUtils.INTEGER) || type.equals(RegexUtils.DOUBLE) ||
                type.equals(RegexUtils.BOOLEAN) || type.equals(RegexUtils.CHAR) ||
                type.equals(RegexUtils.STRING);
    }
}
