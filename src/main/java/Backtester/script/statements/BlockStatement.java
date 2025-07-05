package Backtester.script.statements;

import java.util.List;

public class BlockStatement {
    public final List<Statement> statements;

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }
}
