package poker;
import java.util.ArrayList;

public class GameOfPoker {
	public int dealerPosition; //(Button)dealer position 
	private PotOfChips pot;
	public ArrayList<PokerPlayer> players;
	private ArrayList<PokerPlayer> playersInCurrHand;
	private int numPlayers;
	private DeckOfCards deck;
	private boolean handFinished = false;
	private int ante;
	private ArrayList<PokerPlayer> winners;
	
	public GameOfPoker(int cPlayers, HumanPokerPlayer player){
		numPlayers = cPlayers+1; //computer players + human players
		players = new ArrayList<PokerPlayer>();
		ComputerPlayerCreator ComputerPlayers = new ComputerPlayerCreator(cPlayers); //create all computer players
		players.add(player);//add human player
		for(PokerPlayer p : ComputerPlayers.getPlayers()){ //add all computer players
			players.add(p);
		}
		
		deck = new DeckOfCards(); //create deck
		dealerPosition = 0;
		pot = new PotOfChips();
		ante = 10;
	}
	
	public ArrayList<PokerPlayer> sortPlayers(int sortPos){ //sorts an arraylist of poker players clockwise from sortPos
		ArrayList<PokerPlayer> sorted = new ArrayList<PokerPlayer>();
		sorted.add(players.get(sortPos)); //add dealer pos as first element
		for(int i=0;i<numPlayers-1;i++){ 
			int j = i+sortPos+1;
			if(j>=numPlayers){
				j -= numPlayers;
			}
			sorted.add(players.get(j)); 
		}
		return sorted;
	}
	
	public void  dealPhase(int dealerPos){
		deck.resetDeck();
		playersInCurrHand = sortPlayers(dealerPosition);
		for(PokerPlayer p : playersInCurrHand){ 
			p.addChips(-ante); //player places ante
			pot.placeBet(ante); //ante added to pot
			p.getDeltHand(deck.dealCards()); //player delt cards
		}
		
	}
	
	public synchronized int openingPhase(){
		
		PokerPlayer opener = new PokerPlayer("tmpName");
		for(PokerPlayer p : playersInCurrHand){
			p.resetCurrBet();//reset the bets to 0
		}
		
		boolean openerExists = false;
		for(PokerPlayer p : playersInCurrHand){
			if(p.hasOpener()){
				openerExists=true;
			}
		}
		
		if(!openerExists){
			System.out.println("No one has openers, going to next hand");
			return pot.getChips();
		}
		
		for(PokerPlayer p : playersInCurrHand){
			int openBet = 0;
			if(p.hasOpener()){ //if a player can open
				if(p.isHuman){
					 openBet = ((HumanPokerPlayer)p).getOpener();
				}
				else{
					openBet = ((ComputerPokerPlayer)p).getOpener(ante);
				}
				
				if(openBet > 0){
					opener = p;
					playersInCurrHand = sortPlayers(playersInCurrHand.indexOf(p));
					p.setCurrBet(openBet);
					pot.setCurrBetVal(openBet); 
					p.addChips(-openBet);
					pot.placeBet(openBet);
					if(openBet >0){
						System.out.println(p.getName() + " opens with " + openBet + " chips!");
					}
					else{
						System.out.println(p.getName() + " doesn't open.");
					}
					if(p.getChips() == 0){
						System.out.println(p.getName() + " is all in!!");
					}
					break;//exit out of the loop and move to the next stage
				}
			}
			else{
				if(p.isHuman){
					((HumanPokerPlayer)p).printHand();
					System.out.println("You can't open as you don't have a pair or greater");
				}
				System.out.println(p.getName() + " doesn't open.");
			}
			
			if(p == playersInCurrHand.get(playersInCurrHand.size()-1)){ //last player does not have openers
				System.out.println("No openers new hand starting");
				return pot.getChips(); //return chips to carry
				// start new game discard phase will not run and just return;
			}
		}
		
		boolean allMatched = false;
		int currIndex = 1;
		if(playersInCurrHand.get(currIndex).getName().equalsIgnoreCase(opener.getName())){
			currIndex++;
		}
		
		while(!allMatched){
			PokerPlayer p = playersInCurrHand.get(currIndex);
			System.out.println(p.getName()+ ","+currIndex);
			if(p.getCurrBet() < pot.getCurrBetVal()){
				int bet;
				if(p.isHuman){
					bet = ((HumanPokerPlayer)p).getBet(pot);
				}
				else{
					bet = ((ComputerPokerPlayer)p).getBet(pot, true);
				}
				
				if(bet == 0){
					System.out.println(p.getName() + " folds!");
					playersInCurrHand.remove(p);//no need to increase the counter here
				}
				else if(bet == (pot.getCurrBetVal() - p.getCurrBet())){
					System.out.println(p.getName() + " calls!");
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						System.out.println(p.getName() + " is all in!!");
					}
					currIndex++;//move up the counter
				}
				else{
					System.out.println(p.getName() + " raises to " + bet + " chips!");
					pot.setCurrBetVal(bet);
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						System.out.println(p.getName() + " is all in!!");
					}
					currIndex++;
				}
				
				p.setCurrBet(bet);
			}
			else{
				System.out.println(p.getName() + "," + p.getCurrBet());
				allMatched = true;
			}
			
