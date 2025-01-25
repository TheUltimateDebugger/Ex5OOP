package parsing;

import exceptions.FileException;
import exceptions.ValidationException;
import validation.ConditionValidator;
import validation.MethodValidator;
import validation.SymbolTable;
import validation.VariableValidator;

public class SJavaCompiler {
    public static final int LEGAL_CODE = 0;
    public static final int INVALID_CODE = 1;
    public static final int IO_ERROR = 2;
    private Parser parser;
    private SymbolTable symbolTable;

    public SJavaCompiler(String fileName) {
        try {
            this.parser = new Parser(fileName);
        } catch (FileException e) {
            throw new RuntimeException(e);
        }
        symbolTable = new SymbolTable();
    }

    public int compile() {
        String line;
        int scope = 0;
        while ((line = parser.readLine()) != null) {
            System.out.println("the line: " + line);
            if (!RegexUtils.isCommentOrEmpty(line)) {
                if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
                    scope--;
                }
                else if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
                    scope++;
                    VariableValidator variableValidator = new VariableValidator(symbolTable);
                    try {
                        variableValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION)) {
                    scope++;
                    MethodValidator methodValidator = new MethodValidator();
                    try {
                        methodValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.CONDITION)) {
                    scope++;
                    ConditionValidator conditionValidator = new ConditionValidator();
                    try {
                        conditionValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                }
                else {
                    return INVALID_CODE;
                }
            }
        }
        if (scope != 0) {
            return INVALID_CODE;
        }
        return LEGAL_CODE;
    }

    public static void main(String[] args) {
        String fileName = args[0];
        SJavaCompiler compiler = new SJavaCompiler(fileName);
        compiler.compile();
    }
}
