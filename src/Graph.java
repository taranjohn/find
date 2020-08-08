import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.*;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	Node highlightedNode;
	Collection<Road> highlightedRoads = new HashSet<>();
	Collection<Node> articulationPoints = new HashSet<>();
	Collection<Segment> minimumSpanningTree = new HashSet<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(Mapper.SEGMENT_COLOUR);
		for (Segment s : segments)
			s.draw(g2, origin, scale);

		// draw the segments of the minimum spanning trees
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Segment seg : minimumSpanningTree) {
			seg.draw(g2, origin, scale);
		}

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}

		// highlight articulation points
		for (Node node : articulationPoints) {
			node.draw(g2, screen, origin, scale);
		}
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	public void setHighlightPoints(Collection<Node> articulationPoints) {this.articulationPoints = articulationPoints;}

	public void setHighlightSegments(Collection<EdgeTuple> minimumSpanningTree){
		for(EdgeTuple edge: minimumSpanningTree){
			Node firstNode = edge.getFirstNode();
			Node secondNode = edge.getSecondNode();
			this.minimumSpanningTree.add(getSegment(firstNode, secondNode));
		}
	}

	/**
	 * Gets the segment(edge) between two nodes
	 * @param firstNode
	 * @param secondNode
	 * @return segment between the two given nodes
	 */
	public Segment getSegment(Node firstNode, Node secondNode){
		for(Segment segment: firstNode.segments){
			if(segment.start.equals(secondNode)){
				return segment;
			}
			else if(segment.end.equals(secondNode)){
				return segment;
			}
		}
		return null;
	}
}

// code for COMP261 assignments