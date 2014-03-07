package org.opoo.press.filter;

import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.SiteFilter;

public class SiteFilterAdapter implements SiteFilter {
	public static final int DEFAULT_ORDER = 100;
	
	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public void postSetup(Site site) {

	}

	@Override
	public void postRead(Site site) {
	}

	@Override
	public void postGenerate(Site site) {

	}

	@Override
	@Deprecated
	public void postRender(Site site) {

	}

	@Override
	public void postWrite(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postConvertPost(org.opoo.press.Site, org.opoo.press.Post)
	 */
	@Override
	public void postConvertPost(Site site, Post post) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postConvertPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postConvertPage(Site site, Page page) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderPost(org.opoo.press.Site, org.opoo.press.Post)
	 */
	@Override
	public void postRenderPost(Site site, Post post) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postRenderPage(Site site, Page page) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderAllPosts(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPosts(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderAllPages(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPages(Site site) {
	}
}
