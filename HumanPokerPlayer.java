package poker;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class HumanPokerPlayer extends PokerPlayer{
	
	private Twitter twitter;
	private User user;

	public HumanPokerPlayer(User u, Twitter t) {
		super(u.getName());
		user = u;
		twitter = t;
		isHuman = true;
	}
	
	
	
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
		 
		message = this.getHandAndType();
		twitter.sendDirectMessage(user.getId(), message);
		for(int i=0;i<HandOfCards.HAND_SIZE;i++){
			if (n<3 && hand.getDiscardProbability(i) == 100){
				suggestCards.add(hand.getCardAtPos(i));
				n++;
			}
		}
		
		twitter.sendDirectMessage(user.getId(), name + " it's your move!");
		if(n > 0){
			twitter.sendDirectMessage(user.getId(), "Recommend you discard the following cards:");
			for(int i=0;i<n;i++){
				System.out.println(suggestCards.get(i) + ", at position " + hand.getHand().indexOf(suggestCards.get(i)));
				twitter.sendDirectMessage(user.getId(), suggestCards.get(i) + ", at position " + hand.getHand().indexOf(suggestCards.get(i)));
			}
		}
		else{
			twitter.sendDirectMessage(user.getId(), "Recommend you dont discard any cards.");
		}
			
		twitter.sendDirectMessage(user.getId(),"Please enter the cards that you would like to discard by position (0-4):\nYou can discard up to 3 cards\nEnter each index one at a time and then send a message with just a \'.\'");
		
		String input= "";
		while (!input.equals(".")){
			input = getNewMessage();
			try{
				Integer index = Integer.parseInt(input);
				if(numDiscarded < 3){
					if(index <= 4 && index >= 0){
						if(chosenCards.contains(hand.getCardAtPos(index))){
							twitter.sendDirectMessage(user.getId(), "You've already entered that card\nPlease enter again");
						}
						else{
							chosenCards.add(hand.getCardAtPos(index));
							numDiscarded++;
							twitter.sendDirectMessage(user.getId(), "Discarding " + hand.getCardAtPos(index));
						}
					}
					else{
						twitter.sendDirectMessage(user.getId(), "Invalid input, index must be between 0 and 4 inclusive\nPlease enter again");
					}
				}
				else{
					twitter.sendDirectMessage(user.getId(), "You have already selected 3 cards to discard\nWould you like to cancel and select new cards? (Enter y/n)");
					input = getNewMessage();
					
					while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")){
						twitter.sendDirectMessage(user.getId(), "You must enter y/n");
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
		String message = getHandAndType();
		if(currBetVal == 0){
			twitter.sendDirectMessage(user.getId(), message + "\nCheck or bet?\nTo check enter 0, to bet, enter the number of chips");
		}
		else{
			twitter.sendDirectMessage(user.getId(), message + "\nTo match the bet you need " + (currBetVal - currBet) + "chips" + "\nBet or Fold?\nTo fold enter 0, to bet, enter the number of chips");
		}
		boolean valid = false;
		while(!valid){	
			String input = getNewMessage();
			try{
				bet = Integer.parseInt(input);	
				if (bet == numChips){
					twitter.sendDirectMessage(user.getId(), "Are you sure you want to go all in?\nEnter y for yes, any other key for no");
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
					twitter.sendDirectMessage(user.getId(), "Not enough chips!\nYou have " + numChips + " chips");
				}
				else if(bet < (pot.getCurrBetVal() - currBet)){
					if(pot.getCurrBetVal() > numChips){
						twitter.sendDirectMessage(user.getId(), "Not enough chips for current bet, to match you must go all in");
					}
					else{
						twitter.sendDirectMessage(user.getId(), "Invalid, the current bet is: " + pot.getCurrBetVal() + "\nTo match the bet you need " + (pot.getCurrBetVal() - currBet) + "chips");
					}
				}
				else if ((bet > pot.getCurrBetVal() - currBet) && bet < pot.getMinRaiseVal()){
					if(pot.getMinRaiseVal() > numChips){
						twitter.sendDirectMessage(user.getId(), "Not enough chips for min raise, to raise you must go all in");
					}
					twitter.sendDirectMessage(user.getId(), "Invalid: The min raise is "  + pot.getMinRaiseVal() + " chips");
				}
				else{
					valid = true;
				}
			}
			catch (NumberFormatException e){
				twitter.sendDirectMessage(user.getId(), "Invalid input, please enter an integer");
				
			}
		}
		return bet;
	}
	
	public synchronized int getOpener() throws TwitterException{

		String message = this.getHandAndType();
		message += "\nWould you Like to open the hand?\"To open enter a bet, otherwise enter 0";
		twitter.sendDirectMessage(user.getId(), message);
		int bet=0;
		boolean valid = false;
		while(!valid){	
			String input = getNewMessage();
			try{
				bet = Integer.parseInt(input);	
				if (bet == numChips){
					System.out.println("Are you sure you want to go all in?\nEnter y for yes, any other key for no");
					twitter.sendDirectMessage(user.getId(), "Are you sure you want to go all in?\nEnter y for yes, any other key for no");
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
					twitter.sendDirectMessage(user.getId(), "Not enough chips!\nYou have " + numChips + " chips");
				}
				else{
					valid = true;
				}
			}
			catch (NumberFormatException e){
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
