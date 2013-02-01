package com.bookspicker.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum Term implements IsSerializable {

    SPRING2009("SP09"), FALL2009("FA09"),
    SPRING2010("SP10"), SUMMER2010("SU10"), FALL2010("FA10"),
    SPRING2011("SP11"), FALL2011("FA11"),
    SPRING2012("SP12"), FALL2012("FA12"),
    SPRING2013("SP13"), FALL2013("FA13");

    public static final Term CURRENT_TERM = FALL2012;

    private String term;

    Term(String term) {
        this.term = term;
    }
    public String getTerm() {
        return term;
    }
}