package HaitamStockProject.script;

import HaitamStockProject.backtester.caches.ValueAccumulatorCache;
import HaitamStockProject.objects.CompiledScript;
import HaitamStockProject.objects.valueaccumulator.ValueAccumulator;
import HaitamStockProject.script.functions.*;
import HaitamStockProject.script.functions.result.NonVoidScriptFunctionResult;
import HaitamStockProject.script.functions.result.ScriptFunctionResult;
import HaitamStockProject.script.functions.result.VoidScriptFunctionResult;
import HaitamStockProject.script.statements.*;
import HaitamStockProject.script.statements.expressions.*;
import HaitamStockProject.script.tokens.Parser;
import HaitamStockProject.script.tokens.TokenType;
//import HaitamStockProject.series.SeriesBuilder;

import java.util.*;

public class ScriptEvaluator {

    private final ScriptFunctionRegistry registry;
    private final CompiledScript compiledScript;
    private final Map<String, Literal> variables = new HashMap<>();
    private EvaluationContext currentContext;
    private final ValueAccumulatorCache valueAccumulatorCache; // TODO: add this

    public ScriptEvaluator(String script, ScriptFunctionRegistry registry, ValueAccumulatorCache valueAccumulatorCache) {
        Parser parser = new Parser();
        this.compiledScript = parser.parse(script);
        this.registry = registry;
        this.valueAccumulatorCache = valueAccumulatorCache;
    }

    public ScriptEvaluator(CompiledScript script, ScriptFunctionRegistry registry, ValueAccumulatorCache valueAccumulatorCache) {
        this.compiledScript = script;
        this.registry = registry;
        this.valueAccumulatorCache = valueAccumulatorCache;
    }

    public void evaluate(EvaluationContext context) {
        this.currentContext = context;
        List<Statement> statements = compiledScript.statements();
        for (Statement stmt : statements) {
            evaluate(stmt);
        }
    }

    private void evaluate(Statement stmt) {
        switch (stmt) {
            case ExpressionStatement expressionStatement -> evaluate(expressionStatement.expression);
            case IfStatement ifStmt -> {
                Object condition = evaluate(ifStmt.condition);
                if (isTruthy(condition)) {
                    for (Statement bodyStmt : ifStmt.body) {
                        evaluate(bodyStmt);
                    }
                }
            }
            case VariableDeclaration assign -> {
                Object value = evaluate(assign.initializer);
                if (value instanceof NonVoidScriptFunctionResult result) {
                    variables.put(assign.name, result.getValue());
                } else if (value instanceof VoidScriptFunctionResult) {
                    throw new RuntimeException("Assigning variable to void function result");
                } else variables.put(assign.name, new Literal(value));
            }
            case null, default -> throw new RuntimeException("Unknown statement type: " + stmt.getClass());
        }
    }

    private Object evaluate(Expression expr) {
        if (expr instanceof Literal lit) {
            return lit.value;
        } else if (expr instanceof Identifier var) {
            return evaluate(variables.get(var.name));
        } else if (expr instanceof BinaryExpression bin) {
            Object left = evaluate(bin.left);
            Object right = evaluate(bin.right);
            return applyBinary(bin.operator.type, left, right);
        } else if (expr instanceof FunctionCall call) {
            ScriptFunctionResult result = handleFunctionCall(call);
            if (result instanceof NonVoidScriptFunctionResult nonVoid) {
                return nonVoid.getValue().value;
            } else return null;
        } else if (expr instanceof UnaryExpression unaryExpression) {
            return applyUnary(unaryExpression.operator.type, unaryExpression.right);
        }
        throw new RuntimeException("Unknown expression type: " + expr.getClass());
    }

    private ScriptFunctionResult handleFunctionCall(FunctionCall call) {
        String name = call.functionName;
        List<Object> args = call.arguments.stream().map(this::evaluate).toList();
        ScriptFunction function = registry.get(name); // handle this not being found
        return function.execute(args, this.currentContext);
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof ValueAccumulator<?> va && va.getValue() instanceof Boolean b) return b;
        return true;
    }

    private Object applyBinary(TokenType op, Object left, Object right) {
        double l;
        if (left instanceof ValueAccumulator<?> va && va.getValue() instanceof Number d) {
            l = d.doubleValue();
        } else l = ((Number) left).doubleValue();
        double r;
        if (right instanceof ValueAccumulator<?> va && va.getValue() instanceof Number d) {
             r = d.doubleValue();
        } else r = ((Number) right).doubleValue();
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

    private Object applyUnary(TokenType op, Object right) {
        if (op == TokenType.BANG && right instanceof Boolean) {
            return !(Boolean) right;
        } else if (op == TokenType.MINUS) {
            double r;
            if (right instanceof ValueAccumulator<?> va && va.getValue() instanceof Number d) {
                r = d.doubleValue();
            } else r = ((Number) right).doubleValue();
            return -r;
        } else throw new RuntimeException("Could not apply unary operator" + op.name() + " to object " + right.toString());
    }
}
