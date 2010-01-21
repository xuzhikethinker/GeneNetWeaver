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
import java.net.URL;

import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;


/** Offers global parameters (settings) and functions used by the classes of the
 * the SDE package.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class SdeSettings {

	/** The unique instance of SDESettings (Singleton design pattern) */
	private static SdeSettings instance_ = null;
	
	/** Step size dt of the Wiener process (Brownian path) */
	private double dt_;
	/** Solver use stepSize_*multiplier_ as step size for integration  */
	private int multiplier_;
	/** Solver ends at t=maxt_ (start at t=0) */
	private double maxt_;
	/** Optional seed used to generate the Wiener process (no seed: -1) */
	private int seed_;
	
	/** Mersenne Twister random engine */
	private MersenneTwister mersenneTwister_;
	/** Normal distribution N(0,1) (use Polar Box-Muller transformation) */
	private Normal normalDistribution_;
	
	
	// ============================================================================
	// STATIC METHODS
	
	/**
	 * Get SDESettngs instance
	 */
	static public SdeSettings getInstance() {
		
		if (instance_ == null)
			instance_ = new SdeSettings();

		return instance_;
	}
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public SdeSettings() {
		
		dt_ = 0.01;
		multiplier_ = 1;
		maxt_ = 100.;

		setSeed(-1);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialise the random number generator with the defined seed.
	 */
	public void initializeRNG() {
		
		if (seed_ == -1)
			mersenneTwister_ = new MersenneTwister(new java.util.Date());
		else
			mersenneTwister_ = new MersenneTwister(seed_);
		
		normalDistribution_ = new Normal(0, 1, mersenneTwister_); // mean=0, std=1
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Save the content of the given matrix into file.
	 */
	public void saveMatrix(URL filename, DoubleMatrix2D M) {
		
		System.out.println("Write matrix to " + filename.getFile());
		
		if (M == null)
			throw new IllegalArgumentException("Matrix is null");
		
		if (filename == null)
			throw new IllegalArgumentException("URL to save matrix undefined");
		
		try {
			FileWriter fw = new FileWriter(filename.getPath(), false);
			
			for (int i=0; i<M.rows(); i++) {
				fw.write(Double.toString(M.get(i, 0)));
				for (int j=1; j<M.columns(); j++)
					fw.write("\t" + M.get(i, j));
				fw.write("\n");
			}
			fw.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setDt(double dt) { dt_ = dt; }
	public double getDt() { return dt_; }
	
	public void setMultiplier(int multiplier) { multiplier_ = multiplier; }
	public int getMultiplier() { return multiplier_; }
	
	public void setMaxt(double T) { maxt_ = T; }
	public double getMaxt() { return maxt_; }
	
	public void setSeed(int seed) { seed_ = seed; }
	public int getSeed() { return seed_; }
	
	public Normal getNormalDistribution() { return normalDistribution_; }
}
