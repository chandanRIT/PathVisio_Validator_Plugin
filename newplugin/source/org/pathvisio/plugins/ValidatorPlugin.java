package org.pathvisio.plugins;


import java.awt.Color;
//import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
//import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
//import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
//import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
//import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;


import org.jdesktop.swingworker.SwingWorker;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.gui.swing.CommonActions;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.swing.SwingMouseEvent;
import org.xml.sax.SAXException;

//import org.pathvisio.desktop.PvDesktop;
//import org.pathvisio.desktop.plugin.Plugin;
//import org.pathvisio.gui.ProgressDialog;

import edu.stanford.ejalbert.BrowserLauncher;

public class ValidatorPlugin implements Plugin,ActionListener,HyperlinkListener, ApplicationEventListener, ItemListener
{
	private  PvDesktop desktop;
	//private JButton valbutton;
	private final static JEditorPane jta=new JEditorPane("text/html","");
	private static File  schemaFile;
	private static File currentPathwayFile;
	//private JPanel mySideBarPanel ;
	//private JScrollPane scrollPane;
	private static VPathwayElement prevPwe;
	private static Engine eng;
	private static Pathway pth;
	private static final  Color col1= new Color(255,0,0),col2=new Color(0,0,255);
	//private final  SchematronTask st=new SchematronTask();
	private  static SaxonTransformer saxTfr ;
	private  static MIMFormat mimf=new MIMFormat();
	private final static JFileChooser chooser=new JFileChooser();
	private final static JButton valbutton=new JButton("Validate");
	private static int errorCounter,prevSelect;
	private final static JButton chooseSchema=new JButton("Choose Ruleset"); 
	private final static JComboBox jcBox = new JComboBox(new String[]{"Errors & Warnings","Errors only","Warnings only"});
	private final static JComboBox phaseBox = new JComboBox(new String[]{"Phase: All"});
	private final HelloAction helloAction= new HelloAction();
	private static boolean prevHighlight=true;
	private final String imageE_UrlSrcAttr = (getClass().getResource("/error.png")).toString();
    private final String imageW_UrlSrcAttr = (getClass().getResource("/warning.gif")).toString();
    //final String imageUrlE="<img width='12' height='12' src='file:"+System.getProperty("user.dir")+java.io.File.separatorChar+"images"+java.io.File.separatorChar;
    private final String imageUrlE="<img width='11' height='11' src='"+imageE_UrlSrcAttr+"'></img> &nbsp;";
    //final String imageUrlW="<img width='15' height='15' src='file:"+System.getProperty("user.dir")+java.io.File.separatorChar+"images"+java.io.File.separatorChar;
    private final String imageUrlW="<img width='15' height='15' src='"+imageW_UrlSrcAttr+"'></img> &nbsp;";
    private static JCheckBox jcb;
    private final JLabel eLabel=new JLabel("Errors:0",new ImageIcon(getClass().getResource("/error.png")),SwingConstants.CENTER),
    wLabel=new JLabel("Warnings:0",new ImageIcon(getClass().getResource("/warning.png")),SwingConstants.CENTER);
    private static final JTextField schemaTitleTag= new JTextField("  Schema Title: ");
	//private static boolean doExport=false;
	private static String schemaFileType;
	private static Thread threadForSax;
	
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
            LAST_OPENED_SCHEMA_DIR (System.getProperty("user.home")),
            CHECK_BOX_STATUS ("0");
            
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
		//chooseSchema.setHorizontalAlignment(JButton.RIGHT);
	    
