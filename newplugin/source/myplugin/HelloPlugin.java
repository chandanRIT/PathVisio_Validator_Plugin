package myplugin;

import java.awt.Color;
//import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
//import java.net.URI;
import java.util.concurrent.ExecutionException;
//import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
//import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
//import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.jdesktop.swingworker.SwingWorker;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.ProgressDialog;

import edu.stanford.ejalbert.BrowserLauncher;


/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class HelloPlugin implements Plugin,ActionListener,HyperlinkListener, ApplicationEventListener
{
	private  PvDesktop desktop;
	//private JButton valbutton;
	private final  JEditorPane jta=new JEditorPane("text/html","");
	private static File  schemaFile=null;
	private static File currentPathwayFile=null;
	//private JPanel mySideBarPanel ;
	//private JScrollPane scrollPane;
	private static VPathwayElement prevPwe;
	private static Engine eng;
	private static Pathway pth;
	private final  Color col1=new Color(255,0,0),col2=new Color(0,0,255);
	private final  SchematronTask st=new SchematronTask();
	private final  MIMFormat mimf=new MIMFormat();
	private final  JFileChooser chooser=new JFileChooser();
	private final JButton valbutton=new JButton("validate");
	
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
		if(currentPathwayFile==null){
			//comment the below line for normal jfile chooser functionality
			schemaFile=new File("D:\\schematron\\mimschema.sch");
			//commented below to remove  hardcode
			//currentPathwayFile=new File("C:\\Users\\kayne\\Desktop\\currentPathwaytmp.mimml");
			try {
				currentPathwayFile=File.createTempFile("pvv","val");currentPathwayFile.deleteOnExit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Exception in creating current pathway temp file "+e);
				e.printStackTrace();
				
			}
			//col1=new Color(255,0,0);
			//col2=new Color(0,0,255);
		}
			
		//chandan : creating a button for choosing the schema file 
		
		final JButton chooseSchema=new JButton("choose Schema");
		chooseSchema.setActionCommand("choose");
		chooseSchema.addActionListener(this);
		//chooseSchema.setHorizontalAlignment(JButton.RIGHT);
	    
		//creating a jcheckbox and set its status from the .pathvisio pref file
	    boolean jbcinit;
		if(PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS)==1){
	    	jbcinit=true;
	    }else jbcinit=false;
		final JCheckBox jcb= new JCheckBox("real-time validation support", jbcinit);
		jcb.setActionCommand("jcb");
		jcb.addActionListener(this);
		//valbutton.setEnabled(!jbcinit);
		//chandan
		
		// save the desktop reference so we can use it later
		this.desktop = desktop;
		eng=desktop.getSwingEngine().getEngine();
		eng.addApplicationEventListener(this); //done to listen to the event from the engine (pthaway-opened event) 
		
		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Help", helloAction);
		
		final JPanel mySideBarPanel = new JPanel (new GridBagLayout());
		
		//jta=new JEditorPane("text/html","");
		jta.addHyperlinkListener(this);
		jta.setEditable(false);
		jta.setFont(new Font("Times New Roman",Font.PLAIN,10));
		
		final JScrollPane scrollPane = new JScrollPane(jta);
		
        final GridBagConstraints c = new GridBagConstraints();
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        //c.fill = GridBagConstraints.HORIZONTAL;
        //add(textField, c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        mySideBarPanel.add(scrollPane,c);
        
        c.weighty = 0.0;
        mySideBarPanel.add(jcb,c);
        
        //jta.setCaretPosition(jta.getDocument().getLength());
        // mySideBarPanel.setLayout (new FlowLayout(FlowLayout.CENTER));
      /* for(int i=0;i<8;i++){
        jta.append("error @ GraphId : e02bf - An interaction should not start and end with Line arrowheads."+"\n");
        }*/
 
        //final JButton valbutton=new JButton("validate");
        valbutton.setActionCommand("validate");
        valbutton.addActionListener(this);
        
        c.gridwidth=GridBagConstraints.RELATIVE;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.weighty=0.0;c.weightx=1.0;
        
        mySideBarPanel.add(valbutton,c);
        mySideBarPanel.add(chooseSchema,c);
        
        // get a reference to the sidebar
        final JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        //JTabbedPane bottomTabbedPane= new JTabbedPane();
        //bottomTabbedPane.add("validation",mySideBarPanel);
        sidebarTabbedPane.add("pathway-validator", mySideBarPanel);
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
	
        //code for the validate button's initialaiztion 
       // mimf=new MIMFormat();
		//st=new SchematronTask();
        st.setQueryLanguageBinding("xslt2");
        st.setOutputDir(currentPathwayFile.getParent());
        st.setFormat("svrl");
        st.setFile(currentPathwayFile);
        st.setSchema(schemaFile);
        
	}
	public void done() {}
	private final HelloAction helloAction = new HelloAction();
	
	/**
	 * Display a welcome message when this action is triggered. 
	 */
	private class HelloAction extends AbstractAction
	{
		HelloAction()
		{
			// The NAME property of an action is used as 
			// the label of the menu item
			putValue (NAME, "Pathway Validator Help");
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
	public SchematronTask processTask(ProgressKeeper pk, ProgressDialog d, SwingWorker<SchematronTask, Object> sw) {
		sw.execute();
		d.setVisible(true);
		try {
			return sw.get();
		} catch (ExecutionException e)
		{
			System.out.println("Execution Exception in helloplugin---"+e.getMessage());
			return null;
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception---"+e.getCause());
			return null;
		}
	}

	public SchematronTask validatePathway(final SchematronTask st,final MIMFormat mimf)
	{
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(desktop.getFrame(),"", pk, false, true);
		SwingWorker<SchematronTask, Object> sw = new SwingWorker<SchematronTask, Object>() {
			protected SchematronTask doInBackground() {
				pk.setTaskName("Validating pathway");
				
					//MIMFormat mimf=new MIMFormat();
					//SchematronTask st=new SchematronTask();
					try {
					System.out.println("b4 mimf export");
					mimf.doExport(currentPathwayFile, eng.getActivePathway());
					st.setSchema(schemaFile);
		      //    st.setQueryLanguageBinding("xslt2");
		            //st.setOutputEncoding(null);
		       //   st.setOutputDir(currentPathwayFile.getParent());
		       //   st.setFormat("svrl");
		     //       st.setFile(currentPathwayFile);
		            //st.setClasspath(Path.systemClasspath);
		            //st.setFileDirParameter(null);
		            //st.setArchiveNameParameter(null);
		            //st.setFileNameParameter(null);
		            //st.setarchiveDirParameter(null);
		            //st.setPhase(null);
					System.out.println("after mimf export and b4 execute");
					st.execute();
					System.out.println("after  st.execute");
					} catch (Exception e1) { //changed from ConverterException to catch all the errors
					// TODO Auto-generated catch block
					System.out.println("converter Exception--"+e1.getMessage());
					e1.printStackTrace();
					return null;
					}
				
				finally {
					pk.finished();
				}

				return st;
			}
		};

		return processTask(pk, d, sw);
	}

	
	void printOnPanel(){
		VPathwayElement vpe=null;
		PathwayElement pe; 
		StringBuffer sbf=new StringBuffer();
        String tempSt,tempsubSt;pth=eng.getActivePathway();
        int errorCounter=0;
        while (st.failed_itr.hasNext()) {
         	errorCounter+=1;
         	tempSt=st.failed_itr.next();
         	sbf.append(errorCounter+".) "+tempSt+"<br><br>");
         	//jta.(tempSt+"\n");
         	
         	tempsubSt=null;
         	//tempsubSt=tempSt.substring(18+9,18+9+5);
         	tempsubSt=tempSt.substring(tempSt.indexOf(' ')+22,tempSt.indexOf('>')-1);
         	//System.out.println("the id--"+tempsubSt);
         	
         	//if(tempsubSt!=null){
         	pe=pth.getElementById(tempsubSt);
 			if(pe!=null) {
         	vpe=eng.getActiveVPathway().getPathwayElementView(pe);
 		//	if(vpe!=null)
 			vpe.highlight(col2);}
 			else System.out.println("Exception due to no associated PathwayElement @ id "+tempsubSt);
         	//}
         	//else System.out.println("no id provided thus no highlight");
         }
        if(errorCounter!=0) 
        {jta.setText(sbf.toString());}
        else {jta.setText("NO errors/warnings in the pathway");}
        jta.setCaretPosition(0);
         // jta.validate();
         //jta.setCaretPosition(jta.getDocument().getLength());
         
	}
	
	String whichSchema(File sf){
		
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
		
		if(lineFound==true){
			return schemaType;
		}
		else{
			return "no iso:ns tag found";
		}
	}
		
	//chandan
	
	//@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//VPathwayElement vpe=null;
		//PathwayElement pe;
		
		if ("validate".equals(e.getActionCommand())) {
			System.out.println("validate button pressed ");
			
			if(schemaFile==null){
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Please set the schema to use and then press validate");
				return;
			}
			
			
			 //eng=desktop.getSwingEngine().getEngine();
			 
			//SchematronTask st;
			if(eng.hasVPathway()){
				validatePathway(st,mimf);
				
				/*MIMFormat mimf=new MIMFormat();
				try {
				mimf.doExport(currentPathwayFile, eng.getActivePathway());
				} catch (ConverterException e1) {
				// TODO Auto-generated catch block
				System.out.println("converter Exception");
				e1.printStackTrace();
				}*/
				
				/*exporting the pathway in gpml format and then accesing the file object */
			/*try {
				GpmlFormat.writeToXml(eng.getActivePathway(), currentPathwayFile, false);
			} catch (ConverterException e1) {
				// TODO Auto-generated catch block
				System.out.println("converter Exception");
				e1.printStackTrace();
			}*/
				
			}
			else{
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"There's no pathway open to validate");
			return;
			}
			
			printOnPanel();
			
			/*SchematronTask st=new SchematronTask();
            st.setSchema(schemaFile);
            st.setQueryLanguageBinding("xslt2");
            //st.setOutputEncoding(null);
            st.setOutputDir(currentPathwayFile.getParent());
            st.setFormat("svrl");
            st.setFile(currentPathwayFile);
            //st.setClasspath(Path.systemClasspath);
            //st.setFileDirParameter(null);
            //st.setArchiveNameParameter(null);
            //st.setFileNameParameter(null);
            //st.setarchiveDirParameter(null);
            //st.setPhase(null);
            st.execute();*/
            //jta.setText(null);
			
        /*    StringBuffer sbf=new StringBuffer();
            String tempSt,tempsubSt;pth=eng.getActivePathway();
            int errorCounter=0;
            while (st.failed_itr.hasNext()) {
            	errorCounter+=1;
            	tempSt=st.failed_itr.next();
            	sbf.append(errorCounter+".) "+tempSt+"<br><br>");
            	//jta.(tempSt+"\n");
            	
            	tempsubSt=null;
            	//tempsubSt=tempSt.substring(18+9,18+9+5);
            	tempsubSt=tempSt.substring(tempSt.indexOf(' ')+22,tempSt.indexOf('>')-1);
            	//System.out.println("the id--"+tempsubSt);
            	
            	//if(tempsubSt!=null){
            	pe=pth.getElementById(tempsubSt);
    			vpe=eng.getActiveVPathway().getPathwayElementView(pe);
    			if(vpe!=null)
    				vpe.highlight(col2);
    			else System.out.println("Exception @ id "+tempsubSt);
            	//}
            	//else System.out.println("no id provided thus no highlight");
            }
            jta.setText(sbf.toString());
            jta.validate();
            //jta.setCaretPosition(jta.getDocument().getLength());
	        //System.out.println("Hello World!-"+Path.systemClasspath);	
     */   
			}
			else if ("choose".equals(e.getActionCommand())) {
			
			System.out.println("choose schema button pressed");
			//JFileChooser
			//chooser = new JFileChooser();
		    if(chooser.getDialogTitle()==null){
			chooser.setDialogTitle("schema to use");
		    chooser.setApproveButtonText("use schema");
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
					return "schema files (*.sch)";
				}

			});

			
		    }
		    int returnVal = chooser.showOpenDialog(desktop.getFrame());
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: "+chooser.getSelectedFile().getName());
		       //System.out.println(System.getProperty("user.home")+" -- "+System.getProperty("user.dir"));
		       schemaFile=chooser.getSelectedFile();
		       System.out.println("schema is of type: "+whichSchema(schemaFile));
		       PreferenceManager.getCurrent().setFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR, schemaFile);
		    }
					   
		}
		else if("jcb".equals(e.getActionCommand())){
			if(((JCheckBox)e.getSource()).isSelected()){
				System.out.println("jcb selected");
				//valbutton.setEnabled(false);
				PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,1);
			}else{
				System.out.println("jcb deselected");
				//valbutton.setEnabled(true);
				PreferenceManager.getCurrent().setInt(SchemaPreference.CHECK_BOX_STATUS,0);
			}
			//System.out.println("some event fired from jcb--"+PreferenceManager.getCurrent().getInt(SchemaPreference.CHECK_BOX_STATUS));
						
		}
		
	}
	//@Override
	public void hyperlinkUpdate(HyperlinkEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getEventType()== HyperlinkEvent.EventType.ACTIVATED) {
			
			if(prevPwe!=null){
			prevPwe.highlight(col2);
			}
			System.out.println("hi there-"+arg0.getDescription());
			
			PathwayElement	pe=pth.getElementById(arg0.getDescription());
			VPathwayElement	vpe=eng.getActiveVPathway().getPathwayElementView(pe);
			vpe.highlight(col1);
			prevPwe=vpe;
	    }
	}
	//@Override
	public void applicationEvent(ApplicationEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("event occured");
		if( e.getType()==ApplicationEvent.PATHWAY_OPENED){
			jta.setText("");
			System.out.println("event pathway opened occured");
		}
		
	}
	
}