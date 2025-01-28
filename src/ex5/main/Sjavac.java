package ex5.main;

import ex5.exceptions.FileException;
import ex5.exceptions.ValidationException;
import ex5.parsing.FileProcessor;
import ex5.parsing.RegexUtils;
import ex5.validation.*;

import java.io.FileNotFoundException;

/**
 * Main class for compiling a custom language. Reads a file, performs syntax and semantic validation.
 * @author Tomer Zilberman
 */
public class Sjavac {
    public static final int LEGAL_CODE = 0;
    public static final int INVALID_CODE = 1;
    public static final int IO_ERROR = 2;
    public final FileProcessor fileProcessor;
    private final SymbolTable symbolTable;
    private static final String UNMATCHED_BRACES_ERROR = "Unmatched opening/closing braces.";

    /**
     * Initializes the compiler with the given file.
     *
     * @param fileName The name of the file to compile.
     */
    public Sjavac(String fileName) {
        try {
            this.fileProcessor = new FileProcessor(fileName);
        } catch (FileException e) {
            throw new RuntimeException(e);
        }
        symbolTable = new SymbolTable();
    }

    /**
     * Performs an initial sweep of the file to validate the syntax and braces.
     *
     * @return LEGAL_CODE if valid, INVALID_CODE if errors found.
     */
    public int initialSweep() {
        String line;
        ValidatorFactory factory = new ValidatorFactory(symbolTable);

        while ((line = fileProcessor.readLine()) != null) {
            line = line.trim();
            if (RegexUtils.isCommentOrEmpty(line)) continue;
            try {
                Validator validator = factory.getValidatorForSweep(line);
                if (validator != null) validator.validate(line);
            } catch (ValidationException e) {
                return INVALID_CODE;
            }
        }

        if (symbolTable.getScope() != 0) {
            System.err.println(UNMATCHED_BRACES_ERROR);
            return INVALID_CODE;
        }
        return LEGAL_CODE;
    }

    /**
     * Compiles the file and checks for validation errors.
     *
     * @return LEGAL_CODE if valid, INVALID_CODE if errors found.
     */
    public int compile() {
        String line;
        ValidatorFactory factory = new ValidatorFactory(symbolTable);

        while ((line = fileProcessor.readLine()) != null) {
            line = line.trim();
            if (!RegexUtils.isCommentOrEmpty(line)) {
                try {
                    Validator validator = factory.getValidator(line);
                    if (validator != null) validator.validate(line);
                } catch (ValidationException e) {
                    return INVALID_CODE;
                }
            }
        }

        if (symbolTable.getScope() != 0) return INVALID_CODE;
        return LEGAL_CODE;
    }

    /**
     * Main method to run the compiler with the given file.
     *
     * @param args Command-line arguments; the first argument is the file name.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public static void main(String[] args) throws FileNotFoundException {
        String fileName = args[0];
        Sjavac compiler = new Sjavac(fileName);
        System.out.println(compiler.initialSweep());
        compiler.fileProcessor.reset();
        System.out.println(compiler.compile());
    }
}
