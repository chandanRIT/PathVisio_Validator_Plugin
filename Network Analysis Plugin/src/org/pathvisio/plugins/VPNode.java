package org.pathvisio.plugins;

//ALL the Node and Edge related classes defined here
class Position {
	boolean visitStatus;
}

class VPNode extends Position{
	int id;// 
	String graphId; // all the nodes will have this
}

class VPDataNode extends VPNode{
	static int count=0;
	String dNodeType;
	
	public VPDataNode(String gid, String dnt){
		graphId=gid;
		dNodeType=dnt;
		id=++count;
	}
	
	public String toString(){
		return "DN-"+id+" ("+graphId+")";
	}
}

class VPLine extends VPNode{
	static int count=0;// id assigned to each node based on its type
	String[] graphRef=new String[2];// since 1 line always refers to 2 nodes (the 2 nodes being its end-points)
	
	public VPLine(){
		id=++count;
	}
	
	public String toString(){
		return "L-"+id+" ("+graphId+")";
	}
}

class VPAnchor extends VPNode{
	static int count=0;
	String shape;
	VPNode connectedToLine;
	
	public VPAnchor(String shape, String gid, VPLine vpl){
		this.shape=shape;
		graphId=gid;
		connectedToLine=vpl;
		id=++count;
	}
	
	public String toString(){
		return "A-"+id+" ("+graphId+")";
	}
}

// separate class Edge for the JUNG graph
class JPEdge extends Position{
	String[] nodesConnectedTo= new String[2];
	static int count=0;
	int eId;
	
	public JPEdge(String nId1,String nId2){
		nodesConnectedTo[0]=nId1;
		nodesConnectedTo[1]=nId2;
		eId=++count;
	}
	
	public String toString(){
		return "E-" + eId;
	}
}