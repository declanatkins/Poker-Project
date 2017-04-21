package poker;

import twitter4j.*;
import twitter4j.auth.*;
//import twitter4j.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		 * all other parts of the program should require no further input from us
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
	}
	
	public Twitter getTwitter(){
		return twitter;
	}
	
	public synchronized static void main(String[] args) throws IOException, TwitterException, InterruptedException{
		
		RandomDealTwitterBot twitterBot = new RandomDealTwitterBot();
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
					System.out.println("!!");
					flagGameStart = true;
					//get the users that have retweeted
					IDs ids =  tw.getRetweeterIds(s.getId(), -1);
					long[] idArr = ids.getIDs();
					for(int i=0;i<idArr.length;i++){
						User u = tw.showUser(idArr[i]);
						System.out.println(u.getName());
						HumanPokerPlayer h = new HumanPokerPlayer(u,tw);
						games.add(new GameOfPoker(2,h,u));
					}	
				}	
				else{
					twitterBot.sleep();
				}
			}
		}
		System.out.println("!");
		for(GameOfPoker g : games){
			User u = g.getUser();
			tw.sendDirectMessage(u.getId(), "Welcome to Random Deal's Five Card Draw Poker Game!");
		
		}
		
		/*
		while(!games.isEmpty()){
			for(GameOfPoker g : games){
				
			}
		}*/
		
	}
	
	private void sleep(){//runs this loop to prevent to many requests being made
		for(long i=0;i<10000000;i++);
	}
}
