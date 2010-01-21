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

/**
 * Adaptation of the following code(s)
 * - Java desktop drag and drop
 *   by Gregg Wonderly
 *   June 1, 2006
 */

package ch.epfl.lis.gnwgui.idesktop;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;

/** Interactive element iElement.
 *   
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 * TODO: Modify setLabelLayout() and related functions to handle items with more
 * than one or two lines for the label.
 *
 */
abstract public class IElement extends JLabel {
	
    /** Default serialisation */
	private static final long serialVersionUID = 1L;
	
    /** Complete name of the item. */
    protected String label_ = "";
    /** Item label background color when selected. */
    protected Color itemLabelSelecColor_ = Color.LIGHT_GRAY; // Default color
	
    /** Is this CDDItem in a selected state ? */
    protected boolean selected_ = false;
    /** Is this CDDItem currently dragged ? */
    protected boolean dragged_ = false;
    /** Can this item be draggable */
    protected boolean draggable_ = true;
    /** Is the cursor of the mouse over the icon right now ? */
    protected boolean mouseOverIcon_ = false;
    /** Can the item be destroyed ? */
    protected boolean destroyable_ = true;
    /** Use the a brighter icon when the mouse is over the item ? */
    protected boolean usedIconHighlighted_ = true;
    
    /** Define the step movement when the item is dragged. */
    protected int mvtItemStep_ = 1;
    
    /** The CDDDesktop to which belongs this CDDItem. */
    protected IDesktop desktop_ = null;
    /** The JLayeredPane we are on, if any. */
    protected JLayeredPane pane_ = null;
    /** The MouseAdapter created to manage the mouse events. */
    protected MyMouseAdapter mma_;
    /** The MouseMotionListener created to manage our drag state. */
    protected MyMouseMotionListener mml;
    
    /** The last CddItem that was selected. */
    public static IElement lastItem;
    /** The current, active CddItem. */
    public static IElement curItem;
    
    /** Width (variable) */
    protected double width_ = 0.;
    /** Height (constant) */
    protected double height_ = 32.;
    /** Width of the icon. */
    protected double iconWidth_ = 32.;
    /** Height of the icon. */
    protected double iconHeight_ = 32.;
    
    /** Images of the item (original and highlighted) */
    protected HighlightableImage icon_ = null;
    
    /** Father element of this element */
    protected IElement father_ = null;
    /** Children of this element */
    protected ArrayList<IElement> children_ = new ArrayList<IElement>();
    
    /** Logger for this class */
    private static Logger log = Logger.getLogger(IElement.class.getName());

    // =======================================================================================
    // PRIVATE METHODS
    //
    
    private void initialize() {
    	icon_ = setDefaultIcon(Color.GRAY); // Set the default icon for the item
        mouseEntry();
    }
    
    private void initialize(IDesktop desk) {
    	initialize();
    	desktop_ = desk;
    	pane_ = desk.getDesktopPane();
    }
    
    // =======================================================================================
    // ABSTRACT METHODS
    //

    abstract protected void leftMouseButtonInvocationSimple();
    abstract protected void leftMouseButtonInvocationDouble();
    abstract protected void wheelMouseButtonInvocation();
    abstract protected void rightMouseButtonInvocation();

    // =======================================================================================
    // PUBLIC AND PROTECTED METHODS
    //
    
    public IElement(String label) {
        super(""); // don't set the label here, instead use always setLabel()
        initialize();
        setLabel(label);
    }
    
    public IElement(String label, IDesktop desk) {
        super("");
        initialize(desk);
        setLabel(label);
    }
    
    public IElement(IElement item) {
    	super("");
    	initialize();
        this.draggable_ = item.draggable_;
        this.mvtItemStep_ = item.mvtItemStep_;
        this.desktop_ = item.desktop_;
        this.pane_ = item.pane_;
        this.iconWidth_ = item.iconWidth_;
        this.iconHeight_ = item.iconHeight_;
        this.icon_ = item.icon_;
        this.usedIconHighlighted_ = item.usedIconHighlighted_;
        this.setLabel(item.getLabel());
        this.label_ = item.label_;
        this.itemLabelSelecColor_ = item.itemLabelSelecColor_;
        this.father_ = item.father_;
        this.children_ = item.children_;
        this.setToolTipText(item.getToolTipText());
    }

