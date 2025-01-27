package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

public class ConditionValidator implements Validator {
    SymbolTable symbolTable;

    public ConditionValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void validate(String line) throws ValidationException {
        // if/while regex
        if (line.matches(RegexUtils.IF_WHILE_BLOCK)) {
            String condition = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
            validateCondition(condition);
        }
    }

    public void validateCondition(String overallCondition) throws ValidationException {
        // condition && and || regex for split
        String[] conditions = overallCondition.split(RegexUtils.CONDITION_SPLITTERS);
        for (String condition : conditions) {
            condition = condition.trim();
            String literalType = getLiteralType(condition);
            if (literalType.isEmpty()) {
                int lookupScope = symbolTable.findVariableScope(condition);
                if (lookupScope == -1) {
                    throw new ValidationException("Variable '" + condition + "' is undefined.");
                }
                if (!symbolTable.isVariableInitialized(lookupScope, condition)) {
                    throw new ValidationException("Variable '" + condition + "' is uninitialized.");
                }
                String type = symbolTable.getVariableType(lookupScope, condition);
                if (!(type.equals("boolean") || type.equals("double") || type.equals("int"))) {
                    throw new ValidationException("Variable '" + condition + "' has invalid type.");
                }
            }
            else if (!(literalType.equals("boolean") || literalType.equals("int") ||
                    literalType.equals("double"))) {
                throw new ValidationException("Literal '" + condition + "' is undefined.");
            }
        }
        symbolTable.enterScope();
    }

    private String getLiteralType(String literal) {
        // match literal to type regex
        if (literal.matches(RegexUtils.INTEGER_ONLY)) {
            return "int";
        } else if (literal.matches(RegexUtils.DOUBLE_ONLY)) {
            return "double";
        } else if (literal.matches(RegexUtils.BOOLEAN_ONLY)) {
            return "boolean";
        } else if (literal.matches(RegexUtils.STRING_ONLY)) {
            return "String";
        } else if (literal.matches(RegexUtils.CHAR_ONLY)) {
            return "char";
        }
        return "";
    }
}