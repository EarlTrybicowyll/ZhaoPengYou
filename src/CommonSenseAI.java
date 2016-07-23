import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonSenseAI extends Player {

	public CommonSenseAI(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	void initializeRound(GameInfo gameInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	Set<Integer> draw(Card newCard, List<Card> hand, GameInfo gameInfo) {
		if (gameInfo.getRound() == 0) {
			if (gameInfo.getTrumpSuit() == null && newCard.getBaseRank() == gameInfo.getTrumpRank()) {
				// no one else called yet
				Set<Integer> trump = new HashSet<>();
				trump.add(hand.indexOf(newCard));
				return trump;
			}
		}
		return null;
	}

	@Override
	Set<Integer> handleKitty(List<Card> handPlusKitty, int kittySize, GameInfo gameInfo) {
		// Void the shortest suits
		Map<Suit, List<Integer>> sortedCards = GameAIUtils.sortCardsBySuit(handPlusKitty);
		Set<Integer> returnIndices = new HashSet<Integer>();
		while (returnIndices.size() < kittySize) {
			int numNeeded = kittySize - returnIndices.size();
			Suit shortestSuit = GameAIUtils.getShortestOffSuit(sortedCards);
			List<Integer> shortestCards = sortedCards.get(shortestSuit);
			for (int i = 0; i < Math.min(shortestCards.size(), numNeeded); i++) {
				returnIndices.add(shortestCards.get(i));
			}
			sortedCards.remove(shortestSuit);
		}
		
		return returnIndices;
	}

	@Override
	PartnerCall callPartner(List<Card> hand, List<Card> kitty, GameInfo gameInfo) {
		// TODO Auto-generated method stub
		int id = 0;
		Card c;
		do {
			c = new Card(id);
			id++;
			c.setTrump(gameInfo.getTrumpSuit(), gameInfo.getTrumpRank());
		} while (c.isTrump() || hand.contains(c));
		
		return new PartnerCall(c, 1);
	}

	@Override
	Set<Integer> lead(List<Card> hand, GameInfo gameInfo) {
		// TODO Auto-generated method stub
		Set<Integer> lead = new HashSet<>();
		// Attempt to play any tractors first, in decreasing order of size
		for (int multiplicity = gameInfo.getNumDecks(); multiplicity>= 2; multiplicity--) {
			int maxStraightLen = 0;
			Set<Integer> maxIndices = null;
			for (Suit suit : Suit.values()) {
				List<Set<Integer>> partition = GameRules.partitionCardsToStraights(hand, suit, multiplicity);
				for (Set<Integer> straight : partition) {
					if (straight.size() > maxStraightLen) {
						maxStraightLen = straight.size();
						maxIndices = straight;
					}
				}
			}
			
			if (maxIndices != null) {
				return maxIndices;
			}
		}
		
		// Default to best card TODO
		lead.add(hand.size() - 1);
		return lead;
	}

	@Override
	Set<Integer> play(List<List<Card>> previousPlays, List<Card> hand, GameInfo gameInfo) {
		// TODO Auto-generated method stub
		List<Card> lead = previousPlays.get(0);
		Suit suit = lead.get(0).getSuit();
		Set<Integer> play = new HashSet<>();
		int pointsSoFar = GameRules.getNumPointsPlay(previousPlays);
		if (lead.size() == 1) {
			// special single card-case
			// check for matching suit; if not fill hand with garbage
			List<Integer> sameSuit = GameAIUtils.getSameSuit(hand, suit);
			if (!sameSuit.isEmpty()) {
				play.add(sameSuit.get(sameSuit.size() - 1));
			} else {
				if (pointsSoFar > 0) {
					// Attempt to trump points
					fillPlay(play, hand, lead.size(), Suit.TRUMP);
				}
				fillPlay(play, hand, lead.size(), null);
			}
			return play;
		}
		
		int straightLen = GameRules.isConsecutiveRanks(lead);
		int leadMultiplicity = lead.size() / straightLen;
		
		// Match multiples
		List<Set<Integer>> matches = GameAIUtils.getCardsMultiplicity(hand, suit, leadMultiplicity);
		for (int i = 0; i < Math.min(matches.size(), straightLen); i++) {
			play.addAll(matches.get(i));
		}
		
		// Match singles of same suit
		fillPlay(play, hand, lead.size(), suit);
		// fill in with "lowest" cards in hand
		fillPlay(play, hand, lead.size(), null);
		
		return play;
	}
	
	/**
	 * Helper method that fills in the set play with arbitrary new cards from hand until
	 * the size of play is targetSize. If suit is not null, added cards must have suit suit,
	 * though the final size of play is not guaranteed to be targetSize in this case;
	 * @param play A set that is of size at most targetSize
	 * @param hand
	 * @param targetSize between 0 and hand.size()
	 */
	private void fillPlay(Set<Integer> play, List<Card> hand, int targetSize, Suit suit) {
		if (play.size() == targetSize) return;
		for (int i = 0; i < hand.size(); i++) {
			if (play.contains(i)) continue;
			if (suit != null && hand.get(i).getSuit() != suit) continue;
			play.add(i);
			if (play.size() == targetSize) return;
		}
	}

}
