public class EdgeTuple {

    // fields
    private Node firstNode;
    private Node secondNode;
    private double edgeWeight;

    public EdgeTuple(Node firstNode, Node secondNode, double edgeWeight) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.edgeWeight = edgeWeight;
    }

    public Node getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(Node firstNode) {
        this.firstNode = firstNode;
    }

    public Node getSecondNode() {
        return secondNode;
    }

    public void setSecondNode(Node secondNode) {
        this.secondNode = secondNode;
    }

    public double getEdgeWeight() {
        return edgeWeight;
    }

    public void setEdgeWeight(double edgeWeight) {
        this.edgeWeight = edgeWeight;
    }
}
