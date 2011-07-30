package org.pathvisio.plugins;

import java.awt.FontMetrics;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

public class VPUtility {

	static void resetPhaseBox(JComboBox phaseBox){
		if(!phaseBox.isEnabled())
			phaseBox.setEnabled(true);
		
		//refreshing the drop down to include phases of the selected schema by clearing out the previous items and adding new ones
		while(phaseBox.getItemCount()!=1){
			phaseBox.removeItemAt(phaseBox.getItemCount()-1);
		}
		
	}
	
	static String cutTitleString(String ss,JTextField schemaTitleTag)
	{	
		
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
					return ss;
				}
		
			}
		} else schemaTitleTag.setToolTipText(null);
		return ss;
	}
	
	/**
	 * An alternate method to enable/disable the "Reconsider (Un-Ignore)" button in the sub menu item,
	 * but this might be slower than the "checkUncheck" method
	 * @param jcbmi the JCheckBoxMenuItem that received the check/uncheck event 
	 */
	private void okButtonED(JCheckBoxMenuItem jcbmi){ //ED: Enable / Disable  
		
		JMenu subMenu=(JMenu)((JPopupMenu)jcbmi.getParent()).getInvoker();//since can not acces the parent directly here
		int lengthOfIgnored=subMenu.getMenuComponentCount();
		int index=lengthOfIgnored-1;
		int NOFchecked=0;
		//System.out.println("total in submenu4 "+subMenu4.getMenuComponentCount());
		while(index>1){
			if( ( (JCheckBoxMenuItem)subMenu.getMenuComponent(index) ).getState() ){
				NOFchecked++;
				break;
			}
			index--;
		}
		if(NOFchecked!=0) 
			subMenu.getMenuComponent(0).setEnabled(true);
		else subMenu.getMenuComponent(0).setEnabled(false);
	}
	
	
	
}
