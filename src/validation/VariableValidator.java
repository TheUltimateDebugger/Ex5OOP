package validation;

import exceptions.ValidationException;

public class VariableValidator implements Validator {
    private SymbolTable symbolTable;

    public VariableValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    // TODO: remove usage of scope int - we're always at the smallest one.
    public void validate(String line, int scope) throws ValidationException {
        if (line.matches("^(final\\s+)?(int|double|boolean|char|String)\\s+.+;$")) {
            boolean isFinal = line.startsWith("final");
            if (isFinal) { line = line.substring("final".length()); }
            String[] typeAndNames = line.split("\\s+", 2);
            String[] names = typeAndNames[1].split(",");
            names[names.length - 1] = names[names.length - 1].replace(";", "");

            for (String name : names) {
                String value = null;
                if (name.contains("=")) {
                    value = name.split("=", 2)[1].trim();
                    name = name.split("=", 2)[0].trim();
                }
                validateDeclaration(scope, name, typeAndNames[0], isFinal, value==null);
                if (value != null) {
                    validateAssignment(name, value);
                }
            }
        }

        else if (line.matches("^[a-zA-Z_][\\w]*\\s*=\\s*.+;$")) {
            String[] variables = line.split(",");
            variables[variables.length - 1] = variables[variables.length - 1].replace(";", "");
            for (String variable : variables) {
                String[] tokens = variable.split("=", 2);
                if (tokens.length != 2) {
                    throw new ValidationException("Invalid assignment syntax: " + line);
                }
                validateAssignment(tokens[0].trim(), tokens[1].trim());
            }
        }

        else {
            throw new ValidationException("Invalid variable syntax: " + line);
        }
    }

    private void validateDeclaration(int scope, String name, String type,
                                    boolean isFinal, boolean isInitialized) throws ValidationException {
        if (symbolTable.variableExists(scope, name)) {
            throw new ValidationException("Variable '" + name +
                    "' already declared in the current scope.");
        }
        if (!isValidType(type)) {
            throw new ValidationException("Invalid type '" + type +
                    "' for variable '" + name + "'.");
        }
        if (scope == 0) {
            symbolTable.addGlobalVariable(name, type, isInitialized, isFinal);
        } else {
            symbolTable.addLocalVariable(name, type, isInitialized, isFinal);
        }
    }

    private void validateAssignment(String name, String value) throws ValidationException {
        int scope = symbolTable.findVariableScope(name);
        if (scope == -1) {
            throw new ValidationException("Variable '" + name +
                    "' does not exist in the current or parent scopes.");
        }
        if (symbolTable.isVariableFinal(scope, name)) {
            throw new ValidationException("Cannot assign to final variable '" + name + "'.");
        }

        String variableType = symbolTable.getVariableType(scope, name);
        if (!isTypeCompatible(variableType, value)) {
            // TODO: change print for uninitialized vars
            throw new ValidationException("Type mismatch: Cannot assign value '" + value +
                    "' to variable '" + name + "' of type '" + variableType + "'.");
        }
    }

    private boolean isTypeCompatible(String variableType, String value) {
        if (symbolTable.findVariableScope(value) != -1) {
            return variableType.equals(
                    symbolTable.getVariableType(symbolTable.findVariableScope(value), value)
            ) && symbolTable.isVariableInitialized(symbolTable.findVariableScope(value), value);
        }
        switch (variableType) {
            case "int":
                return value.matches("-?\\d+");
            case "double":
                return value.matches("-?\\d+(\\.\\d+)?");
            case "boolean":
                return value.equals("true") || value.equals("false") ||
                        value.matches("-?\\d+(\\.\\d+)?");
            case "char":
                return value.matches("'.'");
            case "String":
                return value.matches("\".*\"");
            default:
                return false;
        }
    }

    private boolean isValidType(String type) {
        return type.equals("int") || type.equals("double") || type.equals("boolean") ||
                type.equals("char") || type.equals("String");
    }

    // TODO: remove this
    public static void main(String[] args) throws ValidationException {
        SymbolTable symbolTable = new SymbolTable();
        VariableValidator validator = new VariableValidator(symbolTable);
        validator.validate("int a = 10;", 0);
        validator.validate("a = 10;", 0);
        symbolTable.enterScope();
        validator.validate("int a = 20, b = 30,c=40;", 1);
    }
}