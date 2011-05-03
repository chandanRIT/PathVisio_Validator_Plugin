package myplugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.apache.tools.ant.types.Path;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.plugin.Plugin;

/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class HelloPlugin implements Plugin,ActionListener
{
	private PvDesktop desktop;
	JButton valbutton;
	public void init(PvDesktop desktop) 
	{
		// save the desktop reference so we can use it later
		this.desktop = desktop;
		
		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Help", helloAction);
		JPanel mySideBarPanel = new JPanel ();
        mySideBarPanel.setLayout (new FlowLayout(FlowLayout.CENTER));
        mySideBarPanel.add (new JLabel ("Hello SideBar"), BorderLayout.CENTER);
        valbutton=new JButton("validate");
        valbutton.setActionCommand("display");
        valbutton.addActionListener(this);
        mySideBarPanel.add(valbutton);
        // get a reference to the sidebar
        JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        
        // add or panel with a given Title
        sidebarTabbedPane.add("Title", mySideBarPanel);
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
			
			SchematronTask st=new SchematronTask();
            st.setSchema(new File("D:\\schematron\\input.sch"));
            st.setQueryLanguageBinding("xslt2");
            //st.setOutputEncoding(null);
            st.setOutputDir("C:\\Users\\kayne\\Desktop");
            st.setFormat("svrl");
            st.setFile(new File("D:\\schematron\\input.xml"));
            st.setClasspath(Path.systemClasspath);
            //st.setFileDirParameter(null);
            //st.setArchiveNameParameter(null);
            //st.setFileNameParameter(null);
            //st.setarchiveDirParameter(null);
            st.setPhase(null);
           st.execute();
	
	System.out.println("Hello World!-"+Path.systemClasspath);	
        System.out.println("yes ");    
		} 
		
	}

	
}