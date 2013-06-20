package org.opoo.press;

/**
 * OpooPress Plugin.
 * 
 * @author Alex Lin
 */
public interface Plugin/* extends Ordered*/{
	
	/**
	 * 
	 * @param registry
	 */
	void initialize(Registry registry);
}
