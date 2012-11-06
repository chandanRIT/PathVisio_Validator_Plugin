package org.pathvisio.plugins;

//ALL the Node and Edge related classes defined here
class Position {
	boolean visitStatus;
}

class VPNode extends Position {
	int id;//
	String graphId; // all the nodes will have this

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (this.getClass() != other.getClass())
			return false;
		VPNode otherVPNode = (VPNode) other;
		return this.graphId.equals(otherVPNode.graphId);
	}
}

class VPDataNode extends VPNode {
	static int count = 0;
	String dNodeType;
	int edgeIndex;

	public VPDataNode(String gid, String dnt) {
		graphId = gid;
		dNodeType = dnt;
		id = ++count;
	}

	public String toString() {
		return "DN-" + id + " (" + graphId + ")";
	}
}

class VPLine extends VPNode {
	static int count = 0;// id assigned to each node based on its type
	String[] graphRef = new String[2];// since 1 line always refers to 2 nodes
										// (the 2 nodes being its end-points)

	public VPLine() {
		id = ++count;
	}

	public String toString() {
		return "L-" + id + " (" + graphId + ")";
	}
}

class VPAnchor extends VPNode {
	static int count = 0;
	String shape;
	VPNode connectedToLine;

	public VPAnchor(String shape, String gid, VPLine vpl) {
		this.shape = shape;
		graphId = gid;
		connectedToLine = vpl;
		id = ++count;
	}

	public String toString() {
		return "A-" + id + " (" + graphId + ")";
	}
}

// separate class Edge for the JUNG graph
class JPEdge extends Position implements Comparable<JPEdge> {
	String[] nodesConnectedTo = new String[2];
	static int count = 0;
	int eId;

	public JPEdge(String nId1, String nId2) {
		nodesConnectedTo[0] = nId1;
		nodesConnectedTo[1] = nId2;
		eId = ++count;
	}

	public String toString() {
		return ("E-" + eId);
	}

	public int compareTo(JPEdge jpe) {
		// TODO Auto-generated method stub
		if (eId > jpe.eId)
			return 1;
		else if (eId < jpe.eId)
			return -1;
		else
			return 0;
	}
}