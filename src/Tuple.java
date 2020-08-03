public class Tuple {

    //fields
    private Node currentNode;
    private int depth;
    private Node parentNode;

    public Tuple(Node currentNode, int depth, Node parentNode) {
        this.currentNode = currentNode;
        this.depth = depth;
        this.parentNode = parentNode;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }
}
