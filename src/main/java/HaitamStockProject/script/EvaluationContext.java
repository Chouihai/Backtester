package HaitamStockProject.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationContext {

    private final Map<String, Object> variables = new HashMap<>();
    private final List<String> log = new ArrayList<>();

    public Object get(String name) {
        return variables.get(name);
    }

    public void set(String name, Object value) {
        variables.put(name, value);
    }

    public void log(String entry) {
        log.add(entry);
    }

    public List<String> getLog() {
        return log;
    }
}
