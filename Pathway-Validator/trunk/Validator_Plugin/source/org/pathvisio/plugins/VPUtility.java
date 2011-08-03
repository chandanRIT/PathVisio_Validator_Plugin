package org.pathvisio.plugins;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import javax.swing.table.DefaultTableModel;

import org.pathvisio.preferences.Preference;
import org.pathvisio.view.VPathwayElement;

public class VPUtility {
	static final String USER_DIR = System.getProperty("user.home");
	static ImageIcon eIcon;
	static ImageIcon wIcon;
	static boolean prevHighlight=true;
	static boolean changeOfSchema=false;
	static boolean allIgnored;
	static int prevSelect;
	static Color col1;//= new Color(255,0,0),col2=new Color(0,0,255);
	static Color col2;
	static VPathwayElement prevPwe;
	static String schemaFileType;
	static String schemaString;
	
	
	enum SchemaPreference implements Preference
	{
		LAST_OPENED_SCHEMA_DIR (VPUtility.USER_DIR),
		CHECK_BOX_STATUS ("0"),APPLY_IGNORED_RULES_CHECKBOX ("0"),SVRL_FILE (VPUtility.USER_DIR);

		private String defaultValue;
		SchemaPreference (String defaultValue) 
		{
			this.defaultValue = defaultValue;
		}

		public String getDefault() {
			return defaultValue;
		}               
	}
	

	static void resetPhaseBox(JComboBox phaseBox){
		if(!phaseBox.isEnabled())
			phaseBox.setEnabled(true);
		
		//refreshing the drop down to include phases of the selected schema by clearing out the previous items and adding new ones
		while(phaseBox.getItemCount()!=1){
			phaseBox.removeItemAt(phaseBox.getItemCount()-1);
		}
		
	}
	
	static String cutSchemaTitleString(String ss,JTextField schemaTitleTag){
		
		FontMetrics fm= schemaTitleTag.getFontMetrics(schemaTitleTag.getFont());
		int fontWidth=fm.stringWidth(ss);
		int TFwidfth= schemaTitleTag.getWidth()-77;
		if(fontWidth>=TFwidfth)
		{	schemaTitleTag.setToolTipText(ss);
			for(int index=ss.length()-1; index>0 ; index=index-2)// for faster looping
			{	
				fontWidth=fm.stringWidth(ss.substring(0,index));
				if (fontWidth<TFwidfth){ 
					ss=ss.substring(0,index-1)+"..";
					//System.out.println(index);
					break;
				}
		
			}
		} else 
			schemaTitleTag.setToolTipText(null);
		
		schemaTitleTag.setText("Schema Title: "+ss);
		schemaTitleTag.setCaretPosition(0);
		return ss;
	}

	/**
	 * custom JTable class to override the the method "isCellEditable", in order to
	 *  render all the cells un-editable for the table 
	 */
	static class MyTableModel extends DefaultTableModel{
		public boolean isCellEditable(int row, int column){  
		    return false;  
		  }  
	}
	
	static class CustomMenuItem extends JMenuItem {

		    public CustomMenuItem(String text) {
		        super(text);
		    }

		    public CustomMenuItem() {
		        super();
		    }

		    protected void processMouseEvent(MouseEvent e) {
		        if (isEnabled()) super.processMouseEvent(e);
		    }
		}
	
	static class StayOpenCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

		   //@Override
		   protected void doClick(MenuSelectionManager msm) {
		      menuItem.doClick(0);
		   }

		   public static ComponentUI createUI(JComponent c) {
		      return new StayOpenCheckBoxMenuItemUI();
		   }
	}


	
}


