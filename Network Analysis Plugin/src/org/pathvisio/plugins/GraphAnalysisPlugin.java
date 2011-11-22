package org.pathvisio.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.view.VPathwayElement;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class GraphAnalysisPlugin implements Plugin , ApplicationEventListener {

	private PvDesktop desktop;
	JTextField tf1=new JTextField();
	private JTextField tf2=new JTextField();
	private JComboBox alBox;
	private JButton runButton;
	private JTextArea resultsArea;//,result2;
	private StringBuilder stb;
	private File currentPathwayFile;
	private Engine engine;
	private JFrame frame;
	private NAP_Utility.VPWListener vpwListener;
	VPathwayElement selectedVPwe;
	private Graph<VPNode, JPEdge> createdJGraph;

	private ArrayList<VPNode> vpnodes;// = new ArrayList<VPNode>();
	PathwayXMLHandler sh;// = new PathwayXMLHandler(vpnodes);
	private SAXParser saxParser;

	// variables used in generating JGraph output file
	private File jGFile;
	//private FileWriter jGOfw;
	//private BufferedWriter jGObw;
	private JCheckBox jcb1;
	private GraphMLWriter<VPNode, JPEdge> jGOgw;
	FindCycleDFS alg_findCycle;

	public void init(PvDesktop pvdesktop) {
		System.out.println("JPlugin init called");

		desktop=pvdesktop;
		engine=desktop.getSwingEngine().getEngine();

		engine.addApplicationEventListener(this);

		// initialise string buffer stb
		stb=new StringBuilder();

		initNAP_Utility();
		
		//registering listener for an already open pathway (auto-saved pathway)
		vpwListener=new NAP_Utility.VPWListener();
		if(engine.hasVPathway())
			engine.getActiveVPathway().addVPathwayListener(vpwListener);

		createJPluginUI();

		//initialize variables for generating JGraph output file
		jGFile = new File(System.getProperty("user.home"),//JGraph File
		"JPluginGraph.xml");
		jGOgw= new GraphMLWriter<VPNode, JPEdge>();

		//create the file for PW export
		currentPathwayFile=new File(System.getProperty("java.io.tmpdir"),
		"JPluginExportedPathway.xml");
		//currentPathwayFile.deleteOnExit();
		
	}

	public void done() {
		// TODO Auto-generated method stub
		System.out.println("plugin closed");
	}

	private void initNAP_Utility(){
		NAP_Utility.engine=engine;
		NAP_Utility.stb=stb;
		NAP_Utility.GAplugin=this;
	}

	private void generateJGraphOutputFile(Graph<VPNode, JPEdge> jGraph){
		FileWriter jGOfw;
		BufferedWriter jGObw;
		if (jcb1.isSelected()) {
			try {
				// a new object has to be created every time, since save method below closes the streams
				jGOfw=new FileWriter(jGFile);
				jGObw=new BufferedWriter(jGOfw);
				jGOgw.save(jGraph, jGObw);
			} catch (IOException e) {
				System.out.println("exception in file for graph output");
				e.printStackTrace();
			}
		} else System.out.println("JoutputFile deleted or not:"+jGFile.delete());

	}

	private VPNode findNodebyGId(String gid){
		if(gid==null) System.out.println("gid passed to findNode is null");

		for(VPNode vpn:vpnodes){
			if(vpn.graphId==null || vpn.graphId.equals("")) System.out.println(vpn+" has null or empty id");
			if(gid.equals(vpn.graphId)){
				System.out.println("node found");
				return vpn;
			} 
		}

		System.out.println("node not found in findNode for "+gid);
		return null;
	}

	private Graph<VPNode, JPEdge> createJGraphFromPWFile(File pwExportedFile){//create Graph from Pathway
		Graph<VPNode, JPEdge> vpgraph = new SparseGraph<VPNode, JPEdge>();
		if(vpnodes==null) vpnodes = new ArrayList<VPNode>();
		if (sh==null) sh = new PathwayXMLHandler(vpnodes);

		//resetting stuff
		VPLine.count=0;VPDataNode.count=0;VPAnchor.count=0;JPEdge.count=0;
		vpnodes.clear();
		resultsArea.setText("");

		try{
			if(saxParser==null) 
				saxParser=SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(pwExportedFile, sh);

		} catch(Exception e){
			System.out.println("exception in sax");
			e.printStackTrace();
		}

		//System.out.println("vpodes list :"+vpnodes);

		String gId2;
		VPNode vpcn;
		for(VPNode vpn:vpnodes){
		//System.out.print(" " +vpn);
			if(vpn instanceof VPLine) {
				vpgraph.addVertex(vpn);
				try{
					vpgraph.addEdge(new JPEdge(vpn.graphId, gId2=( (VPLine)vpn ).graphRef[0]), vpn, findNodebyGId(gId2));
					vpgraph.addEdge(new JPEdge(vpn.graphId, gId2=( (VPLine)vpn ).graphRef[1]), vpn, findNodebyGId(gId2));
				}catch(Exception e){
					System.out.println("Exception in adding edges to line");
					e.printStackTrace();
					//continue;
				}
			}
			else if(vpn instanceof VPAnchor){
				//not needed to add the vertex since automatically added by the addEdge method 
				vpcn=( (VPAnchor)vpn ).connectedToLine;

				vpgraph.addVertex(vpn);
				vpgraph.addEdge(new JPEdge(vpn.graphId, vpcn.graphId), vpn, vpcn);
			}
			else if(vpn instanceof VPDataNode){
				vpgraph.addVertex(vpn);
			}
		}
		//System.out.println();
		return vpgraph;
	}

	private void createAndDisplayJGraphUI(Graph<VPNode, JPEdge> vpgraph){

		// The Layout<V, E> is parameterized by the vertex and edge types
		Layout<VPNode, JPEdge> layout = new CircleLayout<VPNode, JPEdge>(vpgraph);
		layout.setSize(new Dimension(950,750)); // sets the initial size of the space

		// The BasicVisualizationServer<V,E> is parameterized by the edge types
		BasicVisualizationServer<VPNode,JPEdge> vv =
			new BasicVisualizationServer<VPNode,JPEdge>(layout);
		vv.setPreferredSize(new Dimension(1000,800)); //Sets the viewing area size
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<VPNode>());
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<JPEdge>());
		//vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		if(frame==null){
			frame = new JFrame("Simple Graph View");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(vv);
			frame.pack();
			frame.setVisible(true);
		}
		else {
			frame.getContentPane().remove(0);
			frame.getContentPane().add(vv);
			frame.pack();
			//frame.setVisible(true);
		}

	}

	private boolean checkForGIdValidity(VPNode vpn1,VPNode vpn2){

		String failMessage="invalid GraphId in";
		boolean failFlag=false;
		if(vpn1==null){
			failMessage+=" input1";
			failFlag=true;
			//return;
		}
		if(vpn2==null){
			failMessage+=" input2";
			failFlag=true;
			//return;
		}

		if(failFlag) {
			JOptionPane.showMessageDialog(
					desktop.getFrame(),	failMessage,"JPlugin",JOptionPane.INFORMATION_MESSAGE);

			return false;
		}
		return true;
	}

	private void createGraphAndrunAlgorithm(int alBoxselection, String input1, String input2){

		if(! engine.hasVPathway() ){
			JOptionPane.showMessageDialog(
					desktop.getFrame(), "Pathway not open","JPlugin",JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		//reset StringBuilder and highlight
		stb.setLength(0);
		engine.getActiveVPathway().resetHighlight();
		if(selectedVPwe!=null)
			selectedVPwe.deselect();

		//if export below fails , then return
		if(!exportPathwayToXML()) return; 
		//if(createdJGraph==null)
		createdJGraph=createJGraphFromPWFile(currentPathwayFile);
		createAndDisplayJGraphUI(createdJGraph);
		generateJGraphOutputFile(createdJGraph);

		VPNode vpn1=findNodebyGId(input1), vpn2=findNodebyGId(input2);
		if(!checkForGIdValidity(vpn1, vpn2)) return;

		switch(alBoxselection){
		case 0:	NAP_Utility.alg_shortestPath_DA(createdJGraph, vpn1, vpn2);
		break;
		case 1:	NAP_Utility.alg_DFS_cycleFinding(alg_findCycle,createdJGraph,vpn1);
		break;
		case 2: NAP_Utility.alg_minSpanForest(createdJGraph,vpn1);
		break;
		default:
			System.out.println("pressed another");
		}

		NAP_Utility.computeAndDisplayStatistics(createdJGraph,vpn1,vpn2,vpnodes);

		resultsArea.setText(stb.toString());
	}

	private boolean exportPathwayToXML(){
		try {
			GpmlFormat.writeToXml (engine.getActivePathway(), currentPathwayFile, true);
		} catch (Exception e) {
			System.out.println("GPML conversion Exception");
			e.printStackTrace();

			JOptionPane.showMessageDialog(
					desktop.getFrame(), "GPML conversion Error (unable to covert pathway into XML formatted file )",
					"JPlugin",JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return true;
	}

	// this method envelopes the actual method to catch any runtime or uncaught exceptions
	private void runAction(int sel, String input1, String input2){   
		try{
			createGraphAndrunAlgorithm(sel, input1, input2);
		}catch(Exception e){
			JOptionPane.showMessageDialog(
					desktop.getFrame(), "Exception occured during run",
					"JPlugin",JOptionPane.INFORMATION_MESSAGE);

			e.printStackTrace();
		}
	}

	// all the actionPerformed() events are delegated to this method 
	private void JPActionListenerMethod(ActionEvent e){
		if(e.getSource() instanceof JTextField || "run".equals(e.getActionCommand()) ){
			//System.out.println("text field event");
			runAction(alBox.getSelectedIndex(), tf1.getText().trim(), tf2.getText().trim());
		}

		/*else if("run".equals(e.getActionCommand())){
			System.out.println("run button pressed");
			runAction(alBox.getSelectedIndex(), tf1.getText().trim(), tf2.getText().trim());
		}*/
	}

	private void createJPluginUI(){
		final JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
		Border etch = BorderFactory.createEtchedBorder();
		//CellConstraints cc = new CellConstraints();
		final GridBagConstraints c = new GridBagConstraints(), c2 = new GridBagConstraints(),c3=new GridBagConstraints();
		ActionListener aListener;

		JPanel mainPanel= new JPanel(new BorderLayout()), inputPanel = new JPanel(new GridBagLayout());
		inputPanel.setBorder (BorderFactory.createTitledBorder (etch, "Input Pane"));

		//add the input labels and the text-input-fields
		c.fill=GridBagConstraints.NONE;
		//c.gridx=2;
		//c.gridwidth=GridBagConstraints.RELATIVE;
		inputPanel.add (new JLabel (" input 1: "), c);

		c.weightx=0.1;
		c.fill=GridBagConstraints.HORIZONTAL;
		//c.gridwidth=GridBagConstraints.REMAINDER;
		inputPanel.add(tf1,c);
		tf1.addActionListener(aListener=new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPActionListenerMethod(e);			
			}
		});
		tf1.addFocusListener(vpwListener);

		c.weightx=0.0;
		c.fill=GridBagConstraints.NONE;
		//c.gridwidth=GridBagConstraints.RELATIVE;
		inputPanel.add (new JLabel ("  input 2: "), c);

		c.weightx=0.1;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridwidth=GridBagConstraints.REMAINDER;
		inputPanel.add(tf2,c);
		tf2.addActionListener(aListener);
		tf2.addFocusListener(vpwListener);

		//adding an empty row between the 2 rows in input panel
		JLabel spaceLabel=new JLabel("dssd");
		//spaceLabel.setBackground(Color.WHITE);
		spaceLabel.setForeground(inputPanel.getBackground());
		inputPanel.add(spaceLabel,c);

		JPanel insideInput= new JPanel(new GridBagLayout());

		//add comboBox to the insider in the input pane
		//c.weightx=0.0;
		c2.gridwidth=GridBagConstraints.RELATIVE;
		c2.fill=GridBagConstraints.NONE;
		insideInput.add(alBox=new JComboBox(NAP_Utility.algComboBoxList),c2);
		alBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				int selection=alBox.getSelectedIndex();

				if(selection==3){
					tf1.setEnabled(false);tf2.setEnabled(false);
				}
				else {
					tf1.setEnabled(true);tf2.setEnabled(true);
				} 


			}
		});

		//add run button to the insider pane in input pane
		c2.weightx=0.1;
		c2.fill=GridBagConstraints.HORIZONTAL;
		c.gridwidth=GridBagConstraints.REMAINDER;
		insideInput.add (runButton=new JButton("Run"), c2);
		runButton.addActionListener(aListener);
		runButton.setActionCommand("run");

		c2.weightx=0;
		inputPanel.add(insideInput,c);

		//add a checkbox to the input pane
		//JCheckBox jcb1;
		inputPanel.add(jcb1=new JCheckBox("generate JGraph output"),c);
		jcb1.setToolTipText("generated in "+System.getProperty("user.home")+System.getProperty("file.separator")+"JPluginGraph.xml");

		/* spaceLabel=new JLabel("dssd");
		//spaceLabel.setBackground(Color.WHITE);
		spaceLabel.setForeground(inputPanel.getBackground());
		inputPanel.add(spaceLabel,c);*/


		//////////// lower panel containing the text area//////////////
		JPanel resultsPanel= new JPanel(new GridBagLayout());
		resultsPanel.setBorder(BorderFactory.createTitledBorder(etch,"results Pane"));
		c3.weightx=1;c3.weighty=1;
		c3.fill=GridBagConstraints.BOTH;
		//c2.gridwidth=GridBagConstraints.RELATIVE;
		resultsPanel.add(resultsArea=new JTextArea(),c3);
		resultsArea.setEditable(false);
		//resultsArea.setOpaque(false);
		//resultsPanel.add(result2=new JLabel());

		mainPanel.add(inputPanel,BorderLayout.NORTH);
		mainPanel.add(resultsPanel,BorderLayout.CENTER);
		sidebarTabbedPane.add("Jplugin",mainPanel);

		sidebarTabbedPane.setSelectedComponent(mainPanel);
	}

	public void applicationEvent(ApplicationEvent e) {

		if( e.getType()==ApplicationEvent.PATHWAY_OPENED || e.getType()==ApplicationEvent.PATHWAY_NEW){
			System.out.println("another pathway opened");
			resultsArea.setText("");tf1.setText("");tf2.setText("");
			engine.getActiveVPathway().addVPathwayListener(vpwListener);
		}

	}

}