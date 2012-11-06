/*
 * Author: Cameron Dykstra
 * Email: kramin42@gmail.com
 * 
 * This is an implementation of the game Mancala in java
 * 
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY 
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 */

import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.Event;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class g extends Applet implements Runnable {

	public void start() {
		new Thread(this).start();
	}

	//int mx = 0, my = 0;
	
	boolean showNumbers = true;
	
	boolean antialiasing=true;
	boolean trailTransperancy = true;
	
	boolean mouseClick = false;
	int potClicked = 0;
	
	int potXPos[] = { 190, 255, 350, 450, 545, 610, 680, 610, 545, 450, 350, 255, 190, 120 };
	int potYPos[] = { 420, 490, 520, 520, 490, 420, 300, 180, 110,  80,  80, 110, 180, 300 };
	int potRad[] =  {  45,  45,  45,  45,  45,  45,  90,  45,  45,  45,  45,  45,  45,  90 };
	
	int numOfBtns = 9;
	int btnW = 100,btnH = 20,btnSpc=10,btnXoff=10,btnYoff=20;//offsets
	int btnJumpPos=4;
	int btnJumpAmnt=270;//split the group of buttons
	boolean btnStates[]={true,false,true,false,false,false,false,true,false};
	
	boolean redAI=true,blueAI=false;
	
	int AIEasy=1,AIStd=3,AIMod=5,AIHard=10,AIdifficulty = 3;
	
	boolean createNewGame = true;
	boolean gameStarted = false;
	boolean gameStarting = false;
	boolean startGame = false;
	boolean gameOver = false;
	int strtBtnX=300,strtBtnY=340,strtBtnW=200,strtBtnH=40;

	public void run() {
		int w = 800, h = 600;
		setSize(w, h); // For AppletViewer, remove later.

		// Set up the graphics stuff, double-buffering.
		BufferedImage screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) screen.getGraphics();
		Graphics2D appletGraphics = (Graphics2D) getGraphics();
		
		
		
		boolean gameEnding = false;
		boolean gameEnding2 = false;
		Random rand = new Random();

		// other vars
		boolean player = false;// player blue is false, player red is true
		boolean winner = false;
		boolean tie = false;
		boolean turnOver = false;
		boolean capture = false;
		boolean extraTurn = false;
		boolean areBallsReady = false;
		boolean checkGameOver=false;
		boolean potSelected=false;
		int sPot = 0;
		int capturePot = 0;
		
		//Color btnSelClr = new Color(128,255,128);
		String btnNames[] = {"Anti-Aliasing","Easy","Standard","Moderate","Hard","Resign","AI vs AI","You vs AI","2 player"};
		
		
		int size = 0;
		
		String text = "";
		
		FontMetrics fm;
		Rectangle2D rect;
		
		int gameState[] = { 4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0 };
		ArrayList<Integer> ballsInPot[] = new ArrayList[14];
		
		ArrayList<Integer> sendListSrc = new ArrayList<Integer>();
		ArrayList<Integer> sendListTrgt = new ArrayList<Integer>();
		
		double xp[] = new double[48];
		double yp[] = new double[48];
		double xv[] = new double[48];
		double yv[] = new double[48];
		int pot[] = new int[48];
		boolean trvl[] = new boolean[48];
		boolean free[] = new boolean[48];
		Color clr[] = new Color[48];
		int bRad = 1;
		
		//trail buffered images and vars
		float alphaMult = 0.92f;
		int trailNum = 25;
		int trailInterval = 1;
		float trailAlpha[] = new float[trailNum];
		float alpha = 1.0f;
		for (int i=trailNum-1;i>=0;i--){
			trailAlpha[i]=alpha;
			alpha*=alphaMult;
		}
		float defaultAlpha =0.2f;
		
		ArrayList<Integer> trailXpos[] = new ArrayList[48];
		ArrayList<Integer> trailYpos[] = new ArrayList[48];
		for (int i=0;i<48;i++){
			trailXpos[i] = new ArrayList<Integer>();
			trailYpos[i] = new ArrayList<Integer>();
		}

		// constants
		int ballspeed = 6;
		double friction = 0.97;
		double extraFriction = 0.97;
		int extraFrictionRadius = 100;
		double gravity = 0.15;

		// fonts
		Font normalFont = new Font("Ariel", Font.PLAIN, 12);
		Font largeFont = new Font("Ariel", Font.PLAIN, 60);
		Font mediumFont = new Font("Ariel", Font.PLAIN, 30);
		Font hugeFont = new Font("Ariel", Font.PLAIN, 120);
		
		//Colours
		Color clrBG = Color.black;
		Color clrLines = Color.darkGray;
		Color clrDimRed = new Color(0x66FF0000,true);
		Color clrDimBlue = new Color(0x660000FF,true);
		Color clrTextRed = Color.red;
		Color clrTextBlue = Color.blue;
		Color clrBtnBG = new Color(0x8040FF00,true);
		Color clrText = Color.white;

		// Some variables to use for the fps.
		int tick = 0, fps = 0, acc = 0;
		long lastTime = System.nanoTime();
		
		for (int i = 0; i < 14; i++) {
			ballsInPot[i] = new ArrayList<Integer>();
		}
		
		//generate the colours
		for (int i = 0; i < 48; i++) {
			int[] ballclr={0,0,0};
			ballclr[0] = rand.nextInt(255);
			ballclr[1] = rand.nextInt(255);
			ballclr[2] = rand.nextInt(255);
			while (ballclr[0]+ballclr[1]+ballclr[2]<255){//ensure bright colours
				int n = rand.nextInt(3);
				ballclr[n]+=1000;
				if (ballclr[n]>255) ballclr[n]=255;
			}
			clr[i] = new Color(ballclr[0],ballclr[1],ballclr[2]);
		}
		
		//generate random positions and velocities
		for (int i = 0; i < 48; i++) {
			free[i]=true;
			xv[i] = (rand.nextInt(800)) / 100.0 - 4;
			yv[i] = (rand.nextInt(800)) / 100.0 - 4;
			xp[i] = rand.nextInt(w);
			yp[i] = rand.nextInt(h);
		}

		// Game loop.
		while (true) {
			if (createNewGame){
				//new game code
				for (int i=0; i<14;i++){
					ballsInPot[i].clear();
					gameState[i] = 0;
				}
				for (int i = 0; i < 48; i++) {
					free[i]=true;
				}
				//gameState[6] = 0;
				//gameState[13] = 0;

				player = rand.nextBoolean();// player 1 is false, player 2 is true
				turnOver = false;
				capture = false;
				extraTurn = false;
				gameOver = false;
				gameEnding = false;
				gameEnding2=false;
				tie = false;
				createNewGame=false;
				gameStarted = false;
				gameStarting = false;
				startGame = false;
			}
			
			long now = System.nanoTime();
			acc += now - lastTime;
			tick++;
			if (acc >= 1000000000L) {
				acc -= 1000000000L;
				fps = tick;
				tick = 0;
			}

			//game update
			areBallsReady=true;
			for (int i = 0; i < 48; i++) {
				if (trvl[i]) {
					areBallsReady=false;
					break;
				}
			}
			
			//update balls
			for (int i = 0; i < 48; i++) {
				//update x position
				xp[i]+=xv[i];
				//update y position
				yp[i]+=yv[i];
				
				if (tick%trailInterval==0){
				//update trail
				trailXpos[i].add((int)xp[i]);
				trailYpos[i].add((int)yp[i]);
				while (trailXpos[i].size()>trailNum) trailXpos[i].remove(0);
				while (trailYpos[i].size()>trailNum) trailYpos[i].remove(0);
				}
				
				double dist = Math.sqrt((xp[i]-potXPos[pot[i]])*(xp[i]-potXPos[pot[i]])+(yp[i]-potYPos[pot[i]])*(yp[i]-potYPos[pot[i]]));
				
				if (!trvl[i])
				{
					if (free[i]){
						if (xp[i]>w){xp[i]=w;xv[i]=-xv[i];}
						if (xp[i]<0){xp[i]=0;xv[i]=-xv[i];}
						if (yp[i]>h){yp[i]=h;yv[i]=-yv[i];}
						if (yp[i]<0){yp[i]=0;yv[i]=-yv[i];}
					}
					else if (dist>potRad[pot[i]]-bRad)
					{
						//if (dist>potRad[pot[i]]-bRad+100) System.out.println("way out!!!: "+pot[i]);
						double unx = (xp[i]-potXPos[pot[i]])/dist;//unit norm
			    		double uny = (yp[i]-potYPos[pot[i]])/dist;
			    		double utx = -uny;//unit tangent
			    		double uty = unx;
			    		double vn = unx*xv[i]+uny*yv[i];//normal component
			    		double vt = utx*xv[i]+uty*yv[i];//tangent component
			    		vn = -vn;
			    		xv[i] = vn*unx + vt*utx;
						yv[i] = vn * uny + vt * uty;
					}
				} else {
					xv[i]*=friction;
					yv[i]*=friction;
					if (dist <potRad[pot[i]] - bRad+extraFrictionRadius){
						xv[i]*=extraFriction;
						yv[i]*=extraFriction;
					}
					xv[i]+=gravity*(potXPos[pot[i]]-xp[i])/dist;
					yv[i]+=gravity*(potYPos[pot[i]]-yp[i])/dist;
					if (dist < potRad[pot[i]] - bRad - 1) {
						trvl[i] = false;
						int count=0;
						for (int j=0; j<48; j++){
							if (!trvl[j]) count++;
						}
						//System.out.println("balls in pots: "+count);
					}
				}
			}
			
			if (!(gameEnding || gameOver) && gameStarted) {
				
				if (turnOver) {
					//System.out.println("next turn");
					player = !player;
					turnOver = false;
					checkGameOver=true;
				} else {
					if (extraTurn) {
						extraTurn = false;
						checkGameOver=true;
					} else if (capture && areBallsReady) {
						int targetPot = 13;
						if (!player) {
							targetPot = 6;
						}
						size=ballsInPot[capturePot].size();
						while (size != 0) {
							sendListSrc.add(capturePot);
							sendListTrgt.add(targetPot);
							size--;
						}
						capturePot = 12 - capturePot;
						size=ballsInPot[capturePot].size();
						while (size != 0) {
							sendListSrc.add(capturePot);
							sendListTrgt.add(targetPot);
							size--;
						}
						turnOver = true;
						capture = false;
					}
				}
			}
			
			if (btnStates[5]){
				gameOver = true;
				gameEnding = false;
				gameEnding2 = false;
				winner=true;
				checkGameOver=false;
				btnStates[5]=false;
			}
			
			if (checkGameOver){
				int a = 0;
				for (int i = 0; i < 6; i++) {
					a += gameState[i];
				}
				int b = 0;
				for (int i = 7; i < 13; i++) {
					b += gameState[i];
				}
				
				//System.out.println("checking for game end a: "+a+", b: "+b);

				if (a == 0 || b == 0) {
					//System.out.println("game ending");
					gameEnding = true;
				}
				checkGameOver=false;
			}
			
			// user play
			if (!(gameEnding || gameOver) && mouseClick && gameStarted) {
				mouseClick = false;
				if ((!blueAI && !player) || (!redAI && player)){
					//System.out.println("clicked pot " + potClicked);
					potSelected=true;
					sPot=potClicked;
				}
			}
			
			//ai play
			if (!(gameEnding || gameOver || turnOver) && ((blueAI && !player) || (redAI && player)) && areBallsReady && gameStarted){
				int plyr = -1;
				if (player){plyr=1;}
				int a=-100,b=100;
				int move=-1;
				int maxValue=-100;
				int start=0;
				if (plyr==1){start=7;}
				for (int i=start;i<start+6;i++){
					if (gameState[i]==0) {continue;}
					int childGstate[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
					for (int k=0;k<14;k++){
						childGstate[k]=gameState[k];
					}
					int num = childGstate[i];
					childGstate[i] = 0;
					int j = i;
					boolean exTurn = false;
					while (num != 0) {
						j++;
						if ((plyr == -1 && j == 13) || (plyr == 1 && j == 6)) {
							j++;
						}
						if (j > 13) {
							j -= 14;
						}
						childGstate[j]++;
						num--;
						if (num == 0) {
//							String s = "child game state: ";
//							for (int n = 0; n < 14; n++) {
//								s += childGstate[n] + " ";
//							}
//							System.out.println(s);
//							System.out.println("last ball in pot "+j);
							if ((plyr == -1 && j == 6) || (plyr == 1 && j == 13)) {// extra turn
								exTurn = true;
								//System.out.println("AI found extra turn");
							} else if (childGstate[j] == 1 && ((plyr == -1 && j < 6) || (plyr == 1 && j > 6 && j < 13))) {// capture
								childGstate[j] = 0;
								childGstate[(int) (9.5 + 3.5 * plyr)] += 1 + childGstate[12 - j];
								//System.out.println("AI capture found, sent to " + (int) (9.5 + 3.5 * plyr));
								childGstate[12 - j] = 0;
							}
						}
					}
					int val = 0;
					if (exTurn) {
						val = negamax(childGstate, AIdifficulty, a, b, plyr);
					} else {
						val = -negamax(childGstate, AIdifficulty - 1, -b, -a, -plyr);
					}
					//System.out.println("value of pot "+i+" is "+val);
					if (val>maxValue){
						maxValue=val;
						move=i;
					}
				}
				
				//System.out.println("AI move: " + move);
				potSelected=true;
				sPot=move;
			}
			
			if (potSelected){
				if (((!player && sPot < 6) || (player && sPot > 6 && sPot < 13)) && areBallsReady) {
					int currentPot = sPot;
					size = ballsInPot[sPot].size();
					while (size != 0) {
						currentPot++;
						if ((!player && currentPot == 13) || (player && currentPot == 6)) {
							currentPot++;
						}
						if (currentPot > 13) {
							currentPot -= 14;
						}
						if (size == 1) {// last ball
							//System.out.println("last ball going to pot "+currentPot);
							turnOver = true;
							if ((!player && currentPot == 6) || (player && currentPot == 13)) {
								turnOver = false;
								extraTurn = true;
								//System.out.println("extra turn");
							} else if (gameState[currentPot] == 0 && ((!player && currentPot < 6) || (player && currentPot > 6 && currentPot < 13))) {
								turnOver = false;
								capture = true;
								capturePot = currentPot;
								//System.out.println("capture");
							}
						}
						sendListSrc.add(sPot);
						sendListTrgt.add(currentPot);
						size--;
					}
				}
				potSelected=false;
			}

			if (gameEnding && areBallsReady) {
				for (int i = 0; i < 6; i++) {
					size = ballsInPot[i].size();
					while (size != 0) {
						sendListSrc.add(i);
						sendListTrgt.add(6);
						size--;
					}
				}
				for (int i = 7; i < 13; i++) {
					size = ballsInPot[i].size();
					while (size != 0) {
						sendListSrc.add(i);
						sendListTrgt.add(13);
						size--;
					}
				}
				gameEnding2=true;
			}
			
			if (startGame){
				startGame=false;
				gameStarting=true;
				//System.out.println("game starting");
				areBallsReady=false;
				gameState[6]=48;//temporarily assign them all to an main pot, they get sent to the appropriate pot in the next few lines
				for (int i=0; i<48; i++){
					ballsInPot[6].add(i);
					int p = i / 4;
					if (p >= 6) {
						p++;
					}
					//System.out.println(p);
					sendListSrc.add(6);
					sendListTrgt.add(p);
					free[i]=false;
				}
			}
			
			if (gameStarting && areBallsReady){
				//System.out.println("game started");
				gameStarting=false;
				gameStarted=true;
			}
			
			// send balls from pot to pot
			while (sendListSrc.size() > 0) {
//				if (ballsInPot[src].size() == 0) {
//					
//				}
				int src=sendListSrc.get(sendListSrc.size()-1);
				int dest=sendListTrgt.get(sendListTrgt.size()-1);
				int index = ballsInPot[src].get(ballsInPot[src].size() - 1);
				trvl[index] = true;
				int targetx = w / 2;// potXPos[dest]+rand.nextInt(20)-10;
				int targety = h / 2;// potYPos[dest]+rand.nextInt(20)-10;
				double dist = Math.sqrt((xp[index] - targetx) * (xp[index] - targetx) + (yp[index] - targety) * (yp[index] - targety));
				xv[index] = ballspeed * (targetx - xp[index]) / dist;
				yv[index] = ballspeed * (targety - yp[index]) / dist;
				pot[index]=dest;
				// System.out.println("xvel: "+balls[index].xvel+", yvel: "+balls[index].yvel);
				ballsInPot[dest].add(index);
				ballsInPot[src].remove(ballsInPot[src].size() - 1);
				gameState[src]--;
				gameState[dest]++;
				//System.out.println("sending ball "+index+" from pot "+src+" to pot "+dest);
				//System.out.println(ballsInPot[dest].size());
				sendListSrc.remove(sendListSrc.size()-1);
				sendListTrgt.remove(sendListTrgt.size()-1);
				//print game state
//				String s = "game state: ";
//				for (int i = 0; i < 14; i++) {
//					s += gameState[i] + " ";
//				}
//				System.out.println(s);
			}
			
			if (sendListSrc.size()!=sendListTrgt.size()){System.out.println("sendList error, this should not happen");}

			if (gameEnding2 && areBallsReady) {
				//System.out.println("game over");
				gameOver = true;
				gameEnding = false;
				gameEnding2 = false;
				if (gameState[6] == gameState[13]) {
					tie = true;
				} else if (gameState[6] > gameState[13]) {
					winner = false;
				} else {
					winner = true;
				}
			}

			lastTime = now;

			// Render
			
			if (antialiasing)
			{
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			}
			else
			{
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}

			// draw the background
			// g2d.drawImage(background, 0, 0, w-1, h-1, this);
			g2d.setColor(clrBG);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, defaultAlpha));
			g2d.fillRect(0, 0, w, h);
			
			
			g2d.setColor(clrText);
			g2d.setFont(normalFont);
			g2d.drawString("FPS " + String.valueOf(fps), 10, h-10);
			//g2d.drawString("Created by Cameron Dykstra", w-200, h-20);

			// temporary
			// g2d.setColor(Color.black);
			// g2d.drawOval(balls[1].potXPos-balls[1].potRad,
			// balls[1].potYPos-balls[1].potRad, 2*balls[1].potRad,
			// 2*balls[1].potRad);
			
			//draw the trails
			for (int i = 0; i < 48; i++) {
				int prevX = (int)xp[i];
				int prevY = (int)yp[i];
				for (int j = trailXpos[i].size()-1; j >=0 ; j--) {
					if (trailTransperancy){
						//g2d.setColor(new Color(clr[i].getRed(),clr[i].getGreen(),clr[i].getBlue(),(int)(255*trailAlpha[j])));
						
					} else {
						//g2d.setColor(new Color((int)(clr[i].getRed()*trailAlpha[j]),(int)(clr[i].getGreen()*trailAlpha[j]),(int)(clr[i].getBlue()*trailAlpha[j])));
					}
					g2d.setColor(clr[i]);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, trailAlpha[j]));
					g2d.drawLine(prevX, prevY, trailXpos[i].get(j), trailYpos[i].get(j));
					//g2d.draw(new Line2D.Float(prevX, prevY, trailXpos[i].get(j), trailYpos[i].get(j)));
					prevX = trailXpos[i].get(j);
					prevY = trailYpos[i].get(j);
				}
			}
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, defaultAlpha));
			

			// draw the pots
			g2d.setColor(clrLines);
			for (int i = 0; i < 14; i++) {
				if (i<7) g2d.setColor(clrDimBlue);
				else  g2d.setColor(clrDimRed);
				g2d.drawOval(potXPos[i] - potRad[i], potYPos[i] - potRad[i], 2 * potRad[i], 2 * potRad[i]);
			}

			// draw the current player text
			if (!(gameOver || gameEnding) && gameStarted) {
				if (!player) {
					g2d.setColor(clrDimBlue);
					fm   = g2d.getFontMetrics(mediumFont);
					rect = fm.getStringBounds("Blue's turn", g2d);
					g2d.setFont(mediumFont);
					g2d.drawString("Blue's turn", (int)(w/2 + 100 -rect.getWidth()/2), (int)(h/2-rect.getHeight()/2  + fm.getAscent()));  // Draw the string.
				} else {
					g2d.setColor(clrDimRed);
					fm   = g2d.getFontMetrics(mediumFont);
					rect = fm.getStringBounds("Red's turn", g2d);
					g2d.setFont(mediumFont);
					g2d.drawString("Red's turn", (int)(w/2 - 100 -rect.getWidth()/2), (int)(h/2-rect.getHeight()/2  + fm.getAscent()));
				}
			}
			
			//draw the number of captured balls
			g2d.setColor(clrDimRed);
			text=""+gameState[13];
			fm   = g2d.getFontMetrics(hugeFont);
			rect = fm.getStringBounds(text, g2d);
			g2d.setFont(hugeFont);
			g2d.drawString(text, (int)(potXPos[13]-rect.getWidth()/2), (int)(potYPos[13]-rect.getHeight()/2  + fm.getAscent()));
			
			g2d.setColor(clrDimBlue);
			text=""+gameState[6];
			fm   = g2d.getFontMetrics(hugeFont);
			rect = fm.getStringBounds(text, g2d);
			g2d.setFont(hugeFont);
			g2d.drawString(text, (int)(potXPos[6]-rect.getWidth()/2), (int)(potYPos[6]-rect.getHeight()/2  + fm.getAscent()));
			
			//draw the number of balls in each pot
			if (showNumbers){
				g2d.setColor(clrDimBlue);
				for (int i=0;i<6;i++){
					text = ""+gameState[i];
					fm   = g2d.getFontMetrics(largeFont);
					rect = fm.getStringBounds(text, g2d);
					g2d.setFont(largeFont);
					g2d.drawString(text, (int)(potXPos[i]-rect.getWidth()/2), (int)(potYPos[i]-rect.getHeight()/2  + fm.getAscent()));
				}
				g2d.setColor(clrDimRed);
				for (int i=7;i<13;i++){
					text = ""+gameState[i];
					fm   = g2d.getFontMetrics(largeFont);
					rect = fm.getStringBounds(text, g2d);
					g2d.setFont(largeFont);
					g2d.drawString(text, (int)(potXPos[i]-rect.getWidth()/2), (int)(potYPos[i]-rect.getHeight()/2  + fm.getAscent()));
				}
			}
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			//draw the balls
			for (int i = 0; i < 48; i++) {
				g2d.setColor(clr[i]);
				g2d.fillOval((int)xp[i]-bRad, (int)yp[i]-bRad, 2*bRad, 2*bRad);
			}
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, defaultAlpha));
			
			//draw the buttons
			g2d.setColor(clrBtnBG);
			for (int i=0;i<numOfBtns;i++){
				if (i==0) continue;//do not draw antialias button
				if (btnStates[i]){
					g2d.fillRect(btnXoff, btnYoff+i*btnH+i*btnSpc+(i>btnJumpPos ? btnJumpAmnt : 0), btnW, btnH);
				}
			}
			g2d.setColor(clrLines);
			for (int i=0;i<numOfBtns;i++){
				if (i==0) continue;//do not draw antialias button
				if (i==0 || (i!=5 && !gameStarted) || (i==5 && gameStarted) || btnStates[i]){
					g2d.drawRect(btnXoff, btnYoff+i*btnH+i*btnSpc+(i>btnJumpPos ? btnJumpAmnt : 0), btnW, btnH);
				}
			}
			g2d.setColor(clrText);
			for (int i=0;i<numOfBtns;i++){
				if (i==0) continue;//do not draw antialias button
				if (i==0 || (i!=5 && !gameStarted) || (i==5 && gameStarted) || btnStates[i]){
					text = btnNames[i];
					fm   = g2d.getFontMetrics(normalFont);
					rect = fm.getStringBounds(text, g2d);
					g2d.setFont(normalFont);
					g2d.drawString(text, (int)(btnXoff+btnW/2 - rect.getWidth()/2), (int)(btnYoff+i*(btnH+btnSpc)+(i>btnJumpPos ? btnJumpAmnt : 0)+btnH/2 - rect.getHeight()/2 + fm.getAscent()));
				}
			}
			
			//draw the startgame and newgame buttons
			if (!startGame && !gameStarting && !gameStarted){
				g2d.setColor(clrLines);
				g2d.drawRect(strtBtnX, strtBtnY, strtBtnW, strtBtnH);
				g2d.setColor(clrText);
				text = "Start Game";
				fm   = g2d.getFontMetrics(mediumFont);
				rect = fm.getStringBounds(text, g2d);
				g2d.setFont(mediumFont);
				g2d.drawString(text, (int)(strtBtnX+strtBtnW/2 - rect.getWidth()/2), (int)(strtBtnY+strtBtnH/2 - rect.getHeight()/2 + fm.getAscent()));
			} else if (gameOver){
				g2d.setColor(clrLines);
				g2d.drawRect(strtBtnX, strtBtnY, strtBtnW, strtBtnH);
				g2d.setColor(clrText);
				text = "New Game";
				fm   = g2d.getFontMetrics(mediumFont);
				rect = fm.getStringBounds(text, g2d);
				g2d.setFont(mediumFont);
				g2d.drawString(text, (int)(strtBtnX+strtBtnW/2 - rect.getWidth()/2), (int)(strtBtnY+strtBtnH/2 - rect.getHeight()/2 + fm.getAscent()));
			}
			

			// draw the winner screen
			if (gameOver) {
				g2d.setColor(Color.black);
				text = "Tie";
				if (!tie && !winner){
					g2d.setColor(clrTextBlue);
					text = "Blue Wins!";
				} else if (!tie && winner){
					g2d.setColor(clrTextRed);
					text = "Red Wins!";
				}
				fm   = g2d.getFontMetrics(largeFont);
				rect = fm.getStringBounds(text, g2d);
				g2d.setFont(largeFont);
				g2d.drawString(text, (int)(w/2-rect.getWidth()/2), (int)(h/2-rect.getHeight()/2  + fm.getAscent()));
			}
			
			//draw the trails
