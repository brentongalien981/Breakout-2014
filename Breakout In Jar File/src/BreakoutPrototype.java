import acm.graphics.*;
import acm. program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class BreakoutPrototype extends GraphicsProgram {
	
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;
	
	// Gameboard's dimension
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;
	
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;
	private static final int PADDLE_Y_OFFSET = 30;
	
	private static final int NBRICKS_PER_ROW = 10;
	private static final int NBRICKS_ROWS = 10;
	
	// Separation between bricks
	private static final int BRICK_SEP = 4;
	
	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;
	private static final int BRICK_HEIGHT = 8;
	private static final int BRICK_Y_OFFSET = 70;
	
	private static final int BALL_RADIUS = 10;
	
	private static final int NTURNS = 3;
	
	private GRect brick, paddle;
	private GOval ball;
	
	// Velocity of the ball.
	private double vx, vy;
	private GPoint p0, p1, p2, p3,
				   p0Last, p1Last, p2Last, p3Last;
	
	private static int yLastBrick;
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
	GLabel Bricks;
	private boolean paused = false;
	private boolean notYetStarted = true;
	private boolean youWon = false;
	private static int laughCount = 0;
	
	
	// AUDIOS
	private AudioClip bounceClip2 = MediaTools.loadAudioClip("hitPipe.wav");
	private AudioClip bounceClip3 = MediaTools.loadAudioClip("skyrocket.wav");
	private AudioClip newBallLaugh = MediaTools.loadAudioClip("newBallLaugh.wav");
	private AudioClip gameOverLaugh = MediaTools.loadAudioClip("gameOverLaugh.wav");
	private AudioClip crowdCheerWin = MediaTools.loadAudioClip("crowdCheerWin.wav");
	private AudioClip gameOverLossBoo = MediaTools.loadAudioClip("gameOverLossBoo.wav");
	private AudioClip kidsCheerWin = MediaTools.loadAudioClip("kidsCheerWin.wav");
	
	private GLabel game;
	private GLabel gameButton() {
		GLabel gameBtn = new GLabel("New Game");
		return gameBtn;
	}
	
	/*game = gameButton();
	game.setLocation(0, 10);
	game.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			if(ae.getActionCommand().equals("New Game")) {
				println("New Game Button is clicked.");
			}
		}
	});*/
	
	public static void main(String args[]) {
		new BreakoutPrototype().start(args);
	}
	
	public void run() {
		setSize(WIDTH, HEIGHT);
		setBricks();
		createPaddle();
		addMouseListeners();
		addKeyListeners();		
		
		try {
			int turns = 3;
			for(int i=0; i<NTURNS; i++) {
				
				if(youWon) break;
				ball = new GOval(((WIDTH/2) - BALL_RADIUS), ((HEIGHT/2) - BALL_RADIUS), 2*BALL_RADIUS, 2*BALL_RADIUS);
				ball.setFilled(true);
				add(ball);
				
				vx = getVx();
				vy = getVy();
				
				for(;;) {				
					showStatus("Bricks: " + countBricks() + "     Turns: " + turns );
					
					synchronized(this) {
						while(notYetStarted) {
							GLabel l = new GLabel("Click to start");
							l.setFont("Helvetica-20");
							l.setLocation((WIDTH - l.getWidth())/2, ((HEIGHT - l.getAscent())/2) - 20);
							add(l);
							wait();
							remove(l);	
						}
						while(paused) {
							GLabel l = new GLabel("PAUSED");
							l.setFont("Helvetica-20");
							l.setLocation((WIDTH - l.getWidth())/2, (HEIGHT - l.getAscent())/2);
							add(l);
							wait();
							remove(l);
						}				
					}
					
					ball.move(vx, vy);
					if(ball.getY () >= (HEIGHT - 2*BALL_RADIUS)) {
						remove(ball);
						if(laughCount<2) {
							newBallLaugh.play();
							++laughCount;
						}
						break;
					}
					else if(ball.getX() <= 1 | ball.getX() >= (WIDTH - 2*BALL_RADIUS - 1)) vx = -vx;
					else if(ball.getY() <= 1) vy = -vy;
					
					
					checkForEdges();
					setCollidingObject();
					if(thereIsCollidingObject()) {
						bounceOff();
						//bounceClip3.play();
						if(!paddleIsCollider()) {
							removeCollidingObject();
							bounceClip2.play(); 
						}
					}
					
					checkForLastEdges();
					pause((long)2);
					if(countBricks() == 0 ) {
						youWon = true;
						break;
					}
				}
				notYetStarted = true;
				--turns;
			}
			if(youWon) {
				GLabel l = new GLabel("You Win!");
				l.setFont("Helvetica-36");
				l.setLocation((WIDTH - l.getWidth())/2, (HEIGHT - l.getAscent())/2);
				add(l);
				bounceClip3.play();
				Thread.sleep(400);
				crowdCheerWin.play();
				Thread.sleep(1200);	
				kidsCheerWin.play();
				
				
			}
			else {
				GLabel l = new GLabel("You Loser!");
				l.setFont("Helvetica-36");
				l.setLocation((WIDTH - l.getWidth())/2, (HEIGHT - l.getAscent())/2);
				add(l);
				
				gameOverLaugh.play();				
			}
		}
		catch(Exception exc) {}
	
	//newBall();
	
	}
	
	/*synchronized private void newBall() {
		notYetStarted = true;
		notify();
	}*/
		

	private int countBricks() {
		int i;
		i =  ((int)getElementCount()) - 2;
		return i;
	}
	
	// This is to wait the user to click before
	// waiting for the ball to launch
	/*private void startGame() {
		
	}
	*/	
	
	private void checkForEdges() {
		p0 = new GPoint(ball.getX(), ball.getY());
		p1 = new GPoint(ball.getX() + 2*BALL_RADIUS, ball.getY());
		p2 = new GPoint(ball.getX(), ball.getY() + 2*BALL_RADIUS);
		p3 = new GPoint(ball.getX() + 2*BALL_RADIUS, ball.getY() + 2*BALL_RADIUS);
	}
	
	private void checkForLastEdges() {
		p0Last = new GPoint(ball.getX(), ball.getY());
		p1Last = new GPoint(ball.getX() + 2*BALL_RADIUS, ball.getY());
		p2Last = new GPoint(ball.getX(), ball.getY() + 2*BALL_RADIUS);
		p3Last = new GPoint(ball.getX() + 2*BALL_RADIUS, ball.getY() + 2*BALL_RADIUS);
	}
	
	private void bounceOff() {
		if(vx > 0 & vy > 0) {
			if(getElementAt(p2) != null) vy = -vy;
			else if(getElementAt(p1) != null) vx = -vx;
			else if(getElementAt(p3) != null) {
				if(p3.getX() > getElementAt(p3).getX()) vy = -vy;
				else if(p3.getY() > getElementAt(p3).getY()) vx = -vx;
				else if(p3.getX() == getElementAt(p3).getX()) {
					if(p3Last.getX() > p0.getX()) vx = -vx;
					else if(p3Last.getY() > p0.getY()) vy = -vy;
					else {
						vx = -vx;
						vy = -vy;
					}
				}
			}
		}
		else if(vx > 0 & vy < 0) {
			if(getElementAt(p0) != null) vy = -vy;
			else if(getElementAt(p3) != null) vx = -vx;
			else if(getElementAt(p1) != null) {
				if(p1.getX() > getElementAt(p1).getX()) vy = -vy;
				else if(p1.getY() < getElementAt(p1).getY()) vx = -vx;
				else if(p1.getX() == getElementAt(p1).getX()) {
					if(p1Last.getX() > p2.getX()) vx = -vx;
					else if(p1Last.getY() < p2.getY()) vy = -vy;
					else {
						vx = -vx;
						vy = -vy;
					}
				}
			}
		}
		else if(vx < 0 & vy > 0) {
			if(getElementAt(p3) != null) vy = -vy;
			else if(getElementAt(p0) != null) vx = -vx;
			else if(getElementAt(p2) != null) {
				if(p2.getX() < (getElementAt(p2).getX() + BRICK_WIDTH)) vy = -vy;
				else if(p2.getY() > (getElementAt(p2).getY() + BRICK_HEIGHT)) vx = -vx;
				else if(p2.getX() == (getElementAt(p2).getX() + BRICK_WIDTH)) {
					if(p2Last.getX() < p1.getX()) vx = -vx;
					else if(p2Last.getY() > p1.getY()) vy = -vy;
					else {
						vx = -vx;
						vy = -vy;
					}
				}
			}
		}
		else if(vx < 0 & vy < 0) {
			if(getElementAt(p1) != null) vy = -vy;
			else if(getElementAt(p2) != null) vx = -vx;
			else if(getElementAt(p0) != null) {
				if(p0.getX() > (getElementAt(p0).getX() + BRICK_WIDTH)) vy = -vy;
				else if (p0.getY() < (getElementAt(p0).getY() + BRICK_HEIGHT)) vx = -vx;
				else if(p0.getX() == (getElementAt(p0).getX() + BRICK_WIDTH)) {
					if(p0Last.getX() < p3.getX()) vx = -vx;
					else if(p0Last.getY() < p3.getY()) vy = -vy;
					else /*if(p0Last.getX() == p3.getX())*/ {
						vx = -vx;
						vy = -vy;
					}
				}
			}
		}
	}
	
	private boolean paddleIsCollider() {
		if(collidingObject1 == paddle) return true;
		if(collidingObject2 == paddle) return true;
		if(collidingObject3 == paddle) return true;
		if(collidingObject4 == paddle) return true;
		return false;
	}
	
	private void removeCollidingObject() {
		if(collidingObject1 != null) {
			remove(collidingObject1);
			collidingObject1 = null;
		}
		if(collidingObject2 != null) {
			remove(collidingObject2);
			collidingObject2 = null;
		}
		if(collidingObject3 != null) {
			remove(collidingObject3);
			collidingObject3 = null;
		}
		if(collidingObject4 != null) {
			remove(collidingObject4);
			collidingObject4 = null;
		}
	}
		
	private boolean thereIsCollidingObject() {
		if(collidingObject1 != null) return true;
		if(collidingObject2 != null) return true;
		if(collidingObject3 != null) return true;
		if(collidingObject4 != null) return true;
		return false;
	}
	
	private void setCollidingObject() {
		collidingObject1 = getElementAt(p0);
		collidingObject2 = getElementAt(p1);
		collidingObject3 = getElementAt(p2);
		collidingObject4 = getElementAt(p3);
	}
	
	private GObject collidingObject1, collidingObject2, 
					collidingObject3, collidingObject4;
	
	
	// this is the shorter one
	/*private GObject getCollidingObject() {
		checkForCollision();
		collidingObject = null;
		if((collidingObject = getElementAt(p0)) != null) return collidingObject;		
		if((collidingObject = getElementAt(p1))  != null) return collidingObject;		
		if((collidingObject = getElementAt(p2))  != null) return collidingObject;
		if((collidingObject = getElementAt(p3)) != null) return collidingObject;
		return collidingObject;		
	}*/
	
	
	// Get velocities.
	private double getVx() {
		vx = rgen.nextDouble(0.5, 1.0);
		if(rgen.nextBoolean(0.5)) vx = -vx;
		return vx;
	}
	
	private double getVy() {
		vy = 1.0;
		return vy;
	}
	

	// Setup the bricks
	private void setBricks() {
		int x;
		int y = BRICK_Y_OFFSET;
		Color color = Color.RED;
		for(int i=0; i<NBRICKS_ROWS; i++) {
			x = BRICK_SEP;			
						
			if(i == 2 | i == 3)	color = Color.ORANGE;			
			if(i == 4 | i == 5) color = Color.YELLOW;
			if(i == 6 | i == 7) color = Color.GREEN;
			if(i == 8 | i == 9) color = Color.CYAN;		
			
			drawBricks(x, y, color);
			y = yLastBrick + BRICK_SEP + BRICK_HEIGHT;
		}
	}
	
	private void drawBricks(int xPos, int yPos, Color color) {
		for(int j=0; j<NBRICKS_PER_ROW; j++) {
			brick = new GRect(xPos, yPos, BRICK_WIDTH, BRICK_HEIGHT);
			brick.setColor(color);
			brick.setFilled(true);
			add(brick);
			xPos += BRICK_WIDTH + BRICK_SEP;
			if(j == 9) yLastBrick = (int) brick.getY();
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		if(e.getX() <= (WIDTH - PADDLE_WIDTH)) {
		paddle.setLocation(e.getX(), paddle.getY());
		}		
	}
	
	synchronized public void mouseClicked(MouseEvent e) {
		notYetStarted = false;
		paused = false;
		notify();
	}
	
	synchronized public void keyTyped(KeyEvent ke) {
		paused = true;
	}
	
	/*public void mouseClicked(MouseEvent e) {
		startGame();
	}*/
	
	private void createPaddle() {
		//paddle = new GRect((WIDTH - PADDLE_WIDTH)/2, HEIGHT - PADDLE_Y_OFFSET + PADDLE_HEIGHT, WIDTH, PADDLE_HEIGHT);
		paddle = new GRect((WIDTH - PADDLE_WIDTH)/2, HEIGHT - PADDLE_Y_OFFSET + PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFillColor(Color.GREEN);
		paddle.setFilled(true);
		add(paddle);
	}
	
	
	/*// This is to remove the bricks that the ball has collided to.
	public void mouseClicked(MouseEvent e) {
		//point = new GPoint(e.getPoint());
		remove(getElementAt(e.getX(), e.getY()));
		//getElementAt(point);
		//if(gobj != null) remove(gobj);
	}
	private GPoint point;
	private GObject gobj;
	*/
	
	
	/*
	// This is how the ball should change path.
	private void bounceOff() {
		if(vx > 0 & vy > 0) {
			if(getElementAt(p3) != null) {
				if(getElementAt(p2) != null) vy = -vy;
				else vx = -vx;
			}
		}
		else if(vx > 0 & vy < 0) {
			if(getElementAt(p1) != null) {
				if(getElementAt(p3) != null) vx = -vx;
				else vy = -vy;
			}
		}
		else if(vx < 0 & vy > 0) {
			if(getElementAt(p3) != null) {
				if(getElementAt(p2) != null) vy = -vy;
				else vx = -vx;
			}
		}
		else if(vx < 0 & vy < 0) {
			if(getElementAt(p1) != null) {
				if(getElementAt(p3) != null) vx = -vx;
				else vy = -vy;
			}
		}
	}
	*/
}
