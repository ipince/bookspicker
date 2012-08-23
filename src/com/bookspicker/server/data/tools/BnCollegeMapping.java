package com.bookspicker.server.data.tools;

import java.util.HashMap;
import java.util.Map;

import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;

public class BnCollegeMapping {
	
	/**
	 * To find out what these ought to be. Simply visit The Coop's
	 * textbook selection page, enable Firebug's Console to see outgoing
	 * requests, and then select a term from the dropdown. Copy the
	 * URL request and examine its parameters.
	 */
	public final String campusId;
	public final Map<Term, String> termIds;
	public final String storeId;
	public final String catalogId;
	public final String subdomain;
	
	private static final BnCollegeMapping MIT;
	private static final BnCollegeMapping HARVARD;
	private static final BnCollegeMapping UCHICAGO;
	private static final BnCollegeMapping DARTMOUTH;
	private static final BnCollegeMapping NORTHWESTERN;
	
	static {
		// Here are all the mappings
		Map<Term, String> mitTerms = new HashMap<Term, String>();
		mitTerms.put(Term.SPRING2010, "39998878");
		mitTerms.put(Term.SUMMER2010, "42412987");
		mitTerms.put(Term.FALL2010, "42412988");
		mitTerms.put(Term.SPRING2011, "45154119");
		
		MIT = new BnCollegeMapping(
				"mitcoopbooks",
				"33484058", // campusId
				mitTerms,
				"52081", // storeId
				"10001"); // catalogId
		
		Map<Term, String> harvardTerms = new HashMap<Term, String>();
		harvardTerms.put(Term.SUMMER2010, "41247149");
		harvardTerms.put(Term.FALL2010, "41918310");
		
		HARVARD = new BnCollegeMapping(
				"harvardcoopbooks",
				"33484061", // campusId
				harvardTerms,
				"52084", // storeId
				"10001"); // catalogId
		
		Map<Term, String> chicagoTerms = new HashMap<Term, String>();
		chicagoTerms.put(Term.SPRING2011, "45805605");
		
		UCHICAGO = new BnCollegeMapping(
				"uchicago",
				"14704448", // campusId
				chicagoTerms,
				"15063", // storeId
				"10001"); // catalogId
		
		Map<Term, String> dartmouthTerms = new HashMap<Term, String>();
		dartmouthTerms.put(Term.SPRING2011, "45761889");
		
		DARTMOUTH = new BnCollegeMapping(
				"dartmouthbooks",
				"30533055", // campusId
				dartmouthTerms,
				"30555", // storeId
				"10001"); // catalogId
		
		Map<Term, String> northwesternTerms = new HashMap<Term, String>();
		northwesternTerms.put(Term.SPRING2011, "45842246");
		
		NORTHWESTERN = new BnCollegeMapping(
				"northwestern",
				"30533053", // campusId
				northwesternTerms,
				"30553", // storeId
				"10001"); // catalogId
	}
	
	public static BnCollegeMapping getMappingsFor(School school) {
		switch (school) {
		case MIT: return MIT;
		case HARVARD: return HARVARD;
		case UCHICAGO: return UCHICAGO;
		case DARTMOUTH: return DARTMOUTH;
		case NORTHWESTERN: return NORTHWESTERN;
		}
		return null;
	}
	
	private BnCollegeMapping(String subdomain,
			String campusId, Map<Term, String> termIds,
			String storeId, String catalogId) {
		this.subdomain = subdomain;
		this.campusId = campusId;
		this.termIds = termIds;
		this.storeId = storeId;
		this.catalogId = catalogId;
	}

}
