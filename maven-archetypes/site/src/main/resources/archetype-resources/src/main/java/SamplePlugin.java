#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.List;

import org.opoo.press.Config;
import org.opoo.press.ConfigAware;
import org.opoo.press.Plugin;
import org.opoo.press.Post;
import org.opoo.press.Registry;
import org.opoo.press.Site;
import org.opoo.press.processor.ProcessorAdapter;

public class SamplePlugin implements Plugin{

	@Override
	public void initialize(Registry reg) {
		System.out.println("-- initializing " + this + " --");
		reg.registerGenerator(new SampleGenerator());
		reg.registerProcessor(new MySampleProccesor());
	}
	
	static class MySampleProccesor extends ProcessorAdapter implements ConfigAware{
		@Override
		public int getOrder() {
			return super.getOrder() + 100;
		}

		@Override
		public void postWrite(Site site) {
			super.postWrite(site);
			
			List<Post> posts = site.getPosts();
			System.out.println("-- There are " + posts.size() + " posts in this site. --");
		}

		@Override
		public void setConfig(Config config) {
			System.out.println("-- Set config... --");
		}
	}
}
