package semantic;

/* 语义分析中可能出现的错误 */
public class ErrorToken {
    private String errorType ;  // 错误码
    private int line ;          // 错误所在行数

    public ErrorToken(String errorType, int line) {
        this.errorType = errorType;
        this.line = line ;
    }


}
