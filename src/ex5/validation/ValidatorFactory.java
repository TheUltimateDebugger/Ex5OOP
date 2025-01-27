package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

import java.util.random.RandomGenerator;

public class ValidatorFactory {
    private ConditionValidator conditionValidator;
    private MethodValidator methodValidator;
    private VariableValidator variableValidator;
    private SymbolTable symbolTable;
    private boolean isInMethodBody = false, wasPreviousLineReturn = false;

    public ValidatorFactory(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        conditionValidator = new ConditionValidator(symbolTable);
        methodValidator = new MethodValidator(symbolTable);
        variableValidator = new VariableValidator(symbolTable);
    }
    public Validator getValidator(String line) throws ValidationException {
        line = line.trim();
        Validator returnValue = null;
        if (RegexUtils.matches(line, RegexUtils.RETURN_STATEMENT)) {
            wasPreviousLineReturn = true;
            returnValue = methodValidator;
        }
        else {
            if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
                symbolTable.exitScope();
                if (wasPreviousLineReturn) { isInMethodBody = false; }
                returnValue = null;
            } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
                if (symbolTable.getScope() == 0) { return null; }
                returnValue = variableValidator;
            } else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION)) {
                if (isInMethodBody) {
                    throw new ValidationException("Cannot declare method inside method");
                }
                isInMethodBody = true;
                if (symbolTable.getScope() == 1) { return null; }
                returnValue = methodValidator;
            } else if (RegexUtils.matches(line, RegexUtils.IF_WHILE_BLOCK)) {
                returnValue = conditionValidator;
            } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_VALUE_CHANGE)) {
                returnValue = variableValidator;
            } else if (RegexUtils.matches(line, RegexUtils.METHOD_CALL_ONLY)) {
                returnValue = methodValidator;
            } else {
                throw new ValidationException("Invalid line in global scope: " + line);
            }
            wasPreviousLineReturn = false;
        }

        return returnValue;
    }

    public Validator getValidatorForSweep(String line) throws ValidationException {
        if (RegexUtils.matches(line, RegexUtils.IF_WHILE_BLOCK)) {
            symbolTable.enterScope();
            return null;
        }
        if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
            symbolTable.exitScope();
            if (symbolTable.getScope() == 0) {
                isInMethodBody = false;
            }
            return null;
        }
        if (isInMethodBody) { return null; }
        if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
            return variableValidator;
        } else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION)) {
            methodValidator.validateMethodDeclarationForSweep(line);
            isInMethodBody = true;
            return null;
        } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_VALUE_CHANGE)) {
            return variableValidator;
        } else {
            throw new ValidationException("Invalid line in global scope: " + line);
        }
    }
}
