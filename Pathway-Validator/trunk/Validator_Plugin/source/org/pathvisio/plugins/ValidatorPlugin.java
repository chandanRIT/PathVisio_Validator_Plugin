package org.pathvisio.plugins;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.view.VPathwayElement;
import org.xml.sax.SAXException;
import edu.stanford.ejalbert.BrowserLauncher;
import gov.nih.nci.lmp.mimGpml.MIMFormat;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;

public class ValidatorPlugin implements Plugin,ActionListener, ApplicationEventListener,
										ItemListener, ComponentListener
{
	PvDesktop desktop;
	//private JButton valbutton;
	//private final static JEditorPane jta=new JEditorPane("text/html","");
	private static File  schemaFile;
	private static File currentPathwayFile;
	//private JPanel mySideBarPanel ;
	//private JScrollPane scrollPane;
	static VPathwayElement prevPwe;
	private static Engine eng;
	static Pathway pth;
	private static Color col1;//= new Color(255,0,0),col2=new Color(0,0,255);
	static Color col2;
	//private final  SchematronTask st=new SchematronTask();
	private  static SaxonTransformer saxTfr ;
	private  static MIMFormat mimf;//=new MIMFormat();
	private static JFileChooser chooser;
	private final static JButton valbutton=new JButton("Validate");
	private static int errorCounter;
	static int prevSelect;
	private final static JButton chooseSchema=new JButton("Choose Ruleset"); 
	private final static JComboBox jcBox = new JComboBox(new String[]{"Errors & Warnings","Errors only","Warnings only"});
	private final static JComboBox phaseBox = new JComboBox(new String[]{"Phase: All"});
	private final ValidatorHelpAction vhelpAction= new ValidatorHelpAction();
	static boolean prevHighlight=true;
	private static JCheckBox jcb;
    final JLabel eLabel=new JLabel("Errors:0",new ImageIcon(getClass().getResource("/error.png")),SwingConstants.CENTER);
	final JLabel wLabel=new JLabel("Warnings:0",new ImageIcon(getClass().getResource("/warning.png")),SwingConstants.CENTER);
    static final JTextField schemaTitleTag= new JTextField("Schema Title: ");
	//private static boolean doExport=false;
	private static String schemaFileType;
	//private static Thread threadForSax;
	private static GroovyObject grvyObject;
	static ArrayList<Object> globGroovyResult;
	//private static int phaseBoxSelection=0;
	private static boolean changeOfSchema=false;
	final MyTableModel mytbm=new MyTableModel();
	final JCheckBox svrlOutputChoose= new JCheckBox(" Generate SVRL file",false);
	private GroovyValidator groovyValidator;
	private ValidatorPlugin vPlugin=this;
	
	final JTable jtb = new JTable(mytbm){
    	public Class getColumnClass(int column){  
        return getValueAt(0, column).getClass();  
    	}  
    };
	
    ImageIcon EIcon;
	ImageIcon WIcon;
	private final ArrayList<String> graphIdsList = new ArrayList<String>();
	ArrayList<String> ignoredErrorTypesList;
	ArrayList<String> ignoredElements;
	ArrayList<String> ignoredSingleError;
	private JPopupMenu popup;
	private JMenu subMenu4,subMenu5,subMenu6;
	boolean allIgnored;
    private int[] checkedUnchecked;
    String schemaString;
	
	public ValidatorPlugin(){
		
		 System.out.println("init callled");
		 //errorCounter=0;prevSelect=0;
		 //jta=new JEditorPane("text/html","");
		 //schemaFile=null;
		 //currentPathwayFile=null;
		 //private JPanel mySideBarPanel ;
		 //private JScrollPane scrollPane;
		 //col1=new Color(255,0,0);col2=new Color(0,0,255);
		 //private final  SchematronTask st=new SchematronTask();
		 //saxTfr = new SaxonTransformer();
		 //mimf=new MIMFormat();
		 //chooser=new JFileChooser();
		 //valbutton=new JButton("Validate");
		 //chooseSchema=new JButton("Choose Ruleset"); 
		 //helloAction = new HelloAction();
	}
	
	enum SchemaPreference implements Preference
    {
            LAST_OPENED_SCHEMA_DIR (System.getProperty("user.home")),CHECK_BOX_STATUS ("0");
            
            private String defaultValue;
            SchemaPreference (String defaultValue) 
            {
                    this.defaultValue = defaultValue;
            }
            
            public String getDefault() {
                    return defaultValue;
            }               
    }
	
	public void init(PvDesktop desktop) 
	{   
		// save the desktop reference so we can use it later
		this.desktop = desktop;
		eng=desktop.getSwingEngine().getEngine();
		eng.addApplicationEventListener(this); // To listen to the events from the engine (eg. Pathaway-opened event) 
		
		// register Validator help page action in the "Help" menu.
		desktop.registerMenuAction ("Help", vhelpAction);
		
		createPluginUIAndTheirListeners();
		
        //code for setting the winx and winy of main window of pathvisio in preference file
        //PreferenceManager.getCurrent().set(GlobalPreference.WIN_X, "0");
        //PreferenceManager.getCurrent().set(GlobalPreference.WIN_Y,"0");
        
		//initialization code for currentPathwayFile object
        if(currentPathwayFile==null){
			//comment the below line for normal jfile chooser functionality
			//schemaFile=new File("D:\\schematron\\mimschema.sch");
			//commented below to remove  hardcode
			//currentPathwayFile=new File("C:\\Users\\kayne\\Desktop\\currentPathwaytmp.mimml");
			try {
				//currentPathwayFile=File.createTempFile("pvv","val");
				currentPathwayFile=new File(System.getProperty("java.io.tmpdir"), "ValidatorPluginExportedPathway.xml");
				currentPathwayFile.deleteOnExit();
			} catch (Exception e) {
				System.out.println("Exception in creating current pathway temp file "+e);
				e.printStackTrace();
			}
		}
        
	}
	
	public void done() {
		System.out.println("unloading plugin");
		//SaxonTransformer.getSvrlFile().deleteOnExit();
		
	}
	
	/**
	 * Almost all the UI related objects are created or assigned listeners here.
	 */
	private void createPluginUIAndTheirListeners(){
		
		schemaTitleTag.setEditable(false);
		schemaTitleTag.addComponentListener(this);
		
		//creating a button for choosing the schema file 
		//chooseSchema=new JButton("Choose Ruleset");
		chooseSchema.setActionCommand("choose");
		chooseSchema.addActionListener(this);
		
		//creating a jcheckbox ("Highlight All") and set its status from the .pathvisio pref file
	    boolean jbcinit;
		if(PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS)==1){
	    	jbcinit=true;
	    }else jbcinit=false;
		
		jcb= new JCheckBox(" Highlight All", jbcinit);
		jcb.setActionCommand("jcb");
		jcb.addActionListener(this);
		jcb.setEnabled(false);//set to false , to enable it only when validate is pressed
		//valbutton.setEnabled(!jbcinit);
		
		svrlOutputChoose.setActionCommand("svrlOutputChoose");
		svrlOutputChoose.addActionListener(this);
		svrlOutputChoose.setEnabled(false);
		
		//final JComboBox jcBox = new JComboBox(new String[]{"Errors & Warnings","Errors only","Warnings only"});
		jcBox.setActionCommand("jcBox");
		jcBox.addActionListener(this);
		jcBox.setEnabled(false);
		//jcBox.setSelectedIndex(1);
		
		phaseBox.setActionCommand("phaseBox");
		//phaseBox.addActionListener(this);
		phaseBox.addItemListener(this);
		phaseBox.setEnabled(false);
		
		//adding labels for warning and error counts
		eLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		wLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		//jtable settings
		jtb.setTableHeader(null);
	    jtb.setFont(new Font("Verdana", Font.PLAIN, 15));
	    //jtb.setGridColor(Color.WHITE);
	    
	    mytbm.addColumn("image"); 
	    mytbm.addColumn("errors and warnings");
	    
	    jtb.getColumn("image").setMaxWidth(23);
	    
	    jtb.addMouseListener(new java.awt.event.MouseAdapter()
	    {
	    	public void mouseClicked(java.awt.event.MouseEvent e){
	    		listenToJTableMouseClicks(e);
	    	}
	    });

	    jtb.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer(graphIdsList));
	    
	    //create a scrollpane and adding jtable to the pane
		final JScrollPane scrollPane = new JScrollPane(jtb);
		scrollPane.getViewport().setBackground(Color.WHITE);
		//scrollPane.setOpaque(true);
		//scrollPane.setForeground(Color.white);
		//scrollPane.setColumnHeader(null);
		
		//code for layout of the components in JPanel goes here
		final JPanel mySideBarPanel = new JPanel (new GridBagLayout());
		//Font f=new Font("Times New Roman", Font.BOLD, 16);
		final GridBagConstraints c = new GridBagConstraints();
        
        c.weighty = 0.0;c.weightx=0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        //c.gridwidth = GridBagConstraints.RELATIVE;
        mySideBarPanel.add(eLabel,c);
        mySideBarPanel.add(wLabel,c);
        
        c.fill=GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mySideBarPanel.add(svrlOutputChoose,c);
        
        c.fill=GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        mySideBarPanel.add(schemaTitleTag,c);
        
        //c.fill=GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mySideBarPanel.add(phaseBox,c);
        
        //c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        mySideBarPanel.add(scrollPane,c);
        
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        mySideBarPanel.add(jcb,c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        mySideBarPanel.add(jcBox,c);
       
        //final JButton valbutton=new JButton("validate");
        valbutton.setActionCommand("validate");
        valbutton.addActionListener(this);
        
        c.gridwidth=GridBagConstraints.RELATIVE;
        mySideBarPanel.add(valbutton,c);
        mySideBarPanel.add(chooseSchema,c);
        
        // get a reference to the sidebar and add the jpanel created above, to it
        final JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        sidebarTabbedPane.add("Validator", mySideBarPanel);
        sidebarTabbedPane.setSelectedComponent(mySideBarPanel);
    	
	}
	
	private void createAndInitialize_RightClickMenuUI(){
		
		ignoredErrorTypesList=new ArrayList<String>();
		ignoredElements=new ArrayList<String>();
		ignoredSingleError=new ArrayList<String>();
		checkedUnchecked = new int[6];
		
		//The 2 icon objects below are used wherever the icon images appear in the UI
		EIcon = new ImageIcon(getClass().getResource("/error.png"));
		WIcon = new ImageIcon(getClass().getResource("/warning.png"));
		
		popup = new JPopupMenu("filter");// popup named "filter"
		
		JMenuItem menuItem123= new CustomMenuItem("Ignore Element");
		menuItem123.addActionListener(this);
		menuItem123.setActionCommand("menuItem1");
		popup.add(menuItem123);
		
		menuItem123 = new CustomMenuItem("Ignore this Error/Warning");
		menuItem123.setActionCommand("menuItem2");
		menuItem123.addActionListener(this);
		popup.add(menuItem123);
		
		menuItem123 = new CustomMenuItem("Ignore this Error/Warning Type");
		menuItem123.setActionCommand("menuItem3");
		menuItem123.addActionListener(this);
		popup.add(menuItem123);
		popup.addSeparator();
		
		ImageIcon img=new ImageIcon(getClass().getResource("/ignore.png"));
		subMenu4= new JMenu("Ignored Error/Warning Types");
		subMenu4.setIcon(img);
		
		subMenu5= new JMenu("Ignored Elements");
		subMenu5.setIcon(img);
		
		subMenu6= new JMenu("Ignored Errors/Warnings");
		subMenu6.setIcon(img);
		
		JMenuItem subMenuItemOkButton=new CustomMenuItem( "Reconsider (Un-Ignore)");//	new ImageIcon(getClass().getResource("/ignore.png")) );
		subMenuItemOkButton.setActionCommand("subMenu4ReConsider");
		subMenuItemOkButton.addActionListener(this);
		subMenuItemOkButton.setEnabled(false);
		subMenu4.add(subMenuItemOkButton);
		
		subMenuItemOkButton=new CustomMenuItem( "Reconsider (Un-Ignore)");//	new ImageIcon(getClass().getResource("/ignore.png")) );
		subMenuItemOkButton.setActionCommand("subMenu5ReConsider");
		subMenuItemOkButton.addActionListener(this);
		subMenuItemOkButton.setEnabled(false);
		subMenu5.add(subMenuItemOkButton);
		
		subMenuItemOkButton=new CustomMenuItem( "Reconsider (Un-Ignore)");//	new ImageIcon(getClass().getResource("/ignore.png")) );
		subMenuItemOkButton.setActionCommand("subMenu6ReConsider");
		subMenuItemOkButton.addActionListener(this);
		subMenuItemOkButton.setEnabled(false);
		subMenu6.add(subMenuItemOkButton);
		
		subMenu4.addSeparator();
		subMenu4.setEnabled(false);
		
		subMenu5.addSeparator();
		subMenu5.setEnabled(false);
		
		subMenu6.addSeparator();
		subMenu6.setEnabled(false);
		
		popup.add(subMenu5);
		popup.add(subMenu6);
		popup.add(subMenu4);
		
	}
	
	/**
	 * delegate method to listen to the mouse clicked events from the JTable in the JPanel 
	 * and handle them.
	 */
	private void listenToJTableMouseClicks(java.awt.event.MouseEvent e){
		
		//code for highlight node for a left/right click event
		int row=jtb.rowAtPoint(e.getPoint());
		if(prevPwe!=null && graphIdsList.size()!=0 ){ 

			if( ! prevPwe.isHighlighted() ){
				prevHighlight=false; 
			}

			if(prevHighlight){
				prevPwe.highlight(col2);//col2 is blue
			}
			else 
				prevPwe.unhighlight();

			String gId=graphIdsList.get(row);
			
			if(!gId.equals("null") && !gId.equals("")){
				//System.out.println("graphId is "+graphIdsList.get(row));
				PathwayElement	pe=pth.getElementById(gId);
				
				if(pe!=null){
					VPathwayElement	vpe=eng.getActiveVPathway().getPathwayElementView(pe);
					vpe.highlight(col1);//col1 is red
					//code to scroll to the particular element that's being highlighted
					eng.getActiveVPathway().getWrapper().scrollCenterTo((int)vpe.getVBounds().getCenterX(),(int)vpe.getVBounds().getCenterY());
					prevPwe=vpe;
					eng.getActiveVPathway().redraw();
				}
				
				//System.out.println("row # "+(row+1)+" pressed");
			} else System.out.println("graphId is null or empty");
			//System.out.println(" Value in the cell clicked :"+jtb.getValueAt(row,col).toString());
		}else System.out.println("prevPwe is null or no errors");

		//right click event handling is done below
		if (e.getButton()== MouseEvent.BUTTON3) {
			jtb.clearSelection();

			String eachCellTip = jtb.getToolTipText(e);
			boolean discardFirst2Menus=false;
			if(eachCellTip!=null) discardFirst2Menus=eachCellTip.equals("----");    			
			
			//decide which main menuitems (1,2,3) to show and which not to
			if(discardFirst2Menus || allIgnored){
				//System.out.println("inside if d a "+discardFirst2Menus+" "+allIgnored);
				if(discardFirst2Menus) 
					jtb.getSelectionModel().setSelectionInterval(row, row);

				//if(popup.getComponent(2).isEnabled())
				for(int MI=0; MI<3 ; MI++){
					if(MI==2 && discardFirst2Menus){
						popup.getComponent(MI).setEnabled(true); 
					}
					else
						popup.getComponent(MI).setEnabled(false); 
				}
			}	
			else {
				//System.out.println("inside else d a "+discardFirst2Menus+" "+allIgnored);
				jtb.getSelectionModel().setSelectionInterval(row, row);//to select the row with right click
				//if(!popup.getComponent(2).isEnabled())
				for(int MI=0; MI<3 ; MI++)
					popup.getComponent(MI).setEnabled(true); 
			}
			
			popup.show(e.getComponent(), e.getX(), e.getY());
		}

	}
	
	/**
	 * custom JTable class to override the the method "isCellEditable", in order to
	 *  render all the cells un-editable for the table 
	 */
	 class MyTableModel extends DefaultTableModel{
		public boolean isCellEditable(int row, int column){  
		    return false;  
		  }  
	}
	
	/**
	 * Open a Browser link to the plugin's help page when this action is triggered. 
	 */
	private class ValidatorHelpAction extends AbstractAction
	{
		ValidatorHelpAction()
		{
			// The NAME property of an action is used as 
			// the label of the menu item
			putValue (NAME, "Validator Help");
		}
		
		/**
		 *  called when the user selects the menu item
		 */
		public void actionPerformed(ActionEvent arg0) 
		{	
             try
             {
                 BrowserLauncher bl = new BrowserLauncher(null);
                 bl.openURLinBrowser("http://pathvisio.org/wiki/PathwayValidatorHelp");
             }
             catch (Exception ex)
             {
            	 JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
     					"could not launch the page","Validator Plugin",JOptionPane.ERROR_MESSAGE);
     			 ex.printStackTrace();
             }	
		}
	}

	/**
	 * "validatePathway" calls this method internally , a separate thread to do the task 
	 */
	public void processTask(ProgressKeeper pk, ProgressDialog d, SwingWorker<Object, Object> sw) {
		
		sw.execute();
		d.setVisible(true);
		
		try {
			 sw.get();
		} catch (ExecutionException e)
		{
			System.out.println("ExecutionException in ValidatorPlugin---"+e.getMessage());
			JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
					"Validation Exception in Schematron","Validator Plugin",JOptionPane.ERROR_MESSAGE);
			return;
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception---"+e.getCause());
			JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
					"Validation Exception in Schematron","Validator Plugin",JOptionPane.ERROR_MESSAGE);
			return ;
		}
	}

	/**
	 * method to carry out the validation task in background, while a progress bar runs in the foreground   
	 */
	public void validatePathway(final SaxonTransformer tempSaxTrnfr,final MIMFormat mimf){
		
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(desktop.getFrame(),"Validator plugin", pk, false, true);
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
		
			protected Object doInBackground() {
				pk.setTaskName("Validating pathway");
					
				try{
					exportAndValidate(tempSaxTrnfr, mimf);
				}
				catch (Exception e1) { //changed from ConverterException to catch all the errors
					//System.out.println("Exception in validatepathway method--"+e1.getMessage());
					JOptionPane.showMessageDialog(desktop.getFrame(), 
							"Validation Exception in Schematron","Validator Plugin",JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
					return null;
				}
				finally {
					pk.finished();
				}

				return null;
			}
		};

		 processTask(pk, d, sw);
	}
	
	private void exportAndValidate(final SaxonTransformer tempSaxTrnfr,final MIMFormat mimf) 
	throws ConverterException,IOException,ParserConfigurationException,
	TransformerException,SAXException{
		
			//if(!schemaFileType.equalsIgnoreCase("groovy")){
				System.out.println("b4 export called: "+schemaFileType);
				//if(!doExport){
				
				if(schemaFileType.equalsIgnoreCase("gpml")){
					GpmlFormat.writeToXml (eng.getActivePathway(), currentPathwayFile, true);
					System.out.println("gpml export called");
				}
				else {
					mimf.doExport(currentPathwayFile, eng.getActivePathway());
					System.out.println("mimVis export called");
				}

				//}
				//doExport=false;
				SaxonTransformer.setschemaFile(schemaFile);
				System.out.println("after mimf export and b4 execute");
				tempSaxTrnfr.produceSvrlAndThenParse();
				System.out.println("after  st.execute");
				printOnPanel();
			//}
			//else runGroovy(grvyObject);
	}
	
	private void printOnPanel(){
		prevHighlight=true;
		VPathwayElement vpe=null;
		PathwayElement pe; 
		String tempSt,combined,tempsubSt;pth=eng.getActivePathway();
        Iterator<String> tempIterator = (saxTfr.diagnosticReference).iterator();
        int i=0,j=0,k=0,eCount=0,wCount=0;
        //String imageUrl=imageUrlE;
        ImageIcon EWIcon=EIcon; 
        graphIdsList.clear();
        
        clearTableRows();
        int higco=0; 
        
        eng.getActiveVPathway().resetHighlight();//unhighlight all nodes
        
        while (tempIterator.hasNext()) {
         	errorCounter+=1;
         	tempSt=tempIterator.next();
         	combined=tempSt;
         	String[] splitString=tempSt.split("@@");
         	tempSt=splitString[1];
         	tempsubSt=splitString[0];
         	
         	//if(tempSt.compareTo("error - An interaction should not start and end with Line arrowheads.")==0) continue;
         	if(ignoredErrorTypesList.contains(tempSt)||
         			ignoredElements.contains(tempsubSt)|| ignoredSingleError.contains(combined)) 
         		continue;
         	
         	if(tempSt.startsWith("warning")){ EWIcon=WIcon; wCount++;}else { EWIcon=EIcon;eCount++;}
         	
         	if(prevSelect==0){
         		mytbm.addRow(new Object[]{EWIcon,++i +".) "+tempSt});
         		//System.out.println("prevsel 0");
         	}
         	else if(prevSelect==1 && tempSt.startsWith("error")){
         		//System.out.println("prevsel 1");
         		mytbm.addRow(new Object[]{EWIcon,++j +".) "+tempSt});
         	}
         	else if(prevSelect==2 && tempSt.startsWith("warning")){
         		//System.out.println("prevsel 2");
         		mytbm.addRow(new Object[]{EWIcon,++k +".) "+tempSt});
         	}
         	else{
         		//System.out.println("not passed"); 
         		//make tempSt null , so that only the corresponding nodes are highlighted, when selecting the drop down (E / W / E&W)
         		tempSt=null;
         		//highlightFlag=0;//for unhighlight method
         	}
         	
         	
         	if(tempSt!=null){
         		//tempsubSt=null;
         		//System.out.println("the id--"+tempsubSt);
         		//if(!tempsubSt.equals("null"))
         		graphIdsList.add(tempsubSt);
         		pe=pth.getElementById(tempsubSt);
         		//System.out.println(++higco+" --> "+tempsubSt);
 			
         		if(pe!=null) {
         			vpe=eng.getActiveVPathway().getPathwayElementView(pe);
         			vpe.highlight(col2);
         			prevPwe=vpe;
         		}
         		else System.out.println("no available graphId @ id: "+tempsubSt);
         	}
         	
         	
        }
        
        eLabel.setText("Errors:"+eCount); wLabel.setText("Warnings:"+wCount);
     	
        //refreshing the pathway , so that all the nodes highlighted appear highlighted
        //VPathway vpwTemp = eng.getActiveVPathway();
		//vpwTemp.setPctZoom(vpwTemp.getPctZoom());
        eng.getActiveVPathway().redraw();
        
        if( (prevSelect==0 && i!=0) || (prevSelect==1 && j!=0) || (prevSelect==2 && k!=0) ){ 
        	allIgnored=false;// this boolean required for disabling/enabling the right mouse click menuitems
        }
        else if(prevSelect==0){
        	mytbm.addRow(new Object[]{"","No Errors and Warnings"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        else if(prevSelect==1){
        	mytbm.addRow(new Object[]{EIcon,"No Errors"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        else if(prevSelect==2){
        	mytbm.addRow(new Object[]{WIcon,"No Warnings"});
        	allIgnored=true;
        	jtb.setEnabled(false);
     		
        }
        
	}

	private void clearRightClickStuff(){
		
			ignoredErrorTypesList.clear();
			ignoredElements.clear();
			ignoredSingleError.clear();
		
			subMenu4.setEnabled(false);
			while(subMenu4.getMenuComponentCount()>2){// clearing the subMenu4's checkboxes
				subMenu4.remove(2);
			}
			
			subMenu5.setEnabled(false);
			while(subMenu5.getMenuComponentCount()>2){// clearing the subMenu5's checkboxes
				subMenu5.remove(2);
			}
			
			subMenu6.setEnabled(false);
			while(subMenu6.getMenuComponentCount()>2){// clearing the subMenu4's checkboxes
				subMenu6.remove(2);
			}
	}
	
	private void printItOnTable(){
		if(!schemaFileType.equalsIgnoreCase("groovy"))
			printOnPanel();
		else
			groovyValidator.sortGroovyResultsAndPrint(globGroovyResult);
		
	}
	
	/**
	 * this highlights all the nodes in the list of nodes passed to it. 
	 */
	private void vhighlightAll(){
		PathwayElement pe=null;
		VPathwayElement vpe=null;
		
		for(String s:graphIdsList){
			pe=pth.getElementById(s);
			//System.out.println(++higco+" --> "+tempsubSt);

			if(pe!=null) {
				vpe=eng.getActiveVPathway().getPathwayElementView(pe);
				vpe.highlight(col2);
				prevPwe=vpe;
			}
			else System.out.println("no available graphId @ id: "+s);

		}
		
		eng.getActiveVPathway().redraw();
	}
	
	/**
	 * removes all the JTable rows, (the validation messages) 
	 */
	private void clearTableRows(){
		mytbm.setRowCount(0);
		jtb.setEnabled(true);
		
	}
	
	/**
	 * This method is to enable/disable the "Reconsider (Un-Ignore)" button in the sub menu item 
	 * @param jcbmi the JCheckBoxMenuItem that received the check/uncheck event 
	 */
	private void checkUncheck(JCheckBoxMenuItem jcbmi){
		
		JMenu subMenu=(JMenu)((JPopupMenu)jcbmi.getParent()).getInvoker();//since can not acces the parent directly here
		
		int indx = subMenu==subMenu4 ? 0 : ((subMenu==subMenu5) ? 2 : 4);
		
		if (jcbmi.getState())  checkedUnchecked[indx]++;
		else checkedUnchecked[indx+1]++;
		
		if(checkedUnchecked[indx]==checkedUnchecked[indx+1]) {
			checkedUnchecked[indx]=0;checkedUnchecked[indx+1]=0;
			subMenu.getMenuComponent(0).setEnabled(false);
			
		}else subMenu.getMenuComponent(0).setEnabled(true);

	}
	
	/**
	 * all the checked items in the Ignored list are considered for validation again
	 * @param subMenu
	 * @param ignoredList
	 */
	private void reConsiderTheIgnored(JMenu subMenu,ArrayList<String> ignoredList){
		int lengthOfIgnored=subMenu.getMenuComponentCount();
		int index=lengthOfIgnored-1;
		int NOFchecked=0;
		//System.out.println("total in submenu4 "+subMenu4.getMenuComponentCount());
		while(index>1){
			if( ( (JCheckBoxMenuItem)subMenu.getMenuComponent(index) ).getState() ){
				ignoredList.remove(index-2);
				subMenu.remove(index);
				NOFchecked++;
			}
			index--;
		}
		
		if(ignoredList.isEmpty()) subMenu.setEnabled(false); 
		
		subMenu.getMenuComponent(0).setEnabled(false);
		
		if(NOFchecked>0) printItOnTable();
	}
	
	/**
	 * This method adds the checked Errors/Warnings (E/W) to the corresponding main menuItem and also to the ignored list.
	 * @param subMenu the main menuItem to which the ignored E/W text has to be added
	 * @param EWMtext the E/W text to be added
	 * @param ignList the list to which a copy is added, to be used when considering back the E/W 
	 */
	private void addToSubMenu(JMenu subMenu,String EWMtext, ArrayList<String> ignList){ // Error/Warning message Text : EWMText
		//ImageIcon EWIcon=null;
		
		ignList.add(EWMtext);
		
		if(EWMtext.length()>80) EWMtext = EWMtext.substring(0,78)+"...";
		
		//if(EWMtext.startsWith("error")) EWIcon=EIcon; else EWIcon=WIcon;
		if(ignList==ignoredSingleError)
			EWMtext=EWMtext.replace("@@", " : ");
		else if (ignList==ignoredElements) {
			PathwayElement pe=pth.getElementById(EWMtext);
			EWMtext=EWMtext+" : "+pe.getObjectType();
		}
		
		JCheckBoxMenuItem subMenuItemCBMI= new JCheckBoxMenuItem(EWMtext);
		subMenuItemCBMI.setUI(new StayOpenCheckBoxMenuItemUI());
		subMenuItemCBMI.setActionCommand("subMenuItemCBMI");
		subMenuItemCBMI.addActionListener(this);
		subMenu.add(subMenuItemCBMI);
		
		if(ignList.size()==1) subMenu.setEnabled(true);  
		printItOnTable();
		
	}
	
	/**
	 * this is used to parse the schema file type, its name, its default phase and set them to corresponding fields,
	 *  and reset the values in the phase combo-box 
	 */
	private void parseSchemaAndSetValues(){
		
		SchemaHandler mySHandler=new SchemaHandler();
		
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(schemaFile, mySHandler);
		
		} catch (SAXException e) {
			JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
					"problem with the Schematron Ruleset","Validator Plugin",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
					"problem while accessing Schematron Ruleset","Validator Plugin",JOptionPane.ERROR_MESSAGE);
		
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		schemaString=mySHandler.getTheTitle();
		schemaTitleTag.setText("Schema Title: "+VPUtility.cutTitleString(schemaString,schemaTitleTag));
		schemaTitleTag.setCaretPosition(0);
		//System.out.println("Schema Title - "+mySHandler.getTheTitle());
		
		ArrayList<String> phasesList=mySHandler.getPhases();
		String dp = mySHandler.getDefaultPhase();
		saxTfr.transformer1.setParameter("phase",dp);
		//System.out.println("Default Phase - "+dp);
		
		schemaFileType=mySHandler.getType();
		//System.out.println("Schema Type = "+mySHandler.getType());
		
		/*Iterator<String> it=mySHandler.getPhases().iterator();
		while(it.hasNext()){
		System.out.println("Schema phase - "+it.next());	
		}*/
		
		VPUtility.resetPhaseBox(phaseBox);
		
		Iterator<String> tempIterator= phasesList.iterator();
		while(tempIterator.hasNext()){
			phaseBox.addItem("Phase: "+tempIterator.next());
			//System.out.println(tempIterator.next());
		}
		
		// to determine the index of the phaseBox based on value of default Phase(dp) 
		changeOfSchema=true;
		int phaseIndex;
		if( (phaseIndex=phasesList.indexOf(dp))!=-1 ){
			phaseBox.setSelectedIndex(phaseIndex+1);
	   	}
		else {
			phaseBox.setSelectedIndex(0);
		}
		changeOfSchema=false;	
		
	}

	private Thread create_SAXTFR_InAThread(){
		//System.out.println("choose pressed for 1st time");
		Thread threadForSax=new Thread(){ 
			public void run(){
				
				try {
					System.out.println("This thread for saxtranform runs");
					saxTfr= new SaxonTransformer();SaxonTransformer.setInputFile(currentPathwayFile);
					System.out.println("This thread for saxtranform completes its run");
				} catch (TransformerConfigurationException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
								"problem while configuring Saxon Transformer","Validator Plugin",JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		threadForSax.start();
		return threadForSax;
	}
	
	// the actual Listener method which does the handling of events related to "Choose Ruleset" button
	private void chooseRulesetListener(){
		
		System.out.println("choose schema button pressed");
		Thread threadForSax=null;
		
		if(chooser==null){
			chooser=new JFileChooser();
			createAndInitialize_RightClickMenuUI();
		}
		
		if(chooser.getDialogTitle()==null){
			   
			threadForSax=create_SAXTFR_InAThread();
			
			chooser.setDialogTitle("Choose Ruleset");
	    	chooser.setApproveButtonText("Open");
	    	chooser.setAcceptAllFileFilterUsed(false);
	    	chooser.setCurrentDirectory(PreferenceManager.getCurrent().getFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR));
	    
	    	chooser.addChoosableFileFilter(new FileFilter() {
			
	    		public boolean accept(File f) {
			
	    			if(f.isDirectory()) return true;
	    			
	    			String ext = f.toString().substring(f.toString().length()-3);
				
	    			if(ext.equalsIgnoreCase("sch")||ext.equalsIgnoreCase("ovy")||ext.equalsIgnoreCase("xml")) {
	    				return true;
	    			}
				
	    			return false;
	    		}
			
	    		public String getDescription() {
	    			return "Schematron (.sch & .xml) & Groovy (.groovy)";
	    		}

	    	});
	    	
	    }
	    
	    int returnVal = chooser.showOpenDialog(desktop.getFrame());

	    //wait for the transformer creation in the thread to complete 
	    if(threadForSax!=null){
	    	try{
	    		threadForSax.join();
	    		threadForSax=null;
	    	} catch (InterruptedException e1) {
	    		// TODO Auto-generated catch block
	    		e1.printStackTrace();
	    	}
	    }
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	
	    	//stopping the changes made by changing the state of phasebox to 0
	    	changeOfSchema=true;
	    	phaseBox.setSelectedIndex(0);
	    	changeOfSchema=false;
	       
	    	System.out.println("You chose this schematron file: "+chooser.getSelectedFile().getName());
	        schemaFile=chooser.getSelectedFile();
	       //System.out.println("schema is of type: "+(schemaFileType=whichSchema(schemaFile)));
	       
	        String schemaFileSubString=(schemaFile.toString().substring(schemaFile.toString().length()-3));
	        
	        //if the file chosen is of type ".groovy", then do groovy specific logic
	        if(schemaFileSubString.equalsIgnoreCase("ovy") ){
	        	jcBox.setSelectedIndex(0);
	        	schemaFileType="groovy";
	        	//phaseBox.setEnabled(false);
	        	if(groovyValidator==null) groovyValidator=new GroovyValidator(vPlugin,eng,phaseBox,graphIdsList);
	        	grvyObject=groovyValidator.loadGroovy(schemaFile);
	        	svrlOutputChoose.setEnabled(false);
	        }
	       
	        // if the chosen file is of type ".sch" (schema file)
	        else {
	        	parseSchemaAndSetValues();
	        	svrlOutputChoose.setEnabled(true);
	        }
	     
	        // setting/clearing the rightclick related stuff
			clearRightClickStuff();
			
	        valbutton.doClick();
	        PreferenceManager.getCurrent().setFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR, schemaFile);
	       
	    }
			
	}
	
	// the actual Listener method which does the handling of events related to "Validate" button
	private void validateButtonListener(){
		System.out.println("validate button pressed ");
		//initializing the color objects
		if(col1==null){
			col1= new Color(255,0,0);
			col2=new Color(0,0,255);
		}

		// check whether a schema is chosen or not 
		if(schemaFile==null){
			JOptionPane.showMessageDialog(
					desktop.getFrame(), 
			"Please choose a Ruleset and then press Validate");
			//System.out.println("after ok");

			chooseSchema.doClick();

			return;
		}

		// check if a pathway is opened
		if(eng.hasVPathway()){
			errorCounter=0;

			//reset values, to make the drop down option to errors and warnings (default option), when validate is pressed
			prevSelect=0;jcBox.setSelectedIndex(0);

			if(!schemaFileType.equalsIgnoreCase("groovy")){ 

				if(mimf==null){
					mimf=new MIMFormat();
				}
				validatePathway(saxTfr,mimf);
				//printOnPanel();
			}
			else {
				groovyValidator.runGroovy(grvyObject);
			}

			jcBox.setEnabled(true);jcb.setEnabled(true);
		}
		else{
			JOptionPane.showMessageDialog(
					desktop.getFrame(), 
			"Please open a Pathway to start validation");
			return;
		}
	}
		
	// Listeners for almost all of the UI components are below this line:
	
	public void actionPerformed(ActionEvent e) {

		if ("validate".equals(e.getActionCommand())) { // "Validate" button preseed
			validateButtonListener();
		}

	// The listeners for the six right click menuItems go below  
		else if ("menuItem1".equals(e.getActionCommand())) { //Ignore this Element
			//System.out.println("pressed Igonore Element");
			int rowNumberClicked = jtb.getSelectedRow() ;
			String graphIdToAdd = graphIdsList.get(rowNumberClicked);
			addToSubMenu(subMenu5,graphIdToAdd,ignoredElements);

			/*for(String s : ignoredElements){
				System.out.println(s);
				}*/
		}

		else if ("menuItem2".equals(e.getActionCommand())) {//Ignore this Error
			//System.out.println("pressed Ignore this Error/Warning");

			int rowNumberClicked = jtb.getSelectedRow() ;
			String graphIdToAdd = graphIdsList.get(rowNumberClicked);

			String valueAtTheRow=(String)jtb.getValueAt(jtb.getSelectedRow(), 1);
			valueAtTheRow=valueAtTheRow.substring(valueAtTheRow.indexOf('.')+3);

			String combined= graphIdToAdd+"@@"+valueAtTheRow;
			addToSubMenu(subMenu6,combined,ignoredSingleError);
		}

		else if ("menuItem3".equals(e.getActionCommand())) {//Ignore this Error Type
			//System.out.println("Ignore this Error Type pressed");
			//if(ignoredErrorTypesList==null) ignoredErrorTypesList=new ArrayList<String>();

			String valueAtTheRow=(String)jtb.getValueAt(jtb.getSelectedRow(), 1);
			valueAtTheRow=valueAtTheRow.substring(valueAtTheRow.indexOf('.')+3);

			addToSubMenu(subMenu4,valueAtTheRow,ignoredErrorTypesList);
		}
		
		else if ("subMenu5ReConsider".equals(e.getActionCommand())) {
			reConsiderTheIgnored(subMenu5,ignoredElements);
		}

		else if ("subMenu4ReConsider".equals(e.getActionCommand())) {
			reConsiderTheIgnored(subMenu4,ignoredErrorTypesList);
		}

		else if ("subMenu6ReConsider".equals(e.getActionCommand())) {
			reConsiderTheIgnored(subMenu6,ignoredSingleError);
		}
		
	// the listener for submenuItem contained in 4,5,6 menuItems above
		else if("subMenuItemCBMI".equals(e.getActionCommand())){
			//System.out.println("item checked");
			JCheckBoxMenuItem jcbmi=(JCheckBoxMenuItem)e.getSource();
			//okButtonED(jcbmi); // this method can be a bit slower for large groups
			checkUncheck(jcbmi); // This must be faster 
		}

		//listener method for "Choose Ruleset" button
		else if ("choose".equals(e.getActionCommand())) { // "Choose Ruleset" button pressed 
			chooseRulesetListener();
		}
		
		// listener for "Hightlight All" checkbox 
		else if("jcb".equals(e.getActionCommand())){ 

			if(((JCheckBox)e.getSource()).isSelected()){
				//System.out.println("jcb selected");
				vhighlightAll();
				PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,1);
			}
			else {
				//System.out.println("jcb deselected");
				//valbutton.setEnabled(true);
				PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,0);
				eng.getActiveVPathway().resetHighlight();//unhighlight all
			}
			//System.out.println("some event fired from jcb--"+PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS));
		}

		// listener for "errors/warnings drop down box"
		else if("jcBox".equals(e.getActionCommand())){ 

			JComboBox cbox = (JComboBox)e.getSource();
			if(prevSelect != cbox.getSelectedIndex()) {
				prevSelect = cbox.getSelectedIndex();
				printItOnTable();
				//System.out.println(cbox.getSelectedItem());
			}
		}

		// listener for the "Generate SVRL file" checkbox
		else if("svrlOutputChoose".equals(e.getActionCommand())){

			if( ((JCheckBox)e.getSource()).isSelected() ){
				SaxonTransformer.setProduceSvrl(true); 
			}else 
				SaxonTransformer.setProduceSvrl(false);
		}

	}
	
	//@Override ApplicationEventListener interface, for receiving events when a pathway is opened/closed/loaded
	public void applicationEvent(ApplicationEvent e) {

		if( e.getType()==ApplicationEvent.PATHWAY_OPENED || e.getType()==ApplicationEvent.PATHWAY_NEW){
			
			//reset values
			clearTableRows();
			// setting/clearing the rightclick related stuff
			if(ignoredErrorTypesList!=null){
				clearRightClickStuff();
			}
			jcBox.setEnabled(false);jcb.setEnabled(false);
			errorCounter=0;
			eLabel.setText("Errors:0");wLabel.setText("Warnings:0");
		}

	}

	// overriden method for ItemListener interface, invoked for change in phasebox selection
	public void itemStateChanged(ItemEvent arg0) {  

		if(!changeOfSchema && arg0.getStateChange()==1){
			//phaseBoxSelection=((JComboBox)arg0.getSource()).getSelectedIndex();
			if(!schemaFileType.equalsIgnoreCase("groovy")){
				//donot forget to change the index if the "Phase: " format is changed
				String temp=( (String)arg0.getItem() ).substring(7);

				if(temp.equals("All")){
					saxTfr.transformer1.setParameter("phase", "#ALL" );
				}
				else{
					saxTfr.transformer1.setParameter("phase", temp );
				}

				//System.out.println("item selected --"+temp );
			}
			if(eng.hasVPathway()) valbutton.doClick();
		}

	}
	
	// the 4 methods below are the method overrides for ComponentListener interface, only the 3rd one below is required here, 
	//in order to handle events related to change in component's size
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		if(schemaString!=null){
		schemaTitleTag.setText("Schema Title: "+VPUtility.cutTitleString(schemaString,schemaTitleTag));
		schemaTitleTag.setCaretPosition(0);
		//System.out.println("no of chars "+schemaTitleTag.get);
		}
	}
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}	