package com.autodb.ops.dms.domain.email;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * email service
 *
 * @author dongjs
 * @since 16/1/20
 */
@Component
public class EmailService {
    private static Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${server.host}")
    private String serverHost;

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    @Qualifier("emailSenderExecutorService")
    private ExecutorService mailExecutorService;

    public void sendMain(EmailDo emailDo) {
        try {
            Template template = velocityEngine.getTemplate(emailDo.getTemplate());

            StringWriter htmlMail = new StringWriter();
            VelocityContext context = new VelocityContext();

            // context
            context.put("host", serverHost);
            emailDo.getContext().entrySet().forEach(entry -> context.put(entry.getKey(), entry.getValue()));
            template.merge(context, htmlMail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(mailSender.getUsername());

            // to and cc
            List<String> to = emailDo.getTo();
            helper.setTo(to.toArray(new String[to.size()]));
            List<String> cc = emailDo.getCc();
            if (cc != null && cc.size() > 0) {
                helper.setCc(cc.toArray(new String[cc.size()]));
            }
            helper.setSubject(emailDo.getSubject());
            helper.setText(htmlMail.toString(), true);

            // send
            mailExecutorService.submit(() -> {
                try {
                    mailSender.send(message);
                } catch (MailException e) {
                    log.error("send email {}, exception: {}", emailDo.getSubject(), e.getMessage());
                }
            });
        } catch (MailException | MessagingException e) {
            log.error("send email {}, exception: {}", emailDo.getSubject(), e.getMessage());
        }
    }

    protected void prepare(EmailDo emailDo) {
        emailDo.getCc().removeAll(emailDo.getTo());
    }
}