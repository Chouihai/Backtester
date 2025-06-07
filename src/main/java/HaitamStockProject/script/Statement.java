package HaitamStockProject.script;

import java.util.List;

public abstract class Statement {}//implements AstNode {}

class VariableDeclaration extends Statement {
    public final String name;
    public final Expression initializer;

    public VariableDeclaration(String name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }
}

class ExpressionStatement extends Statement {
    public final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }
}

class IfStatement extends Statement {
    public final Expression condition;
    public final List<Statement> body;

    public IfStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }
}
