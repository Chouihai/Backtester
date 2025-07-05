package Backtester.script;
import Backtester.script.statements.*;
import Backtester.script.statements.expressions.*;
import Backtester.script.tokens.Parser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private List<Statement> parse(String source) {
        Parser parser = new Parser();
        return parser.parse(source).statements();
    }

    @Test
    public void testLiteralExpression() {
        List<Statement> stmts = parse("x = 42");
        assertEquals(1, stmts.size());

        assertInstanceOf(VariableDeclaration.class, stmts.get(0));
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);
        assertEquals("x", decl.name);
        assertInstanceOf(Literal.class, decl.initializer);
        assertEquals(42, ((Literal) decl.initializer).value);
    }

    @Test
    public void testBinaryExpression() {
        List<Statement> stmts = parse("result = 5 + 3");
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);

        assertInstanceOf(BinaryExpression.class, decl.initializer);
        BinaryExpression expr = (BinaryExpression) decl.initializer;

        assertInstanceOf(Literal.class, expr.left);
        assertInstanceOf(Literal.class, expr.right);
    }

    @Test
    public void testUnaryExpression() {
        List<Statement> stmts = parse("x = -5");
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);
        assertInstanceOf(UnaryExpression.class, decl.initializer);
    }

    @Test
    public void oogaBooga() {
        List<Statement> stmts = parse("""
                
                x = -5
                
                y = 3
                
                
                """);
        assertEquals(2, stmts.size());
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);
        assertInstanceOf(UnaryExpression.class, decl.initializer);
    }


    @Test
    public void testFunctionCallExpression() {
        List<Statement> stmts = parse("y = ema(close, 20)");
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);

        assertInstanceOf(FunctionCall.class, decl.initializer);
        FunctionCall call = (FunctionCall) decl.initializer;

        assertEquals("ema", call.functionName);
        assertEquals(2, call.arguments.size());
    }

    @Test
    public void testVariableReference() {
        List<Statement> stmts = parse("z = close");
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);
        assertInstanceOf(Identifier.class, decl.initializer);
        assertEquals("close", ((Identifier) decl.initializer).name);
    }

    @Test
    public void testExpressionStatement() {
        List<Statement> stmts = parse("entry(\"Long\", true, 100)");
        assertEquals(1, stmts.size());
        ExpressionStatement exp = (ExpressionStatement) stmts.getFirst();
        assertEquals("entry", ((FunctionCall) exp.expression).functionName);
        List<Expression> arguments = ((FunctionCall) exp.expression).arguments;
        assertEquals(3, arguments.size());
        assertEquals("Long", ((Literal) arguments.getFirst()).value);
        assertEquals(true, ((Literal) arguments.get(1)).value);
        assertEquals(100, ((Literal) arguments.getLast()).value);
    }

    @Test
    public void testIfStatementWithOneLine() {
        List<Statement> stmts = parse("""
            if close > open
                entry("Long", true, 100)
            """);

        assertEquals(1, stmts.size());
        assertInstanceOf(IfStatement.class, stmts.get(0));
        IfStatement ifStmt = (IfStatement) stmts.get(0);

        assertInstanceOf(BinaryExpression.class, ifStmt.condition);
        assertEquals(1, ifStmt.body.size());
        assertInstanceOf(ExpressionStatement.class, ifStmt.body.get(0));
    }

    @Test
    public void testIfStatementWithMultipleLines() {
        List<Statement> stmts = parse("""
            if close > open
                x = 1
                y = 2.0
            """);

        assertEquals(1, stmts.size());
        IfStatement ifStmt = (IfStatement) stmts.get(0);

        assertEquals(2, ifStmt.body.size());
        assertInstanceOf(VariableDeclaration.class, ifStmt.body.get(0));
        assertInstanceOf(VariableDeclaration.class, ifStmt.body.get(1));
    }

    @Test
    public void testSmaAndOrderParsing() {
        List<Statement> stmts = parse("""
                sma20 = sma(20)
                sma50 = sma(50)
                
                if (sma20 > sma50)
                    createOrder("long", true, 10)
                    closeOrder("position1")
                if (sma50 > sma20)
                    createOrder("position1", false, 10)
                """);

        assertEquals(4, stmts.size());
        VariableDeclaration variableDeclaration1 = (VariableDeclaration) stmts.get(0);
        assertTrue(variableDeclaration1.initializer.isFunctionCall());
        assertEquals("sma20", variableDeclaration1.name);

        VariableDeclaration variableDeclaration2 = (VariableDeclaration) stmts.get(1);
        assertTrue(variableDeclaration2.initializer.isFunctionCall());
        assertEquals("sma50", variableDeclaration2.name);

        IfStatement ifStatement1 = (IfStatement) stmts.get(2);

        assertTrue(ifStatement1.condition.isBinary());
        assertEquals(2, ifStatement1.body.size());
        ExpressionStatement exprStmt = (ExpressionStatement) ifStatement1.body.get(0);
        ExpressionStatement exprStmt2 = (ExpressionStatement) ifStatement1.body.get(1);
        assertTrue(exprStmt.expression.isFunctionCall());
        assertTrue(exprStmt2.expression.isFunctionCall());

        IfStatement ifStatement2 = (IfStatement) stmts.get(3);
        assertTrue(ifStatement2.condition.isBinary());
        ExpressionStatement exprStmt3 = (ExpressionStatement) ifStatement1.body.get(0);
        assertTrue(exprStmt3.expression.isFunctionCall());
    }

    @Test
    public void testCrossoverAndOrderParsing() {
        List<Statement> stmts = parse("""
                sma20 = sma(20)
                sma50 = sma(50)
                
                if (crossover(sma20, sma50))
                    createOrder("long", true, 10)
                if (crossover(sma50, sma20))
                    createOrder("position1", false, 10)
                """);

        assertEquals(4, stmts.size());
        VariableDeclaration variableDeclaration1 = (VariableDeclaration) stmts.get(0);
        assertTrue(variableDeclaration1.initializer.isFunctionCall());
        assertEquals("sma20", variableDeclaration1.name);

        VariableDeclaration variableDeclaration2 = (VariableDeclaration) stmts.get(1);
        assertTrue(variableDeclaration2.initializer.isFunctionCall());
        assertEquals("sma50", variableDeclaration2.name);

        IfStatement ifStatement1 = (IfStatement) stmts.get(2);

        assertTrue(ifStatement1.condition.isFunctionCall());
        assertEquals(1, ifStatement1.body.size());
        ExpressionStatement exprStmt = (ExpressionStatement) ifStatement1.body.get(0);
        assertTrue(exprStmt.expression.isFunctionCall());

        IfStatement ifStatement2 = (IfStatement) stmts.get(3);
        assertTrue(ifStatement2.condition.isFunctionCall());
        ExpressionStatement exprStmt3 = (ExpressionStatement) ifStatement2.body.get(0);
        assertTrue(exprStmt3.expression.isFunctionCall());
    }

    @Test
    public void testChainedBinaryExpression() {
        List<Statement> stmts = parse("x = 1 + 2 * 3");
        VariableDeclaration decl = (VariableDeclaration) stmts.get(0);
        assertInstanceOf(BinaryExpression.class, decl.initializer);

        BinaryExpression top = (BinaryExpression) decl.initializer;
        assertEquals("+", top.operator.lexeme);

        assertInstanceOf(Literal.class, top.left);
        assertInstanceOf(BinaryExpression.class, top.right);
    }
}
