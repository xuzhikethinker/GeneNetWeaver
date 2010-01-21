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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;


/** Time course experiments, see documentation for details.
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 *
 */
public class TimeSeriesExperiment extends Experiment {

	/** Time series data */
	private ArrayList<DoubleMatrix2D> timeSeries_;
	/** Protein data */
	private ArrayList<DoubleMatrix2D> timeSeriesProteins_;
	/** The duration of the experiment */
	private double maxt_;
	/** Number of time points (maxt/dt + 1)*/
	private int numTimePoints_;
	/** Set true to remove the perturbation after maxt/2 */
	private boolean restoreWildTypeAtHalftime_;
	
    /** Logger for this class */
	private Logger log = Logger.getLogger(TimeSeriesExperiment.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor
	 */
	public TimeSeriesExperiment(Solver.type solverType, Perturbation perturbation, boolean restoreWildTypeAtHalftime, String label) {
		
		super(solverType, perturbation, label);
		timeSeries_ = null;
		timeSeriesProteins_ = null;
		xy0_ = null;
		restoreWildTypeAtHalftime_ = restoreWildTypeAtHalftime;
		setMaxtAndNumTimePoints();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Run all experiments
	 */
	public void run(DoubleMatrix1D xy0) {
		
		xy0_ = xy0;
		
		String simulationType = "ODEs";
		if (solverType_ == Solver.type.SDE)
			simulationType = "SDEs";
		log.log(Level.INFO, "Simulating time-series " + label_ + " using " + simulationType + " ...");

		boolean simulateLoadedExperiments = (timeSeries_ != null);
		if (simulateLoadedExperiments)
			throw new RuntimeException("NEEDS TO BE FIXED, NOT FUNCTIONAL");
		
		if (!simulateLoadedExperiments) {
			timeSeries_ = new ArrayList<DoubleMatrix2D>();
			if (modelTranslation_)
				timeSeriesProteins_ = new ArrayList<DoubleMatrix2D>();
		}
		
		// create and run the time series experiments
		for (int i=0; i<numExperiments_; i++) {
			log.log(Level.INFO, "Simulating time-series number " + (i+1) + " ...");
			integrate(i);
		}
		log.log(Level.INFO, "");
		
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the initial conditions xy0 (the vector is copied)
	 */
	public void setInitialConditions(DoubleMatrix1D xy0) {
		
		int length = numGenes_;
		if (modelTranslation_)
			length *= 2;
		
		if (xy0.size() != length)
			throw new IllegalArgumentException("TimeSeriesExperiment:setInitialConditions(): " +
					"xy0.length = " + xy0.size() + " doesn't match the number of state variables " + length);
		
		xy0_ = new DenseDoubleMatrix1D(length);
		xy0_.assign(xy0);
	}

		
	// ----------------------------------------------------------------------------
	
	/**
	 * Run the numerical integration of the k'th time-series and add the results to timeSeries_ and timeSeriesProteins_.
	 * The wild-type is restored after the experiments.
	 */
	public void integrate(int k) {

		if (GnwSettings.getInstance().getDt()*(numTimePoints_-1) != maxt_)
			throw new RuntimeException("dt * (numTimePoints-1) != maxt");
		
		// allocate space
		DoubleMatrix2D ts = new DenseDoubleMatrix2D(numTimePoints_, numGenes_);
		DoubleMatrix2D tsProteins = null;
		if (modelTranslation_)
			tsProteins = new DenseDoubleMatrix2D(numTimePoints_, numGenes_);

		if (xy0_ == null)
			throw new NullPointerException("TimeSeriesExperiment:integrate(): No initial condition set!");

		Solver solver = new Solver(solverType_, grn_, xy0_.toArray());
		double t = 0;
		
		// for SDEs, simulate the wild-type for a short time to get a new independent sample
		if (solverType_ == Solver.type.SDE) {
			double tlim = maxt_/10.0;
			do {
				try {
					t += solver.step();
				} catch (Exception e) {
					log.log(Level.INFO, "TimeSeriesExperiment.integrate(): Exception in phase 0, t = " + t + ":" + e.getMessage());
					throw new RuntimeException();
				}
			} while (t < tlim);

			// set this sample as the new initial condition
			xy0_.assign(solver.getState()); 
		}
		
		// Set first line of the time series dataset (at t=0)
		for (int i=0; i<numGenes_; i++)
			ts.set(0, i, xy0_.get(i));
		if (modelTranslation_)
			for (int i=0; i<numGenes_; i++)
				tsProteins.set(0, i, xy0_.get(numGenes_+i));
		
		// apply perturbation
		perturbation_.applyPerturbation(k);
		t = 0; // reset time, the time-series only really starts here
		double dt = GnwSettings.getInstance().getDt();
		double tlim = maxt_/2.0 - 1e-12;
		boolean wildTypeRestored = false;
		int pt = 1;
		
		do {
			double t1 = t;
			try {
				// For ODEs: this steps the time by dt_, but using an adaptive internal step size
				// to guarantee the specified tolerance (getRate() may be called several times for one step)
				// For SDEs: this steps the time by dt_, the solver integrates with a smaller, fixed step size
				// defined in SDESettings by dt_*multiplier_ (SDESettings.dt_ != TimeSeriesExperiment.dt_)
				t += solver.step();
			} catch (Exception e) {
				log.log(Level.INFO, "TimeSeriesExperiment.integrate(): Exception at t = " + t + ":" + e.getMessage());
				throw new RuntimeException();
			}
			
			if (t != t1 + dt)
				throw new RuntimeException("Solver failed to step time by dt, expected t = " + (t1+dt) + ", obtained t = " + t);
			
			if (restoreWildTypeAtHalftime_ && t >= tlim && !wildTypeRestored) {
				perturbation_.restoreWildType();
				wildTypeRestored = true;
			}
			
			// Save the state of the result
			double[] xy = solver.getState();
			for (int g=0; g<numGenes_; g++)
				ts.set(pt, g, xy[g]);

			if (modelTranslation_)
				for (int g=0; g<numGenes_; g++)
					tsProteins.set(pt, g, xy[numGenes_+g]);
			
			pt++;
		} while (t < maxt_);

		assert t == maxt_ : "t=" + t + " maxt=" + maxt_;
		assert pt == numTimePoints_;
		
		// make sure the wild-type is restored
		if (!wildTypeRestored)
			perturbation_.restoreWildType();
		
		// add the new time-series data to the array lists
		timeSeries_.add(ts);
		if (modelTranslation_)
			timeSeriesProteins_.add(tsProteins);
		
		if (solverType_ == Solver.type.SDE && solver.getSDESolver().getXNegativeCounter() > 0)
			log.log(Level.INFO, "SDE: " + solver.getSDESolver().getXNegativeCounter() + " times a concentration became negative due to noise and was set to 0");

	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Add experimental noise to the data
	 */
	public void addNoise() {

		for (int i=0; i<timeSeries_.size(); i++)
			addNoise(timeSeries_.get(i));
		
		if (modelTranslation_)
			for (int i=0; i<timeSeriesProteins_.size(); i++)
				addNoise(timeSeriesProteins_.get(i));
		
		noiseHasBeenAdded_ = true;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Get the maximum concentration in the time series.
	 * For now, only get the max concentration between mRNA levels. Later, perhaps
	 * leave the choice to the user if he prefer, e.g., to normalise by the mRNA
	 * OR protein levels.
	 */
	public double getMaximumConcentration() {

		double max = 0;
		
		for (int i=0; i<timeSeries_.size(); i++) {
			double max_i = getMaximumConcentration(timeSeries_.get(i));
			if (max_i > max)
				max = max_i;
		}
		return max;
	}

	
	// ----------------------------------------------------------------------------

	/** Normalize (i.e. divide by) the given maximum value */
	public void normalize(double max) {
		
		for (int i=0; i<timeSeries_.size(); i++)
			normalize(timeSeries_.get(i), max);
			
		if (modelTranslation_)
			for (int i=0; i<timeSeriesProteins_.size(); i++)
				normalize(timeSeriesProteins_.get(i), max);
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Print all the trajectories to a single file, the initial conditions are printed
	 * to a separate file. Protein trajectories are only printed if translation is
	 * modelled. Append the given string to the filenames (e.g. "-nonoise"). 
	 */
	public void printAll(String postfix) {
		
		if (timeSeries_.size() < 1)
			return;
		
		printTrajectories(postfix + "_" + label_, timeSeries_);    // print mRNA time courses
		if (modelTranslation_)
			printTrajectories(postfix + "_proteins_" + label_, timeSeriesProteins_); // print protein time courses
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Print all the trajectories to a single file, the initial conditions are printed
	 * to a separate file. If the argument is set true, the protein instead of the
	 * mRNA concentrations are printed. append the given string to the filenames (e.g. "-nonoise"). 
	 */
	private void printTrajectories(String postfix, ArrayList<DoubleMatrix2D> timeSeries) {
				
		try { 
			// Filename
			String filename = GnwSettings.getInstance().getOutputDirectory() + grn_.getId() + postfix + ".tsv";
			FileWriter fw = new FileWriter(filename, false);
			
			// Header
			fw.write("\"Time\"\t");
			fw.write(grn_.getHeader(false));

			// For every time series...
			for (int i=0; i<timeSeries.size(); i++) {

				// The data
				DoubleMatrix2D data = timeSeries.get(i);
				double dt = GnwSettings.getInstance().getDt();

				fw.write("\n");
				for (int tp=0; tp<numTimePoints_; tp++) {
					fw.write(Double.toString(tp*dt));

					for (int g=0; g<numGenes_; g++)
						fw.write("\t" + String.format("%.7f", data.get(tp, g)));//Double.toString(data.get(tp, g)));
					fw.write("\n");
				}
			}

			fw.close();
			log.log(Level.INFO, "Writing file " + filename);

		} catch (IOException fe) {
			log.log(Level.INFO, "TimeSeriesExperiment:printDataset(): " + fe.getMessage());
			throw new RuntimeException();
		}
	}

	
	// ============================================================================
	// PRIVATE FUNCTIONS
	
	/** Set maxt_ and numTimePoints_ according to GnwSettings (checks that they are consistent with GnwSettings.dt_) */
	private void setMaxtAndNumTimePoints() {
		
		double dt = GnwSettings.getInstance().getDt();
		maxt_ = GnwSettings.getInstance().getMaxtTimeSeries();
		numTimePoints_ = (int)Math.round(maxt_/dt) + 1;

		if (dt*(numTimePoints_-1) != maxt_)
			throw new RuntimeException("maxt must be a multiple of dt");
	}
	
	
	// ----------------------------------------------------------------------------

	/** Add experimental noise to the given data */
	private void addNoise(DoubleMatrix2D ts) {
		
		for (int i=0; i<numTimePoints_; i++)
			for (int j=0; j<numGenes_; j++)
				ts.set(i, j, addNoise(ts.get(i,j)));
	}
	
	
	// ----------------------------------------------------------------------------

	/** Get the maximum concentration in the given time series. */
	public double getMaximumConcentration(DoubleMatrix2D ts) {

		double max = 0;
		
		for (int i=0; i<numTimePoints_; i++)
			for (int j=0; j<numGenes_; j++)
				if (ts.get(i,j) > max)
					max = ts.get(i,j);
		
		return max;
	}
	
	
	// ----------------------------------------------------------------------------

	/** Normalize (i.e. divide by) the given maximum value */
	public void normalize(DoubleMatrix2D ts, double max) {
		
		for (int i=0; i<numTimePoints_; i++)
			for (int j=0; j<numGenes_; j++)
				ts.set(i, j, ts.get(i,j)/max);
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS

	public int getNumTimePoints() { return numTimePoints_; }
	public ArrayList<DoubleMatrix2D> getTimeSeries() { return timeSeries_; }
	public ArrayList<DoubleMatrix2D> getTimeSeriesProteins() { return timeSeriesProteins_; }
	public boolean getRestoreWildTypeAtHalftime() { return restoreWildTypeAtHalftime_; }
	//public DoubleMatrix1D getXy0() { return xy0_; }
	
	
	// ============================================================================
	// DEPRECATED
	
	/**
	 * Set the initial conditions from an array of strings (the concentrations)
	 */
	@Deprecated
	public void setInitialConditions(String[] xy0) {
		
		int length = numGenes_;
		if (modelTranslation_)
			length *= 2;
		
		if (xy0.length != length)
			throw new IllegalArgumentException("TimeSeriesExperiment:setInitialConditions(): " +
					"xy0.length = " + xy0.length + " doesn't match the number of state variables " + length);
		
		xy0_ = new DenseDoubleMatrix1D(length);
		for (int i=0; i<length; i++)
			xy0_.set(i, Double.parseDouble(xy0[i]));
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Deprecated, this is how it was done in DREAM3.
	 * Set random initial conditions based on a standard condition (the wild-type).
	 * The initial condition for gene i is x_i(0) = wildType_i + epsilon, where
	 * epsilon is a random number from a Gaussian distribution with mean zero and
	 * standard deviation as specified in Universal::perturbationStdev_.  
	 */
	/*
	@Deprecated
	public void setRandomInitialConditions(DoubleMatrix1D wildType) {
		
		if (wildType.size() != numGenes_)
			throw new IllegalArgumentException("TimeSeriesExperiment::setRandomInitialConditions(): " +
					"the size of wildType does not correspond with the number of genes!");
		
		// The distribution for the perturbation: limited in [0 1], the mean will be the corresponding wild-type
		double stdev = GnwSettings.getInstance().getPerturbationStdev();
		RandomParameterGaussian randomInitialCondition = new RandomParameterGaussian(0, 1, 0, stdev, false);
		
		// Allocate space
		int length = numGenes_;
		if (modelTranslation_)
			length *= 2;
		xy0_ =  new DenseDoubleMatrix1D(length);
		
		// Set random initial conditions for the mRNA (x)
		for (int i=0; i<numGenes_; i++) {
			randomInitialCondition.setMean(wildType.get(i));
			xy0_.set(i, randomInitialCondition.getRandomValue());
		}
		
		// Corresponding initial conditions for the proteins (as if the above initial condition would be held fixed)
		// 0 = mTranslation*x_i - deltaProt*y_i  =>  y_i = mTranslation*x_i / deltaProt
		if (modelTranslation_) {
			for (int i=0; i<numGenes_; i++) {
				double m = grn_.getGene(i).getMaxTranslation();
				double d = grn_.getGene(i).getDeltaProtein();
				xy0_.set(numGenes_+i, m*xy0_.get(i) / d);
			}
		}
	}	
	*/
	
}
