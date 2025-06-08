package HaitamStockProject.script;

import HaitamStockProject.script.functions.ScriptFunction;
import HaitamStockProject.script.functions.ScriptFunctionRegistry;
import HaitamStockProject.script.statements.*;
import HaitamStockProject.script.tokens.Parser;
import HaitamStockProject.script.tokens.TokenType;

import java.util.*;

public class ScriptEvaluator {

    private final ScriptFunctionRegistry registry;
    private final List<Statement> statements;
    private final Map<String, Object> variables = new HashMap<>();
    private EvaluationContext currentContext;

    public ScriptEvaluator(String script, ScriptFunctionRegistry registry) {
        Parser parser = new Parser(script);
        this.statements = parser.parse();
        this.registry = registry;
    }

    public void evaluate(EvaluationContext context) {
        this.currentContext = context;
        for (Statement stmt : statements) {
            evaluate(stmt);
        }
    }

    private void evaluate(Statement stmt) {
        if (stmt instanceof ExpressionStatement) {
            evaluate(((ExpressionStatement) stmt).expression);
        } else if (stmt instanceof IfStatement ifStmt) {
            Object condition = evaluate(ifStmt.condition);
            if (isTruthy(condition)) {
                for (Statement bodyStmt : ifStmt.body) {
                    evaluate(bodyStmt);
                }
            }
        } else if (stmt instanceof VariableDeclaration assign) {
            Object value = evaluate(assign.initializer);
            variables.put(assign.name, value);
        }
        throw new RuntimeException("Unknown statement type: " + stmt.getClass());
    }

    private Object evaluate(Expression expr) {
        if (expr instanceof Literal lit) {
            return lit.value;
        } else if (expr instanceof Identifier var) {
            return variables.get(var.name);
        } else if (expr instanceof BinaryExpression bin) {
            Object left = evaluate(bin.left);
            Object right = evaluate(bin.right);
            return applyBinary(bin.operator.type, left, right);
        } else if (expr instanceof FunctionCall call) {
            return handleFunctionCall(call);
        } else if (expr instanceof UnaryExpression unaryExpression) {
            return null; // TODO: handle this
        }
        throw new RuntimeException("Unknown expression type: " + expr.getClass());
    }

    private Object handleFunctionCall(FunctionCall call) {
        String name = call.functionName;
        List<Object> args = call.arguments.stream().map(this::evaluate).toList();
        ScriptFunction function = registry.get(name);
        function.execute(args, this.currentContext);
        throw new RuntimeException("Unknown function: " + name);
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        return true;
    }

    private Object applyBinary(TokenType op, Object left, Object right) {
        double l = ((Number) left).doubleValue();
        double r = ((Number) right).doubleValue();
        return switch (op) {
            case GREATER -> l > r;
            case LESS -> l < r;
            case GREATER_EQUAL -> l >= r;
            case LESS_EQUAL -> l <= r;
            case EQUAL_EQUAL -> Objects.equals(left, right);
            case BANG_EQUAL -> !Objects.equals(left, right);
            case PLUS -> l + r;
            case MINUS -> l - r;
            case STAR -> l * r;
            case SLASH -> l / r;
            default -> throw new RuntimeException("Unsupported operator: " + op);
        };
    }
}
