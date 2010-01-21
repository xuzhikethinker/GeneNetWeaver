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

package ch.epfl.lis.sde;

import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.random.Normal;

/** This class serves as basis for the implementation of a SDE solver.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public abstract class SdeSolver {

	/** SDE system to solve */
	protected Sde system_;
	
	/** dW increments */
	protected DoubleMatrix2D W_;
	/** dZ increments */
	protected DoubleMatrix2D Z_;
	/** Approximation of the SDE system */
	protected DoubleMatrix1D X_;

	/** Time scale */
	protected DoubleMatrix1D time_;
	/** Time scale of the Wiener process */
	protected DoubleMatrix1D timeWiener_;
	/** Number of time points for X */
	protected int numTimePoints_;
	/** Number of time points for the Wiener process */
	protected int numTimePointsWiener_;
	/** Drift coefficients */
	protected DoubleMatrix1D F_;
	/** Diffusion coefficients */
	protected DoubleMatrix2D G_;
	/** Internal integration step size */
	protected double h_;
	/** External integration step  size */
	protected double H_;
	
	/** Set to true if only X >= 0 is wished */
	protected boolean XPositiveOnly_;
	/** if Xpositive is true, count the number of time that a X has at least one element < 0 */
	protected int XNegativeCounter_;
	
	/** Absolute _or_ relative precision _per variable_ need to be satisfied for convergence */
	private double absolutePrecision_ = 0.000001;
	/** See absolutePrecision_, in addition, this is also the tolerance used for integration */ 
	private double relativePrecision_ = 0.0001;
	
	/** Current number of evaluations of the system */
	private int numEvaluations_;
	
	/** Is true if the system has converged */
	private boolean converged_;
	
    /** Logger for this class */
	private static Logger log = Logger.getLogger(SdeSolver.class.getName());

	
    // =======================================================================================
    // ABSTRACT METHOD
	
	/**
	 * Proceed on iteration of the numerical integration.
	 * @throws Exception
	 */
	abstract public void advance(final double t, final double h, final DoubleMatrix1D dW, 
			final DoubleMatrix1D dZ, final DoubleMatrix1D Xin, DoubleMatrix1D Xout) throws Exception;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public SdeSolver() {
		
		system_ = null;
		reset();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * For the need of each step(), W must be regenerated.
	 */
	public void generateW() {
		
		Normal normal = SdeSettings.getInstance().getNormalDistribution();
		double sqrt_dt = Math.sqrt(SdeSettings.getInstance().getDt());
		
		for (int i=0; i<numTimePointsWiener_; i++) {
			for (int j=0; j<system_.getDimension(); j++) {
				double N1 = normal.nextDouble();
				W_.set(i, j, N1*sqrt_dt);
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Regenerate W and Z (Z are usefull for Runge-Kutta of strong order >= 1.5).
	 */
	public void generateWZ() {
		
		SdeSettings settings = SdeSettings.getInstance();
		double sqrt_dt = Math.sqrt(settings.getDt());
		double sqrt_3 = Math.sqrt(3.);
		double pow_dt_3_2 = Math.pow(settings.getDt(), 3.0/2.0);
		double N1, N2;

		for (int i=0; i<numTimePointsWiener_; i++) {
			for (int j=0; j<system_.getDimension(); j++) {
				N1 = settings.getNormalDistribution().nextDouble();
				N2 = settings.getNormalDistribution().nextDouble();
				W_.set(i, j, N1*sqrt_dt);
				Z_.set(i, j, 0.5*(N1+1/sqrt_3*N2)*pow_dt_3_2);
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Must be call after the SDE parametrized and associated to this solver.
	 * The correct seed and all the SDESettings parameters must also have been set.
	 * @param H
	 */
	public void initialize(double H) {
		
		// Print the description of the solver
		//log.info(getDescription());
		
		if (system_ == null)
			throw new IllegalArgumentException("No system has been set");
		
		SdeSettings settings = SdeSettings.getInstance();
		if (settings.getMultiplier() == 0 || settings.getDt() == 0) {
			log.info("Nothing to do");
			return;
		}
			
		int n = system_.getDimension();
		
		// The time the integration will step when step() is called
		// Real integration step is h_ < H_
		H_ = H;

		// Set number of time points to save the solution and the Wiener process
		// => Actually, we want only to save the current solution
		numTimePoints_ = 1;
		if (numTimePoints_ == 1 && settings.getMultiplier() != 1)
			numTimePointsWiener_ = settings.getMultiplier();
		else
			numTimePointsWiener_ = (int) (numTimePoints_-1)*settings.getMultiplier() + 1;
		
		// Set the X with X(0)
		X_ = new DenseDoubleMatrix1D(n);
		X_.assign(system_.getX0());
		
		// Set the time scale
		time_ = new DenseDoubleMatrix1D(numTimePoints_);
		timeWiener_ = new DenseDoubleMatrix1D(numTimePointsWiener_);
		
		h_ = settings.getDt()*(double)settings.getMultiplier(); // the integration step
		H_ = H; // when step() is called, the integration will step with time H_ (integration steps are still h_)
		
		W_ = new DenseDoubleMatrix2D(numTimePointsWiener_, n);
		Z_ = new DenseDoubleMatrix2D(numTimePointsWiener_, n);
		
		settings.initializeRNG(); // use the defined seed to set up the RNG
		
		// drift vector and diffusion matrix
		F_ = new DenseDoubleMatrix1D(n);
		G_ = new DenseDoubleMatrix2D(n, n);
		
		//System.out.println("Solver will run with parameters:");
		//System.out.println("System dimension: " + n);
		//System.out.println("Num points of wiener process: " + numTimePointsWiener_);
		//System.out.println("Integration step h: " + h_);
		//System.out.println("step() duration: " + H_);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Step the integration from the current time t1 to t1+H_, return H_.
	 * @throws Exception
	 */
	public double step() throws Exception {
		
		double t1 = time_.get(0); // get the current time
		double t1_bkp = t1; // to compute the effective step size in the end
		double t2 = t1 + H_; // get the time at which step() must return
		
		int n = system_.getDimension();
		DoubleMatrix1D Xout = new DenseDoubleMatrix1D(n);
		DoubleMatrix1D dW = new DenseDoubleMatrix1D(n);
		
		while (t1 < t2) {
			
			generateW(); // generate new independent Wiener process samples
			
			for (int j=0; j<n; j++) {
				dW.set(j, 0);
				for (int k=0; k < W_.rows(); k++)
					dW.set(j, dW.get(j)+W_.get(k, j));
			}
			
			// compute the drift and the diffusion
			system_.getDriftAndDiffusion(t1, X_, F_, G_);
			
			// compute the next approximation
			advance(t1, h_, dW, null, X_, Xout);
			
			// check the solution before saving it
			checkX(Xout);
			
			// check convergence
//			checkConvergence();
			
			// save the current solution
			X_.assign(Xout);
			
			t1 += h_;
			numEvaluations_++;
		}
		
		time_.set(0, t2);
		return time_.get(0) - t1_bkp;
	}
	
	
	// ----------------------------------------------------------------------------
	
//	public void checkConvergence() {
//		
//		int n = system_.getDimension();
//		int windowSize = window_.rows();
//		
//		// Save the current solution in the window
//		for (int j=0; j<n; j++)
//			window_.set(numEvaluations_ % windowSize, j, X_.get(j));
//		
//		// If the window is large enough to compute averagX_
//		if (numEvaluations_>=windowSize-1) {
//
//			averagedX_.assign(window_.zMult(averager_, null, 1, 0, true));
//			
//			if (numEvaluations_>windowSize-1)
//				converged_ = converged(averagedX_, previousAveragedX_);
//			
//			previousAveragedX_.assign(averagedX_);
//		}
//	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This function performs a check on the current solution X. For instance we want
	 * that the mRNA and protein concentrations are never lower that 0. In this case,
	 * each time a element of X is lower than 0, this element is set to zero.
	 */
	public void checkX(DoubleMatrix1D X) {
		
		if (XPositiveOnly_) {
			
			int n = system_.getDimension();
			int count = 0;
			
			for(int i=0; i<n; i++) {
				if (X.get(i) < 0) {
					X.set(i, 0);
					count++;
				}
			}
			
			if (count > 0)
				XNegativeCounter_++;
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Cleanup the solver
	 */
	public void reset() {
		
		W_ = null;
		Z_ = null;
		X_ = null;
		time_ = null;
		timeWiener_ = null;
		numTimePoints_ = 0;
		numTimePointsWiener_ = 0;
		F_ = null;
		G_ = null;
		h_ = 0.;
		H_ = 0.;
		numEvaluations_ = 0;
		converged_ = false;
		XNegativeCounter_ = 0;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns the description of this solver.
	 */
	public String getDescription() {
		
		return "No description for this solver";
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Standard function to integrate easily a SDE.
	 */
	public void run(int numTimePoints) {
		
		SdeSettings settings = SdeSettings.getInstance();

		int n = system_.getDimension();
		double t = 0;
		double tmax = settings.getMaxt();
		
		initialize(tmax/(numTimePoints-1));
		
		DoubleMatrix1D time = new DenseDoubleMatrix1D(numTimePoints);
		DoubleMatrix2D ts = new DenseDoubleMatrix2D(numTimePoints, n);
		
		int index = 0;
		time.set(index, time_.get(0)); // save current time
		for (int i=0; i<n; i++)
			ts.set(index, i, X_.get(i)); // save the current solution
		index++;
		
		System.out.println("Integration starts now ...");
		
		do {
			try {
				step();
				t = time_.get(0);
				time.set(index, t); // save current time
				
				for (int i=0; i<n; i++)
					ts.set(index, i, X_.get(i)); // save the current solution
				
				index++;
				
			} catch (Exception e) {
				log.log(Level.INFO, "TimeSeriesExperiment.integrate(): Exception, t = " + t + ":" + e.getMessage());
				return;
			}
		} while (t < tmax);
		
		System.out.println("Integration ends ...");
		
		try {
			URL url = new URL("file://" + System.getProperty("user.dir") + "/integration.tsv");
			saveDataWithTime(url, ts, time);
		} catch (MalformedURLException e) {
			System.out.println("SdeSolver.run(): " + e.getMessage());
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Write the integration trajectories with the time scale into file.
	 */
	public void saveDataWithTime(URL filename, DoubleMatrix2D data, DoubleMatrix1D time) {
		
		System.out.println("Save data into " + filename.getFile());
		
		try {
			FileWriter fw = new FileWriter(filename.getPath(), false);
			int R = data.rows();
			int C = data.columns();
			
			for (int i=0; i<R; i++) {
				
				// first column of the file is time scale
				fw.write(Double.toString(time.get(i)));
				
				for (int j=0; j<C; j++)
					fw.write("\t" + data.get(i, j));
				
				fw.write("\n");
			}
			fw.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
//	/** 
//	 * Implements the method gsl_multiroot_test_delta() of GSL:
//	 * This function tests for the convergence of the sequence by comparing the last step dx
//	 * with the absolute error epsabs and relative error epsrel to the current position x.
//	 * The test returns true if the following condition is achieved:
//     * 		|dx_i| < epsabs + epsrel |x_i|
//     * for each component of x and returns false otherwise.
//	 */
//	public boolean converged(final DoubleMatrix1D X, final DoubleMatrix1D previousX) {
//		
//		int n = system_.getDimension();
//		double dx = 0.;
//		
//		for (int i=0; i<n; i++) {
//			
//			dx = Math.abs(X.get(i) - previousX.get(i));
//			
//			if (dx > absolutePrecision_ + relativePrecision_*Math.abs(X.get(i))) {				
//				return false;
//			}
//		}
//		return true;
//	}

	
    // =======================================================================================
    // GETTERS AND SETTERS
	
	public void setSystem(Sde sde) { system_ = sde; }
	public Sde getSystem() { return system_; }
	
	public DoubleMatrix1D getX() { return X_; }
	
	public DoubleMatrix1D getTime() { return time_; }
	
	public void setH(double H) { H_ = H;}
	public double getH() { return H_; }
	
	public void setXPositiveOnly(boolean b) { XPositiveOnly_ = b; }
	public boolean getXPositiveOnly() { return XPositiveOnly_; }
	
	public int getXNegativeCounter() { return XNegativeCounter_; }
	
	public void setAbsolutePrecision(double value) { absolutePrecision_ = value; }
	public double getAbsolutePrecision() { return absolutePrecision_; }
	
	public void setRelativePrecision(double value) { relativePrecision_ = value; }
	public double getRelativePrecision() { return relativePrecision_; }
	
	public boolean converged() { return converged_; }
}
