package poker;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
//import twitter4j.api.*;

//import java.util.Scanner;
public class HumanPokerPlayer extends PokerPlayer{
	
	//private Scanner sc;
	private Twitter twitter;
	private User user;

	public HumanPokerPlayer(User u, Twitter t) {
		super(u.getName());
		user = u;
		twitter = t;
		//sc = new Scanner(System.in);
		isHuman = true;
	}
	/*
	 * UNCOMMENT THIS FOR TESTING GAMEPLAY
	 * public HumanPokerPlayer(String iName) {
		super(iName);
		sc = new Scanner(System.in);
		isHuman = true;
	}
	*/
	
	/*
	 * UNCOMMENT THIS FOR TESTING GAMEPLAY OUTSIDE OF TWITTER BOT
	public void printHand(){
		System.out.println("Your hand is: " + hand);
		System.out.println(this.getHandType());
	}
	*/
	
	public String getHandAndType(){
		String rStr = "Your hand is: " + hand + "\n" + this.getHandType();
		return rStr;
	}
	
	public synchronized int discard() throws TwitterException{
		int numDiscarded=0;
		int n=0;
		String message = getHandAndType();
		
		ArrayList<PlayingCard> suggestCards = new ArrayList<PlayingCard>();
		ArrayList<PlayingCard> chosenCards = new ArrayList<PlayingCard>();
		/*
		 * UNCOMMENT THIS FOR TESTING GAMEPLAY
		 * printHand();
		 */
		message = this.getHandAndType();
		twitter.sendDirectMessage(user.getId(), message);
		for(int i=0;i<HandOfCards.HAND_SIZE;i++){
			if (n<3 && hand.getDiscardProbability(i) == 100){
				suggestCards.add(hand.getCardAtPos(i));
				n++;
			}
		}
		/*
		 * UNCOMMENT THIS FOR TESTING GAMEPLAY
		System.out.println(name + " it's your move!");
		*/
		
		twitter.sendDirectMessage(user.getId(), name + " it's your move!");
		if(n > 0){
			//System.out.println("Recomend you discard the following cards:");
			twitter.sendDirectMessage(user.getId(), "Recommend you discard the following cards:");
			for(int i=0;i<n;i++){
				//System.out.println(suggestCards.get(i) + ", at position " + hand.getHand().indexOf(suggestCards.get(i)));
				twitter.sendDirectMessage(user.getId(), suggestCards.get(i) + ", at position " + hand.getHand().indexOf(suggestCards.get(i)));
			}
		}
		else{
			//System.out.println("Recommend you dont discard any cards.");
			twitter.sendDirectMessage(user.getId(), "Recommend you dont discard any cards.");
		}
		/*
		System.out.println("Please enter the cards that you would like to discard by position (0-4):");
		System.out.println("You can discard up to 3 cards");
		System.out.println("Enter each index one at a time followed by the return key");
		System.out.println("To accept selected indexes and end input, type \'.\' followed by the return key");
		*/
		twitter.sendDirectMessage(user.getId(),"Please enter the cards that you would like to discard by position (0-4):\nYou can discard up to 3 cards\nEnter each index one at a time and then send a message with just a \'.\'");
		
		String input= "";
		while (!input.equals(".")){
			//input = sc.nextLine();
			input = getNewMessage();
			try{
				Integer index = Integer.parseInt(input);
				if(numDiscarded < 3){
					if(index <= 4 && index >= 0){
						if(chosenCards.contains(hand.getCardAtPos(index))){
							//System.out.println("You've already entered that card");
							//System.out.println("Please enter again");
							twitter.sendDirectMessage(user.getId(), "You've already entered that card\nPlease enter again");
						}
						else{
							chosenCards.add(hand.getCardAtPos(index));
							numDiscarded++;
							//System.out.println("Discarding " + hand.getCardAtPos(index));
							twitter.sendDirectMessage(user.getId(), "Discarding " + hand.getCardAtPos(index));
						}
					}
					else{
						//System.out.println("Invalid input, index must be between 0 and 4 inclusive");
						//System.out.println("Please enter again");
						twitter.sendDirectMessage(user.getId(), "Invalid input, index must be between 0 and 4 inclusive\nPlease enter again");
					}
				}
				else{
					//System.out.println("You have already selected 3 cards to discard");
					//System.out.println("Would you like to cancel and select new cards? (Enter y/n)");
					twitter.sendDirectMessage(user.getId(), "You have already selected 3 cards to discard\nWould you like to cancel and select new cards? (Enter y/n)");
					//input = sc.nextLine();
					input = getNewMessage();
					
					while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")){
					//	System.out.println("You must enter y/n");
						twitter.sendDirectMessage(user.getId(), "You must enter y/n");
//						input = sc.nextLine();
						input = getNewMessage();
					}
					
					if(input.equalsIgnoreCase("y")){
						numDiscarded = 0;
						chosenCards = new ArrayList<PlayingCard>();
					}
				}
			}
			catch (NumberFormatException e){
				if (!input.equals(".")){
					//System.out.println("Invalid input");
					//System.out.println("Please enter again");
					twitter.sendDirectMessage(user.getId(), "Invalid input\nPlease enter again");
				}
			}
		}
		
