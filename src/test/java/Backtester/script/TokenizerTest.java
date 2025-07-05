package Backtester.script;

import Backtester.script.tokens.Token;
import Backtester.script.tokens.TokenType;
import Backtester.script.tokens.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    private void assertTokenSequence(String input, TokenType... expectedTypes) {
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokens = tokenizer.tokenize();

        // Strip EOF for comparison if it's not expected
        if (expectedTypes.length == 0 || expectedTypes[expectedTypes.length - 1] != TokenType.EOF) {
            tokens.remove(tokens.size() - 1);
        }

        assertEquals(expectedTypes.length, tokens.size(), "Token count mismatch");

        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], tokens.get(i).type,
                    "Expected token type " + expectedTypes[i] + " but got " + tokens.get(i).type + " at index " + i);
        }
    }

    @Test
    public void testSimpleAssignment() {
        assertTokenSequence(
                "x = 5",
                TokenType.IDENTIFIER, TokenType.EQUALS, TokenType.INTEGER
        );
    }


    @Test
    public void testDouble() {
        assertTokenSequence(
                "x = 5.0",
                TokenType.IDENTIFIER, TokenType.EQUALS, TokenType.DOUBLE
        );
    }


    @Test
    public void testFunctionCall() {
        assertTokenSequence(
                "ema(close, 20)",
                TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.IDENTIFIER,
                TokenType.COMMA, TokenType.INTEGER, TokenType.RPAREN
        );
    }

    @Test
    public void testComparisonExpression() {
        assertTokenSequence(
                "sma20 > sma50",
                TokenType.IDENTIFIER, TokenType.GREATER, TokenType.IDENTIFIER
        );
    }

    @Test
    public void testBooleanLiteral() {
        assertTokenSequence(
                "true == false",
                TokenType.TRUE, TokenType.EQUAL_EQUAL, TokenType.FALSE
        );
    }

    @Test
    public void testBang() { // TODO: make it a syntax error to have a space between a bang and an identifier
        assertTokenSequence(
                "b != !a",
                TokenType.IDENTIFIER, TokenType.BANG_EQUAL, TokenType.BANG, TokenType.IDENTIFIER
        );
    }

    @Test
    public void testStringLiteral() {
        assertTokenSequence(
                "\"Long\"",
                TokenType.STRING
        );
    }

    @Test
    public void testStrategyEntryLine() {
        assertTokenSequence(
                "strategy.entry(\"Long\", true, 1000.0, when = long)",
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER,
                TokenType.LPAREN, TokenType.STRING, TokenType.COMMA,
                TokenType.TRUE, TokenType.COMMA, TokenType.DOUBLE, TokenType.COMMA,
                TokenType.WHEN, TokenType.EQUALS, TokenType.IDENTIFIER,
                TokenType.RPAREN
        );
    }

    @Test
    public void testMultipleLines() {
        assertTokenSequence(
                """
                a = 1
                b = 2
                c >= a + b
                """,
                TokenType.IDENTIFIER, TokenType.EQUALS, TokenType.INTEGER, TokenType.NEWLINE,
                TokenType.IDENTIFIER, TokenType.EQUALS, TokenType.INTEGER, TokenType.NEWLINE,
                TokenType.IDENTIFIER, TokenType.GREATER_EQUAL, TokenType.IDENTIFIER,
                TokenType.PLUS, TokenType.IDENTIFIER, TokenType.NEWLINE
        );
    }

    @Test
    public void testSimpleIndent() {
        String source = """
            if true
                strategy.entry("Long", true, 100)
            """;

        assertTokenSequence(source,
                TokenType.IF, TokenType.TRUE, TokenType.NEWLINE,
                TokenType.INDENT,
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER,
                TokenType.LPAREN, TokenType.STRING, TokenType.COMMA, TokenType.TRUE,
                TokenType.COMMA, TokenType.INTEGER, TokenType.RPAREN,
                TokenType.NEWLINE,
                TokenType.DEDENT,
                TokenType.EOF
        );
    }

    @Test
    public void testMultipleDedents() {
        String source = """
            if true
                strategy.entry("Long", true, 100)
                    foo()
            bar()
            """;


        assertTokenSequence(source,
                TokenType.IF, TokenType.TRUE, TokenType.NEWLINE,
                TokenType.INDENT,
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER,
                TokenType.LPAREN, TokenType.STRING, TokenType.COMMA, TokenType.TRUE,
                TokenType.COMMA, TokenType.INTEGER, TokenType.RPAREN,
                TokenType.NEWLINE,
                TokenType.INDENT,
                TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.RPAREN,
                TokenType.NEWLINE,
                TokenType.DEDENT,
                TokenType.DEDENT,
                TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.RPAREN,
                TokenType.NEWLINE,
                TokenType.EOF
        );
    }

}
