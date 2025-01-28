package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

import java.util.ArrayList;

/**
 * Validates method declarations, calls, and return statements in s-Java code.
 * @author Tomer Zilberman
 */
public class MethodValidator implements Validator {
    private static final char START_BRACKET = '(', END_BRACKET = ')';
    private static final String PARAM_DELIMITER = ",", SPACE = " ";
    private final SymbolTable symbolTable;

    /**
     * Constructs a MethodValidator with the provided symbol table for method tracking.
     *
     * @param symbolTable Symbol table for method and variable validation.
     */
    public MethodValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Validates a method-related line for declaration, call, or return statement.
     *
     * @param line The line to validate.
     * @throws ValidationException If the line is invalid or contains errors.
     */
    public void validate(String line) throws ValidationException {
        final String INVALID_METHOD = "Invalid method line: <>";
        final String PLACEHOLDER = "<>";

        if (line.matches(RegexUtils.METHOD_DECLARATION_ONLY)) {
            validateMethodDeclaration(line);
        } else if (line.matches(RegexUtils.METHOD_CALL_ONLY)) {
            validateMethodCall(line);
        } else if (!line.matches(RegexUtils.RETURN_STATEMENT)) {
            throw new ValidationException(INVALID_METHOD.replace(PLACEHOLDER, line));
        }
    }

    /**
     * Validates a method declaration and registers its parameters in the symbol table.
     *
     * @param line The method declaration line.
     * @throws ValidationException If the declaration contains errors.
     */
    public void validateMethodDeclaration(String line) throws ValidationException {
        final int VOID_LENGTH = 4;
        String[] nameAndParams = line.substring(VOID_LENGTH,
                line.indexOf(START_BRACKET)).trim().split(RegexUtils.SPACES);
        String methodName = nameAndParams[nameAndParams.length - 1];
        symbolTable.addMethodParams(methodName);
    }

    /**
     * Validates a method declaration during the sweeping phase.
     *
     * @param line The method declaration line.
     * @throws ValidationException If the method name is illegal or already declared.
     */
    public void validateMethodDeclarationForSweep(String line) throws ValidationException {
        final String INVALID_METHOD = "Method '<>' cannot start with '_' or contain '__'";
        final String ALREADY_DECLARED = "Method '<>' is already declared";
        final String PLACEHOLDER = "<>";
        final int VOID_LENGTH = 4;

        String[] nameAndParams = line.substring(VOID_LENGTH,
                line.indexOf(START_BRACKET)).trim().split(RegexUtils.SPACES);
        String methodName = nameAndParams[nameAndParams.length - 1];

        if (methodName.matches(RegexUtils.ILLEGAL_METHOD_NAME)) {
            throw new ValidationException(INVALID_METHOD.replace(PLACEHOLDER, methodName));
        }
        if (symbolTable.methodExists(methodName)) {
            throw new ValidationException(ALREADY_DECLARED.replace(PLACEHOLDER, methodName));
        }

        String paramsSection = line.substring(line.indexOf(START_BRACKET) + 1,
                line.indexOf(END_BRACKET)).trim();
        ArrayList<String[]> parameters = parseParameters(paramsSection);
        symbolTable.addMethod(methodName, parameters);
    }

    /**
     * Validates a method call, including argument types and counts.
     *
     * @param line The method call line.
     * @throws ValidationException If the method is undefined or arguments are invalid.
     */
    public void validateMethodCall(String line) throws ValidationException {
        final String METHOD_NOT_EXISTS = "Method '<>' does not exist";
        final String METHOD_ARGS = "Method '<1>' expects '<2>' arguments but got '<3>'";
        final String ARGUMENT_INCOMPATIBLE =
            "Argument '<1>' of type '<2>' is not compatible with argument '<3>' in method '<4>'";
        final String PLACEHOLDER_1 = "<1>";
        final String PLACEHOLDER_2 = "<2>";
        final String PLACEHOLDER_3 = "<3>";
        final String PLACEHOLDER_4 = "<4>";

        String methodName = line.substring(0, line.indexOf('(')).trim();

        if (!symbolTable.methodExists(methodName)) {
            throw new ValidationException(METHOD_NOT_EXISTS.replace(PLACEHOLDER_1, methodName));
        }

        String argsSection = line.substring(line.indexOf(START_BRACKET) + 1,
                line.indexOf(END_BRACKET)).trim();
        ArrayList<String[]> arguments = parseArguments(argsSection);
        ArrayList<String[]> parameters = symbolTable.getMethodParameters(methodName);

        if (parameters.size() != arguments.size()) {
            throw new ValidationException(METHOD_ARGS
                    .replace(PLACEHOLDER_1, methodName)
                    .replace(PLACEHOLDER_2, String.valueOf(parameters.size()))
                    .replace(PLACEHOLDER_3, String.valueOf(arguments.size())));
        }

        for (int i = 0; i < parameters.size(); i++) {
            String expectedType = parameters.get(i)[0];
            String argumentType = arguments.get(i)[0];

            if (!expectedType.equals(argumentType)) {
                throw new ValidationException(ARGUMENT_INCOMPATIBLE
                        .replace(PLACEHOLDER_1, arguments.get(i)[1])
                        .replace(PLACEHOLDER_2, argumentType)
                        .replace(PLACEHOLDER_3, expectedType)
                        .replace(PLACEHOLDER_4, methodName));
            }
        }
    }

