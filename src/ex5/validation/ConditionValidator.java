package ex5.validation;

import ex5.exceptions.ValidationException;

public class ConditionValidator implements Validator {
    SymbolTable symbolTable;

    public ConditionValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void validate(String line) throws ValidationException {
        // if/while regex
        if (line.matches("^(if|while)\\s*\\((.+)\\)\\s*\\{$")) {
            String condition = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
            validateCondition(condition);
        }
    }

    public void validateCondition(String overallCondition) throws ValidationException {
        // condition && and || regex for split
        String[] conditions = overallCondition.split("(&&|\\|\\|)");
        for (String condition : conditions) {
            String literalType = getLiteralType(condition);
            if (literalType.isEmpty()) {
                int lookupScope = symbolTable.findVariableScope(condition);
                if (lookupScope == -1) {
                    throw new ValidationException("Variable '" + condition + "' is undefined.");
                }
                if (!symbolTable.isVariableInitialized(lookupScope, condition)) {
                    throw new ValidationException("Variable '" + condition + "' is uninitialized.");
                }
            }
            else if (!(literalType.equals("boolean") || literalType.equals("int") ||
                    literalType.equals("double"))) {
                throw new ValidationException("Literal '" + condition + "' is undefined.");
            }
            symbolTable.enterScope();
        }
    }

    private String getLiteralType(String literal) {
        // match literal to type regex
        if (literal.matches("^-?\\d+$")) {
            return "int";
        } else if (literal.matches("^-?\\d+\\.\\d+$")) {
            return "double";
        } else if (literal.equals("true") || literal.equals("false")) {
            return "boolean";
        } else if (literal.matches("^\".*\"$")) {
            return "String";
        } else if (literal.matches("^'.'$")) {
            return "char";
        }
        return "";
    }
}