import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Game engine to control interactions between players and coordinate the game.
 * @author bryce
 *
 */
public class GameEngine {
	
	private final int numPlayers;
	private final int numDecks;
	private final int numCardsPerPlayer;
	// All the cards used in this game (54 * numDecks cards)
	private List<Card> cards;
	private List<Player> players;
	private List<List<Card>> playerHands;
	private List<Card> kitty;
	
	private int round;
	private Player host;
	// The trump for the current round
	private Suit trumpSuit;
	private Rank trumpRank;
	private Player leadPlayer;
	private PartnerCall partnerCall;
	
	public GameEngine(int numPlayers) {
		this.numPlayers = numPlayers;
		this.numDecks = (numPlayers + 1) / 2;
		cards = new ArrayList<>();
		for (int i = 0; i < numDecks; i++) {
			for (int j = 0; j < 54; j++) {
				Card c = new Card(j);
				cards.add(c);
			}
		}
		numCardsPerPlayer = (cards.size() - 6) / numPlayers; 
		
		players = new ArrayList<>();
		players.add(new CommonSenseAI(0));
		for (int i = 1; i < numPlayers; i++) {
			players.add(new DumbAIPlayer(i));
		}
		
		round = 0;
		host = null;
		partnerCall = null;
	}
	
	/**
	 * Runs the actual game
	 */
	public void start() {
		runRound();
		round++;
	}
	
	private void runRound() {
		trumpRank = Rank.TWO;
		trumpSuit = distributeCards(trumpRank);
		updateCards(trumpSuit, trumpRank);
		partnerCall = runHost();
		printPlayerHands();
		roundPlayPhase();
	}
	
	/**
	 * Runs the play phase of a round, when players actually play cards
	 */
	private void roundPlayPhase() {
		System.out.println("Playing round with trump " + trumpSuit + " and " + trumpRank);
		leadPlayer = host;
		int playNum = 0;
		int[] playerScores = new int[numPlayers];
		
		while (playerHands.get(0).size() != 0) {
			List<List<Card>> previousPlays = new ArrayList<>();
			GameInfo gameInfo = new GameInfo(numPlayers, numDecks, host.getId(), trumpRank, trumpSuit, partnerCall, round);
			SortedSet<Integer> lead;
			do {
				lead = new TreeSet<>(leadPlayer.lead(getPlayerHand(leadPlayer), gameInfo));
			} while (!GameRules.isValidLead(getPlayerHand(leadPlayer), lead));
			
			List<Card> leadCards = removeCardsFromHand(leadPlayer, lead);
			previousPlays.add(makeCardList(leadCards));
			printPlay(leadPlayer, leadCards, playNum);

			for (int i = 1; i < numPlayers; i++) {
				int playerId = (leadPlayer.getId() + i) % numPlayers;
				Player player = players.get(playerId);
				SortedSet<Integer> play;
				do {
					play = new TreeSet<>(player.play(previousPlays, getPlayerHand(player), gameInfo));
					//printPlay(player, GameAIUtils.indicesToCards(getPlayerHand(player), play), playNum);
				} while (!GameRules.isValidPlay(leadCards, play, getPlayerHand(player)));
				
				List<Card> playedCards = removeCardsFromHand(player, play);
				previousPlays.add(makeCardList(playedCards));
				printPlay(player, playedCards, playNum);
				// TODO:  EACH PLAYER GETS OWN COPY OF PREVIOUS PLAY
			}
			
			// Determine winner, prepare for next play, etc
			int winningIndex = GameRules.getWinningIndex(previousPlays);
			Player winningPlayer = players.get((winningIndex + leadPlayer.getId()) % numPlayers);
			int numPoints = GameRules.getNumPointsPlay(previousPlays);
			playerScores[winningPlayer.getId()] += numPoints;
			System.out.println("Player " + winningPlayer.getId() + " wins hand " + playNum + " for " + numPoints + " points!\n");
			leadPlayer = winningPlayer;
			playNum++;
		}
		
		System.out.println("=================== Final Scores ================");
		for (int i = 0; i < numPlayers; i++) {
			System.out.println("Player " + i + ": " + playerScores[i]);
		}
		System.out.println("Kitty: " + GameRules.getNumPoints(kitty));
		
	}
	
	private List<Card> getPlayerHand(Player player) {
		return playerHands.get(player.getId());
	}
	
	private List<Card> removeCardsFromHand(Player player, SortedSet<Integer> indices) {
		List<Card> playerHand = getPlayerHand(player);
		List<Card> removedCards = new ArrayList<>();
		int numRemoved = 0;
		for (Integer i : indices) {
			removedCards.add(playerHand.remove(i.intValue() - numRemoved));
			numRemoved++;
		}
		return removedCards;
	}
	
	
	
	
	
	/**
	 * Waits until the host has decided cards to put back in the kitty as well as 
	 * what card to call for partner.
	 * 
	 * @return the call of the host for choosing  partner
	 */
	private PartnerCall runHost() {
		int kittySize = kitty.size();
		List<Card> handPlusKitty = new ArrayList<>();
		handPlusKitty.addAll(kitty);
		handPlusKitty.addAll(playerHands.get(host.getId()));
		
		SortedSet<Integer> returnKitty;
		GameInfo kittyInfo = new GameInfo(numPlayers, numDecks, host.getId(), trumpRank, trumpSuit, partnerCall, round);
		do {
			returnKitty = new TreeSet<>(host.handleKitty(makeCardList(handPlusKitty), kittySize, kittyInfo));
		} while (returnKitty.size() != kittySize);
		
		kitty = new ArrayList<>();
		int numRemoved = 0;
		for (Integer i : returnKitty) {
			kitty.add(handPlusKitty.remove(i.intValue() - numRemoved));
			numRemoved++;
		}
		
		playerHands.set(host.getId(), handPlusKitty);
		
		PartnerCall partnerCall = null;
		do {
			partnerCall = host.callPartner(handPlusKitty, kitty, kittyInfo);
		} while (!verifyPartnerCall(partnerCall));
		
		return partnerCall;
	}
	
