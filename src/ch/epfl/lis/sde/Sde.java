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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;


/** This class represents a system of stochastic differential equations (SDE).
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public abstract class Sde {

	/** Ito scheme */
	public static final int ITO = 1;
	/** Stratonovich scheme */
	public static final int STRATONOVICH = 2;
	
	/** Scheme to use during the numerical integration */
	protected int scheme_;
	
	/** Identifier for this function */
	protected String id_;
	/** Dimension N of the SDE */
	protected int dimension_;
	/** Initial condition */
	protected DoubleMatrix1D X0_;
	
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/**
	 * Computes the drift coefficients F and diffusion coefficient G at a given time.
	 * @throws Exception If the scheme asked for is not implemented.
	 */
	abstract public void getDriftAndDiffusion(final double t, final DoubleMatrix1D Xin, 
			DoubleMatrix1D F, DoubleMatrix2D G) throws Exception ;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor (default scheme is Ito)
	 * @param dimension Dimension of the system
	 */
	public Sde(int dimension) {
		
		this(dimension, Sde.ITO, null);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This constructor allows to specify the scheme used during numerical integrations
	 * (Ito or Stratonovich).
	 * @param dimension Dimension of the system
	 * @param scheme Itô or Stratonovich scheme
	 */
	public Sde(int dimension, int scheme) {
		
		this(dimension, scheme, null);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This constructor allows to define the scheme used during numerical integrations
	 * (Ito or Stratonovich) and the initial condition X0.
	 */
	public Sde(int dimension, int scheme, DoubleMatrix1D X0) {
		
		dimension_ = dimension;
		scheme_ = scheme;
		id_ = "noid";
		
		X0_ = new DenseDoubleMatrix1D(dimension);
		
		if (X0 == null)
			X0_.assign(1.0); // often better suited than X0=0 (e.g. exp functions)
		else
			X0_.assign(X0);
	}
	
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setDimension(int dimension) { dimension_ = dimension; } 
	public int getDimension() { return dimension_; }
	
	public void setId(String id) { id_ = id; }
	public String getId() { return id_; }
	
	public void setScheme(int scheme) { scheme_ = scheme; }
	public int getScheme() { return scheme_; }
	
	public void setX0(DoubleMatrix1D X0) { X0_.assign(X0); }
	public DoubleMatrix1D getX0() { return X0_; }
}
