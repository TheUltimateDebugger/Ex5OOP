package ex5.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that provides predefined regular expressions and helper methods
 * for validating and ex5.parsing s-Java code elements.
 */
public class RegexUtils {

    // Predefined regex patterns for s-Java constructs

    /** Regex for a valid variable name
     * (e.g., starts with a letter, followed by letters, digits, or underscores). */
    public static final String VARIABLE_NAME = "[a-zA-Z_][a-zA-Z0-9_]*";

    /** Regex expression for illegal variable names */
    public static final String ILLEGAL_VARIABLE_NAME = "(.*__+.|_)";


    public static final String ILLEGAL_METHOD_NAME = "(.*__+.|_.*)";


    /** Regex for a valid method name (similar to variable name but may follow stricter conventions). */
    public static final String METHOD_NAME = "[a-zA-Z_][a-zA-Z0-9_]*";

    /** Regex for a primitive type declaration (e.g., int, double, boolean, String). */
    public static final String PRIMITIVE_TYPE = "(int|double|boolean|String|char)";

    /** Regex for a single-line comment (e.g., // this is a comment). */
    public static final String SINGLE_LINE_COMMENT = "//.*";

    /** Regex for an empty line (only spaces or tabs). */
    public static final String EMPTY_LINE = "\\s*";

    public static final String END_LINE = "\\s*;$";


    /** Regex for a method declaration. */
    public static final String METHOD_DECLARATION_ONLY = "^void" +
            "\\s+" + METHOD_NAME + "\\s*\\(.*\\)\\s*\\{$";

    public static final String INTEGER_ONLY = "^-?\\d+$";

    public static final String DOUBLE_ONLY = "(-|\\+)?(\\d*\\.?\\d+|\\d+\\.?\\d*)";

    public static final String BOOLEAN_ONLY = "^(true|false)$";

    public static final String STRING_ONLY = "^\".*\"$";

    public static final String CHAR_ONLY = "^'.'$";

    public static final String VARIABLE_VALUES =
            "(true|false|\".*\"|'.'|(-|\\+)?\\d*\\.?\\d+|(-|\\+)?\\d+\\.?\\d*|" + VARIABLE_NAME + ")";

    public static final String METHOD_CALL_ONLY = "^" + METHOD_NAME + "\\s*\\(.*\\)\\s*;$";

    public static final String RETURN_STATEMENT = "^return" + END_LINE;

    /** Regex for a variable declaration. */
    public static final String VARIABLE_DECLARATION = "^(final\\s*)?" + PRIMITIVE_TYPE + "\\s+"
            + "(" + VARIABLE_NAME + "(\\s*=\\s*" + VARIABLE_VALUES + ")?\\s*,\\s*)*" + VARIABLE_NAME +
            "(\\s*=\\s*" + VARIABLE_VALUES + ")?" + END_LINE;


    public static final String VARIABLE_VALUE_CHANGE = "^" + VARIABLE_NAME + "\\s*=\\s*" + VARIABLE_VALUES
            + "\\s*;";

    /** Regex for a closing scope. */
    public static final String CLOSING_SCOPE = "^}$";

    public static final String CONDITION_SPLITTERS = "(&&|\\|\\|)";

    /** Regex for an if or while condition. */
    public static final String IF_WHILE_BLOCK = "^(if|while)\\s*\\(.+\\)\\s*\\{$";

    /**
     * Compiles and returns a Pattern object for the given regex.
     *
     * @param regex The regular expression to compile.
     * @return A Pattern object.
     */
    public static Pattern compilePattern(String regex) {
        return Pattern.compile(regex);
    }

    /**
     * Checks if a given string matches the provided regex pattern.
     *
     * @param input The string to test.
     * @param regex The regex pattern to match against.
     * @return True if the string matches the pattern, false otherwise.
     */
    public static boolean matches(String input, String regex) {
        Pattern pattern = compilePattern(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * Extracts the first match of the given regex in the input string.
     *
     * @param input The string to search.
     * @param regex The regex pattern to search for.
     * @return The first match if found, or null if no match exists.
     */
    public static String extractFirstMatch(String input, String regex) {
        Pattern pattern = compilePattern(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Extracts all matches of the given regex in the input string.
     *
     * @param input The string to search.
     * @param regex The regex pattern to search for.
     * @return An array of all matches, or an empty array if no matches are found.
     */
    public static String[] extractAllMatches(String input, String regex) {
        Pattern pattern = compilePattern(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder matches = new StringBuilder();

        while (matcher.find()) {
            matches.append(matcher.group()).append("\n");
        }

        return matches.toString().split("\n");
    }

    /**
     * Checks if a line is a comment or empty line.
     *
     * @param line The line to check.
     * @return True if the line is a comment or empty, false otherwise.
     */
    public static boolean isCommentOrEmpty(String line) {
        return matches(line, SINGLE_LINE_COMMENT) || matches(line, EMPTY_LINE);
    }

}