    /**
     * Parses method parameters into a list of type-name pairs.
     *
     * @param rawParameters The raw parameters string from a method declaration.
     * @return List of parameter type-name pairs.
     * @throws ValidationException If any parameter is invalid.
     */
    private ArrayList<String[]> parseParameters(String rawParameters) throws ValidationException {
        final String PARAMETER_INVALID = "Parameter '<>' is invalid";
        final String INVALID_PARAM_SYNTAX = "Invalid parameter syntax: <>";
        final String INVALID_PARAM_TYPE = "Invalid parameter type: <>";
        final String INVALID_PARAM_NAME = "Duplicate parameter name: <>";
        final String PLACEHOLDER = "<>";
        final int SPLIT_LIMIT = 3;

        ArrayList<String[]> parametersList = new ArrayList<>();

        if (rawParameters == null || rawParameters.matches(RegexUtils.EMPTY_LINE)) {
            return parametersList;
        }

        String[] parameters = rawParameters.split(PARAM_DELIMITER);

        for (String parameter : parameters) {
            String[] parameterParts = parameter.trim().split(RegexUtils.SPACES, SPLIT_LIMIT);
            if (parameterParts.length == SPLIT_LIMIT) {
                if (!parameterParts[0].equals(RegexUtils.FINAL)) {
                    throw new ValidationException(
                            PARAMETER_INVALID.replace(PLACEHOLDER, parameter));
                }
                parameterParts = new String[]{parameterParts[0] + SPACE +
                        parameterParts[1], parameterParts[2]};
            }
            if (parameterParts.length != 2) {
                throw new ValidationException(INVALID_PARAM_SYNTAX.replace(PLACEHOLDER, parameter));
            }
            if (!RegexUtils.isValidType(parameterParts[0])) {
                throw new ValidationException(
                        INVALID_PARAM_TYPE.replace(PLACEHOLDER, parameterParts[0]));
            }
            for (String[] existingParam : parametersList) {
                if (existingParam[1].equals(parameterParts[1])) {
                    throw new ValidationException(INVALID_PARAM_NAME.
                            replace(PLACEHOLDER, parameterParts[1]));
                }
            }
            parametersList.add(parameterParts);
        }

        return parametersList;
    }

    /**
     * Parses method call arguments into a list of type-value pairs.
     *
     * @param rawArguments The raw arguments string from a method call.
     * @return List of argument type-value pairs.
     * @throws ValidationException If any argument is invalid.
     */
    private ArrayList<String[]> parseArguments(String rawArguments) throws ValidationException {
        final String ARGUMENT_INVALID = "Argument '<>' is of unknown type";
        final String PLACEHOLDER = "<>";
        final int OUTSIDE_SCOPE = -1;

        ArrayList<String[]> arguments = new ArrayList<>();
        if (rawArguments.isEmpty()) {
            return arguments;
        }

        String[] args = rawArguments.split(PARAM_DELIMITER);

        for (String arg : args) {
            arg = arg.trim();
            int argScope = symbolTable.findVariableScope(arg);
            String type = (argScope != OUTSIDE_SCOPE) ?
                    symbolTable.getVariableType(argScope, arg) : RegexUtils.getLiteralType(arg);

            if (type.isEmpty()) {
                throw new ValidationException(ARGUMENT_INVALID.replace(PLACEHOLDER, arg));
            }

            arguments.add(new String[]{type, arg});
        }

        return arguments;
    }
}
