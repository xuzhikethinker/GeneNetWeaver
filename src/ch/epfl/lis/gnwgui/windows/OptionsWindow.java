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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


/** This dialog allows the user to choice one of the several process available for the networks.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class OptionsWindow extends GenericWindow {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** This panel contains all the options for the dynamical models. */
	protected JPanel dynamicNetDisplay_;
	/** This panel contains all the option for the structures. */
	protected JPanel staticNetDisplay_;
	
	/** This panel is displayed in the main and contains neither dynamicNetDisplay_ or staticNetDisplay_. */
	protected JPanel mainDisplay_;
	/** Layout of mainDisplay. */
	protected CardLayout mainDisplayLayout_ = new CardLayout();
	
	/** Dynamical model: export model as file */
	protected JButton exportDM_;
	/** Dynamical model: generate benchmark */
	protected JButton simulationDM_;
	/** Dynamical model: extract subnets */
	protected JButton subnetExtractionDM_;
	/** Dynamical model: graph visualization */
	protected JButton visualizationDM_;
	/** Dynamical model: rename network */
	protected JButton renameDM_;
	/** Structure: export model as file */
	protected JButton exportStructure_;
	/** Structure: conversion structure into dynamical model */
	protected JButton conversionStructure_;
	/** Structure: extract subnets */
	protected JButton subnetExtractionStructure_;
	/** Structure: graph visualization */
	protected JButton visualizationStructure_;
	/** Strcuture: rename network */
	protected JButton renameStructure_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(OptionsWindow.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor
	 */
	public OptionsWindow(Frame aFrame) {
		
		super(aFrame, true);
		setTitle2("Select a Task");
		setTitle("Options");
		setBounds(100, 100, 420, 380);

		mainDisplay_ = new JPanel();
		mainDisplay_.setLayout(mainDisplayLayout_);
		mainDisplay_.setBackground(Color.WHITE);
		getContentPane().add(mainDisplay_, BorderLayout.CENTER);

		staticNetDisplay_ = new JPanel();
		staticNetDisplay_.setBorder(new EmptyBorder(30, 80, 30, 80));
		final GridLayout gridLayout = new GridLayout(0, 1);
		gridLayout.setVgap(10);
		staticNetDisplay_.setLayout(gridLayout);
		staticNetDisplay_.setBackground(Color.WHITE);
		staticNetDisplay_.setName("staticNetDisplay_");
		mainDisplay_.add(staticNetDisplay_, staticNetDisplay_.getName());

		renameStructure_ = new JButton();
		renameStructure_.setFont(new Font("Sans", Font.PLAIN, 11));
		renameStructure_.setPreferredSize(new Dimension(70, 70));
		renameStructure_.setText("Rename");
		staticNetDisplay_.add(renameStructure_);

		visualizationStructure_ = new JButton();
		visualizationStructure_.setFont(new Font("Sans", Font.PLAIN, 11));
		visualizationStructure_.setPreferredSize(new Dimension(70, 70));
		visualizationStructure_.setText("<html><center>Visualization<br>(Slow for networks > 200 nodes)</center></html>");
		staticNetDisplay_.add(visualizationStructure_);

		subnetExtractionStructure_ = new JButton();
		subnetExtractionStructure_.setFont(new Font("Sans", Font.PLAIN, 11));
		subnetExtractionStructure_.setPreferredSize(new Dimension(70, 70));
		subnetExtractionStructure_.setText("Subnetwork Extraction");
		staticNetDisplay_.add(subnetExtractionStructure_);

		conversionStructure_ = new JButton();
		conversionStructure_.setFont(new Font("Sans", Font.PLAIN, 11));
		conversionStructure_.setPreferredSize(new Dimension(70, 70));
		conversionStructure_.setText("Generate Kinetic Model");
		staticNetDisplay_.add(conversionStructure_);

		dynamicNetDisplay_ = new JPanel();
		dynamicNetDisplay_.setBorder(new EmptyBorder(30, 80, 30, 80));
		final GridLayout gridLayout_1 = new GridLayout(0, 1);
		gridLayout_1.setVgap(10);
		dynamicNetDisplay_.setLayout(gridLayout_1);
		dynamicNetDisplay_.setBackground(Color.WHITE);
		dynamicNetDisplay_.setName("dynamicNetDisplay_");
		mainDisplay_.add(dynamicNetDisplay_, dynamicNetDisplay_.getName());

		renameDM_ = new JButton();
		renameDM_.setPreferredSize(new Dimension(70, 70));
		renameDM_.setFont(new Font("Dialog", Font.PLAIN, 11));
		renameDM_.setText("Rename");
		dynamicNetDisplay_.add(renameDM_);

		visualizationDM_ = new JButton();
		visualizationDM_.setPreferredSize(new Dimension(70, 70));
		visualizationDM_.setFont(new Font("Dialog", Font.PLAIN, 11));
		visualizationDM_.setText("<html><center>Visualization<br>Slow for networks > 200 nodes</center></html>");
		dynamicNetDisplay_.add(visualizationDM_);

		subnetExtractionDM_ = new JButton();
		subnetExtractionDM_.setPreferredSize(new Dimension(70, 70));
		subnetExtractionDM_.setFont(new Font("Dialog", Font.PLAIN, 11));
		subnetExtractionDM_.setText("Subnetwork Extraction");
		dynamicNetDisplay_.add(subnetExtractionDM_);

		simulationDM_ = new JButton();
		simulationDM_.setPreferredSize(new Dimension(70, 70));
		simulationDM_.setFont(new Font("Dialog", Font.PLAIN, 11));
		simulationDM_.setText("Generate Datasets");
		dynamicNetDisplay_.add(simulationDM_);

		exportDM_ = new JButton();
		exportDM_.setPreferredSize(new Dimension(70, 70));
		exportDM_.setFont(new Font("Dialog", Font.PLAIN, 11));
		exportDM_.setText("Export Network (TSV, GML, DOT, SBML)");
		dynamicNetDisplay_.add(exportDM_);

		exportStructure_ = new JButton();
		exportStructure_.setFont(new Font("Sans", Font.PLAIN, 11));
		exportStructure_.setPreferredSize(new Dimension(70, 70));
		exportStructure_.setText("Export Network (TSV, GML, DOT)");
		staticNetDisplay_.add(exportStructure_);
		
		setLocationRelativeTo(aFrame);
	}
}
