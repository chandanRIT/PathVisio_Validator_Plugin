package org.pathvisio.plugins;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;

import org.apache.commons.collections15.Transformer;
import org.pathvisio.core.Engine;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.core.view.VPathwayEvent;
import org.pathvisio.core.view.VPathwayListener;

import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Graph;

class NAP_Utility {
	static String[] algComboBoxList = new String[]{"Dijkstra Algorith for the shortest path",
		"DFS based Cycle Finding","Minimum Spanning Tree","To add"};
	static Engine engine;
	static StringBuilder stb;
	static GraphAnalysisPlugin GAplugin;
	
	static List<JPEdge> wrapperForGraphIncidentEdges(Graph<VPNode, JPEdge> graph, VPNode vpn, int ffIndex){
		List<JPEdge> list=new ArrayList<JPEdge>(graph.getIncidentEdges(vpn));
		Collections.sort(list);
		return list.subList(ffIndex, list.size());
	}
	
	static List<JPEdge> alg_shortestPath_DA(Graph<VPNode, JPEdge> vpgraph,VPNode n1, VPNode n2 ){
		DijkstraShortestPath<VPNode,JPEdge> alg = new DijkstraShortestPath<VPNode, JPEdge>(vpgraph);
		List<JPEdge> l = alg.getPath(n1, n2);
		System.out.println("The shortest unweighted path from " + n1 +" to " + n2 + " is:"+l);

		if(l.isEmpty()) {
			stb.append("Algorithm Analysis : nodes not connected");
			return l;
		}
		else stb.append("Algorithm Analysis completed successfully!");

		//pickup the graphIds connected to the edges
		HashSet<String> graphIDs = new HashSet<String>();
		for (JPEdge jpe:l){
			//if(!graphIDs.contains(jpe.nodesConnectedTo[0]))
			graphIDs.add(jpe.nodesConnectedTo[0]);
			//if(!graphIDs.contains(jpe.nodesConnectedTo[1]))
			graphIDs.add(jpe.nodesConnectedTo[1]);
		} 

		//highlight all the listed nodes in blue
		highlightNodes(graphIDs);
		//for highlighting the source and destination. nodes in a different color
		highLightNode(n1.graphId,Color.green);
		highLightNode(n2.graphId,Color.red);
		//remember to redraw after the last highlight  
		engine.getActiveVPathway().redraw();

		System.out.println("shortest path nodes:"+graphIDs);
		return l;
	}

	static ArrayList<Position> alg_DFS_cycleFinding(FindCycleDFS alg_findCycle,Graph<VPNode, JPEdge> graph, VPNode vpn1){
		JPEdge jpe;
		if(GAplugin.alg_findCycle==null){
			GAplugin.alg_findCycle=new FindCycleDFS();
			//System.out.println("alg DFS instance");
		}
		ArrayList<Position> l = GAplugin.alg_findCycle.execute(graph, vpn1,null);
		//System.out.println("The cycle path for " + vpn1 +" is:"+l);

		if(l.isEmpty()) {
			stb.append("Algorithm Analysis : No cycle detected");
			return l;
		}
		else stb.append("Algorithm Analysis completed successfully!");

		//pickup the graphIds connected to the edges
		HashSet<String> graphIDs = new HashSet<String>();
		for (Position pos:l){
			if(pos instanceof JPEdge){
				jpe=(JPEdge)pos;
				//if(!graphIDs.contains(jpe.nodesConnectedTo[0]))
				graphIDs.add(jpe.nodesConnectedTo[0]);
				//if(!graphIDs.contains(jpe.nodesConnectedTo[1]))
				graphIDs.add(jpe.nodesConnectedTo[1]);

			} 
		}
		//highlight all the listed nodes in blue
		highlightNodes(graphIDs);
		
		//for highlighting the source and destination. nodes in a different color
		//highLightNode(vpn1.graphId,Color.green);
		//highLightNode(n2.graphId,Color.red);
		
		engine.getActiveVPathway().redraw();

		System.out.println("Cycle nodes:"+graphIDs);
		return l;
	}

	static void alg_minSpanForest(Graph<VPNode, JPEdge> graph, VPNode vpn){
		//VPNode vpn=findNodebyGId(tf1.getText());
		MinimumSpanningForest<VPNode, JPEdge> msf= new MinimumSpanningForest<VPNode, JPEdge>
		(graph, new DelegateForest<VPNode, JPEdge>() ,vpn);
		System.out.println("min sapnning tree nodes: " + msf.getForest().getVertices());
	}

	private static void highLightNode(String gId,Color color){
		PathwayElement pwe;
		VPathwayElement vpwe;
		pwe = engine.getActivePathway().getElementById(gId);

		if(pwe!=null) {
			vpwe=engine.getActiveVPathway().getPathwayElementView(pwe);
			vpwe.highlight(color);

		}else System.out.println("pwe is null for id "+gId);

	}

