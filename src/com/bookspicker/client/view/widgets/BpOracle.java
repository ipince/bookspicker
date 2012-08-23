package com.bookspicker.client.view.widgets;

import java.util.ArrayList;
import java.util.List;

import com.bookspicker.client.BooksPicker;
import com.bookspicker.client.service.SuggestionService;
import com.bookspicker.client.service.SuggestionServiceAsync;
import com.bookspicker.shared.School;
import com.bookspicker.shared.BpOracleSuggestion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SuggestOracle;

public class BpOracle extends SuggestOracle {

    private final static SuggestionServiceAsync SUGGESTION_SERVICE = GWT.create(SuggestionService.class);

    private final boolean SHOW_BOLD = true;
    private final Response EMPTY_RESPONSE =  new Response(new ArrayList<Suggestion>());

    private static BpOracle INSTANCE;

    private BpOracle() {}

    public static BpOracle getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BpOracle();
        }
        return INSTANCE;
    }

    @Override
    public boolean isDisplayStringHTML() {return SHOW_BOLD;};

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {

        GWT.log("Query: " + request.getQuery(), null);
        final School school = BooksPicker.getSchool();

        // Send RPC
        final long now = System.currentTimeMillis();
        SUGGESTION_SERVICE.getClassSuggestion(school, request.getQuery(), request.getLimit(), new AsyncCallback<List<BpOracleSuggestion>>() {

            @Override
            public void onSuccess(List<BpOracleSuggestion> result) {
                GWT.log(request.getQuery() + ": RPC took " + (System.currentTimeMillis() - now), null);
                Response response = null;
                if (SHOW_BOLD) {
                    List<BpOracleSuggestion> formatted = convertToFormattedSuggestions(request.getQuery().toLowerCase(), result);
                    response = new Response(formatted);
                } else {
                    response = new Response(result);
                }
                callback.onSuggestionsReady(request, response);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onSuggestionsReady(request, EMPTY_RESPONSE);
            }
        });
    }

    private final char WHITESPACE_CHAR = ' ';

    /**
     * Returns real suggestions with the given query in <code>strong</code> html
     * font.
     *
     * @param query query string
     * @param candidates candidates
     * @return real suggestions
     */
    private List<BpOracleSuggestion> convertToFormattedSuggestions(String query,
            List<BpOracleSuggestion> candidates) {
        List<BpOracleSuggestion> suggestions = new ArrayList<BpOracleSuggestion>();

        for (int i = 0; i < candidates.size(); i++) {
            BpOracleSuggestion candidate = candidates.get(i);
            int index = 0;
            int cursor = 0;
            // Use real suggestion for assembly.
            String formattedSuggestion = candidate.getDisplayString();

            // Create strong search string.
            StringBuffer accum = new StringBuffer();

            while (true) {
                index = candidate.getDisplayString().toLowerCase().indexOf(query, index);
                if (index == -1) {
                    break;
                }
                int endIndex = index + query.length();
                if (index == 0 || (WHITESPACE_CHAR == candidate.getDisplayString().toLowerCase().charAt(index - 1))) {
                    String part1 = escapeText(formattedSuggestion.substring(cursor, index));
                    String part2 = escapeText(formattedSuggestion.substring(index,
                            endIndex));
                    cursor = endIndex;
                    accum.append(part1).append("<strong>").append(part2).append(
                    "</strong>");
                }
                index = endIndex;
            }

            // Check to make sure the search was found in the string.
            if (cursor == 0) {
                continue;
            }

            // Finish creating the formatted string.
            String end = escapeText(formattedSuggestion.substring(cursor));
            accum.append(end);
            BpOracleSuggestion suggestion = new BpOracleSuggestion(candidate.getReplacementString(), accum.toString(), candidate.getQueryString());
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    private static HTML convertMe = GWT.isClient() ? new HTML() : null;
    private String escapeText(String escapeMe) {
        if (convertMe != null) {
            convertMe.setText(escapeMe);
            String escaped = convertMe.getHTML();
            return escaped;
        }
        return escapeMe;
    }

}
