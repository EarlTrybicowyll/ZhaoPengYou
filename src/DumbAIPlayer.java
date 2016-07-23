import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The simplest functional AI player, mostly for testing
 * @author bryce
 *
 */
public class DumbAIPlayer extends Player {

	public DumbAIPlayer(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initializeRound(GameInfo gameInfo) {
		// TODO Auto-generated method stub
		// We are dumb, so don't do anything
	}

	@Override
	public Set<Integer> draw(Card newCard, List<Card> hand, GameInfo gameInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Set<Integer> handleKitty(List<Card> handPlusKitty, int kittySize, GameInfo gameInfo) {
		// TODO Auto-generated method stub
		Set<Integer> kitty = new HashSet<>();
		for (int i = 0; i < kittySize; i++) {
			kitty.add(i);
		}
		return kitty;
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
		lead.add(hand.size() - 1);
		
		return lead;
	}

	@Override
	Set<Integer> play(List<List<Card>> previousPlays, List<Card> hand, GameInfo gameInfo) {
		List<Card> lead = previousPlays.get(0);
		Suit suit = lead.get(0).getSuit();
		Set<Integer> play = new HashSet<>();
		if (lead.size() == 1) {
			// special single card-case
			// check for matching suit; if not fill hand with garbage
			List<Integer> sameSuit = GameAIUtils.getSameSuit(hand, suit);
			if (!sameSuit.isEmpty()) {
				play.add(sameSuit.get(sameSuit.size() - 1));
			} else {
				fillPlay(play, hand, lead.size(), null);
			}
			return play;
		}
		
		int straightLen = GameRules.isConsecutiveRanks(lead);
		int leadMultiplicity = lead.size() / straightLen;
		
		// Match multiples
		List<Set<Integer>> matches = GameAIUtils.getCardsMultiplicity(hand, suit, leadMultiplicity);
		for (int i = 0; i < Math.min(matches.size(), straightLen); i++) {
			// Add multiplicity of them
			Iterator<Integer> iter = matches.get(i).iterator();
			for (int j = 0; j < leadMultiplicity; j++) {
				play.add(iter.next());
			}
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
