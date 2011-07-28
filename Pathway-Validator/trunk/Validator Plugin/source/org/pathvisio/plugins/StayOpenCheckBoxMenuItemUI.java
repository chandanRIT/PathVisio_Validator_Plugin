package org.pathvisio.plugins;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

public class StayOpenCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

	   //@Override
	   protected void doClick(MenuSelectionManager msm) {
	      menuItem.doClick(0);
	   }

	   public static ComponentUI createUI(JComponent c) {
	      return new StayOpenCheckBoxMenuItemUI();
	   }
}

 class CustomMenuItem extends JMenuItem {

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