package org.pathvisio.plugins;
import java.util.ArrayList;


/** This class specializes DFS to find a cycle. */
public class FindCycleDFS 
extends DFS< Object, ArrayList<Position>> {
	protected ArrayList<Position> cycle; // sequence of edges of the cycle
	protected boolean done;
	protected VPNode cycleStart;
	
	public void setup() { 
		cycle = new ArrayList<Position>();
		done = false;
	}
	
	protected void startVisit(VPNode v) {
		cycle.add(v); 
	}
	
	protected void finishVisit(VPNode v) {
		cycle.remove(cycle.size()-1);	// remove v from cycle
		if (!cycle.isEmpty()) cycle.remove(cycle.size()-1); // remove edge into v from cycle
	}
	
	protected void traverseDiscovery(JPEdge e, VPNode from) { 
		cycle.add(e); 
	}
	
	protected void traverseBack(JPEdge e, VPNode from) {
		cycle.add(e);		// back edge e creates a cycle
		cycleStart = graph.getOpposite(from, e);
		cycle.add(cycleStart);	// first vertex completes the cycle
		done = true;
	}
	
	protected boolean isDone() {
		return done; 
	} 
	
	public ArrayList<Position> finalResult(ArrayList<Position> r) {
		// remove the vertices and edges from start to cycleStart
		ArrayList<Position> tempCycle= new ArrayList<Position>(cycle);
		if (!cycle.isEmpty()) {
			for (Position p: tempCycle) {
				if (p == cycleStart)
					break;
				cycle.remove(p);                     // remove vertex from cycle
			}
		}
		return cycle; // list of the vertices and edges of the cycle 
	}
}