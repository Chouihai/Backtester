package Backtester.script.tokens;

import java.util.*;

public class Tokenizer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final Stack<Integer> indentStack = new Stack<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
//        keywords.put("strategy", TokenType.STRATEGY);
//        keywords.put("entry", TokenType.ENTRY);
//        keywords.put("close", TokenType.CLOSE);
        keywords.put("if", TokenType.IF);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("when", TokenType.WHEN);
    }

    public Tokenizer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        indentStack.push(0);
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '/': addToken(TokenType.SLASH); break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUALS); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '\n':
                line++;
                addToken(TokenType.NEWLINE);
                handleIndentation();
                break;
            case ' ':
            case '\r':
            case '\t':
                break; // ignore whitespace
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            addToken(TokenType.DOUBLE);
            advance(); // consume the '.'
            while (isDigit(peek())) advance();
        }
        else addToken(TokenType.INTEGER);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string at line " + line);
        }

        advance(); // closing "
        String value = source.substring(start + 1, current - 1);
        tokens.add(new Token(TokenType.STRING, value, line));
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        if (type != null) {
            String text = source.substring(start, current);
            tokens.add(new Token(type, text, line));
        }
    }

    private void handleIndentation() {
        int spaces = 0;
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ') {
                spaces++;
                advance();
            } else if (c == '\t') {
                spaces += 4; // or however you want to treat tabs
                advance();
            } else {
                break;
            }
        }

        if (spaces == currentIndent()) {
            return;
        } else if (spaces > currentIndent()) {
            indentStack.push(spaces);
            addToken(TokenType.INDENT);
        } else {
            while (spaces < currentIndent()) {
                indentStack.pop();
                addToken(TokenType.DEDENT);
            }
            if (spaces != currentIndent()) {
                throw new RuntimeException("Inconsistent indentation at line " + line);
            }
        }
    }

    private int currentIndent() {
        return indentStack.isEmpty() ? 0 : indentStack.peek();
    }
}
