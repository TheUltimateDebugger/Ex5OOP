package ex5.main;

import ex5.exceptions.FileException;
import ex5.exceptions.ValidationException;
import ex5.parsing.Parser;
import ex5.parsing.RegexUtils;
import ex5.validation.ConditionValidator;
import ex5.validation.MethodValidator;
import ex5.validation.SymbolTable;
import ex5.validation.VariableValidator;

public class Sjavac {
    public static final int LEGAL_CODE = 0;
    public static final int INVALID_CODE = 1;
    public static final int IO_ERROR = 2;
    private Parser parser;
    private SymbolTable symbolTable;

    public Sjavac(String fileName) {
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
                    MethodValidator methodValidator = new MethodValidator(symbolTable);
                    try {
                        methodValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.CONDITION)) {
                    scope++;
                    ConditionValidator conditionValidator = new ConditionValidator(symbolTable);
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
        Sjavac compiler = new Sjavac(fileName);
        compiler.compile();
    }
}
