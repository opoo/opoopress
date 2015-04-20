/*
 * Copyright 2014 Alex Lin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opoo.press;

import java.util.List;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class ProcessorsProcessor implements Processor{
	private final List<Processor> processors;
	
	public ProcessorsProcessor(List<Processor> processors){
		this.processors = processors;
//		if(this.processors != null){
//			Collections.sort(processors, Ordered.COMPARATOR);
//		}
	}
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postSetup(org.opoo.press.Site)
	 */
	@Override
	public void postSetup(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postSetup(site);
			}
		}
	}

	@Override
	public void postRead(Site site, Page page) {
		if(processors != null){
			for(Processor p: processors){
				p.postRead(site, page);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRead(org.opoo.press.Site)
	 */
	@Override
	public void postRead(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postRead(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postGenerate(org.opoo.press.Site)
	 */
	@Override
	public void postGenerate(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postGenerate(site);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postConvertPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postConvertPage(Site site, Page page) {
		if(processors != null){
			for(Processor p: processors){
				p.postConvertPage(site, page);
			}
		}
	}

	@Override
	public void postConvertAllPages(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postConvertAllPages(site);
			}
		}
	}

	@Override
	public void preRenderAllPages(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.preRenderAllPages(site);
			}
		}
	}

	@Override
	public void preRenderPage(Site site, Page page) {
		if(processors != null){
			for(Processor p: processors){
				p.preRenderPage(site, page);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postRenderPage(Site site, Page page) {
		if(processors != null){
			for(Processor p: processors){
				p.postRenderPage(site, page);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderAllPages(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPages(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postRenderAllPages(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postWrite(org.opoo.press.Site)
	 */
	@Override
	public void postWrite(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postWrite(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postCleanup(org.opoo.press.Site)
	 */
	@Override
	public void postCleanup(Site site) {
		if(processors != null){
			for(Processor p: processors){
				p.postCleanup(site);
			}
		}
	}

	@Override
	public void beforeBuildTheme(Theme theme) {
		if(processors != null){
			for(Processor p: processors){
				p.afterBuildTheme(theme);
			}
		}
	}

	@Override
	public void afterBuildTheme(Theme theme) {
		if(processors != null){
			for(Processor p: processors){
				p.afterBuildTheme(theme);
			}
		}
	}
}
