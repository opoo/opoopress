/*
 * Copyright 2013 Alex Lin.
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
package org.opoo.press.mail;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.MailAddress;
import org.apache.mailet.base.GenericRecipientMatcher;

/**
 * @author Alex Lin
 */
public class OpooPressMatcher extends GenericRecipientMatcher {
	private static final Log log = LogFactory.getLog(OpooPressMatcher.class);
	private String config;

	/* (non-Javadoc)
	 * @see org.apache.mailet.base.GenericMatcher#init()
	 */
	@Override
	public void init() throws MessagingException {
		config = getCondition();
		log.info("The config: " + config);
		if(StringUtils.isBlank(config)){
			throw new IllegalArgumentException("Config required.");
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mailet.base.GenericRecipientMatcher#matchRecipient(org.apache.mailet.MailAddress)
	 */
	@Override
	public boolean matchRecipient(MailAddress recipient) throws MessagingException {
		String email = recipient.toString();
		if(config.contains("|" + email + "|")){
			log.info("Mail address is acceptable: " + email);
			return true;
		}
		return false;
	}
}
