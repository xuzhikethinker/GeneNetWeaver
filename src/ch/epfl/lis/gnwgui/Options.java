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

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.windows.OptionsWindow;
import ch.epfl.lis.imod.ImodNetwork;

/** Allows the user to choice one of the several process available applicable on
 * a network structure or dynamical model.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class Options extends OptionsWindow {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** NetworkItem related to this option box. */
	private GenericElement item_ = null;
	
    /** Logger for this class */
	private static Logger log = Logger.getLogger(Options.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	public Options(Frame aFrame, GenericElement item) {
		
		super(aFrame);
		
		item_ = item;

		String title1, title2;
		title1 = title2 = "";
		if (item_ instanceof StructureElement) {
			mainDisplayLayout_.show(mainDisplay_, staticNetDisplay_.getName());
			ImodNetwork network = ((StructureElement)item_).getNetwork();
			title1 = item_.getLabel();
			title2 = /*"Structure, " + */network.getSize() + " nodes, " + network.getNumEdges() + " edges";
		} else if (item_ instanceof DynamicalModelElement) {
			mainDisplayLayout_.show(mainDisplay_, dynamicNetDisplay_.getName());
			GeneNetwork geneNetwork = ((DynamicalModelElement)item_).getGeneNetwork();
			title1 = item_.getLabel();
			title2 = /*"GRN, " + */geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
		}
		setGeneralInformation(title1 + " (" + title2 + ")");
		
		/**
		 * ACTIONS
		 */
		visualizationStructure_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				dispose();
				viewNetwork(item_);
			}
		});
		
		exportStructure_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				exportNetwork();
			}
		});
		
		conversionStructure_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				conversion2DynamicalModel();
			}
		});

		renameStructure_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				renameAction();
			}
		});
		
		subnetExtractionStructure_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				dispose();
				subnetworkExtraction(item_);
			}
		});
		
		renameDM_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				renameAction();
			}
		});
		
		visualizationDM_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				dispose();
				viewNetwork(item_);
			}
		});
		
		exportDM_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				exportNetwork();
			}
		});
		
		subnetExtractionDM_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				dispose();
				subnetworkExtraction(item_);
			}
		});
		
		simulationDM_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				try {
					dispose();
					generateDREAM3GoldStandard(item_);
				} catch (Exception e) {
					log.warning(e.getMessage());
				}
			}
		});
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Convert a structure into a dynamical model. The item of the structure on the
	 * desktop is also transformed into a dynamical model item.
	 */
	public void conversion2DynamicalModel() {

		JOptionPane optionPane = new JOptionPane();
		Object msg[] = {"Do you want to remove autoregulatory interactions ?"};
		optionPane.setMessage(msg);
		optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		optionPane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
		JDialog dialog = optionPane.createDialog(this, "Dynamical model");
		dialog.setVisible(true);
		Object value = optionPane.getValue();
	   
		if (value == null || !(value instanceof Integer)) {
			log.log(Level.WARNING, "Wrong returned value.");
			return;
		}
		else {
			int i = ((Integer)value).intValue();
			if (i == JOptionPane.CLOSED_OPTION || i == JOptionPane.CANCEL_OPTION) {
				return;
			}
			else if (i == JOptionPane.OK_OPTION) {
				// Close the options dialog
				enterAction();
				// Remove autoregulatory interactions
				((StructureElement) item_).getNetwork().removeAutoregulatoryInteractions();
				log.info("Autoregulatory interactions are removed from network " + ((StructureElement) item_).getNetwork().getId() + "!");
			}
			else if (i == JOptionPane.NO_OPTION)
				enterAction();
		}
		
		// Set the cursor in waiting mode
		GnwGuiSettings.getInstance().getGnwGui().getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		GnwGuiSettings.getInstance().getGnwGui().getFrame().repaint(100);

		GnwGuiSettings global = GnwGuiSettings.getInstance();
		StructureElement staticNetwork = (StructureElement) item_;
		
		// Create a new dynamic network from a static one and initialize its parameters.
		DynamicalModelElement grnItem = new DynamicalModelElement(staticNetwork);
		grnItem.getGeneNetwork().randomInitialization();
		
		if (item_.getFather() != null) {
			int index = staticNetwork.getFather().getChildren().indexOf(item_);
			staticNetwork.getFather().getChildren().remove(item_);
			staticNetwork.getFather().getChildren().add(index, grnItem);
		}
		
		global.getNetworkDesktop().replaceItem(staticNetwork, grnItem);
		GnwGuiSettings.getInstance().getGnwGui().getFrame().setCursor(Cursor.getDefaultCursor());
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to generate benchmarks.
	 * @throws Exception
	 */
	public static void generateDREAM3GoldStandard(GenericElement item) throws Exception {
		Simulation rd = new Simulation(new Frame(), item);
		rd.setVisible(true);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Open a dialog to rename the network.
	 */
	public void renameAction() {
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Rename rd = new Rename(global.getGnwGui().getFrame(), item_);
		escapeAction();
		rd.setVisible(true);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to visualize the network graph.
	 * @param item
	 */
	public static void viewNetwork(GenericElement item) {
		GraphViewer dialog = new GraphViewer(new Frame(), item);
		dialog.setVisible(true);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to extract one or several subnetworks.
	 */
	public static void subnetworkExtraction(GenericElement item) {
		SubnetExtraction dialog = new SubnetExtraction(new Frame(), item);
//		escapeAction();
		dialog.setVisible(true);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to export the present network.
	 */
	public void exportNetwork() {
		escapeAction();
		IONetwork.saveAs(item_);
	}
}
