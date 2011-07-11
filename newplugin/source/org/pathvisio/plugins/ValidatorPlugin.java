package org.pathvisio.plugins;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.StyledEditorKit.ItalicAction;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.xml.sax.SAXException;
import edu.stanford.ejalbert.BrowserLauncher;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;

public class ValidatorPlugin implements Plugin,ActionListener, ApplicationEventListener, ItemListener
{
	private  PvDesktop desktop;
	//private JButton valbutton;
	//private final static JEditorPane jta=new JEditorPane("text/html","");
	private static File  schemaFile;
	private static File currentPathwayFile;
	//private JPanel mySideBarPanel ;
	//private JScrollPane scrollPane;
	private static VPathwayElement prevPwe;
	private static Engine eng;
	private static Pathway pth;
	private static Color col1,col2;//= new Color(255,0,0),col2=new Color(0,0,255);
	//private final  SchematronTask st=new SchematronTask();
	private  static SaxonTransformer saxTfr ;
	private  static MIMFormat mimf;//=new MIMFormat();
	private static JFileChooser chooser;
	private final static JButton valbutton=new JButton("Validate");
	private static int errorCounter,prevSelect;
	private final static JButton chooseSchema=new JButton("Choose Ruleset"); 
	private final static JComboBox jcBox = new JComboBox(new String[]{"Errors & Warnings","Errors only","Warnings only"});
	private final static JComboBox phaseBox = new JComboBox(new String[]{"Phase: All"});
	private final HelloAction helloAction= new HelloAction();
	private static boolean prevHighlight=true;
	//private final String imageE_UrlSrcAttr = (getClass().getResource("/error.png")).toString();
    //private final String imageW_UrlSrcAttr = (getClass().getResource("/warning.gif")).toString();
    //final String imageUrlE="<img width='12' height='12' src='file:"+System.getProperty("user.dir")+java.io.File.separatorChar+"images"+java.io.File.separatorChar;
    //private final String imageUrlE="<img width='11' height='11' src='"+imageE_UrlSrcAttr+"'></img> &nbsp;";
    //final String imageUrlW="<img width='15' height='15' src='file:"+System.getProperty("user.dir")+java.io.File.separatorChar+"images"+java.io.File.separatorChar;
    //private final String imageUrlW="<img width='15' height='15' src='"+imageW_UrlSrcAttr+"'></img> &nbsp;";
    private static JCheckBox jcb;
    private final JLabel eLabel=new JLabel("Errors:0",new ImageIcon(getClass().getResource("/error.png")),SwingConstants.CENTER),
    wLabel=new JLabel("Warnings:0",new ImageIcon(getClass().getResource("/warning.png")),SwingConstants.CENTER);
    private static final JTextField schemaTitleTag= new JTextField("  Schema Title: ");
	//private static boolean doExport=false;
	private static String schemaFileType;
	private static Thread threadForSax;
	private static GroovyObject grvyObject;
	private static ArrayList<Object> globGroovyResult;
	//private static int phaseBoxSelection=0;
	private static boolean changeOfSchema=false;
	private final MyTableModel mytbm=new MyTableModel();
	final JCheckBox svrlOutputChoose= new JCheckBox(" Generate SVRL file",false);
	
	private final JTable jtb = new JTable(mytbm){
    	public Class getColumnClass(int column){  
        return getValueAt(0, column).getClass();  
    	}  
    };
	
    private ImageIcon EIcon;
	private ImageIcon WIcon;
	private final ArrayList<String> graphIdsList = new ArrayList<String>();
	private ArrayList<String> ignoredErrorTypesList;
	private JPopupMenu popup;
	//private JMenuItem menuItem123;//,menuItem2,menuItem3;
	private JMenu subMenu4;
	private boolean allIgnored;
    
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
		schemaTitleTag.setEditable(false);
		
		//chandan : creating a button for choosing the schema file 
		//chooseSchema=new JButton("Choose Ruleset");
		chooseSchema.setActionCommand("choose");
		chooseSchema.addActionListener(this);
		