		for(PlayingCard P : chosenCards){
			hand.removeCard(P);
		}
		
		
		return numDiscarded;
	}
	
	public synchronized int getBet(PotOfChips pot) throws TwitterException{
		
		int bet=0;
		int currBetVal = pot.getCurrBetVal();
		/*
		 * UNCOMMENT THIS FOR TESTING GAMEPLAY
		 * printHand();
		 */
		String message = getHandAndType();
		if(currBetVal == 0){
			//System.out.println("Check or bet?\nTo check enter 0, to bet, enter the number of chips");
			twitter.sendDirectMessage(user.getId(), message + "\nCheck or bet?\nTo check enter 0, to bet, enter the number of chips");
		}
		else{
			//System.out.println("To match the bet you need " + (currBetVal - currBet) + "chips");
			//System.out.println("Bet or Fold?\nTo fold enter 0, to bet, enter the number of chips");
			twitter.sendDirectMessage(user.getId(), message + "\nTo match the bet you need " + (currBetVal - currBet) + "chips" + "\nBet or Fold?\nTo fold enter 0, to bet, enter the number of chips");
		}
		boolean valid = false;
		while(!valid){	
			//String input = sc.nextLine();
			String input = getNewMessage();
			try{
				bet = Integer.parseInt(input);	
				if (bet == numChips){
					//System.out.println("Are you sure you want to go all in?\nEnter y for yes, any other key for no");
					twitter.sendDirectMessage(user.getId(), "Are you sure you want to go all in?\nEnter y for yes, any other key for no");
					//input = sc.nextLine();
					
					if (input.equalsIgnoreCase("y")){
						valid = true;
						allIn = true;
					}
				}
				else if(bet == 0){
					valid = true;
				}
				else if(bet > numChips){
					//System.out.println("Not enough chips!");
					//System.out.println("You have " + numChips + " chips");
					twitter.sendDirectMessage(user.getId(), "Not enough chips!\nYou have " + numChips + " chips");
				}
				else if(bet < (pot.getCurrBetVal() - currBet)){
					if(pot.getCurrBetVal() > numChips){
						//System.out.println("Not enough chips for current bet, to match you must go all in");
						twitter.sendDirectMessage(user.getId(), "Not enough chips for current bet, to match you must go all in");
					}
					else{
						//System.out.println("Invalid, the current bet is: " + pot.getCurrBetVal());
						//System.out.println("To match the bet you need " + (pot.getCurrBetVal() - currBet) + "chips");
						twitter.sendDirectMessage(user.getId(), "Invalid, the current bet is: " + pot.getCurrBetVal() + "\nTo match the bet you need " + (pot.getCurrBetVal() - currBet) + "chips");
					}
				}
				else if ((bet > pot.getCurrBetVal() - currBet) && bet < pot.getMinRaiseVal()){
					if(pot.getMinRaiseVal() > numChips){
						//System.out.println("Not enough chips for min raise, to raise you must go all in");
						twitter.sendDirectMessage(user.getId(), "Not enough chips for min raise, to raise you must go all in");
					}
					//System.out.println("Invalid: The min raise is "  + pot.getMinRaiseVal() + " chips");
					twitter.sendDirectMessage(user.getId(), "Invalid: The min raise is "  + pot.getMinRaiseVal() + " chips");
				}
				else{
					valid = true;
				}
			}
			catch (NumberFormatException e){
				//System.out.println("Invalid input, please enter an integer");
				twitter.sendDirectMessage(user.getId(), "Invalid input, please enter an integer");
				
			}
		}
		return bet;
	}
	
	public synchronized int getOpener() throws TwitterException{
		/*
		 * UNCOMMENT THIS FOR TESTING GAMEPLAY
		 * printHand();
		 */
		String message = this.getHandAndType();
		//System.out.println("Would you Like to open the hand?\"To open enter a bet, otherwise enter 0");
		message += "\nWould you Like to open the hand?\"To open enter a bet, otherwise enter 0";
		twitter.sendDirectMessage(user.getId(), message);
		int bet=0;
		boolean valid = false;
		while(!valid){	
			
			//String input = sc.nextLine();
			String input = getNewMessage();
			try{
				bet = Integer.parseInt(input);	
				if (bet == numChips){
					//System.out.println("Are you sure you want to go all in?\nEnter y for yes, any other key for no");
					twitter.sendDirectMessage(user.getId(), "Are you sure you want to go all in?\nEnter y for yes, any other key for no");
//					input = sc.nextLine();
					input = getNewMessage();
					if (input.equalsIgnoreCase("y")){
						valid = true;
						allIn = true;
					}
				}
				else if(bet == 0){
					valid = true;
				}
				else if(bet > numChips){
					//System.out.println("Not enough chips!");	
					//System.out.println("You have " + numChips + " chips");
					twitter.sendDirectMessage(user.getId(), "Not enough chips!\nYou have " + numChips + " chips");
				}
				else{
					valid = true;
				}
			}
			catch (NumberFormatException e){
				//System.out.println("Invalid input, please enter an integer");
				twitter.sendDirectMessage(user.getId(),"Invalid input, please enter an integer");
			}
			
		}
		return bet;
	}
	/*Receive latest message from the user
	 * the message will then be deleted so that it won't interfere 
	 * with later method calls
	 */
	private synchronized String getNewMessage() throws TwitterException{
		String message="";
		boolean flagCorrectMessageRecieved = false;
		
		while(!flagCorrectMessageRecieved){
			RandomDealTwitterBot.sleep();
			RandomDealTwitterBot.sleep();
			ResponseList<DirectMessage> messages = null;
			messages = twitter.getDirectMessages();
			
			
			
			for(DirectMessage m: messages){
				if(m.getSender().getId() == user.getId()){
					message = m.getText();
					flagCorrectMessageRecieved = true;
					twitter.destroyDirectMessage(m.getId());//remove the message
				}
			}
		}
		
		return message;
	}
}
