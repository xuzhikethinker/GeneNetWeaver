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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ch.epfl.lis.gnwgui.idesktop.IBin;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;
import ch.epfl.lis.gnwgui.idesktop.IElement;

/** Extends a iDesktop to display open, bin and network icons.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class NetworkManager extends IDesktop {
	
	/** Instance of the bin which will be placed on the desktop. */
	private IBin bin_ = null;
	/** Import item */
	private IElement import_ = null;
	
    /** Logger for this class */
	private static Logger log = Logger.getLogger(NetworkManager.class.getName());
	
    
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     * @param name Name of the desktop
     */
	public NetworkManager(String name) {
		super(name);
		init();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialization
	 * The desktop is created with a bin and the item "Open" used to allow the user
	 * to import networks.
	 */
	@SuppressWarnings("serial")
	public void init() {
		
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		
		/**
		 * Create and initialize an item which will be used to import the
		 * networks onto the desktop, and finally place it on the desktop.
		 */
		import_ = new IElement("Open", this) {
			
			@Override
			public void leftMouseButtonInvocationSimple() {}
			public void wheelMouseButtonInvocation() {}
			
			public void rightMouseButtonInvocation() {
				IONetwork.open();
			}
			
			protected void leftMouseButtonInvocationDouble() {
				IONetwork.open();
			}
		};
		import_.setItemIcon(new ImageIcon(global.getImportNetworkIcon()).getImage());
		import_.setDestroyable(false);
		import_.setToolTipText("<html>Import a network structure or a kinetic network model</html>");
		addItemOnDesktop(import_);
		
		/**
		 * Create and initialize a bin and place it on the desktop.
		 */
		bin_ = new IBin(this, "Recycle Bin");
		bin_.setEmptyIcon(new ImageIcon(global.getBinEmptyIcon()).getImage());
		bin_.setFilledIcon(new ImageIcon(global.getBinFullIcon()).getImage());
		bin_.setToolTipText("Drag-and-drop networks to delete (cannot be undone)");
		addItemOnDesktop(bin_);
		
		displayOptionsDialog(desktopPane_);
		displayRenameDialog(desktopPane_);
		displayExtractionDialog(desktopPane_);
		displayVisualizationDialog(desktopPane_);
		displayOpenDialog(desktopPane_);
		displaySaveDialog(desktopPane_);
		displayBenchmarkDialog(desktopPane_);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when an item is released. For instance, if the item is released on
	 * the bin and is destroyable, the item is remove from the desktop.
	 */
	public void itemReleased(IElement item) {
		
		if (isItemOnAnother(item, bin_)) {
			if (!item.equals(bin_) && item.isDestroyable()) {
				bin_.addItemIntoBin(item);
				removeItemFromDesktop(item); // Step2: Remove the item from the desktop
			}
		}
		repaintDesktop();
		desktopPane_.repaint();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Show the options window when the user presses ENTER on an element from
	 * the desktop.
	 */
	@SuppressWarnings("serial")
	public void displayOptionsDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_OPTIONS_DIALOG");
	   jp.getActionMap().put("DISPLAY_OPTIONS_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayOptionsDialog();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Action to do when F2 or R are pressed.
	 */
	@SuppressWarnings("serial")
	public void displayRenameDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
	   KeyStroke k2 = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_RENAME_DIALOG");
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k2, "DISPLAY_RENAME_DIALOG2");
	   
	   jp.getActionMap().put("DISPLAY_RENAME_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayRenameDialog();
		   }
	   });
	   
	   jp.getActionMap().put("DISPLAY_RENAME_DIALOG2", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayRenameDialog();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Action to do when E is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayExtractionDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_EXTRACTION_DIALOG");
	   jp.getActionMap().put("DISPLAY_EXTRACTION_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayExtractionDialog();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Action to do when V is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayVisualizationDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_V, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_VISUALIZATION_DIALOG");
	   jp.getActionMap().put("DISPLAY_VISUALIZATION_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayVisualizationDialog();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Action to do when O (letter) is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayOpenDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_O, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_OPEN_DIALOG");
	   jp.getActionMap().put("DISPLAY_OPEN_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayOpenDialog();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Action to do when S is pressed.
	 */
	@SuppressWarnings("serial")
	public void displaySaveDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_SAVE_DIALOG");
	   jp.getActionMap().put("DISPLAY_SAVE_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displaySaveDialog();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Action to do when B is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayBenchmarkDialog(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_BENCHMARK_DIALOG");
	   jp.getActionMap().put("DISPLAY_BENCHMARK_DIALOG", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   displayBenchmarkDialog();
		   }
	   });
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Calls the options window of the selected element.
	 */
	public void displayOptionsDialog() {
		IElement element = IElement.curItem;
		
		if (element == import_) {
			IONetwork.open();
			return;
		}
		
		if (element != null && element != bin_) {
			Options dialog = null;
			if (element instanceof StructureElement)
				dialog = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				dialog = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (DynamicalModelElement) element);
			else
				dialog = null;
			
			if (dialog != null) {
				dialog.setVisible(true);
				return;
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open the dialog "Rename" if the selected item is a network.
	 */
	public void displayRenameDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			Rename dialog = null;
			if (element instanceof StructureElement)
				dialog = new Rename(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				dialog = new Rename(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (DynamicalModelElement) element);
			else
				dialog = null;
			
			if (dialog != null) {
				dialog.setVisible(true);
				return;
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open the dialog "Subnet Extractions" if the selected item is a network.
	 */
	public void displayExtractionDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			if (element instanceof StructureElement)
				Options.subnetworkExtraction((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				Options.subnetworkExtraction((DynamicalModelElement) element);
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open the "Visualization" dialog if the selected item is a network.
	 */
	public void displayVisualizationDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			if (element instanceof StructureElement)
				Options.viewNetwork((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				Options.viewNetwork((DynamicalModelElement) element);
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open the "Opening network" dialog.
	 */
	public void displayOpenDialog() {
		IONetwork.open();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Open the "Saving network" dialog.
	 */
	public void displaySaveDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			if (element instanceof StructureElement)
				IONetwork.saveAs((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				IONetwork.saveAs((DynamicalModelElement) element);
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open the "Benchmark generator" dialog if the selected item is a dynamical model.
	 */
	public void displayBenchmarkDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_ && element instanceof DynamicalModelElement) {
			try {
				Options.generateDREAM3GoldStandard((DynamicalModelElement) element);
			} catch (Exception e) {
				log.warning(e.getMessage());
			}
		}
	}
}