		//creating a jcheckbox ("Highlight All") and set its status from the .pathvisio pref file
	    boolean jbcinit;
		if(PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS)==1){
	    	jbcinit=true;
	    }else jbcinit=false;
		
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
		
		jcb= new JCheckBox(" Highlight All", jbcinit);
		jcb.setActionCommand("jcb");
		jcb.addActionListener(this);
		jcb.setEnabled(false);//set to false , to enable it only when validate is pressed
		//valbutton.setEnabled(!jbcinit);
		
		// save the desktop reference so we can use it later
		this.desktop = desktop;
		eng=desktop.getSwingEngine().getEngine();
		eng.addApplicationEventListener(this); // To listen to the events from the engine (eg. Pathaway-opened event) 
		
		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Help", helloAction);
		
		final JPanel mySideBarPanel = new JPanel (new GridBagLayout());
		//Font f=new Font("Times New Roman", Font.BOLD, 16);
		//System.out.println("the font used"+f);
		//jta.setFont(f);
		//jta=new JEditorPane("text/html","");
		
		//jta.addHyperlinkListener(this);
		//jta.setEditable(false);
		
		//adding labels for warning and error counts
		eLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		wLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		jtb.setTableHeader(null);
	    jtb.setFont(new Font("Verdana", Font.PLAIN, 15));
	    
	    mytbm.addColumn("image"); 
	    mytbm.addColumn("errors and warnings");
	    
	    jtb.getColumn("image").setMaxWidth(23);
	    
	    /*jtb.addMouseMotionListener(new MouseMotionAdapter(){
	    	   
	    	public void mouseMoved(MouseEvent e){
	    		int row=jtb.rowAtPoint(e.getPoint());
	    		String tt=graphIdsList.get(row);
	    		if(tt.equals("null")) tt="--";
	    		jtb.setToolTipText(tt);
	    	    }//end MouseMoved
	    	});
	    */
	    jtb.addMouseListener(new java.awt.event.MouseAdapter()
	    {
	    	public void mouseClicked(java.awt.event.MouseEvent e){
	    		int row=jtb.rowAtPoint(e.getPoint());
	    		
	    		/*if (//!(row >= 0 && row < jtb.getRowCount()) || 
	    			e.getButton()== MouseEvent.BUTTON3) {
	    			jtb.clearSelection();
	    			System.out.println("ryt click "+row);
	    			
	    			JPopupMenu popup = new JPopupMenu("chan");
	    			JMenuItem menuItem1= new JMenuItem("Ignore Element"),
	    			menuItem2 = new JMenuItem("Ignore this Error"),
	    			menuItem3 = new JMenuItem("Ignore this Error Type");
	    			popup.add(menuItem1);popup.add(menuItem2);popup.add(menuItem3);
	    			
	    			jtb.getSelectionModel().setSelectionInterval(row, row);//to select the row with right click
	    			popup.show(e.getComponent(), e.getX(), e.getY());
	    		}*/ 
	    		
	    		if(prevPwe!=null && graphIdsList.size()!=0 ){ 
	    			//int row=jtb.rowAtPoint(e.getPoint());
	    			//int col= jtb.columnAtPoint(e.getPoint());
	    			//System.out.println("prevpwe "+prevPwe );
	    			//System.out.println("graphId not equal to null");
	    					
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
	    		
	    		if (e.getButton()== MouseEvent.BUTTON3) {
	    			jtb.clearSelection();
	    			//System.out.println("ryt click "+row);
	    			
	    			if(allIgnored ){
	    				if(popup.getComponent(0).isEnabled())
	    					for(int MI=0; MI<3 ; MI++)
	    						popup.getComponent(MI).setEnabled(false); 
	    			}
	    			else {
	    				jtb.getSelectionModel().setSelectionInterval(row, row);//to select the row with right click
	    				if(!popup.getComponent(0).isEnabled())
	    					for(int MI=0; MI<3 ; MI++)
	    						popup.getComponent(MI).setEnabled(true); 

	    			}

		    		popup.show(e.getComponent(), e.getX(), e.getY());
		    	}
	    	
	    	}
	    });

	    jtb.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer(graphIdsList));
	    //ImageIcon aboutIcon = new ImageIcon(getClass().getResource("/warning.png"));
	    
		final JScrollPane scrollPane = new JScrollPane(jtb);
		//scrollPane.setBackground(Color.WHITE);
		//scrollPane.setForeground(Color.white);
		//scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setColumnHeader(null);
		
		//code for layout of the components goes here
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
        
        // get a reference to the sidebar
        final JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        //JTabbedPane bottomTabbedPane= new JTabbedPane();
        //bottomTabbedPane.add("validation",mySideBarPanel);
        sidebarTabbedPane.add("Validator", mySideBarPanel);
        desktop.getSideBarTabbedPane().setSelectedComponent(mySideBarPanel);
        //setting the winx, winy values 
        
        //code for setting the winx and winy of main window of pathvisio in preference file
        //PreferenceManager.getCurrent().set(GlobalPreference.WIN_X, "0");
        //PreferenceManager.getCurrent().set(GlobalPreference.WIN_Y,"0");
        
        //JPanel mypanel=new JPanel();
        //mypanel.add(bottomTabbedPane);
        //desktop.getFrame().add(mypanel);
       // desktop.getFrame().validate();
        
        //sidebarTabbedPane.setLayout(DEFAULT);
    	//sidebarTabbedPane.add(new JButton("Button 1"));
        //sidebarTabbedPane.ad
	     
        //saxTfr.setschemaFile(schemaFile);
        //saxTfr.setInputFile(currentPathwayFile);
        
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
	public void done() {}
	
	//to make all the cells in the table uneditable
	private class MyTableModel extends DefaultTableModel{
		public boolean isCellEditable(int row, int column){  
		    return false;  
		  }  
	}
	
	/**
	 * Open a Browser link to the plugin's help page when this action is triggered. 
	 */
	private class HelloAction extends AbstractAction
	{
		HelloAction()
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
                 ex.printStackTrace();
             }	
		}
	}

	//chandan validate method using SwingWorker<T,V> which uses a separate thread to do the task
	public void processTask(ProgressKeeper pk, ProgressDialog d, SwingWorker<Object, Object> sw) {
		
		sw.execute();
		d.setVisible(true);
		
		try {
			 sw.get();
		} catch (ExecutionException e)
		{
			System.out.println("ExecutionException in ValidatorPlugin---"+e.getMessage());
			return;
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception---"+e.getCause());
			return ;
		}
	}

	public void validatePathway(final SaxonTransformer tempSaxTrnfr,final MIMFormat mimf)
	{
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(desktop.getFrame(),"Validator plugin", pk, false, true);
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
		
		protected Object doInBackground() {
					
					pk.setTaskName("Validating pathway");
				
					//if(schemaFile.)	
					//MIMFormat mimf=new MIMFormat();
					//SchematronTask st=new SchematronTask();
					try {
						
						System.out.println("b4 export called"+schemaFileType);
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
					
					}
					
					catch (Exception e1) { //changed from ConverterException to catch all the errors
						System.out.println("Exception in validatepathway method--"+e1.getMessage());
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
	
	private void printOnPanel(){
		prevHighlight=true;
		VPathwayElement vpe=null;
		PathwayElement pe; 
		//StringBuilder sbf=new StringBuilder();
        String tempSt,tempsubSt;pth=eng.getActivePathway();
        Iterator<String> tempIterator = (saxTfr.diagnosticReference).iterator();
        int i=0,j=0,k=0,eCount=0,wCount=0;
        //String imageUrl=imageUrlE;
        ImageIcon EWIcon=EIcon; 
        graphIdsList.clear();
        
        clearTableRows();
        //sbf.append("<font size='4' face='verdana'>");
        int higco=0; 
        
        eng.getActiveVPathway().resetHighlight();//unhighlight all nodes
        
        while (tempIterator.hasNext()) {
         	errorCounter+=1;
         	tempSt=tempIterator.next();
         	String[] splitString=tempSt.split("@@");
         	tempSt=splitString[1];
         	tempsubSt=splitString[0];
         	
         	//if(tempSt.compareTo("error - An interaction should not start and end with Line arrowheads.")==0) continue;
         	if(ignoredErrorTypesList.contains(tempSt)) continue;
         	
         	if(tempSt.startsWith("warning")){ EWIcon=WIcon; wCount++;}else { EWIcon=EIcon;eCount++;}
         	
         	if(prevSelect==0){
         		//sbf.append(imageUrl + ++i +".) "+tempSt+"<br><br>");
         		mytbm.addRow(new Object[]{EWIcon,++i +".) "+tempSt});
         		//System.out.println("prevsel 0");
         	}
         	else if(prevSelect==1 && tempSt.startsWith("error")){
         		//System.out.println("prevsel 1");
         		//sbf.append(imageUrl + ++j +".) "+tempSt+"<br><br>");	
         		mytbm.addRow(new Object[]{EWIcon,++j +".) "+tempSt});
         	}
         	else if(prevSelect==2 && tempSt.startsWith("warning")){
         		//System.out.println("prevsel 2");
         		//sbf.append(imageUrl + ++k +".) "+tempSt+"<br><br>");	
         		mytbm.addRow(new Object[]{EWIcon,++k +".) "+tempSt});
         	}
         	else{
         		System.out.println("not passed"); 
         		//make tempSt null , so that only the corresponding nodes are highlighted, when selecting the drop down (E / W / E&W)
         		tempSt=null;
         		//highlightFlag=0;//for unhighlight method
         	}
         	
         	eLabel.setText("Errors:"+eCount); wLabel.setText("Warnings:"+wCount);
         	
         	if(tempSt!=null){
         		//tempsubSt=null;
         		//tempsubSt=tempSt.substring(tempSt.indexOf(' ')+22,tempSt.indexOf('>')-1);
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
        
        //refreshing the pathway , so that all the nodes highlighted appear highlighted
        //VPathway vpwTemp = eng.getActiveVPathway();
		//vpwTemp.setPctZoom(vpwTemp.getPctZoom());
        eng.getActiveVPathway().redraw();
        
        //sbf.append("</font>");
        
        if( (prevSelect==0 && i!=0) || (prevSelect==1 && j!=0) || (prevSelect==2 && k!=0) ){ 
        	//jta.setText(sbf.toString());
        	allIgnored=false;// this boolean required for disabling/enabling the right mouse click menuitems
        }
        else if(prevSelect==0){
        	//jta.setText("<b><font size='4' face='verdana'>No Errors and Warnings</font></b>");
        	mytbm.addRow(new Object[]{"","No Errors and Warnings"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        else if(prevSelect==1){
        	//jta.setText("<b><font size='4' face='verdana'>No Errors</font></b>");	
        	mytbm.addRow(new Object[]{EIcon,"No Errors"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        else if(prevSelect==2){
        	//jta.setText("<b><font size='4' face='verdana'>No Warnings</font></b>");	
        	mytbm.addRow(new Object[]{WIcon,"No Warnings"});
        	allIgnored=true;
        	jtb.setEnabled(false);
     		
        }
        
        //jta.setCaretPosition(0);
        //sbf.setLength(0); 
        
	}

	private String cutTitleString(String ss)
	{	
		schemaTitleTag.setToolTipText(ss);
		if(ss.length()>16) return (ss.substring(0,16)+"..").toUpperCase();
		return ss;
	}	
	
	private void printItOnTable(){
		if(!schemaFileType.equalsIgnoreCase("groovy"))
			printOnPanel();
		else
			sortGroovyResultsAndPrint(globGroovyResult);
		
	}
	
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
	
	private void clearTableRows(){
		mytbm.setRowCount(0);
		jtb.setEnabled(true);
		
	}
	
	private void reConsiderTheIgnored(){
		int lengthOfIgnored=subMenu4.getMenuComponentCount();
		int index=lengthOfIgnored-1;
		int NOFchecked=0;
		//System.out.println("total in submenu4 "+subMenu4.getMenuComponentCount());
		while(index>1){
			if( ( (JCheckBoxMenuItem)subMenu4.getMenuComponent(index) ).getState() ){
				ignoredErrorTypesList.remove(index-2);
				subMenu4.remove(index);
				NOFchecked++;
			}
			index--;
		}
		if(ignoredErrorTypesList.size()==0) subMenu4.setEnabled(false); 
		if(NOFchecked>0) printItOnTable();
	}
	
	private void addToSubMenu(String EWMtext){ // Error/Warning message Text : EWMText
		ImageIcon EWIcon=null;
		
		if(EWMtext.length()>80) EWMtext = EWMtext.substring(0,78)+"...";
		
		//if(EWMtext.startsWith("error")) EWIcon=EIcon; else EWIcon=WIcon;
		
		JCheckBoxMenuItem subMenuItemCBMI= new JCheckBoxMenuItem(EWMtext);
		subMenuItemCBMI.setUI(new StayOpenCheckBoxMenuItemUI());
		//subMenuItemCBMI.setActionCommand("subMenuItemCBMI");
		//subMenuItemCBMI.addActionListener(this);
		subMenu4.add(subMenuItemCBMI);
		
	}
	
	private void parseSchemaAndSetValues(){
		
		SchemaHandler mySHandler=new SchemaHandler();
		
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(schemaFile, mySHandler);
		
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		schemaTitleTag.setText("Schema Title: "+cutTitleString(mySHandler.getTheTitle()));
		//System.out.println("Schema Title - "+mySHandler.getTheTitle());
		
		schemaFileType=mySHandler.getType();
		//System.out.println("Schema Type = "+mySHandler.getType());
		
		/*Iterator<String> it=mySHandler.getPhases().iterator();
		
		while(it.hasNext()){
		System.out.println("Schema phase - "+it.next());	
		}*/
		
		if(!phaseBox.isEnabled())
			phaseBox.setEnabled(true);
		
		Iterator<String> tempIterator= mySHandler.getPhases().iterator();
		
		//refreshing the drop down to include phases of the selected schema by clearing out the previous items and adding new ones
		while(phaseBox.getItemCount()!=1){
			phaseBox.removeItemAt(phaseBox.getItemCount()-1);
		}
		
		while(tempIterator.hasNext()){
			phaseBox.addItem("Phase: "+tempIterator.next());
			//System.out.println(tempIterator.next());
		}

		
	}
	
	private void sortGroovyResultsAndPrint(ArrayList<Object> tempList){
		
		Iterator<Object> tempIterator = tempList.iterator();
		int counter=0;
		String tempSt,graphId;
		String[] tempArray;
		//StringBuilder sbf=new StringBuilder();
		int[] ijkew={0,0,0,0,0};//i=0,j=0,k=0,eCount=0,wCount=0;
		clearTableRows();
		graphIdsList.clear();
		
		//System.out.println("inside groovy prevSelect = "+prevSelect);
		//sbf.append("<font size='4' face='verdana'>");
		eng.getActiveVPathway().resetHighlight();//unhighlight all nodes
	    
		while (tempIterator.hasNext()) {
	         	
			 Object tempObject = tempIterator.next();
	         counter++;
	         
	         if( tempObject instanceof ArrayList){
	         		
	         		System.out.println("Array list detected in the result");
	         		
	         		for(String[] sa: (ArrayList<String[]>)tempObject){
	         		
	         			//System.out.println(sa[0]+" @ "+sa[2]+" -- "+sa[1]);
	         			
	         			if(sa[0]==null) sa[0]="error"; // default role is null, if role is not set
	         			
	         			/*if(sa[2]==null){
		         			//sa[2]=" - ";
	         				graphId=null;
	         				//tempSt=sa[0]+"-"+sa[1];
		         		}
		         		else{
		         			graphId=sa[2];
		         			//sa[2]=" @ GraphId : "+"<a href='"+sa[2]+"'>"+sa[2]+"</a>"+" - ";
		         			//tempSt=sa[0]+" @ GraphId : "+"<a href='"+sa[2]+"'>"+sa[2]+"</a>"+" - "+sa[1];
		         		}*/
	         			graphId=sa[2];
	         			tempSt=sa[0]+"-"+sa[1];
	         			//tempSt=sa[0]+sa[2]+sa[1];
	         			//call with tempst as arg
	         			if(ignoredErrorTypesList.contains(tempSt)) continue;
	         			printGroovy(tempSt,graphId,ijkew);
	         		}
	         		
	         	}
	         	
	         else {
	         		System.out.println("String Array detected "+counter);
	         		tempArray= (String[])tempObject;
	         		
	         		if(tempArray[0]==null) tempArray[0]="error";
	         		
	         		/*if(tempArray[2]==null){
	         			//tempArray[2]=" - ";
	         			graphId=null;
	         			//tempSt=tempArray[0]+" - "+tempArray[1];
	         		}else{
	         			graphId=tempArray[2];
	         			//tempArray[2]=" @ GraphId : "+"<a href='"+tempArray[2]+"'>"+tempArray[2]+"</a>"+" - ";
	         			//tempSt=tempArray[0]+" - "+tempArray[1];
	         		}*/
	         		graphId=tempArray[2];
	         		tempSt=tempArray[0]+" - "+tempArray[1];
	         		if(ignoredErrorTypesList.contains(tempSt)) continue;
	         		//tempSt=tempArray[0]+tempArray[2]+tempArray[1];
	         		//call with tempst as arg
	         		printGroovy(tempSt,graphId,ijkew);
	         }
	      
		 }
		
		eLabel.setText("Errors:"+ijkew[3]); wLabel.setText("Warnings:"+ijkew[4]);
		
		//sbf.append("</font>");
		
   		//refreshing the pathway , so that all the nodes highlighted appear highlighted
		//VPathway vpwTemp = eng.getActiveVPathway();
		//vpwTemp.setPctZoom(vpwTemp.getPctZoom());
		eng.getActiveVPathway().redraw();
        
        if( (prevSelect==0 && ijkew[0]!=0) || (prevSelect==1 && ijkew[1]!=0) || (prevSelect==2 && ijkew[2]!=0) ){ 
        	//jta.setText(sbf.toString());
        	allIgnored=false;
        }
        else if(prevSelect==0){
        	//jta.setText("<b><font size='4' face='verdana'>No Errors and Warnings</font></b>");
        	mytbm.addRow(new Object[]{"","No Errors and Warnings"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        else if(prevSelect==1){
        	//jta.setText("<b><font size='4' face='verdana'>No Errors</font></b>");	
        	mytbm.addRow(new Object[]{EIcon,"No Errors"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        else if(prevSelect==2){
        	//jta.setText("<b><font size='4' face='verdana'>No Warnings</font></b>");	
        	mytbm.addRow(new Object[]{WIcon,"No Warnings"});
        	allIgnored=true;
        	jtb.setEnabled(false);
        }
        
        System.out.println("-----------groovy part end-------------- ");
        
        //jta.setCaretPosition(0);
        //sbf.setLength(0); 
		
}
	
	private void printGroovy(String tempSt,String graphId,int[] ijkew){
		
		prevHighlight=true;
		VPathwayElement vpe=null;
		PathwayElement pe; 
		//String imageUrl=imageUrlE;
		ImageIcon EWIcon=EIcon;
		pth=eng.getActivePathway();
        //int higco=0; 
        
        if(tempSt.startsWith("warning")){ EWIcon=WIcon; ijkew[4]++;}else { EWIcon=EIcon; ijkew[3]++;}
		
		if(prevSelect==0){
			//sbf.append(imageUrl + ++ijkew[0] +".) "+tempSt+"<br><br>");
			//System.out.println("prevsel 0");
			mytbm.addRow(new Object[]{EWIcon,++ijkew[0] +".) "+tempSt});
        }
		else if(prevSelect==1 && tempSt.startsWith("error")){
			//System.out.println("prevsel 1");
			//sbf.append(imageUrl + ++ijkew[1] +".) "+tempSt+"<br><br>");	
			mytbm.addRow(new Object[]{EWIcon,++ijkew[1] +".) "+tempSt});
		}
		else if(prevSelect==2 && tempSt.startsWith("warning")){
			//System.out.println("prevsel 2");
			//sbf.append(imageUrl + ++ijkew[2] +".) "+tempSt+"<br><br>");	
			mytbm.addRow(new Object[]{EWIcon,++ijkew[2] +".) "+tempSt}); 
		}
		else{
			System.out.println("not passed"); 
			//make tempSt null , so that only the corresponding nodes are highlighted, when selecting the drop down (E / W / E&W)
			graphId=null;tempSt=null;
			//highlightFlag=0;//for unhighlight method
         }
        if(tempSt!=null){
        	graphIdsList.add(graphId+"");	
        }
		
        if(graphId!=null){
				
         	pe=pth.getElementById(graphId);
         	
         	if(pe!=null) {
         		vpe=eng.getActiveVPathway().getPathwayElementView(pe);
         		vpe.highlight(col2);
         		prevPwe=vpe;
         	}
         	else System.out.println("no available graphId @ id: "+graphId);
         }
      
	}
	
	private GroovyObject loadGroovy(File schemaFile){
		
		System.out.println("reached inside loadGroovy method");
		ArrayList<String[]> tempArray=new ArrayList<String[]>();
		
  	   	GroovyClassLoader loader =  new GroovyClassLoader(getClass().getClassLoader());
  	   	Class groovyClass=null;
  	   	GroovyObject groovyObject=null;
  	   
  	   	try {
  		   groovyClass = loader.parseClass(schemaFile);
  		   schemaTitleTag.setText("Schema Title: "+cutTitleString(groovyClass.getSimpleName()));
  		   groovyObject = (GroovyObject) groovyClass.newInstance();
  	   	}
  	   	catch (Exception e1) {
  		   System.out.println("Exception @ groovy = "+e1.getMessage());
  		   e1.printStackTrace();
  	   	}
  	   	
  	   	if(!phaseBox.isEnabled())
   			phaseBox.setEnabled(true);
  	   	
  	    //refreshing the drop down to include phases of the selected schema by clearing out the previous items and adding new ones
	   	while(phaseBox.getItemCount()!=1){
	   		phaseBox.removeItemAt(phaseBox.getItemCount()-1);
	   	}
	   
	   	try{
	   		tempArray=(ArrayList<String[]>)(groovyObject.invokeMethod("phaseSupport", null));
	   	}
	   	catch(Exception e){System.out.println("phaseSupport method not present"); return groovyObject;}
	   	
	   	Iterator<String[]> tempIterator= tempArray.iterator();

	   	while(tempIterator.hasNext()){
	   		phaseBox.addItem("Phase: "+(tempIterator.next())[0]);
	   		//System.out.println(tempIterator.next());
	   	}


	   	return groovyObject;
		
	}
	
	private void runGroovy(GroovyObject groovyObject){
		
		System.out.println("--------------groovy---------------");
		ArrayList<Object> tempArray=new ArrayList<Object>();
		
		//phaseBoxSelection=2;
  	     	   
  	   	Pathway argPw= eng.getActivePathway();
  	   	
  	   	//checking every line element for graphId before sending the pathway for validation, generate graphId if graphId is not found
  	   	for(PathwayElement pwe: argPw.getDataObjects()){
  		
  	   		if( pwe.getObjectType()==ObjectType.LINE && ( pwe.getGraphId()=="" | pwe.getGraphId()==null) ){
  	   			pwe.setGeneratedGraphId();
  	   		}
  		}
  	   	
  	   	if(phaseBox.getSelectedIndex()==0){
  	   	
  	   		//if(argPw!=null){
  	   		/*Object[] args = {argPw};
  	   		tempArray=(ArrayList<Object>)(groovyObject.invokeMethod("main", args));*/
  	   	
  	   		//code for running groovy script from java   
  	   		Binding binding = new Binding();
  	   		binding.setVariable("groovyObject", groovyObject);
  	   		binding.setVariable("tempArray", tempArray);
  	   		binding.setVariable("argPw",argPw );
  	
  	   		GroovyShell shell = new GroovyShell(binding);
  
  	   		try {
  	   			shell.evaluate(getClass().getResourceAsStream("/GroovyScriptKC.kc"));//running groovy script from a file named GroovyScriptKC.kc
  	   		} catch (CompilationFailedException e) {
  	   			System.out.println("CompilationFailedException in the groovyshell code");
  	   			e.printStackTrace();
  	   		} 
  	   	}
  	   	
  	   	else { // this code runs only when there are phases present in the groovy rule 
  	   		
  	   		ArrayList<String[]> phaseTotal;//=new ArrayList<Object>();
  	   		phaseTotal=(ArrayList<String[]>)(groovyObject.invokeMethod("phaseSupport", null));
  	   		String methodNamesWithCommas=  ((String[])phaseTotal.get(phaseBox.getSelectedIndex()-1))[1];
  	   		String[] methodNamesArray= methodNamesWithCommas.split( ",\\s*" );
  	   		
  	   		for(String methodName :methodNamesArray)
  	   			tempArray.add(groovyObject.invokeMethod(methodName.trim(), argPw));
    	}
  	   	
  	   	//remove null results from the overall result from the ruleset
  	   	while(tempArray.contains(null)){
  	   		tempArray.remove(null);
  	   	}
  	   	
  	   	globGroovyResult=tempArray; 	   	
  	   	sortGroovyResultsAndPrint(globGroovyResult);
  	   	//}
  	   	//else System.out.println("no pathway is open to run groovy");
  	   	
	}
		
	//@Override
	public void actionPerformed(ActionEvent e) {
		
		if ("validate".equals(e.getActionCommand())) { // "Validate" button preseed
			System.out.println("validate button pressed ");
			
			if(col1==null){
				col1= new Color(255,0,0);
				col2=new Color(0,0,255);
			}
			
			if(schemaFile==null){
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Please choose a Ruleset and then press Validate");
				//System.out.println("after ok");
				
				chooseSchema.doClick();
				
				return;
			}
			
			if(eng.hasVPathway()){
				errorCounter=0;
				
				//set the below line, to make the drop down option to errors and warnings (default option), when validate is pressed
				prevSelect=0;jcBox.setSelectedIndex(0);
				
				if(!schemaFileType.equalsIgnoreCase("groovy")){ 
				
					if(mimf==null){
						mimf=new MIMFormat();
					}
					
					validatePathway(saxTfr,mimf);
					printOnPanel();
					
				}
				else {
					runGroovy(grvyObject);
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
		
		else if ("menuItem1".equals(e.getActionCommand())) {//Ignore Element
			if(((JMenuItem)e.getSource()).getText().contains("Reconsider")){
				//System.out.println("pressed consider submenu");
				reConsiderTheIgnored();
			} 
			else System.out.println("presses menu1");
		}
		
		else if ("menuItem2".equals(e.getActionCommand())) {//Ignore this Error
			System.out.println("presses menu2");
		}
		
		else if ("menuItem3".equals(e.getActionCommand())) {//Ignore this Error Type
			//System.out.println("Ignore this Error Type pressed");
			//if(ignoredErrorTypesList==null) ignoredErrorTypesList=new ArrayList<String>();
			String valueAtTheRow=(String)jtb.getValueAt(jtb.getSelectedRow(), 1);
			ignoredErrorTypesList.add(valueAtTheRow.substring(valueAtTheRow.indexOf('.')+3));
			addToSubMenu(valueAtTheRow.substring(valueAtTheRow.indexOf('.')+3));
			if(ignoredErrorTypesList.size()==1) subMenu4.setEnabled(true);  
			printItOnTable();
			
			/*for(String s : ignoredErrorTypesList){
				System.out.println(s);
			}*/
			
		}
		
		else if ("choose".equals(e.getActionCommand())) { // "Choose Ruleset" button pressed 
			
			System.out.println("choose schema button pressed");
			
			if(chooser==null){
				chooser=new JFileChooser();
				
				ignoredErrorTypesList=new ArrayList<String>();
				
				EIcon = new ImageIcon(getClass().getResource("/error.png"));
				WIcon = new ImageIcon(getClass().getResource("/warning.png"));
				
				//graphIdsList=new ArrayList<String>();
				
				popup = new JPopupMenu("filter");// popup named "filter"
				
				
				JMenuItem menuItem123= new JMenuItem("Ignore Element");
				menuItem123.addActionListener(this);
				menuItem123.setActionCommand("menuItem1");
				popup.add(menuItem123);
				
				menuItem123 = new JMenuItem("Ignore this Error/Warning");
				menuItem123.setActionCommand("menuItem2");
				menuItem123.addActionListener(this);
				popup.add(menuItem123);
				
				menuItem123 = new JMenuItem("Ignore this Error/Warning Type");
				menuItem123.setActionCommand("menuItem3");
				menuItem123.addActionListener(this);
				popup.add(menuItem123);
				popup.addSeparator();
				
				subMenu4= new JMenu("Ignored Error/Warning Types");
				subMenu4.setIcon(new ImageIcon(getClass().getResource("/ignore.png")));
				
				JMenuItem subMenuItemOkButton=new JMenuItem( "Reconsider (Un-Ignore)");//	new ImageIcon(getClass().getResource("/ignore.png")) );
				subMenuItemOkButton.setActionCommand("menuItem1");
				subMenuItemOkButton.addActionListener(this);
				subMenu4.add(subMenuItemOkButton);
				
				subMenu4.addSeparator();
				subMenu4.setEnabled(false);
				
				popup.add(subMenu4);
			}
			
			if(chooser.getDialogTitle()==null){
				   
				System.out.println("choose pressed for 1st time");
				threadForSax=new Thread(){ 
					public void run(){
						
						try {
							System.out.println("This thread for saxtranform runs");
							saxTfr= new SaxonTransformer();SaxonTransformer.setInputFile(currentPathwayFile);
							System.out.println("This thread for saxtranform completes its run");
						} catch (TransformerConfigurationException e1) {
								e1.printStackTrace();
						}
					}
				};
				threadForSax.start();
				
				chooser.setDialogTitle("Choose Ruleset");
		    	chooser.setApproveButtonText("Open");
		    	chooser.setAcceptAllFileFilterUsed(false);
		    	chooser.setCurrentDirectory(PreferenceManager.getCurrent().getFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR));
		    
		    	chooser.addChoosableFileFilter(new FileFilter() {
				
		    		public boolean accept(File f) {
				
		    			if(f.isDirectory()) return true;
		    			
		    			String ext = f.toString().substring(f.toString().indexOf('.')+1);
					
		    			if(ext.equalsIgnoreCase("sch")||ext.equalsIgnoreCase("groovy")||ext.equalsIgnoreCase("xml")) {
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
			try{
				threadForSax.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	
		    	//stopping the changes made by change the state of phasebox to 0
		    	changeOfSchema=true;
		    	phaseBox.setSelectedIndex(0);
		    	changeOfSchema=false;
		       
		    	System.out.println("You chose this schematron file: "+chooser.getSelectedFile().getName());
		        schemaFile=chooser.getSelectedFile();
		       //System.out.println("schema is of type: "+(schemaFileType=whichSchema(schemaFile)));
		       
		        String schemaFileSubString=(schemaFile.toString().substring(schemaFile.toString().indexOf('.')+1));
		        
		        //if the file chosen is of type ".groovy", then do groovy specific logic
		        if(schemaFileSubString.equalsIgnoreCase("groovy") ){
		        	jcBox.setSelectedIndex(0);
		        	schemaFileType="groovy";
		        	//phaseBox.setEnabled(false);
		        	grvyObject=loadGroovy(schemaFile);
		        	svrlOutputChoose.setEnabled(false);
		        }
		       
		        // if the chosen file is of type ".sch" (schema file)
		        else {
		        	//phaseBox.setSelectedIndex(0);
		        	SaxonTransformer.transformer1.clearParameters();
		        	parseSchemaAndSetValues();
		        	svrlOutputChoose.setEnabled(true);
		        }
		     
		        // setting/clearing the rightclick related stuff
				ignoredErrorTypesList.clear();
				subMenu4.setEnabled(false);
				while(subMenu4.getMenuComponentCount()>2){// clearing the subMenu4's checkboxes
					subMenu4.remove(2);
				}
				
		        valbutton.doClick();
		        PreferenceManager.getCurrent().setFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR, schemaFile);
		       
		     
		    }
					   
		}
		
		/*else if("subMenuItemCBMI".equals(e.getActionCommand())){
			System.out.println("check na");
			
		}*/
		
		else if("jcb".equals(e.getActionCommand())){ // "Hightlight All" checkbox
			//eng.getActiveVPathway().resetHighlight();

			if(((JCheckBox)e.getSource()).isSelected()){
				System.out.println("jcb selected");
				//valbutton.setEnabled(false);

				/*if(!schemaFileType.equalsIgnoreCase("groovy"))
					printOnPanel();//call only the highlighting part, (highlight all!)
				else 
					sortGroovyResultsAndPrint(globGroovyResult);
				 */
				vhighlightAll();

				PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,1);
			}
			else {
				System.out.println("jcb deselected");
				//valbutton.setEnabled(true);
				PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,0);
				eng.getActiveVPathway().resetHighlight();//unhighlight all
			}
			//System.out.println("some event fired from jcb--"+PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS));
		}
		
		else if("jcBox".equals(e.getActionCommand())){ // "errors/warnings drop down box"
			
			JComboBox cbox = (JComboBox)e.getSource();
			
			if(prevSelect != cbox.getSelectedIndex()) {
				prevSelect = cbox.getSelectedIndex();
				
				/*if(!schemaFileType.equalsIgnoreCase("groovy"))
					printOnPanel();
				else
					sortGroovyResultsAndPrint(globGroovyResult);
				*/
				printItOnTable();
				
				System.out.println(cbox.getSelectedItem());
			}
		}
		
		else if("svrlOutputChoose".equals(e.getActionCommand())){
			
			if( ((JCheckBox)e.getSource()).isSelected() ){
				SaxonTransformer.setProduceSvrl(true); 
			}else 
				SaxonTransformer.setProduceSvrl(false);
		}
		
	}
	
	//@Override
	public void applicationEvent(ApplicationEvent e) {
		
		//System.out.println("event occured");
		if( e.getType()==ApplicationEvent.PATHWAY_OPENED || e.getType()==ApplicationEvent.PATHWAY_NEW){
			
			//jta.setText("");
			clearTableRows();
			
			// setting/clearing the rightclick related stuff
			ignoredErrorTypesList.clear();
			subMenu4.setEnabled(false);
			while(subMenu4.getMenuComponentCount()>2){// clearing the subMenu4's checkboxes
				subMenu4.remove(2);
			}
			
			
			jcBox.setEnabled(false);jcb.setEnabled(false);
			errorCounter=0;
			eLabel.setText("Errors:0");wLabel.setText("Warnings:0");
			
			//mimf=new MIMFormat();
			// thread code for export when a pathway is opened, for making the validation faster
			/*new Thread(){
				public void run(){
					try {
						doExport=true;
						mimf.doExport(currentPathwayFile, eng.getActivePathway());
				
					} catch (ConverterException e1) {
						e1.printStackTrace();
					}
				}
			}.start();*/
			
			System.out.println("event pathway opened occured or event new  pathway occured");
		
		}
		
		/*else if(e.getType()==ApplicationEvent.PATHWAY_NEW){
			
			jta.setText("");
			jcBox.setEnabled(false);jcb.setEnabled(false);
			errorCounter=0;
			eLabel.setText("Errors:0");wLabel.setText("Warnings:0");
			System.out.println("event new  pathway occured");
		
		}*/
		
		/*else if(e.getType()==SwingMouseEvent.MOUSE_CLICK){
			System.out.println("mouse clicked");
		}*/
		
	}
	
	public void itemStateChanged(ItemEvent arg0) { // invoked for change in phasebox selection 
		
		if(!changeOfSchema && arg0.getStateChange()==1){
			//phaseBoxSelection=((JComboBox)arg0.getSource()).getSelectedIndex();
			
			
			if(!schemaFileType.equalsIgnoreCase("groovy")){
				//donot forget to change the index if the "Phase: " format is changed
				String temp=( (String)arg0.getItem() ).substring(7);
			
				if(temp.equals("All")){
					SaxonTransformer.transformer1.clearParameters();
				}
				else{
					SaxonTransformer.transformer1.setParameter("phase", temp );
				}
			
				
			System.out.println("item selected --"+temp );
			}
			if(eng.hasVPathway()) valbutton.doClick();
		}
	
	}
	
}	