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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.windows.RenameWindow;

/** This dialog is used to rename a network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class Rename extends RenameWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Interactive element (network item are IElement in GNW) to rename. */
	private IElement item_ = null;
	
	/** Document associated to the text field newName_ */
	private Document newNameDocument_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(Rename.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     * @param aFrame
     * @param item
     */
	public Rename(Frame aFrame, IElement item) {
		
		super(aFrame);

		item_ = item;
		
		if (item != null) {
			newName_.setText(item.getLabel());
			newName_.selectAll();
		}
		
		String title1, title2;
		title1 = title2 = "";
		if (item_ instanceof StructureElement) {
			ImodNetwork network = ((StructureElement)item_).getNetwork();
			title1 = item_.getLabel();
			title2 = network.getSize() + " nodes, " + network.getNumEdges() + " edges";
		} else if (item_ instanceof DynamicalModelElement) {
			GeneNetwork geneNetwork = ((DynamicalModelElement)item_).getGeneNetwork();
			title1 = item_.getLabel();
			title2 = geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
		}
		setGeneralInformation(title1 + " (" + title2 + ")");

		/**
		 * ACTIONS
		 */
		// If the document tells us that the text field is empty -> apply button disable
		newNameDocument_ = newName_.getDocument();
		newNameDocument_.addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				applyButton.setEnabled(!newName_.getText().equals(""));
			}
			public void insertUpdate(DocumentEvent arg0) {
				applyButton.setEnabled(!newName_.getText().equals(""));
			}
			public void removeUpdate(DocumentEvent arg0) {
				applyButton.setEnabled(!newName_.getText().equals(""));
			}
		});
		
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				enterAction();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				escapeAction();
			}
		});
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Redefines the function enterAction instantiated in the class DialogReactive.
	 * After validation, the related item is renamed.
	 */
	@Override
	public void enterAction() {
		// Change the name of the network
		if (item_ instanceof StructureElement)
			((StructureElement)item_).getNetwork().setId(newName_.getText());
		else if (item_ instanceof DynamicalModelElement)
			((DynamicalModelElement)item_).getGeneNetwork().setId(newName_.getText());
		
		// Change the label displayed on the desktop
		item_.setLabel(newName_.getText());
		
		GnwGuiSettings.getInstance().getNetworkDesktop().recalculateColumnWidths(item_);
		dispose();
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS

	public void setNewName(String text) { newName_.setText(text); }
	public String getNewName() { return newName_.getText(); }
}
