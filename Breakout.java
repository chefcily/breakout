/*
 * File: Breakout.java
 * -------------------
 * Name: Cecily Foote
 * Section Leader: Anna Geiduschek
 * 
 * This file implements the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;
	
	/** In order to achieve the effect of animation */
	private static final int PAUSE_TIME = 10;
	
	/** The paddle */
	private GRect paddle;

	/** The ball */
	private GOval ball;
	
	/** The message denoting the player's number of lives */
	private GLabel lifeStatus;
	
	/** The x- and y-components of the ball's velocity */
	private double vx, vy;
	
	/** The space between the lives and points statuses and the wall */
	private static final double SCORE_X_OFFSET = 5.0;
	
	public void run() {
		setUpBreakout();
		playBreakout();
	}
	
	/* Make the bricks and paddle */
	private void setUpBreakout() {
		setUpBricks();
		paddle = addPaddle();
		addMouseListeners();
	}

	/* Allows the player to play NTURNS rounds of Breakout */
	private void playBreakout() {
		//keeps track of remaining bricks
		int bricksLeft = NBRICK_ROWS * NBRICKS_PER_ROW;
		int lives = NTURNS;

		for (int turnsLeft = NTURNS; turnsLeft > 0; turnsLeft--) {
			ball = makeBall();
			lifeStatus = new GLabel("Lives Remaining: " + lives);
			lifeStatus.setFont("Helvetica-12");
			lifeStatus.setColor(Color.BLACK);
			lifeStatus.setLocation(SCORE_X_OFFSET,
								   HEIGHT - lifeStatus.getHeight() / 2.0);
			add(lifeStatus);
			
			//cues the player to start the game
			GLabel startMessage = new GLabel("Click to serve!");
			startMessage.setFont("Helvetica-24");
			startMessage.setColor(Color.BLACK);
			startMessage.setLocation((WIDTH - startMessage.getWidth()) / 2.0,
									 HEIGHT / 2.0 - startMessage.getHeight() * 2.0);
			add(startMessage);
			waitForClick();
			remove(startMessage);
			remove(lifeStatus);
			
			//sets the ball's initial velocity
			RandomGenerator rgen = RandomGenerator.getInstance();
			vy = 3.5;
			vx = rgen.nextDouble(1.0, 3.0);
			if (rgen.nextBoolean(0.5)) vx = -vx;

			bricksLeft = playBall(ball, bricksLeft);
			if (bricksLeft != 0) lives--;
			remove(ball);
			remove(lifeStatus);
			if (bricksLeft == 0) break;
		}
		
		//displays that the player is out of lives
		lifeStatus = new GLabel("Lives Remaining: " + lives);
		lifeStatus.setFont("Helvetica-12");
		lifeStatus.setColor(Color.BLACK);
		lifeStatus.setLocation(SCORE_X_OFFSET,
							   HEIGHT - lifeStatus.getHeight() / 2.0);
		add(lifeStatus);
			
		//display lose message if the player didn't remove all the bricks
		GLabel loseMessage = new GLabel("YOU LOSE :(");
		loseMessage.setFont("Helvetica-24");
		loseMessage.setColor(Color.BLACK);
		loseMessage.setLocation((WIDTH - loseMessage.getWidth()) / 2.0,
								(HEIGHT - loseMessage.getHeight()) / 2.0);
		add(loseMessage);
	}
	
	/* Sets the ball in motion and defines its bouncing */
	private int playBall(GOval ball, int bricksLeft) {
		while(true) {
			ball.move(vx, vy);
			pause(PAUSE_TIME);
			
			//checks for and responds to collisions at each corner
			if (getCollidingObject() != null) {
				GObject collider = getCollidingObject();
				respondToCollision(collider);
				if (collider != paddle) bricksLeft--;
			}

			//prints win message if all the bricks are gone
			if (bricksLeft == 0) {
				remove(ball);
				GLabel winMessage = new GLabel("YOU WIN :)");
				winMessage.setFont("Helvetica-24");
				winMessage.setColor(Color.BLACK);
				winMessage.setLocation((WIDTH - winMessage.getWidth()) / 2.0,
										(HEIGHT - winMessage.getHeight()) / 2.0);
				add(winMessage);
			}
			
			//responds to hitting the edges of the window
			if (hasHitWall()) vx = -vx;
			if (hasHitCeiling()) vy = -vy;
			if (hasHitBottom()) break; //ends a turn
		}
		
		return bricksLeft;
	}

	
	/* Responds to a collision with the paddle or with bricks */
	private void respondToCollision(GObject collider) {
		if (collider == paddle) {
			vy = -(Math.abs(vy));
		}
		if (collider != paddle) {
			vy = -vy;
			remove(collider);
		}
	}
	
	/* Returns the object involved in a collision or null if there
	 * is no collision.	 
	 */
	private GObject getCollidingObject() {
		//simplifies defining the four corners of the ball
		double ballX = ball.getX();
		double ballY = ball.getY();
		double d = BALL_RADIUS * 2.0;
		
		//defines the four corners of the ball
		GPoint cornerTL = new GPoint(ballX, ballY);
		GPoint cornerTR = new GPoint(ballX + d, ballY);
		GPoint cornerBR = new GPoint(ballX + d, ballY + d);
		GPoint cornerBL = new GPoint(ballX, ballY + d);
		
		/* Checks for collisions at each corner and returns object
		 * involved in the collision
		 */
		if (getElementAt(cornerTL) != null) {
			return getElementAt(cornerTL);
		} else if (getElementAt(cornerTR) != null) {
			return getElementAt(cornerTR);
		} else if (getElementAt(cornerBR) != null) {
			return getElementAt(cornerBR);
		} else if (getElementAt(cornerBL) != null) {
			return getElementAt(cornerBL);
		} else return null;

	}
	
	/* Checks if the ball has run into the left or right wall */
	private boolean hasHitWall() {
		double lWallX = 0;
		double rWallX = WIDTH - BALL_RADIUS * 2.0;
		
		return ball.getX() <= lWallX || ball.getX() >= rWallX;
	}
	
	/* Checks if the ball has run into the top of the window */
	private boolean hasHitCeiling() {
		return ball.getY() <= 0;
	}
	
	/* Checks if the ball has fallen off the screen */
	private boolean hasHitBottom() {
		return ball.getY() >= HEIGHT;
	}
	
	/* Makes all rows of colored bricks */
	private void setUpBricks() {
		//coordinates for the top left brick
		double x0 = (WIDTH - (NBRICKS_PER_ROW * BRICK_WIDTH)
				 - (NBRICKS_PER_ROW - 1.0) * BRICK_SEP) / 2.0;
		double y0 = BRICK_Y_OFFSET;

		//draw bricks left to right, top to bottom
		for (int row = 0; row < NBRICK_ROWS; row++) {
			for (int col = 0; col < NBRICKS_PER_ROW; col++) {
				double x = x0 + col * (BRICK_WIDTH + BRICK_SEP);
				double y = y0 + row * (BRICK_HEIGHT + BRICK_SEP);				
				
				Color color = Color.RED;
				switch (row) {
				case 2: case 3: color = Color.ORANGE; break;
				case 4: case 5: color = Color.YELLOW; break;
				case 6: case 7: color = Color.GREEN; break;
				case 8: case 9: color = Color.CYAN; break;
				}
				drawBrick(x, y, color);
			}
		}
	}
	
	/* Draws one filled and colored brick */
	private void drawBrick(double x, double y, Color COLOR) {
		GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
		brick.setFilled(true);
		brick.setColor(COLOR);
		add(brick);
	}
	
	/* Creates and adds the paddle used to reflect the ball */
	private GRect addPaddle() {
		GRect paddle = new GRect((WIDTH - PADDLE_WIDTH) / 2.0,
								 HEIGHT - PADDLE_Y_OFFSET,
								 PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		paddle.setColor(Color.BLACK);
		add(paddle);
		
		return paddle;
	}
	
	/* Makes the paddle follow the mouse in the x-direction */
	public void mouseMoved(MouseEvent e) {
		double mouseX = e.getX();
		paddle.setLocation(mouseX - PADDLE_WIDTH / 2.0,
						   HEIGHT - PADDLE_Y_OFFSET);
		
		//keep the paddle from going off the screen
		if (mouseX < PADDLE_WIDTH / 2.0) {
			paddle.setLocation(0, HEIGHT - PADDLE_Y_OFFSET);
		}
		if (mouseX > WIDTH - PADDLE_WIDTH / 2.0) {
			paddle.setLocation(WIDTH - PADDLE_WIDTH,
							   HEIGHT - PADDLE_Y_OFFSET);
		}
	}

	/* Makes the ball and centers it in the window */
	private GOval makeBall() {
		//the center of the application window
		double cx = WIDTH / 2.0;
		double cy = HEIGHT / 2.0;
		
		GOval circle = new GOval(cx - BALL_RADIUS, cy - BALL_RADIUS,
							     BALL_RADIUS * 2, BALL_RADIUS * 2);
		circle.setFilled(true);
		circle.setColor(Color.BLACK);
		add(circle);
		
		return circle;
	}
}
