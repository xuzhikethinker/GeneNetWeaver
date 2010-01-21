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

package ch.epfl.lis.gnw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.ios.ParseException;
import ch.epfl.lis.networks.ios.TSVParser;


/** 
 * Extends the basic TSVParser to load and save some formats specific to GNW.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */ 
public class TSVParserGNW extends TSVParser { 

	
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(TSVParserGNW.class.getName());
	
	
	// ============================================================================
	// PUBLIC FUNCTIONS
	
	/**
	 * Constructor, calls super constructor
	 */
	public TSVParserGNW(Structure struct){
		super(struct);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor, calls super constructor
	 */
	public TSVParserGNW(ImodNetwork struct, URL absPath) {
		super(struct, absPath);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Read a RegulonDB flat file of TF-gene interactions.
	 * 
	 * 1. Download the original file from RegulonDB
	 * (http://regulondb.ccg.unam.mx/html/Data_Sets.jsp, File 1. TF-gene interactions).
	 * It is in TSV format, the TF is in column 2, the target in column 4, and the type 
	 * of interaction in column 6 (+ activator, - repressor, +- dual, ? unknown). Note
	 * that they actually also have some "+?", it's not clear to me what that's supposed to
	 * mean, so I will treat them like unknowns. There are also some interactions of TFs with
	 * "Phantom genes", I also don't understand what that is and will ignore them.
	 * I didn't find any documentation on their formats (or on anything else) on their website...
	 * 
	 * 2. Remove the header and the few lines with the phantom genes manually before parsing the file.
	 * 
	 * @throws Exception
	 * @throws ParseException
	 */
	public void readRegulonDB() throws Exception, ParseException, FileNotFoundException {
		
		if (structure_ == null) {
			error_ = "read: Instance of Structure is null!";
			throw new Exception(error_);
		}

		ArrayList<String[]> rawData = readTSV(absPath_);
		
		for (int i=0; i < rawData.size(); i++) {
			try {
				
				// The TF. Format: "geneA|" or "geneA|geneB|"
				String tfIds[] = rawData.get(i)[1].split("\\|");
				if (tfIds.length == 0)
					throw new ParseException("Line " + (i+1) + ", column 2: missing '|'");
				
				addNode(tfIds[0]);				
				if (tfIds.length == 2)
					addNode(tfIds[1]);			
				if (tfIds.length > 2)
					throw new ParseException("Line " + (i+1) + ", column 2: more than two '|'");
				
				// The Target. Format: geneA
				String targetId = rawData.get(i)[3];
				addNode(targetId);
				
				// Add the edges
				// We treat the +? like a ?
				if (rawData.get(i)[5].equalsIgnoreCase("+?"))
					rawData.get(i)[5] = "?";
				addEdge(tfIds[0], targetId, rawData.get(i)[5]);
				
				if (tfIds.length == 2)
					addEdge(tfIds[1], targetId, rawData.get(i)[5]);
				
			} catch (ArrayIndexOutOfBoundsException aioobu) {
				error_ = "read: " + aioobu.getMessage();
				throw new RuntimeException(error_);
			}
		}
				
		File f = new File(absPath_.getPath());
		structure_.setId(f.getName());
		structure_.setComment("");
		structure_.setDirected(true);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Reads the Drosophila regulatory network that Pouya sent me.
	 */
	public void readDrosophilaPouya() throws Exception, ParseException, FileNotFoundException {
		
		if (structure_ == null) {
			error_ = "read: Instance of Structure is null!";
			throw new Exception(error_);
		}

		ArrayList<String[]> rawData = readTSV(absPath_);
		absPath_ = new URL("file:///Users/marbach/Java/gnw/resources/dream4/drosophila/name-syn.tsv");
		ArrayList<String[]> geneNames = readTSV(absPath_);
		absPath_ = new URL("file:///Users/marbach/Java/gnw/resources/dream4/drosophila/name-syn-tf.tsv");
		ArrayList<String[]> tfNames = readTSV(absPath_);
		
		for (int l=0; l<rawData.size(); l++) {
			try {
				
				String[] line = rawData.get(l);
				
				String regulatorName = line[0];
				String regulatorCG1 = null; // some reguators correspond to two CGs
				String regulatorCG2 = null;
				regulatorName = regulatorName.substring(0, regulatorName.indexOf('_'));
				
				// search for the name<->CG correspondence in geneNames
				for (int i=0; i<geneNames.size(); i++) {
					if (regulatorName.equals(geneNames.get(i)[0])) {
						regulatorCG1 = geneNames.get(i)[1];
						break;
					}
				}
				// search for the name<->CG correspondence in tfNames
				for (int i=0; i<tfNames.size(); i++) {
					if (regulatorName.equals(tfNames.get(i)[0])) {
						if (regulatorCG1 == null)
							regulatorCG1 = tfNames.get(i)[1];
						else {
							regulatorCG2 = tfNames.get(i)[1];
							break;
						}
					}
				}
				
				if (regulatorCG1 == null) {
					System.out.println("Motif ignored because no CG available: " + regulatorName);
					continue;
				}

				addNode(regulatorCG1);
				if (regulatorCG2 != null)
					addNode(regulatorCG2);
				
				for (int i=1; i<line.length; i++) {
					addNode(line[i]);
					addEdge(regulatorCG1, line[i], Edge.UNKNOWN_STRING);
					if (regulatorCG2 != null)
						addEdge(regulatorCG2, line[i], Edge.UNKNOWN_STRING);
				}
				
			} catch (ArrayIndexOutOfBoundsException aioobu) {
				error_ = "read: " + aioobu.getMessage();
				throw new RuntimeException(error_);
			}
		}
				
		File f = new File(absPath_.getPath());
		structure_.setId(f.getName());
		structure_.setComment("");
		structure_.setDirected(true);
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Write the network structure to a file in the format used for the DREAM gold standards
	 * @throws Exception
	 * @throws ParseException
	 */
	public void writeGoldStandard() {
		
		log_.log(Level.INFO, "Writing file " + absPath_.getPath());
		
		if (structure_ == null) {
			error_ = "write: Instance of Structure is null!";
			throw new RuntimeException(error_);
		}
		
		try { 
			FileWriter fw = new FileWriter(absPath_.getPath(), false);
			int numNodes = structure_.getSize();
			boolean noSelfLoops = GnwSettings.getInstance().getIgnoreAutoregulatoryInteractionsInEvaluation();
			
			// Get the adjacency matrix
			GraphUtilities util = new GraphUtilities((ImodNetwork) structure_);
			boolean[][] A = util.getAdjacencyMatrix();
			
			// Write the present edges
			for (int i=0; i<numNodes; i++)
				for (int j=0; j<numNodes; j++)
					if (A[j][i] == true && (!noSelfLoops || i != j))
						fw.write(structure_.getNode(i).getLabel() + "\t" + structure_.getNode(j).getLabel() + "\t1\n");
			
			// Write the zero edges
			for (int i=0; i<numNodes; i++)
				for (int j=0; j<numNodes; j++)
					if (A[j][i] == false && (!noSelfLoops || i != j))
						fw.write(structure_.getNode(i).getLabel() + "\t" + structure_.getNode(j).getLabel() + "\t0\n");
			
			fw.close();

		} catch (IOException fe) {
			error_ = "write: " + fe.getMessage();
			throw new RuntimeException(error_);
		}
	}

}
