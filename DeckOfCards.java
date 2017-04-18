package poker;
import java.util.Random;

public class DeckOfCards {
	
	
	
	private static final int DECK_SIZE = 52;
	private static final int SUIT_SIZE = 13;
	private static final int NUM_SUITS = 4;
	private PlayingCard[] deck;
	private PlayingCard[] discardPile;//unsure as to why we should be returning discarded cards to the end of the deck
									//I don't see how it helps, and really can only cause issues
									//Therefore discarded cards will be stored in this array
	private int deckPos;
	private int discardPos;
	
	public DeckOfCards(){//constructor to create the deck
		deckPos = 0;
		discardPos = 0;
		discardPile = new PlayingCard[DECK_SIZE];
		deck = new PlayingCard[DECK_SIZE];//creates deck of cards
		
		for(int i=0;i<NUM_SUITS;i++){//loop for the suits
			for(Integer j=1;j<=SUIT_SIZE;j++){//loop for the card number
				switch (j){//switch to create the object with A for ace and other specific exceptions
				case 1:
					deck[i*SUIT_SIZE + j-1] = new PlayingCard (PlayingCard.SUITS[i], 14, "A");//for ace
					break;
				case 11:
					deck[i*SUIT_SIZE + j-1] = new PlayingCard (PlayingCard.SUITS[i], j, "J");//for jack
					break;
				case 12:
					deck[i*SUIT_SIZE + j-1] = new PlayingCard (PlayingCard.SUITS[i], j, "Q");//for queen
					break;
				case 13:
					deck[i*SUIT_SIZE + j -1] = new PlayingCard (PlayingCard.SUITS[i], j, "K");//for king
					break;
				default:
					deck[i*SUIT_SIZE + j-1] = new PlayingCard (PlayingCard.SUITS[i], j, j.toString());//for all other cards
					//use of Integer as opposed to int was for the toString() method
					//needed to deal with case of j=10
				}
			}		
		}
		
		shuffle();//shuffle the deck at the start
	}
	
	private synchronized void shuffle(){//swaps cards at 2 random positions until the deck is shuffled 
		//private as it should only be accessed at specific times
		Random rand = new Random();
		for(int i=0;i<DECK_SIZE*DECK_SIZE;i++){
			int j = rand.nextInt(DECK_SIZE);
			int k = rand.nextInt(DECK_SIZE);
			PlayingCard tmp = deck[j];
			deck[j] = deck[k];
			deck[k] = tmp;
		}
		
		return;
	}
	public synchronized HandOfCards dealCards(){
		HandOfCards hand = new HandOfCards(this);
		return hand;
	}
	
	public synchronized PlayingCard dealNext(){
		if(deckPos>=DECK_SIZE){
			System.out.println("Error, deck out of cards\nResetting Deck");//in the event that the deck runs out of cards, reset deck and return null
			resetDeck();
			return null;
		}
		return deck[deckPos++];//move the pointer forwards
	}
	
	public synchronized void resetDeck(){//set the pointer to the first element of the array and shuffle the deck
		deckPos = 0;
		shuffle();
		return;
	}
	
	public synchronized void returnCard(PlayingCard discarded){//places returned cards in the discard pile
		discardPile[discardPos++] = discarded;
		return;
	}
	
	public HandOfCards giveNewCards(HandOfCards hand, int numCards){
		for(int i=0;i<numCards;i++){
			hand.getNewCard(this.dealNext());
		}
		
		return hand;
	}

}
