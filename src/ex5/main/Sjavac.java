package ex5.main;

import ex5.exceptions.FileException;
import ex5.exceptions.SJavaException;
import ex5.exceptions.ValidationException;
import ex5.parsing.FileProcessor;
import ex5.parsing.RegexUtils;
import ex5.validation.*;

import java.io.FileNotFoundException;

public class Sjavac {
    public static final int LEGAL_CODE = 0;
    public static final int INVALID_CODE = 1;
    public static final int IO_ERROR = 2;
    public final FileProcessor fileProcessor;
    private final SymbolTable symbolTable;
    private static final String UNMATCHED_BRACES_ERROR = "Unmatched opening/closing braces.";

    public Sjavac(String fileName) {
        try {
            this.fileProcessor = new FileProcessor(fileName);
        } catch (FileException e) {
            throw new RuntimeException(e);
        }
        symbolTable = new SymbolTable();
    }

    public int initialSweep() {
        String line;
        ValidatorFactory factory = new ValidatorFactory(symbolTable);

        while ((line = fileProcessor.readLine()) != null) {
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
            //TODO: shouldn't be exception?
            System.err.println(UNMATCHED_BRACES_ERROR);
            return INVALID_CODE;
        }
        return LEGAL_CODE;
    }

    public int compile() {
        String line;
        ValidatorFactory factory = new ValidatorFactory(symbolTable);
        while ((line = fileProcessor.readLine()) != null) {
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
        final int FILE_INDEX = 0;
        String fileName = args[FILE_INDEX];
        Sjavac compiler = new Sjavac(fileName);
        System.out.println(compiler.initialSweep());
        compiler.fileProcessor.reset();
        System.out.println(compiler.compile());
    }
}
