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

import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.random.Normal;


/** Abstract class for an experiment type.
 * 
 * Subclasses are SteadyStateExperiment
 * and TimeSeriesExperiment. The subclasses define the data types and implement
 * the corresponding simulations, this class contains everything that is common
 * to the different experiment types.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
abstract public class Experiment {

	/** Define which type of solver to use (ODE or SDE) */
	Solver.type solverType_;
	
	/** The label of the experiment (appended to filenames when saving, e.g. wildtype, knockouts, ...) */
	protected String label_;
	
	/** Reference to GeneNetwork object */
	protected GeneNetwork grn_;
	/** Number of genes */
	protected int numGenes_;
	
	/** Defines the perturbations to be applied (compute only wild-type if null) */
	protected Perturbation perturbation_;
	/** Number of perturbations, if perturbation_ is set, otherwise 1 (only wild-type) */
	protected int numExperiments_;
	/** Initial conditions (mRNA and proteins in one concatenated vector) */
	protected DoubleMatrix1D xy0_;

	/** Set true if proteins are modeled */
	protected boolean modelTranslation_;
	
	/** Normal distribution used to generate the different types of noise */
	private Normal normalDistribution_;
	/** Set true to add normal noise to the data */
	private boolean addNormalNoise_;
	/** Set true to add lognormal noise to the data */
	private boolean addLognormalNoise_;
	/** Set true to use a realistic model of microarray noise, similar to a mix of normal and lognormal */
	private boolean addMicroarrayNoise_;
	/** The standard deviation of the normal noise */
	private double normalStdev_;
	/** The standard deviation of the lognormal noise */
	private double lognormalStdev_;
	/** Flag, set true after noise has been added to the data */
	protected boolean noiseHasBeenAdded_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(Experiment.class.getName());
	
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/** Perform the experiment */
	abstract void run(DoubleMatrix1D xy0);
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public Experiment(Solver.type solverType, Perturbation perturbation, String label) {
		
		GnwSettings set = GnwSettings.getInstance();
		solverType_ = solverType;
		label_ = label;
		grn_ = null;
		numGenes_ = 0;
		perturbation_ = perturbation;
		if (perturbation_ != null)
			numExperiments_ = perturbation_.getNumPerturbations();
		else
			numExperiments_ = 1;
		xy0_ = null;
		modelTranslation_ = set.getModelTranslation();
		normalDistribution_ = set.getNormalDistribution();
		
		addNormalNoise_ = set.getAddNormalNoise();
		addLognormalNoise_ = set.getAddLognormalNoise();
		addMicroarrayNoise_ = set.getAddMicroarrayNoise();
		if ((addMicroarrayNoise_ && addNormalNoise_) || (addMicroarrayNoise_ && addLognormalNoise_))
			throw new RuntimeException("You can't add both normal/lognormal noise and microarray noise");

		normalStdev_ = set.getNormalStdev();
		lognormalStdev_ = set.getLognormalStdev();
		
		noiseHasBeenAdded_ = false;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Add log-normal noise to the data point x, set values below threshold to zero. TODO check that normal/lognormal works */
	protected double addNoise(double x) {
		
		if (x < 0)
			throw new IllegalArgumentException("Experiment:addNoise(): x < 0!");
		
		double xPlusNoise = x;
		
		// Note, in the constructor we tested that not both microarray noise and normal/lognormal noise
		// is added, which makes no sense. However, normal and lognormal noise can be added together
		if (addLognormalNoise_)
			xPlusNoise = addLogNormalNoise(x);
		if (addNormalNoise_)
			xPlusNoise += normalDistribution_.nextDouble(0, normalStdev_); // '+=' because we use mean 0
		if (addMicroarrayNoise_)		
			xPlusNoise = addMicroarrayNoise(x);
			
		if (xPlusNoise < 0)
			xPlusNoise = 0;
		
		return xPlusNoise;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Add log-normal noise to the data point x */
	private double addLogNormalNoise(double x) {
		
		if (x < 0)
			throw new IllegalArgumentException("Experiment:addNoise(): x < 0!");
		else if (x == 0.0)
			return 0.0;
		
		// transform to log-scale
		double xLog = Math.log10(x); // TODO does it make a difference whether we use log10, log2...?
		
		// add normal noise with mean zero: y = x + n(0, s) = n(m, s)
		xLog = normalDistribution_.nextDouble(xLog, lognormalStdev_);
		
		return Math.pow(10, xLog);
	}
	
	
	//	----------------------------------------------------------------------------
	
	/** Use the model of microarray noise by Tu et al. (2002) */
	private double addMicroarrayNoise(double x) {
		
		if (x < 0)
			throw new IllegalArgumentException("Experiment:addNoise(): x < 0!");
		else if (x == 0.0)
			return 0.0;
		
		// TODO allow these parameters to be set by the user? (btw, this could be done only once)
		double alpha = 0.001;
		double beta = 0.69;
		double K = 0.01;
		
		double variance = alpha + (beta - alpha)/(1 + (x/K));
		double w = normalDistribution_.nextDouble(0, Math.sqrt(variance));
		
		return x*Math.exp(w);
	}

	
	// ----------------------------------------------------------------------------
	
	/** Concatenate two vectors into one array */
	protected double[] concatenateVectors(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		
		double[] output = new double[v1.size() + v2.size()];
		
		for (int i=0; i < v1.size(); i++)
			output[i] = v1.get(i);
		
		for (int i=0; i < v2.size(); i++)
			output[v1.size() + i] = v2.get(i);
		
		return output;
	}

	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getLabel() { return label_; }

	public void setNumGenes(int numGenes) { numGenes_ = numGenes; }
	public int getNumGenes() { return numGenes_; }
	
	public Perturbation getPerturbation() { return perturbation_; }
	
	public GeneNetwork getGrn() { return grn_; }
	/** Set the grn_ and numGenes_ */
	public void setGrn(GeneNetwork grn) {
		grn_ = grn;
		numGenes_ = grn.getSize();
	}
	
}