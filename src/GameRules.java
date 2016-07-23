import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A set of universal methods that determine whether or not plays are valid.  Used by the engine to verify 
 * rules are followed and can also be used by Players if desired.
 * @author bryce
 *
 */
public class GameRules {
	
	public static int getNumPoints(List<Card> cards) {
		int numPoints = 0;
		for (Card c : cards) {
			numPoints += c.getPointValue();
		}
		return numPoints;
	}
	
	public static int getNumPointsPlay(List<List<Card>> plays) {
		int numPoints = 0;
		for (List<Card> cards : plays) {
			numPoints += getNumPoints(cards);
		}
		return numPoints;
	}
	
	/**
	 * Returns the index of the winning play from the given list of plays.  Assumes that all plays are valid.
	 * @param plays
	 * @return
	 */
	public static int getWinningIndex(List<List<Card>> plays) {
		int winningIndex = 0;
		List<Card> winningPlay = plays.get(0);
		for (int i = 1; i < plays.size(); i++) {
			if (getHigherHand(winningPlay, plays.get(i)) == 1) {
				winningIndex = i;
				winningPlay = plays.get(i);
			}
		}
		
		return winningIndex;
	}
	
	/**
	 * Determines which hand is higher, assuming that handZero correctly followed the 
	 * rules of play and played first (i.e. is the default winner).
	 * 
	 * @param handZero
	 * @param handOne
	 * @return 0 if handZero was higher, 1 if handOne was higher
	 */
	public static int getHigherHand(List<Card> handZero, List<Card> handOne) {
		Card cardZero = handZero.get(0);
		Suit suitZero = cardZero.getSuit();
		if (!cardsSameSuit(handOne)) return 0;
		Card cardOne = handOne.get(0);
		Suit suitOne = cardOne.getSuit();
		if (handZero.size() != 1) {
			int straightZeroLen = isConsecutiveRanks(handZero);
			int straightOneLen = isConsecutiveRanks(handOne);
			int straightOneMultiplicity = allCardsSameCount(handOne);
			int straightZeroMultiplicity = allCardsSameCount(handZero);
			if (straightOneMultiplicity != straightZeroMultiplicity) {
				return 0;
			}
			if (straightZeroLen >= straightOneLen) return 0;
		}
		
		if (suitZero == Suit.TRUMP) {
			if (suitOne != Suit.TRUMP) return 0;
			return cardZero.getRank().ordinal() >= cardOne.getRank().ordinal() ? 0 : 1;
		} else {
			if (suitOne == Suit.TRUMP) return 1;
			if (suitOne != suitZero) return 0;
			return cardZero.getRank().ordinal() >= cardOne.getRank().ordinal() ? 0 : 1;
		}
		
	}
	
	public static boolean isValidLead(List<Card> hand, Set<Integer> leadIndices) {
		List<Card> lead = GameAIUtils.indicesToCards(hand, leadIndices);
		return isValidLead(lead);
	}

	/**
	 * Returns whether the given list of cards constitutes a valid lead.
	 * @param lead
	 * @return
	 */
	public static boolean isValidLead(List<Card> lead) {
		if (lead.size() == 0) return false;
		if (!cardsSameSuit(lead)) return false;
		
		// Should ensure that the cards have the same count
		int cardMultiplicity = allCardsSameCount(lead);
		if (cardMultiplicity == 0) return false;
		// Straights have more than 1 card each
		if (cardMultiplicity == 1 && lead.size() > 1) return false;
		
		if (isConsecutiveRanks(lead) == 0) return false;
		
		return true;
	}
	
	public static boolean isValidPlay(List<Card> lead, Set<Integer> playIndices, List<Card> playerHand) {
		List<Card> play = GameAIUtils.indicesToCards(playerHand, playIndices);
		return isValidPlay(lead, play, playerHand);
	}
	
