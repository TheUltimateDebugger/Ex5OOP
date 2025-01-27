package ex5.parsing;

import ex5.exceptions.FileException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class FileProcessor {
    private InputStream inputStream;
    private Scanner scanner;
    private String filename;

    public FileProcessor(String fileName) throws FileException {
        this.filename = fileName;
        try {
            inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            throw new FileException("File not found");
        }
        scanner = new Scanner(inputStream);
    }

    public String readLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return null;
    }

    public void reset() throws FileNotFoundException {
        scanner = new Scanner(new FileInputStream(filename));
    }
}
