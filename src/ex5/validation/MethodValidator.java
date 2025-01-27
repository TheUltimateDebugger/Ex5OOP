package ex5.validation;

import ex5.exceptions.ValidationException;

import java.util.ArrayList;

public class MethodValidator implements Validator {
    private final SymbolTable symbolTable;

    public MethodValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    public void validate(String line) throws ValidationException {
        // declaration regex
        if (line.matches("^\\s*void\\s+[a-zA-Z_][\\w]*\\s*\\(.*\\)\\s*\\{$")) {
            validateMethodDeclaration(line);
        }
        // method call regex
        else if (line.matches("^\\s*[a-zA-Z_][\\w]*\\s*\\(.*\\)\\s*;$")) {
            validateMethodCall(line);
        }
        // return regex
        else if (line.matches("^\\s*return\\s*;$")) {
            // the call to this method in case of return is unneeded, but it is good practice
            // in case we'd like to add functionality to return statements.
        }
        else {
            throw new ValidationException("Invalid method line: " + line);
        }
    }

    public void validateMethodDeclaration(String line) throws ValidationException {
        String[] nameAndParams = line.substring(4, line.indexOf('(')).trim().split("\\s+");
        String methodName = nameAndParams[nameAndParams.length - 1];
        symbolTable.addMethodParams(methodName);
    }

    public void validateMethodDeclarationForSweep(String line) throws ValidationException {
        String[] nameAndParams = line.substring(4, line.indexOf('(')).trim().split("\\s+");
        String methodName = nameAndParams[nameAndParams.length - 1];
        if (methodName.matches("(.*__*.|_)")) {
            throw new ValidationException("Method '" + methodName
                    + "' cannot start with '_' or contain '__'");
        }
        if (symbolTable.methodExists(methodName)) {
            throw new ValidationException("Method '" + methodName + "' is already declared.");
        }
        String paramsSection = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        ArrayList<String[]> parameters = parseParameters(paramsSection);
        symbolTable.addMethod(methodName, parameters);
    }

    public void validateMethodCall(String line) throws ValidationException {
        // TODO: check requirements for whitespaces (in general, not just this case)
        String methodName = line.substring(0, line.indexOf('(')).trim();
        if (!symbolTable.methodExists(methodName)) {
            throw new ValidationException("Method '" + methodName + "' does not exist.");
        }

        String argsSection = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        ArrayList<String[]> arguments = parseArguments(argsSection);
        ArrayList<String[]> parameters = symbolTable.getMethodParameters(methodName);
        if (parameters.size() != arguments.size()) {
            throw new ValidationException("Method '" + methodName + "' expects " +
                    parameters.size() + " arguments but got " + arguments.size() + ".");
        }
        for (int i = 0; i < parameters.size(); i++) {
            String expectedType = parameters.get(i)[0];
            String argumentType = arguments.get(i)[0];         // Argument value or variable name

            if (!expectedType.equals(argumentType)) {
                throw new ValidationException("Argument '" + arguments.get(i)[1] + "' of type '" +
                        argumentType + "' is incompatible with expected type '" +
                        expectedType + "' in method '" + methodName + "'.");
            }
        }
    }

    private ArrayList<String[]> parseParameters(String rawParameters)
            throws ValidationException {
        ArrayList<String[]> parametersList = new ArrayList<>();
        // empty regex
        if (rawParameters == null || rawParameters.matches("^\\s*$")) { return parametersList; }
        String[] parameters = rawParameters.split(",");
        for (String parameter : parameters) {
            String[] parameterParts = parameter.trim().split("\\s+", 3);
            if (parameterParts.length == 3) {
                if (!parameterParts[0].equals("final")) {
                    throw new ValidationException("Parameter '" + parameterParts[0] +
                            "' is not a valid parameter.");
                }
                parameterParts = new String[] {parameterParts[0] + " " +parameterParts[1],
                        parameterParts[2]};
            }
            if (parameterParts.length != 2) {
                throw new ValidationException("Invalid parameter syntax: " + parameter);
            }
            if (!isValidType(parameterParts[0])) {
               throw new ValidationException("Invalid parameter type: " + parameterParts[0]);
            }
            for (int i = 1; i < parametersList.size(); i++) {
                if (parametersList.get(i)[1].equals(parameterParts[1])) {
                    throw new ValidationException("Duplicate parameter name: " + parameterParts[1]);
                }
            }

            parametersList.add(parameterParts);
        }

        return parametersList;
    }

    private ArrayList<String[]> parseArguments(String rawArguments) throws ValidationException {
        ArrayList<String[]> arguments = new ArrayList<>();
        if (rawArguments.isEmpty()) {
            return arguments; // No arguments
        }

        String[] args = rawArguments.split(",");
        String type;
        for (String arg : args) {
            arg = arg.trim();
            int argScope = symbolTable.findVariableScope(arg);
            if (argScope != -1) {
                type = symbolTable.getVariableType(argScope, arg);
            }
            else {
                type = getLiteralType(arg);
            }
            arguments.add(new String[]{type, arg});
        }
        return arguments;
    }

    private String getLiteralType(String argument) throws ValidationException {
        // match literal to type regex
        if (argument.matches("^-?\\d+$")) {
            return "int";
        } else if (argument.matches("^-?\\d+\\.\\d+$")) {
            return "double";
        } else if (argument.equals("true") || argument.equals("false")) {
            return "boolean";
        } else if (argument.matches("^\".*\"$")) {
            return "String";
        } else if (argument.matches("^'.'$")) {
            return "char";
        }
        else {
            throw new ValidationException("Argument '" + argument + "' is of unknown type");
        }
    }

    private boolean isValidType(String type) {
        if (type.contains("final")) {
            type = type.replace("final", "");
        }
        type = type.trim();
        return type.equals("int") || type.equals("double") || type.equals("boolean") ||
                type.equals("char") || type.equals("String");
    }

}
