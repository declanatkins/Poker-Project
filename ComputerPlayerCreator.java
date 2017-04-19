package poker;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;

public class ComputerPlayerCreator {
	
	private ArrayList<PokerPlayer> players;
	private Random rand;
	private int i;
	
	public ComputerPlayerCreator(int numPlayers){
		players = new ArrayList<PokerPlayer>();
		rand = new Random();
		for(i=0;i<numPlayers;i++){
			try {
				File file = new File("ListOfNames.txt");
				Scanner sc = new Scanner(file);
				ArrayList<Integer> usedLines = new ArrayList<Integer>();
				for(i=0;i<numPlayers;i++){
					int pos = rand.nextInt(100);
					while(usedLines.contains(pos)){
						pos = rand.nextInt(100);
					}
					System.out.println(pos);
					String name;
					try{
						skipLines(pos, sc);
						name = sc.nextLine();
					}
					catch (NoSuchElementException e){
						File file2 = new File("ListOfNames.txt");
						Scanner sc2 = new Scanner(file2);
						skipLines(pos, sc2);
						name = sc2.nextLine(); 
						sc2.close();
					}
					ComputerPokerPlayer next = new ComputerPokerPlayer(name);
					players.add(next);
				}
				sc.close();
			} catch (FileNotFoundException e) {
				ComputerPokerPlayer next = new ComputerPokerPlayer("comp" + i);
				players.add(next);
			}
		}
	}
	
	private void skipLines(int numLines, Scanner sc){
		for(int i = 0;i<numLines;i++){
			sc.nextLine();
		}
	}
	
	public ArrayList<PokerPlayer> getPlayers(){
		return players;
	}
}
