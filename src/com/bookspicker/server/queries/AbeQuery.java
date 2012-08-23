package com.bookspicker.server.queries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.OnlineOffer;
import com.bookspicker.shared.Bundle.Condition;
import com.bookspicker.shared.Offer.StoreName;

/**
 * 
 * @author Jonathan
 */


/**
 * Arguments:
 *  outputsize:
 *  Micro Ð includes result count*, book ID, ISBN 10 and 13, listing condition (NEW or
 * NOT NEW), item condition (new, very good, etc.)**, quantity, vendor currency, listing
 * price, total book price, first book shipping cost, extra book shipping cost, min and max
 * shipping days***, listing url
 * Short Ð includes all of above plus author, title, publisher name and catalogue image
 * Medium Ð includes all of the above plus vendor name, vendor location, vendor ID, seller
 * rating, keywords, subject, binding type, and edition type 
 * Long Ð includes all of the above plus book jacket, publication year, vendor price, and
 * vendor description, and vendor image (if there is one).
 *  
 *It is sorted with descending price by default.  
 * 
 * PID = our unique costumerId  
 *  
 */

public class AbeQuery implements BookstoreQuery {
    private String clientKey="2c016a82-5885-455f-b1ea-cd6beaf3d4eb"; 
    // Micro doesn't return us bullshit information that we don't need.
    private String searchLink ="http://search2.abebooks.com/search?clientkey="  + clientKey + "&outputsize=Long";
    private String preSaleURL ="http://www.abebooks.com/abe/ParaRoute?pid=17352&url=http://";
    private static XmlParsingTools xmlParsingTools = new XmlParsingTools();
   
    @Override
    public void getBooksOffers(Bundle bundle) {
        Condition bundleCondition = bundle.getCondition();
        for (Book book : bundle.getBooksThatNeedUpdates()) {
            // There are two calls for Condition.ALL in order to get the cheapest new and used book.
            if (bundleCondition == Condition.ALL || bundleCondition == Condition.NEW) {
                getOffer(bundle, book, searchLink + "&bookcondition=newonly");
            }
            if (bundleCondition == Condition.ALL || bundleCondition == Condition.USED) {
                getOffer(bundle, book, searchLink +"&bookcondition=usedonly");
            }
        } 
    }

    private void getOffer(Bundle bundle, Book book, String uri) {
        String url = uri + "&isbn=" + book.getEan();
        System.out.println(url);
        
        Document document = xmlParsingTools.parseXmlToDoc(url);
        if (document == null) return;
        Element item = (Element) document.getElementsByTagName("Book").item(0);
        if (item == null) return;
        double priceDbl = xmlParsingTools.getDoubleValue(item, "listingPrice");
        int price = (int) Math.round(priceDbl * 100);
        double shippingDbl = xmlParsingTools.getDoubleValue(item, "firstBookShipCost");
        int shippingPrice = (int) Math.round(shippingDbl * 100);
        String seller = xmlParsingTools.getTextValue(item, "vendorName");
        String condition = xmlParsingTools.getTextValue(item, "listingCondition") + " - " + 
            xmlParsingTools.getTextValue(item, "itemCondition");
        String saleURL = xmlParsingTools.getTextValue(item, "listingUrl");
        boolean international= isInternational(xmlParsingTools.getTextValue(item, "vendorDescription"));
        
        bundle.addOffer(book, new OnlineOffer(price, shippingPrice, StoreName.ABE_BOOKS, 
                seller, condition, preSaleURL + saleURL, international));
    }

    private boolean isInternational(String textValue) {
        boolean international = false;
        if (textValue != null) {
            international =  textValue.toLowerCase().matches(".*international.*");
        } 
        return international;
    }
}
