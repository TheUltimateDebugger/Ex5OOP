package ex5.main;

import ex5.exceptions.FileException;
import ex5.exceptions.ValidationException;
import ex5.parsing.Parser;
import ex5.parsing.RegexUtils;
import ex5.validation.*;

import java.io.FileNotFoundException;

public class Sjavac {
    public static final int LEGAL_CODE = 0;
    public static final int INVALID_CODE = 1;
    public static final int IO_ERROR = 2;
    // TODO change back to private, this was only for testing
    public final Parser parser;
    private final SymbolTable symbolTable;

    public Sjavac(String fileName) {
        try {
            this.parser = new Parser(fileName);
        } catch (FileException e) {
            throw new RuntimeException(e);
        }
        symbolTable = new SymbolTable();
    }

    public int initialSweep() {
        String line;
        ValidatorFactory factory = new ValidatorFactory(symbolTable);

        while ((line = parser.readLine()) != null) {
            line = line.trim();
            if (RegexUtils.isCommentOrEmpty(line)) { continue; }
            try {
                Validator validator = factory.getValidatorForSweep(line);
                if (validator == null) {
                    continue;
                }
                validator.validate(line);
            }
            catch (ValidationException e) { return INVALID_CODE; }
        }

        if (symbolTable.getScope() != 0) {
            System.err.println("Unmatched opening/closing braces.");
            return INVALID_CODE;
        }
        return LEGAL_CODE;
    }

    public int compile() {
        String line;
        ValidatorFactory factory = new ValidatorFactory(symbolTable);
        while ((line = parser.readLine()) != null) {
            line = line.trim();
            if (!RegexUtils.isCommentOrEmpty(line)) {
                try {
                    Validator validator = factory.getValidator(line);
                    if (validator == null) {
                        continue;
                    }
                    validator.validate(line);
                }
                catch (ValidationException e) { return INVALID_CODE; }
            }
        }
        if (symbolTable.getScope() != 0) {
            return INVALID_CODE;
        }
        return LEGAL_CODE;
    }

    public static void main(String[] args) throws FileNotFoundException, ValidationException {
        String fileName = args[0];
        Sjavac compiler = new Sjavac(fileName);
        System.out.println(compiler.initialSweep());
        System.out.println("first pass completed");
        compiler.parser.reset();
        System.out.println(compiler.compile());
    }
}
