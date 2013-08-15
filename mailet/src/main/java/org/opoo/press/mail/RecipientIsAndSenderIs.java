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

import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.base.GenericMatcher;

/**
 * RecipientIs and SenderIs.
 * 
 * <pre>
 * &lt;mailet match="RecipientIsAndSenderIs=user1@example.com,user2@example.com|myaddress@gmail.com" ..&gt;
 * &lt;/mailet&gt;
 * </pre>
 * @author Alex Lin
 */
public class RecipientIsAndSenderIs extends GenericMatcher {
	private static final Log log = LogFactory.getLog(RecipientIsAndSenderIs.class);
	private Collection<MailAddress> recipients;
	private Collection<MailAddress> senders;

	/* (non-Javadoc)
	 * @see org.apache.mailet.base.GenericMatcher#init()
	 */
	@Override
	public void init() throws MessagingException {
		String condition = getCondition();
		//log.info("The condition: " + condition);
		if(StringUtils.isBlank(condition)){
			throw new IllegalArgumentException("Config required.");
		}
		
		String[] arr = StringUtils.split(condition, '|');
		if(arr.length != 2){
			System.out.println(arr.length);
			throw new IllegalArgumentException("Recipient addresses and sender addresses must split by '|'.");
		}
		
		log.info("Acceptable Recipients: " + arr[0]);
		log.info("Acceptable Senders: " + arr[1]);
		
		StringTokenizer st0 = new StringTokenizer(arr[0], ", \t", false);
        recipients = new HashSet<MailAddress>();
        while (st0.hasMoreTokens()) {
            recipients.add(new MailAddress(st0.nextToken()));
        }
		
        StringTokenizer st1 = new StringTokenizer(arr[1], ", \t", false);
        senders = new HashSet<MailAddress>();
        while (st1.hasMoreTokens()) {
            senders.add(new MailAddress(st1.nextToken()));
        }
	}

	@Override
    public Collection<MailAddress> match(Mail mail) throws MessagingException{
		if(!senders.contains(mail.getSender())){
			if(log.isDebugEnabled()){
				log.debug("Sender is not acceptable: " + mail.getSender());
			}
			return null;
		}
		log.info("Sender is acceptable: " + mail.getSender());
		
        Collection<MailAddress> matching = new Vector<MailAddress>();
        for (MailAddress rec : mail.getRecipients()) {
            if (matchRecipient(rec)) {
            	log.info("Recipient is acceptable: " + rec);
                matching.add(rec);
            }
        }
        return matching;
    }
    
	public boolean matchRecipient(MailAddress recipient) throws MessagingException {
		return recipients.contains(recipient);
	}
}
