import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class GameRulesTest {

	@Test
	public void winningHandTest() {
		List<Card> p0Hand = Arrays.asList(new Card(Suit.CLUBS, Rank.FIVE), new Card(Suit.CLUBS, Rank.FIVE));
		List<Card> p1Hand = Arrays.asList(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.TWO));
		List<Card> p2Hand = Arrays.asList(new Card(Suit.CLUBS, Rank.QUEEN), new Card(Suit.CLUBS, Rank.ACE));
		List<Card> p3Hand = Arrays.asList(new Card(Suit.TRUMP, Rank.JOKER), new Card(Suit.CLUBS, Rank.COLOR_JOKER));
		List<List<Card>> plays = Arrays.asList(p0Hand, p1Hand, p2Hand, p3Hand);
		setTrump(plays, Suit.CLUBS, Rank.TWO);
		
		int winningIndex = GameRules.getWinningIndex(plays);
		assertEquals(winningIndex, 0);
	}
	
	public void setTrump(List<List<Card>> plays, Suit suit, Rank rank) {
		for (List<Card> play : plays) {
			for (Card c : play) {
				c.setTrump(suit, rank);
			}
		}
	}

}
