
public enum Rank {
	TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), 
	QUEEN("Q"), KING("K"), ACE("A"), NUMBER("#"), SUITED_NUMBER("S#"), JOKER("!"), COLOR_JOKER("@");
	
	private String displayString;
	
	Rank(String displayString) {
		this.setDisplayString(displayString);
	}

	public String getDisplayString() {
		return displayString;
	}

	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}
}
