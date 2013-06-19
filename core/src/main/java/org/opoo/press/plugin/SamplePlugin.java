package org.opoo.press.plugin;

import org.opoo.press.Plugin;
import org.opoo.press.Registry;
import org.opoo.press.Site;
import org.opoo.press.filter.SiteFilterAdapter;


/**
 * A sample plug in show how to develop a custom PlugIn for OpooPress.
 * 
 * @author Alex Lin
 *
 */
public class SamplePlugin implements Plugin {
	@Override
	public void init(Registry registry) {
		System.out.println("Initialize plugin 'SamplePlugin' for " + registry.getSite());
		registry.registerSiteFilter(new SampleSiteFilter());
	}

	public static class SampleSiteFilter extends SiteFilterAdapter{
		@Override
		public void postWrite(Site site) {
			System.out.println("=========Site Write Complete!===========");
		}
	}
}
