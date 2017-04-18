package poker;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;

public class ComputerPlayerCreator {
	
	private Scanner sc;
	private ArrayList<PokerPlayer> players;
	private Random rand;
	private int i;
	private File file;
	
	public ComputerPlayerCreator(int numPlayers){
		players = new ArrayList<PokerPlayer>();
		rand = new Random();
		file = new File("ListOfNames.txt");
		try {
			for(i=0;i<numPlayers;i++){
				sc = new Scanner(file);
				ArrayList<Integer> usedLines = new ArrayList<Integer>();
				for(i=0;i<numPlayers;i++){
					int pos = rand.nextInt(100);
					while(usedLines.contains(pos)){
						pos = rand.nextInt(99);
					}
					
					skipLines(pos);
					String name = sc.nextLine();
					ComputerPokerPlayer next = new ComputerPokerPlayer(name);
					players.add(next);
				}
			}
		} catch (FileNotFoundException e) {
			for(i=0;i<numPlayers;i++){
				ComputerPokerPlayer next = new ComputerPokerPlayer("comp" + i);
				players.add(next);
			}
		}
	}
	
	private void skipLines(int numLines){
		for(int i = 0;i<numLines;i++){
			sc.nextLine();
		}
	}
	
	public ArrayList<PokerPlayer> getPlayers(){
		return players;
	}
}
