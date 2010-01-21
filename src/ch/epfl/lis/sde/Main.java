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
import cern.colt.matrix.impl.DenseDoubleMatrix1D;


/** This class illustrates the functionalities of the SDE package.
 * 
 * Introduction of the SDE package and how to use it to numerically
 * integrate a system of stochastic differential equations (SDE). The
 * time series of the integration is save to the file integration.tsv
 * in the current directory. The first column of this file represents
 * the time scale. The (m+1)th column represents the trajectories of
 * the mth variables of the system.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
@SuppressWarnings("unused")
public class Main {

	public static void main(String[] args) {
		
		System.out.println("ch.epfl.lis.sde.Main()");
		
		/**
		 * Settings for the numerical integration
		 */
		SdeSettings settings = SdeSettings.getInstance();
		settings.setSeed(-1); // seed for the RNG, -1 to use
							  // current time as seed
		settings.setMaxt(1); // integration from t=0 to t=tmax
		settings.setDt(0.01); // step-size of the Wiener process
		settings.setMultiplier(1); // integration step size = settings.dt*
								   // settings.multiplier_
		
		/**
		 * Instantiate the SDE solver
		 */
		SdeSolver solver = SdeSolverFactory.createSolver(SdeSolverFactory.MILSTEIN);
		if (solver == null)
			System.out.println("Unable to instantiate the solver");
		
		/**
		 * Define the system to integrate
		 */
		TestFunction system = new TestFunction(10); // set the dimension of system
		system.setSigma(0.2);
//		DoubleMatrix1D X0 = new DenseDoubleMatrix1D(system.getDimension());
//		system.setX0(X0.assign(0)); // set the initial conditions
		system.setScheme(Sde.STRATONOVICH); // set the scheme (Ito or Stratonovich)
		solver.setSystem(system); // associate the system to the solver
		
		/**
		 * Run the numerical integration
		 */
		try {
			// run() is a demonstration function to show how integrate
			// a system of SDEs using the present SDE package.
			solver.run(101); // set the number of time points to save
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("ByeBye!");
	}
}