	/**
	 * Returns whether this is a valid call for choosing a partner 
	 * @param partnerCall
	 * @return
	 */
	private boolean verifyPartnerCall(PartnerCall partnerCall) {
		Card card = partnerCall.getCard();
		card.setTrump(trumpSuit, trumpRank);
		if (card.isTrump()) {
			return false;
		}
		if (0 >= partnerCall.getInstance() || partnerCall.getInstance() >= numDecks) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Distributes cards to the players.
	 * @return The trump suit, as declared by the players
	 */
	private Suit distributeCards(Rank trumpRank) {
		Suit trumpSuit = null;
		int numTrump = 0;
		// the id of the called player
		Player calledPlayer = null;
		
		Collections.shuffle(cards);
		int currentCardIndex = 0;
		
		// Reset hands to be empty
		playerHands = new ArrayList<>();
		for (int i = 0; i < numPlayers; i++) {
			playerHands.add(new ArrayList<>());
		}
		
		for (int j = 0; j < numCardsPerPlayer; j++) {
			for (int i = 0; i < numPlayers; i++) {
				Card c = cards.get(currentCardIndex);
				Player player = players.get(i);
				List<Card> hand = playerHands.get(i);
				hand.add(c);
				GameInfo gameInfo = new GameInfo(numPlayers, numDecks, -1, trumpRank, trumpSuit, partnerCall, round);
				Set<Integer> calledTrump = player.draw(c.clone(), makeCardList(hand), gameInfo);
				
				if (verifyTrumpCall(trumpSuit, numTrump, calledPlayer, player, calledTrump, hand)) {
					trumpSuit = hand.get(GameRules.getFirst(calledTrump)).getBaseSuit();
					numTrump = calledTrump.size();
					calledPlayer = player;
				}
				
				currentCardIndex++;
			}
		}
		
		// Put remaining cards in the kitty
		kitty = new ArrayList<>();
		while (currentCardIndex < cards.size()) {
			kitty.add(cards.get(currentCardIndex));
			currentCardIndex++;
		}
		
		// Set host if first round
		if (round == 0) {
			host = calledPlayer;
			if (host == null) {
				// no one called, so default to player 0
				host = players.get(0);
			}
		}
		
		if (trumpSuit == Suit.TRUMP) return null;
		return trumpSuit;	
	}
	
	/**
	 * Returns whether the given attempt at a call for trump is a valid call for trump.
	 * 
	 * TODO:  FIND ACTUAL RULES FOR OVERTURNING TRUMP WTIH MORE THAN 2 DECKS
	 * @param trumpSuit
	 * @param numTrump
	 * @param calledPlayer
	 * @param player
	 * @param newTrump
	 * @param hand
	 * @return
	 */
	private boolean verifyTrumpCall(Suit trumpSuit, int numTrump, Player calledPlayer, Player player, 
									Set<Integer> newTrump, List<Card> hand) {
		// Ignore no-calls
		if (newTrump == null) return false;
		
		// Verify not over-turning self
		if (calledPlayer == player) return false;
	
		// Verify all called cards are the same
		Card firstCard = hand.get(GameRules.getFirst(newTrump));
		for (Integer c : newTrump) {
			if (firstCard.getId() != hand.get(c).getId()) {
				return false;
			}
		}
		
		// Handle pair of joker no-trump call
		if (firstCard.isJoker()) {
			return newTrump.size() > 1;
		}
		
		// Verify more cards called than previously called
		if (newTrump.size() <= numTrump) return false;
			
		return true;
	}
	
	/**
	 * Prints the hands of all players, for debugging
	 */
	private void printPlayerHands() {
		for (int i = 0; i < numPlayers; i++) {
			Player player = players.get(i);
			List<Card> hand = playerHands.get(i);
			hand.sort(null);
			System.out.println("Player " + player.getId() + ":");
			System.out.print("    ");
			System.out.println(hand);
		}
		
		System.out.println("Kitty: ");
		kitty.sort(null);
		System.out.print("    ");
		System.out.println(kitty);
	}
	
	/**
	 * Prints the cards played by a given player
	 * @param player
	 * @param play
	 */
	private void printPlay(Player player, List<Card> play, int playNum) {
		System.out.println("Round " + playNum + " - Player " + player.getId() + " plays: ");
		System.out.print("    ");
		System.out.println(play);
	}
	
	/** 
	 * Updates all the cards to take into account the trump suit.
	 * For now does not use object vars to allow different trumps
	 * to be set (possible uses with AIs)
	 * @param trump
	 */
	private void updateCards(Suit trumpSuit, Rank trumpRank) {
		for (Card c : cards) {
			c.setTrump(trumpSuit, trumpRank);
		}
	}
	

	
	/**
	 * Returns a new list of cards identical to the old one logically but having no shared references.
	 * Used before passing any information back to the players
	 * @param initialCards
	 * @return
	 */
	private List<Card> makeCardList(List<Card> initialCards) {
		List<Card> newList = new ArrayList<>();
		for (Card c : initialCards) {
			newList.add(c.clone());
		}
		return newList;
	}
}
