package Backtester.script.tokens;

public enum TokenType {
    // Keywords
    STRATEGY, ENTRY, CLOSE, IF, ELIF, ELSE, TRUE, FALSE, WHEN, NULL,

    // Identifiers and literals
    IDENTIFIER, INTEGER, DOUBLE, STRING,

    // Operators
    PLUS, MINUS, STAR, SLASH,
    EQUALS, GREATER, LESS,
    GREATER_EQUAL, LESS_EQUAL,
    EQUAL_EQUAL, BANG_EQUAL,
    BANG,

    // Punctuation
    LPAREN, RPAREN,
    COMMA, DOT, COLON,
    NEWLINE, INDENT, DEDENT,

    // Special
    EOF
}
