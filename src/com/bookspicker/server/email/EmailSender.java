package com.bookspicker.server.email;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Class used to send emails.
 * TODO(ipince): refactor to use templates.
 * 
 * @author Rodrigo Ipince
 */
public class EmailSender {

    private static final Pattern EMAIL_REGEX = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}",
            Pattern.CASE_INSENSITIVE);

    private static final String SMTP_SERVER = "smtp.gmail.com";
    private static final String GMAIL_USERNAME = "bookspicker";
    // TODO(ipince): redacted for github. Should pass it in.
    private static final String GMAIL_PASS = "";

    private static final String FROM_EMAIL = "support@bookspicker.com";
    private static final String FROM_PERSONAL = "BooksPicker";

    public static void sendExpirationEmail(String sellerEmail, String sellerName,
            String book, int expirationDays, boolean autoPricing) throws AddressException, 
            UnsupportedEncodingException, MessagingException {
        List<String> to = new ArrayList<String>();
        to.add(sellerEmail);
        sendEmail(FROM_EMAIL, FROM_PERSONAL, to,
                new ArrayList<String>(), // no cc
                new ArrayList<String>(), // no bcc
                FROM_EMAIL,
                getExpirationSubject(getShortTitle(book)),
                getExpirationBody(sellerName, book, expirationDays, autoPricing));
    }

    /**
     * Sends email to seller letting him know that his book has been sold. The
     * buyer is CC'd.
     */
    public static void sendIntroductionEmail(String sellerEmail, String sellerName,
            String buyerEmail, String book, String price) throws AddressException, 
            UnsupportedEncodingException, MessagingException {

        List<String> to = new ArrayList<String>();
        to.add(sellerEmail);
        List<String> cc = new ArrayList<String>();
        cc.add(buyerEmail);
        sendEmail(FROM_EMAIL, FROM_PERSONAL, to, cc,
                new ArrayList<String>(), buyerEmail,
                getIntroductionSubject(getShortTitle(book)),
                getIntroductionBody(sellerName, book, price));
    }

    /**
     * Generic method to send an email
     * @throws MessagingException 
     * @throws UnsupportedEncodingException 
     */
    public static void sendEmail(String fromEmail, String fromName, List<String> to, List<String> cc, List<String> bcc,
            String replyTo, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.setProperty("mail.smtps.auth", "true");

        Session session = Session.getDefaultInstance(props, null);

        Transport transport = null;
        try {
            transport = session.getTransport("smtps");

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail, fromName));
            for (String email : to) {
                msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(email));
            }
            for (String email : cc) {
                msg.addRecipient(Message.RecipientType.CC,
                        new InternetAddress(email));
            }
            for (String email : bcc) {
                msg.addRecipient(Message.RecipientType.BCC,
                        new InternetAddress(email));
            }
            msg.setReplyTo(new Address[] { new InternetAddress(replyTo)});
            msg.setSubject(subject);
            msg.setText(body);

            transport.connect(SMTP_SERVER, GMAIL_USERNAME, GMAIL_PASS);
            transport.sendMessage(msg, msg.getAllRecipients());

            // We actually wanna throw these exceptions and handle
            // them outside!
        } catch (NoSuchProviderException e) {
        	throw e;
        } catch (AddressException e) {
        	throw e;
        } catch (MessagingException e) {
        	throw e;
        } catch (UnsupportedEncodingException e) {
        	throw e;
        } finally {
        	transport.close();
        }
    }
    public static boolean isEmail(String email) {
    	if (email == null)
    		return false;
        Matcher m = EMAIL_REGEX.matcher(email);
        return m.find();
    }

    private static String getShortTitle(String title) {
        if (title.length() > 35) {
            title = title.substring(0, 30) + "...";
        }
        return title;
    }

    // Email text generators

    private static String getExpirationSubject(String book) {
        StringBuilder sb = new StringBuilder();
        sb.append(EXPIRATION_SUBJECT_1);
        sb.append(book);
        sb.append(EXPIRATION_SUBJECT_2);
        return sb.toString();
    }

    private static String getExpirationBody(String sellerName, String book,
    		int expirationDays, boolean autoPricing) {
        StringBuilder sb = new StringBuilder();
        sb.append(EXPIRATION_BODY_1);
        sb.append(sellerName);
        sb.append(EXPIRATION_BODY_2);
        sb.append(book);
        sb.append(EXPIRATION_BODY_3);
        sb.append(expirationDays);
        sb.append(EXPIRATION_BODY_4);
        if (!autoPricing)
            sb.append(EXPIRATION_BODY_OPT);
        sb.append(EMAIL_SIGNATURE);
        return sb.toString();
    }

    private static String getIntroductionSubject(String book) {
        StringBuilder sb = new StringBuilder();
        sb.append(INTRODUCTION_SUBJECT_1);
        sb.append(book);
        sb.append(INTRODUCTION_SUBJECT_2);
        return sb.toString();
    }

    private static String getIntroductionBody(String sellerName, String book,
            String price) {
        StringBuilder sb = new StringBuilder();
        sb.append(INTRODUCTION_BODY_1);
        sb.append(sellerName);
        sb.append(INTRODUCTION_BODY_2);
        sb.append(book);
        sb.append(INTRODUCTION_BODY_3);
        sb.append(price);
        sb.append(INTRODUCTION_BODY_4);
        sb.append(EMAIL_SIGNATURE);
        return sb.toString();
    }

    private static final String EXPIRATION_SUBJECT_1 = "Your BooksPicker listing for '";
    private static final String EXPIRATION_SUBJECT_2 = "' has expired";
    private static final String EXPIRATION_BODY_1 = "Hi "; // name
    private static final String EXPIRATION_BODY_2 = ",\n\nYour listing for '";
    private static final String EXPIRATION_BODY_3 = "' has expired after ";
    private static final String EXPIRATION_BODY_4 = " days on the market. " +
    "If you want to repost your book, do so here:\n\nhttp://bookspicker.com/#sell\n\n" +
    "If you don't want to repost, do nothing!\n\n";
    private static final String EXPIRATION_BODY_OPT = "BTW, we noticed you chose to set your own " +
    "price for your book. For better results, try our automatic pricing option. Learn more " +
    "about it by visiting:\n\nhttp://bookspicker.com/#faq\n\n";

    private static final String INTRODUCTION_SUBJECT_1 = "You have just sold '";
    private static final String INTRODUCTION_SUBJECT_2 = "' on BooksPicker!";
    private static final String INTRODUCTION_BODY_1 = "Hi "; // name
    private static final String INTRODUCTION_BODY_2 = ",\n\nCongratulations, we have found a buyer for your " +
    "listing of '";
    private static final String INTRODUCTION_BODY_3 = "' on BooksPicker!\n\nThe buyer is CC'd on this " +
    "email and the agreed purchase price is ";
    private static final String INTRODUCTION_BODY_4 = ".\n\nPlease follow up with the buyer to complete " +
    "the transaction. If the transaction doesn't complete, you can repost your book at:\n\n" +
    "http://bookspicker.com/#sell\n\n";

    private static final String EMAIL_SIGNATURE = "Thank you for using BooksPicker!\n-The BP Team";

}
