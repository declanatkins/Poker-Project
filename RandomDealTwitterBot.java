package poker;

import twitter4j.*;
import twitter4j.auth.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RandomDealTwitterBot {
	

	private Twitter twitter;
	private Scanner sc;
	private AccessToken aT;
	
	public RandomDealTwitterBot() throws IOException, TwitterException{
		twitter = new TwitterFactory().getInstance(); 
		sc = new Scanner(System.in);
		twitter.setOAuthConsumer("ZgE1zJHbogbkzy2zmFnFfQLPd", "xJ588TfGrnjAMYbOAgc9TeKdhChohPVD5d4gVwN0l5cm8H2JSr");
		RequestToken rT = twitter.getOAuthRequestToken();
		/*
		 * This part has to be done through the console at our end, so that the twitterbot can
		 * start working
		 * 
		 * This is the only part of the program that requires console input
		 */
		while (null == aT) {
			System.out.println("Open the following URL and grant access to your account:");
			System.out.println(rT.getAuthorizationURL());
			System.out.print("Enter the PIN:");
			String pin = sc.nextLine();
			try{
				if(pin.length() > 0){
					aT = twitter.getOAuthAccessToken(rT, pin);
				}else{
					aT = twitter.getOAuthAccessToken();
				}
			} catch (TwitterException te) {
				if(401 == te.getStatusCode()){
					System.out.println("Unable to get the access token.");
				}else{
					te.printStackTrace();
				}
			}
		}
		/*
		 * Adds a rate limit status listener so that the game never 
		 * crashes due to exceeding the rate limit
		 */
		twitter.addRateLimitStatusListener( new RateLimitStatusListener() {
		    public void onRateLimitStatus( RateLimitStatusEvent event ) {
		        System.out.println("Limit["+event.getRateLimitStatus().getLimit() + "], Remaining[" +event.getRateLimitStatus().getRemaining()+"]");
		    }

		    public void onRateLimitReached( RateLimitStatusEvent event ) {
		    	RateLimitStatus r = event.getRateLimitStatus();
		    	System.out.println("Waiting for reset...");
				long reset = r.getSecondsUntilReset();
				long currTime = System.currentTimeMillis();
				long resetPos = currTime + (reset*1000) + 30000;//time until reset plus 30secs to ensure reset happens
				while(currTime != resetPos){
					currTime = System.currentTimeMillis();
				}
		    }
		} );
	}
	
	public Twitter getTwitter(){
		return twitter;
	}
	
	public synchronized static void main(String[] args) throws IOException, TwitterException, InterruptedException{
		
		RandomDealTwitterBot twitterBot = new RandomDealTwitterBot();
		List<User> listUsers = new ArrayList<User>();
		boolean flagGameStart = false;
		List<GameOfPoker> games = new ArrayList<GameOfPoker>();
		Twitter tw = twitterBot.getTwitter();
		ResponseList<Status> statuses = tw.getUserTimeline();
		
		for(Status s: statuses){//remove last status update to prevent duplications error
			tw.destroyStatus(s.getId());	
		}
		StatusUpdate su = new StatusUpdate("Fancy a game? Follow us and retweet this status for some cards, some fun and maybe more ;) - Sent by RD Twitter Bot");
		tw.updateStatus(su);
		
		/*
		 * run this until someone retweets to start a game
		 */
		while(!flagGameStart){
			ResponseList<Status> statuses2 = tw.getUserTimeline();
			if(!statuses2.isEmpty()){
				Status s = statuses2.get(0);
				if(s.getRetweetCount() > 0){
					flagGameStart = true;
					//get the users that have retweeted
					IDs ids =  tw.getRetweeterIds(s.getId(), -1);
					long[] idArr = ids.getIDs();
					System.out.println(idArr.length);
					for(int i=0;i<idArr.length;i++){
						User u = tw.showUser(idArr[i]);
						listUsers.add(u);
						System.out.println(u.getName());
						HumanPokerPlayer h = new HumanPokerPlayer(u,tw);
						games.add(new GameOfPoker(2,h,u, tw));
					}	
				}	
				else{
					RandomDealTwitterBot.sleep();
				}
			}
		}
		for(GameOfPoker g : games){
			User u = g.getUser();
			tw.sendDirectMessage(u.getId(), "Welcome to Random Deal's Five Card Draw Poker Game!");
			for(PokerPlayer p : g.players){
				if(!p.isHuman){
					tw.sendDirectMessage(u.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(6));
				}
			}
		}
		
		
		while(!games.isEmpty()){
			int carry=0;
			for(GameOfPoker game : games){
				synchronized(game){
					game.dealPhase(game.dealerPosition);
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
					game.addCarriedChips(carry);
					
					if(games.size() > 1){
						//let the user know that we're dealing with a different user now
						tw.sendDirectMessage(game.getUser().getId(), "Another user is also playing, we'll be back soon!");
					}
				
				}
			}
			//here we will check if any other users have retweeted the status. If they have
			//we can also create them and add them to the list of games
			
			statuses = tw.getUserTimeline();
			List<GameOfPoker> newGames = new ArrayList<GameOfPoker>();
			if(!statuses.isEmpty()){
				Status s = statuses.get(0);
				if(s.getRetweetCount() > 0){
					//get the users that have retweeted
					IDs ids =  tw.getRetweeterIds(s.getId(), -1);
					long[] idArr = ids.getIDs();
					System.out.println(idArr.length);
					for(int i=0;i<idArr.length;i++){
						User u = tw.showUser(idArr[i]);
						if(!listUsers.contains(u)){
							listUsers.add(u);
							System.out.println(u.getName());
							HumanPokerPlayer h = new HumanPokerPlayer(u,tw);
							GameOfPoker g = new GameOfPoker(2,h,u, tw);
							games.add(g);
							newGames.add(g);
						}
					}	
				}	
			}
			for(GameOfPoker g : newGames){
				User u = g.getUser();
				tw.sendDirectMessage(u.getId(), "Welcome to Random Deal's Five Card Draw Poker Game!");
				for(PokerPlayer p : g.players){
					if(!p.isHuman){
						tw.sendDirectMessage(u.getId(), p.getName() + ": " + ((ComputerPokerPlayer)p).getChat(6));
					}
				}
			}
		}
		
	}
	
	public static void sleep(){//runs this loop to prevent to many requests being made
		for(long i=0;i<60000000;i++);
		return;
	}
	

}