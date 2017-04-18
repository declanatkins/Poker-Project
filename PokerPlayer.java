package poker;

public class PokerPlayer {
	
	protected HandOfCards hand;
	protected String name;
	protected int numChips;
	protected boolean allIn;
	protected boolean isHuman;//is it a human or comp player
	protected int currBet;//stores the current level the player has bet to in the bet stage
	
	public PokerPlayer(String iName){
		name = iName;
		numChips = 1000;
		allIn = false;
	}
	
	public synchronized int getCurrBet(){
		return currBet;
	}
	
	public synchronized void setCurrBet(int bet){
		currBet += bet;
	}
	
	
	
	public synchronized String getName(){
		return name;
	}
	
	public synchronized int getHandValue(){
		return hand.getGameValue();
	}
	
	public synchronized String getHandType(){
		if(hand.isRoyalFlush()){
			return "That's a Royal Flush!";
		}
		else if(hand.isStraightFlush()){
			return "That's a Straight Flush!";
		}
		else if(hand.isFourOfAKind()){
			return "That's Poker!";
		}
		else if(hand.isFullHouse()){
			return "That's a Full House!";
		}
		else if(hand.isFlush()){
			return "That's a Flush!";
		}
		else if(hand.isStraight()){
			return "That's a Straight!";
		}
		else if(hand.isThreeOfAKind()){
			return "That's Trips!";
		}
		else if(hand.isTwoPair()){
			return"That's Two Pair!";
		}
		else if(hand.isPair()){
			return "That's a Pair!";
		}
		else {
			return "That's a High Hand!";
		}
	}
	
	public synchronized HandOfCards getHand(){
		return hand;
	}
	public synchronized void getDeltHand(HandOfCards delt){
		this.hand = delt;
	}
	
	public synchronized int getChips(){
		return numChips;
	}
	
	public synchronized void addChips(int amount){
		numChips += amount;
	}
	
	public synchronized boolean hasOpener(){
		if(hand.isHighCard()){
			return false;
		}
		
		return true;
	}

	public synchronized boolean checkHuman(){
		return isHuman;
	}
	
	public synchronized boolean checkAllIn(){
		return allIn;
	}
}
