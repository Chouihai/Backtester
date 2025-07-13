package Backtester.script.statements;

import Backtester.script.statements.expressions.Expression;

import java.util.List;

public class IfStatement extends Statement {
    public final List<IfBranch> branches;

    public IfStatement(List<IfBranch> branches) {
        this.branches = branches;
    }

    public static class IfBranch {
        public final Expression condition; // null for else branch
        public final List<Statement> body;

        public IfBranch(Expression condition, List<Statement> body) {
            this.condition = condition;
            this.body = body;
        }

        public boolean isElse() {
            return condition == null;
        }
    }
}
