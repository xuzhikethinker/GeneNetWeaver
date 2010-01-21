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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.KeyStroke;

/** Interactive desktop iDesktop
 * 
 * Implement a Macintosh-inspired files-explorer. Elements on the desktop are displayed
 * column by column. When clicking on an element, its children are displayed in the next
 * column. The spaces between the columns are adaptative, i.e. if the label of an
 * element changes (new label, bold font), if it is deleted or if a new element is added,
 * the locations of the next columns will be automatically adapted.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
abstract public class IDesktop {
	
	/** Instance of JDesktopPanel. */
	protected JDesktopPane desktopPane_ = null;
	
	/** Height of one item. [px] */
	protected int heightOneItem_ = 36;
	
	/** Initial gap (top) [px] */
	protected int vGap0_ = 30;
	/** Initial gap (left) [px] */
	protected int hGap0_ = 10;
	/** Horizontal gap between 2 items. [px] */
	protected int hGap_ = 15;
	/** Vertical gap between 2 items. [px] */
	protected int vGap_ = 20;
	
    /** If an item is close enough of a new location, it's attracted and placed there. */
	protected double gravity_ = 0.5;

	/** Content of the desktop */
	protected ArrayList< ArrayList<IElement> > content_ = new ArrayList< ArrayList<IElement> >();
	/** Different width is used for each column. */
	protected ArrayList<Integer> columnWidths_ = new ArrayList<Integer>();
	
    /** Logger for this class */
	private static Logger log = Logger.getLogger(IDesktop.class.getName());
    
	
    // =======================================================================================
    // PRIVATE METHODS
    //
	
    /**
     * Activates all the mouse listening activities
     */
    private void mouseEntry() {
    	desktopPane_.addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
            	IElement.clearSelection();
            }
        });
