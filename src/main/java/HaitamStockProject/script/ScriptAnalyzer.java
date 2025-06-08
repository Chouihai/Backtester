package HaitamStockProject.script;

import HaitamStockProject.script.statements.Expression;
import HaitamStockProject.script.statements.ExpressionStatement;
import HaitamStockProject.script.statements.FunctionCall;
import HaitamStockProject.script.statements.Statement;
import HaitamStockProject.script.tokens.Parser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScriptAnalyzer {

    private final List<Statement> statements;

    public ScriptAnalyzer(String script) {
        Parser parser = new Parser(script);
        statements = parser.parse();
    }

    public Set<String> getAllFunctionsUsed() {
        Set<String> result = new HashSet<>();
        for(Statement statement: statements) {
            if (statement instanceof ExpressionStatement) {
                Expression expr = ((ExpressionStatement) statement).expression;
                if (expr instanceof FunctionCall) {
                    result.add(((FunctionCall) expr).functionName);
                }
            }
        }
        return result;
    }
}
