package ex5.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class with regex patterns and helper methods for validating and parsing s-Java code.
 * The class assumes that it deals with lines that has been trimmed.
 * @author Tomer Zilberman
 */
public class RegexUtils {

    /** Valid variable name */
    public static final String VARIABLE_NAME = "[a-zA-Z_][a-zA-Z0-9_]*";

    /** Invalid variable name*/
    public static final String ILLEGAL_VARIABLE_NAME = "(.*__+.|_)";

    /** Invalid method name */
    public static final String ILLEGAL_METHOD_NAME = "(.*__+.|_.*)";

    /** Valid method name */
    public static final String METHOD_NAME = "[a-zA-Z_][a-zA-Z0-9_]*";

    /** Valid primitive types in s-Java: int, double, boolean, String, and char. */
    public static final String PRIMITIVE_TYPE = "(int|double|boolean|String|char)";

    /** Single-line comment: starts with "//". */
    public static final String SINGLE_LINE_COMMENT = "//.*";

    /** Empty line: contains only spaces or tabs. */
    public static final String EMPTY_LINE = "\\s*";

    /** Line end: optional spaces followed by a semicolon. */
    public static final String END_LINE = "\\s*;$";

    /** Method declaration with no option to add anything */
    public static final String METHOD_DECLARATION_ONLY = "^void\\s+" + METHOD_NAME + "\\s*\\(.*\\)\\s*\\{$";

    /** Integer literal with no place to add anything: allows optional negative signs and digits. */
    public static final String INTEGER_ONLY = "^-?\\d+$";

    /** Double literal with no place to add anything: matches floating-point numbers with optional signs. */
    public static final String DOUBLE_ONLY = "(-|\\+)?(\\d*\\.?\\d+|\\d+\\.?\\d*)";

    /** Boolean literal with no place to add anything: matches "true" or "false". */
    public static final String BOOLEAN_ONLY = "^(true|false)$";

    /** String literal with no place to add anything: text enclosed in double quotes. */
    public static final String STRING_ONLY = "^\".*\"$";

    /** Char literal with no place to add anything: single character enclosed in single quotes. */
    public static final String CHAR_ONLY = "^'.'$";

    /** Variable values: valid literals, variable names, or combinations. */
    public static final String VARIABLE_VALUES =
            "(true|false|\".*\"|'.'|(-|\\+)?\\d*\\.?\\d+|(-|\\+)?\\d+\\.?\\d*|" + VARIABLE_NAME + ")";

    /** Method call: matches method name followed by parentheses and optional arguments. */
    public static final String METHOD_CALL_ONLY = "^" + METHOD_NAME + "\\s*\\(.*\\)\\s*;$";

    /** Return statement: matches the keyword 'return' followed by a semicolon. */
    public static final String RETURN_STATEMENT = "^return" + END_LINE;

    /** Variable declaration: optional 'final', a primitive type,
     * and variable names with optional initialization. */
    public static final String VARIABLE_DECLARATION = "^(final\\s*)?" + PRIMITIVE_TYPE + "\\s+" +
            "(" + VARIABLE_NAME + "(\\s*=\\s*" + VARIABLE_VALUES + ")?\\s*,\\s*)*" + VARIABLE_NAME +
            "(\\s*=\\s*" + VARIABLE_VALUES + ")?" + END_LINE;

    /** Variable value assignment: matches variable names followed by an equals sign and value. */
    public static final String VARIABLE_VALUE_CHANGE = "^" + VARIABLE_NAME +
            "\\s*=\\s*" + VARIABLE_VALUES + "\\s*;";

    /** Closing scope: matches a single closing curly brace. */
    public static final String CLOSING_SCOPE = "^}$";

    /** Condition splitters: logical operators '&&' and '||'. */
    public static final String CONDITION_SPLITTERS = "(&&|\\|\\|)";

    /** If/while block: matches 'if' or 'while' followed by parentheses and an opening curly brace. */
    public static final String IF_WHILE_BLOCK = "^(if|while)\\s*\\(.+\\)\\s*\\{$";

    /** The keyword for an Integer */
    public static final String INTEGER = "int";

    /** The keyword for a Double */
    public static final String DOUBLE = "double";

    /** The keyword for a Boolean */
    public static final String BOOLEAN = "boolean";

    /** The keyword for a String */
    public static final String STRING = "string";

    /** The keyword for a char */
    public static final String CHAR = "char";

    /** The keyword for a final */
    public static final String FINAL = "final";

    /**
     * Compiles a regex into a Pattern.
     *
     * @param regex Regex to compile.
     * @return Compiled Pattern object.
     */
    public static Pattern compilePattern(String regex) {
        return Pattern.compile(regex);
    }

    /**
     * Checks if a string matches a regex.
     *
     * @param input Input string.
     * @param regex Regex pattern.
     * @return True if matches, false otherwise.
     */
    public static boolean matches(String input, String regex) {
        Pattern pattern = compilePattern(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * Checks if a line is a comment or empty.
     *
     * @param line Line to check.
     * @return True if comment or empty, false otherwise.
     */
    public static boolean isCommentOrEmpty(String line) {
        return matches(line, SINGLE_LINE_COMMENT) || matches(line, EMPTY_LINE);
    }

    /**
     * Determines the type of a literal argument.
     *
     * @param argument Input argument.
     * @return Type of the literal (int, double, boolean, String, char), or empty if invalid.
     */
    public static String getLiteralType(String argument) {
        if (argument.matches(INTEGER_ONLY)) return INTEGER;
        if (argument.matches(DOUBLE_ONLY)) return DOUBLE;
        if (argument.matches(BOOLEAN_ONLY)) return BOOLEAN;
        if (argument.matches(STRING_ONLY)) return STRING;
        if (argument.matches(CHAR_ONLY)) return CHAR;
        return "";
    }

    /**
     * Validates if a type is a supported s-Java type.
     *
     * @param type Type to validate.
     * @return True if valid, false otherwise.
     */
    public static boolean isValidType(String type) {
        if (type.contains(FINAL)) type = type.replace(FINAL, "");
        type = type.trim();
        return type.equals(INTEGER) || type.equals(DOUBLE) || type.equals(BOOLEAN) ||
                type.equals(CHAR) || type.equals(STRING);
    }
}
