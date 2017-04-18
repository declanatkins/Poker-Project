package poker;

//import java.util.ArrayList;

public class PotOfChips {
	
	private int chips;
	private int currBetVal;
	private int minRaiseVal;
	//private ArrayList<Integer> potAvailabeToEachPlayer;
	
	public PotOfChips(){
		chips = 0;
		currBetVal = 0;
		minRaiseVal = 0;
	}
	
	public synchronized int getChips(){
		return chips;
	}
	
	public synchronized int getCurrBetVal(){
		return currBetVal;
	}
	
	public synchronized int getMinRaiseVal(){
		return minRaiseVal;
	}
	
	public synchronized int placeBet(int bet){
		chips += bet;
		return bet;
	}
	
	public synchronized void setCurrBetVal(int bet){
		currBetVal = bet;
		minRaiseVal = 2*bet;
	}
	
	public synchronized void reset(){
		chips = 0;
		currBetVal = 0;
		minRaiseVal = 0;
	}

}
