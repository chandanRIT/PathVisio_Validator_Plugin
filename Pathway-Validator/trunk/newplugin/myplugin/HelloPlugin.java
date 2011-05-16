package myplugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
//import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
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
import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.pathvisio.core.Engine;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;


/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class HelloPlugin implements Plugin,ActionListener,HyperlinkListener
{
	private PvDesktop desktop;
	//private JButton valbutton;
	private JEditorPane jta;
	private File schemaFile=null,currentPathwayFile=null;
	//private JPanel mySideBarPanel ;
	//private JScrollPane scrollPane;
	private VPathwayElement prevPwe;
	private Engine eng;
	private Pathway pth;
	private static Color col1,col2;
	
	enum SchemaPreference implements Preference
    {
            LAST_OPENED_SCHEMA_DIR (System.getProperty("user.home"));

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
			col1=new Color(255,0,0);
			col2=new Color(0,0,255);
		}
			
		//chandan : creating a button for choosing the schema file 
		JButton chooseSchema=new JButton("choose Schema");
		chooseSchema.setActionCommand("choose");
		chooseSchema.addActionListener(this);
		//chooseSchema.setHorizontalAlignment(JButton.RIGHT);
	    //chandan
		
		// save the desktop reference so we can use it later
		this.desktop = desktop;
		
		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Help", helloAction);
		
		JPanel mySideBarPanel = new JPanel (new GridBagLayout());
		jta=new JEditorPane("text/html","");
		jta.addHyperlinkListener(this);
		jta.setEditable(false);
		jta.setFont(new Font("Times New Roman",Font.PLAIN,10));
		JScrollPane scrollPane = new JScrollPane(jta);
		
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        //c.fill = GridBagConstraints.HORIZONTAL;
        //add(textField, c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        mySideBarPanel.add(scrollPane,c);
        //jta.setCaretPosition(jta.getDocument().getLength());

        // mySideBarPanel.setLayout (new FlowLayout(FlowLayout.CENTER));
      /* for(int i=0;i<8;i++){
        jta.append("error @ GraphId : e02bf - An interaction should not start and end with Line arrowheads."+"\n");
        }*/
 
        JButton valbutton=new JButton("validate");
        valbutton.setActionCommand("validate");
        valbutton.addActionListener(this);
        c.gridwidth=GridBagConstraints.RELATIVE;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.weighty=0.0;c.weightx=1.0;
        mySideBarPanel.add(valbutton,c);
        mySideBarPanel.add(chooseSchema,c);
        
        // get a reference to the sidebar
        JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        
        //JTabbedPane bottomTabbedPane= new JTabbedPane();
        //bottomTabbedPane.add("validation",mySideBarPanel);
        
        sidebarTabbedPane.add("pathway-validator", mySideBarPanel);
        //JPanel mypanel=new JPanel();
        //mypanel.add(bottomTabbedPane);
        //desktop.getFrame().add(mypanel);
       // desktop.getFrame().validate();
        // add or panel with a given Title
        
        //sidebarTabbedPane.setLayout(DEFAULT);

    	//sidebarTabbedPane.add(new JButton("Button 1"));
        //sidebarTabbedPane.ad
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
			putValue (NAME, "Welcome message");
		}
		
		/**
		 *  called when the user selects the menu item
		 */

		public void actionPerformed(ActionEvent arg0) 
		{	//currentPathwayFile=new File("C:\\Users\\kayne\\Desktop\\currentPathwaytmp.mimml");
		
			JOptionPane.showMessageDialog(
					desktop.getFrame(), 
					"Hello World");	
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		VPathwayElement vpe=null;
		PathwayElement pe;
		
		if ("validate".equals(e.getActionCommand())) {
			System.out.println("validate button pressed ");
			
			if(schemaFile==null){
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Please set the schema to use and then press validate");
				return;
			}
			
			
			 eng=desktop.getSwingEngine().getEngine();
		
			if(eng.hasVPathway()){
				MIMFormat mimf=new MIMFormat();
				try {
				mimf.doExport(currentPathwayFile, eng.getActivePathway());
				} catch (ConverterException e1) {
				// TODO Auto-generated catch block
				System.out.println("converter Exception");
				e1.printStackTrace();
				}
				
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
			
			SchematronTask st=new SchematronTask();
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
            st.execute();
            //jta.setText(null);
            StringBuffer sbf=new StringBuffer();
            String tempSt,tempsubSt;pth=eng.getActivePathway();
            int errorCounter=0;
            while (st.failed_itr.hasNext()) {
            	errorCounter+=1;
            	tempSt=st.failed_itr.next();
            	sbf.append(errorCounter+".) "+tempSt+"<br><br>");
            	//jta.(tempSt+"\n");
            	
            	tempsubSt=null;
            	tempsubSt=tempSt.substring(18+9,18+9+5);
            	//System.out.println("the id--"+tempsubSt);
            	
            	if(tempsubSt!=null){
            	pe=pth.getElementById(tempsubSt);
    			vpe=eng.getActiveVPathway().getPathwayElementView(pe);
    			vpe.highlight(col2);
    			}
            	else System.out.println("no id provided thus no highlight");
            }
            jta.setText(sbf.toString());
            jta.validate();
            //jta.setCaretPosition(jta.getDocument().getLength());
	        //System.out.println("Hello World!-"+Path.systemClasspath);	
        }
		else if ("choose".equals(e.getActionCommand())) {
			
			System.out.println("choose schema button pressed");
			
			JFileChooser chooser = new JFileChooser();
		    chooser.setDialogTitle("schema to use");
		    chooser.setApproveButtonText("use schema");
		    chooser.setAcceptAllFileFilterUsed(false);
			chooser.setCurrentDirectory(PreferenceManager.getCurrent().getFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR));
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("schema files (*.sch)", "sch");
		    
		    chooser.setFileFilter(filter);
		  
		    int returnVal = chooser.showOpenDialog(desktop.getFrame());
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: "+chooser.getSelectedFile().getName());
		       //System.out.println(System.getProperty("user.home")+" -- "+System.getProperty("user.dir"));
		       schemaFile=chooser.getSelectedFile();
		       PreferenceManager.getCurrent().setFile(SchemaPreference.LAST_OPENED_SCHEMA_DIR, schemaFile);
		    }
					   
		}
		
	}
	@Override
	public void hyperlinkUpdate(HyperlinkEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			
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
	
}