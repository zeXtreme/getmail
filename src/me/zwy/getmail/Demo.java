package me.zwy.getmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.sun.mail.util.BASE64DecoderStream;

public class Demo {

	public static void main(String[] args) throws Exception{
		File file = new File("C:/Users/Zeng/Desktop/eml文件附件提取/(不好意�?�，请以此份为准）田震燃+综治监察.eml");
		String path = file.getAbsolutePath();
		File dic = new File(path.substring(0, path.length()-4));
		if(!dic.exists()){
			dic.mkdirs();
		}
		InputStream is = new FileInputStream(file);
		MimeMessage mail = new MimeMessage(Session.getDefaultInstance(new Properties()), is);
		System.out.println(mail.getSubject());
		Address[] as = mail.getFrom();
		for(Address ad : as){
			System.out.println(ad);
		}
		System.out.println(mail.getSender());
		Address[] ass = mail.getAllRecipients();
		for(Address ad : ass){
			System.out.println(ad);
		}
		Multipart part = (Multipart) mail.getContent();
		int count = part.getCount();
		for(int i=0;i<count;i++){
			BodyPart bp = part.getBodyPart(i);
			System.out.println(bp.getContent());
			if(bp.getContent() instanceof MimeMultipart){
				Multipart bps = (Multipart) bp.getContent();
				int counts = bps.getCount();
				for(int j=0;j<counts;j++){
					BodyPart bpss = bps.getBodyPart(j);
					System.out.println(bpss.getContent());
				}
			}else if(bp.getContent() instanceof BASE64DecoderStream){
				String fileName = bp.getFileName();
				String[] fs = fileName.split("\\?");
				String fn = new String(Base64.getDecoder().decode(fs[3]), fs[1]);
				BASE64DecoderStream in = (BASE64DecoderStream) bp.getContent();
				FileOutputStream out = new FileOutputStream(dic.getAbsolutePath() + "/" + fn);
				int n;
				while((n = in.read()) != -1){
					out.write(n);
				}
				in.close();
				out.flush();
				out.close();
			}
		}
	}
	
}