	private static void highlightNodes(HashSet<String> nodes){
		Pathway pw = engine.getActivePathway();
		VPathway vpw = engine.getActiveVPathway();
		PathwayElement pwe;
		VPathwayElement vpwe;

		for(String node:nodes){
			pwe = pw.getElementById(node);
			if(pwe!=null) {
				vpwe=vpw.getPathwayElementView(pwe);
				vpwe.highlight(Color.blue);

			} else System.out.println("pwe is null for id "+node);
		}
		//engine.getActiveVPathway().redraw();
	}

	static Object[] findMaxDegreeAndMapNodes(Graph<VPNode, JPEdge> graph,ArrayList<VPNode> nodes){
		int maxdegree=0,degree;
		Map<VPNode, Integer> nodeToDegreeMap=new HashMap<VPNode, Integer>();

		for(VPNode node : nodes){

			if(node instanceof VPDataNode){
				degree=graph.degree(node);

				if(maxdegree <= degree){ // <= , to include all the nodes with the max degree
					maxdegree=degree;
					nodeToDegreeMap.put(node, degree);
				}

			}

		}

		return new Object[]{(Integer)maxdegree,nodeToDegreeMap};// 1.) max degree value 2.) node2degree map
	}

	static void findAndDisplayNodesWithMaxDegree(Object[] inputFMD){
		int maxD=(Integer)inputFMD[0];
		stb.append("\n\nNodes with the Maximum Degree: "+maxD);

		StringBuilder tempStb=new StringBuilder();

		for( Map.Entry<VPNode, Integer> entry : ( (Map<VPNode, Integer>)inputFMD[1]).entrySet() ){
			if(entry.getValue()== maxD){
				tempStb.append(entry.getKey().graphId+" ");
			}
		}

		stb.append("\n Nodes: "+tempStb.toString());

	}

	static void computeAndDisplayStatistics(Graph<VPNode, JPEdge> graph , 
			VPNode vpn1, VPNode vpn2, ArrayList<VPNode> vpnodes){
		//StringBuilder stb=new StringBuilder();
		String gId1=vpn1.graphId,gId2=vpn2.graphId;

		//for the 2 input nodes

		//degree
		stb.append("\n\nDegree:");
		stb.append("\n node1: "+gId1+", degree: "+graph.degree(vpn1));
		stb.append("\n node2: "+gId2+", degree: "+graph.degree(vpn2));

		//closeness centrality for the 2 nodes
		ClosenessCentrality<VPNode, JPEdge> cs = new ClosenessCentrality<VPNode, JPEdge>(graph);
		stb.append("\n\nCloseness Cenratlity score:");
		stb.append("\n node1: "+gId1+", score:"+cs.getVertexScore(vpn1));
		stb.append("\n node2: "+gId2+", score:"+cs.getVertexScore(vpn2));

		//distance statistics
		stb.append("\n\nAverage Distances:");
		Transformer<VPNode, Double> tfr = DistanceStatistics.averageDistances(graph);
		stb.append("\n node1: "+gId1+", Avg.dist:"+tfr.transform(vpn1));
		stb.append("\n node2: "+gId2+", Avg.dist:"+tfr.transform(vpn2));

		//Neighbor count
		stb.append("\n\nNeighbor count:");
		stb.append("\n node1: "+vpn1.graphId+", count: "+graph.getNeighborCount(vpn1));
		stb.append("\n node2: "+vpn2.graphId+", count: "+graph.getNeighborCount(vpn2));

		//connectivity Test
		/*stb.append("\n\nConnectivityTest:");
		stb.append("\n connectivity Test result: "+new ConnectivityDFS().execute(graph, vpn1, null));
		 */

		//page rank score
		/*stb.append("\n\n Page Rank score:");
		PageRank<VPNode, JPEdge> pr= new PageRank<VPNode, JPEdge>(graph, 1);
		stb.append("\n node1: "+vpn1.graphId+", score:"+pr.getVertexScore(vpn1));
		stb.append("\n node2: "+vpn2.graphId+", score:"+pr.getVertexScore(vpn2));
		 */

		//for the entire network ( all the nodes and edges )

		//diameter
		stb.append("\n\nDiameter of the graph: "+DistanceStatistics.diameter(graph));

		//Nodes with the max degree
		findAndDisplayNodesWithMaxDegree(findMaxDegreeAndMapNodes(graph, vpnodes));
	}

	static class VPWListener implements VPathwayListener,FocusListener{
		private JTextField jtfLastFocussed=GAplugin.tf1;
		
		public void vPathwayEvent(VPathwayEvent e) {
			org.pathvisio.core.view.MouseEvent me;
			if( ((me=e.getMouseEvent())!=null) && 
					me.getType()==org.pathvisio.core.view.MouseEvent.MOUSE_DOWN ){
				//System.out.println("Pathway area clicked");
				VPathwayElement vpwe;
				if((vpwe=e.getAffectedElement())!=null && vpwe instanceof Graphics){
						jtfLastFocussed.setText(((Graphics)vpwe).getPathwayElement().getGraphId());
				}
				GAplugin.selectedVPwe=vpwe;
			}
		}

		public void focusGained(FocusEvent arg0) {
			//System.out.println("focus on jtfs");
			jtfLastFocussed=(JTextField)arg0.getSource();
		}

		public void focusLost(FocusEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

}


