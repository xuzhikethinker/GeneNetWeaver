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
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/** This class implements the explicit Euler-Maruyama method (only for Ito scheme, strong order of convergence 0.5)
 * 
 * The iterative Euler method is given by the formula [1]
 * 
 * Xn+1 = Xn + Fn*h + Gn*dWn (Euler)
 * 
 * where h is the integration step size. This method requires to use the Ito scheme.
 * If one wants to integrate using the Stratonovich scheme, the Euler-Heun method
 * can be applied (see below).
 * 
 * If the diffusion term is zero, then the problem is fully deterministic. In this case,
 * the Euler method achieves strong order of convergence of 1. Following Kloeden et al. 
 * (1994), Euler method can have strong order of convergence of 1 in case of additive noise,
 * i.e. when the diffusion term is constant and has the form
 * 
 * G(t,x) := G(t)
 * 
 * with appropriate smoothness assumptions on both drift F and diffusion G term.
 * 
 * "Note however that the most widely used Euler scheme for the numeric solution of 
 * Langevin equations requires the equation to be in Ito form."
 * http://en.wikipedia.org/wiki/Stratonovich_integral
 * 
 * If one wants to integrate using the Stratonovich scheme, the Euler-Heun method
 * can be used [1-2]
 * 
 * Xn+1 = Xn + Fn*h + 0.5[Gn + G(Xaux)]*dWn (Euler-Heun)
 * Xaux = Xn + Gn*dWn
 * 
 * 
 * [1] P.E. Kloeden, E. Platen, and H. Schurz, Numerical solution of SDE through
 *     computer experiments, Springer, 1994. (pp 98, 150-153)
 * 
 * [2] H. Gilsing and T. Shardlow, SDELab: A package for solving stochastic differ-
 *     ential equations in MATLAB, Journal of Computational and Applied Math-
 *     ematics 205 (2007), no. 2, 1002–1018.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class Euler extends SdeSolver {
	
	
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Default constructor
	 */
	public Euler() {
		super();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Steps the integration of one step size h.
	 * @throws Exception
	 */
	public void advance(final double t, final double h, final DoubleMatrix1D dW,
			final DoubleMatrix1D dZ, DoubleMatrix1D Xin, DoubleMatrix1D Xout) throws Exception {
		
		int n = system_.getDimension();
		
		if (system_.getScheme() == Sde.ITO) {
			for (int i=0; i<n; i++)
				Xout.set(i, Xin.get(i) + F_.get(i)*h + G_.get(i, i)*dW.get(i));
		}
		else if (system_.getScheme() == Sde.STRATONOVICH) {
			// Computes Xaux to compute later GXaux
			DoubleMatrix1D Xaux = new DenseDoubleMatrix1D(n);
			for (int i=0; i<n; i++)
				Xaux.set(i, Xin.get(i) + G_.get(i, i)*dW.get(i)); // Xaux = Xn + Gn*dWn
			// Computes GXaux
			DoubleMatrix2D GXaux = new DenseDoubleMatrix2D(n,n);
			system_.getDriftAndDiffusion(t, Xaux, new DenseDoubleMatrix1D(n), GXaux);
			
			for (int i=0; i<n; i++) {
				Xout.set(i, Xin.get(i) + F_.get(i)*h + 0.5*(G_.get(i, i) + GXaux.get(i, i))*dW.get(i));
			}
		}
		else
			throw new Exception("Euler:advance(): unhandled scheme for \"" + system_.getId()  + "\" (" + system_.getScheme() + ")");
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns the description of this solver.
	 */
	public String getDescription() {
		
		String desc = "Euler-Maruyama solver (explicit), Kloeden et al., 1994.\n";
		desc += "Euler-Heun method is used for Stratonovich scheme!";
		desc += "Strong/weak convergence orders: 0.5/1.0\n";
		desc += "Only diagonal noise is handled.";
		
		return desc;
	}
}
