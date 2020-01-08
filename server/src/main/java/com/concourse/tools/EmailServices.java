package com.concourse.tools;

import com.concourse.models.CourseInviteToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

@Slf4j
@Service
@PropertySource("classpath:keys.properties")
public class EmailServices {

    @Value("${client.baseurl}")
    private String BASE_URL;

    @Value("${gmail.username}")
    private String GMAIL_USERNAME;

    @Value("${gmail.password}")
    private String GMAIL_PASSWORD;

    public boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public Session getSession() {
        if (GMAIL_USERNAME.equals("") || GMAIL_PASSWORD.equals("")){
            log.error("GMAIL IS NOT CONFIGURED. PLEASE FILL THE FIELDS IN src/main/resources/keys.properties.");
            System.exit(1);
        }

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        return  Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
                    }
                });
    }

    public boolean sendCode(String code, InternetAddress recipientAddress) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(GMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAddress.getAddress()));
            message.setSubject("Concourse Login Passcode: " + code);
            message.setContent(String.format("<p>Here's your passcode!</p> <br> <h1>%s</h1>", code), "text/html;charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendConfirmationToken(String code, String recipientAddressString) {
        try {
            InternetAddress recipientAddress = new InternetAddress(recipientAddressString);

            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(GMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAddress.getAddress()));
            message.setSubject("Concourse Registration Verification");
            message.setContent(String.format("<p>Click the following link to finish registration!</p> <br> " +
                            "<h1><a href=\"%s\">Finish Registration</h1>", BASE_URL + "/register/confirm/" + code),
                    "text/html;charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, CourseInviteToken> sendCourseInviteToken(Map<String, CourseInviteToken> emailTokenMap, String courseName){
        Map<String, CourseInviteToken> failedEmails = new HashMap<>();

        String loginLink = BASE_URL + "/login";
        String registerLink = BASE_URL + "/register";
        String joinCourseLink = BASE_URL + "/course/join";

        for (Map.Entry<String, CourseInviteToken> entry : emailTokenMap.entrySet()){

            String content = String.format(
                    "<p>Hi! You're invited to join course %s as %s. <a href=\"%s\">Register</a> (as <b>%s</b>) or <a href=\"%s\">Login</a> to Concourse, then go to <a href=\"%s\">Join" +
                            " Course</a> Page</p>" +
                            "  <p>Enter the following code: </p>" +
                            "  <ul>" +
                            "    <li>" +
                            "      Course ID: %s" +
                            "    </li>" +
                            "    <li>" +
                            "      Invite ID: %s" +
                            "    </li>" +
                            "  </ul>"
                    ,
                    courseName, entry.getValue().getRole().toLowerCase(), registerLink,
                    entry.getValue().getRole().toLowerCase(), loginLink,
                    joinCourseLink, entry.getValue().getCourseId(), entry.getValue().getInviteId());
            try {
                InternetAddress recipientAddress = new InternetAddress(entry.getKey());

                Message message = new MimeMessage(getSession());
                message.setFrom(new InternetAddress(GMAIL_USERNAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAddress.getAddress()));
                message.setSubject("Concourse - Invitation to Join " + courseName);
                message.setContent(content, "text/html;charset=utf-8");
                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
                failedEmails.put(entry.getKey(), entry.getValue());
            }
        }
        return failedEmails;
    }
}
