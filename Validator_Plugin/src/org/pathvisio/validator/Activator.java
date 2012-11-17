package org.pathvisio.validator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator
{
	private Plugin plugin;
	
	@Override
	public void start(BundleContext context) throws Exception
	{
		plugin = new ValidatorPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception
	{
		plugin.done();
	}

}
