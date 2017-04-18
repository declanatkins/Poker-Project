package poker;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

public class HandOfCards {
	
	
	public static final int HAND_SIZE = 5;//constant size of the hand
	private static final int[] HIGH_HAND_AND_FLUSH_POS_MULTIPLIER = {14,28,42,56,70};//multiplies positions in the hand so to distinguish between hands like AKQJ9 and AQJ109 etc
	private static final int BASE_PAIR_VALUE = 2650;
	private static final int PAIR_INCREASE_MULTIPLIER = 40;//multiplied by card value to ensure pair of 3s always better than 2s always better than high hand
	private static final int TWO_PAIR_INCREASE_MULTIPLIER1 = 170;
	private static final int TWO_PAIR_INCREASE_MULTIPLIER2 = 14;
	private static final int BASE_TWO_PAIR_VALUE = 3206;//put two pair above all pairs
	private static final int BASE_TRIPS_VALUE = 5800;//put trips above all two pair
	private static final int TRIPS_INCREASE_MULTIPLIER = 30;//same principle as pair multiplier
	private static final int BASE_STRAIGHT_VALUE = 6280;
	private static final int BASE_FLUSH_VALUE = 6300;
	private static final int BASE_FULL_HOUSE_VALUE=12500;
	private static final int FULL_HOUSE_TRIPS_INCREASE_MULTIPLIER=14;//ensure trip part is more valuable than the pair
	private static final int BASE_QUADS_VALUE=17800;
	private static final int QUAD_INCREASE_MULTIPLIER = 14;
	private static final int BASE_STRAIGHT_FLUSH_VALUE = 23100;
	private static final int ROYAL_FLUSH_VALUE = 23114;
	
	private ArrayList<PlayingCard> handCards;//array storing the cards in the hand
	
	public HandOfCards(DeckOfCards deck){
		handCards = new ArrayList<PlayingCard>();
		for(int i=0; i<HAND_SIZE;i++){
			handCards.add(deck.dealNext());
		}
		sort();
	}

	private void sort(){//using bubble sort
		
		boolean flag = true;
		while(flag){
			flag = false;
			for(int i=0;i<handCards.size()-1;i++){
				if(handCards.get(i).getGameValue() > handCards.get(i+1).getGameValue()){
					flag = true;
					PlayingCard tmp = handCards.get(i);
					handCards.set(i,handCards.get(i+1));
					handCards.set(i+1,tmp);
				}
			}
		}
	}
	
	public ArrayList<PlayingCard> getHand(){
		return handCards;
	}
	
	public synchronized void removeCard(PlayingCard p){
		handCards.remove(p);
	}
	
	public synchronized PlayingCard getCardAtPos(int pos){
		return handCards.get(pos);
	}

	
	public synchronized void getNewCard(PlayingCard p){
		handCards.add(p);
		sort();
	}
	
	
	public boolean isRoyalFlush(){
		int gameValSum=0;
		for(int i=0;i<HAND_SIZE;i++){
			gameValSum += handCards.get(i).getGameValue();
		}
		if (gameValSum == 60){//sum of A+K+Q+J+10
			char suit = handCards.get(0).getSuit();
			for(int i=1;i<HAND_SIZE;i++){
				if (handCards.get(i).getSuit() != suit){
					return false;
				}
			}
		}
		else{
			return false;
		}
		
		return true;
	}
	
	public boolean isStraightFlush(){
		
		if(!isRoyalFlush()){
			//changed to deal with specific case for A2345 straight flush
			for(int i=0;i<HAND_SIZE-1;i++){
				if(handCards.get(i).getSuit() != handCards.get(i+1).getSuit()){//check suit
					return false;
				}
			}
			//prelim check for wheels:
			int matches=0;
			List<Integer> wheelStraightValues = new ArrayList<Integer>();
			wheelStraightValues.add(2);
			wheelStraightValues.add(3);
			wheelStraightValues.add(4);
			wheelStraightValues.add(5);
			wheelStraightValues.add(14);
			for(int i=0;i<HAND_SIZE;i++){
				if(wheelStraightValues.contains(handCards.get(i).getGameValue())){
					Integer val = handCards.get(i).getGameValue();
					wheelStraightValues.remove(val);
					matches++;
				}
			}
			if(matches == 5){
				return true;
			}
			else{
				for(int i=0;i<HAND_SIZE-1;i++){
					if(handCards.get(i).getGameValue() != handCards.get(i+1).getGameValue() -1){
						return false;
					}
				}
			}
		
		return true;//if in order and suited return true
		}
		
		return false;//only reached if its a royal flush
	}
	
