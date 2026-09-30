package dev.xuya.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

/**
 * 邮件发送服务：封装 spring-boot-starter-mail，支持文本/HTML/附件。
 * 配置在 spring.mail.*。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendText(String from, String[] to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        mailSender.send(msg);
        log.info("文本邮件已发送: to={} subject={}", String.join(",", to), subject);
    }

    public void sendHtml(String from, String[] to, String subject, String html) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(msg);
        log.info("HTML邮件已发送: to={} subject={}", String.join(",", to), subject);
    }

    public void sendHtmlWithAttachments(String from, String[] to, String subject,
                                        String html, List<String> filePaths) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        for (String path : filePaths) {
            var file = new java.io.File(path);
            if (file.exists()) {
                helper.addAttachment(file.getName(), file);
            }
        }
        mailSender.send(msg);
        log.info("带附件邮件已发送: to={} subject={} attachments={}", String.join(",", to), subject, filePaths.size());
    }
}
