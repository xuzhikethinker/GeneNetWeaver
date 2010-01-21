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


import java.awt.Image;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.ios.ParseException;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;

/** Extends the generic network element to define network structures.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class StructureElement extends GenericElement {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Instance of BasicNetwork or one of its subclasses. */
	private ImodNetwork network_ = null;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(StructureElement.class.getName());
	
    
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Constructor
	 * @param label Label of the element.
	 */
	public StructureElement(String label) {
		super(label);
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param label Label of the element.
	 * @param desk IDestop on which the element is added.
	 */
	public StructureElement(String label, IDesktop desk) {
		super(label, desk);
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param item An existing StaticNetwork
	 */
	public StructureElement(StructureElement item) {
		super(item);
		this.network_ = item.network_;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Copy function
	 */
	public StructureElement copy() {
		return new StructureElement(this);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialization
	 */
	public void initialize() {		
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		setItemLabelSelecColor(global.getItemSelectedBgcolor());
		Image icon = new ImageIcon(global.getStructureIcon()).getImage();
		setItemIcon(icon);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load a structure.
	 * @param path
	 * @param format
	 * @throws FileNotFoundException
	 * @throws ParseException
	 * @throws Exception
	 */
	public void load(URL path, int format) throws FileNotFoundException, ParseException, Exception {
		network_ = new ImodNetwork();
		network_.load(path, format);
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void setNetwork(ImodNetwork network) { network_ = network; }
	public ImodNetwork getNetwork() { return network_; }
}
