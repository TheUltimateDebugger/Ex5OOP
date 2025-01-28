package ex5.validation;

import ex5.exceptions.ValidationException;
import ex5.parsing.RegexUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a symbol table for managing variables and methods across multiple scopes.
 * It supports global and local variables, as well as methods with parameters.
 * @author Tomer Zilberman
 */
public class SymbolTable {

    /**
     * Represents a variable with its attributes such as name, type, initialization state, etc.
     */
    private static class Variable {
        /** Name of the variable. */
        public String name;

        /** Data type of the variable (e.g., int, String). */
        public String type;

        /** Whether the variable is initialized. */
        public boolean isInitialized;

        /** Whether the variable is final (immutable after initialization). */
        public boolean isFinal;

        /** Whether the variable is an uninitialized global variable. */
        public boolean isUninitializedGlobal;

        /**
         * Constructs a Variable object.
         *
         * @param name The name of the variable.
         * @param type The data type of the variable.
         * @param isInitialized Whether the variable is initialized.
         * @param isFinal Whether the variable is final.
         * @param isUninitializedGlobal Whether the variable is an uninitialized global.
         */
        public Variable(String name, String type, boolean isInitialized,
                        boolean isFinal, boolean isUninitializedGlobal) {
            this.name = name;
            this.type = type;
            this.isInitialized = isInitialized;
            this.isFinal = isFinal;
            this.isUninitializedGlobal = isUninitializedGlobal;
        }
    }

    /** A list of scopes, each represented as a map of variable names to Variable objects. */
    private ArrayList<HashMap<String, Variable>> scopes;

    /** A map of method names to their parameter details. */
    private HashMap<String, ArrayList<String[]>> methods;

    /**
     * Constructs a SymbolTable with an initial global scope.
     */
    public SymbolTable() {
        this.scopes = new ArrayList<>();
        scopes.add(new HashMap<>()); // Add global scope
        this.methods = new HashMap<>();
    }

    /**
     * Adds a global variable to the symbol table.
     *
     * @param name The name of the variable.
     * @param type The data type of the variable.
     * @param isInitialized Whether the variable is initialized.
     * @param isFinal Whether the variable is final.
     */
    public void addGlobalVariable(String name, String type,
                                  boolean isInitialized, boolean isFinal) {
        scopes.get(0).put(name, new Variable(name, type, isInitialized, isFinal, true));
    }

    /**
     * Adds a local variable to the current scope.
     *
     * @param name The name of the variable.
     * @param type The data type of the variable.
     * @param isFinal Whether the variable is final.
     * @param isInitialized Whether the variable is initialized.
     */
    public void addLocalVariable(String name, String type, boolean isFinal, boolean isInitialized) {
        scopes.get(scopes.size() - 1).put(name, new Variable(name, type,
                isInitialized, isFinal, true));
    }

    /**
     * Marks a variable as initialized in a given scope.
     *
     * @param scope The scope index where the variable is defined.
     * @param name  The name of the variable.
     * @throws ValidationException If the variable does not exist.
     */
    public void initializeVariable(int scope, String name) throws ValidationException {
        final String NOT_EXISTING_NAME = "Variable <> does not exist";
        final String PLACEHOLDER = "<>";
        if (variableExists(scope, name)) {
            scopes.get(scope).get(name).isInitialized = true;
            if (getScope() == 0) {
                scopes.get(scope).get(name).isUninitializedGlobal = false;
            }
        } else {
            throw new ValidationException(NOT_EXISTING_NAME.replace(PLACEHOLDER, name));
        }
    }

    /**
     * Resets the global variables to their initialization state.
     */
    public void resetGlobalsToGlobalInitializationState() {
        for (Variable var : scopes.get(0).values()) {
            var.isInitialized = !var.isUninitializedGlobal;
        }
    }

    /**
     * Finds the scope index where a variable is defined.
     *
     * @param name The name of the variable.
     * @return The scope index, or -1 if not found.
     */
    public int findVariableScope(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (variableExists(i, name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if a variable exists in a given scope.
     *
     * @param scope The scope index.
     * @param name  The name of the variable.
     * @return True if the variable exists, false otherwise.
     */
    public boolean variableExists(int scope, String name) {
        return scopes.get(scope).containsKey(name);
    }

    /**
     * Retrieves the type of a variable in a given scope.
     *
     * @param scope The scope index.
     * @param name  The name of the variable.
     * @return The type of the variable.
     */
    public String getVariableType(int scope, String name) {
        return scopes.get(scope).get(name).type;
    }

    /**
     * Checks if a variable is initialized in a given scope.
     *
     * @param scope The scope index.
     * @param name  The name of the variable.
     * @return True if the variable is initialized, false otherwise.
     */
    public boolean isVariableInitialized(int scope, String name) {
        return scopes.get(scope).get(name).isInitialized;
    }

    /**
     * Checks if a variable is final in a given scope.
     *
     * @param scope The scope index.
     * @param name  The name of the variable.
     * @return True if the variable is final, false otherwise.
     */
    public boolean isVariableFinal(int scope, String name) {
        return scopes.get(scope).get(name).isFinal;
    }

    /**
     * Adds a method to the symbol table.
     *
     * @param name       The name of the method.
     * @param parameters The parameters of the method, represented as a list of string arrays.
     */
    public void addMethod(String name, ArrayList<String[]> parameters) {
        methods.put(name, parameters);
        enterScope();
    }

    /**
     * Adds method parameters to the current scope.
     *
     * @param name The name of the method whose parameters are to be added.
     */
    public void addMethodParams(String name) {
        final int FIRST_PARAM = 0, SECOND_PARAM = 1;
        enterScope();
        ArrayList<String[]> parameters = methods.get(name);
        for (String[] parameter : parameters) {
            boolean isFinal = false;
            if (parameter[FIRST_PARAM].startsWith(RegexUtils.FINAL)) {
                isFinal = true;
                parameter[FIRST_PARAM] = parameter[FIRST_PARAM].substring(
                        RegexUtils.FINAL.length()).trim();
            }
            scopes.get(scopes.size() - 1).put(parameter[SECOND_PARAM],
                    new Variable(parameter[SECOND_PARAM], parameter[FIRST_PARAM],
                            true, isFinal, true));
        }
    }

    /**
     * Checks if a method exists in the symbol table.
     *
     * @param name The name of the method.
     * @return True if the method exists, false otherwise.
     */
    public boolean methodExists(String name) {
        return methods.containsKey(name);
    }

    /**
     * Retrieves the parameters of a method.
     *
     * @param name The name of the method.
     * @return A list of parameters represented as string arrays.
     */
    public ArrayList<String[]> getMethodParameters(String name) {
        return methods.get(name);
    }

    /**
     * Enters a new scope by adding a new map to the scopes list.
     */
    public void enterScope() {
        scopes.add(new HashMap<>());
    }

    /**
     * Exits the current scope by removing the last map from the scopes list.
     *
     * @throws ValidationException If there are mismatched braces
     * (i.e., trying to exit the global scope).
     */
    public void exitScope() throws ValidationException {
        final String MISMATCH_BRACES = "Mismatching opening and closing braces";
        final int WRONG_SIZE = 1;
        if (scopes.size() == WRONG_SIZE) {
            throw new ValidationException(MISMATCH_BRACES);
        }
        scopes.get(scopes.size() - 1).clear();
        scopes.remove(scopes.size() - 1);
    }

    /**
     * Retrieves the current scope index.
     *
     * @return The index of the current scope.
     */
    public int getScope() {
        return scopes.size() - 1;
    }
}
