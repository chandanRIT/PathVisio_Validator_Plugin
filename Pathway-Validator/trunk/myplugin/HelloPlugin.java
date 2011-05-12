package myplugin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.plugin.Plugin;

/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class HelloPlugin implements Plugin,ActionListener
{
	private PvDesktop desktop;
	private JButton valbutton;
	private JTextArea jta;
	private File schemaFile=null;
	private File currentPathwayFile;
	
	public void init(PvDesktop desktop) 
	{   currentPathwayFile=new File("C:\\Users\\kayne\\Desktop\\currentPathwaytmp.mimml");
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
		jta=new JTextArea();
		jta.setEditable(false);
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
 
        valbutton=new JButton("validate");
        valbutton.setActionCommand("display");
        valbutton.addActionListener(this);
        mySideBarPanel.add(valbutton);
        mySideBarPanel.add(chooseSchema);
        
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
		{
			JOptionPane.showMessageDialog(
					desktop.getFrame(), 
					"Hello World");	
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if ("display".equals(e.getActionCommand())) {
			
			if(schemaFile==null){
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Please set the schema to use and then press validate");
				return;
			}
			SwingEngine se=desktop.getSwingEngine();
			Engine eng=se.getEngine();
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
			else {
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"There's no pathway open to validate");
			return;
			}
			SchematronTask st=new SchematronTask();
            st.setSchema(schemaFile);
            st.setQueryLanguageBinding("xslt2");
            //st.setOutputEncoding(null);
            st.setOutputDir("C:\\Users\\kayne\\Desktop");
            st.setFormat("svrl");
            st.setFile(currentPathwayFile);
            //st.setClasspath(Path.systemClasspath);
            //st.setFileDirParameter(null);
            //st.setArchiveNameParameter(null);
            //st.setFileNameParameter(null);
            //st.setarchiveDirParameter(null);
            //st.setPhase(null);
            st.execute();
            jta.setText(null);
            
            while (st.failed_itr.hasNext()) {
            	jta.append(st.failed_itr.next()+"\n");
            }
            //jta.setCaretPosition(jta.getDocument().getLength());
	        //System.out.println("Hello World!-"+Path.systemClasspath);	
            System.out.println("validate button pressed ");    
		
		}
		else{
			
			System.out.println("choose schema button pressed");
			
			JFileChooser chooser = new JFileChooser();
		    chooser.setDialogTitle("schema to use");
		    chooser.setApproveButtonText("use schema");
			
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "schema files(.sch)", "sch");
		    
		    chooser.setFileFilter(filter);
		  
		    int returnVal = chooser.showOpenDialog(desktop.getFrame());
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: " +
		            chooser.getSelectedFile().getName());
		       schemaFile=chooser.getSelectedFile();
		    }
					   
		}
		
	}

	
}