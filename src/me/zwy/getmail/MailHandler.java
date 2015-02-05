package me.zwy.getmail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MailHandler {
	
	private MimeMessage mail;
	
	public MailHandler(MimeMessage mail){
		this.mail = mail;
	}
	
	/**
	 * 获取邮件主题
	 * @return 邮件主题
	 */
	public String getSubject(){
		try {
			return mail.getSubject();
		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取发件人
	 * @return 发件人
	 */
	public String[] getFrom(){
		try {
			Address[] froms = mail.getFrom();
			String[] fs = new String[froms.length];
			for(int i=0;i<froms.length;i++){
				String from = froms[i].toString();
				fs[i] = MimeUtility.decodeText(from);
			}
			return fs;
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取收件人
	 * @return 收件人
	 */
	public String[] getRecipients(){
		try {
			Address[] recipients = mail.getAllRecipients();
			String[] rs = new String[recipients.length];
			for(int i=0;i<recipients.length;i++){
				String recipient = recipients[i].toString();
				rs[i] = MimeUtility.decodeText(recipient);
			}
			return rs;
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取邮件内容
	 * @return 可能有两种，一种是纯文本，一种是html格式
	 */
	public Map<ContentType, String> getContent(){
		Map<ContentType, String> contents = new HashMap<ContentType, String>();
		try {
			Multipart content = (Multipart) mail.getContent();
			int count = content.getCount();
			MimeMultipart c = null;
			for(int i=0;i<count;i++){
				BodyPart bodyPart = content.getBodyPart(i);
				if(bodyPart.isMimeType("multipart/alternative")){
					c = (MimeMultipart) bodyPart.getContent();
					break;
				}
			}
			if(c != null){
				for(int i=0;i<c.getCount();i++){
					BodyPart bodyPart = c.getBodyPart(i);
					if(bodyPart.isMimeType("text/plain")){
						contents.put(ContentType.TEXT, bodyPart.getContent().toString());
					}else if(bodyPart.isMimeType("text/html")){
						contents.put(ContentType.HTML, bodyPart.getContent().toString());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return contents;
	}
	
	/**
	 * 获取附件
	 * @return key为附件文件名，value为BodyPart
	 */
	public Map<String, BodyPart> getAttachment(){
		Map<String, BodyPart> attachments = new HashMap<String, BodyPart>();
		try {
			Multipart content = (Multipart) mail.getContent();
			int count = content.getCount();
			for(int i=0;i<count;i++){
				BodyPart bodyPart = content.getBodyPart(i);
				if("attachment".equals(bodyPart.getDisposition())){
					attachments.put(MimeUtility.decodeText(bodyPart.getFileName()), bodyPart);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return attachments;
	}
	
	/**
	 * 解码含有base64编码的字符串
	 * @param s 需要解码的字符串
	 * @return 解码后的字符串
	 */
	public String decodeBase64(String s){
		try {
			Pattern p = Pattern.compile("=\\?(\\w)+(\\?B\\?)(\\w|/)+\\?=");
			Matcher m = p.matcher(s);
			if(m.find()){
				String encodeStr = m.group();
				String[] keys = encodeStr.split("\\?");
				String decodeStr = m.replaceFirst(new String(Base64.getDecoder().decode(keys[3]), keys[1]));
				return decodeStr;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static enum ContentType{
		TEXT, HTML
	}

}
