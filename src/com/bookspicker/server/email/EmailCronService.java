package com.bookspicker.server.email;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.bookspicker.Log4JInitServlet;
import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.User;

public class EmailCronService {
	
	@Parameter(names = "--sendEmails")
	public boolean sendEmails = false;
	
	@Parameter(names = "--updateDb")
	public boolean updateDb = false;
	
	@Parameter(names = "--expirationDays")
	public int expirationDays = 7;
	
	@Parameter(names = "--help")
	public boolean help = false;

    private static Logger logger = Log4JInitServlet.logger;

    /**
     * @param args
     */
    public static void main(String[] args) {
    	EmailCronService emailer = new EmailCronService();
    	JCommander jcomm = new JCommander(emailer, args);
    	
    	if (emailer.help) {
    		jcomm.usage();
    		System.exit(1);
    	}
    	
    	emailer.run();
    }
    
    private void run() {
        logger.info("EmailCronService - Starting scheduled cron task for updating active local sales");
        logger.info("Sending emails: " + sendEmails);
        logger.info("Updating database: " + updateDb);
        logger.info("Expiration days used: " + expirationDays);
        
        int offerCounter = 0;
        int offerInvalidated = 0;
        List<LocalOffer> activeOffers = LocalOfferManager.getManager().getActiveLocalOffers();
        for (LocalOffer offer : activeOffers) {
            offerCounter++;
            if (offer.shouldExpire(expirationDays)) {
                Long offerId = offer.getId();
                offer.deactivate();
                if (updateDb) {
                	LocalOfferManager.getManager().update(offer);
                }
                User user = offer.getOwner();
                offerInvalidated++;
                logger.info("EmailCronService - deactivated offer: " + offerId);

                try {
                	if (sendEmails) {
                		EmailSender.sendExpirationEmail(user.getFbEmail(), user.getName(),
                				offer.getBook().getTitle(), expirationDays, offer.isAutoPricing());
                	}
                } catch (AddressException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MessagingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        logger.info("EmailCronService - Finished deactive local sales");
        logger.info("EmailCronService - Out of the initially active offers, remain active: " + offerCounter + "; were deactivated: " + offerInvalidated);
    }
}
