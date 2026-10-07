package syntax.AST;
import lexer.Token;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* 抽象语法树节点类 */
public class AstNode {
    private String name;    // 当前树节点（非终结符名称）
    private Token token;    // 终结符节点才有 token
    private final List<AstNode> nodes = new ArrayList<>();

    public AstNode(String name) {
        this.name = name;
        this.token = null;
    }

    public AstNode(Token token) {
        this.name = null;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }

    public void addChild(AstNode child) {
        if(child != null) {
            nodes.add(child);
        }
    }

    public void addChild(Token token) {
        AstNode node = new AstNode(token);
        nodes.add(node);
    }

    public List<AstNode> getChildren() {
        return nodes;
    }

    public boolean isTerminal() {
        return token != null;
    }

    public void printTree() {
        if(!this.nodes.isEmpty()) {
            for(var node : nodes) {
                node.printTree();
            }
        }
        if(!isTerminal()) System.out.println(name);
        else System.out.println(token);
    }


    public void printTree(BufferedWriter bw) throws IOException {
        if(!this.nodes.isEmpty()) {
            for(var node : nodes) {
                node.printTree(bw);
            }
        }
        if(!isTerminal()) bw.write(this.name+"\n");
        else bw.write(this.token.toString()+"\n");
    }
}

