package CA.CAPS.service;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {
	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String from;

	@Async
	public void sendMail(String toemail, String subject, String text) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setSubject(subject);
		mailMessage.setText(text);
		mailMessage.setTo(toemail);
		mailMessage.setFrom(from);
		mailSender.send(mailMessage);
	}

	@Async
	public void sendValidationCode(String email, HttpSession session) {
		String code = ValidationCode();
		session.setAttribute("email", email);
		session.setAttribute("code", code);
		String subject = "validation code for CAPS registration";
		String text = "Your registration verification code is: " + code;
		sendMail(email, subject, text);
	}

	public String ValidationCode() {
		StringBuilder str = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 6; i++) {
			str.append(random.nextInt(10));
		}
		return str.toString();
	}
}