		//creating a jcheckbox and set its status from the .pathvisio pref file
	    boolean jbcinit;
		if(PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS)==1){
	    	jbcinit=true;
	    }else jbcinit=false;
		
		final JCheckBox svrlOutputChoose= new JCheckBox(" Generate SVRL file",false);
		svrlOutputChoose.setActionCommand("svrlOutputChoose");
		svrlOutputChoose.addActionListener(this);
		//svrlOutputChoose.setEnabled(false);
		
		
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
		eng.addApplicationEventListener(this); //done to listen to the event from the engine (pthaway-opened event) 
		
		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Help", helloAction);
		
		final JPanel mySideBarPanel = new JPanel (new GridBagLayout());
		//Font f=new Font("Times New Roman", Font.BOLD, 16);
		//System.out.println("the font used"+f);
		//jta.setFont(f);
		//jta=new JEditorPane("text/html","");
		jta.addHyperlinkListener(this);
		jta.setEditable(false);
		
		//adding labels for warning and error counts
		eLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		wLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		final JScrollPane scrollPane = new JScrollPane(jta);
		
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
        
        c.gridwidth = GridBagConstraints.REMAINDER;
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
        
        //phaseBox.addItem("chandan");
		
        //jta.setCaretPosition(jta.getDocument().getLength());
        // mySideBarPanel.setLayout (new FlowLayout(FlowLayout.CENTER));
        /* for(int i=0;i<8;i++){
        jta.append("error @ GraphId : e02bf - An interaction should not start and end with Line arrowheads."+"\n");
        }*/
 
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
        //setting the winx, winy values 
        
        //code for setting the winx and winy of main window of pathvisio in preference file
        //PreferenceManager.getCurrent().set(GlobalPreference.WIN_X, "0");
        //PreferenceManager.getCurrent().set(GlobalPreference.WIN_Y,"0");
        
        //JPanel mypanel=new JPanel();
        //mypanel.add(bottomTabbedPane);
        //desktop.getFrame().add(mypanel);
       // desktop.getFrame().validate();
        // add or panel with a given Title
        
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
				currentPathwayFile=File.createTempFile("pvv","val");currentPathwayFile.deleteOnExit();
			} catch (Exception e) {
				System.out.println("Exception in creating current pathway temp file "+e);
				e.printStackTrace();
				
			}
			//col1=new Color(255,0,0);
			//col2=new Color(0,0,255);
		}
        
	}
	public void done() {}
	
	/**
	 * Display a welcome message when this action is triggered. 
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
		final ProgressDialog d = new ProgressDialog(desktop.getFrame(),"", pk, false, true);
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
		
		protected Object doInBackground() {
					
					pk.setTaskName("Validating pathway");
				
					//if(schemaFile.)	
					//MIMFormat mimf=new MIMFormat();
					//SchematronTask st=new SchematronTask();
					try {
						
						System.out.println("b4 export called");
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
		StringBuilder sbf=new StringBuilder();
        String tempSt,tempsubSt;pth=eng.getActivePathway();
        Iterator<String> tempIterator = (saxTfr.diagnosticReference).iterator();
        int i=0,j=0,k=0,eCount=0,wCount=0;
        String imageUrl=imageUrlE;
        
        sbf.append("<font size='4' face='verdana'>");
        int higco=0;//int highlightFlag=1;// 1 for highlight, 0 for unhighlight 
        
        eng.getActiveVPathway().resetHighlight();//unhighlight all nodes
        
        while (tempIterator.hasNext()) {
         	errorCounter+=1;
         	tempSt=tempIterator.next();
         	
         	if(tempSt.startsWith("warning")){ imageUrl=imageUrlW; wCount++;}else { imageUrl=imageUrlE; eCount++;}
         	
         	if(prevSelect==0){
         		sbf.append(imageUrl + ++i +".) "+tempSt+"<br><br>");
         		//System.out.println("prevsel 0");
         	}
         	else if(prevSelect==1 && tempSt.startsWith("error")){
         		//System.out.println("prevsel 1");
         		sbf.append(imageUrl + ++j +".) "+tempSt+"<br><br>");	
         	}
         	else if(prevSelect==2 && tempSt.startsWith("warning")){
         		//System.out.println("prevsel 2");
         		sbf.append(imageUrl + ++k +".) "+tempSt+"<br><br>");	
         	}
         	else{
         		System.out.println("not passed"); 
         		//make tempSt null , so that only the corresponding nodes are highlighted, when selecting the drop down (E / W / E&W)
         		tempSt=null;
         		//highlightFlag=0;//for unhighlight method
         	}
         	
         	eLabel.setText("Errors:"+eCount); wLabel.setText("Warnings:"+wCount);
         	
         	if(tempSt!=null){
         		tempsubSt=null;
         	//tempsubSt=tempSt.substring(18+9,18+9+5);
         	
         		tempsubSt=tempSt.substring(tempSt.indexOf(' ')+22,tempSt.indexOf('>')-1);
         	//System.out.println("the id--"+tempsubSt);
         	
         		pe=pth.getElementById(tempsubSt);
         		//System.out.println(++higco+" --> "+tempsubSt);
 			
         		if(pe!=null) {
         			vpe=eng.getActiveVPathway().getPathwayElementView(pe);
         			vpe.highlight(col2);
         		}
         		else System.out.println("id not parsed properly @ id "+tempsubSt);
         	
         	}
         	prevPwe=vpe;
        }
        
        //refreshing the pathway , so that all the nodes highlighted appear highlighted
        VPathway vpwTemp = eng.getActiveVPathway();
		vpwTemp.setPctZoom(vpwTemp.getPctZoom());
		
        sbf.append("</font>");
        
        if( (prevSelect==0 && i!=0) || (prevSelect==1 && j!=0) || (prevSelect==2 && k!=0) ){ 
        	jta.setText(sbf.toString());
        }
        else if(prevSelect==0){
        	jta.setText("<b><font size='4' face='verdana'>No Errors and Warnings</font></b>");
        }
        else if(prevSelect==1){
        	jta.setText("<b><font size='4' face='verdana'>No Errors</font></b>");	
        }
        else if(prevSelect==2){
        	jta.setText("<b><font size='4' face='verdana'>No Warnings</font></b>");	
        }
        
        jta.setCaretPosition(0);
        sbf.setLength(0); 
        
	}

	/*private void ExtractPhaseValuesFromSchema(File sf){
		
		String line;
		int phaseNumber=0;
		ArrayList<String> PhaseValues= new ArrayList<String>();
		boolean titleTagFound=false;
		
		try {
		BufferedReader reader = new BufferedReader(new FileReader(sf));
		int lineNo=0;
		
			while ((line=reader.readLine())!=null) {
				lineNo+=1;
				
				if(!titleTagFound){
					if((line.indexOf("<iso:title"))!=-1){
						schemaTitleTag.setText("Schema Title: "+line.substring(line.indexOf('>')+1, line.indexOf("/")-1));
						titleTagFound=true;
					}
				}
				
				if((line.indexOf("<iso:phase "))!=-1){
					phaseNumber++;
					System.out.println("1st <iso:phase> tag found at line--"+lineNo);
					PhaseValues.add(line.substring(line.indexOf(" id=")+5, line.indexOf(">")-1));
				
				}
				if(line.indexOf("<iso:pattern ")!=-1){
					System.out.println("phase finding is over");
					break;
				}
			}
		}
		catch(Exception ex) {
			System.out.println("Exception in the whichSchema method");
			ex.printStackTrace();
		}	
			
			if(!phaseBox.isEnabled())
				phaseBox.setEnabled(true);
			
			Iterator<String> tempIterator= PhaseValues.iterator();
			
			//System.out.println("the item count of phaseBox is "+phaseBox.getItemCount());
			
			//refreshing the drop down to include phases of the selected schema by clearing out the previous items and adding new ones
			while(phaseBox.getItemCount()!=1){
				phaseBox.removeItemAt(phaseBox.getItemCount()-1);
			}
			
			while(tempIterator.hasNext()){
				phaseBox.addItem("Phase: "+tempIterator.next());
				//System.out.println(tempIterator.next());
			}
			//return PhaseValues;
	}
	
	private String whichSchema(File sf){
		
		boolean lineFound=false;int index=0;
		String line,schemaType=null;
		
		try {
		BufferedReader reader = new BufferedReader(new FileReader(sf));
		int lineNo=0;
		
			while ((line=reader.readLine())!=null) {
				lineNo+=1;
				if((index=line.indexOf("<iso:ns"))!=-1){
				
				System.out.println("1st <iso:ns> tag found at line--"+lineNo);
				schemaType=line.substring(index+16, line.indexOf("uri")-2);
				lineFound=true;
				break;
				}
				else {
			//System.out.println("this line has no <iso:ns> tag");
				}
			}

		}
		catch(Exception ex) {
			System.out.println("Exception in the whichSchema method");
			ex.printStackTrace();
		}
		
		ExtractPhaseValuesFromSchema(sf);
		
		if(lineFound==true){
			return schemaType;
		}
		else{
			return "no iso:ns tag found";
		}
	
	}*/
	
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
		
		schemaTitleTag.setText("Schema Title: "+mySHandler.getTheTitle());
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
		
	//@Override
	public void actionPerformed(ActionEvent e) {
		
		if ("validate".equals(e.getActionCommand())) {
			System.out.println("validate button pressed ");
			
			if(schemaFile==null){
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Please choose a Ruleset and then press validate");
				System.out.println("after ok");
				
				chooseSchema.doClick();
				
				return;
			}
			
			if(eng.hasVPathway()){
				errorCounter=0;
//set the below line, to make the drop down option to errors and warnings (default option), when validate is pressed
				prevSelect=0;jcBox.setSelectedIndex(0);
				
				validatePathway(saxTfr,mimf);
				jcBox.setEnabled(true);jcb.setEnabled(true);
				
			}
			else{
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"There's no pathway open to validate");
				return;
			}
			
			printOnPanel();
			
		}
		else if ("choose".equals(e.getActionCommand())) {
			
			System.out.println("choose schema button pressed");
			//JFileChooser
			//chooser = new JFileChooser();
			if(chooser.getDialogTitle()==null){
				   System.out.println("choose pressed for 1st time");
			threadForSax=new Thread(){ 
			  public void run(){
			   	
				   try {
					   System.out.println("This thread for saxtranform runs");
					   saxTfr= new SaxonTransformer();SaxonTransformer.setInputFile(currentPathwayFile);
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
		    			
		    			String ext = f.toString().substring(f.toString().length() - 3);
					
		    			if(ext.equalsIgnoreCase("sch")) {
		    				return true;
		    			}
					
		    			return false;
		    		}
				
		    		public String getDescription() {
		    			return "Schematron files (*.sch)";
		    		}

		    	});

		    }
		    
		    int returnVal = chooser.showOpenDialog(desktop.getFrame());
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       
		        System.out.println("You chose to open this file: "+chooser.getSelectedFile().getName());
		        schemaFile=chooser.getSelectedFile();
		       //System.out.println("schema is of type: "+(schemaFileType=whichSchema(schemaFile)));
		       
		       parseSchemaAndSetValues();
		       
		       
		       PreferenceManager.getCurrent().setFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR, schemaFile);
		       
		     //wait for the transformer creation in the thread to complete 
			try{
				   threadForSax.join();
			   } catch (InterruptedException e1) {
				   // TODO Auto-generated catch block
				   e1.printStackTrace();
			   }
		    }
					   
		}
		else if("jcb".equals(e.getActionCommand())){
			//eng.getActiveVPathway().resetHighlight();
				if(((JCheckBox)e.getSource()).isSelected()){
					System.out.println("jcb selected");
					//valbutton.setEnabled(false);
					printOnPanel();//call only the highlighting part, (highlight all!)
					PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,1);
				}else{
					System.out.println("jcb deselected");
					//valbutton.setEnabled(true);
					PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,0);
					eng.getActiveVPathway().resetHighlight();//unhighlight all
				}
			//System.out.println("some event fired from jcb--"+PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS));
						
		}
		
		else if("jcBox".equals(e.getActionCommand())){
			
			JComboBox cbox = (JComboBox)e.getSource();
			
			if(prevSelect != cbox.getSelectedIndex()) {
				prevSelect = cbox.getSelectedIndex();
				printOnPanel();
				System.out.println(cbox.getSelectedItem());
			}
		}
		
		else if("svrlOutputChoose".equals(e.getActionCommand())){
			if( ((JCheckBox)e.getSource()).isSelected() ){
				SaxonTransformer.setProduceSvrl(true); 
			}else SaxonTransformer.setProduceSvrl(false);
			
		}	
	}
	//@Override
	public void hyperlinkUpdate(HyperlinkEvent arg0) {
	
		if (arg0.getEventType()== HyperlinkEvent.EventType.ACTIVATED) {
			
			if( ! prevPwe.isHighlighted() ){prevHighlight=false; }
			
				if(prevHighlight){
				prevPwe.highlight(col2);
				}//col2 is blue
				else prevPwe.unhighlight();
			
			
			System.out.println("hi there-"+arg0.getDescription());
			
			PathwayElement	pe=pth.getElementById(arg0.getDescription());
			VPathwayElement	vpe=eng.getActiveVPathway().getPathwayElementView(pe);
			vpe.highlight(col1);//col1 is red
			prevPwe=vpe;
			
			//pathway diagram refresh code,setting zoom refreshes the diagram.
			VPathway vpwTemp = eng.getActiveVPathway();
			vpwTemp.setPctZoom(vpwTemp.getPctZoom());
			
	   }
	}
	//@Override
	public void applicationEvent(ApplicationEvent e) {
		
		//System.out.println("event occured");
		if( e.getType()==ApplicationEvent.PATHWAY_OPENED){
			jta.setText("");
			jcBox.setEnabled(false);jcb.setEnabled(false);
			errorCounter=0;eLabel.setText("Errors:0");wLabel.setText("Warnings:0");
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
			
			System.out.println("event pathway opened occured");
		}
		else if(e.getType()==ApplicationEvent.PATHWAY_NEW){
			jta.setText("");
			jcBox.setEnabled(false);jcb.setEnabled(false);
			errorCounter=0;eLabel.setText("Errors:0");wLabel.setText("Warnings:0");
			System.out.println("event new  pathway occured");
		}
		else if(e.getType()==SwingMouseEvent.MOUSE_CLICK){
			System.out.println("mouse clicked");
		}
		
	}
	public void itemStateChanged(ItemEvent arg0) {
		if(arg0.getStateChange()==1){
			//donot forget to change the index if the "Phase: " format is changed
			String temp=( (String)arg0.getItem() ).substring(7);
			if(temp.equals("All")){
				SaxonTransformer.transformer1.clearParameters();
			}
			else{
				SaxonTransformer.transformer1.setParameter("phase", temp );
			}
			if(eng.hasVPathway()) valbutton.doClick();
			System.out.println("item selected --"+temp );
			
		}
	}
	
}	