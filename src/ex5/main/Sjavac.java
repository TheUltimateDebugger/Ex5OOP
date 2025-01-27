package ex5.main;

import ex5.exceptions.FileException;
import ex5.exceptions.ValidationException;
import ex5.parsing.Parser;
import ex5.parsing.RegexUtils;
import ex5.validation.ConditionValidator;
import ex5.validation.MethodValidator;
import ex5.validation.SymbolTable;
import ex5.validation.VariableValidator;

import java.io.FileNotFoundException;

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

    public void initialSweep() throws ValidationException {
        String line;
        int scope = 0; // Global scope
        boolean insideMethodBody = false; // Track if we're inside a method body

        VariableValidator variableValidator = new VariableValidator(symbolTable);
        MethodValidator methodValidator = new MethodValidator(symbolTable);

        while ((line = parser.readLine()) != null) {
            line = line.trim();
            if (RegexUtils.isCommentOrEmpty(line)) { continue; }
            if (RegexUtils.matches(line, RegexUtils.IF_WHILE_BLOCK)) {
                scope++;
                continue;
            }
            if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
                scope--;
                if (scope == 0) { insideMethodBody = false; }
                continue;
            }
            if (insideMethodBody) { continue; }
            if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
                variableValidator.validate(line, scope);
            } else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION)) {
                scope++;
                methodValidator.validateMethodDeclaration(line);
                insideMethodBody = true;
            } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_VALUE_CHANGE)) {
                variableValidator.validate(line, scope);
            } else {
                throw new ValidationException("Invalid line in global scope: " + line);
            }

        }

        if (scope != 0) {
            System.err.println("Unmatched opening/closing braces.");
        }

    }

    public int compile() throws ValidationException {
        String line;
        int scope = 0;
        boolean isInMethodBody = false, wasPreviousLineReturn = false;
        while ((line = parser.readLine()) != null) {
            if (!RegexUtils.isCommentOrEmpty(line)) {
                line = line.trim();
                if (RegexUtils.matches(line, RegexUtils.CLOSING_SCOPE)) {
                    scope--;
                    symbolTable.exitScope();
                    if (wasPreviousLineReturn) {
                        isInMethodBody = false;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.VARIABLE_DECLARATION)) {
                    if (scope  == 0) { continue; }
                    System.out.println("variable declaration");
                    VariableValidator variableValidator = new VariableValidator(symbolTable);
                    try {
                        variableValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.METHOD_DECLARATION)) {
                    scope++;
                    if (isInMethodBody) { throw new ValidationException("Cannot declare method inside method"); }
                    isInMethodBody = true;
                    if (scope == 1) { continue; }
                    MethodValidator methodValidator = new MethodValidator(symbolTable);
                    try {
                        methodValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        System.out.println(e.getMessage());
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.IF_WHILE_BLOCK)) {
                    System.out.println("if while block");
                    scope++;
                    ConditionValidator conditionValidator = new ConditionValidator(symbolTable);
                    try {
                        conditionValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        System.out.println(e.getMessage());
                        return INVALID_CODE;
                    }
                } else if (RegexUtils.matches(line, RegexUtils.VARIABLE_VALUE_CHANGE)) {
                    System.out.println("variable value change");
                    VariableValidator variableValidator = new VariableValidator(symbolTable);
                    try {
                        variableValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        System.out.println(e.getMessage());
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.METHOD_CALL_ONLY)) {
                    System.out.println("method call only");
                    MethodValidator methodValidator = new MethodValidator(symbolTable);
                    try {
                        methodValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                }
                else if (RegexUtils.matches(line, RegexUtils.RETURN_STATEMENT)) {
                    System.out.println("return statement");
                    MethodValidator methodValidator = new MethodValidator(symbolTable);
                    try {
                        methodValidator.validate(line, scope);
                    } catch (ValidationException e) {
                        return INVALID_CODE;
                    }
                    wasPreviousLineReturn = true;
                    continue;
                }
                else {
                    throw new ValidationException("Invalid line in global scope: " + line);
                }
                wasPreviousLineReturn = false;
            }
        }
        if (scope != 0) {
            return INVALID_CODE;
        }
        return LEGAL_CODE;
    }

    public static void main(String[] args) throws FileNotFoundException, ValidationException {
        String fileName = args[0];
        Sjavac compiler = new Sjavac(fileName);
        compiler.initialSweep();
        System.out.println("first pass completed");
        compiler.parser.reset();
        System.out.println(compiler.compile());
    }
}