    public void mouseEntry() {
    	mma_ = new MyMouseAdapter();
        addMouseListener(mma_);
    }
    
    public void removeMouseEntry() {
    	removeMouseListener(mma_);
    	mma_ = null;
    }
    
    public static synchronized void clearSelection() {
        if(lastItem != null) {
            lastItem.exit();
        }
        lastItem = null;
    }
    
    protected void reportException(Exception ex) {
        log.log(Level.SEVERE, ex.toString(), ex);
    }
    
    public Dimension getSpecificSize() {
    	return new Dimension((int)width_, (int)height_);
    }
    
    public void updateLabelFormat(boolean selected) {
    	
		String output = "<html>";
    	
    	String bgcolor = RGB2HexaColor(itemLabelSelecColor_);

    	if (selected) {
    		output += "<font bgcolor=\"#" + bgcolor + "\">";
    	}
    	else {
    		output += "<font>";
    	}

    	if (children_.size() != 0)
    		output += "<b>" + label_ + "</b>";
    	else
    		output += label_;
    	
		output += "</font></html>";
    	this.setHorizontalAlignment(JLabel.LEFT);
    	this.setVerticalAlignment(JLabel.CENTER);
        	
    	setText(output);
    }
    
    public void setLabel(String label) {
    	label_ = label;
    	updateLabelFormat(selected_);    	
    	setSize(new Dimension((int) getPreferredWidth(), (int) height_));
    }
    
    public double getPreferredWidth() {
    	double pureLabelSize = 0;
    	if (children_ != null && children_.size() > 0)
    		pureLabelSize = new JLabel("<html><b>" + label_ + "</b></html>").getPreferredSize().getWidth();
    	else
    		pureLabelSize = new JLabel("<html>" + label_ + "</html>").getPreferredSize().getWidth();
    	
    	// icon size + gap between icon and label + label width
    	return iconWidth_ + 10 + pureLabelSize;
    }
    
    
    
	public static String RGB2HexaColor(Color c) {
		return Integer.toHexString( c.getRGB() & 0x00ffffff );
	}

    public void paintComponent(Graphics g) {
		super.paintComponent(g);
        updateLabelFormat(selected_ && !dragged_);
//        setOpaque(false);
    }
    
