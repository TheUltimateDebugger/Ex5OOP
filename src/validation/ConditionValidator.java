package validation;

import exceptions.ValidationException;

public class ConditionValidator implements Validator {
    SymbolTable symbolTable;

    public ConditionValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void validate(String line, int scope) throws ValidationException {
        if (line.matches("^(if|while)\\s*\\((.+)\\)\\s*\\{$")) {
            String condition = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
            validateCondition(condition);
        }
        if (line.matches("$\\s*}\\s*")) {
            symbolTable.exitScope();
        }
    }

    public void validateCondition(String overallCondition) throws ValidationException {
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

    private String getLiteralType(String literal) throws ValidationException {
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

    public static void main(String[] args) throws ValidationException {
        SymbolTable symbolTable = new SymbolTable();
        ConditionValidator validator = new ConditionValidator(symbolTable);
        validator.validate("if (-21) {", 0);
        validator.validate("int a = 1;", 1);
        validator.validate("}", 1);
        validator.validate("", 1);
    }
}