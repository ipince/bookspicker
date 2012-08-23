package com.bookspicker.server.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bookspicker.server.data.market.MarketDataPoint;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Stat;
import com.bookspicker.shared.Term;
import com.bookspicker.shared.LocalOffer.Strategy;
import com.bookspicker.shared.Offer.StoreName;

public class EvaluationScripts {
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
//		calculateBookSearchBreakdown();
//		calculateBundleSearchStats();
		calculateLocalMarketBuyerEffectiveness();
//		calculateLocalMarketSellerEffectiveness();
//		calculateConversionRates();
//		calculateAutoPriceStats();
		System.out.println("Took " + (System.currentTimeMillis() - start) + "ms");
	}
	
	private static void calculateBundleSearchStats() {
		List<Stat> stats = StatManager.getManager().getStatsOfType(Stat.BUNDLE_SEARCH);
		
		int min = -1;
		int max = -1;
		int sum = 0;
		int count = 0;
		
		String[] dummy;
		for (Stat stat : stats) {
			dummy = stat.search.split(",");
			if (dummy.length < min || min == -1) 
				min = dummy.length;
			
			if (dummy.length > max || max == -1) 
				max = dummy.length;
			
			sum += dummy.length;
			count++;
		}
		
		System.out.println("min: " + min);
		System.out.println("max: " + max);
		System.out.println("sum: " + sum);
		System.out.println("count: " + count);
		System.out.println("avg: " + (sum * 1.0 / count));
	}
	
	private static void calculateBookSearchBreakdown() {
		List<Stat> stats = StatManager.getManager().getStatsOfType(Stat.BOOK_SEARCH);
		
		int isbnCount = 0;
		int titleCount = 0;
		for (Stat stat : stats) {
			if (stat.search.matches("(\\d{12}|\\d{9})(\\d|X)")) {
				isbnCount++;
				System.out.println("         ISBN: " + stat.search);
			} else {
				titleCount++;
				System.out.println("TITLE: " + stat.search);
			}
		}
		
		System.out.println("Title count: " + titleCount);
		System.out.println("ISBN count: " + isbnCount);
	}
	
	private static void calculateLocalMarketBuyerEffectiveness() {
		// Date stuff
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date dataStart = null;
		Date dataEnd = null;
		Date regDay = null;
        try {
            dataStart = df.parse("09/09/2010");
            dataEnd = df.parse("22/09/2010");
            regDay = df.parse("07/09/2010");
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
		
		// Get number of times local offers were shown
		List<LocalOffer> offers = LocalOfferManager.getManager().getAllLocalOffers();
		double numTimesBpOffersWereShownProRated = 0; // estimate of numTimesShown AFTER reg day
		int numTimesBpOffersWereShownAbsolute = 0; // sum of all numTimesShown
		for (LocalOffer offer : offers) {
			numTimesBpOffersWereShownAbsolute += offer.numTimesShown;
			if (offer.getCreationDate().after(regDay)) {
				numTimesBpOffersWereShownProRated += offer.numTimesShown;
			} else { // created before reg day
				if (offer.isSold() && offer.timeSold.before(regDay)) {
					// add nothing -- no way to estimate
				} else {
					// add pro-rated amount of times shown
					Date endDate = dataEnd;
					if (offer.isSold()) // after reg day!
						endDate = offer.timeSold;
					
					long tmp1 = endDate.getTime() - regDay.getTime();
					long tmp2 = endDate.getTime() - offer.getCreationDate().getTime();
					numTimesBpOffersWereShownProRated +=
						offer.numTimesShown * (tmp1 * 1.0 / tmp2);
				}
			}
		}
		
		// Get number of books searched for overall (AFTER september 9)
		int numBookSearchesWithinDateRange = 0;
        String[] dummy;
		List<Stat> bundleStats = StatManager.getManager().getStatsOfType(Stat.BUNDLE_SEARCH);
		for (Stat stat : bundleStats) {
			if (stat.date.after(dataStart)) { // valid date
				dummy = stat.search.split(",");
				numBookSearchesWithinDateRange += dummy.length;
			}
		}
		
		// Get other data (from BUY clicks)
		List<Stat> stats = StatManager.getManager().getStatsOfType(Stat.BUY_LINK_CLICK);
		MarketDataPointManager mdpm = MarketDataPointManager.getManager();
		List<MarketDataPoint> points;
		
		int totalClicks = stats.size();
		int clicksWithData = 0;
		
		int numBpOffersClicked = 0;
		int numLocalOffersClicked = 0;
//		List<Integer> bpClickedIndeces = new ArrayList<Integer>(); // for sanity check
//		List<Integer> bpClickedIndeces2 = new ArrayList<Integer>(); // for sanity check
		for (int i = 0; i < stats.size(); i++) {
			if (stats.get(i).store.equals(StoreName.LOCAL.getName())) {
				numBpOffersClicked++;
//				bpClickedIndeces.add(i); // sanity check
			}
			if (isLocal(stats.get(i).store))
				numLocalOffersClicked++;
		}
		
		int numLocalVsNonLocalChoices = 0;
		int choseLocal = 0;
		int choseLocalWhenItWasNOTCheapest = 0;
		int localVsNonLocalPremiumSum = 0;
		
		int numBpVsOtherChoices = 0;
		int numBpCheapest = 0;
		int choseBp = 0;
		int choseBpWhenItWasNOTCheapest = 0;
		int bpVsOtherPremiumSum = 0;
		
		boolean bp, local, nonLocal, bpCheapest;
		Integer lowestBp, lowestLocal, lowestNonLocal;
		
		
		int bpWasOption = 0;
		int bpWasCheapest = 0;
		int bpWasCheapestExcludingIntl = 0;
		int bpClicks = 0;
		int bpClicksWhenCheapest = 0;
		int bpClicksWhenCheapestExcludingIntl = 0;
		int bpCheapPriceSum = 0;
		int bpCheapPriceSumExcludingIntl = 0;
		int bpPremium = 0;
		int bpPremiumExcludingIntl = 0;
		int localWasOption = 0;
		int localWasCheapest = 0;
		int localWasCheapestExcludingIntl = 0;
		int localClicks = 0;
		int localClicksWhenCheapest = 0;
		int localClicksWhenCheapestExcludingIntl = 0;
		int localCheapPriceSum = 0;
		int localCheapPriceSumExcludingIntl = 0;
		int localPremium = 0;
		int localPremiumExcludingIntl = 0;
		int nonLocalWasOption = 0;
		int nonLocalWasCheapest = 0;
		int nonLocalWasCheapestExcludingIntl = 0;
		int nonLocalClicks = 0;
		int nonLocalClicksWhenCheapest = 0;
		int nonLocalClicksWhenCheapestExcludingIntl = 0;
		int nonLocalCheapPriceSum = 0;
		int nonLocalCheapPriceSumExcludingIntl = 0;
		int nonLocalPremium = 0;
		int nonLocalPremiumExcludingIntl = 0;
		int amazonWasOption = 0;
		int amazonWasCheapest = 0;
		int amazonWasCheapestExcludingIntl = 0;
		int amazonClicks = 0;
		int amazonClicksWhenCheapest = 0;
		int amazonClicksWhenCheapestExcludingIntl = 0;
		int amazonCheapPriceSum = 0;
		int amazonCheapPriceSumExcludingIntl = 0;
		int amazonPremium = 0;
		int amazonPremiumExcludingIntl = 0;
		int allClicksWhenCheapest = 0;
		int allClicksWhenCheapestExcludingIntl = 0;
		int allPremium = 0;
		int allPremiumExcludingIntl = 0;
		
		int sumAllOptions = 0;
		int sumLocalOptions = 0;
		int sumNonLocalOptions = 0;
		int sumBpOptions = 0;
		int numChoices = 0;
		
		boolean bpFound, localFound, nonLocalFound, amazonFound;
		Integer cheapestPrice, cheapestNonInternational;
		String cheapestStore, cheapestStoreExcludingIntl;
		for (int i = 0; i < stats.size(); i++) {
			Stat stat = stats.get(i);
			// Sanity check round (comment out on real round) TODO: comment out
//			if (!bpClickedIndeces.contains(i))
//				continue;
			
			if (stat.isbn.equals("0138147574")) // skip due to skew
				continue;
//			if (stat.localId != null && stat.localId.equals("326")) // skip due to skew
//				continue;
//			if (stat.localId != null && stat.localId.equals("283")) // skip due to skew
//				continue;
			
			points = mdpm.getDataPointsWithBuyId(stat.getId().toString());
			
			if (!points.isEmpty()) { // SINCE 9/9, when market data was introduced
				
				boolean skipIntl = false;
				// Fix data points due to stupid mistake (add some default shipping costs)
				for (MarketDataPoint point : points) {
					if (!isLocal(point.store)) {
						if (point.store.equals(StoreName.ABE_BOOKS.getName())) {
							point.price += 399;
						} else if (point.store.equals(StoreName.ALIBRIS.getName())) {
							point.price += 399;
						} else if (point.store.equals(StoreName.AMAZON.getName())) {
							point.price += 399;
						} else if (point.store.equals(StoreName.AMAZON_MARKETPLACE.getName())) {
							point.price += 399;
						} else if (point.store.equals(StoreName.HALF.getName())) {
							point.price += 399;
						}
					}
					if (point.bookCondition.toLowerCase().contains("international"))
						skipIntl = true;
				}
				if (skipIntl)
					continue;
				
				// get cheapest price and find sellers
				bpFound = false; localFound = false; nonLocalFound = false; amazonFound = false;
				cheapestPrice = null; cheapestNonInternational = null;
				cheapestStore = ""; cheapestStoreExcludingIntl = "";
				for (MarketDataPoint point : points) {
					if (cheapestPrice == null) { // first one is cheapest
						cheapestPrice = point.price;
						cheapestStore = point.store;
					}
					if (!point.bookCondition.toLowerCase().contains("international")) {
						if (cheapestNonInternational == null) { // first on is cheapest
							cheapestNonInternational = point.price;
							cheapestStoreExcludingIntl = point.store;
						}
					}
					
					if (point.store.equals(StoreName.LOCAL.getName())) {
						bpFound = true;
						sumBpOptions++;
					}
					if (point.store.equals(StoreName.AMAZON.getName()) ||
							point.store.equals(StoreName.AMAZON_MARKETPLACE.getName()))
						amazonFound = true;
					if (isLocal(point.store)) {
						localFound = true;
						sumLocalOptions++;
					} else {
						nonLocalFound = true;
						sumNonLocalOptions++;
					}
				}
				sumAllOptions += points.size();
				numChoices++;
				
				// actual data gathered
				
				// Was seller x an option? If so, was it the cheapest?
				if (bpFound) {
					bpWasOption++;
					if (cheapestStore.equals(StoreName.LOCAL.getName()))
						bpWasCheapest++;
					if (cheapestStoreExcludingIntl.equals(StoreName.LOCAL.getName()))
						bpWasCheapestExcludingIntl++;
				}
				if (localFound) {
					localWasOption++;
					if (isLocal(cheapestStore))
						localWasCheapest++;
					if (isLocal(cheapestStoreExcludingIntl))
						localWasCheapestExcludingIntl++;
				}
				if (nonLocalFound) {
					nonLocalWasOption++;
					if (!isLocal(cheapestStore))
						nonLocalWasCheapest++;
					if (!isLocal(cheapestStoreExcludingIntl))
						nonLocalWasCheapestExcludingIntl++;
				}
				if (amazonFound) {
					amazonWasOption++;
					if (cheapestStore.equals(StoreName.AMAZON.getName()) ||
							cheapestStore.equals(StoreName.AMAZON_MARKETPLACE.getName()))
						amazonWasCheapest++;
					if (cheapestStoreExcludingIntl.equals(StoreName.AMAZON.getName()) ||
							cheapestStoreExcludingIntl.equals(StoreName.AMAZON_MARKETPLACE.getName()))
						amazonWasCheapestExcludingIntl++;
				}
				
				// In general
				clicksWithData++;
				if (stat.price <= cheapestPrice) {
					allClicksWhenCheapest++;
				} else {
					allPremium += stat.price - cheapestPrice;
				}
				if (stat.price <= cheapestNonInternational) {
					allClicksWhenCheapestExcludingIntl++;
				} else {
					allPremiumExcludingIntl += stat.price - cheapestNonInternational;
				}
				
				// For BP
				if (stat.store.equals(StoreName.LOCAL.getName())) {
					bpClicks++;
//					bpClickedIndeces2.add(i); // For sanity check only
					if (stat.price <= cheapestPrice) {
						bpClicksWhenCheapest++;
					} else {
						bpPremium += stat.price - cheapestPrice;
						bpCheapPriceSum += cheapestPrice;
					}
					if (stat.price <= cheapestNonInternational) {
						bpClicksWhenCheapestExcludingIntl++;
					} else {
						bpPremiumExcludingIntl += stat.price - cheapestNonInternational;
						bpCheapPriceSumExcludingIntl += cheapestNonInternational;
					}
				}
				
				// For Local
				if (isLocal(stat.store)) {
					localClicks++;
					if (stat.price <= cheapestPrice) {
						localClicksWhenCheapest++;
					} else {
						localPremium += stat.price - cheapestPrice;
						localCheapPriceSum += cheapestPrice;
					}
					if (stat.price <= cheapestNonInternational) {
						localClicksWhenCheapestExcludingIntl++;
					} else {
						localPremiumExcludingIntl += stat.price - cheapestNonInternational;
						localCheapPriceSumExcludingIntl += cheapestNonInternational;
					}
				}
				
				// For NonLocal
				if (!isLocal(stat.store)) {
					nonLocalClicks++;
					if (stat.price <= cheapestPrice) {
						nonLocalClicksWhenCheapest++;
					} else {
						nonLocalPremium += stat.price - cheapestPrice;
						nonLocalCheapPriceSum += cheapestPrice;
					}
					if (stat.price <= cheapestNonInternational) {
						nonLocalClicksWhenCheapestExcludingIntl++;
					} else {
						nonLocalPremiumExcludingIntl += stat.price - cheapestNonInternational;
						nonLocalCheapPriceSumExcludingIntl += cheapestNonInternational;
					}
				}
				
				// For Amazon
				if (stat.store.equals(StoreName.AMAZON.getName()) ||
						stat.store.equals(StoreName.AMAZON_MARKETPLACE.getName())) {
					amazonClicks++;
					if (stat.price <= cheapestPrice) {
						amazonClicksWhenCheapest++;
					} else {
						amazonPremium += stat.price - cheapestPrice;
						amazonCheapPriceSum += cheapestPrice;
					}
					if (stat.price <= cheapestNonInternational) {
						amazonClicksWhenCheapestExcludingIntl++;
					} else {
						amazonPremiumExcludingIntl += stat.price - cheapestNonInternational;
						amazonCheapPriceSumExcludingIntl += cheapestNonInternational;
					}
				}
				
				
//				bp = false; local = false; nonLocal = false;
//				bpCheapest = false;
//				lowestBp = null; lowestLocal = null; lowestNonLocal = null;
//				
//				for (MarketDataPoint point : points) {
//					if (point.store.equals(StoreName.LOCAL.getName())) {
//						bp = true;
//						if (lowestBp == null || point.price < lowestBp)
//							lowestBp = point.price;
//					}
//						
//					if (isLocal(point.store)) {
//						local = true;
//						if (lowestLocal == null || point.price < lowestLocal)
//							lowestLocal = point.price;
//					} else {
//						nonLocal = true;
//						if (lowestNonLocal == null || point.price < lowestNonLocal)
//							lowestNonLocal = point.price;
//					}
//				}
//				
//				// local vs. nonlocal choice
//				if (local && nonLocal) {
//					numLocalVsNonLocalChoices++;
//					
//					if (isLocal(stat.store)) {
//						choseLocal++;
//						if (stat.price > lowestNonLocal) {
//							choseLocalWhenItWasNOTCheapest++;
//							localVsNonLocalPremiumSum += stat.price - lowestNonLocal;
//						}
//					}
//				}
//				
//				// BP vs. other
//				if (bp && nonLocal) {
//					numBpVsOtherChoices++;
//					if (stat.price <= Math.min(lowestLocal, lowestNonLocal))
//						numBpCheapest++;
//					
//					if (stat.store.equals(StoreName.LOCAL.getName())) {
//						choseBp++;
//						if (stat.price > Math.min(lowestLocal, lowestNonLocal)) {
//							choseBpWhenItWasNOTCheapest++;
//							bpVsOtherPremiumSum += stat.price - Math.min(lowestLocal, lowestNonLocal);
//						}
//					}
//				}
			}
		}
		
		System.out.println("Number of times BooksPicker books were shown: " + numTimesBpOffersWereShownAbsolute);
		System.out.println("Pro-rated number of times BooksPicker books were shown: " + numTimesBpOffersWereShownProRated);
		System.out.println("Total book searches within data date range: " + numBookSearchesWithinDateRange);
		
		System.out.println();
		System.out.println("Num times BP was an option: " + bpWasOption);
		System.out.println("Num times BP was cheapest option: " + bpWasCheapest);
		System.out.println("Num times BP was chosen: " + bpClicks);
		System.out.println("Num times BP was chosen when cheapest: " + bpClicksWhenCheapest);
		System.out.println("Sum of Cheapest prices: " + bpCheapPriceSum);
		System.out.println("Sum of BP premium: " + bpPremium);
		System.out.println("Num times BP was cheapest option (EI): " + bpWasCheapestExcludingIntl);
		System.out.println("Num times BP was chosen when cheapest (EI): " + bpClicksWhenCheapestExcludingIntl);
		System.out.println("Sum of Cheapest prices (EI): " + bpCheapPriceSumExcludingIntl);
		System.out.println("Sum of BP premium (EI): " + bpPremiumExcludingIntl);
		
		System.out.println();
		System.out.println("Num times local was an option: " + localWasOption);
		System.out.println("Num times local was cheapest option: " + localWasCheapest);
		System.out.println("Num times local chosen: " + localClicks);
		System.out.println("Num times local chosen when cheapest: " + localClicksWhenCheapest);
		System.out.println("Sum of Cheapest prices: " + localCheapPriceSum);
		System.out.println("Sum of local premium: " + localPremium);
		System.out.println("Num times local was cheapest option (EI): " + localWasCheapestExcludingIntl);
		System.out.println("Num times local chosen when cheapest (EI): " + localClicksWhenCheapestExcludingIntl);
		System.out.println("Sum of Cheapest prices (EI): " + localCheapPriceSumExcludingIntl);
		System.out.println("Sum of local premium (EI): " + localPremiumExcludingIntl);
		
		System.out.println();
		System.out.println("Num times nonlocal was an option: " + nonLocalWasOption);
		System.out.println("Num times nonlocal was cheapest option: " + nonLocalWasCheapest);
		System.out.println("Num times nonlocal chosen: " + nonLocalClicks);
		System.out.println("Num times nonlocal chosen when cheapest: " + nonLocalClicksWhenCheapest);
		System.out.println("Sum of Cheapest prices: " + nonLocalCheapPriceSum);
		System.out.println("Sum of nonlocal premium: " + nonLocalPremium);
		System.out.println("Num times nonlocal was cheapest option (EI): " + nonLocalWasCheapestExcludingIntl);
		System.out.println("Num times nonlocal chosen when cheapest (EI): " + nonLocalClicksWhenCheapestExcludingIntl);
		System.out.println("Sum of Cheapest prices (EI): " + nonLocalCheapPriceSumExcludingIntl);
		System.out.println("Sum of nonlocal premium (EI): " + nonLocalPremiumExcludingIntl);
		
		System.out.println();
		System.out.println("Num times amazon was an option: " + amazonWasOption);
		System.out.println("Num times amazon was cheapest option: " + amazonWasCheapest);
		System.out.println("Num times amazon chosen: " + amazonClicks);
		System.out.println("Num times amazon chosen when cheapest: " + amazonClicksWhenCheapest);
		System.out.println("Sum of Cheapest prices: " + amazonCheapPriceSum);
		System.out.println("Sum of amazon premium: " + amazonPremium);
		System.out.println("Num times amazon was cheapest option (EI): " + amazonWasCheapestExcludingIntl);
		System.out.println("Num times amazon chosen when cheapest (EI): " + amazonClicksWhenCheapestExcludingIntl);
		System.out.println("Sum of Cheapest prices (EI): " + amazonCheapPriceSumExcludingIntl);
		System.out.println("Sum of amazon premium (EI): " + amazonPremiumExcludingIntl);
		
		System.out.println();
		System.out.println("Total clicks: " + totalClicks);
		System.out.println("Clicks with data : " + clicksWithData);
		System.out.println("Total clicks when cheapest: " + allClicksWhenCheapest);
		System.out.println("Sum of All premium: " + allPremium);
		System.out.println("Total clicks when cheapest (EI): " + allClicksWhenCheapestExcludingIntl);
		System.out.println("Sum of All premium (EI): " + allPremiumExcludingIntl);
		
		System.out.println();
		System.out.println("Sum options: " + sumAllOptions);
		System.out.println("Sum BP options: " + sumBpOptions);
		System.out.println("Sum local options: " + sumLocalOptions);
		System.out.println("Sum nonlocal options: " + sumNonLocalOptions);
		System.out.println("Num choices (search results): " + numChoices);
		
		System.out.println();
		System.out.println("Local offers clicked: " + numLocalOffersClicked);
		System.out.println("BP offers clicked: " + numBpOffersClicked);
		
		
		// For sanity check purposes only
//		System.out.println("Bp clicked indeces 1 (" + bpClickedIndeces.size() + "): " + bpClickedIndeces.toString());
//		System.out.println("Bp clicked indeces 2 (" + bpClickedIndeces2.size() + "): " + bpClickedIndeces2.toString());
//		List<Integer> diff = new ArrayList<Integer>();
//		for (Integer i1 : bpClickedIndeces)
//			if (!bpClickedIndeces2.contains(i1))
//				diff.add(i1);
//		System.out.println("Difference (" + diff.size() + "): " + diff.toString());
	}
	
	private static boolean isLocal(String store) {
		return (store.equals(StoreName.BOOK_EX.getName()) ||
				store.equals(StoreName.THE_COOP.getName()) ||
				store.equals(StoreName.LOCAL.getName()));
	}
	
	private static void calculateLocalMarketSellerEffectiveness() {
		List<LocalOffer> offers = LocalOfferManager.getManager().getAllLocalOffers();
		ClassManager cm = ClassManager.getManager();
		SchoolClass sc;
		
		int totalOffers = offers.size();
		int totalRealOffers = 0;
		int totalOffersWithClassData = 0;
		int totalOffersWithAccurateClassData = 0;
		int totalOffersWithInaccurateClassData = 0;
		
		int totalOffersSoldOnce = 0; // "sold" (email sent at least once)
		int totalOffersSoldAndNotReposted = 0; // presumably really sold
		int totalOffersWithClassDataSoldOnce = 0;
		int totalOffersWithClassDataSoldAndNotReposted = 0;
		int totalOffersWithAccurateClassDataSoldOnce = 0;
		int totalOffersWithAccurateClassDataSoldAndNotReposted = 0;
		int totalOffersWithInaccurateClassDataSoldOnce = 0;
		int totalOffersWithInaccurateClassDataSoldAndNotReposted = 0;
		
		Long totalTimeOnMarket;
		for (LocalOffer offer : offers) {
			totalTimeOnMarket = offer.timeOnMarketAggressive + 
				offer.timeOnMarketConservative +
				offer.timeOnMarketFixed;
			
			if (totalTimeOnMarket > 1000 * 60 * 60) { // > 1 hour
				totalRealOffers++;
				if (offer.soldOnce)
					totalOffersSoldOnce++;
				if (offer.isSold())
					totalOffersSoldAndNotReposted++;
				
				if (offer.getClassCode() != null) {
					totalOffersWithClassData++;
					if (offer.soldOnce)
						totalOffersWithClassDataSoldOnce++;
					if (offer.isSold())
						totalOffersWithClassDataSoldAndNotReposted++;
					
					sc = cm.getClassByCode(School.MIT, Term.CURRENT_TERM, offer.getClassCode());
					if (sc != null) {
						boolean accurate = false;
						for (ClassBook cb : sc.getBooks()) {
							if (cb.getBook().getIsbn().equals(offer.getBook().getIsbn())) {
								accurate = true;
								break;
							}
						}
						if (accurate) {
							totalOffersWithAccurateClassData++;
							if (offer.soldOnce)
								totalOffersWithAccurateClassDataSoldOnce++;
							if (offer.isSold())
								totalOffersWithAccurateClassDataSoldAndNotReposted++;
						} else {
							totalOffersWithInaccurateClassData++;
							if (offer.soldOnce)
								totalOffersWithInaccurateClassDataSoldOnce++;
							if (offer.isSold())
								totalOffersWithInaccurateClassDataSoldAndNotReposted++;
						}
					}
				}
			}
		}
		
		System.out.println("Total Offers: " + totalOffers);
		System.out.println("Total (Real) Offers: " + totalRealOffers);
		System.out.println("Total Offers w Class Data: " + totalOffersWithClassData);
		System.out.println("Total Offers w Accurate Class Data: " + totalOffersWithAccurateClassData);
		System.out.println("Total Offers w Inaccurate Class Data: " + totalOffersWithInaccurateClassData);
		
		System.out.println("Total (Real) Offers Sold Once: " + totalOffersSoldOnce);
		System.out.println("Total (Real) Offers Sold And Not Reposted: " + totalOffersSoldAndNotReposted);
		System.out.println("Total Offers w Class Data Sold Once: " + totalOffersWithClassDataSoldOnce);
		System.out.println("Total Offers w Class Data Sold And Not Reposted: " + totalOffersWithClassDataSoldAndNotReposted);
		System.out.println("Total Offers w Accurate Class Data Sold Once: " + totalOffersWithAccurateClassDataSoldOnce);
		System.out.println("Total Offers w Accurate Class Data Sold And Not Reposted: " + totalOffersWithAccurateClassDataSoldAndNotReposted);
		System.out.println("Total Offers w Inaccurate Class Data Sold Once: " + totalOffersWithInaccurateClassDataSoldOnce);
		System.out.println("Total Offers w Inaccurate Class Data Sold And Not Reposted: " + totalOffersWithInaccurateClassDataSoldAndNotReposted);
	}
	
	private static void calculateConversionRates() {
		List<Stat> stats = StatManager.getManager().getStatsOfType(Stat.BUY_LINK_CLICK);
		
		int numBooksPickerClicks = 0; // booksSold(68)/this = conversionRate
		for (Stat stat : stats) {
			if (stat.store.equals(StoreName.LOCAL.getName())) {
				numBooksPickerClicks++;
			}
		}
		
		System.out.println("Total clicks on BP offers: " + numBooksPickerClicks);
	}
	
	public static void calculateAutoPriceStats() {
		List<LocalOffer> offers = LocalOfferManager.getManager().getAllLocalOffers();
		
		int totalOffers = offers.size();
		
		int totalRealOffers = 0;
		int totalOffersWithNoAutoPricing = 0;
		int totalOffersWithAutoPricingQuickie = 0;
		int totalOffersWithAutoPricingMoney = 0;
		List<Long> totalOffersTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithNoAutoPricingTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithAutoPricingQuickieTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithAutoPricingMoneyTimeOnMarket = new ArrayList<Long>();
//		int totalOffersPrice = 0;
//		int totalOffersWithNoAutoPricingTimePrice = 0;
//		int totalOffersWithAutoPricingQuickiePrice = 0;
//		int totalOffersWithAutoPricingMoneyPrice = 0;
		
		int totalOffersSoldOnce = 0; // "sold" (email sent at least once)
		int totalOffersWithNoAutoPricingSoldOnce = 0;
		int totalOffersWithAutoPricingQuickieSoldOnce = 0;
		int totalOffersWithAutoPricingMoneySoldOnce = 0;
		List<Long> totalOffersSoldOnceTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithNoAutoPricingSoldOnceTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithAutoPricingQuickieSoldOnceTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithAutoPricingMoneySoldOnceTimeOnMarket = new ArrayList<Long>();
		int totalOffersPriceSoldOnce = 0;
		int totalOffersWithNoAutoPricingPriceSoldOnce = 0;
		int totalOffersWithAutoPricingQuickiePriceSoldOnce = 0;
		int totalOffersWithAutoPricingMoneyPriceSoldOnce = 0;
		List<Book> booksSoldOnce = new ArrayList<Book>();
		List<Book> booksWithNoAutoPricingSoldOnce = new ArrayList<Book>();
		List<Book> booksWithAutoPricingQuickieSoldOnce = new ArrayList<Book>();
		List<Book> booksWithAutoPricingMoneySoldOnce = new ArrayList<Book>();
		
		int totalOffersSoldAndNotReposted = 0; // presumably really sold
		int totalOffersWithNoAutoPricingSoldAndNotReposted = 0;
		int totalOffersWithAutoPricingQuickieSoldAndNotReposted = 0;
		int totalOffersWithAutoPricingMoneySoldAndNotReposted = 0;
		List<Long> totalOffersSoldAndNotRepostedTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithNoAutoPricingSoldAndNotRepostedTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithAutoPricingQuickieSoldAndNotRepostedTimeOnMarket = new ArrayList<Long>();
		List<Long> totalOffersWithAutoPricingMoneySoldAndNotRepostedTimeOnMarket = new ArrayList<Long>();
		int totalOffersPriceSoldAndNotReposted = 0;
		int totalOffersWithNoAutoPricingPriceSoldAndNotReposted = 0;
		int totalOffersWithAutoPricingQuickiePriceSoldAndNotReposted = 0;
		int totalOffersWithAutoPricingMoneyPriceSoldAndNotReposted = 0;
		List<Book> booksSoldAndNotReposted = new ArrayList<Book>();
		List<Book> booksWithNoAutoPricingSoldAndNotReposted = new ArrayList<Book>();
		List<Book> booksWithAutoPricingQuickieSoldAndNotReposted = new ArrayList<Book>();
		List<Book> booksWithAutoPricingMoneySoldAndNotReposted = new ArrayList<Book>();
		
		Long totalTimeOnMarket;
		for (LocalOffer offer : offers) {
			totalTimeOnMarket = offer.timeOnMarketAggressive + 
				offer.timeOnMarketConservative +
				offer.timeOnMarketFixed;
			
			if (totalTimeOnMarket > 1000 * 60 * 60) { // > 1 hour
				totalRealOffers++;
				totalOffersTimeOnMarket.add(totalTimeOnMarket);
				if (offer.soldOnce) {
					totalOffersSoldOnce++;
					totalOffersSoldOnceTimeOnMarket.add(totalTimeOnMarket);
					totalOffersPriceSoldOnce += offer.getSellingPrice();
				}
				if (offer.isSold()) {
					totalOffersSoldAndNotReposted++;
					totalOffersSoldAndNotRepostedTimeOnMarket.add(totalTimeOnMarket);
					totalOffersPriceSoldAndNotReposted += offer.getSellingPrice();
					booksSoldAndNotReposted.add(offer.getBook());
				}
				
				if (!offer.isAutoPricing()) {
					totalOffersWithNoAutoPricing++;
//					totalOffersWithNoAutoPricingTimeOnMarket.add(totalTimeOnMarket);
					totalOffersWithNoAutoPricingTimeOnMarket.add(offer.timeOnMarketFixed);
					if (offer.soldOnce) {
						totalOffersWithNoAutoPricingSoldOnce++;
//						totalOffersWithNoAutoPricingSoldOnceTimeOnMarket.add(totalTimeOnMarket);
						totalOffersWithNoAutoPricingSoldOnceTimeOnMarket.add(offer.timeOnMarketFixed);
						totalOffersWithNoAutoPricingPriceSoldOnce += offer.getSellingPrice();
					}
					if (offer.isSold()) {
						totalOffersWithNoAutoPricingSoldAndNotReposted++;
//						totalOffersWithNoAutoPricingSoldAndNotRepostedTimeOnMarket.add(totalTimeOnMarket);
						totalOffersWithNoAutoPricingSoldAndNotRepostedTimeOnMarket.add(offer.timeOnMarketFixed);
						totalOffersWithNoAutoPricingPriceSoldAndNotReposted += offer.getSellingPrice();
						booksWithNoAutoPricingSoldAndNotReposted.add(offer.getBook());
					}
				} else {
					if (offer.getStrategy().equals(Strategy.AGGRESSIVE)) {
						totalOffersWithAutoPricingQuickie++;
//						totalOffersWithAutoPricingQuickieTimeOnMarket.add(totalTimeOnMarket);
						totalOffersWithAutoPricingQuickieTimeOnMarket.add(offer.timeOnMarketAggressive);
						if (offer.soldOnce) {
							totalOffersWithAutoPricingQuickieSoldOnce++;
//							totalOffersWithAutoPricingQuickieSoldOnceTimeOnMarket.add(totalTimeOnMarket);
							totalOffersWithAutoPricingQuickieSoldOnceTimeOnMarket.add(offer.timeOnMarketAggressive);
							totalOffersWithAutoPricingQuickiePriceSoldOnce += offer.getSellingPrice();
						}
						if (offer.isSold()) {
							totalOffersWithAutoPricingQuickieSoldAndNotReposted++;
//							totalOffersWithAutoPricingQuickieSoldAndNotRepostedTimeOnMarket.add(totalTimeOnMarket);
							totalOffersWithAutoPricingQuickieSoldAndNotRepostedTimeOnMarket.add(offer.timeOnMarketAggressive);
							totalOffersWithAutoPricingQuickiePriceSoldAndNotReposted += offer.getSellingPrice();
							booksWithAutoPricingQuickieSoldAndNotReposted.add(offer.getBook());
						}
					} else if (offer.getStrategy().equals(Strategy.CONSERVATIVE)) {
						totalOffersWithAutoPricingMoney++;
//						totalOffersWithAutoPricingMoneyTimeOnMarket.add(totalTimeOnMarket);
						totalOffersWithAutoPricingMoneyTimeOnMarket.add(offer.timeOnMarketConservative);
						if (offer.soldOnce) {
							totalOffersWithAutoPricingMoneySoldOnce++;
//							totalOffersWithAutoPricingMoneySoldOnceTimeOnMarket.add(totalTimeOnMarket);
							totalOffersWithAutoPricingMoneySoldOnceTimeOnMarket.add(offer.timeOnMarketConservative);
							totalOffersWithAutoPricingMoneyPriceSoldOnce += offer.getSellingPrice();
						}
						
						if (offer.isSold()) {
							totalOffersWithAutoPricingMoneySoldAndNotReposted++;
//							totalOffersWithAutoPricingMoneySoldAndNotRepostedTimeOnMarket.add(totalTimeOnMarket);
							totalOffersWithAutoPricingMoneySoldAndNotRepostedTimeOnMarket.add(offer.timeOnMarketConservative);
							totalOffersWithAutoPricingMoneyPriceSoldAndNotReposted += offer.getSellingPrice();
							booksWithAutoPricingMoneySoldAndNotReposted.add(offer.getBook());
						}
					}
				}
			}
		}
		
		System.out.println("Total Offers: " + totalOffers);
		
		System.out.println();
		System.out.println("Total (Real) Offers: " + totalRealOffers);
		System.out.println("Total Offers w/o auto pricing: " + totalOffersWithNoAutoPricing);
		System.out.println("Total Offers w auto pricing (quickie): " + totalOffersWithAutoPricingQuickie);
		System.out.println("Total Offers w auto pricing (money): " + totalOffersWithAutoPricingMoney);
		System.out.println("Total (Real) Offers time on market: " + totalOffersTimeOnMarket);
		System.out.println("Total Offers w/o auto pricing time on market: " + totalOffersWithNoAutoPricingTimeOnMarket);
		System.out.println("Total Offers w auto pricing (quickie) time on market: " + totalOffersWithAutoPricingQuickieTimeOnMarket);
		System.out.println("Total Offers w auto pricing (money) time on market: " + totalOffersWithAutoPricingMoneyTimeOnMarket);
		
		System.out.println();
		System.out.println("Total (Real) Offers Sold Once: " + totalOffersSoldOnce);
		System.out.println("Total Offers w/o auto pricing Sold Once: " + totalOffersWithNoAutoPricingSoldOnce);
		System.out.println("Total Offers w auto pricing (quickie) Sold Once: " + totalOffersWithAutoPricingQuickieSoldOnce);
		System.out.println("Total Offers w auto pricing (money) Sold Once: " + totalOffersWithAutoPricingMoneySoldOnce);
		System.out.println("Total (Real) Offers time on market Sold Once: " + totalOffersSoldOnceTimeOnMarket);
		System.out.println("Total Offers w/o auto pricing time on market Sold Once: " + totalOffersWithNoAutoPricingSoldOnceTimeOnMarket);
		System.out.println("Total Offers w auto pricing (quickie) time on market Sold Once: " + totalOffersWithAutoPricingQuickieSoldOnceTimeOnMarket);
		System.out.println("Total Offers w auto pricing (money) time on market Sold Once: " + totalOffersWithAutoPricingMoneySoldOnceTimeOnMarket);
		System.out.println("Total (Real) Offers price Sold Once: " + totalOffersPriceSoldOnce);
		System.out.println("Total Offers w/o auto pricing price Sold Once: " + totalOffersWithNoAutoPricingPriceSoldOnce);
		System.out.println("Total Offers w auto pricing (quickie) price Sold Once: " + totalOffersWithAutoPricingQuickiePriceSoldOnce);
		System.out.println("Total Offers w auto pricing (money) price Sold Once: " + totalOffersWithAutoPricingMoneyPriceSoldOnce);
		
		System.out.println();
		System.out.println("Total (Real) Offers Sold And Not Reposted: " + totalOffersSoldAndNotReposted);
		System.out.println("Total Offers w/o auto pricing Sold And Not Reposted: " + totalOffersWithNoAutoPricingSoldAndNotReposted);
		System.out.println("Total Offers w auto pricing (quickie) Sold And Not Reposted: " + totalOffersWithAutoPricingQuickieSoldAndNotReposted);
		System.out.println("Total Offers w auto pricing (money) Sold And Not Reposted: " + totalOffersWithAutoPricingMoneySoldAndNotReposted);
		System.out.println("Total (Real) Offers time on market Sold And Not Reposted: " + totalOffersSoldAndNotRepostedTimeOnMarket);
		System.out.println("Total Offers w/o auto pricing time on market Sold And Not Reposted: " + totalOffersWithNoAutoPricingSoldAndNotRepostedTimeOnMarket);
		System.out.println("Total Offers w auto pricing (quickie) time on market Sold And Not Reposted: " + totalOffersWithAutoPricingQuickieSoldAndNotRepostedTimeOnMarket);
		System.out.println("Total Offers w auto pricing (money) time on market Sold And Not Reposted: " + totalOffersWithAutoPricingMoneySoldAndNotRepostedTimeOnMarket);
		System.out.println("Total (Real) Offers price Sold And Not Reposted: " + totalOffersPriceSoldAndNotReposted);
		System.out.println("Total Offers w/o auto pricing price Sold And Not Reposted: " + totalOffersWithNoAutoPricingPriceSoldAndNotReposted);
		System.out.println("Total Offers w auto pricing (quickie) price Sold And Not Reposted: " + totalOffersWithAutoPricingQuickiePriceSoldAndNotReposted);
		System.out.println("Total Offers w auto pricing (money) price Sold And Not Reposted: " + totalOffersWithAutoPricingMoneyPriceSoldAndNotReposted);
		
		System.out.println();
		System.out.println("Distribution of time on market for offers without auto pricing:\n" + commaSeparated(totalOffersWithNoAutoPricingSoldAndNotRepostedTimeOnMarket));
		System.out.println("Distribution of time on market for offers with auto pricing (quickie):\n" + commaSeparated(totalOffersWithAutoPricingQuickieSoldAndNotRepostedTimeOnMarket));
		
		List<Stat> stats = StatManager.getManager().getStatsOfType(Stat.BUNDLE_SEARCH);
		for (Stat stat : stats) {
			
		}
		
        String[] dummy;
        int numTimesBooksWithNoAutoPricingSoldAndNotRepostedSearched = 0;
        int numTimesBooksWithAutoPricingQuickieSoldAndNotRepostedSearched = 0;
		List<Stat> bundleStats = StatManager.getManager().getStatsOfType(Stat.BUNDLE_SEARCH);
		for (Stat stat : bundleStats) {
			dummy = stat.search.split(",");
			for (String str : dummy) {
				for (Book b : booksWithNoAutoPricingSoldAndNotReposted) {
					if (b.getIsbn().equals(str))
						numTimesBooksWithNoAutoPricingSoldAndNotRepostedSearched++;
				}
				for (Book b : booksWithAutoPricingQuickieSoldAndNotReposted) {
					if (b.getIsbn().equals(str))
						numTimesBooksWithAutoPricingQuickieSoldAndNotRepostedSearched++;
				}
			}
		}
		
		System.out.println();
		System.out.println("Books that sold with fixed price: (" + numTimesBooksWithNoAutoPricingSoldAndNotRepostedSearched + " searches)");
		for (Book b : booksWithNoAutoPricingSoldAndNotReposted) {
			System.out.println(b.getTitle());
		}
		
		System.out.println();
		System.out.println("Books that sold with quickie strategey: (" + numTimesBooksWithAutoPricingQuickieSoldAndNotRepostedSearched + " searches)");
		for (Book b : booksWithAutoPricingQuickieSoldAndNotReposted) {
			System.out.println(b.getTitle());
		}
	}
	
	private static String commaSeparated(List<? extends Object> list) {
		StringBuilder sb = new StringBuilder();
		for (Object o : list) {
			if (list.get(0) != o)
				sb = sb.append(",");
			sb = sb.append(o.toString());
		}
		return sb.toString();
	}

}
