package HaitamStockProject.script;

import java.util.List;

public abstract class Expression {}// implements AstNode {}

class Literal extends Expression {
    public final Object value;

    public Literal(Object value) {
        this.value = value;
    }
}

class Identifier extends Expression {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }
}

class BinaryExpression extends Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public BinaryExpression(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}

class UnaryExpression extends Expression {
    public final Token operator;
    public final Expression right;

    public UnaryExpression(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }
}

class FunctionCall extends Expression {
    public final String functionName;
    public final List<Expression> arguments;

    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }
}
