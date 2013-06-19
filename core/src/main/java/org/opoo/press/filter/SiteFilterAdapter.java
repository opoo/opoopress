package org.opoo.press.filter;

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
	public void postRender(Site site) {

	}

	@Override
	public void postWrite(Site site) {
	}
}
