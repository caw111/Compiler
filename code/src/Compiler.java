// 进行词法分析
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import utils.Tools;

import java.io.*;

public class Compiler {

    public static void main(String[] args) {
        Lexer lexer;
        Token token;
        StringBuilder content = new StringBuilder();
        StringBuilder error = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader("testfile.txt"))) {
            String source = Tools.ReadFile(br);
            lexer = new Lexer(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (BufferedWriter fwOut = new BufferedWriter(new FileWriter("lexer.txt"));
             BufferedWriter fwErr = new BufferedWriter(new FileWriter("error.txt"));){
            while ((token = lexer.nextToken()) != null) {
                if(token.getType() != TokenType.a)
                    content.append(token.toString());
                else
                    error.append(token.toString());
            }
            if(error.isEmpty())
                fwOut.write(String.valueOf(content));
            fwErr.write(String.valueOf(error));
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}