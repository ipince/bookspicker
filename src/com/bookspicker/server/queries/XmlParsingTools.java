package com.bookspicker.server.queries;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bookspicker.Log4JInitServlet;

public class XmlParsingTools {
	
	private static Logger logger = Log4JInitServlet.logger;
	
	/**
	 * Renders a URI to a {@Document} format. Can be used to convert files and HTML.
	 *  @return The created document, or null if an error occurred.
	 */
	public Document parseXmlToDoc(String requestUri) {
		Document document;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			logger.error("Can't retrive XML Document" + e.getStackTrace().toString());
			return null;
		}
		try {
			logger.debug("XmlParsingTools - requestURI: " + requestUri);
			document = db.parse(requestUri);
		} catch (SAXException e) {
			logger.error("Can't retrive XML Document" + e.getStackTrace().toString());
			return null;
		} catch (IOException e) {
			logger.error("Can't retrive XML Document" + e.getStackTrace().toString());
			return null;
		}
		return document;
	}

	/**
	 * Gets textValue from an XML element children.
	 * 
	 * @return The extracted String, or null if an error occurred.
	 */
	public String getTextValue(Element ele, String tagName) {
		if (ele == null) {
			return null;
		}
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			if (el.getTextContent() != null) {
				textVal = el.getTextContent();
			}
		}
		return textVal;
	}  

	/**
	 * Gets intValue from XML element Children.
	 * 
	 * @return The extracted int, or -1 if an error occurred.
	 */
	public int getIntValue(Element ele, String tagName) {
		String value = getTextValue(ele, tagName);
		if (value == null) {
			return -1;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException ex) { 
			//TODO(Rodrigo) - add logging.
			return -1;
		}
	}

	/**
	 * Gets DoubleValue from XML element Children.
	 * @return The extracted double, or -1 if an error occurred.
	 */
	public double getDoubleValue(Element ele, String tagName) {
		String value = getTextValue(ele, tagName);
		if (value == null) {
			return -1;
		}
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException ex) { 
			//TODO(Rodrigo) - add logging.
			return -1.0;
		}
	}

	/**
	 * If the same tag appears multiple times it retrieves all the elements.
	 */
	public String[] getAllTextValue(Element ele, String tagName) {
		String[] textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			textVal = new String[nl.getLength()];
			for (int i = 0; i < nl.getLength(); i++) {      
				Element el = (Element) nl.item(i);
				textVal[i] = el.getTextContent();//getFirstChild().getNodeValue();
			}
		}
		return textVal;
	}

	/**
	 * Canonicalize the query string as required by Amazon.
	 * 
	 * @param sortedParamMap    Parameter name-value pairs in lexicographical order.
	 * @return                  Canonical form of query string.
	 */
	public String canonicalize(SortedMap<String, String> sortedParamMap) {
		if (sortedParamMap.isEmpty()) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();
		Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry<String, String> kvpair = iter.next();
			buffer.append(percentEncodeRfc3986(kvpair.getKey()));
			buffer.append("=");
			buffer.append(percentEncodeRfc3986(kvpair.getValue()));
			if (iter.hasNext()) {
				buffer.append("&");
			}
		}
		String cannoical = buffer.toString();
		return cannoical;
	}

	/**
	 * Percent-encode values according the RFC 3986. The built-in Java
	 * URLEncoder does not encode according to the RFC, so we make the
	 * extra replacements.
	 * 
	 * @param s decoded string
	 * @return  encoded string per RFC 3986
	 */
	public String percentEncodeRfc3986(String s) {
		String out;
		try {
			out = URLEncoder.encode(s, AmazonSignedRequestsHelper.UTF8_CHARSET)
			.replace("+", "%20")
			.replace("*", "%2A")
			.replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			out = s;
		}
		return out;
	}
}