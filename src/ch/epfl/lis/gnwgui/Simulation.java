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

import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;

import ch.epfl.lis.gnw.BenchmarkGenerator;
import ch.epfl.lis.gnw.CancelException;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.windows.SimulationWindow;
import ch.epfl.lis.gnwgui.windows.WaitingSnake;
import ch.epfl.lis.imod.ImodNetwork;


/** This dialog handles all the simulations parameters of steady-states and
 * time-series experiments.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class Simulation extends SimulationWindow {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** NetworkItem to simulate */
	private GenericElement item_ = null;
	
    /** Logger for this class */
    private static Logger log = Logger.getLogger(Simulation.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Constructor
	 */
	public Simulation(Frame aFrame, GenericElement item) {
		
		super(aFrame);
		item_ = item;
		
		GnwSettings settings = GnwSettings.getInstance();
		
		// Model
		model_.setModel(new DefaultComboBoxModel(new String[] {"Deterministic (ODEs)", "Stochastic (SDEs)", "Run both (ODEs and SDEs)"}));
		if (settings.getSimulateODE() && !settings.getSimulateSDE())
			model_.setSelectedIndex(0);
		else if (!settings.getSimulateODE() && settings.getSimulateSDE())
			model_.setSelectedIndex(1);
		else if (settings.getSimulateODE() && settings.getSimulateSDE())
			model_.setSelectedIndex(2);
		
		// Experiments
		wtSS_.setSelected(true);
		wtSS_.setEnabled(false);
		
		knockoutSS_.setSelected(settings.generateSsKnockouts());
		knockdownSS_.setSelected(settings.generateSsKnockdowns());
		multifactorialSS_.setSelected(settings.generateSsMultifactorial());
		dualKnockoutSS_.setSelected(settings.generateSsDualKnockouts());
		
		knockoutTS_.setSelected(settings.generateTsKnockouts());
		knockdownTS_.setSelected(settings.generateTsKnockdowns());
		multifactorialTS_.setSelected(settings.generateTsMultifactorial());
		dualKnockoutTS_.setSelected(settings.generateTsDualKnockouts());
		
		timeSeriesAsDream4_.setSelected(settings.generateTsDREAM4TimeSeries());
		
		// Set model of "number of time series" spinner
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(10000);
		model.setStepSize(1);
		model.setValue(settings.getNumTimeSeries());
		numTimeSeries_.setModel(model);
		
		// Set model of "duration" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(100000);
		model.setStepSize(10);
		model.setValue((int) settings.getMaxtTimeSeries());
		tmax_.setModel(model);
		
		// Set model of "number of points per time series" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(3);
		model.setMaximum(100000);
		model.setStepSize(10);
		
		double dt = settings.getDt();
		double maxt = settings.getMaxtTimeSeries();
		int numMeasuredPoints = (int)Math.round(maxt/dt) + 1;

		if (dt*(numMeasuredPoints-1) != maxt)
			throw new RuntimeException("Duration of time series (GnwSettings.maxtTimeSeries_) must be a multiple of the time step (GnwSettings.dt_)");
		
		model.setValue(numMeasuredPoints);
		numPointsPerTimeSeries_.setModel(model);
		
		perturbationNew_.setSelected(!settings.getLoadPerturbations());
		perturbationLoad_.setSelected(settings.getLoadPerturbations());
		
		// Noise
		
		// Diffusion multiplier (SDE only)
		model = new SpinnerNumberModel();
		model.setMinimum(0.0);
		model.setMaximum(10.);
		model.setStepSize(0.01);
		model.setValue(settings.getNoiseCoefficientSDE());
		sdeDiffusionCoeff_.setModel(model);
		
		noNoise_.setSelected(!settings.getAddMicroarrayNoise() && !settings.getAddNormalNoise() && !settings.getAddLognormalNoise());
		useMicroarrayNoise_.setSelected(settings.getAddMicroarrayNoise());
		useLogNormalNoise_.setSelected(settings.getAddNormalNoise() || settings.getAddLognormalNoise());
		addGaussianNoise_.setSelected(settings.getAddNormalNoise());
		addLogNormalNoise_.setSelected(settings.getAddLognormalNoise());
		
		// Set model of "Gaussian noise std" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(0.000001);
		model.setMaximum(10.);
		model.setStepSize(0.01);
		model.setValue(settings.getNormalStdev());
		gaussianNoise_.setModel(model);
		
		// Set model of "log-normal noise std" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(0.000001);
		model.setMaximum(10.);
		model.setStepSize(0.01);
		model.setValue(settings.getLognormalStdev());
		logNormalNoise_.setModel(model);
		
		normalizeNoise_.setSelected(settings.getNormalizeAfterAddingNoise());
		
		// Set the text field with the user path
		userPath_.setText(GnwSettings.getInstance().getOutputDirectory());
		
		setModelAction();
		setExperimentAction();
		setNoiseAction();
		
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
		
		// Set tool tips for all elements of the window
		addTooltips();
	
		/**
		 * ACTIONS
		 */
		
		model_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setModelAction();
			}
		});
		
		dream4Settings_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setDream4Settings();
			}
		});

		browse_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				
				IODialog dialog = new IODialog(new Frame(""), "Select Target Folder", 
						GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD, null);
				
				dialog.selectOnlyFolder(true);
				dialog.display();
				
				if (dialog.getSelection() != null)
					userPath_.setText(dialog.getSelection());
			}
		});
		
		runButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				enterAction();
			}
		});
		
		cancelButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				GnwSettings.getInstance().stopBenchmarkGeneration(true);
				escapeAction();
			}
		});
		
		knockoutSS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		knockdownSS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		multifactorialSS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		dualKnockoutSS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		knockoutTS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		knockdownTS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		multifactorialTS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		dualKnockoutTS_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		timeSeriesAsDream4_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setExperimentAction();
			}
		});
		
		noNoise_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setNoiseAction();
			}
		});
		
		useMicroarrayNoise_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setNoiseAction();
			}
		});
		
		useLogNormalNoise_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setNoiseAction();
			}
		});
		
		addGaussianNoise_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setNoiseAction();
			}
		});
		
		addLogNormalNoise_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				setNoiseAction();
			}
		});
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public void setModelAction() {
		boolean useSDE = model_.getSelectedIndex() == 1 || model_.getSelectedIndex() == 2;
		sdeDiffusionCoeff_.setEnabled(useSDE);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Set parameters as we did for the DREAM4 benchmarks */
	public void setDream4Settings() {
			
		// Model
		model_.setSelectedIndex(2);
		
		// Experiments
		knockoutSS_.setSelected(true);
		knockdownSS_.setSelected(true);
		multifactorialSS_.setSelected(true);
		dualKnockoutSS_.setSelected(true);
		
		knockoutTS_.setSelected(false);
		knockdownTS_.setSelected(false);
		multifactorialTS_.setSelected(false);
		dualKnockoutTS_.setSelected(false);
		
		timeSeriesAsDream4_.setSelected(true);
		
		// Set "number of time series" spinner
		//numTimeSeries_.getModel().setValue(99);
		numTimeSeries_.setValue(10);
		
		// "duration" spinner
		tmax_.setValue(1000);
		
		// Set model of "number of points per time serie" spinner
		numPointsPerTimeSeries_.setValue(21);
		
		perturbationNew_.setSelected(true);
		perturbationLoad_.setSelected(false);
		
		// Noise
		
		// Diffusion multiplier (SDE only)
		sdeDiffusionCoeff_.setValue(0.05);
		
		noNoise_.setSelected(false);
		useMicroarrayNoise_.setSelected(true);
		useLogNormalNoise_.setSelected(false);
		addGaussianNoise_.setSelected(false);
		addLogNormalNoise_.setSelected(false);
		
		normalizeNoise_.setSelected(true);
		
		// Enables/disables stuff in the GUI (num time points etc., which may be disabled/gray otherwise)
		setExperimentAction();
		setNoiseAction();
		setModelAction();
	}
	
	
	// ----------------------------------------------------------------------------
	
	public void setExperimentAction() {		
		boolean useTimeSeries = (knockoutTS_.isSelected() || knockdownTS_.isSelected() || multifactorialTS_.isSelected() || dualKnockoutTS_.isSelected() || timeSeriesAsDream4_.isSelected());
		numTimeSeriesLabel_.setEnabled(timeSeriesAsDream4_.isSelected());
		numTimeSeries_.setEnabled(timeSeriesAsDream4_.isSelected());
		durationOfSeriesLabel_.setEnabled(useTimeSeries);
		numPointsPerSeriesLabel_.setEnabled(useTimeSeries);
		tmax_.setEnabled(useTimeSeries);
		numPointsPerTimeSeries_.setEnabled(useTimeSeries);
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public void setNoiseAction() {
		addGaussianNoise_.setEnabled(useLogNormalNoise_.isSelected());
		addLogNormalNoise_.setEnabled(useLogNormalNoise_.isSelected());
		gaussianNoise_.setEnabled(useLogNormalNoise_.isSelected() && addGaussianNoise_.isSelected());
		logNormalNoise_.setEnabled(useLogNormalNoise_.isSelected() && addLogNormalNoise_.isSelected());
		normalizeNoise_.setEnabled(!noNoise_.isSelected());
	}
	
	
	// ----------------------------------------------------------------------------
		
	
	public void escapeAction() {
		
		super.escapeAction();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Run the simulation process and benchmark generation.
	 * Save the simulation parameters defined by the user in the settings of GNW.
	 * The the simulation thread reads these values in the settings of GNW.
	 */
	public void enterAction() {
		
		try {
			GeneNetwork grn = ((DynamicalModelElement) item_).getGeneNetwork();
			SimulationThread simulation = null;
			
			GnwSettings settings = GnwSettings.getInstance();
			
			// Save the required settings
			// Model
			settings.setSimulateODE(model_.getSelectedIndex() == 0 || model_.getSelectedIndex() == 2);
			settings.setSimulateSDE(model_.getSelectedIndex() == 1 || model_.getSelectedIndex() == 2);
			
			// Experiments
			settings.generateSsKnockouts(knockoutSS_.isSelected());
			settings.generateSsKnockdowns(knockdownSS_.isSelected());
			settings.generateSsMultifactorial(multifactorialSS_.isSelected());
			settings.generateSsDualKnockouts(dualKnockoutSS_.isSelected());
			
			settings.generateTsKnockouts(knockoutTS_.isSelected());
			settings.generateTsKnockdowns(knockdownTS_.isSelected());
			settings.generateTsMultifactorial(multifactorialTS_.isSelected());
			settings.generateTsDualKnockouts(dualKnockoutTS_.isSelected());
			
			settings.generateTsDREAM4TimeSeries(timeSeriesAsDream4_.isSelected());
			
			if (timeSeriesAsDream4_.isSelected()) { // is saved only if "Time series as in DREAM4 is selected"
				settings.setNumTimeSeries((Integer) numTimeSeries_.getModel().getValue());
			}
			
			double maxt = (Integer) tmax_.getModel().getValue();
			double dt = maxt / ((Integer) numPointsPerTimeSeries_.getModel().getValue() - 1);
			if (durationOfSeriesLabel_.isEnabled())
				settings.setMaxtTimeSeries(maxt);
			if (numPointsPerSeriesLabel_.isEnabled())
				settings.setDt(dt);
				//settings.setNumMeasuredPoints((Integer) numPointsPerTimeSerie_.getModel().getValue());
			
			settings.setLoadPerturbations(perturbationLoad_.isSelected());
			
			if (settings.getSimulateSDE())
				settings.setNoiseCoefficientSDE((Double) sdeDiffusionCoeff_.getModel().getValue());
			
			settings.setAddMicroarrayNoise(useMicroarrayNoise_.isSelected());
			settings.setAddNormalNoise(useLogNormalNoise_.isSelected() && addGaussianNoise_.isSelected());
			settings.setAddLognormalNoise(useLogNormalNoise_.isSelected() && addLogNormalNoise_.isSelected());
			
			if (settings.getAddNormalNoise())
				settings.setNormalStdev((Double) gaussianNoise_.getModel().getValue());
			
			if (settings.getAddLognormalNoise())
				settings.setNormalStdev((Double) logNormalNoise_.getModel().getValue());
			
			settings.setNormalizeAfterAddingNoise(normalizeNoise_.isSelected());
			
			simulation = new SimulationThread(grn);
			
			
			// Perhaps make a test on the path validity
			settings.setOutputDirectory(userPath_.getText());
			settings.stopBenchmarkGeneration(false); // reset
			simulation.start();
		}
		catch (Exception e) {
			log.warning(e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** Add tooltips for all elements of the window */
	private void addTooltips() {
				
		dream4Settings_.setToolTipText(
				"<html>Set all parameters of this window to the values<br>" +
				"that were used to generate the DREAM4 challenges</html>");
		normalizeNoise_.setToolTipText(
				"<html>After adding experimental noise (measurement error, normalize<br>" +
				"by dividing all concentrations values by the maximum mRNA<br>" +
				"concentration of all datasets</html>");
		noNoise_.setToolTipText(
				"<html>Do not add any experimental noise (measurement error) after the simulation<br>" +
				"(if SDEs are used, there will still be noise in the dynamics)</html>");
		wtSS_.setToolTipText(
				"<html>Generate the steady state of the wild-type<br>" +
				"(can't be disabled)</html>");
		useLogNormalNoise_.setToolTipText(
				"<html>Select checkboxes below to add normal (Gaussian)<br>" +
				"and/or log-normal noise after the simulation</html>");
		addGaussianNoise_.setToolTipText(
				"<html>Select to add normal (Gaussian) noise</html>");
		addLogNormalNoise_.setToolTipText(
				"<html>Select to add log-normal noise</html>");
		useMicroarrayNoise_.setToolTipText(
				"<html>Select to use the model of noise in microarrays that was used for the DREAM4<br>" +
				"challenges, which is similar to a mix of normal and log-normal noise<br>" +
				"(Tu, Stolovitzky, and Klein. <i>PNAS</i>, 99:14031-14036, 2002)</html>");
		
		String networkName = "<i>" + item_.getLabel() + "</i>";
		perturbationLoad_.setToolTipText(
				"<html>Load the perturbations from the following files<br>" +
				"(they must be located in the output directory):<br>" +
				"- " + networkName + "_multifactorial_perturbations.tsv<br>" +
				"- " + networkName + "_dualknockouts_perturbations.tsv<br>" +
				"- " + networkName + "_dream4_timeseries_perturbations.tsv</html>");
		perturbationNew_.setToolTipText(
				"<html>Generate new perturbations, select if you don't have<br>" +
				"predefined perturbations that you want to use</html>");
		
		timeSeriesAsDream4_.setToolTipText(
				"<html>Generate time series as those provided in DREAM4 (<i>in addition</i><br>" +
				"to time series for knockouts, knockdowns, etc. selected above)</html>");
		dualKnockoutTS_.setToolTipText(
				"<html>Trajectories for dual knockouts (at t=0 is the<br>" +
				"wild-type, at this time the dual knockout is done)</html>");
		multifactorialTS_.setToolTipText(
				"<html>Trajectories for multifactorial perturbations (at t=0 is<br>" +
				"the wild-type, at this time the perturbation is applied)</html>");
		knockdownTS_.setToolTipText(
				"<html>Trajectories for knockdowns (at t=0 is the<br>" +
				"wild-type, at this time the knockdown is done)</html>");
		knockoutTS_.setToolTipText(
				"<html>Trajectories for the knockouts (at t=0 is the<br>" +
				" wild-type, at this time the knockout is done)</html>");
		dualKnockoutSS_.setToolTipText(
				"<html>Steady states for dual knockouts (pairs are selected<br>" +
				"according to how many genes they co-regulate)</html>");
		multifactorialSS_.setToolTipText(
				"<html>Steady states for multifactorial perturbations</html>");
		logNormalNoise_.setToolTipText(
				"<html>Standard deviation of the log-normal noise</html>");
		numTimeSeries_.setToolTipText(
				"<html>The number of time series (a different perturbation<br>" +
				"is used for every time series)</html>");
		knockdownSS_.setToolTipText(
				"<html>Steady states for knockdown of every gene</html>");
		knockoutSS_.setToolTipText(
				"<html>Steady states for knockout of every gene</html>");
		model_.setToolTipText(
				"<html>Select ODEs (deterministic) or SDEs (noise in dynamics) for the<br>" +
				"simulation of all experiments selected below. If you select both,<br>" +
				"they will be run one after the other using the same perturbations<br>" +
				"and the label <i>nonoise</i> will be added to the data from the ODEs.</html>");
		tmax_.setToolTipText(
				"<html>Duration of the time series experiments</html>");
		sdeDiffusionCoeff_.setToolTipText(
				"<html>Multiplicative constant of the noise term in the SDEs<br>" +
				"(if set to 0, using SDEs is equivalent to using ODEs)</html>");
		gaussianNoise_.setToolTipText(
				"<html>Standard deviation of the Gaussian noise</html>");
		numPointsPerTimeSeries_.setToolTipText(
				"<html>Number of points per time series (defines how many points are saved<br>" +
				"in the datasets, does not affect precision of numerical integration)</html>");
		runButton_.setToolTipText(
				"<html>Set parameters to the given values and run all experiments</html>");
		cancelButton_.setToolTipText(
				"<html>Abort (the thread may finish the current experiment before it exits)</html>");
		
		// tooltips disappear only after 10s
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		
	}
	
	
	/** Thread to generate the benchmarks.
	 * 
	 * @author Thomas Schaffter (firstname.lastname@gmail.com)
	 *
	 */
	private class SimulationThread implements Runnable {
		
		/** Serialization */
		private static final long serialVersionUID = 1L;
		
		/** Implemented types of benchmark */
		//public static final int DREAM3 = 1;
		//public static final int DREAM4 = 2;
		
		/** Main Thread */
		private Thread myThread_;
		/** handles the experiments */
		private BenchmarkGenerator benchmarkGenerator_ = null;

		
		// ============================================================================
		// PUBLIC METHODS
		
		/**
		 * Constructor
		 */
		public SimulationThread(GeneNetwork grn) {
			
			super();
			myThread_ = null;
			benchmarkGenerator_ = new BenchmarkGenerator(grn);
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Start function
		 */
		public void start() {
			// If myThread_ is null, we start it!
			if (myThread_ == null) {
				myThread_ = new Thread(this);
				myThread_.start();
			}
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Stop function
		 */
		public void stop() {
			myThread_ = null;
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Run function
		 */
		public void run() {
			snake_.start();
			myCardLayout_.show(runButtonAndSnakePanel_, snake_.getName());

			try {
				benchmarkGenerator_.generateGoldStandard(); // all the options have been saved in GnwSettings
				done();
			} catch (CancelException e) {
				log.info(e.getMessage());
			} catch (Exception e) {
				log.warning(e.getMessage());
			}
	    }
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Function called after run().
		 */
		public void done() {
			snake_.stop();
			myCardLayout_.show(runButtonAndSnakePanel_, runButton_.getName());
			log.log(Level.INFO, "Done!");
			escapeAction();
		}
	}
}
