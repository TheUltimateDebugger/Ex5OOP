package ex5.validation;

import ex5.exceptions.ValidationException;

public class VariableValidator implements Validator {
    private SymbolTable symbolTable;

    public VariableValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    public void validate(String line) throws ValidationException {
        // variable declaration regex (including initialization)
        if (line.matches("^(final\\s+)?(int|double|boolean|char|String)\\s+.+;$")) {
            boolean isFinal = line.startsWith("final");
            if (isFinal) { line = line.substring("final".length()).trim(); }
            String[] typeAndNames = line.split("\\s+", 2);
            String[] names = typeAndNames[1].split(",");
            names[names.length - 1] = names[names.length - 1].replace(";", "");

            for (String name : names) {
                String value = null;
                if (name.contains("=")) {
                    value = name.split("=", 2)[1].trim();
                    name = name.split("=", 2)[0].trim();
                }
                validateDeclaration(name, typeAndNames[0], isFinal, value!=null);
                if (value != null) {
                    validateAssignment(name, value, true);
                }
            }
        }
        // assignment regex
        else if (line.matches("^[a-zA-Z_][\\w]*\\s*=\\s*.+;$")) {
            String[] variables = line.split(",");
            variables[variables.length - 1] = variables[variables.length - 1].replace(";", "");
            for (String variable : variables) {
                String[] tokens = variable.split("=", 2);
                if (tokens.length != 2) {
                    throw new ValidationException("Invalid assignment syntax: " + line);
                }
                validateAssignment(tokens[0].trim(), tokens[1].trim(), false);
            }
        }

        else {
            throw new ValidationException("Invalid variable syntax: " + line);
        }
    }

    private void validateDeclaration(String name, String type,
                                    boolean isFinal, boolean isInitialized) throws ValidationException {
        if (name.matches("(.*__.*|_)")) {
            throw new ValidationException("Variable '" + name +
                    "' cannot have '__' in it or be only '_'");
        }
        if (symbolTable.variableExists(symbolTable.getScope(), name)) {
            throw new ValidationException("Variable '" + name +
                    "' already declared in the current scope.");
        }
        if (!isValidType(type)) {
            throw new ValidationException("Invalid type '" + type +
                    "' for variable '" + name + "'.");
        }
        if (symbolTable.getScope() == 0) {
            symbolTable.addGlobalVariable(name, type, isInitialized, isFinal);
        } else {
            symbolTable.addLocalVariable(name, type, isInitialized, isFinal);
        }
    }

    private void validateAssignment(String name, String value, boolean isDeclaration) throws ValidationException {
        int scope = symbolTable.findVariableScope(name);
        if (scope == -1) {
            throw new ValidationException("Variable '" + name +
                    "' does not exist in the current or parent scopes.");
        }
        if (!isDeclaration && symbolTable.isVariableFinal(scope, name)) {
            throw new ValidationException("Cannot assign to final variable '" + name + "'.");
        }

        String variableType = symbolTable.getVariableType(scope, name);
        if (!isTypeCompatible(variableType, value)) {
            throw new ValidationException("Type mismatch: Cannot assign value '" + value +
                    "' to variable '" + name + "' of type '" + variableType + "', or variable" +
                    "is uninitialized.");
        }
    }

    private boolean isTypeCompatible(String variableType, String value) {
        if (symbolTable.findVariableScope(value) != -1) {
            return variableType.equals(
                    symbolTable.getVariableType(symbolTable.findVariableScope(value), value)
            ) && symbolTable.isVariableInitialized(symbolTable.findVariableScope(value), value);
        }
        // regex matches per each type
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
        // TODO: add list of types and make this more dynamic
        return type.equals("int") || type.equals("double") || type.equals("boolean") ||
                type.equals("char") || type.equals("String");
    }
}