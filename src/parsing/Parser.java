package parsing;

import exceptions.FileException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class Parser {
    private InputStream inputStream;
    Scanner scanner;

    public Parser(String fileName) throws FileException {
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
}