    /**
     * Draw a basic image and set it as default icon.
     */
    public HighlightableImage setDefaultIcon(Color color) {
		BufferedImage myImage = new BufferedImage((int)iconWidth_, (int)iconHeight_, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = myImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		Paint pt = g2.getPaint();
		g2.setPaint( new GradientPaint( (int)iconWidth_, 0, color,
				(int)iconWidth_, (int)iconHeight_, color.darker() ) );
		g2.fillRoundRect(0, 0, (int)iconWidth_, (int)iconHeight_, 0, 0 );
        g2.setPaint(pt);
        return new HighlightableImage(myImage);
    }

    /**
     * Called when the mouse enters this CddItem.
     */
    protected void enter() {
        selected_ = true;
        curItem = this;
        repaint();
    }

    /**
     * Called when the mouse exits this CddItem.
     */
    protected void exit() {
        curItem = null;
        selected_ = false;
        repaint();
    }
    
    protected void arrive() {
       setIcon(new ImageIcon(icon_.getHighlightedImage()));
    }
    
    protected void leave() {
       	setIcon(new ImageIcon(icon_.getImage()));
    }

    /**
     * Called when the user performs a context menu click using the popup trigger button/operation
     * associated with the component.
     * 
     * Subclasses can override this method to provide an operation.
     * 
     * @param ev The associated mouse event to get the context from.
     */
    protected void popup(MouseEvent ev) {}
    
    
    // =======================================================================================
    // GETTERS AND SETTERS
    //
    
    public void setItemIcon(Image icon) {
    	icon_ = new HighlightableImage(icon);
    	setIcon(new ImageIcon(icon_.getImage()));
    }
    
    public void setItemIcon(HighlightableImage icon) {
    	icon_ = icon;
    	setIcon(new ImageIcon(icon_.getImage()));
    }
    
	public void setItemLabelSelecColor(Color c) { itemLabelSelecColor_ = c; }
	public Color getItemLabelSelecColor() { return itemLabelSelecColor_; }
	
	public void setDraggable(boolean b) { draggable_ = b; }
	public boolean getDraggable() { return draggable_; }
	
	public String getLabel() { return label_; }
	
	public void setDestroyable(boolean destroyable) { destroyable_= destroyable; }
	public boolean isDestroyable() { return destroyable_; }
	
    public void addChild(IElement item) {
    	children_.add(item);
    	setSize(new Dimension((int) getPreferredWidth(), (int) height_));
    }
    
    public void clearChildren() {
    	children_.clear();
    	setSize(new Dimension((int) getPreferredWidth(), (int) height_));
    }
    
    public ArrayList<IElement> getChildren() {
    	return children_;
    }
    
    public void setFather(IElement item) {
    	father_ = item;
    }
    
    public IElement getFather() {
    	return father_;
    }
    
    /**
     * Is the item currently dragged ?
     * @param state Dragged or not ?
     */
    public void isDragged(boolean state) {
    	dragged_ = state;
    	repaint();
    }
	
	


	
	
	
	
	
	
	
	
    /**
     * The MouseAdapter that handles click events to establish the MouseMotionListener.
     */
    private class MyMouseAdapter extends MouseAdapter {
    	
        /** The item this listener is associated with. */
        IElement item;
      
        
        /**
         * Creates an instance.
         */
        public MyMouseAdapter() {
            this.item = IElement.this;
        }
        
        
        /**
         * Called when the user clicks on the CddItem.
         * A click means "pressed-and-released"
         * @param ev The event associated with the click operation.
         */
        public void mouseClicked(MouseEvent ev) {
        	
            if (ev.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            	
            	enter();

            	try {
            		
            		if (ev.getClickCount() == 1)
        				item.leftMouseButtonInvocationSimple();
            		else if (ev.getClickCount() == 2)
            			item.leftMouseButtonInvocationDouble();
            		else
            			return;
            		
            	} catch(Exception ex) {
            		reportException(ex);
            	}
            	return;

            } else if (ev.getButton() == java.awt.event.MouseEvent.BUTTON2) {
            	// Middle button (wheel)
            	enter();

            	try {
        			if (ev.getClickCount() == 1)
        				item.wheelMouseButtonInvocation();
            	} catch(Exception ex) {
            		reportException(ex);
            	}
            	return;
            } else if (ev.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            	// Right button
            	enter();
            	try {
        			if (ev.getClickCount() == 1)
        				item.rightMouseButtonInvocation();
            	} catch(Exception ex) {
            		reportException(ex);
            	}
            }
        }
        
        
        /**
         * Called when the mouse enters the item area.
         * @param ev The event associated with the mouse entering in the area of the component.
         */
        public void mouseEntered(MouseEvent ev) {
     
        	arrive();
        	mouseOverIcon_ = true;
        	repaint();
        }
        
        
        /**
         * Called when the mouse leaves the item area.
         * @param ev The event associated with the mouse leaving in the area of the component.
         */
        public void mouseExited(MouseEvent evt) {

        	leave();
        	mouseOverIcon_ = false;
        	repaint();
        }
        

        /**
         * Called when the mouse is pressed down.
         * @param ev The associated event.
         */
        public void mousePressed( MouseEvent ev ) {

//        	if (ev.getButton() == java.awt.event.MouseEvent.BUTTON2) {
//        		return;
//        	}
        	
        	enter();
        	
            // Check if popup
            if (ev.isPopupTrigger()) {
                // switch selected_ to this item
                if (lastItem != null && lastItem != IElement.this) {
                    lastItem.exit();
                }
                lastItem = IElement.this;
                lastItem.enter();
                
                // Show the menu if any
                popup(ev);
                return;
            }
            
            // Not popup, so clear last selected_
            if(lastItem != null) {
                lastItem.exit();
            }
            
            // If not button 1, just ignore
//            if(ev.getButton() != java.awt.event.MouseEvent.BUTTON1)
//                return;
            
            // Activate motion listener and what for drag.
            createMotionListener(ev);
            
            // Add the mouse listener created, or already existing.
            IElement.this.addMouseMotionListener(mml);
        }
        

        /**
         * Called to create the MouseMotionListener when the drag operation starts.
         * @param ev The associated mouse event.
         */
        private void createMotionListener(MouseEvent ev) {
        	
            lastItem = IElement.this;
            lastItem.enter();

            final int offx = ev.getX();
            final int offy = ev.getY();

            // Create new listener as needed
            if (mml == null) {
                mml = new MyMouseMotionListener();
            }

            // Set drag offsets into object
            mml.setOffsets(offx, offy );
        }

        
        /**
         * Called when the mouse button is released.
         * @param ev The associated event.
         */
        public void mouseReleased(MouseEvent ev) {
        	
            if (ev.isPopupTrigger()) {
                // Make sure the correct last item is identified.
                if (lastItem != null && lastItem != IElement.this) {
                    lastItem.exit();
                }

                lastItem = IElement.this;
                lastItem.enter();
                popup(ev);

                // Stop listening to mouse motion events.
                if (mml != null)
                    IElement.this.removeMouseMotionListener(mml);
                
                return;
            }

            // Not popup, remove motion listener
            IElement.this.removeMouseMotionListener(mml);

            // When dropped, move back to the default layer.
            if (pane_ != null) {
                pane_.setLayer(IElement.this, JLayeredPane.DEFAULT_LAYER.intValue(), 0);
            }

            isDragged(false);

            if (desktop_ != null)
            	desktop_.itemReleased(item);
        }
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * The MouseMotionAdapter used to track the drag operation.
     */
    private class MyMouseMotionListener extends MouseMotionAdapter {
    	
        /** The x offset of the initial mouse click from the left edge of the CddItem. */
        int offx;
        /** The y offset of the mouse from the top of the CddItem */
        int offy;
        
        /**
         * Updates the current offsets for each successive drag operation to the click point that the
         * mouse was out when the mouse was pressed.
         * @param x The X location of the initial mouse down event.
         * @param y The Y location of the initial mouse down event.
         */
        public void setOffsets(int x, int y) {
            offx = x;
            offy = y;
        }
        
        
        /**
         * Called when the mouse is moved without a button down.
         * @param ev The associated event for this operation.
         */
        public void mouseMoved(MouseEvent ev) {
        	mouseOverIcon_ = true;
        }
        
        
        /**
         * Called when the mouse is moved with button one down.
         * @param ev The associated mouse event.
         */
        public void mouseDragged(MouseEvent ev) {
        	
        	if (!draggable_)
        		return;
        	
        	isDragged(true);
        	
            Point pt = getLocation();
            Point p = new Point( ev.getX()+pt.x-offx,
                ev.getY()+pt.y-offy);

            // Positioning is every mvtItemStep_ pixels to make it easier to line things up.
            int xoff = p.x % mvtItemStep_;
            int yoff = p.y % mvtItemStep_;

            p = new Point( p.x-xoff+mvtItemStep_, p.y-yoff+mvtItemStep_);
            isDragged(true);

            // On a JDesktoppane_, change the layer so that
            // we pass over everything on the desktop
            if( pane_ != null ) {
                pane_.setLayer( IElement.this,
                    JLayeredPane.DRAG_LAYER.intValue() );
            }
            setLocation( p.x, p.y );
        }
    }
}