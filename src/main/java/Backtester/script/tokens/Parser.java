package Backtester.script.tokens;

import Backtester.objects.CompiledScript;
import Backtester.script.statements.ExpressionStatement;
import Backtester.script.statements.IfStatement;
import Backtester.script.statements.Statement;
import Backtester.script.statements.VariableDeclaration;
import Backtester.script.statements.expressions.*;

import java.util.*;

public class Parser {

    private List<Token> tokens;
    private Set<String> functionCalls;
    private int current = 0;

    public CompiledScript parse(String s) {
        Tokenizer tokenizer = new Tokenizer(s);
        tokens = tokenizer.tokenize();
        functionCalls = new HashSet<>();
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            while(check(TokenType.NEWLINE)) advance();
            Statement stmt = parseStatement();
            statements.add(stmt);
            while(check(TokenType.NEWLINE)) advance();
        }
        return new CompiledScript(statements, functionCalls);
    }


    private Statement parseStatement() {
        if (match(TokenType.IF)) {
            return parseIfStatement();
        }

        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUALS)) {
            return parseVariableDeclaration();
        }

        return parseExpressionStatement();
    }

    private Statement parseVariableDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        consume(TokenType.EQUALS, "Expected '=' after variable name.");
        Expression expr = parseExpression();
        VariableDeclaration var = new VariableDeclaration(name.lexeme, expr);
        return var;
    }

    private Statement parseExpressionStatement() {
        Expression expr = parseExpression();
        return new ExpressionStatement(expr);
    }

    private Statement parseIfStatement() {
        Expression condition = parseExpression();
        consume(TokenType.NEWLINE, "Expected newline after if condition.");
        consume(TokenType.INDENT, "Expected indent after if condition.");

        List<Statement> body = new ArrayList<>();

        while (!(isAtEnd() || check(TokenType.DEDENT))) {
            body.add(parseStatement());
            if (!isAtEnd()) consume(TokenType.NEWLINE, "Expected newline between if body statements.");
        }
        if (!isAtEnd()) consume(TokenType.DEDENT, "Expected dedent after if statement.");
        return new IfStatement(condition, body);
    }

    private Expression parseExpression() {
        return parseEquality();
    }

    private Expression parseEquality() {
        Expression expr = parseComparison();

        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expression right = parseComparison();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseComparison() {
        Expression expr = parseTerm();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = parseTerm();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = parseFactor();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseFactor() {
        Expression expr = parseUnary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = parseUnary();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseUnary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = parseUnary();
            return new UnaryExpression(operator, right);
        }

        return parsePrimary();
    }

    private Expression parsePrimary() {
        if (match(TokenType.FALSE)) return new Literal(false);
        if (match(TokenType.TRUE)) return new Literal(true);
        if (match(TokenType.INTEGER)) return new Literal(Integer.parseInt(previous().lexeme));
        if (match(TokenType.DOUBLE)) return new Literal(Double.parseDouble(previous().lexeme));
        if (match(TokenType.STRING)) return new Literal(previous().lexeme);

        if (match(TokenType.IDENTIFIER)) {
            Token identifier = previous();
            if (match(TokenType.LPAREN)) {
                List<Expression> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RPAREN, "Expected ')' after arguments.");
                FunctionCall.validateArguments(identifier.lexeme, args.size());
                FunctionCall fn = new FunctionCall(identifier.lexeme, args);
                functionCalls.add(fn.functionName);
                return fn;
            } else return new Identifier(identifier.lexeme);
        }

        if (match(TokenType.LPAREN)) {
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return expr;
        }

        throw error(peek(), "Expected expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        return current + 1 < tokens.size() && tokens.get(current + 1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException("Parse error at '" + token.lexeme + "': " + message);
    }
}
