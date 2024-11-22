import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SP_6 {

    // Define token patterns using regular expressions
    private static final List<TokenPattern> TOKEN_PATTERNS = Arrays.asList(
        new TokenPattern("KEYWORD", "\\b(int|float|if|else|while|return)\\b"),
        new TokenPattern("IDENTIFIER", "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b"),
        new TokenPattern("NUMBER", "\\b\\d+(\\.\\d+)?\\b"), // Integer or Float
        new TokenPattern("OPERATOR", "[+\\-*/%=]"),
        new TokenPattern("PUNCTUATOR", "[{}();,]"),
        new TokenPattern("NEWLINE", "\\n"),
        new TokenPattern("SKIP", "[ \t]+"), // Spaces and tabs
        new TokenPattern("MISMATCH", ".")   // Any unrecognized character (error)
    );

    // Arrays for storing tokens by type
    private static final List<String> keywords = new ArrayList<>();
    private static final List<String> identifiers = new ArrayList<>();
    private static final List<String> numbers = new ArrayList<>();
    private static final List<String> operators = new ArrayList<>();
    private static final List<String> punctuators = new ArrayList<>();

    public static void main(String[] args) {
        // Specify the input file here
        String filename = "SP6_input.c";  // Change this to your input file's name

        try {
            List<Token> tokens = analyzeFile(filename);
            printTokens(tokens);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    // Tokenize the input file line-by-line
    private static List<Token> analyzeFile(String filename) throws IOException {
        List<Token> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                tokens.addAll(tokenizeLine(line, lineNumber));
                lineNumber++;
            }
        }
        return tokens;
    }

    // Tokenize a single line of input
    private static List<Token> tokenizeLine(String line, int lineNumber) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = buildMatcher(line);

        while (matcher.find()) {
            for (TokenPattern pattern : TOKEN_PATTERNS) {
                if (matcher.group(pattern.type) != null) {
                    String lexeme = matcher.group(pattern.type);

                    switch (pattern.type) {
                        case "NEWLINE", "SKIP" -> {
                            // Skip these tokens
                        }
                        case "MISMATCH" -> 
                            System.err.printf("Error: Unexpected character '%s' on line %d%n", lexeme, lineNumber);
                        default -> {
                            int tokenValue = addToSpecificArray(pattern.type, lexeme);
                            tokens.add(new Token(lineNumber, lexeme, pattern.type, tokenValue));
                        }
                    }
                    break;
                }
            }
        }
        return tokens;
    }

    // Add token to its respective array and return its index within that array (1-based)
    private static int addToSpecificArray(String type, String lexeme) {
        List<String> targetList;

        switch (type) {
            case "KEYWORD" -> targetList = keywords;
            case "IDENTIFIER" -> targetList = identifiers;
            case "NUMBER" -> targetList = numbers;
            case "OPERATOR" -> targetList = operators;
            case "PUNCTUATOR" -> targetList = punctuators;
            default -> throw new IllegalStateException("Unexpected token type: " + type);
        }

        // Check if the lexeme already exists in the list
        if (!targetList.contains(lexeme)) {
            targetList.add(lexeme); // Add only if it's not already there
        }

        // Return the 1-based index of the lexeme in the list
        return targetList.indexOf(lexeme) + 1;
    }

    // Build the regex matcher for the line using all token patterns
    private static Matcher buildMatcher(String line) {
        StringBuilder regex = new StringBuilder();
        for (TokenPattern pattern : TOKEN_PATTERNS) {
            regex.append(String.format("|(?<%s>%s)", pattern.type, pattern.regex));
        }
        return Pattern.compile(regex.substring(1)).matcher(line); // Skip leading "|"
    }

    // Print the tokens in a table-like format
    private static void printTokens(List<Token> tokens) {
        System.out.printf("%-5s %-15s %-15s %-15s%n", "Line", "Lexeme", "Token Type", "Token Value");
        System.out.println("--------------------------------------------------------------");
        for (Token token : tokens) {
            System.out.printf("%-5d %-15s %-15s %-15d%n", token.lineNumber, token.lexeme, token.type, token.tokenValue);
        }
    }

    // Class to store token information
    private static class Token {
        int lineNumber;
        String lexeme;
        String type;
        int tokenValue;

        Token(int lineNumber, String lexeme, String type, int tokenValue) {
            this.lineNumber = lineNumber;
            this.lexeme = lexeme;
            this.type = type;
            this.tokenValue = tokenValue;
        }
    }

    // Class to store token pattern (type and regex)
    private static class TokenPattern {
        String type;
        String regex;

        TokenPattern(String type, String regex) {
            this.type = type;
            this.regex = regex;
        }
    }
}
