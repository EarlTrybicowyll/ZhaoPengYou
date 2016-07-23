
public class Card implements Comparable<Card> {

	// The actual id of this card, a number from 0 to 53
	private int id;
	// The suit, as written on the physical card
	private final Suit baseSuit;
	// The rank, as written on the physical card
	private final Rank baseRank;
	// The suit, for the purposes of playing (includes trump)
	private Suit suit;
	// The rank, for the purposes of playing (reorders to account for trump)
	private Rank rank;

	public Card(int id) {
		this.id = id;
		if (id < 52) {
			this.baseSuit = Suit.values()[id / 13];
			this.baseRank = Rank.values()[id % 13];
		} else {
			this.baseRank = Rank.values()[Rank.values().length + id - 54];
			this.baseSuit = Suit.TRUMP;
		}
	}
	
	/**
	 * Used for testing; does not check the card is a proper card.
	 * @param suit
	 * @param rank
	 */
	public Card(Suit suit, Rank rank) {
		// check for jokers everything else is simple
		if (rank.ordinal() >= Rank.JOKER.ordinal()) {
			this.baseSuit = Suit.TRUMP;
			this.baseRank = rank;
			this.id = 52 + (rank == Rank.COLOR_JOKER ? 1 : 0);
		} else {
			this.baseSuit = suit;
			this.baseRank = rank;
			this.id = 13 * suit.ordinal() + rank.ordinal();
		}
	}
	
	private Card(Card card) {
		this.id = card.id;
		this.baseSuit = card.getBaseSuit();
		this.suit = card.getSuit();
		this.baseRank = card.getBaseRank();
		this.rank = card.getRank();
	}
	
	/**
	 * Updates the practical suit and rank of this physical card, assuming the given suit and rank
	 * are trump.  Does not necessarily declare this particular card to be trump (if it is not trump,
	 * its suit and rank will match its base suit and rank)
	 * 
	 * @param trumpSuit Null if no trump
	 * @param trumpRank 2 through A only
	 */
	public void setTrump(Suit trumpSuit, Rank trumpRank) {
		if (id >= 52) {
			suit = baseSuit;
			rank = baseRank;
			return;
		}
		
		if (baseSuit == trumpSuit) {
			suit = Suit.TRUMP;
		} else {
			suit = baseSuit;
		}
		if (baseRank == trumpRank) {
			suit = Suit.TRUMP;
			rank = (baseSuit == trumpSuit) ? Rank.SUITED_NUMBER : Rank.NUMBER;
		} else {
			rank = baseRank;
		}
	}
	
	@Override
	public String toString() {
		if (id < 52) return baseRank + " of " + baseSuit;
		if (id == 52) return "JOKER";
		return "COLORED JOKER";
	}
	
	public Suit getBaseSuit() {
		return baseSuit;
	}
	
	public Rank getBaseRank() {
		return baseRank;
	}
	
	public Suit getSuit() {
		return suit;
	}
	
	public Rank getRank() {
		return rank;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isJoker() {
		return id > 51;
	}
	
	public boolean isTrump() {
		return this.getSuit() == Suit.TRUMP;
	}
	
	public int getPointValue() {
		if (baseRank == Rank.FIVE) return 5;
		if (baseRank == Rank.TEN || baseRank == Rank.KING) return 10;
		return 0;
	}

	/**
	 * Compares this card, based on suit and rank (not base suit and base rank).
	 * Gives an absolute ordering to the cards, but does not necessarily give 
	 * the card that would win a trick when played.  Used for displaying cards.
	 */
	@Override
	public int compareTo(Card o) {
		if (getSuit() == Suit.TRUMP) {
			if (o.getSuit() == Suit.TRUMP) {
				int dif = getRank().ordinal() - o.getRank().ordinal();
				if (dif != 0) return dif;
				// Differentiate between offsuit trumps
				return getId() - o.getId();
			} else {
				return 1;
			}
		} else {
			if (o.getSuit() == Suit.TRUMP) {
				return -1;
			} else {
				return getId() - o.getId();
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Card)) return false;
		Card other = (Card) o;
		return getId() == other.getId();
	}
	
	/**
	 * Returns a card identical to this one, but in a separate object
	 * @return
	 */
	public Card clone() {
		return new Card(this);
	}
	
	/**
	 * Returns whether this represents an actual card (used to prevent players from declaring non-existant cards)
	 * @return
	 */
	public boolean isValid() {
		return 0 <= id && id < 54;
	}
}
