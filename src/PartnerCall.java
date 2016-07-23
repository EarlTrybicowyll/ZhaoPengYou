/**
 * Simple class to store what is needed when calling a partner - a card and which instance
 * @author bryce
 *
 */
public class PartnerCall {
	private final Card card;
	private final int instance;
	
	public PartnerCall(Card card, int instance) {
		this.card = card;
		this.instance = instance;
	}

	public int getInstance() {
		return instance;
	}

	public Card getCard() {
		return card;
	}
}
