package org.pathvisio.NWAplugin;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.plugins.GraphAnalysisPlugin;

public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext context) throws Exception {
		GraphAnalysisPlugin plugin = new GraphAnalysisPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);
		
		

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
	 
	}

}