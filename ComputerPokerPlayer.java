package poker;

import java.util.ArrayList;
import java.util.Random;

public class ComputerPokerPlayer extends PokerPlayer{
	
	private double aggressionVal;//determines how aggressively the computer player will play
	private Random rand;

	public ComputerPokerPlayer(String iName) {
		super(iName);
		rand = new Random();
		aggressionVal = (rand.nextInt(100)+1)/100.0;//value between 0 and 1
		isHuman = false;
	}
	
	/*Calculates how much the computer should bet
	 * based on aggression, hand strength, current bet,
	 * current number of chips, and number of chips in 
	 * the pot
	 */
	
	public synchronized int getBet(PotOfChips pot, boolean prediscard){
		int bet=0;
		double betCalc;
		
		if(prediscard){
			int improveVal = hand.getImproveValue();
			betCalc = (aggressionVal*numChips)/((hand.getMaxVal()*2)/improveVal);
		}
		else{
			betCalc = (aggressionVal*numChips)/((hand.getMaxVal())/hand.getGameValue());
		}
		
		if(betCalc < pot.getCurrBetVal()){
			if(betCalc - 0 > pot.getCurrBetVal() - betCalc){
				bet = pot.getCurrBetVal();
			}
			else{
				bet = 0;
			}
		}
		else if (betCalc == pot.getCurrBetVal()){
			bet = pot.getCurrBetVal();
		}
		else if(betCalc < pot.getMinRaiseVal()){
			if(betCalc - pot.getCurrBetVal() > pot.getMinRaiseVal() - betCalc){
				bet = pot.getMinRaiseVal();
			}
			else{
				bet = pot.getCurrBetVal();
			}
		}
		else if(pot.getMinRaiseVal() > 0 && betCalc > 3*pot.getMinRaiseVal() ){//standardise the bets to stop craziness
			bet = 3*pot.getMinRaiseVal();
		}
		else{
			bet = (int) betCalc;
		}
		
		if(bet > numChips){
			bet = numChips;
		}
		
		return bet;
	}
	
	public synchronized int discard(){
		int numDiscarded=0;
		ArrayList<PlayingCard> discards = new ArrayList<PlayingCard>();
		for(int i=0;i<HandOfCards.HAND_SIZE;i++){
			int prob = hand.getDiscardProbability(i);
			if(numDiscarded < 3){
				if (prob == 100){
					discards.add(hand.getCardAtPos(i));
					numDiscarded++;
				}
				else if (prob > 0){
					if(rand.nextBoolean()){
						discards.add(hand.getCardAtPos(i));
						numDiscarded++;
					}
				}
			}
			else{
				break;//break if 3 discard cards already found
			}
		}
		
		for(PlayingCard p : discards){
			hand.removeCard(p);
		}
		
		
		return numDiscarded;
	}
	
	public synchronized int getOpener(int ante){
		int bet=0;
		
		if(hand.isPair()){
			bet = (int) aggressionVal*10 + ante;
		}
		else{
			bet = (int)  (aggressionVal*10 + aggressionVal*8*ante);
		}
		if(bet > numChips){
			bet = numChips;
		}
		return bet;
	}

}