			if(currIndex > playersInCurrHand.size()-1){
				currIndex =0;
			}
			
		}
		
		if(playersInCurrHand.size() == 1){
			winners = new ArrayList<PokerPlayer>();
			winners.add(playersInCurrHand.get(0));
			handFinished = true;
		}
		return 0;
	} //by the end this phase if someone opened we should have done a round of
	  // betting and ready for the next person to discard
	  //phase returns 0 if no carrys if there is a carry then return the amount to add to pot next time
	
	public void discardPhase(){
		int discarded = 0;
		for(PokerPlayer p : playersInCurrHand){
			if(p.isHuman){
				discarded = ((HumanPokerPlayer)p).discard();
				p.getDeltHand(deck.giveNewCards(p.hand, discarded));
			}
			else{
				discarded = ((ComputerPokerPlayer)p).discard();
				p.getDeltHand(deck.giveNewCards(p.hand, discarded));
			}
			
			if(discarded > 0){
				System.out.println(p.getName() + " discards " + discarded + " cards!");
			}
			else{
				System.out.println(p.getName() + " chooses not to discard!");
			}
		}
	} //by the end of this phase all players left have discarded
	
	public void finalBetPhase(){ //remember for folds to remove player from currHand
		
		for(PokerPlayer p : playersInCurrHand){
			p.resetCurrBet();//reset the bets to 0
		}
		pot.setCurrBetVal(0);
		
		boolean allMatched = false;
		boolean goneThroughAll = false;
		int currIndex = 0;
		
		while(!allMatched){
			PokerPlayer p = playersInCurrHand.get(currIndex);
			if((p.getCurrBet() < pot.getCurrBetVal() || (!goneThroughAll)) && !p.checkAllIn()){
				int bet;
				if(p.isHuman){
					bet = ((HumanPokerPlayer)p).getBet(pot);
				}
				else{
					bet = ((ComputerPokerPlayer)p).getBet(pot, true);
				}
				if(bet == 0){
					if(pot.getCurrBetVal() > 0){
						System.out.println(p.getName() + " folds!");
						playersInCurrHand.remove(p);//no need to increase the counter here
					}
					else{
						System.out.println(p.getName() + " checks!");
						currIndex++;
					}
				}
				else if(bet == (pot.getCurrBetVal() - p.getCurrBet())){
					System.out.println(p.getName() + " calls!");
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						System.out.println(p.getName() + " is all in!!");
					}
					currIndex++;//move up the counter
				}
				else{
					System.out.println(p.getName() + " raises to " + bet + " chips!");
					pot.setCurrBetVal(bet);
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						System.out.println(p.getName() + " is all in!!");
					}
					currIndex++;
				}
				
				p.setCurrBet(bet);
			}
			else{
				allMatched = true;
			}
			
			if(currIndex > playersInCurrHand.size()-1){
				goneThroughAll = true;
				currIndex =0;
			}
			
		}
		
		if(playersInCurrHand.size() == 1){
			winners = new ArrayList<PokerPlayer>();
			winners.add(playersInCurrHand.get(0));
			handFinished = true;
		}

	}
	
	public void showDown(){
		int highest = 0; //stores index of player with highest hand.
		for(PokerPlayer p: playersInCurrHand){
			System.out.println(p.getName() + " has " + p.getHand() + "!\n" + p.getHandType());
			
			if(p.getHandValue() > highest){
				highest = p.getHandValue();
				winners = new ArrayList<PokerPlayer>();
				winners.add(p);
			}
			if(p.getHandValue() == highest && !winners.contains(p)){
				winners.add(p);
			}
			
		}
	}
	
	public void endOfHand(){

		for(PokerPlayer p : winners){
			if(winners.size() > 1){
				System.out.println(p.getName()+ " splits the pot and gets " + (pot.getChips()/winners.size()) + " chips!");
			}
			else{
				System.out.println(p.getName()+ " wins the pot and gets " + (pot.getChips()) + " chips!");
				p.addChips(pot.getChips());
			}
		}
		System.out.println("\n\n\n\n");
		pot.reset();
	}
	
	public boolean testHandFinished(){
		return handFinished;
	}
	
	public void addCarriedChips(int carry){
		pot.placeBet(carry);
	}
}
