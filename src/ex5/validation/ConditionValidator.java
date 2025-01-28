package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

/**
 * Validates if/while conditions in s-Java code by checking syntax, variable states, and types.
 * @author Tomer Zilberman
 */
public class ConditionValidator implements Validator {
    SymbolTable symbolTable;

    /**
     * Constructs a ConditionValidator with the given symbol table for variable tracking.
     *
     * @param symbolTable Symbol table to use for validation.
     */
    public ConditionValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Validates a single line, ensuring it matches an if/while condition block.
     *
     * @param line The line to validate.
     * @throws ValidationException If the line contains invalid conditions.
     */
    public void validate(String line) throws ValidationException {
        final String START_CONDITION = "(", END_CONDITION = ")";
        // Check for if/while syntax using regex
        if (line.matches(RegexUtils.IF_WHILE_BLOCK)) {
            String condition = line.substring(line.indexOf(START_CONDITION) + 1,
                    line.indexOf(END_CONDITION)).trim();
            validateCondition(condition);
        }
    }

    /**
     * Validates the condition inside an if/while block, ensuring all variables and literals are valid.
     *
     * @param overallCondition The overall condition string.
     * @throws ValidationException If any variable or literal in the condition is invalid.
     */
    public void validateCondition(String overallCondition) throws ValidationException {
        // Error messages for invalid conditions
        final String VARIABLE_UNDEFINED = "Variable <> is undefined";
        final String VARIABLE_UNINITIALIZED = "Variable <> is uninitialized";
        final String VARIABLE_INVALID_TYPE = "Variable <> has invalid type";
        final String LITERAL_UNDEFINED = "Literal <> is undefined";
        final String PLACEHOLDER = "<>";
        final int EXCEPTION_VALUE = -1;

        // Split conditions using logical operators (&&, ||)
        String[] conditions = overallCondition.split(RegexUtils.CONDITION_SPLITTERS);
        for (String condition : conditions) {
            condition = condition.trim();
            String literalType = RegexUtils.getLiteralType(condition);

            if (literalType.isEmpty()) {
                // Check if variable exists in the symbol table
                int lookupScope = symbolTable.findVariableScope(condition);
                if (lookupScope == EXCEPTION_VALUE) {
                    throw new ValidationException(VARIABLE_UNDEFINED.replace(PLACEHOLDER, condition));
                }

                // Check if variable is initialized
                if (!symbolTable.isVariableInitialized(lookupScope, condition)) {
                    throw new ValidationException(VARIABLE_UNINITIALIZED.replace(PLACEHOLDER, condition));
                }

                // Check if variable type is valid (boolean, double, or int)
                String type = symbolTable.getVariableType(lookupScope, condition);
                if (!(type.equals(RegexUtils.BOOLEAN) || type.equals(RegexUtils.DOUBLE) ||
                        type.equals(RegexUtils.INTEGER))) {
                    throw new ValidationException(VARIABLE_INVALID_TYPE.replace(PLACEHOLDER, condition));
                }
            } else if (!(literalType.equals(RegexUtils.BOOLEAN) || literalType.equals(RegexUtils.INTEGER) ||
                    literalType.equals(RegexUtils.DOUBLE))) {
                // Check if the literal type is valid
                throw new ValidationException(LITERAL_UNDEFINED.replace(PLACEHOLDER, condition));
            }
        }
        // Enter a new scope in the symbol table
        symbolTable.enterScope();
    }
}
