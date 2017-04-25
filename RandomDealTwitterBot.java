package poker;

import twitter4j.*;
import twitter4j.auth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RandomDealTwitterBot {
	

	private Twitter twitter;
	private AccessToken aT;
	private String savedAccessToken = "854883872944910341-TYldkevNlelZDq3p52XIn50XaAoY0gL";
	private String savedAccessTokenSecret = "rjnWPusOsEXNvGavdK185v5p5p1csQwWWYwxIPGCddvYc";
	
	public RandomDealTwitterBot() throws IOException, TwitterException{
		twitter = new TwitterFactory().getInstance(); 
		twitter.setOAuthConsumer("ZgE1zJHbogbkzy2zmFnFfQLPd", "xJ588TfGrnjAMYbOAgc9TeKdhChohPVD5d4gVwN0l5cm8H2JSr");
		aT = new AccessToken(savedAccessToken, savedAccessTokenSecret);
		twitter.setOAuthAccessToken(aT);
		/*
		 * Adds a rate limit status listener so that the game never 
		 * crashes due to exceeding the rate limit
		 */
		twitter.addRateLimitStatusListener( new RateLimitStatusListener() {
		    public synchronized void onRateLimitStatus( RateLimitStatusEvent event ) {
		        System.out.println("Limit["+event.getRateLimitStatus().getLimit() + "], Remaining[" +event.getRateLimitStatus().getRemaining()+"]");
		    }

		    public synchronized void onRateLimitReached( RateLimitStatusEvent event ) {
		    	RateLimitStatus r = event.getRateLimitStatus();
		    	System.out.println("Waiting for reset...\n" + r.getSecondsUntilReset());
				long reset = r.getSecondsUntilReset();
				long currTime = System.currentTimeMillis();
				long resetPos = currTime + (reset*1000);//time until reset plus 30secs to ensure reset happens
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
		StatusUpdate su = new StatusUpdate("Fancy a game? Follow us and retweet this status to play Five Card Draw Poker! - Sent by RD Twitter Bot");
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
			List<GameOfPoker> gamesToRemove = new ArrayList<GameOfPoker>();
			for(GameOfPoker game : games){
				synchronized(game){
					boolean flagGameFinished = false;
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
					if(game.isFinished()){//test if the game is finished
						flagGameFinished = true;
						gamesToRemove.add(game);
					}
					if(games.size() > 1 && !flagGameFinished){
						//let the user know that we're dealing with a different user now
						tw.sendDirectMessage(game.getUser().getId(), "Another user is also playing, we'll be back soon!");
					}
				
				}
			}
			
			for(GameOfPoker g: gamesToRemove){//remove finished games from the list
				tw.sendDirectMessage(g.getUser().getId(), "Goodbye and thanks for playing!");
				games.remove(g);
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
		for(long i=0;i<120000000;i++);
		return;
	}
	

}