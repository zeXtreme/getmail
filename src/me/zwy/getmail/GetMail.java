package me.zwy.getmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.BASE64DecoderStream;

import me.zwy.getmail.MailHandler.ContentType;

public class GetMail {
	
	private static File workDir;
	
	private static File descDir;
	
	public static void main(String[] args) {
		if(args.length == 2 && "from".equals(args[0])){
			workDir = new File(args[1]);
			descDir = workDir;
		}else if(args.length == 4 && "from".equals(args[0]) && "to".equals(args[2])){
			workDir = new File(args[1]);
			descDir = new File(args[3]);
		}else{
			System.out.println("参数错误");
			return;
		}
		if(!workDir.isDirectory() || !descDir.isDirectory()){
			System.out.println("指定的参数不是目录");
		}
		start();
	}
	
	private static void start(){
		for(File file : workDir.listFiles()){
			if(file.getName().endsWith(".eml")){
				work(file);
			}
		}
		System.out.println("工作完成！！！");
	}

	private static void work(File file) {
		System.out.println("开始解析" + file.getAbsolutePath());
		InputStream is;
		try {
			String path = file.getAbsolutePath();
			File dic = new File(path.substring(0, path.length()-4));
			if(!dic.exists()){
				dic.mkdirs();
			}
			is = new FileInputStream(file);
			MimeMessage mail = new MimeMessage(Session.getDefaultInstance(new Properties()), is);
			MailHandler handler = new MailHandler(mail);
			saveContent(handler, dic.getAbsolutePath());
			saveAttachment(handler, dic.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	private static void saveContent(MailHandler handler, String path){
		System.out.println("开始保存内容");
		PrintWriter pw = null;
		try {
			File saveFile = new File(path + "/" + handler.getSubject() + ".txt");
			System.out.println("保存到" + saveFile.getAbsolutePath());
			pw = new PrintWriter(saveFile);
			pw.println("发送自：");
			for(String from : handler.getFrom()){
				pw.println(from);
			}
			pw.println();
			pw.println("接收者：");
			for(String recipient : handler.getRecipients()){
				pw.println(recipient);
			}
			pw.println();
			pw.println("主题：");
			pw.println(handler.getSubject());
			pw.println();
			pw.println("正文：");
			pw.println(handler.getContent().get(ContentType.TEXT));
			pw.flush();
			System.out.println("邮件内容保存完毕");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(pw != null){
				pw.close();
			}
		}
	}
	
	private static void saveAttachment(MailHandler handler, String path){
		System.out.println("开始保存附件");
		try {
			Map<String, BodyPart> attachments = handler.getAttachment();
			Set<Entry<String, BodyPart>> entrySet = attachments.entrySet();
			for(Entry<String, BodyPart> entry : entrySet){
				File saveFile = new File(path + "/" + entry.getKey());
				System.out.println("保存到" + saveFile.getAbsolutePath());
				FileOutputStream out = new FileOutputStream(saveFile);
				BASE64DecoderStream in = (BASE64DecoderStream) entry.getValue().getContent();
				int n;
				while((n = in.read()) != -1){
					out.write(n);
				}
				in.close();
				out.flush();
				out.close();
			}
			System.out.println("附件保存完毕");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
}
