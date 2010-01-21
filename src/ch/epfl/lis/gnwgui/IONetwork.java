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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import ch.epfl.lis.gnwgui.filefilters.FilenameUtilities;
import ch.epfl.lis.gnwgui.filefilters.FilterNetworkAll;
import ch.epfl.lis.gnwgui.filefilters.FilterNetworkDOT;
import ch.epfl.lis.gnwgui.filefilters.FilterNetworkGML;
import ch.epfl.lis.gnwgui.filefilters.FilterNetworkSBML;
import ch.epfl.lis.gnwgui.filefilters.FilterNetworkTSV;
import ch.epfl.lis.gnwgui.filefilters.FilterNetworkTSVDREAM;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnw.TSVParserGNW;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.ios.ParseException;

import javax.swing.filechooser.FileFilter;

/** Implements all the methods to load/save networks from/to files.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class IONetwork {
	
    /** Logger for this class */
    private static Logger log = Logger.getLogger(IONetwork.class.getName());
	
    
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Constructor
	 */
	public IONetwork() {}
	
	
	// ----------------------------------------------------------------------------
	
	public static void open() {
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Open Network",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD, null);
		
		dialog.addFilter(new FilterNetworkTSV());
		dialog.addFilter(new FilterNetworkTSVDREAM());
		dialog.addFilter(new FilterNetworkGML());
		dialog.addFilter(new FilterNetworkDOT());
		dialog.addFilter(new FilterNetworkSBML());
		dialog.addFilter(new FilterNetworkAll());
		
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.display();

		open(dialog.getSelection(), dialog.getSelectedFilter());
	}

	
	
	/**
	 * Instantiate the Open dialog used to load a structure or a dynamical model.
	 */
	public static void open(String absPath, FileFilter f) {
		
		try {
			if (absPath != null) {
				String dir = FilenameUtilities.getDirectory(absPath);
				String filename = FilenameUtilities.getFilenameWithoutPath(absPath);
				URL url = new URL("file://" + absPath);

				if (f == null || f instanceof FilterNetworkAll)
					loadItem(filename, url, null); // open according to the extension
				else if (f instanceof FilterNetworkTSV)
					loadItem(filename, url, ImodNetwork.TSV);
				else if (f instanceof FilterNetworkGML)
					loadItem(filename, url, ImodNetwork.GML);
				else if (f instanceof FilterNetworkDOT)
					loadItem(filename, url, ImodNetwork.DOT);
				else if (f instanceof FilterNetworkSBML)
					loadItem(filename, url, GeneNetwork.SBML);
				else
					throw new Exception("Selected format unhandled!");

				// Save the current path as user path
				GnwSettings.getInstance().setOutputDirectory(dir);
			}
		} catch (FileNotFoundException e) {
			openingFailedDialog("GNW Message", absPath, "File not found!");
			log.log(Level.WARNING, e.getMessage());
		} catch (ParseException e) {
			openingFailedDialog("GNW Message", absPath, "Error occurs during parsing.<br>" +
			"See logs for more information.");
			log.log(Level.WARNING, e.getMessage());
		} catch (Exception e) {
			openingFailedDialog("GNW Message", absPath, "Unhandled exception!<br>" +
			"See logs for more information.");
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Save the given network in a specified format and file. If the extension is
	 * not written by the (lazy) user, it's automatically added. If the specified
	 * file already exists, a dialog box informs the user and allow him to quit.
	 * @param item Network to save
	 */
	public static void saveAs(GenericElement item) {
		
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Save Network",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE, IODialog.SWING);
		
		dialog.addFilter(new FilterNetworkTSV());
		dialog.addFilter(new FilterNetworkTSVDREAM());
		dialog.addFilter(new FilterNetworkGML());
		dialog.addFilter(new FilterNetworkDOT());
		if (item instanceof DynamicalModelElement)
			dialog.addFilter(new FilterNetworkSBML());
		
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.setSelection(item.getLabel());
		dialog.display();
		
		try {
			if (dialog.getSelection() != null) {

				URL url = new URL("file://" + dialog.getSelection());
				FileFilter selectedFilter = dialog.getSelectedFilter();
				String output = null;

				if (selectedFilter instanceof FilterNetworkTSV) {
					String path = "file://" + dialog.getSelection();
					String[] extension = {FilterNetworkTSV.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
						return;

					exportTSVStructure(item, url);
					output = "The network " + item.getLabel() + " was exported in TSV format";
				}
				else if (selectedFilter instanceof FilterNetworkTSVDREAM) {
					String path = "file://" + dialog.getSelection();
					String[] extension = {FilterNetworkTSVDREAM.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
						return;

					exportTSVDREAMStructure(item, url);
					output = "The network " + item.getLabel() + " was exported in DREAM format";
				}
				else if (selectedFilter instanceof FilterNetworkGML) {
					String path = "file://" + dialog.getSelection();
					String[] extension = {FilterNetworkGML.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
						return;

					exportGMLStructure(item, url);
					output = "The network " + item.getLabel() + " was exported in GML format";
				}
				else if (selectedFilter instanceof FilterNetworkDOT) {
					String path = "file://" + dialog.getSelection();
					String[] extension = {FilterNetworkDOT.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
						return;

					exportDOTStructure(item, url);
					output = "The network " + item.getLabel() + " was exported in DOT format";
				}
				else if (selectedFilter instanceof FilterNetworkSBML) {
					String path = "file://" + dialog.getSelection();
					String[] extension = {FilterNetworkSBML.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
						return;

					exportSBMLGeneRegulatoryNetwork(item, url);
					output = "The network " + item.getLabel() + " was exported in SBML format";
				}
				else
					throw new Exception("Error");

				// Save the current path as user path
				GnwSettings.getInstance().setOutputDirectory(dialog.getDirectory());
				log.info(output);
				
				//printSavingInfo(item);
			}
		} catch (MalformedURLException e) {
			savingFailedDialog("GNW Message", dialog.getSelection(), "Malformed URL!<br>" +
			"See logs for more information.");
			log.log(Level.WARNING, e.getMessage());
		} catch (Exception e) {
			savingFailedDialog("GNW Message", dialog.getSelection(), "Unhandled exception!<br>" +
			"See logs for more information.");
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display a popup if an error occurs during the loading of a network.
	 * @param title Title of the dialog box
	 * @param file Absolute path to the network file
	 * @param cause Description of the error
	 */
	public static void openingFailedDialog(String title, String file, String cause) {

		String msg = 
			"Opening '" + file + "' failed:<br><br>" +
			cause;
 		 
 		 msg = "<html>" + msg + "</html>";
			JOptionPane.showMessageDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), msg, title, JOptionPane.WARNING_MESSAGE);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display a popup if an error occurs during the saving of a network.
	 * @param title Title of the dialog box
	 * @param file Absolute path to the network file
	 * @param cause Description of the error
	 */
	public static void savingFailedDialog(String title, String file, String cause) {

		String msg = 
			"Saving '" + file + "' failed:<br><br>" +
			cause;
 		 
 		 msg = "<html>" + msg + "</html>";
			JOptionPane.showMessageDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), msg, title, JOptionPane.WARNING_MESSAGE);
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Export the structure of a network into TSV format.
	 * @param item Network to save
	 * @param absPath Absolute path
	 * @throws MalformedURLException
	 */
	public static void exportTSVStructure(GenericElement item, URL absPath) throws MalformedURLException {

		try {
			if (item instanceof StructureElement)
				((StructureElement)item).getNetwork().saveTSV(absPath); // was saveDREAM
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().saveTSV(absPath); // was saveDREAM
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------

	
	/**
	 * Export the structure of a network into TSV DREAM-compatible format.
	 * @param item Network to save
	 * @param absPath Absolute path
	 * @throws MalformedURLException
	 */
	public static void exportTSVDREAMStructure(GenericElement item, URL absPath) throws MalformedURLException {

		try {
			if (item instanceof StructureElement) {
				//((StructureElement)item).getNetwork().saveDREAM(absPath); // was saveDREAM
				TSVParserGNW parser = new TSVParserGNW(((StructureElement)item).getNetwork(), absPath);
				parser.writeGoldStandard();
			} else if (item instanceof DynamicalModelElement) {
				//((DynamicalModelElement)item).getGeneNetwork().saveDREAM(absPath); // was saveDREAM
				TSVParserGNW parser = new TSVParserGNW(((DynamicalModelElement)item).getGeneNetwork(), absPath);
				parser.writeGoldStandard();
			}
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Export the structure of a network into GML format.
	 * @param item Network to save
	 * @param absPath Absolute path
	 * @throws MalformedURLException
	 */
	public static void exportGMLStructure(GenericElement item, URL absPath) throws MalformedURLException {
			
		try {
			if (item instanceof StructureElement) {
				Structure network = ((StructureElement)item).getNetwork();
				NetworkGraph layout = item.getNetworkViewer();
				if (network.saveNodePosition() && layout != null)
					layout.saveStructureLayout();
				network.saveGML(absPath);
			}
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().saveGML(absPath);
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Export the structure of a network into DOT format.
	 * @param item Network to save
	 * @param absPath Absolute path
	 * @throws MalformedURLException
	 */
	public static void exportDOTStructure(GenericElement item, URL absPath) throws MalformedURLException {
		
		try {
			if (item instanceof StructureElement) {
				Structure network = ((StructureElement)item).getNetwork();
				NetworkGraph layout = item.getNetworkViewer();
				if (network.saveNodePosition() && layout != null)
					layout.saveStructureLayout();
				network.saveDOT(absPath);
			}
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().saveDOT(absPath);
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Export a dynamical model into SBML format.
	 * @param item Network to save
	 * @param absPath Absolute path
	 * @throws MalformedURLException
	 */
	public static void exportSBMLGeneRegulatoryNetwork(GenericElement item, URL absPath) throws MalformedURLException {
		
		try {
			if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().writeSBML(absPath);
			else {
				throw new Exception("Only Gene Regulatory Networks can be exported into SBML format!");
			}
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This function loads a network using the given path and named the network following the given
	 * name. If any format is given, the function select the parser accordingly to the extension of
	 * the file. If the name of the network starts with a "#", the name of the network is the name
	 * of the network file.
	 * @param name Label of the element as printed on the iDesktop
	 * @param absPath Absolute path
	 * @param format File format
	 * @return Information about the loaded network (size, number of interactions, etc.)
	 * @throws FileNotFoundException
	 * @throws ParseException
	 * @throws Exception
	 */
	public static void loadItem(String name, URL absPath, Integer format) throws FileNotFoundException, ParseException, Exception {
		
		boolean type;
		
		if (name.equals("") || name.charAt(0) == '#')
			name = FilenameUtilities.getFilenameWithoutPath(absPath.getPath());
		
		name = FilenameUtilities.getFilenameWithoutExtension(name);

		// If format is null, we select an open strategy according to its extension
		if (format == null) {
			String extension = FilenameUtilities.getExtension(absPath.getPath());
	        if (extension.equals("tsv"))
	        	format = ImodNetwork.TSV;
	        else if (extension.equals("gml"))
	        	format = ImodNetwork.GML;
	        else if (extension.equals("dot"))
	        	format = ImodNetwork.DOT;
	        else if (extension.equals("xml"))
	        	format = GeneNetwork.SBML;
		}
		
		type = false;
		type = (format == ImodNetwork.TSV || type);
		type = (format == ImodNetwork.GML || type);
		type = (format == ImodNetwork.DOT || type);
		
		
		if (type)
			loadStructureItem(name, absPath, format);
		
		type = false;
		type = (format == GeneNetwork.SBML || type);
		
		if (type)
			loadDynamicNetworkItem(name, absPath, format);
		
		// Should not arrived here
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load a network structure and add it to the Network Manager.
	 * @param name Label of the element as printed on the iDesktop
	 * @param absPath Absolute path
	 * @param format File format
	 * @throws FileNotFoundException
	 * @throws ParseException
	 * @throws Exception
	 */
	public static void loadStructureItem(String name, URL absPath, Integer format) throws FileNotFoundException, ParseException, Exception {
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		StructureElement network = new StructureElement(name, global.getNetworkDesktop());

		network.load(absPath, format);
		network.getNetwork().setId(name);
		
		// As DOT format has a place where network Id is defined, we take it
		// as label for the item displayed on the desktop.
		if (format == Structure.DOT)
			network.setText(network.getNetwork().getId());
		
		global.getNetworkDesktop().addItemOnDesktop(network);
		
		printOpeningInfo(network);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load a dynamical model and add it to the Network Manager.
	 * @param name Label of the element as printed on the iDesktop
	 * @param absPath Absolute path
	 * @param format File format
	 * @throws IOException
	 * @throws Exception
	 */
	public static void loadDynamicNetworkItem(String name, URL absPath, Integer format) throws IOException, Exception {
		
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		DynamicalModelElement grn = new DynamicalModelElement(name, global.getNetworkDesktop());
		
		grn.load(absPath, format);
		grn.getGeneNetwork().setId(name);
		global.getNetworkDesktop().addItemOnDesktop(grn);
		
		printOpeningInfo(grn);
	}
	
	
	
	public static void printOpeningInfo(GenericElement item) {
		String[] msg = openingInfo(item);
		log.info(msg[0] + "\n" + msg[1] + "\n"); // print to console
	}
	
	public static String[] openingInfo(GenericElement item) {
		
		String msg[] = {"", ""};
		
		if (item instanceof StructureElement) {
			Structure n = ((StructureElement)item).getNetwork();
			msg[0] = "Loaded network structure";
			msg[1] = n.getId() + " (" + n.getSize() + " nodes, " + n.getNumEdges() + " edges)";
		} else if (item instanceof DynamicalModelElement) {
			GeneNetwork n = ((DynamicalModelElement)item).getGeneNetwork();
			msg[0] = "Loaded dynamical network";
			msg[1] = n.getId() + " (" + n.getSize() + " genes, " + n.getNumEdges() + " regulations)";
		}
		return msg;
	}
	
	/*
	public static void printSavingInfo(GenericElement item) {
		String[] msg = savingInfo(item);
		log.info(msg[0] + "\n" + msg[1] + "\n"); // print to console
	}
	
	public static String[] savingInfo(GenericElement item) {
		
		String msg[] = {"", ""};
		
		if (item instanceof StructureElement) {
			Structure n = ((StructureElement)item).getNetwork();
			msg[0] = "Saved network structure";
			msg[1] = n.getId() + " (" + n.getSize() + " nodes, " + n.getNumEdges() + " edges)";
		} else if (item instanceof DynamicalModelElement) {
			GeneNetwork n = ((DynamicalModelElement)item).getGeneNetwork();
			msg[0] = "Saved dynamical network";
			msg[1] = n.getId() + " (" + n.getSize() + " genes, " + n.getNumEdges() + " regulations)";
		}
		return msg;
	}*/
}