	/**
	 * Returns whether the play is a valid response to the lead, given the contents
	 * of the players hand.  Assumes that lead is a valid lead, without checking.
	 * 
	 * @param lead The cards originally lead for this trick
	 * @param play The play whose validity is to be assessed
	 * @param playerHand The hand of the player attempting to play play (play should be
	 * 					 a subset of playerHand)
	 * @return
	 */
	public static boolean isValidPlay(List<Card> lead, List<Card> play, List<Card> playerHand) {
		if (lead.size() != play.size()) return false;
		
		// Check suit requirement
		Suit suit = lead.get(0).getSuit();
		// count cards matching in play
		int countPlay = 0;
		for (Card c : play) {
			if (c.getSuit() == suit) countPlay++;
		}
		
		int countHand = 0;
		for (Card c : playerHand) {
			if (c.getSuit() == suit) countHand++;
		}
		
		// False if didn't play all cards of suit when possible
		if (countPlay < lead.size() && countPlay < countHand) return false;
		
		// If no cards match suit, any play works; used so that we can assume
		// at least one playable card for remaining tests
		if (countHand == 0) return true;
		
		int leadStraightLen = isConsecutiveRanks(lead);
		if (leadStraightLen == 0) return true;
		int leadMultiplicity = lead.size() / leadStraightLen;				// must be integer assuming valid lead
		
		if (leadMultiplicity == 1) return true;
		
		// Ensure as many groups of this consistency are played as mandated
		List<Set<Integer>> playerStraights = partitionCardsToStraights(playerHand, suit, leadMultiplicity);
		int playerMultiplicityCount = 0;
		for (Set<Integer> straight : playerStraights) {
			playerMultiplicityCount += straight.size() / leadMultiplicity;
		}
		
		List<Set<Integer>> playedStraights = partitionCardsToStraights(play, suit, leadMultiplicity);
		int playedMultiplicityCount = 0;
		for (Set<Integer> straight : playedStraights) {
			playedMultiplicityCount += straight.size() / leadMultiplicity;
		}
		
		if (playedMultiplicityCount == leadStraightLen) return true;
		return playerMultiplicityCount == playedMultiplicityCount;
	}
	
	/**
	 * Partitions the cards of the given suit in the given hand into straights of the given 
	 * multiplicity.  
	 * 
	 * @param hand
	 * @param multiplicity The multiplicity of the straight, > 1
	 * @return
	 */
	public static List<Set<Integer>> partitionCardsToStraights(List<Card> cards, Suit suit, int multiplicity) {
		List<Card> hand = new ArrayList<>(cards);
		Collections.sort(hand);
		// Ensure there's at least one matching suit to avoid edge cases later
		if (!hand.stream().anyMatch(c -> c.getSuit() == suit)) return new ArrayList<>();
		
		List<List<Card>> straights = new ArrayList<>();
		
		while (hasSuitMultiplicity(hand, multiplicity, suit)) {
			List<Card> curStraight = new ArrayList<>();
			
			int searchingRank = 0;
			while (searchingRank < Rank.values().length) {
				Rank rank = Rank.values()[searchingRank];
				Set<Card> multiples = suitRankMultiplicity(hand, suit, rank, multiplicity);
				if (multiples.isEmpty()) {
					if (!curStraight.isEmpty()) {
						straights.add(curStraight);
						curStraight = new ArrayList<>();
					}
					searchingRank++;
					continue;
				}
				curStraight.addAll(removeCards(hand, getFirst(multiples), multiplicity));
				searchingRank++;
			}
		}
		
		List<Set<Integer>> straightIndices = new ArrayList<>();
		for (List<Card> straight : straights) {
			straightIndices.add(cardsToIndices(cards, straight));
		}
		
		return straightIndices;
	}
	
	/**
	 * Returns a unique index for each card in toFind such that the card at that index in hand
	 * matches the card in toFind.  Returns null if any card cannot be found.
	 * 
	 * @param hand
	 * @param toFind
	 * @return
	 */
	public static Set<Integer> cardsToIndices(List<Card> hand, List<Card> toFind) {
		Set<Integer> indices = new HashSet<>();
		for (Card c : toFind) {
			for (int i = 0; i < hand.size(); i++) {
				if (hand.get(i).equals(c) && !indices.contains(i)) {
					indices.add(i);
					break;
				}
			}
		}
		
		return indices;
	}
	
