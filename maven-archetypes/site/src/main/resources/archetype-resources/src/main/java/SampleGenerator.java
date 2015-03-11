#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.List;

import org.opoo.press.Generator;
import org.opoo.press.Post;
import org.opoo.press.Site;

public class SampleGenerator implements Generator {

	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered${symbol_pound}getOrder()
	 */
	@Override
	public int getOrder() {
		return 1000;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Generator${symbol_pound}generate(org.opoo.press.Site)
	 */
	@Override
	public void generate(Site site) {
		List<Post> posts = site.getPosts();
		for(Post post: posts){
			post.set("generated_by", SampleGenerator.class.getName());
		}
	}

}
