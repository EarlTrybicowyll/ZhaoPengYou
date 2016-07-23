/**
 * Class kept up-to-date by the game engine with public information available to the players.  Container
 * class only, so a new instance is created each time new information is needed by players. 
 * @author bryce
 *
 */
public class GameInfo {
	private final int numPlayers;
	private final int numDecks;
	// The player id of the host
	private final Integer host;
	private final Rank trumpRank;
	private final Suit trumpSuit;
	private final PartnerCall partnerCall;
	private final int round;
	
	public GameInfo(int numPlayers, int numDecks, Integer host, Rank trumpRank, Suit trumpSuit, PartnerCall partnerCall, int round) {
		this.numPlayers = numPlayers;
		this.numDecks = numDecks;
		this.host = host;
		this.trumpRank = trumpRank;
		this.trumpSuit = trumpSuit;
		this.partnerCall = partnerCall;
		this.round = round;
	}

	public int getNumPlayers() {
		return numPlayers;
	}

	public int getNumDecks() {
		return numDecks;
	}

	public Integer getHost() {
		return host;
	}

	public Suit getTrumpSuit() {
		return trumpSuit;
	}

	public Rank getTrumpRank() {
		return trumpRank;
	}

	public PartnerCall getPartnerCall() {
		return partnerCall;
	}

	public int getRound() {
		return round;
	}
}