//			for (int i=0; i<trailBIs.size();i++){
//				float[] factors = new float[] {
//						trailAlphas[i], trailAlphas[i], trailAlphas[i]
//					};
//					float[] offsets = new float[] {
//					    0.0f, 0.0f, 0.0f
//					};
//				RescaleOp op = new RescaleOp(factors,offsets,null);
//				g.drawImage(trailBIs.get(i), op, 0, 0);
//			}
			
//			float[] factors = new float[] {
//					1.0f,1.0f,1.0f,alphaMult
//			};
//			float[] offsets = new float[] {
//					0.0f, 0.0f, 0.0f, 0.0f
//			};
//			RescaleOp op = new RescaleOp(factors,offsets,null);
//			op.filter(screen, screen);
//			
//			factors = new float[] {
//					1.0f,1.0f,1.0f,1.0f
//			};
//			offsets = new float[] {
//					0.0f, 0.0f, 0.0f,0.0f
//			};
//			op = new RescaleOp(factors,offsets,null);
			
			
			//draw the new screen to the old screen
			//g.drawImage(newScreen, null, 0, 0);

			// Draw the entire results on the screen.
			appletGraphics.drawImage(screen, 0, 0, null);

			do {
				Thread.yield();
			} while (System.nanoTime() - lastTime < 16000000L);

			if (!isActive()) {
				return;
			}
		}
	}

	public boolean handleEvent(Event e) {
		if (e.id == Event.MOUSE_UP){
			// mouse button released
			//mx = e.x;
			//my = e.y;
			for (int i = 0; i < 14; i++) {
				if ((e.x - potXPos[i]) * (e.x - potXPos[i]) + (e.y - potYPos[i]) * (e.y - potYPos[i]) < potRad[i] * potRad[i]) {
					potClicked = i;
					mouseClick = true;
					break;
				}
			}
			if (e.x>strtBtnX && e.x<strtBtnX+strtBtnW && e.y>strtBtnY && e.y<strtBtnY+strtBtnH){
				if (gameOver){
					createNewGame = true;
				}
				if (!gameStarted){
					startGame = true;
				}
			}
			for (int i=0;i<numOfBtns;i++){
				if (i!=0 && i!=5 && gameStarted){continue;}
				if (i==5 && !gameStarted){continue;}
				if (e.x>btnXoff && e.x<btnXoff+btnW && e.y>btnYoff+i*btnH+i*btnSpc+(i>btnJumpPos ? btnJumpAmnt : 0) && e.y<btnYoff+(i+1)*btnH+i*btnSpc+(i>btnJumpPos ? btnJumpAmnt : 0)){
					btnStates[i]=!btnStates[i];
					switch (i) {
					case 0://disabled
						//antialiasing=!antialiasing;
						//trailTransperancy=!trailTransperancy;
						break;
					case 1:
						AIdifficulty=AIEasy;
						break;
					case 2:
						AIdifficulty=AIStd;
						break;
					case 3:
						AIdifficulty=AIMod;
						break;
					case 4:
						AIdifficulty=AIHard;
						break;
					case 5:
						btnStates[5]=true;
						break;
					case 6:
						redAI=true;
						blueAI=true;
						break;
					case 7:
						redAI=true;
						blueAI=false;
						break;
					case 8:
						redAI=false;
						blueAI=false;
						break;
					}
					if (i>0 && i<5){
						btnStates[1]=false;
						btnStates[2]=false;
						btnStates[3]=false;
						btnStates[4]=false;
						btnStates[i]=true;
					}
					if (i>5 && i<9){
						btnStates[6]=false;
						btnStates[7]=false;
						btnStates[8]=false;
						btnStates[i]=true;
					}
					//System.out.println("AI difficulty is: "+AIdifficulty);
				}
			}
		}
		return false;
	}
	
	public int negamax(int[] gstate,int depth, int a, int b, int plyr)
	{
		if (depth == 0 || gstate[0]+gstate[1]+gstate[2]+gstate[3]+gstate[4]+gstate[5]==0 || gstate[7]+gstate[8]+gstate[9]+gstate[10]+gstate[11]+gstate[12]==0)
		{
			//System.out.println("heuristic: "+plyr*(gstate[7]+gstate[8]+gstate[9]+gstate[10]+gstate[11]+gstate[12]+2*gstate[13]-(gstate[0]+gstate[1]+gstate[2]+gstate[3]+gstate[4]+gstate[5]+2*gstate[6])));
			return plyr*(gstate[7]+gstate[8]+gstate[9]+gstate[10]+gstate[11]+gstate[12]+2*gstate[13]-(gstate[0]+gstate[1]+gstate[2]+gstate[3]+gstate[4]+gstate[5]+2*gstate[6]));
		} else {
			int start = 0;
			if (plyr==1){start=7;}
			for (int i=start;i<start+6;i++){
				if (gstate[i]==0) {continue;}
				int childGstate[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
				for (int k=0;k<14;k++){
					childGstate[k]=gstate[k];
				}
				int num = childGstate[i];
				childGstate[i]=0;
				int j = i;
				boolean extraTurn = false;
				while(num!=0){
					j++;
					if ((plyr==-1 && j==13) || (plyr==1 && j==6)){
						j++;
					}
					if (j>13){j-=14;}
					childGstate[j]++;
					num--;
					if (num==0){
						if ((plyr==-1 && j==6) || (plyr==1 && j==13)){//extra turn
							extraTurn = true;
						} else if (childGstate[j]==1 && ((plyr==-1 && j < 6) || (plyr==1 && j > 6 && j < 13))){//capture
							childGstate[j]=0;
							childGstate[(int)(9.5+3.5*plyr)]+=1+childGstate[12-j];
							//System.out.println("AI capture sent to "+(int)(9.5+3.5*plyr));
							childGstate[12-j]=0;
						}
					}
				}
				int val = 0;
				if (extraTurn){
					val = negamax(childGstate,depth,a,b,plyr);
				} else {
					val = -negamax(childGstate,depth-1,-b,-a,-plyr);
				}
				if (val>=b){
					return val;
				}
				if (val>=a){
					a=val;
				}
			}
			return a;
		}
	}
}