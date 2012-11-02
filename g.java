import java.applet.Applet;
import java.awt.Event;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class g extends Applet implements Runnable {

	public void start() {
		new Thread(this).start();
	}

	int mx = 0, my = 0;
	
	boolean showNumbers = true;
	
	boolean antialiasing=true;
	
	boolean mouseClick = false;
	int potClicked = 0;
	
	int potXPos[] = { 300, 400, 525, 675, 800, 900, 1050, 900, 800, 675, 525, 400, 300, 150 };
	int potYPos[] = { 530, 600, 650, 650, 600, 530, 400, 270, 200, 150, 150, 200, 270, 400 };
	int potRad[] = { 50, 50, 50, 50, 50, 50, 100, 50, 50, 50, 50, 50, 50, 100 };

	public void run() {
		int w = 1200, h = 800;
		setSize(w, h); // For AppletViewer, remove later.

		// Set up the graphics stuff, double-buffering.
		BufferedImage screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) screen.getGraphics();
		Graphics2D appletGraphics = (Graphics2D) getGraphics();

		boolean gameOver = false;
		boolean gameEnding = false;
		boolean gameEnding2 = false;
		boolean createNewGame = true;
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
		
		int size = 0;
		
		String text = "";

		int AIdifficulty = 3;
		
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
		Color clr[] = new Color[48];
		int bRad = 5;

		// constants
		int ballspeed = 4;
		double friction = 0.985;
		double extraFriction = 0.90;
		double gravity = 0.1;

		// fonts
		Font normalFont = new Font("Ariel", Font.PLAIN, 12);
		Font largeFont = new Font("Ariel", Font.PLAIN, 60);
		Font mediumFont = new Font("Ariel", Font.PLAIN, 30);
		Font hugeFont = new Font("Ariel", Font.PLAIN, 120);

		// Some variables to use for the fps.
		int tick = 0, fps = 0, acc = 0;
		long lastTime = System.nanoTime();
		
		for (int i = 0; i < 14; i++) {
			ballsInPot[i] = new ArrayList<Integer>();
		}

		// Game loop.
		while (true) {
			if (createNewGame){
				//new game code
				for (int i = 0; i < 48; i++) {
					clr[i] = new Color(rand.nextInt(200), rand.nextInt(200), rand.nextInt(200));
					// balls[i].travelling=true;
					xv[i] = (rand.nextInt(200)) / 100.0 - 1;
					yv[i] = (rand.nextInt(200)) / 100.0 - 1;
					int p = i / 4;
					if (p >= 6) {
						p++;
					}
					pot[i]=p;
					xp[i] = potXPos[p] + rand.nextInt(20) - 10;
					yp[i] = potYPos[p] + rand.nextInt(20) - 10;
					ballsInPot[p].add(i);
					gameState[p] = 4;
				}
				gameState[6] = 0;
				gameState[13] = 0;

				player = rand.nextBoolean();// player 1 is false, player 2 is true
				turnOver = false;
				capture = false;
				extraTurn = false;
				gameOver = false;
				gameEnding = false;
				gameEnding2=false;
				tie = false;
				createNewGame=false;
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
				
				double dist = Math.sqrt((xp[i]-potXPos[pot[i]])*(xp[i]-potXPos[pot[i]])+(yp[i]-potYPos[pot[i]])*(yp[i]-potYPos[pot[i]]));
				
				if (!trvl[i])
				{
					if (dist>potRad[pot[i]]-bRad)
					{
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
					if (dist <potRad[pot[i]] - bRad+50){
						xv[i]*=extraFriction;
						yv[i]*=extraFriction;
					}
					xv[i]+=gravity*(potXPos[pot[i]]-xp[i])/dist;
					yv[i]+=gravity*(potYPos[pot[i]]-yp[i])/dist;
					if (dist < potRad[pot[i]] - bRad - 1) {
						trvl[i] = false;
					}
				}
			}
			
			if (!(gameEnding || gameOver)) {
				
				if (turnOver) {
					System.out.println("next turn");
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
					System.out.println("game ending");
					gameEnding = true;
				}
				checkGameOver=false;
			}
			
			// user play
			if (!(gameEnding || gameOver) && mouseClick) {
				mouseClick = false;
				if (!player){
					System.out.println("clicked pot " + potClicked);
					potSelected=true;
					sPot=potClicked;
				}
			}
			
			//ai play
			if (!(gameEnding || gameOver || turnOver) && player && areBallsReady){
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
				
				System.out.println("AI move: " + move);
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
								System.out.println("extra turn");
							} else if (gameState[currentPot] == 0 && ((!player && currentPot < 6) || (player && currentPot > 6 && currentPot < 13))) {
								turnOver = false;
								capture = true;
								capturePot = currentPot;
								System.out.println("capture");
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
				//System.out.println("sending ball from pot "+src+" to pot "+dest);
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
				System.out.println("game over");
				gameOver = true;
				gameEnding = false;
				gameEnding2 = false;
				if (gameState[6] == gameState[13]) {
					tie = true;
				} else if (gameState[6] > gameState[13]) {
					winner = true;
				} else {
					winner = false;
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
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, w, h);
			
			g2d.setColor(Color.black);
			g2d.setFont(normalFont);
			g2d.drawString("FPS " + String.valueOf(fps), 20, 30);

			for (int i = 0; i < 48; i++) {
				g2d.setColor(clr[i]);
				g2d.fillOval((int)xp[i]-bRad, (int)yp[i]-bRad, 2*bRad, 2*bRad);
			}

			// temporary
			// g2d.setColor(Color.black);
			// g2d.drawOval(balls[1].potXPos-balls[1].potRad,
			// balls[1].potYPos-balls[1].potRad, 2*balls[1].potRad,
			// 2*balls[1].potRad);

			// draw the pots
			g2d.setColor(Color.black);
			for (int i = 0; i < 14; i++) {
				g2d.drawOval(potXPos[i] - potRad[i], potYPos[i] - potRad[i], 2 * potRad[i], 2 * potRad[i]);
			}

			// draw the current player text
			if (!(gameOver || gameEnding)) {
				if (!player) {
					g2d.setColor(Color.blue);
					fm   = g2d.getFontMetrics(mediumFont);
					rect = fm.getStringBounds("Blue's turn", g2d);
					g2d.setFont(mediumFont);
					g2d.drawString("Blue's turn", (int)(w/2-rect.getWidth()/2), (int)(50-rect.getHeight()/2  + fm.getAscent()));  // Draw the string.
				} else {
					g2d.setColor(Color.red);
					fm   = g2d.getFontMetrics(mediumFont);
					rect = fm.getStringBounds("Red's turn", g2d);
					g2d.setFont(mediumFont);
					g2d.drawString("Red's turn", (int)(w/2-rect.getWidth()/2), (int)(50-rect.getHeight()/2  + fm.getAscent()));
				}
			}
			
			//draw the number of captured balls
			g2d.setColor(new Color(0x44FF0000,true));
			text=""+gameState[13];
			fm   = g2d.getFontMetrics(hugeFont);
			rect = fm.getStringBounds(text, g2d);
			g2d.setFont(hugeFont);
			g2d.drawString(text, (int)(potXPos[13]-rect.getWidth()/2), (int)(potYPos[13]-rect.getHeight()/2  + fm.getAscent()));
			
			g2d.setColor(new Color(0x440000FF,true));
			text=""+gameState[6];
			fm   = g2d.getFontMetrics(hugeFont);
			rect = fm.getStringBounds(text, g2d);
			g2d.setFont(hugeFont);
			g2d.drawString(text, (int)(potXPos[6]-rect.getWidth()/2), (int)(potYPos[6]-rect.getHeight()/2  + fm.getAscent()));
			
			//draw the number of balls in each pot
			if (showNumbers){
				g2d.setColor(new Color(0x440000FF,true));
				for (int i=0;i<6;i++){
					text = ""+gameState[i];
					fm   = g2d.getFontMetrics(largeFont);
					rect = fm.getStringBounds(text, g2d);
					g2d.setFont(largeFont);
					g2d.drawString(text, (int)(potXPos[i]-rect.getWidth()/2), (int)(potYPos[i]-rect.getHeight()/2  + fm.getAscent()));
				}
				g2d.setColor(new Color(0x44FF0000,true));
				for (int i=7;i<13;i++){
					text = ""+gameState[i];
					fm   = g2d.getFontMetrics(largeFont);
					rect = fm.getStringBounds(text, g2d);
					g2d.setFont(largeFont);
					g2d.drawString(text, (int)(potXPos[i]-rect.getWidth()/2), (int)(potYPos[i]-rect.getHeight()/2  + fm.getAscent()));
				}
			}

			// draw the winner screen
			if (gameOver) {
				g2d.setColor(Color.black);
				text = "Tie";
				if (!tie && winner){
					g2d.setColor(Color.blue);
					text = "Blue Wins!";
				} else if (!tie && !winner){
					g2d.setColor(Color.red);
					text = "Red Wins!";
				}
				fm   = g2d.getFontMetrics(largeFont);
				rect = fm.getStringBounds(text, g2d);
				g2d.setFont(largeFont);
				g2d.drawString(text, (int)(w/2-rect.getWidth()/2), (int)(h/2-rect.getHeight()/2  + fm.getAscent()));
			}

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
		switch (e.id) {
		case Event.KEY_PRESS:
		case Event.KEY_ACTION:
			// key pressed
			break;
		case Event.KEY_RELEASE:
			// key released
			break;
		case Event.MOUSE_DOWN:
			// mouse button pressed
			break;
		case Event.MOUSE_UP:
			// mouse button released
			mx = e.x;
			my = e.y;
			for (int i = 0; i < 14; i++) {
				if ((mx - potXPos[i]) * (mx - potXPos[i]) + (my - potYPos[i]) * (my - potYPos[i]) < potRad[i] * potRad[i]) {
					potClicked = i;
					mouseClick = true;
					break;
				}
			}
			break;
		case Event.MOUSE_MOVE:
			mx = e.x;
			my = e.y;
			break;
		case Event.MOUSE_DRAG:
			break;
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