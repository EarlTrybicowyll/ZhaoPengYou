import java.util.List;
import java.util.Set;

/**
 * Interface to represent a generic player; could be human or computer
 * @author bryce
 *
 */
public abstract class Player {
	
	private int id;
	
	public Player(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	 * Called at the start of a round to provide information specific to this round.
	 * @param trumpRank
	 * @param position
	 */
	abstract void initializeRound(GameInfo gameInfo);
	
	/**
	 * Called each time a card is added to this players hand when cards are drawn.
	 * 
	 * @param newCard A copy of the newly drawn card
	 * @param hand An immutable view of the entire hand drawn so far (for convenience)
	 * @param curTrumpSuit The currently called trump suit
	 * @return The indices of the cards the player would like to reveal to declare trump,
	 *         if any.  null if nothing should be declared
	 */
	abstract Set<Integer> draw(Card newCard, List<Card> hand, GameInfo gameInfo);
	
	/**
	 * Called if player is host; given the cards in his hand as well as the kitty, the
	 * player must return a set of the indices of the cards to put back into the kitty.
	 * 
	 * @param handPlusKitty An immutable view of the list of cards in the players hand 
	 * 						including the kitty
	 * @param kittySize The number of cards in the kitty
	 * @param gameInfo
	 * @return
	 */
	abstract Set<Integer> handleKitty(List<Card> handPlusKitty, int kittySize, GameInfo gameInfo);
	
	/**
	 * Called after the host player handles the kitty, when he must declare who is to be the partner.
	 * 
	 * @param hand The hand the player will use to play
	 * @param kitty The kitty as was previously chosen by the player
	 * @param gameInfo
	 * @return
	 */
	abstract PartnerCall callPartner(List<Card> hand, List<Card> kitty, GameInfo gameInfo);
	
	/**
	 * Called when the player should lead for a trick.
	 * 
	 * @param hand A copy of the hand from which the player should play
	 * @param gameInfo
	 * @return A set containing the indices of the cards in the hand to play
	 */
	abstract Set<Integer> lead(List<Card> hand, GameInfo gameInfo);
	
	/**
	 * Called when a player should play in a trick, in response to previous plays.
	 * 
	 * @param previousPlays A list of previous plays, in the order they occurred.  Each play is given by a list 
	 * 						of cards
	 * @param hand			A copy of the hand from which the player should play
	 * @param gameInfo
	 * @return A set containing the indices of the cards in the hand to play
	 */
	abstract Set<Integer> play(List<List<Card>> previousPlays, List<Card> hand, GameInfo gameInfo);

}
