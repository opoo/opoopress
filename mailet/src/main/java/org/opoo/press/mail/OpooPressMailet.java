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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.base.GenericMailet;
import org.opoo.press.Application;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.opoo.press.SlugHelper;
import org.opoo.press.impl.ContextImpl;
import org.opoo.press.impl.PostImpl;
import org.opoo.press.impl.SiteManagerImpl;
import org.opoo.press.slug.ChineseToPinyinSlugHelper;
import org.opoo.press.source.NoFrontMatterException;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.source.SourceParser;
import org.opoo.press.util.LinkUtils;

/**
 * Mailet for publish post by mail.
 * 
 * @author Alex Lin
 */
public class OpooPressMailet extends GenericMailet {
	private static final Logger log = LoggerFactory.getLogger(OpooPressMailet.class);
	private static final SimpleDateFormat NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final String[] EXTS = {".html", ".markdown", ".md", ".textile"};
	
	private ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
	private SlugHelper slugHelper = new ChineseToPinyinSlugHelper();
	private SiteManager siteManager;
	private SourceParser sourceParser;
	
	private String execCommand;// = "mvn deploy";
	private File site;
	
	/* (non-Javadoc)
	 * @see org.apache.mailet.base.GenericMailet#init()
	 */
	@Override
	public void init() throws MessagingException {
		super.init();
		execCommand = getInitParameter("command", execCommand);
		String siteDir = getInitParameter("site");
		
		log.info("Site directory: " + siteDir);
		log.info("Command: " + execCommand);
		
		if(StringUtils.isBlank(siteDir)){
			throw new IllegalArgumentException("Site is required.");
		}
		
		site = new File(siteDir);
		if(!site.exists() || !site.isDirectory()){
			throw new IllegalArgumentException("Site is not a valid directory.");
		}
		
		new ContextImpl().initialize();
		siteManager = Application.getContext().getSiteManager();
		sourceParser = Application.getContext().getSourceParser();
	}
	
	private void info(PrintWriter out, String message){
		log.info(message);
		out.println(message);
	}
	
	private void warn(PrintWriter out, String message){
		log.warn(message);
		out.println("[WARN] " + message);
	}
	
	private void error(PrintWriter out, String message, Throwable e){
		log.error(message, e);
		out.println(message);
		out.println("------");
		e.printStackTrace(out);
		out.println();
		out.println();
		out.flush();
	}

