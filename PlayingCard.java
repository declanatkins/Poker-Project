package poker;

public class PlayingCard { 
	
	static public final char[] SUITS = {'H', 'D', 'S', 'C'};//stored the suits in an array for simplicity
	
	private char suit;
	private int gameValue;
	private String faceValue;
	
	public PlayingCard(char inSuit, int inGValue, String inFValue){//takes in the suit, the game value and the face value as a Sting
		suit = inSuit;
		gameValue = inGValue;
		faceValue = inFValue;
	}
	
	public synchronized int getGameValue(){//returns the game Value
		return gameValue;
	}
	
	public String toString(){
		return faceValue + suit;//returns string value of form KD for King of Diamonds
	}
	
	public char getSuit(){
		return suit;
	}

}
