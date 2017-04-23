package poker;
import java.util.ArrayList;

import twitter4j.*;

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
	private User user;
	private Twitter tw;
	
	public GameOfPoker(int cPlayers, HumanPokerPlayer player, User u, Twitter t){
		user = u;
		tw = t;
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
		for(int i=1;i<numPlayers;i++){ 
			int j = i+sortPos;
			if(j>=numPlayers){
				j -= numPlayers;
			}
			sorted.add(players.get(j)); 
		}
		return sorted;
	}
	
	public void  dealPhase(int dealerPos) throws TwitterException{
		deck.resetDeck();
		String message="";
		playersInCurrHand = sortPlayers(dealerPosition);
		for(PokerPlayer p : playersInCurrHand){ 
			p.addChips(-ante); //player places ante
			pot.placeBet(ante); //ante added to pot
			p.getDeltHand(deck.dealCards()); //player delt cards
			message += p.getName() + " pays ante of " + ante + " chips!\n";
		}
		
		tw.sendDirectMessage(user.getId(), message);
	}
	
	public synchronized int openingPhase() throws TwitterException{
		
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
			tw.sendDirectMessage(user.getId(), "No one has openers, going to next hand");
			int chips = pot.getChips();
			pot.reset();
			return chips;
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
					if(!p.isHuman){
						tw.sendDirectMessage(user.getId(),p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(0));
					}
					playersInCurrHand = sortPlayers(playersInCurrHand.indexOf(p));
					p.setCurrBet(openBet);
					pot.setCurrBetVal(openBet); 
					p.addChips(-openBet);
					pot.placeBet(openBet);
					tw.sendDirectMessage(user.getId(), p.getName() + " opens with " + openBet + " chips!");
					if(p.getChips() == 0){
						tw.sendDirectMessage(user.getId(), p.getName() + " is all in!!");
					}
					break;//exit out of the loop and move to the next stage
				}
				else{
					tw.sendDirectMessage(user.getId(), p.getName() + " doesn't open.");
				}
			}
			else{
				if(p.isHuman){
					HumanPokerPlayer h = (HumanPokerPlayer) p;
					tw.sendDirectMessage(user.getId(), h.getHandAndType() + "\n" + "You can't open as you don;t have a pair or greater.");
				}
				tw.sendDirectMessage(user.getId(), p.getName() + " doesn't open");
			}
			
			if(p == playersInCurrHand.get(playersInCurrHand.size()-1)){ //last player does not have openers
				tw.sendDirectMessage(user.getId(), "No openers new hand starting");
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
					if(!p.isHuman){
						tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(3));
					}
					tw.sendDirectMessage(user.getId(),p.getName() + " folds!" );
					playersInCurrHand.remove(p);//no need to increase the counter here
					
				}
				else if(bet == (pot.getCurrBetVal() - p.getCurrBet())){
					if(!p.isHuman){
						tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(1));
					}
					tw.sendDirectMessage(user.getId(), p.getName() + " calls!");
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						tw.sendDirectMessage(user.getId(), p.getName() + " is all in!!");
					}
					currIndex++;//move up the counter
				}
				else{
					if(!p.isHuman){
						tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(2));
					}
					tw.sendDirectMessage(user.getId(),  p.getName() + " raises to " + bet + " chips!");
					pot.setCurrBetVal(bet);
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						tw.sendDirectMessage(user.getId(), p.getName() + " is all in!!");
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
	
	public synchronized void discardPhase() throws TwitterException{
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
				tw.sendDirectMessage(user.getId(), p.getName() + " discards " + discarded + " cards!");
			}
			else{
				tw.sendDirectMessage(user.getId(),p.getName() + " chooses not to discard!");
			}
		}
	} //by the end of this phase all players left have discarded
	
	public synchronized void finalBetPhase() throws TwitterException{ //remember for folds to remove player from currHand
		
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
						if(!p.isHuman){
							tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(3));
						}
						tw.sendDirectMessage(user.getId(), p.getName() + " folds!");
						playersInCurrHand.remove(p);//no need to increase the counter here
					}
					else{
						tw.sendDirectMessage(user.getId(), p.getName() + " checks!");
						currIndex++;
					}
				}
				else if(bet == (pot.getCurrBetVal() - p.getCurrBet())){
					if(!p.isHuman){
						tw.sendDirectMessage(user.getId(),p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(1));
					}
					tw.sendDirectMessage(user.getId(), p.getName() + " calls!");
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						tw.sendDirectMessage(user.getId(), p.getName() + " is all in!!");
					}
					currIndex++;//move up the counter
				}
				else{
					if(!p.isHuman){
						tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(2));
					}
					tw.sendDirectMessage(user.getId(), p.getName() + " raises to " + bet + " chips!");
					pot.setCurrBetVal(bet);
					p.addChips(-bet);
					pot.placeBet(bet);
					if(p.getChips() == 0){
						tw.sendDirectMessage(user.getId(), p.getName() + " is all in!!");
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
	
	public void showDown() throws TwitterException{
		int highest = 0; //stores index of player with highest hand.
		for(PokerPlayer p: playersInCurrHand){
			tw.sendDirectMessage(user.getId(), p.getName() + " has " + p.getHand() + "!\n" + p.getHandType());
			
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
	
	public void endOfHand() throws TwitterException{
		for(PokerPlayer p : playersInCurrHand){
			if(winners.contains(p)){
				if(!p.isHuman){
					tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(4));
				}
			}
			else{
				if(!p.isHuman){
					tw.sendDirectMessage(user.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(5));
				}
			}
		}

		for(PokerPlayer p : winners){
			if(winners.size() > 1){
				tw.sendDirectMessage(user.getId(), p.getName()+ " splits the pot and gets " + (pot.getChips()/winners.size()) + " chips!");
				p.addChips(pot.getChips()/winners.size());
			}
			else{
				tw.sendDirectMessage(user.getId(), p.getName()+ " wins the pot and gets " + (pot.getChips()) + " chips!");
				p.addChips(pot.getChips());
			}
		}
		for(int i=0;i<players.size();i++){
			if(players.get(i).getChips() <=0 && !players.get(i).isHuman){
				players.remove(i);
				i--;
			}
		}
		pot.reset();
	}
	
	public boolean testHandFinished(){
		return handFinished;
	}
	
	public void addCarriedChips(int carry){
		pot.placeBet(carry);
	}
	
	public User getUser(){
		return user;
	}
	
	/*
	 * This deals with whether or not the user wishes to continue playing,
	 * or else has been eliminated from the game
	 */
	public boolean isFinished() throws TwitterException{
		boolean isFinished = false;
		
		for(PokerPlayer p: players){
			if(p.isHuman && p.getChips() <= 0){
				tw.sendDirectMessage(user.getId(), "You're out of chips, Game Over!");
				return  true;
			}
		}
		
		tw.sendDirectMessage(user.getId(), "Keep Playing? Answer with yes/no");
		RandomDealTwitterBot.sleep();
		boolean invalid = true;
		while(invalid){
			ResponseList<DirectMessage> messages = tw.getDirectMessages();
			for(DirectMessage m : messages){
				String s = m.getText();
				User u = m.getSender();
				
				if(u.getId() == user.getId()){
					if(s.equalsIgnoreCase("yes")){
						invalid = false;
						break;
					}
					else if(s.equalsIgnoreCase("no")){
						invalid = false;
						handFinished = true;
					}
					else{
						tw.sendDirectMessage(user.getId(), "Invalid answer. Please answer with yes/no");
					}
				}
			}
			
		}
		
		
		return isFinished;
	}
}