	public static boolean hasSuitMultiplicity(List<Card> cards, int multiplicity, Suit suit) {
		int[] histogram = makeCardHistogram(cards);
		return cards.stream().anyMatch(c -> histogram[c.getId()] >= multiplicity && c.getSuit() == suit);
	}
	
	public static List<Card> removeCards(List<Card> hand, Card card, int numRemove) {
		List<Card> removed = new ArrayList<>();
		for (int i = 0; i < numRemove; i++) {
			hand.remove(card);
			removed.add(card);
		}
		
		return removed;
	}
	
	/**
	 * Returns all cards matching the given suit and rank with at least the given multiplicity.  Returns an empty
	 * set if no cards match.  May return multiple distinct cards (e.g. offsuit trump).
	 * @param cards
	 * @param suit
	 * @param rank
	 * @param multiplicity
	 * @return
	 */
	public static Set<Card> suitRankMultiplicity(List<Card> cards, Suit suit, Rank rank, int multiplicity) {
		int[] histogram = makeCardHistogram(cards);
		Set<Card> matches = cards.stream().filter(c -> histogram[c.getId()] >= multiplicity && c.getSuit() == suit && c.getRank() == rank).collect(Collectors.toSet());	
		return matches;
	}
	
	/**
	 * Returns the unique count of all cards represented by the histogram, or 0
	 * if there exist multiple counts of cards
	 * @param cardHistogram A histogram representing at least 1 card
	 * @return
	 */
	public static int allCardsSameCount(List<Card> cards) {
		int[] cardHistogram = makeCardHistogram(cards);
		int firstIndex = 0;
		while (cardHistogram[firstIndex] == 0) firstIndex++;
		// Ensure all cards with ids between the two indices have the same count
		for (int i = firstIndex + 1; i < cardHistogram.length; i++) {
			if (cardHistogram[i] != cardHistogram[firstIndex] &&
				cardHistogram[i] != 0)  {
				return 0;
			}
		}
		
		return cardHistogram[firstIndex];
	}
	
	/**
	 * If the cards form a straight according to their ranks (ignoring multiplicity of
	 * cards), returns the length of that straight.  Otherwise, returns 0.
	 * 
	 * @param cardHistogram
	 * @return
	 */
	public static int isConsecutiveRanks(List<Card> cards) {
		Set<Integer> rankOrdinals = new HashSet<>();
		int minOrdinal = Rank.values().length;
		int maxOrdinal = -1;
		
		for (Card c : cards) {
			int ordinal = c.getRank().ordinal();
			rankOrdinals.add(ordinal);
			if (minOrdinal > ordinal) minOrdinal = ordinal;
			if (maxOrdinal < ordinal) maxOrdinal = ordinal;
		}
		
		// Ensure every rank between min and max is included
		for (int i = minOrdinal + 1; i < maxOrdinal; i++) {
			if (!rankOrdinals.contains(i)) return 0;
		}
		
		return maxOrdinal - minOrdinal + 1;
	}
	
	/**
	 * Returns whether the given list of cards are all of the same suit.
	 * @param cards
	 * @return
	 */
	public static boolean cardsSameSuit(List<Card> cards) {
		Suit suit = cards.get(0).getSuit();
		return cards.stream().allMatch(c -> c.getSuit() == suit);
	}
	
	/**
	 * Makes a histogram of the given cards, indexed by card id.
	 * @param cards
	 * @return
	 */
	public static int[] makeCardHistogram(List<Card> cards) {
		int[] histogram = new int[54];
		for (Card c : cards) {
			histogram[c.getId()]++;
		}
		
		return histogram;
	}
	
	public static <T> T getFirst(Set<T> set) {
		return set.iterator().next();
	}
}
