package org.pathvisio.plugins;

/** This class specializes DFS to determine whether the graph is connected. */
public class ConnectivityDFS extends DFS < Object, Boolean> {
  protected int reached;
  protected void setup() { reached = 0; }
  protected void startVisit(VPNode v) { reached++; }
  protected Boolean finalResult(Boolean dfsResult) { 
    return new Boolean(reached == graph.getVertexCount());
  }
}