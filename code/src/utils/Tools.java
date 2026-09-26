package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Tools {
    private static Tools instance;

    public static Tools getInstance() {
        if (instance == null) {
            instance = new Tools();
        }
        return instance;
    }

    private static String[] Assignsy = { "include", "return", "for", "if", "while", "else", };

    public static String[] getAssignsy() {
        return Assignsy;
    }

    public static boolean isSpace(char ch) {
        return ch == ' ';
    }

    public static boolean isNewline(char ch) {
        return ch == '\n';
    }

    public static boolean isTab(char ch) {
        return ch == '\t';
    }

    public static boolean isLetter(char ch) {
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
            return true;
        return false;
    }

    public static boolean isDigit(char ch) {
        if (ch >= '0' && ch <= '9')
            return true;
        return false;
    }

    public static boolean isColon(char ch) {
        return ch == ':';
    }

    public static boolean isPlus(char ch) {
        return ch == '+';
    }

    public static boolean isMinus(char ch) {
        return ch == '-';
    }

    public static boolean isStar(char ch) {
        return ch == '*';
    }

    public static boolean isLpar(char ch) {
        return ch == '(';
    }

    public static boolean isRpar(char ch) {
        return ch == ')';
    }

    public static boolean isSemi(char ch) {
        return ch == ';';
    }

    public static boolean isDivi(char ch) {
        return ch == '/';
    }

    public static boolean isEqu(char ch) {
        return ch == '=';
    }

    // 判断是否是标识符，如果是返回这个标识符，否则返回空
    public static String isAssignsy(String str) {
        for (var it : Assignsy) {
            if (it.equals(str)) {
                return it;
            }
        }
        return null;
    }

    public static String ReadFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return new String(content);
        } catch (Exception e) {
            System.err.println("文件读取失败：" + e.getMessage());
        }
        return null;
    }

    public static String ReadFile(BufferedReader br) {
        StringBuilder content = new StringBuilder();
        String line;
        try {
            while((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            return new String(content);
        } catch (Exception e) {
            System.err.println("文件读取失败：" + e.getMessage());
        }
        return null;
    }
}