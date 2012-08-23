package com.bookspicker.server.services;

import java.util.Date;
import java.util.List;

import com.bookspicker.client.service.StatService;
import com.bookspicker.server.data.MarketDataPointManager;
import com.bookspicker.server.data.StatManager;
import com.bookspicker.server.data.market.MarketDataPoint;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.Stat;
import com.bookspicker.shared.User;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class StatServiceImpl extends RemoteServiceServlet implements StatService {
	
	@Override
	public void logBuyClick(final String isbn, final Offer clickedOffer,
			final List<Offer> competingOffers) {
		
		// Log buy click
		User user = UserServiceImpl.getUser(getThreadLocalRequest(), getThreadLocalResponse());
		final String uid = user != null ? user.getId().toString() : null;
		final String ip = getThreadLocalRequest().getRemoteAddr();
		
		HelperThreads.execute(new Runnable() {
			@Override
			public void run() {
				
				Stat stat = Stat.newBuyLinkStat(isbn,
						clickedOffer.getStoreName().getName(),
						clickedOffer.getTotalPrice(),
						clickedOffer.getCondition(),
						clickedOffer instanceof LocalOffer ? ((LocalOffer) clickedOffer).getId().toString() : null,
						uid, ip);
				stat = StatManager.getManager().save(stat);
				
				// Save competing offers for market analysis
				MarketDataPointManager mdpManager = MarketDataPointManager.getManager();
				MarketDataPoint mdp;
				Date date = new Date();
				for (Offer offer : competingOffers) {
					mdp = new MarketDataPoint(stat.getId(), offer, date);
					mdpManager.save(mdp);
				}
			}
		});
	}
	
}
