package ex5.parsing;

import ex5.exceptions.FileException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * The FileProcessor class provides functionality to process and read lines from a file.
 * It uses a Scanner for reading the file line by line and allows resetting the file reading process
 * @author Tomer Zilberman
 */
public class FileProcessor {
    private InputStream inputStream; // The input stream for the file
    private Scanner scanner;        // Scanner object used for reading the file
    private String filename;        // Name of the file being processed

    /**
     * Constructs a FileProcessor for the specified file.
     *
     * @param fileName the name of the file to process
     * @throws FileException if the file cannot be found
     */
    public FileProcessor(String fileName) throws FileException {
        final String FILE_ERROR_MESSAGE = "File: " + fileName + " did not open";
        this.filename = fileName;
        try {
            inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            throw new FileException(FILE_ERROR_MESSAGE);
        }
        scanner = new Scanner(inputStream); // Initialize the Scanner with the input stream.
    }

    /**
     * Reads the next line from the file.
     *
     * @return the next line from the file, or null if no more lines are available
     */
    public String readLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine(); // Return the next line if available.
        }
        return null; // Return null if no more lines are present.
    }

    /**
     * Resets the file reading process by reinitializing the Scanner object.
     *
     * @throws FileNotFoundException if the file cannot be reopened
     */
    public void reset() throws FileNotFoundException {
        scanner = new Scanner(new FileInputStream(filename));
    }
}