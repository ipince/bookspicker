package com.bookspicker.server.data.tools;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bookspicker.server.data.CoopOfferManager;
import com.bookspicker.server.data.tools.DataUtils.BookMat;
import com.bookspicker.shared.CoopOffer;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;

public class TheCoopOfferImporter {
	
	private static String mainPath;

	public static void main(String[] args) {
		// Choose school and term!
		School school = DataUtils.getSchoolFromUser();
		Term term = DataUtils.getTermFromUser();
		
		mainPath = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
			term.toString() + DataUtils.DIR_SEP + 
			DataUtils.FILE_DATE_PREFIX + 
			DataUtils.DIR_SEP;
		
		new TheCoopOfferImporter().importOffers();
	}

	/**
	 * Assumes that the table is empty!
	 */
	private void importOffers() {
		// Read bookmats into list
		List<BookMat> bookmats = new ArrayList<BookMat>();
		readBookMats("books.dat", bookmats);
		
		CoopOfferManager com = CoopOfferManager.getManager();
		CoopOffer offer;
//		List<CoopOffer> dbOffers;
		int usedPrice, newPrice;
		double usedPriceDbl, newPriceDbl;
		Set<String> isbns = new HashSet<String>();
		for (BookMat bookmat : bookmats) {
			if (!isbns.contains(bookmat.book.getIsbn())) {
				try {
//					dbOffers = com.getLocalOffersFor(bookmat.book);
					newPriceDbl = Double.valueOf(bookmat.newPrice.replace("$", ""));
					newPrice = (int) Math.round(newPriceDbl * 100);
					offer = new CoopOffer(bookmat.book.getIsbn(), "New", newPrice, bookmat.url);
					com.save(offer);

					usedPriceDbl = Double.valueOf(bookmat.usedPrice.replace("$", ""));
					usedPrice = (int) Math.round(usedPriceDbl * 100);
					offer = new CoopOffer(bookmat.book.getIsbn(), "Used", usedPrice, bookmat.url);
					com.save(offer);

				} catch (NumberFormatException e) {
					System.err.println("Cannot parse price(s): " + bookmat.newPrice + " and/or " + bookmat.usedPrice);
				}
				isbns.add(bookmat.book.getIsbn());
			}
		}
	}
	

	private void readBookMats(String filename, List<BookMat> bookmats) {
		try {
			//  Create a stream for reading.
			FileInputStream fis = new FileInputStream(mainPath + DataUtils.MAT_RESOLVED_PATH + filename);

			//  Next, create an object that can read from that file.
			ObjectInputStream inStream = new ObjectInputStream(fis);

			// Retrieve the Serializable object.
			BookMat bookmat;
			while ((bookmat = (BookMat) inStream.readObject()) != null) {
				bookmats.add(bookmat);
			}
		} catch (EOFException eof) {
			// file ended - ok
		} catch (IOException ioe) {
			System.err.println("Error reading books");
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Error reading books2: " + cnfe.getMessage());
		}
	}

}
