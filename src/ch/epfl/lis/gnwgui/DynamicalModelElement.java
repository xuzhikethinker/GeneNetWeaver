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

package ch.epfl.lis.gnwgui;


import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;

/** Extends the generic network element to define dynamical models.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class DynamicalModelElement extends GenericElement {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Instance of GeneNetwork linked to this object. */
	private GeneNetwork geneNetwork_ = null;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(DynamicalModelElement.class.getName());
	
    
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Constructor
	 * @param label Label of the element
	 */
	public DynamicalModelElement(String label) {
		super(label);
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param label Label of the element
	 * @param desk Reference to the iDesktop
	 */
	public DynamicalModelElement(String label, IDesktop desk) {
		super(label, desk);
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor<br>
	 * WARNING: All the children of item will see their parent changed and be
	 * the element given as parameter.
	 * @param item Element representing a network (dynamical model)
	 */
	public DynamicalModelElement(DynamicalModelElement item) {
		super(item);
		initialize();
		for (int i=0; i < children_.size(); i++)
			children_.get(i).setFather(this);
		this.geneNetwork_ = item.geneNetwork_;
		setSize(new Dimension((int) getPreferredWidth(), (int) height_));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor<br>
	 * WARNING: All the children of item will see their parent changed and be
	 * the element given as parameter.
	 * @param item Element representing a network (structure)
	 */
	public DynamicalModelElement(StructureElement item) {
		super(item);
		initialize();
		for (int i=0; i < children_.size(); i++)
			children_.get(i).setFather(this);
		geneNetwork_ = new GeneNetwork(item.getNetwork());
		setSize(new Dimension((int) getPreferredWidth(), (int) height_));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Copy function
	 */
	public DynamicalModelElement copy() {
		return new DynamicalModelElement(this);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialization
	 */
	public void initialize() {	
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		setItemLabelSelecColor(global.getItemSelectedBgcolor());
		Image icon = new ImageIcon(global.getGrnIcon()).getImage();
		setItemIcon(icon);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load a dynamical model from an URL and a given file format.
	 * @param path URL to the file to load
	 * @param format File format
	 * @throws Exception
	 */
	public void load(URL path, int format) throws Exception, Exception {
		geneNetwork_ = new GeneNetwork();
		geneNetwork_.load(path, format);
	}
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	public void setGeneNetwork(GeneNetwork geneNetwork) { geneNetwork_ = geneNetwork; }
	public GeneNetwork getGeneNetwork() { return geneNetwork_; }
}
