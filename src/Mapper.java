import javax.xml.stream.events.EntityDeclaration;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Set<Node> forest;
	private Set<EdgeTuple> tree;

	// mapper mode fields
	private boolean findAllConnectedAP = false; // if false, it finds all APs on the graph (not just connected part)
	private boolean findAllConnectedMST = false;
	private boolean minimumSpanningTreeFinder = false;
	private boolean articulationPointFinder = false;

	/** Methods which alter the mode of the program (extending the GUI abstract methods */
	public void modeFindAllConnectedAP(){
		findAllConnectedAP = true;
		articulationPointFinder = true;
		findAllConnectedMST = false;
		minimumSpanningTreeFinder = false;
	}

	public void modeFindAllAP(){
		findAllConnectedAP = false;
		articulationPointFinder = true;
		findAllConnectedMST = false;
		minimumSpanningTreeFinder = false;
	}

	public void modeFindConnectedMST(){
		findAllConnectedAP = false;
		articulationPointFinder = false;
		findAllConnectedMST = true;
		minimumSpanningTreeFinder = true;
	}

	public void modeFindAllMST(){
		findAllConnectedAP = false;
		articulationPointFinder = false;
		findAllConnectedMST = false;
		minimumSpanningTreeFinder = true;
	}

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		//this.clicked = closest; // set the clicked field for the articulation point algorithm

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
			if(this.articulationPointFinder) articulationPointFinder(closest);
			else if(this.minimumSpanningTreeFinder) minimumSpanningTreeFinder(closest);
		}
	}

	/**
	 * Articulation point algorithm which initialises all the values, and calls helper algorithm methods
	 * @param clicked node which has been clicked by the user, and will have the search algorithm called on
	 */
	public void articulationPointFinder(Node clicked){
		// clear highlights on gui
		graph.articulationPoints.clear();
		graph.minimumSpanningTree.clear();

		// intialises the node values
		Set<Node> articulationPoints = new HashSet<Node>();

		for(Node node: graph.nodes.values()){
			node.depth = Integer.MAX_VALUE;
			node.reachBack = 0;
			node.children = new HashSet<Node>();
		}

		if(findAllConnectedAP){
			articulationPointFinder(clicked, articulationPoints);
		}
		else {
			for (Node node : graph.nodes.values()) {
				if (node.depth == Integer.MAX_VALUE) {
					articulationPointFinder(node, articulationPoints);
				}
			}
		}

		graph.setHighlightPoints(articulationPoints);
		getTextOutputArea().setText("Number of Articulation Points: " + articulationPoints.size());
	}

	/**
	 * Starts the articulation point algorithm, then calls helper method to expand each neighbour
	 * @param root root of the articulation point algorithm
	 * @param articulationPoints set of articulation points that have already been found
	 */
	public void articulationPointFinder(Node root, Set<Node> articulationPoints){
		int numSubTrees = 0;
		root.depth = 0;

		for(Node neighbour: findNeighbours(root)) {
			if (neighbour.depth == Integer.MAX_VALUE) {
				articulationPointFinder(neighbour, 1, root, articulationPoints);
				numSubTrees++;
			}
			if (numSubTrees > 1) {
				articulationPoints.add(root);
			}
		}
	}

	/**
	 * Algorithm helper method
	 * @param firstNode neighbour of the root node
	 * @param depth depth of the node
	 * @param root root node
	 * @param articulationPoints articulation points which have already been found
	 */
	public void articulationPointFinder(Node firstNode, int depth, Node root, Set<Node> articulationPoints){
		Stack<Tuple> fringe = new Stack<Tuple>();
		fringe.push(new Tuple(firstNode, depth, root)); // push initial tuple onto stack

		while(!fringe.isEmpty()){
			Tuple currentTuple = fringe.peek();
			Node currentNode = currentTuple.getCurrentNode();
			Node parentNode = currentTuple.getParentNode();
			if(currentNode.depth==Integer.MAX_VALUE){ // checks if node is visited
				currentNode.depth = currentTuple.getDepth();
				currentNode.reachBack = currentTuple.getDepth();
				currentNode.children = findNeighbours(currentTuple.getCurrentNode()); // finds all adjacent nodes
				currentNode.children.remove(parentNode); // removes the parent node from the children
			}
			else if(!currentNode.children.isEmpty()){
				Node selectedChild = null;
				for(Node child: currentNode.children){ // gets a child from the set, and then removes the child from the children set
					selectedChild = child;
					break;
				}
				currentNode.children.remove(selectedChild);
				if(selectedChild.depth < Integer.MAX_VALUE){
					currentNode.reachBack = Math.min(selectedChild.depth, currentNode.reachBack);
				}
				else {
					fringe.push(new Tuple(selectedChild, currentTuple.getDepth() + 1, currentNode)); // adds new tuple to the fringe
				}
			}
			else{
				if(!currentNode.equals(firstNode)){
					currentTuple.getParentNode().reachBack = Math.min(currentNode.reachBack, currentTuple.getParentNode().reachBack);
					if(currentNode.reachBack >= parentNode.depth){
						articulationPoints.add(parentNode);
					}
				}
				fringe.remove(currentTuple);
			}
		}
	}

	/**
	 * Returns a set of neighbour nodes of the specified node
	 * @param node
	 * @returns set of neighbours
	 */
	public Set<Node> findNeighbours(Node node){
		Set<Node> neighbours = new HashSet<Node>();
		for(Segment segment: node.segments){
			if(segment.start.equals(node)){
				neighbours.add(segment.end);
			}
			else{
				neighbours.add(segment.start);
			}
		}
		return neighbours;
	}

	/**
	 * Find the minimum spanning tree of the graph using Kruskal's algorithm and display it
	 * @param clicked (if using the find all connected method) displays the minimum spanning tree of the connected graph
	 */
	public void minimumSpanningTreeFinder(Node clicked) {
		// clear highlights on gui
		graph.articulationPoints.clear();
		graph.minimumSpanningTree.clear();

		Set<Node> searchNodes = new HashSet<Node>();
		Set<Segment> searchSegments = new HashSet<Segment>();

		// make forest
		forest = new HashSet<Node>();
		for(Node node: graph.nodes.values()){
			makeSet(node);
			node.mstDepth = 0;
			forest.add(node);
		}

		// make fringe
		PriorityQueue<EdgeTuple> fringe = new PriorityQueue<>((EdgeTuple tuple1, EdgeTuple tuple2) -> {
			if(tuple1.getEdgeWeight() < tuple2.getEdgeWeight()){
				return -1;
			} else {
				return 1;
			}
		});

		// add EdgeTuples to fringe
		if(findAllConnectedMST){ // if finding all the connected graph nodes
			searchNodes = getGraphNodes(searchNodes, clicked);
			for(Segment segment: graph.segments){
				if(searchNodes.contains(segment.start) && searchNodes.contains(segment.end)){
					searchSegments.add(segment);
					fringe.offer(new EdgeTuple(segment.start, segment.end, segment.length));
				}
			}
		}
		else { // if finding all nodes on the graph
			searchNodes.addAll(graph.nodes.values());
			searchSegments.addAll(graph.segments);
			for (Segment segment : searchSegments) {
				fringe.offer(new EdgeTuple(segment.start, segment.end, segment.length));
			}
		}

		// make tree
		 tree = new HashSet<EdgeTuple>();

		// make minimum spanning tree
		while(!fringe.isEmpty()){
			EdgeTuple currentTuple = fringe.poll();
			if(find(currentTuple.getFirstNode()) != find(currentTuple.getSecondNode())){
				union(currentTuple.getFirstNode(), currentTuple.getSecondNode());
				tree.add(currentTuple);
			}
		}

		graph.setHighlightSegments(tree);
		getTextOutputArea().setText("Number of Segments in Minimum Spanning Tree: " + tree.size());

	}

	/**
	 * Creates tree like objects out of the nodes by initialising the parent each node to itself
	 * @param node
	 * @return node with itself as the parent
	 */
	public Node makeSet(Node node){
		node.parent = node;
		return node;
	}

	/**
	 * Finds the parent of the specified node
	 * @param node
	 * @return parent node
	 */
	public Node find(Node node){
		if(node.parent == node){
			return node;
		}
		else{
			return find(node.parent); // recursively call the method with the node's parent until the root is reached
		}
	}

	/**
	 * Merges two trees together (Kruskal's Algorithm)
	 * @param firstNode
	 * @param secondNode
	 */
	public void union(Node firstNode, Node secondNode){
		Node firstRoot = find(firstNode);
		Node secondRoot = find(secondNode);
		if(firstRoot.equals(secondRoot)) return;
		else if(firstRoot.mstDepth < secondRoot.mstDepth){
			firstRoot.parent = secondRoot;
			forest.remove(firstRoot);
		}
		else{
			secondRoot.parent = firstRoot;
			forest.remove(secondRoot);
			if(firstRoot.mstDepth == secondRoot.mstDepth) firstRoot.mstDepth ++;
		}
	}

	/**
	 * Recursive function which gets all nodes in a graph
	 * @param searchNodes set to add the neighbour nodes into
	 * @param current node to get current graph from
	 * @return searchNodes set with all the nodes in the graph added
	 */
	public Set<Node> getGraphNodes(Set<Node> searchNodes, Node current){
		searchNodes.add(current);
		for(Node node: findNeighbours(current)){
			if(!searchNodes.contains(node)){
				getGraphNodes(searchNodes, node); // calls function recursively on neighbour nodes
			}
		}
		return searchNodes;
	}

	@Override
	protected void onSearch() {}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments