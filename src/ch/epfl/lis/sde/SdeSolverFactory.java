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

/** This class allows to instantiate easily SDE solvers.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class SdeSolverFactory {

	/** solvers */
	public static final int EULER = 1;
	public static final int MILSTEIN = 2;
	public static final int RUNGEKUTTA = 3;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public SdeSolverFactory() {
		
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns a solver of the desired type.
	 */
	public static SdeSolver createSolver(final int type) {
		
		SdeSolver solver = null;
		
		switch (type) {
		
			case EULER:
				solver = new Euler();
				break;
			case MILSTEIN:
				solver = new Milstein();
				break;
			case RUNGEKUTTA:
				System.out.println("Runge-Kutta solver not implemented yet.");
				break;
			default:
				System.out.println("Invalid selected solver");
		}
		
		return solver;
	}
}
