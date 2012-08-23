package com.bookspicker.shared;

import java.io.Serializable;

public enum School implements Serializable {
	
	//Note: SchoolSelectionPage.ui.xml has these strings hardcoded.
	NONE("none"),
    MIT("mit"),
    HARVARD("harvard"),
    UCHICAGO("uchicago"),
    DARTMOUTH("dartmouth"),
    NORTHWESTERN("northwestern");

    private final String shortname;
//    private final String amazonAffiliateId;
    
    private School(String shortname) {
        this.shortname = shortname;
    }
    
    public String getName() {
        return shortname;
    }
    
    public String getClassCode(String coursePart,
    		String classPart, String sectionPart) {
    	switch (this) {
    	case MIT:
    		return coursePart + classPart;
    	case UCHICAGO:
    		return coursePart + " / " + classPart + " - " + sectionPart;
    	case NORTHWESTERN:
    		return coursePart + " " + classPart;
    	}
    	return coursePart + classPart;
    }

	public boolean isClass(String query) {
	   String p = "^" + name() + "_\\d*$";
	   return query.matches(p);
	}

	public String cleanClass(String query) {
		return query.substring(name().length() + 1);
	}
	    
	
	public static School fromName(String name) {
		School school;
		try {
			school = School.valueOf(name.toUpperCase());
			return school;
		} catch (RuntimeException e) {
			return NONE;
		}
	}
}
