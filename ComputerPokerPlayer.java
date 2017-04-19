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
	
	public String getChat(int chatCondition){
		/*
		 * Gets chat from the comp player so the user can tell how aggressive a player they are
		 * condition:
		 * on open = 0
		 * on call = 1
		 * on raise = 2
		 * on fold = 3
		 * on win = 4
		 * on loss = 5
		 * on game start = 6
		 */
		
		String[] lowAggOpen = {"Let's do this!", "I'll start things off!", "Here's my opener!", "I'll get us going this time!", "Cool, I get to open!"};
		String[] medAggOpen = {"My turn to start things off!","This is a good hand for me!","This one's mine to win","Strong hand, strong opener!", "Take that!"};
		String[] highAggOpen = {"Some nice bait for the fish!", "No stopping me this time", "Ready to get wrecked?", "Time for some free money!", "Who's going to bite?"};
		String[] lowAggCall = {"I call!", "Just a call, i think!", "Can't raise you here!", "I think I'll just call!", "Don't think I can raise here!"};
		String[] medAggCall = {"I call!", "Not folding here!", "Nice bet, still going to call though!", "Hmmm...what do you have?", "Yea definitely calling here"};
		String[] highAggCall = {"That bet's not scaring me!", "Ha easy call!", "Reeling you in!", "You're just giving me free money!", "More free chips!"};
		String[] lowAggRaise = {"I'm going to raise!", "I think I should raise here!", "Got to go aggressive sometimes!", "I think it's time for a bet", "My turn to bet!"};
		String[] medAggRaise = {"Yea I'm raising!", "Definitely raising here", "Time for a raise!", "Let's take this up a level!","I raise!"};
		String[] highAggRaise = {"Let's have some real fun!", "You call that a bet?", "Now this is a real bet!", "Come on don't be soft!", "Feel free to fold if you're scared!"};
		String[] lowAggFold = {"Woah yea I'm out!", "Far too much for me!", "Not risking that much!", "You must have a really good hand!", "I don't think I can match that!"};
		String[] medAggFold = {"Ok, I fold!", "Yea I'm out!", "Nice bet, I fold!", "Can't match with these cards!", "Pity but I have to fold!"};
		String[] highAggFold = {"You're so jammy, got no choice but to fold!", "How can you always have it?", "Just wait till I get reasonable cards!","Even you can beat a better player with good cards", "I hate folding!"};
		String[] lowAggWin = {"Nice I win!", "Hard Luck!", "I had a good hand!", "It's nice to win for once!", "I thought you might have had me there!"};
		String[] medAggWin = {"Give me them chips!", "Yea I needed those!", "Lovely lovely chips", "Nice win for me!", "No way was I losing that!"};
		String[] highAggWin = {"#rekt!", "#fish!", "Get outplayed!", "Haha sucker!", "Always fear the shark in the water!"};
		String[] lowAggLoss = {"Oh well you can't win them all!", "Thought I had it there! :(", "Wow you played that well!", "Nice Hand!", "I probably shouldn't have stayed this long!"};
		String[] medAggLoss = {"Nice hand!", "I was not expecting that", "Just didnt get the cards I guess", "Wow, well I guess you win", "Oops well that's just poker I guess!"};
		String[] highAggLoss = {"Trash kid, can barely play the game!", "Can't beat these luckers!", "Pft how lucky can you get!", "If only I got good hands", "I do not believe this luck!"};
		String[] lowAggGS = {"Good Luck, Have Fun!", "Hope you enjoy the game!", "Have fun everyone!", "I'm going to win this time!", "Time for some fun!"};
		String[] medAggGS = {"GLHF!", "Let's get playing!", "Ready to go?", "I'm going to win this one!", "My time to shine!"};
		String[] highAggGS = {"Ready to see a real player in action?", "Can't wait to win this!", "This'll be easy money!", "I assume you at least know the rules?", "You won't have those chips for long!"};
		
		switch(chatCondition){
		case 0:
			if(aggressionVal < 0.33){
				return lowAggOpen[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggOpen[rand.nextInt(4)];
			}
			else{
				return highAggOpen[rand.nextInt(4)];
			}
		case 1:
			if(aggressionVal < 0.33){
				return lowAggCall[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggCall[rand.nextInt(4)];
			}
			else{
				return highAggCall[rand.nextInt(4)];
			}
		case 2:
			if(aggressionVal < 0.33){
				return lowAggRaise[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggRaise[rand.nextInt(4)];
			}
			else{
				return highAggRaise[rand.nextInt(4)];
			}
		case 3:
			if(aggressionVal < 0.33){
				return lowAggFold[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggFold[rand.nextInt(4)];
			}
			else{
				return highAggFold[rand.nextInt(4)];
			}
		case 4:
			if(aggressionVal < 0.33){
				return lowAggWin[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggWin[rand.nextInt(4)];
			}
			else{
				return highAggWin[rand.nextInt(4)];
			}
		case 5:
			if(aggressionVal < 0.33){
				return lowAggLoss[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggLoss[rand.nextInt(4)];
			}
			else{
				return highAggLoss[rand.nextInt(4)];
			}
		default:
			if(aggressionVal < 0.33){
				return lowAggGS[rand.nextInt(4)];
			}
			else if(aggressionVal < 0.67){
				return medAggGS[rand.nextInt(4)];
			}
			else{
				return highAggGS[rand.nextInt(4)];
			}
		}	
	}

}
