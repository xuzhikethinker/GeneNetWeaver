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

import java.awt.Frame;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.optimization.CostException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.windows.ErrorWindow;
import ch.epfl.lis.gnwgui.windows.GenericWindow;


/** 
 * Implements experiments where the stead-state of the network is measured.
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class SteadyStateExperiment extends Experiment {
	
	/** Perturbed steady-states */
	private DoubleMatrix2D ssPerturbation_;
	/** Perturbed steady-states for the proteins */
	private DoubleMatrix2D ssPerturbationProteins_;
	
	/** Time of the current steady-state computation */
	private double t_;
	/**
	 * For SteadyStateExperimentODE: return the steady-states as soon as convergence is reached.
	 * If there is no convergence until time maxt_, the values at this point are returned and a
	 * warning message is displayed.
	 */
	private double maxtODE_;
	/**
	 * For stochastic simulations (SDEs), If maxtSDE < 0, we return the state at time 1.5*timeToConvergenceODE_,
	 * where timeToConvergenceODE_ should be set to the time of convergence for the deterministic simulation 
	 * of the same experiment. If maxtSteadyStateSDE > 0, we return the state at that time.
	 */
	private double maxtSDE_;
	/** 
	 * For ODEs: save the time until convergence for each perturbation
	 * For SDEs: return the state at these times.
	 */
	private ArrayList<Double> timeToConvergenceODE_;
	
    /** Logger for this class */
    private static Logger log = Logger.getLogger(SteadyStateExperiment.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public SteadyStateExperiment(Solver.type solverType, Perturbation perturbation, String label) {
		
		super(solverType, perturbation, label);
		
		label_ = label; 
		ssPerturbation_ = null;
		ssPerturbationProteins_ = null;
		t_ = 0;
		maxtODE_ = GnwSettings.getInstance().getMaxtSteadyStateODE();
		maxtSDE_ = GnwSettings.getInstance().getMaxtSteadyStateSDE();
		timeToConvergenceODE_ = null;
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Run all steady-state experiments
	 */
	public void run(DoubleMatrix1D xy0) {
		
		xy0_ = xy0;
		
		try {
			String simulationType = "ODEs";
			if (solverType_ == Solver.type.SDE)
				simulationType = "SDEs";
			log.log(Level.INFO, "Simulating steady-state " + label_ + " using " + simulationType + " ...");
			
			if (solverType_ == Solver.type.SDE && timeToConvergenceODE_ == null && maxtSDE_ < 0)
				throw new RuntimeException("For SDE steady-state simulation, either specify timeToConvergenceODE_ or maxtSDE_");
				
			ssPerturbation_ = new DenseDoubleMatrix2D(numExperiments_, numGenes_);
			if (modelTranslation_)
				ssPerturbationProteins_ = new DenseDoubleMatrix2D(numExperiments_, numGenes_);
				
			if (solverType_ == Solver.type.ODE)
				timeToConvergenceODE_ = new ArrayList<Double>();
			
			computeSteadyStates();
			
			// display the longest time to convergence
			if (solverType_ == Solver.type.ODE) {
				double max = -1;
				for (int i=0; i<timeToConvergenceODE_.size(); i++)
					if (timeToConvergenceODE_.get(i) > max)
						max = timeToConvergenceODE_.get(i);
				log.log(Level.INFO, "Duration of the longest steady state experiment = " + max);
			}
			log.log(Level.INFO, ""); // empty line

		} catch (ConvergenceException e) {
			log.log(Level.INFO, "SteadyStateExperiment::runAll(): ConvergenceException " + e.getMessage());
			throw new RuntimeException();
		} catch (CostException e) {
			log.log(Level.INFO, "SteadyStateExperiment::runAll(): CostException " + e.getMessage());
			throw new RuntimeException();
		} catch (Exception e) {
			log.log(Level.INFO, "SteadyStateExperiment::runAll(): Exception " + e.getMessage());
			throw new RuntimeException();
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Print the mRNA data, append the given string to the filenames (e.g. "_nonoise_wildtype").
	 * If translation is modelled, protein data is also printed
	 */
	public void printAll(String postfix) {
		printMRNA(postfix);
		if (GnwSettings.getInstance().getModelTranslation())
			printProteins(postfix);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Print the mRNA data, append the given string to the filenames (e.g. "-nonoise"). 
	 */
	public void printMRNA(String postfix) {
		
		GnwSettings uni = GnwSettings.getInstance();
		String absPath = uni.getOutputDirectory();
		
		printSteadyStates(absPath + grn_.getId() + postfix + "_" + label_ + ".tsv", ssPerturbation_);
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Print the protein data, append the given string to the filenames (e.g. "-nonoise"). 
	 */
	public void printProteins(String postfix) {
		
		GnwSettings set = GnwSettings.getInstance();
		String absPath = set.getOutputDirectory();
		
		if (!modelTranslation_)
			throw new IllegalArgumentException("SteadyStateExperiment:printProteins(): protein translation was not modeled");

		printSteadyStates(absPath + grn_.getId() + postfix + "_proteins_" + label_ + ".tsv", ssPerturbationProteins_);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Compute the steady-states for all the single-gene perturbations, one after the other.
	 * The result is stored in ssPerturbation.  
	 * @throws Exception 
	 */ 
	public void computeSteadyStates() throws Exception {
		
		// apply each perturbation, one after the other, and compute the steady-states
		for (int i=0; i<numExperiments_; i++) {
			
			// the time limit for the simulation
			double maxt;
			if (solverType_ == Solver.type.SDE) {
				if (maxtSDE_ > 0)
					maxt = maxtSDE_;
				else
					maxt = timeToConvergenceODE_.get(i); 
			} else
				maxt = maxtODE_;
			
			// apply the perturbation
			if (perturbation_ != null)
				perturbation_.applyPerturbation(i);
			
			// compute the steady-state
			computeSteadyState(maxt);
			
			// remove the perturbation
			if (perturbation_ != null)
				perturbation_.restoreWildType();
			
			// put the steady-state into the corresponding line in ssPerturbation_
			DoubleMatrix1D x = grn_.getX();
			for (int j=0; j<numGenes_; j++)
				ssPerturbation_.set(i, j, x.get(j));
			
			if (modelTranslation_) {
				DoubleMatrix1D y = grn_.getY();
				for (int j=0; j<numGenes_; j++)
					ssPerturbationProteins_.set(i, j, y.get(j));
			}
		}
		// remove the perturbation from the network
		if (perturbation_ != null)
			perturbation_.restoreWildType();
	}
	
	
	// ============================================================================
	// PRIVATE METHODS
	
	/**
	 * Compute the steady state of the network after integrating from the given
	 * initial conditions x0 and y0.
	 * @throws Exception 
	 */
	private void computeSteadyState(double maxt) throws Exception {
						
		double[] xy0 = constructInitialCondition(); // initial condition
		t_ = 0;
		double dt = GnwSettings.getInstance().getDt();
		
		Solver solver = new Solver(solverType_, grn_, xy0);

		try{
			do {
				double t1 = t_;
				// this steps the time by dt_, but using a smaller internal step size of the solver
				// (getRate() may be called several times for one step)
				t_ += solver.step();

				if (t_ != t1 + dt)
					throw new RuntimeException("Solver failed to step time by dt, expected t = " + (t1+dt) + ", obtained t = " + t_);

			} while (!solver.converged() && t_ < maxt);
		}catch(RuntimeException e){
			ErrorWindow dialog = new ErrorWindow(new Frame(), false, "Duration (t_max) must be a multiple of [measured points - 1].");
			dialog.setVisible(true);
		}
		
		// note, the state at the last step is already saved both in ODE.state and grn.x_, grn.y_
		
		// save the time of this experiment
		if (solverType_ == Solver.type.ODE)
			timeToConvergenceODE_.add(t_);
		
		// Check the max rate of change at the found solution
		DoubleMatrix1D lastX = grn_.getX();
		DoubleMatrix1D lastY = grn_.getY();
		double[] dxydt;
		double[] xy;
		if (modelTranslation_) {
			dxydt = new double[2*numGenes_];
			xy = concatenateVectors(lastX, lastY);
		} else {
			dxydt = new double[numGenes_];
			xy = lastX.toArray();
		}
			
		grn_.computeDxydt(xy, dxydt);
		
		double max = 0;
		for (int i=0; i<dxydt.length; i++)
			if (dxydt[i] > max)
				max = dxydt[i];

		log.log(Level.INFO, "Saved state at t = " + t_ + ", with maximum dx_i/dt = " + max);
		
		if (solverType_ == Solver.type.SDE && solver.getSDESolver().getXNegativeCounter() > 0)
			log.log(Level.INFO, "SDE: " + solver.getSDESolver().getXNegativeCounter() + " times a concentration became negative due to noise and was set to 0");
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Construct xy0, the initial conditions as an array of double. If x0 is null,
	 * we estimate the initial conditions as the concentration of the genes in the
	 * absence of regulation.
	 */
	private double[] constructInitialCondition() {
		
		double[] xy0 = null;
		
		if (xy0_ == null) {
			// construct a vector of zeros
			double[] zeros;
			if (modelTranslation_) {
				xy0 = new double[2*numGenes_];
				zeros = new double[2*numGenes_];
			} else {
				xy0 = new double[numGenes_];
				zeros = new double[numGenes_];
			}
			for (int i=0; i<zeros.length; i++)
				zeros[i] = 0;

			// Estimate the initial conditions as the concentration of the genes without regulation.
			// 0 = m*f(0) -delta*x_i  =>  x_i = m*f(0) / delta
			grn_.computeDxydt(zeros, xy0); // since x=0, this actually computes m*f(0)
			
			for (int i=0; i<numGenes_; i++) {
				xy0[i] /= grn_.getGene(i).getDelta();
			}

			// Corresponding initial conditions for the proteins:
			// 0 = mTranslation*x_i - deltaProt*y_i  =>  y_i = mTranslation*x_i / deltaProt
			if (modelTranslation_) {
				for (int i=0; i<numGenes_; i++) {
					double m = grn_.getGene(i).getMaxTranslation();
					double d = grn_.getGene(i).getDeltaProtein();
					xy0[numGenes_+i] = m*xy0[i] / d;
				}
			}
			//xy0_ = new DenseDoubleMatrix1D(xy0.length);
			//xy0_.assign(xy0);
		
		} else {
			xy0 = xy0_.toArray();
		}
		return xy0;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Print the given steady-state vector (data). This function is usually used to print the wild-type.
	 */
	public void printSteadyStates(String filename, DoubleMatrix1D wt) {
		
		// copy wt to a 2D matrix
		DoubleMatrix2D data = new DenseDoubleMatrix2D(1, wt.size());
		for (int i=0; i<wt.size(); i++)
			data.set(0, i, wt.get(i));
		
		printSteadyStates(filename, data);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Print the perturbation experiments (data).
	 */
	public void printSteadyStates(String filename, DoubleMatrix2D data) {
		
		try {
			log.log(Level.INFO, "Writing file " + filename);
			FileWriter fw = new FileWriter(filename, false);

			fw.write(grn_.getHeader(false));
			
			// Data
			if (data != null) {
				for (int i=0; i<data.rows(); i++) {
					//fw.write("\"" + grn_.getNode(i).getLabel() + type + "\"");

					for (int j=0; j<data.columns()-1; j++)
						fw.write(String.format("%.7f", data.get(i, j)) + "\t");//Double.toString(value));
					fw.write(String.format("%.7f", data.get(i, data.columns()-1)) + "\n");
				}
			}

			// Close file
			fw.close();

		} catch (IOException fe) {
			log.log(Level.INFO, "SteadyStateExperiment::printData(): " + fe.getMessage());
			throw new RuntimeException();
		}
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Add log-normal noise to the data 
	 */
	public void addNoise() {
		
		GnwSettings settings = GnwSettings.getInstance();
		
		// mRNA
		for (int i=0; i<numExperiments_; i++)
			for (int j=0; j<numGenes_; j++)
				ssPerturbation_.set(i, j, addNoise(ssPerturbation_.get(i,j)));
		
		// proteins
		if (settings.getModelTranslation()) {
			for (int i=0; i<numExperiments_; i++)
				for (int j=0; j<numGenes_; j++)
					ssPerturbationProteins_.set(i, j, addNoise(ssPerturbationProteins_.get(i,j)));
		}
		
		noiseHasBeenAdded_ = true;
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Get the maximum concentration of all experiments.
	 * For now, only get the max concentration between mRNA levels. Later, perhaps
	 * leave the choice to the user if he prefer, e.g., to normalise by the mRNA
	 * OR protein levels.
	 */
	public double getMaximumConcentration() {

		double max = 0;
		for (int i=0; i<numExperiments_; i++) {
			for (int j=0; j<numGenes_; j++) {
				if (ssPerturbation_.get(i,j) > max)
					max = ssPerturbation_.get(i,j);
			}
		}
		
		return max;
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Normalize (i.e. divide by) the given maximum value 
	 */
	public void normalize(double max) {
		
		GnwSettings settings = GnwSettings.getInstance();

		// mRNA
		for (int i=0; i<numExperiments_; i++)
			for (int j=0; j<numGenes_; j++)
				ssPerturbation_.set(i, j, ssPerturbation_.get(i,j)/max);

		// proteins
		if (settings.getModelTranslation()) {
			for (int i=0; i<numExperiments_; i++)
				for (int j=0; j<numGenes_; j++)
					ssPerturbationProteins_.set(i, j, ssPerturbationProteins_.get(i,j)/max);
		}
		
	}

	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public DoubleMatrix2D getSsPerturbation() { return ssPerturbation_; }
	public DoubleMatrix2D getSsPerturbationProteins() { return ssPerturbationProteins_; }
	public ArrayList<Double> getTimeToConvergenceODE() { return timeToConvergenceODE_; }
	public void setTimeToConvergenceODE(ArrayList<Double> t) { timeToConvergenceODE_ = t; }
	public void setMaxtSDE(double maxt) { maxtSDE_ = maxt; }
	
}
