/**
 * 
 */
package com.bookspicker.server.queries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.OnlineOffer;
import com.bookspicker.shared.Bundle.Condition;
import com.bookspicker.shared.Offer.StoreName;

/**
 * @author Jonathan
 *
 */
public class AlibrisQuery implements BookstoreQuery {
    private static XmlParsingTools xmlParsingTools = new XmlParsingTools();
    private String siteId = "7UsZcU1scwU";
	private String usedRequest = 
		"http://partnersearch.alibris.com/cgi-bin/search?site=23615740&qcondhi=5&qisbn=";
	private String newRequest = 
		"http://partnersearch.alibris.com/cgi-bin/search?site=23615740&qcond=6&qisbn=";
	private String sellingLink = 
		"http://click.linksynergy.com/fs-bin/click?id=" + siteId + "&offerid=99238.122856000&type=2&tmpid=939&RD_PARM1=http://www.alibris.com/cart?"; 

	/* (non-Javadoc)
	 * @see com.bookspicker.server.queries.BookstoreQuery#getBooksOffers(com.bookspicker.shared.Bundle)
	 */
	@Override
	public void getBooksOffers(Bundle bundle) {
		Condition bundleCondition = bundle.getCondition();
		for (Book book : bundle.getBooksThatNeedUpdates()) {
			if (bundleCondition == Condition.ALL || bundleCondition == Condition.NEW) {
				getOffer(bundle, book, newRequest);
			}
			if (bundleCondition == Condition.ALL || bundleCondition == Condition.USED) {
				getOffer(bundle, book, usedRequest);
			}
		} 
	}

	private void getOffer(Bundle bundle, Book book, String uri) {
		
		Document document = xmlParsingTools.parseXmlToDoc(uri + book.getIsbn());
		if (document == null) return;

		Element item = (Element) document.getElementsByTagName("book").item(0);
		if (item == null) return;
		double priceDbl = xmlParsingTools.getDoubleValue(item, "price");
		int price = (int) Math.round(priceDbl * 100);
		String seller = xmlParsingTools.getTextValue(item, "sellername");
		String condition = xmlParsingTools.getTextValue(item, "condition");
		if (condition == null) {
		    condition =  xmlParsingTools.getTextValue(item, "comments");
		}
		String workId = xmlParsingTools.getTextValue(item, "work_id");
		String invId = xmlParsingTools.getTextValue(item, "bin");
		String url = sellingLink +  "invId=" + invId + "&pwork=" + workId + "&pisbn=" + book.getIsbn();
		bundle.addOffer(book, new OnlineOffer(price, 399, StoreName.ALIBRIS,seller, condition, url));
	}

	@Override
	public String toString() {
		return "AlibrisQuery";
	}
}