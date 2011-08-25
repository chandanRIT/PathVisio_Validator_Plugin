package org.pathvisio.plugins;

import java.util.Iterator;
import java.util.List;

import edu.uci.ics.jung.graph.Graph;

/** Generic DFS traversal of a graph using the template method pattern.
 * A subclass should override various methods to add functionality.
 * Parameterized types:
 *   V, the type for the elements stored at vertices
 *   E, the type for the elements stored at edges
 *   I, the type for the information object passed to the execute method
 *   R, the type for the result object returned by the DFS
 */
public class DFS<I, R> {
	protected Graph<VPNode, JPEdge> graph;    // The graph being traversed
	protected VPNode start;      // The start vertex for the DFS
	protected I info;               // Information object passed to DFS
	protected R visitResult;        // The result of a recursive traversal call
	
	protected int runCount;
	protected VPDataNode actualFF,currFF,prevFF;
	
	
	/** Mark a position (vertex or edge) as visited. */
	protected void visit(Position p) { p.visitStatus=true; }
	/** Mark a position (vertex or edge) as unvisited. */
	protected void unVisit(Position p) { p.visitStatus=false; }
	/** Test if a position (vertex or edge) has been visited. */
	protected boolean isVisited(Position p) {
		return p.visitStatus;
	}

	/** Setup method that is called prior to the DFS execution. */
	protected void setup() {}
	/** Initializes result (called first, once per vertex visited). */
	protected void initResult() {}
	/** Called when we encounter a vertex (v). */
	protected void startVisit(VPNode v) {}
	/** Called after we finish the visit for a vertex (v). */
	protected void finishVisit(VPNode v) {}
	/** Called when we traverse a discovery edge (e) from a vertex (from). */
	protected void traverseDiscovery(JPEdge e, VPNode from) {}
	/** Called when we traverse a back edge (e) from a vertex (from). */
	protected void traverseBack(JPEdge e, VPNode from) {}
	/** Determines whether the traversal is done early. */
	protected boolean isDone() { return false; /* default value */ }
	/** Returns a result of a visit (if needed). */
	protected R result() { return null; /* default value */ }
	/** Returns the final result of the DFS execute method. */
	protected R finalResult(R r) { return r; /* default value */ }


	protected void checkRoute(){
		
	}
	
	/** Execute a depth first search traversal on graph g, starting
	 * from a start vertex s, passing in an information object (in) */
	public R execute(Graph<VPNode, JPEdge> g, VPNode s, I in) {
		graph = g;
		start = s;
		info = in;
		//route=new StringBuilder();
		for(VPNode v: graph.getVertices()) unVisit(v); // mark vertices as unvisited
		for(JPEdge e: graph.getEdges()) unVisit(e);      // mark edges as unvisited
		setup();           // perform any necessary setup prior to DFS traversal
		return finalResult(dfsTraversal(start));
	}
	/** Recursive template method for a generic DFS traversal.  */
	protected R dfsTraversal(VPNode v) {
		initResult();
		if (!isDone())
			startVisit(v);
		if (!isDone()) {
			visit(v);
			int myEdgeIndex=0;
			if(actualFF!=null && actualFF.equals(v)){
				myEdgeIndex=actualFF.edgeIndex;
				System.out.println("inside index chenge "+myEdgeIndex);
			}
			List<JPEdge> myList=NAP_Utility.wrapperForGraphIncidentEdges(graph, v, myEdgeIndex);
			System.out.println("sublist "+myList);
			Iterator<JPEdge> myIterator=myList.iterator();
			while (myIterator.hasNext()) {
				JPEdge e=myIterator.next();
				
				//String tempRoute=route.toString()+e.eId;
				//System.out.println(" temproute " +tempRoute +"ignored routes "+ignoredRoutesList);
				/*while(v.equals(node_b4BackTrack_snapshot) && ignoredRoutesList.contains(tempRoute)){
					System.out.println("inside temproute while");
					e=myIterator.next();
					tempRoute=route.toString()+e.eId;
				}*/
				
				if (!isVisited(e)) {
					// found an unexplored edge, explore it
					visit(e);
					
					if(myList.size()>1)
						if(v instanceof VPDataNode){
							prevFF=currFF;
							currFF=(VPDataNode)v;
						}	
					
					VPNode w = graph.getOpposite(v, e);
					if (!isVisited(w)) {
						// w is unexplored, this is a discovery edge
						traverseDiscovery(e, v);
						if (isDone()) break;
						visitResult = dfsTraversal(w); // get result from DFS-tree child
						if (isDone()) break;
					}
					else {
						// w is explored, this is a back edge
						traverseBack(e, v);
						if (isDone()) break;
					}
				}
			}
		}
		if(!isDone())
			finishVisit(v);
		return result();
	}
}