package ex5.validation;

import ex5.exceptions.ValidationException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private static class Variable {
        String name;
        String type;
        boolean isInitialized;
        boolean isFinal;

        Variable(String name, String type, boolean isInitialized, boolean isFinal) {
            this.name = name;
            this.type = type;
            this.isInitialized = isInitialized;
            this.isFinal = isFinal;
        }
    }

    private ArrayList<HashMap<String, Variable>> scopes;
    private HashMap<String, ArrayList<String[]>> methods;

    public SymbolTable() {
        this.scopes = new ArrayList<>();
        scopes.add(new HashMap<>());
        this.methods = new HashMap<>();
    }

    public void addGlobalVariable(String name, String type, boolean isInitialized, boolean isFinal) {
        scopes.get(0).put(name, new Variable(name, type, isInitialized, isFinal));
    }

    public void addLocalVariable(String name, String type,
                          boolean isFinal, boolean isInitialized) {
        scopes.get(scopes.size()-1).put(name, new Variable(name, type, isInitialized, isFinal));
    }

    public void initializeVariable(int scope, String name) throws ValidationException {
        if (variableExists(scope, name)) {
            scopes.get(scope).get(name).isInitialized = true;
        }
        else {
            throw new ValidationException("Variable " + name + " is not initialized");
        }
    }

    public int findVariableScope(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (variableExists(i, name)) {
                return i;
            }
        }
        return -1;
    }

    public boolean variableExists(int scope, String name) {
        return scopes.get(scope).containsKey(name);
    }

    public String getVariableType(int scope, String name) {
        return scopes.get(scope).get(name).type;
    }

    public boolean isVariableInitialized(int scope, String name) {
        return scopes.get(scope).get(name).isInitialized;
    }

    public boolean isVariableFinal(int scope, String name) {
        return scopes.get(scope).get(name).isFinal;
    }

    public void addMethod(String name, ArrayList<String[]> parameters) {
        methods.put(name, parameters);
        enterScope();
    }

    public void addMethodParams(String name) {
        enterScope();
        ArrayList<String[]> parameters = methods.get(name);
        for (String[] parameter : parameters) {
            boolean isFinal = false;
            if (parameter[0].startsWith("final")) {
                isFinal = true;
                parameter[0] = parameter[0].substring("final".length()).trim();
            }
            scopes.get(scopes.size() - 1).put(parameter[1], new Variable(parameter[1],
                    parameter[0], true, isFinal));
        }
    }

    public boolean methodExists(String name) {
        return methods.containsKey(name);
    }

    public ArrayList<String[]> getMethodParameters(String name) {
        return methods.get(name);
    }

    public void enterScope() {
        scopes.add(new HashMap<>());
    }

    public void exitScope() throws ValidationException {
        if (scopes.size() == 1) {
            throw new ValidationException("Mismatching opening and closing braces");
        }
        scopes.get(scopes.size()-1).clear();
        scopes.remove(scopes.size()-1);
    }

    public int getScope() {
        return scopes.size() - 1;
    }
}