	public boolean isFourOfAKind(){
		for(int i=0;i<HAND_SIZE-3;i++){
			if(handCards.get(i).getGameValue() == handCards.get(i+3).getGameValue()){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isFullHouse(){
		if(handCards.get(0).getGameValue() == handCards.get(2).getGameValue() && handCards.get(3).getGameValue() == handCards.get(4).getGameValue()){
			return true;
		}
		else if(handCards.get(0).getGameValue() == handCards.get(1).getGameValue() && handCards.get(2).getGameValue() == handCards.get(4).getGameValue()){
			return true;
			
		}
		else{
			return false;
		}
	}
	
	public boolean isFlush(){
		if(!isRoyalFlush() && !isStraightFlush()){
			for(int i=0;i<HAND_SIZE-1;i++){
				if(handCards.get(i).getSuit() != handCards.get(i+1).getSuit()){//check suit
					return false;
				}
			}
		}
		else{
			return false;
		}
		return true;
	}
	
	public boolean isStraight(){
		if(!isRoyalFlush() && !isStraightFlush()){
			//prelim check for wheels:
			int matches=0;
			List<Integer> wheelStraightValues = new ArrayList<Integer>();
			wheelStraightValues.add(2);
			wheelStraightValues.add(3);
			wheelStraightValues.add(4);
			wheelStraightValues.add(5);
			wheelStraightValues.add(14);
			for(int i=0;i<HAND_SIZE;i++){
				if(wheelStraightValues.contains(handCards.get(i).getGameValue())){
					Integer val = handCards.get(i).getGameValue();
					wheelStraightValues.remove(val);//take out the element to avoid double checking in case of pair
					matches++;
				}
			}
			if(matches == 5){
				return true;
			}
			else{
				for(int i=0;i<HAND_SIZE-1;i++){
					if(handCards.get(i).getGameValue() != handCards.get(i+1).getGameValue() -1){
						return false;
					}
				}
			}
			return true;
		}
		return false;
		
	}
	
	public boolean isThreeOfAKind(){
		if(!isFullHouse() && !isFourOfAKind()){
			for(int i=0;i<HAND_SIZE-2;i++){
				if(handCards.get(i).getGameValue() == handCards.get(i+2).getGameValue()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isTwoPair(){
		if(!isFullHouse() && !isFourOfAKind()){
			for(int i=0;i<HAND_SIZE-2;i++){//check for first pair
				if(handCards.get(i).getGameValue() == handCards.get(i+1).getGameValue()){
					for(int j=i+2;j<HAND_SIZE-1;j++){//check for second pair
						if(handCards.get(j).getGameValue() == handCards.get(j+1).getGameValue()){
							return true;//if found 2 pairs
						}
					}
				}
			}
		}
		return false;//if not found or is another hand
	}
	
	public boolean isPair(){
		if(!isFullHouse() && !isThreeOfAKind() && !isFourOfAKind() && !isTwoPair()){
			for(int i=0;i<HAND_SIZE-1;i++){
				if(handCards.get(i).getGameValue() == handCards.get(i+1).getGameValue()){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isHighCard(){
		if(!isRoyalFlush() && !isStraightFlush() && !isFourOfAKind() && !isFullHouse() && !isFlush() && !isStraight() && !isThreeOfAKind() && !isTwoPair() && !isPair()){
			return true;
		}
		
		return false;
	}
	
	
	public String toString(){
		String rString="";
		for(int i=0;i<HAND_SIZE;i++){
			rString += handCards.get(i).toString() + " ";
		}
		return rString;
	}
	
	/*Game value for hands:
	 * Each hand above a high hand has a base value constant to put it above all hands below it
	 * Certain hand types also have multipliers to make the grouped part of the most significant
	 * 
	 * HIGH HAND:
	 * C0*M0 + C1*M1 + C2*M2 + C3*M3 + C4*M4
	 * C = card from hand
	 * M = multiplier from array
	 * number: position in array
	 * 
	 * PAIR:
	 * PBV + P.GV * PIM + OCV
	 * PBV = Pair base value
	 * P.GV = pair card game value
	 * PIM = pair increase multiplier
	 * OCV = sum of other card values
	 * 
	 * TWO PAIR:
	 * TPBV + P1.GV*TPIM1 + P2.GV*TPIM2 + OGV
	 * TPBV = Two pair base value
	 * TPIM1 = 1st two pair multiplier
	 * TPIM2 = 2nd two pair multiplier
	 * 
	 * TRIPS:
	 * TBV + T.GV*TIM + OCV
	 * TBV = Trips base value
	 * T.GV = trip card game value
	 * TIM = Trips increase multiplier
	 * 
	 * Straight:
	 * SBV + HCV
	 * SBV= straight base value
	 * HCV = game value of highest card in the straight
	 * 
	 * Flush:
	 * FBV + C0*M0 + C1*M1 + C2*M2 + C3*M3 + C4*M4
	 * FBV = flush base value
	 * C,M,number = same as in high hand
	 * 
	 * Full House:
	 * FHBV + T.GV*FHTIM + P.GV
	 * FHBV = full house base value
	 * FHTIM = full house trips increase multiplier
	 * T.GV = trip card game value
	 * P.GV = pair card game value
	 * 
	 * POKER:
	 * 4BV + 4.GV*4IM + OCV
	 * 4BV = poker base value
	 * 4.GV = poker card game value
	 * 4IM = poker increase multiplier
	 * OCV = other card value
	 * 
	 * STRAIGHT FLUSH:
	 * SFBV + HCV
	 * SFBV = straight flush base value
	 * HCV = highest card game value
	 * 
	 * ROYAL FLUSH:
	 * Royal flush value
	 */
	
	public synchronized int getGameValue(){
		if(isRoyalFlush()){
			return ROYAL_FLUSH_VALUE;
		}
		else if(isStraightFlush()){
			if(handCards.get(0).getGameValue()==2 && handCards.get(HAND_SIZE-1).getGameValue() == 14){
				return BASE_STRAIGHT_FLUSH_VALUE + 5;
			}
			else{
				return BASE_STRAIGHT_FLUSH_VALUE + handCards.get(HAND_SIZE-1).getGameValue();
			}
		}
		else if(isFourOfAKind()){
			if(handCards.get(0).getGameValue() == handCards.get(3).getGameValue()){
				return BASE_QUADS_VALUE + handCards.get(0).getGameValue()*QUAD_INCREASE_MULTIPLIER + handCards.get(HAND_SIZE-1).getGameValue();
			}
			else{
				return BASE_QUADS_VALUE + handCards.get(0).getGameValue() + handCards.get(HAND_SIZE-1).getGameValue()*QUAD_INCREASE_MULTIPLIER;
			}
		}
		else if(isFullHouse()){
			if(handCards.get(0).getGameValue() == handCards.get(2).getGameValue()){
				return BASE_FULL_HOUSE_VALUE+ handCards.get(0).getGameValue()*FULL_HOUSE_TRIPS_INCREASE_MULTIPLIER + handCards.get(HAND_SIZE-1).getGameValue();
			}
			else{
				return BASE_FULL_HOUSE_VALUE + handCards.get(0).getGameValue() + handCards.get(HAND_SIZE-1).getGameValue()*FULL_HOUSE_TRIPS_INCREASE_MULTIPLIER;
			}
		}
		else if(isFlush()){
			int val = 0;
			for(int i=0;i<HAND_SIZE;i++){
				val += handCards.get(i).getGameValue()*HIGH_HAND_AND_FLUSH_POS_MULTIPLIER[i];
			}
			
			return BASE_FLUSH_VALUE + val;
		}
		else if(isStraight()){
			if(handCards.get(0).getGameValue()==2 && handCards.get(HAND_SIZE-1).getGameValue() == 14){
				return BASE_STRAIGHT_VALUE + 5;
			}
			else{
				return BASE_STRAIGHT_VALUE + handCards.get(HAND_SIZE-1).getGameValue();
			}
		}
		else if(isThreeOfAKind()){
			//place the three cards at the start of the array
			ArrayList<PlayingCard> modCards = new ArrayList<PlayingCard>();
			for(PlayingCard p : handCards){
				modCards.add(p);
			}
			for(int i=0;i<HAND_SIZE-1;i++){
				if(modCards.get(i).getGameValue() == modCards.get(0).getGameValue() || modCards.get(i).getGameValue() == modCards.get(i+1).getGameValue()){
					PlayingCard tmp0,tmp1;
					tmp0 = modCards.get(0);
					modCards.set(0, modCards.get(i));
					for(int j=1;j<=i;j++){
						tmp1 = modCards.get(j);
						modCards.set(j, tmp0);
						tmp0 = tmp1;
					}
				}
			}
			
			if(modCards.get(HAND_SIZE-1).getGameValue() == modCards.get(0).getGameValue()){
				PlayingCard tmp0,tmp1;
				tmp0 = modCards.get(0);
				modCards.set(0, modCards.get(HAND_SIZE-1));
				for(int j=1;j<HAND_SIZE;j++){
					tmp1 = modCards.get(j);
					modCards.set(j, tmp0);
					tmp0 = tmp1;
				}
			}
			
			return BASE_TRIPS_VALUE + modCards.get(0).getGameValue()*TRIPS_INCREASE_MULTIPLIER + modCards.get(HAND_SIZE-1).getGameValue();
			
		}
		else if(isTwoPair()){
			if(handCards.get(0) != handCards.get(1)){
				return BASE_TWO_PAIR_VALUE + handCards.get(1).getGameValue()*TWO_PAIR_INCREASE_MULTIPLIER1 + handCards.get(HAND_SIZE-1).getGameValue()*TWO_PAIR_INCREASE_MULTIPLIER2 + handCards.get(0).getGameValue();
			}
			else if(handCards.get(HAND_SIZE-1).getGameValue() != handCards.get(HAND_SIZE-2).getGameValue()){
				return BASE_TWO_PAIR_VALUE + handCards.get(0).getGameValue()*TWO_PAIR_INCREASE_MULTIPLIER1 + handCards.get(HAND_SIZE-2).getGameValue()*TWO_PAIR_INCREASE_MULTIPLIER2 + handCards.get(HAND_SIZE-1).getGameValue();
			}
			else{
				return BASE_TWO_PAIR_VALUE + handCards.get(0).getGameValue()*TWO_PAIR_INCREASE_MULTIPLIER1 + handCards.get(HAND_SIZE-1).getGameValue()*TWO_PAIR_INCREASE_MULTIPLIER2 + handCards.get(2).getGameValue();
			}
		}
		else if(isPair()){
			//Same theory as for trips place the pair at the start to make it easier
			ArrayList<PlayingCard> modCards = new ArrayList<PlayingCard>();
			for(PlayingCard p : handCards){
				modCards.add(p);
			}
			for(int i=0;i<HAND_SIZE-1;i++){
				if(modCards.get(i).getGameValue() == modCards.get(0).getGameValue() || modCards.get(i).getGameValue() == modCards.get(i+1).getGameValue()){
					PlayingCard tmp0,tmp1;
					tmp0 = modCards.get(0);
					modCards.set(0, modCards.get(i));
					for(int j=1;j<=i;j++){
						tmp1 = modCards.get(j);
						modCards.set(j, tmp0);
						tmp0 = tmp1;
					}
				}
			}
			
			if(modCards.get(HAND_SIZE-1).getGameValue() == modCards.get(0).getGameValue()){
				PlayingCard tmp0,tmp1;
				tmp0 = modCards.get(0);
				modCards.set(0, modCards.get(HAND_SIZE-1));
				for(int j=1;j<HAND_SIZE;j++){
					tmp1 = modCards.get(j);
					modCards.set(j, tmp0);
					tmp0 = tmp1;
				}
			}
			
			return BASE_PAIR_VALUE + modCards.get(0).getGameValue() * PAIR_INCREASE_MULTIPLIER + modCards.get(HAND_SIZE-1).getGameValue();
		}
		else if(isHighCard()){
			int val = 0;
			for(int i=0;i<HAND_SIZE;i++){
				val += handCards.get(i).getGameValue()*HIGH_HAND_AND_FLUSH_POS_MULTIPLIER[i];
			}
			
			return val;
		}
		
		return -1;//on error of unclassified hand
	}
	
	public synchronized int getDiscardProbability(int cardPosition){
		
		if(isRoyalFlush() || isStraightFlush() || isFourOfAKind() || isFullHouse() || isFlush() || isStraight()){//hands that can't be improved or have almost no chance of being improved
			return 0;
		}
		else if(isThreeOfAKind()){
			if(((cardPosition > 0) && handCards.get(cardPosition).getGameValue() == handCards.get(cardPosition-1).getGameValue()) ||((cardPosition < HAND_SIZE-1) && handCards.get(cardPosition).getGameValue() == handCards.get(cardPosition+1).getGameValue())){
				return 0;//if the card is on of the three
			}
			else{
				return 100;
			}
		}
		else if(isTwoPair()){
			if(((cardPosition > 0) && handCards.get(cardPosition).getGameValue() == handCards.get(cardPosition-1).getGameValue()) ||((cardPosition < HAND_SIZE-1) && handCards.get(cardPosition).getGameValue() == handCards.get(cardPosition+1).getGameValue())){
				return 0;//if the card is in one of the pairs
			}
			else{
				//odds of success higher than odds of another player having the same two pair
				//but with A,K,Q could still be worth holding onto
				if(handCards.get(cardPosition).getGameValue() > 11){
					return 80;
				}
				else{
					return 100;
				}
			}
		}
		else if(isPair()){
			if(((cardPosition > 0) && handCards.get(cardPosition).getGameValue() == handCards.get(cardPosition-1).getGameValue()) ||((cardPosition < HAND_SIZE-1) && handCards.get(cardPosition).getGameValue() == handCards.get(cardPosition+1).getGameValue())){
				return 0;//if the card is in the pair
			}
			else{
				if(cardPosition != HAND_SIZE-1){//if its not the highest card always swap
					return 100;
				}
				else{
					//Again might be worth keeping A,K or Q
					if(handCards.get(cardPosition).getGameValue() > 11){
						return 80;
					}
					else{
						return 100;
					}
				}
			}
		}
		else{//if is a high hand
			if (isBrokenWheelsStraight()){
				if (isBrokenWheelsStraightCard(cardPosition)){
					return 100;
				}
				else{
					return 0;
				}	
			}
			else if(isBrokenStraight()){
				if (isBrokenStraightCard(cardPosition)){
					return 100;
				}
				else{
					return 0;
				}	
			}
			else if(isBrokenStraightCheck2()){
				if(isCheck2Card(cardPosition)){
					return 100;
				}
				else{
					return 0;
				}
			}
			else if(isBrokenFlush()){
				if(isBrokenFlushCard(cardPosition)){
					return 100;
				}
				else{
					return 0;
				}
			}
			else if(handCards.get(cardPosition).getGameValue() < 11){//if not a picture card
				return 100;
			}
			else{
				return 0;
			}
		}
	}
	
	private boolean isBrokenWheelsStraight(){
		/*
		 * Special case of wheels straight
		 * checks the values of the hand against the values that should be in the straight
		 */
		int matches=0;
		List<Integer> wheelStraightValues = new ArrayList<Integer>();
		wheelStraightValues.add(2);
		wheelStraightValues.add(3);
		wheelStraightValues.add(4);
		wheelStraightValues.add(5);
		wheelStraightValues.add(14);
		
		for(int i=0;i<HAND_SIZE;i++){
			if(wheelStraightValues.contains(handCards.get(i).getGameValue())){
				matches++;
				wheelStraightValues.remove((Integer) handCards.get(i).getGameValue());
			}
		}
		if(matches == 4){
			return true;
		}
		else{
			return false;
		}
	}
	
	private boolean isBrokenWheelsStraightCard(int cardPos){
		//checks for the problem card and tests if it is the inputted one
		List<Integer> wheelStraightValues = Arrays.asList(2,3,4,5,14);
		for(int i=0;i<HAND_SIZE;i++){
			if(!wheelStraightValues.contains(handCards.get(i).getGameValue())){
				return i == cardPos;
			}
		}
		
		return false;
	}
	
	private boolean isBrokenStraight(){
		/*
		 * checks if it is a broken straight (not wheels)
		 * works by ignoring one card and testing if the others are in sequence using the formula:
		 * n + (n+1) + (n+2) + (n+3) = 4*n + 6
		 */
		for(int i=0;i<HAND_SIZE;i++){
			int sum=0;
			for(int j=0;j<HAND_SIZE;j++){
				if(j != i){
					sum += handCards.get(j).getGameValue();
				}
			}
			if(i==0){
				if (sum - (handCards.get(1).getGameValue()*4) == 6){
					return true;
				}
			}
			else{
				if (sum - (handCards.get(0).getGameValue()*4) == 6){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isBrokenStraightCard(int cardPos){
		//checks the sum ignoring the inputted card to see if it is the problem one
		int sumCards=0;
		if(cardPos == 0){
			for(int i= 1;i<HAND_SIZE;i++){
				sumCards += handCards.get(i).getGameValue();
			}
			
			return ((handCards.get(1).getGameValue()*4) + 6) == sumCards;
		}
		else{
			for(int i=0;i<HAND_SIZE;i++){
				if(i != cardPos){
					sumCards += handCards.get(i).getGameValue();
				}
			}
			
			return ((handCards.get(0).getGameValue()*4) + 6) == sumCards;
		}
	}
	
	private List<Integer> straightFromCardPos(int pos){
		
		List<Integer> straight = new ArrayList<Integer>();
		
		for(int i=0;i<HAND_SIZE;i++){
			straight.add(handCards.get(pos).getGameValue() - (pos - i));
		}
		
		return straight;
	}
	
	private boolean isBrokenStraightCheck2(){
		//this function is used to catch inside straights as the previous function wont work for them
		List<Integer> check;
		for(int i=0;i<HAND_SIZE;i++){
			check = straightFromCardPos(i);
			int matches = 0;
			for(int j=0;j<HAND_SIZE;j++){
				if(check.contains(handCards.get(j).getGameValue())){
					matches++;
				}
			}
			
			if(matches == 4){
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isCheck2Card(int cardPos){
		List<Integer> check;
		for(int i=0;i<HAND_SIZE;i++){
			check = straightFromCardPos(i);
			int matches = 0;
			for(int j=0;j<HAND_SIZE;j++){
				if(check.contains(handCards.get(j).getGameValue())){
					matches++;
				}
			}
			
			if(matches == 4){
				if (!check.contains((Integer) handCards.get(cardPos).getGameValue())){
					return true;
				}
				else{
					return false;
				}
			}
		}
		
		return false;
	}
	
	private boolean isBrokenFlush(){
		//counts the amount of each suit in the hand returns true if one has 4
		int numS=0,numC=0,numH=0,numD=0;
		for(int i=0;i<HAND_SIZE;i++){
			char s = handCards.get(i).getSuit();
			if(s == PlayingCard.SUITS[0]){
				numH++;
			}
			else if(s == PlayingCard.SUITS[1]){
				numD++;
			}
			else if(s == PlayingCard.SUITS[2]){
				numS++;
			}
			else{
				numC++;
			}
		}
		
		return numS == 4 || numC == 4 || numD == 4 || numH == 4; 
	}
	
	private boolean isBrokenFlushCard(int cardPos){
		/*
		 * checks for a card that doesn't match the suit of the next card
		 * then tests the next card after that to decide which one is the problem card
		 */
		int breakPos=HAND_SIZE-1;
		for(int i=0;i<HAND_SIZE-2;i++){
			if(handCards.get(i).getSuit() != handCards.get(i+1).getSuit()){
				if(handCards.get(i+1).getSuit() != handCards.get(i+2).getSuit()){
					breakPos = i+1;
					return cardPos == breakPos;
				}
				else{
					breakPos = i;
					return cardPos == breakPos;
				}
			}
		}
		
		return cardPos == breakPos;
	}
	
	public int getImproveValue(){
		int val;
		if(isBrokenStraight() || isBrokenWheelsStraight()){
			val = BASE_STRAIGHT_VALUE;
		}
		else if(isBrokenFlush()){
			val = BASE_FLUSH_VALUE;
		}
		else if(isPair()){
			val = BASE_TRIPS_VALUE;
		}
		else if(isTwoPair() || isThreeOfAKind()){
			val = BASE_FULL_HOUSE_VALUE;
		}
		else if(isHighCard()){
			val = BASE_PAIR_VALUE;
		}
		else{
			val = getGameValue();
		}
		
		return val;
	}
	
	public int getMaxVal(){
		return ROYAL_FLUSH_VALUE;
	}
}
