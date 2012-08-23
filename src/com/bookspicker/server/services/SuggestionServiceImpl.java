package com.bookspicker.server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.client.service.SuggestionService;
import com.bookspicker.server.data.ClassManager;
import com.bookspicker.shared.BpOracleSuggestion;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;
import com.bookspicker.shared.Trie;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SuggestionServiceImpl extends RemoteServiceServlet implements SuggestionService {

	private static Logger logger = Log4JInitServlet.logger;
	
	private static final Map<School, Trie<BpOracleSuggestion>> trieMap = new HashMap<School, Trie<BpOracleSuggestion>>();
	// cache
	private final Map<School, Map<String, List<BpOracleSuggestion>>> cache = new HashMap<School, Map<String, List<BpOracleSuggestion>>>();
	
	@Override
	public List<BpOracleSuggestion> getClassSuggestion(School school, String query, int limit) {
		long now = System.currentTimeMillis();
		 List<BpOracleSuggestion> suggestions = null;
		if (cache.containsKey(school)) {
		    suggestions = cache.get(school).get(query);
		}
		if (suggestions == null) {
			suggestions = trieMap.get(school).getData(query, limit);
			if (!cache.containsKey(school)) {
			    cache.put(school, new HashMap<String, List<BpOracleSuggestion>>());
			}
			cache.get(school).put(query, suggestions);
		}
		logger.info("SuggestionServiceImpl - Sending " + suggestions.size() + " suggestions for '" + query + "'; took " + (System.currentTimeMillis() - now) + " ms");
		return suggestions;
	}
	
	public static void preprocessClasses() {
		logger.info("SuggestionServiceImpl - Preprocessing classes for suggestions");
			List<SchoolClass> classes = ClassManager.getManager().listClasses(Term.CURRENT_TERM);
			int n = 0;
			for (SchoolClass clas : classes) {
			    Long classId  = clas.getId();
			    String suggestedTitle = clas.getFormatedClassName(); 
			    School school = clas.getSchool();
			    logger.debug("school: " + school.getName() + " class id:" + classId + " title: " + suggestedTitle);

			    if (!trieMap.containsKey(school)) {
			        trieMap.put(school, new Trie<BpOracleSuggestion>());
			    }
			    trieMap.get(school).addWord(suggestedTitle, new BpOracleSuggestion(suggestedTitle, suggestedTitle, school.getName() +"_"+ classId));
			    n++;
			}
			logger.info("SuggestionServiceImpl - Preprocessed " + n + " suggestions");
	    }
}