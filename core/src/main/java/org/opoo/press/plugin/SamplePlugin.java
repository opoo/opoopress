package org.opoo.press.plugin;

import org.opoo.press.Plugin;
import org.opoo.press.ProcessorAdapter;
import org.opoo.press.Registry;
import org.opoo.press.Site;


/**
 * A sample plug in show how to develop a custom PlugIn for OpooPress.
 * 
 * @author Alex Lin
 *
 */
public class SamplePlugin implements Plugin {
	@Override
	public void initialize(Registry registry) {
		System.out.println("=========Initialize plugin 'SamplePlugin' for " 
				+ registry + "===========");
		registry.registerProcessor(new SampleProcessor());
	}

	public static class SampleProcessor extends ProcessorAdapter{
		@Override
		public void postWrite(Site site) {
			System.out.println("=========Site Write Complete!===========");
		}
	}
}
