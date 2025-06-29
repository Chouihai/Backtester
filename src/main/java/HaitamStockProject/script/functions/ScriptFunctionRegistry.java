package HaitamStockProject.script.functions;

import java.util.HashMap;
import java.util.Map;

public class ScriptFunctionRegistry {

    private final Map<String, ScriptFunction> functions = new HashMap<>();

    public void register(String name, ScriptFunction fn) {
        functions.put(name, fn);
    }

    public ScriptFunction get(String name) {
        return functions.get(name);
    }

    public boolean contains(String name) {
        return functions.containsKey(name);
    }

    public int size() {
        return functions.size();
    }
}
