package poker;
//This class  is used to test the other classes
import java.util.Scanner;

public class Test {

	public static synchronized void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Welcome to Random Deal's Five Card Draw Poker Game!");
		System.out.println("Enter your Name to begin:");
		String name = sc.nextLine();
		HumanPokerPlayer h = new HumanPokerPlayer(name);
		GameOfPoker game = new GameOfPoker(2, h);
		int carry = 0;
		game.dealerPosition = 0;
		
		while(game.players.size() > 1 && game.players.contains(h)){ // decide on condition to end game 1 players left or human quit?
			game.dealPhase(game.dealerPosition);
			game.addCarriedChips(carry);
			carry = game.openingPhase(); //if 0 no carry if not amount to carry 
			if(carry == 0){
				if(!game.testHandFinished()){
					game.discardPhase();
					game.finalBetPhase();
					if(!game.testHandFinished()){
						game.showDown();
					}
				}
				game.endOfHand();
			}
			game.dealerPosition++;
			if(game.dealerPosition > game.players.size()-1){
				game.dealerPosition = 0;
			}
		}
		
		sc.close();
	}
}