	@Override
	public void service(Mail mail) throws MessagingException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		String subject = "";
		File file = null;
		try{
			MimeMessage mm = mail.getMessage();
			subject = MimeUtility.decodeText(mm.getSubject());
			Date date = mm.getSentDate();
			String content = getTextContent(mm);
			if(content == null){
				info(out, "Can not extract plain text content in mail, skip generate and deploy OpooPress site: " + site);
				replyFailed(mail, subject, writer.toString(), null);
				return;
			}
			
			log.info("===============================");
			log.info("Subject: " + subject);
			log.info("Sent date: " + date);
			log.info("Size: " + mm.getSize());
			if(log.isDebugEnabled()){
				log.debug(content);
			}
			
			file = writeToPostFile(out, subject, content, date);
			executeCommand(out);
			
			out.flush();
			replyPublished(mail, subject, writer.toString(), file);
		} catch (Exception e) {
			error(out, e.getMessage(), e);
			replyFailed(mail, subject, writer.toString(), file);
			return;
		}
	}
	
	private void replyFailed(Mail mail, String subject, String content, File file) throws MessagingException{
		content = "Failed to publish post '" + subject + "'.\n\n========================\n" + content;
		subject = "[FAILED] " + subject;
		reply(mail, subject, content, file);
	}
	
	private void replyPublished(Mail mail, String subject, String content, File file) throws MessagingException{
		content = "Post '" + subject + "' has been published.\n\n========================\n" + content;
		subject = "[PUBLISHED] " + subject;
		reply(mail, subject, content, file);
	}
	
	private void reply(Mail mail, String subject, String content, File file) throws MessagingException{
		MailAddress recipient = mail.getRecipients().iterator().next();
		MailAddress sender = mail.getSender();
		
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(recipient.toInternetAddress());
		message.setRecipient(Message.RecipientType.TO, sender.toInternetAddress());
		message.setSubject(subject);
		message.setSentDate(new Date());
		//message.setText(content);
		setReplyMailContent(message, content, file);
		
		log.info("Reply mail: " + subject + " -> " + sender);
		getMailetContext().sendMail(message);
	}
	
	private void setReplyMailContent(MimeMessage message, String content, File file) throws MessagingException {
		if(file == null){
			message.setText(content);
		}else{
			MimeMultipart mp = new MimeMultipart();
			
			MimeBodyPart bodyPart1 = new MimeBodyPart();
			bodyPart1.setText(content);
			mp.addBodyPart(bodyPart1);
			
			BodyPart bodyPart2 = new MimeBodyPart();
			bodyPart2.setDataHandler(new DataHandler(new FileDataSource(file)));
			bodyPart2.setFileName(file.getName());
			mp.addBodyPart(bodyPart2);
			
			message.setContent(mp);
			//message.saveChanges();
		}
	}

	private File writeToPostFile(PrintWriter out, String subject, String content, Date sentDate) throws Exception {
		if(sentDate == null){
			sentDate = new Date();
		}

		String name = subject;
		String ext = null;
		String title = subject;
		if(subject.indexOf('|') > 0){
			String[] arr = StringUtils.split(subject, '|');
			if(arr.length == 2){
				title = arr[0];
				name = arr[1];
			}
		}
		
		name = slugHelper.toSlug(name);
		
		if(PostImpl.FILENAME_PATTERN.matcher(name).matches()){
			String dateString = name.substring(0, 10);
			try {
				sentDate = NAME_FORMAT.parse(dateString);
			} catch (ParseException e) {
				//throw new RuntimeException("Subject format not valid: " + subject);
				error(out, "Subject format not valid.", e);
			}
			name = name.substring(11);
		}
		
		int indexOfAny = StringUtils.indexOfAny(name, EXTS);
		if(indexOfAny > -1){
			ext = name.substring(indexOfAny);
			name = name.substring(0, indexOfAny);
		}
		
		Site siteObject = siteManager.createSite(site);
		String filename = processPostFileName(siteObject, sentDate, name, ext);
		File postFile = new File(siteObject.getSource(), filename);
		
		File tempFile = prepareTempFile(content, sentDate, title);
		FileUtils.copyFile(tempFile, postFile);
		FileUtils.deleteQuietly(tempFile);
		
		info(out, "File writen: " + postFile);

		return postFile;
	}

	private String getTextContent(Part part) throws MessagingException, IOException{
		if(part.isMimeType("text/plain")){
			return (String) part.getContent();
		}else if(part.isMimeType("multipart/*")){ 
			Multipart multipart = (Multipart) part.getContent();  
            int count = multipart.getCount();  
            for(int i = 0 ; i < count ; i++){  
            	String textContent = getTextContent(multipart.getBodyPart(i));  
            	if(textContent != null){
            		return textContent;
            	}
            }  
		}
		return null;
	}

	private static String processPostFileName(Site site, Date date, String name, String ext){
		String newPostFileStyle = site.getConfig().get(SiteManagerImpl.NEW_POST_FILE_KEY, SiteManagerImpl.DEFAULT_NEW_POST_FILE);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", name);
		map.put("format", ext != null ? ext.substring(1) : "markdown");
		LinkUtils.addDateParams(map, date);
		String filename = site.getRenderer().renderContent(newPostFileStyle, map);
	
		if(ext != null && !filename.endsWith(ext)){
			log.info("File name extension change: " + filename + " -> " + ext);
			filename = FilenameUtils.removeExtension(filename) + ext;
		}
		
		return filename;
	}
	
	private File prepareTempFile(String content, Date date, String title) throws Exception{
		File file = File.createTempFile("opoopress.mail.", ".post");
		FileUtils.write(file, content, "UTF-8");
		
		SourceEntry sourceEntry = new SourceEntry(file);
		List<String> metaLines = new ArrayList<String>();
		boolean hasFrontMatter = true;
		try {
			Source source = sourceParser.parse(sourceEntry);
			Map<String, Object> meta = source.getMeta();
			if(!meta.containsKey("layout")){
				metaLines.add("layout: post");
			}
			if(!meta.containsKey("date")){
				metaLines.add("date: '" + DATE_FORMAT.format(date) + "'");
			}
			if(!meta.containsKey("title")){
				metaLines.add("title: \"" + title + "\"");
			}
		} catch (NoFrontMatterException e) {
			hasFrontMatter = false;
			metaLines.add(Source.TRIPLE_DASHED_LINE);
			metaLines.add("layout: post");
			metaLines.add("date: '" + DATE_FORMAT.format(date) + "'");
			metaLines.add("title: '" + title + "'");
			metaLines.add(Source.TRIPLE_DASHED_LINE);
		}
		
		if(!metaLines.isEmpty()){
			addMetaLines(file, metaLines, hasFrontMatter);
		}
		return file;
	}
	
	private void addMetaLines(File file, List<String> metaLines, boolean hasFrontMatter) throws IOException {
		log.info("Adding front matter lines...");
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		lines = new ArrayList<String>(lines);
		
		if(hasFrontMatter){
			lines.addAll(1, metaLines);
		}else{
			lines.addAll(0, metaLines);
		}
		
		FileUtils.writeLines(file, "UTF-8", lines);
	}

	private void executeCommand(final PrintWriter out) throws ExecuteException, IOException{
		if(StringUtils.isBlank(execCommand)){
			warn(out, "No command need execute.");
			return;
		}
		
		Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(site.getParentFile());

        CommandLine command = CommandLine.parse(execCommand);
        
        out.println("========================");
        info(out, String.format("Execute command: %s",  command.toString()));
        out.println("========================");
        LogOutputStream outputStream = new LogOutputStream() {
            protected void processLine(String line, int level){
                log.info(String.format("Command logged an out: %s", line));
                out.println(line);
            }
        };
        LogOutputStream errorStream = new LogOutputStream() {
            protected void processLine(String line, int level){
                log.error(String.format("Command logged an error: %s", line));
                out.println(line);
            }
        };
        
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        executor.setStreamHandler(streamHandler);
        executor.setProcessDestroyer(processDestroyer);
        
//        (new Thread(new Runnable(){
//            public void run(){
//                try{
//                    executor.execute(command);
//                }
//                catch(Exception e) {
//                    log.warn(String.format("Command exited with error %s", e.getMessage()));
//                }
//            }
//        })).start();
	
		executor.execute(command);
	}
}
