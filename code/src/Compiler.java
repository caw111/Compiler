import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import syntax.AST.AstNode;
import syntax.Parser;
import utils.Tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Compiler {

    public static void main(String[] args) {
        Lexer lexer;
        Token token;
        List<Token> tokenList = new ArrayList<Token>();
        StringBuilder error = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader("testfile.txt"))) {
            String source = Tools.ReadFile(br);
            lexer = new Lexer(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (BufferedWriter fwOut = new BufferedWriter(new FileWriter("parser.txt"));
             BufferedWriter fwErr = new BufferedWriter(new FileWriter("error.txt"));){
            while ((token = lexer.nextToken()) != null) {
                if(token.getType() != TokenType.NOTE) tokenList.add(token);
            }
            error.append(lexer.getErrorContent());

            Parser parser = new Parser(tokenList);
            AstNode root = parser.start();
            String errorContent = parser.getErrorContent();
            error.append(errorContent);

            if(error.isEmpty()) root.printTree(fwOut);
            List<String> errors = new ArrayList<>(error.toString().lines().toList());
            errors.sort(Comparator.comparingInt(line -> Integer.parseInt(line.substring(0, line.indexOf(' ')))));
            for (String line : errors) {
                fwErr.write(line + "\n");
            }

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
