import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class of utility functions that are helpful for players (AIs in particular) to use to ensure they follow suit, etc.
 * @author bryce
 *
 */
public class GameAIUtils {

	/**
	 * Gets all cards of the given suit whose multiplicity is at least multiplicity and returns them, ordered by
	 * rank in sets containing all the same card.  Used to determine forced plays based on lead.
	 * 
	 * @param cards
	 * @param suit
	 * @param multiplicity
	 * @return
	 */
	public static List<Set<Integer>> getCardsMultiplicity(List<Card> cards, Suit suit, int multiplicity) {
		int[] histogram = GameRules.makeCardHistogram(cards);
		List<Card> hand = new ArrayList<>(cards);
		Collections.sort(hand);
		List<Set<Integer>> sortedCards = new ArrayList<>();
		for (Card c : hand) {
			if (histogram[c.getId()] >= multiplicity && c.getSuit() == suit) {
				sortedCards.add(getMatchingCards(cards, c));
				histogram[c.getId()] = 0;
			}
		}
		return sortedCards;
	}
	
	/**
	 * Returns a set of the indices of the cards matching the given card exactly.
	 * @param cards
	 * @param c
	 * @return
	 */
	public static Set<Integer> getMatchingCards(List<Card> cards, Card toMatch) {
		Set<Integer> matches = new HashSet<>();
		for (int i = 0; i < cards.size(); i++) {
			Card c = cards.get(i);
			if (toMatch.equals(c)) {
				matches.add(i);
			}
		}
		return matches;
	}
	
	/**
	 * Gets the indices of all cards matching the given suit in the given set of cards.
	 * Cards are given in the same order as the hand.
	 * @param cards
	 * @param suit
	 * @return
	 */
	public static List<Integer> getSameSuit(List<Card> cards, Suit suit) {
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).getSuit() == suit) {
				indices.add(i);
			}
		}
		return indices;
	}
	
	/**
	 * Returns the cards at the given indices in the given hand.
	 * @param hand
	 * @param indices
	 * @return
	 */
	public static List<Card> indicesToCards(List<Card> hand, Set<Integer> indices) {
		List<Card> cards = new ArrayList<>();
		for (Integer i : indices) {
			cards.add(hand.get(i));
		}
		Collections.sort(cards);
		return cards;
	}
	
	/**
	 * Returns the number of cards matching the given suit
	 * @param cards
	 * @param suit
	 * @return
	 */
	public static long countSuit(List<Card> cards, Suit suit) {
		return cards.stream().filter(c -> c.getSuit() == suit).count();
	}
	
	/**
	 * Returns a map from suit to the indices of the cards matching those suits
	 * @param cards
	 * @return
	 */
	public static Map<Suit, List<Integer>> sortCardsBySuit(List<Card> cards) {
		Map<Suit, List<Integer>> sortedCards = new HashMap<>();
		for (Suit suit : Suit.values()) {
			sortedCards.put(suit, new ArrayList<>());
		}
		
		for (int i = 0; i < cards.size(); i++) {
			Card c = cards.get(i);
			sortedCards.get(c.getSuit()).add(i);
		}
		
		return sortedCards;
	}
	
	/**
	 * Returns the shortest non-trump, non-void suit from the already sorted suits.  SortedCards
	 * must have a (possibly empty) entry for each suit for this method to work.  Returns trump suit
	 * if void in all offsuits.
	 * @param sortedCards
	 * @return
	 */
	public static Suit getShortestOffSuit(Map<Suit, List<Integer>> sortedCards) {
		Suit shortestSuit = null;
		int shortestLen = 999999;
		for (Suit suit : sortedCards.keySet()) {
			if (suit == Suit.TRUMP) continue;
			int suitSize = sortedCards.get(suit).size();
			if (suitSize < shortestLen) {
				shortestLen = suitSize;
				shortestSuit = suit;
			}
		}
		if (shortestSuit == null) shortestSuit = Suit.TRUMP;
		return shortestSuit;
	}
}
