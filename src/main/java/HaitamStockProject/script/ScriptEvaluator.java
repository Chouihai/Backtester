package HaitamStockProject.script;

import java.util.List;
import java.util.Objects;

public class ScriptEvaluator {
    private final EvaluationContext context = new EvaluationContext();

    public void evaluate(List<Statement> statements) {
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
            context.set(assign.name, value);
        }
        throw new RuntimeException("Unknown statement type: " + stmt.getClass());
    }

    private Object evaluate(Expression expr) {
        if (expr instanceof Literal lit) {
            return lit.value;
        } else if (expr instanceof Identifier var) {
            return context.get(var.name);
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

        if (name.equals("entry")) {
            // TODO: implement
//            String id = (String) args.get(0);
//            boolean isLong = (Boolean) args.get(1);
//            double quantity = ((Number) args.get(2)).doubleValue();
//            context.log("Enter position: " + id + " (" + (isLong ? "long" : "short") + ") x " + quantity);
//            return null;
        }

        if (name.equals("close")) {
            // TODO: implement
//            String id = (String) args.get(0);
//            context.log("Close position: " + id);
//            return null;
        }

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

    public EvaluationContext getContext() {
        return context;
    }
}
