/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.gnwgui.windows;

import java.awt.*;
import java.util.logging.Logger;

import javax.swing.JPanel;

/**  This object is a user waiting item representing a snake turning in circles.
 * 
 * The waiting item is composed of a given number of bullets that represent the road.
 * The snake is a chain of bullets (snake length < road length) with an opaque head color
 * and body's bullets set with the head color but with transparency (alpha) descreasing.
 * Radius of the road as well as radius of bullets or snake's speed are configurable.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class WaitingSnake extends JPanel implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	private Thread myThread_;		/** Main Thread */
	private int sleep_;				/** Sleep time between two paints */
	
	private float R_;				/** Radius of the snake loop */
	private float r_;				/** Radius of the bullets that compose the snake */
	private float offsetX_;			/** X offset */
	private float offsetY_;			/** Y offset */
	private int numBullets_;		/** Number of bullets that compose the total loop */
	private int length_;			/** Number of bullets that compose the snake */
	private int state_;				/** Current position of the snake relative to the loop */
	
	private Color headColor_;		/** Color of the snake's head (RGB, 0 to 255) */
	private int usedTransRange_;	/** Transparency range between the snake's head and tail (0 to 100 percent) */
	private boolean clockwise_;		/** Sense of the snake motion (true=clockwise) */
	
	private int[] ghostList_;		/** List of transparency values for all the bullets of the loop */
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(WaitingSnake.class.getName());

	
	
	// ============================================================================
	// PUBLIC METHODS
	
	// ----------------------------------------------------------------------------
	// Constructor(s)
	
	/**
	 * Default constructor
	 */
	public WaitingSnake() {
		
		super();
		init();
	}
	
	public String getName() { return "snake"; }
	
	// ----------------------------------------------------------------------------
	// Overwriting methods of runnable objects 
	
	/**
	 * Init function
	 */
	public void init() {
		
		myThread_ = null;
		sleep_ = 90;	// 110
		
		R_ = 15f;
		r_ = 2.5f;	// 3.5f
		offsetX_ = 0;
		offsetY_ = 0;
		numBullets_ = 12;	// 8
		length_ = 8;	// 6
		state_ = 0;
		
		// Snake's head: dark gray
		headColor_ = new Color(80, 80, 80);
		// Snake's head: opaque
		// Snake's tail: alpha=20%
		usedTransRange_ = 80;
		
		// Motion of the snake: clockwise
		clockwise_ = true;
		
		// Initialization of the transparency list
		ghostList_ = new int[numBullets_];
		// Current transparency: alpha = 100%
		double currentTransp = 100;
		// The range of transparency defines by usedTransRange_ is used for all the
		// length_ bullets of the snake's body.
		double usedTransp = usedTransRange_/length_;
		
		for (int i=0; i < numBullets_; i++) {
			
			ghostList_[i] = (int)((currentTransp*255)/100);
			
			// If we are always in the snake's body, we decrease alpha
			if (i < length_) {
				currentTransp -= usedTransp;
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Start function
	 */
	public void start() {
		
		// If myThread_ is null, we start it!
		if (myThread_ == null) {
			
			myThread_ = new Thread(this);
			myThread_.start();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Stop function
	 */
	public void stop() {
		
		// myThread_.stop() is deprecated
		myThread_ = null;
		//System.out.println("Waiting snake stopped.");
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Run function
	 */
	public void run() {
		
		Thread thisThread = Thread.currentThread();
		
		while (myThread_ == thisThread) {
			
			repaint();
			
			try {
				// Rather than myThread_.sleep(sleep_);
				Thread.sleep(sleep_);
			}
			catch (InterruptedException ie) {
				
				return;
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	// Graphic functions
	
	/**
	 * Update function used to update the draw.
	 */
	public void update(Graphics g) {
		
		paint(g);
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Paint function used to draw the road and the snake.
	 */
	public void paint(Graphics g) {
		
		paintComponent(g);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Paint function used to draw the road and the snake.
	 */
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);
		
		// Antialiazing
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Angle between two bullets of the loop
		double oneAngle = 2*Math.PI/numBullets_;
		int x, y, alpha, size;
		
		// Draw each bullets of the loop
		for (int i=0; i < numBullets_; i++) {

			// Get alpha of the current bullet
			alpha = ghostList_[(i+state_)%numBullets_];
			g2.setColor(new Color(headColor_.getRed(), headColor_.getGreen(), headColor_.getBlue(), alpha));
			
			// Calculate the coordinates (x,y)
			if (clockwise_) {
				x = (int)(R_*Math.cos(i*-oneAngle) + R_ + offsetX_);
				y = (int)(R_*Math.sin(i*-oneAngle) + R_ + offsetY_);	
			}
			else {
				x = (int)(R_*Math.cos(i*oneAngle) + R_ + offsetX_);
				y = (int)(R_*Math.sin(i*oneAngle) + R_ + offsetY_);
			}
			
			// Diameter of the bullet
			size = (int)(2*r_);
			
			g2.fillOval(x, y, size, size);
		}
		
		// Set the state of the snake relative to the loop
		state_ = (state_+1)%numBullets_;
	}
	
	// ----------------------------------------------------------------------------
	// Access methods
	
	/**
	 * Determines the motion speed of the snake. Time sleep between two draw of the snake.
	 * @param value Time sleep [ms]
	 */
	public void setSleep(int value) {
		
		sleep_ = value;
	}
	
	/**
	 * Get the time sleep between two draw of the snake.
	 * @return Time sleep [ms]
	 */
	public int getSleep() {
		
		return sleep_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the radius of the loop traveled by the snake.
	 * @param value Radius of the loop [pixel]
	 */
	public void setR(float value) {
		
		R_ = value;
	}
	
	/**
	 * Get the radius of the loop traveled by the snake.
	 * @return Radius of the loop [pixel]
	 */
	public float getR() {
		
		return R_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the radius of the bullets that compose the snake and the loop.
	 * @param value Radius of the bullets [pixel]
	 */
	public void setr(float value) {
		
		r_ = value;
	}
	
	/**
	 * Get the radius of the bullets that compose the snake and the loop.
	 * @return Radius of the bullets [pixel]
	 */
	public float getr() {
		
		return r_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the X offset used to start drawing.
	 * @param value X offset [pixel]
	 */
	public void setOffsetX(float value) {
		
		offsetX_ = value;
	}
	
	/**
	 * Get the X offset used to start drawing.
	 * @return X offset [pixel]
	 */
	public float getOffsetX() {
		
		return offsetX_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the Y offset used to start drawing.
	 * @param value Y offset [pixel]
	 */
	public void setOffsetY(float value) {
		
		offsetY_ = value;
	}
	
	/**
	 * Get the Y offset used to start drawing.
	 * @return Y offset [pixel]
	 */
	public float getOffsetY() {
		
		return offsetY_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the number of bullets that compose the loop.
	 * @param value Number of bullets
	 */
	public void setNumBullets(int value) {
		
		numBullets_ = value;
	}
	
	/**
	 * Get the number of bullets that compose the loop.
	 * @return Number of bullets
	 */
	public int getNumBullets() {
		
		return numBullets_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the number of bullets that compose the snake.
	 * @param value Number of bullets
	 */
	public void setLength(int value) {
		
		length_ = value;
	}
	
	/**
	 * Get the number of bullets that compose the snake.
	 * @return Number of bullets
	 */
	public int getLength() {
		
		return length_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the position of the snake relative to the loop. Warning: value%getNumBullets().
	 * @param value Position of the snake
	 */
	public void setState(int value) {
		
		state_ = value%numBullets_;
	}
	
	/**
	 * Get the position of the snake relative to the loop.
	 * @return Position of the snake
	 */
	public int getState() {
		
		return state_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the snake's head color (RGB).
	 * @param c Color object
	 */
	public void setHead(Color c) {
		
		headColor_ = c;
	}
	
	/**
	 * Get the snake's head color as a triplet of three int number (0 to 255).
	 * @return Triplet of RGB elements
	 */
	public Color getHeadColor() {
		
		return headColor_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the motion sense of the snake (clockwise or anticlockwise).
	 * @param sense Motion sense (clockwise = true)
	 */
	public void setMotionSense(boolean sense) {
		
		clockwise_ = sense;
	}
	
	/**
	 * Get the motion sense of the snake (clockwise or anticlockwise).
	 * @return Motion sense (clockwise = true)
	 */
	public boolean getMotionSense() {
		
		return clockwise_;
	}
}