//    	desktopPane_.addMouseListener(new MyMouseAdapter());
    }
    
    // =======================================================================================
    // ABSTRACT METHODS
    //
    
    /**
     * Defines what to do when an item is dragged and then released on the desktop.
     */
    abstract public void itemReleased(IElement item);
    
    // =======================================================================================
    // PUBLIC METHODS
    //
	
	/**
	 * Constructor
	 */
	@SuppressWarnings("serial")
	public IDesktop(String name) {
		
		desktopPane_ = new JDesktopPane() {

            public void paintComponent( Graphics g ) {
            	
        		super.paintComponent(g);
        		Graphics2D g2 = (Graphics2D)g;
        		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        
        initialize();
        desktopPane_.setName(name);
        desktopPane_.setBackground(Color.WHITE);
        // Item must be opaque after being placed
        desktopPane_.setOpaque(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialize the iDesktop.
	 */
	public void initialize() {
		content_.add(new ArrayList<IElement>());
		columnWidths_.add(0);
		mouseEntry(); // Active the mouse listener
		deleteSelectedElement(desktopPane_);
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Add the given element in the first column of this desktop.
	 * @param item
	 */
	public void addItemOnDesktop(IElement item) {
		content_.get(0).add(item);		
		recalculateColumnWidths(item);
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Add the given element on the desktop at the position specified. If an
	 * element already exists at this position, it is simply replaced.
	 * @param item
	 * @param position
	 */
	public void addItemOnDesktop(IElement item, Point position) {
		int c = (int) position.getX();
		int index = (int) position.getY();
		content_.get(c).add(index, item);
		refreshDesktop();
	}	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Recalculates the column width of the one that contains the given element.
	 * The desktop should be then repaint using repaintDesktop().
	 * @param item
	 */
	public void recalculateColumnWidths(IElement item) {
		int c = (int) getElementPosition(item).getX();
		recalculateColumnWidths(c);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Recalculates the column width specified.
	 * @param c
	 */
	public void recalculateColumnWidths(int c) {
		columnWidths_.set(c, 0);
		int tempSize = 0;
		
		ArrayList<IElement> subContent = content_.get(c);
		for (int i=0; i < subContent.size(); i++) {
			tempSize = (int) subContent.get(i).getPreferredWidth();
			if (tempSize > columnWidths_.get(c))
				columnWidths_.set(c, tempSize);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Remove the given element on the desktop. If its children are displayed,
	 * they are also remove.
	 * @param item
	 */
	public void removeItemFromDesktop(IElement item) {
		Point pos = getElementPosition(item);
		
		if (pos == null)
			return;
		
		int c = (int) pos.getX();
		// Remove item
		content_.get(c).remove(item);
		// If this item displays its children, remove them from the desktop
		if ((c+1) < content_.size() && content_.get(c+1) != null && !content_.get(c+1).equals(""))
			if (content_.get(c+1).size() > 0 && content_.get(c+1).get(0).getFather().equals(item))
				for (int j=(c+1); j < content_.size(); j++)
					content_.get(j).clear();
		// Remove this item as child from its eventual father
		if (item.getFather() != null)
			item.getFather().getChildren().remove(item);
		
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Replace the item1 on the desktop by item2.
	 * @param item1
	 * @param item2
	 */
	public void replaceItem(IElement item1, IElement item2) {
		Point pos = getElementPosition(item1);
		int c = (int) pos.getX();
		int index = (int) pos.getY();
		content_.get(c).set(index, item2);
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Repaint the elements on the desktop accordingly to content_.
	 */
	public void repaintDesktop() {
		// Remove all the elements present on the desktop
		desktopPane_.removeAll();
		
		ArrayList<IElement> subContent = null;
		for (int i=0; i < content_.size(); i++) {
			subContent = content_.get(i);
			for (int j=0; j < subContent.size(); j++) {
				subContent.get(j).setLocation( getElementLocationFromPosition(i, j) );
				desktopPane_.add(subContent.get(j));
			}
		}
		desktopPane_.repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Refreshing the desktop consists to recalculate the widths of the columns
	 * and repainting the desktop.
	 */
	public void refreshDesktop() {
		for (int i=0; i < content_.size(); i++)
			recalculateColumnWidths(i);
		repaintDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return the position of the given element as (column,index).
	 * If the element is not present on the desktop, the returned value
	 * is null.
	 * @param item Element
	 * @return Location associated to c and index
	 */
	public Point getElementPosition(IElement item) {
		ArrayList<IElement> subContent = null;
		for (int i=0; i < content_.size(); i++) {
			subContent = content_.get(i);
			for (int j=0; j < subContent.size(); j++)
				if (subContent.get(j).equals(item))
					return new Point(i, j);
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return the location of an element on the desktop from its position determined
	 * by the index of its column (c) and its index in this column (index).
	 * @param c Index of the column
	 * @param index Index of the element in the column c
	 * @return Location associated to c and index
	 */
	public Point getElementLocationFromPosition(int c, int index) {
		int x = hGap0_;
		int y = vGap0_;
		
		for (int i=0; i < c; i++)
			x += columnWidths_.get(i) + hGap_;
		y += index * heightOneItem_;
		
		return new Point(x, y);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display the eventual children of the element given in parameter.
	 * @param item
	 */
	public void displayChildrenOf(IElement item) {
		int numChildren = item.getChildren().size();
		Point pt = getElementPosition(item);
		int c1 = (int) pt.getX() + 1;
		// If the column that will be used to display the children does not
		// already exist -> creation
		if (content_.size()-1 < c1) {
			columnWidths_.add(0);
			content_.add(new ArrayList<IElement>());
		}
		// Remove the content of all the columns next to the one of the given item.
		for (int i=c1; i < content_.size(); i++)
			content_.get(i).clear();
		// Fill the next column with the eventual children of the given item.
		for (int i=0; i < numChildren; i++)
			content_.get(c1).add(item.getChildren().get(i));
		
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return if yes or no the item1 is on the item2.
	 * @param item1
	 * @param item2
	 */
	public boolean isItemOnAnother(IElement item1, IElement item2) {
	
		double itemCoordX = item1.getLocation().getX();
		double itemCoordY = item1.getLocation().getY();
		double binCoordXLeft = item2.getLocation().getX() - gravity_*item2.getSize().width;
		double binCoordXRight = item2.getLocation().getX() + gravity_*item2.getSize().width;
		double binCoordYTop = item2.getLocation().getY() + gravity_*item2.getSize().height;
		double binCoordYDown = item2.getLocation().getY() - gravity_*item2.getSize().height;
		
		if (itemCoordX >= binCoordXLeft && itemCoordX <= binCoordXRight) {
			if (itemCoordY <= binCoordYTop && itemCoordY >= binCoordYDown)
				return true;
		}
		return false;
	}
	
	// ----------------------------------------------------------------------------
	
	public IElement getIElementFromLabel(String label) {
		int numColumns = content_.size();
		int numItems = 0;
		IElement element = null;
		
		for (int i=0; i < numColumns; i++) {
			numItems = content_.get(0).size();
			for (int j=0; j < numItems; j++) {
				element = content_.get(i).get(j);
				if (element.getLabel().equals(label))
					return element;
			}
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
	@SuppressWarnings("serial")
	public void deleteSelectedElement(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DELETE_ELEMENT");
	   jp.getActionMap().put("DELETE_ELEMENT", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   deleteSelectedElement();
		   }
	   });
	}
	
	public void deleteSelectedElement() {
		IElement element = IElement.curItem;
		if (element != null) {
			if (element.isDestroyable()) {
				removeItemFromDesktop(element);
				log.log(Level.INFO, "The network " + element.getLabel() + " and all its children have been deleted!");
			}
		}
	}

    // =======================================================================================
    // GETTERS AND SETTERS
    //
	
	public JDesktopPane getDesktopPane() { return desktopPane_; }
}
