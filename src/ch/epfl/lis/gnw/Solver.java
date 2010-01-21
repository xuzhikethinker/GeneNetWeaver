package ch.epfl.lis.gnw;

import org.opensourcephysics.numerics.ODEMultistepSolver;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.sde.Sde;
import ch.epfl.lis.sde.SdeSettings;
import ch.epfl.lis.sde.SdeSolver;
import ch.epfl.lis.sde.SdeSolverFactory;

/**
 * This class can be used to integrate either ODEs or SDEs. It provides a
 * common interface for the two two types of solvers.
 * 
 * ODEs are numerically integrated using the ODEMultistepSolver from opensourcephysics. To step the time
 * by dt, it performs multiple smaller internal steps of fixed size to guarantee the
 * specified precision. However, there are problems for very large step sizes dt, I think
 * because the solver first tries to use a large internal step size, which can lead to 
 * negative concentrations. Also, I think there was another problem, either the maximum
 * number of iterations got exceeded or there is a bug in the opensourcephysics library
 * (the solver didn't step the time by the full dt it was asked to). For dt < 10, we never
 * experienced problems. So if the user specifies a dt > 10, we step the ODEMultistepSolver
 * multiple times (see numStepsODE_) (for each of these steps the ODEMultistepSolver will
 * in turn perform multiple steps to guarantee the precision).
 * 
 * SDEs are numerically integrated using our own solver, see the class SdeSolver.
 */
public class Solver {

	/** The type of solver (ordinary or stochastic differential equation) */
	public enum type {ODE, SDE, NONE};
	
	/**
	 * ODEMultistepSolver performs multiple ODE steps so that a uniform step size is maintained.
	 * Default engine is RK45.
	 * Note, since here we don't need a specific step size, I also tried the adaptive step size
	 * solver, but got problems with negative concentrations.
	 */
	private ODEMultistepSolver ODESolver_;
	/** Used by the ODESolver_ */
	private GeneNetworkODE ODE_;
	/** Solver for SDEs */
	private SdeSolver SDESolver_;
	/** Used by the SDESolver_ */
	private GeneNetworkSDE SDE_;
	/** For ODEs, we have to make sure that the time steps are not too big (see introductory comment for the class above) */
	private double numStepsODE_;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor
	 */
	public Solver(type solverType, GeneNetwork grn, double[] xy0) {
		
		double dt = GnwSettings.getInstance().getDt();
		if (dt < 10)
			numStepsODE_ = 1;
		else
			numStepsODE_ = 10*Math.floor(Math.log10(dt));
		
		if (solverType == type.ODE)
			initializeODE(grn, xy0);
		else if (solverType == type.SDE)
			initializeSDE(grn, xy0);
		else
			throw new IllegalArgumentException("Unknown simulation type");
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Step the time by dt, return the time step that the solver actually made (should be dt)
	 * @throws Exception 
	 */
	public double step() throws Exception {
		
		if (ODESolver_ != null) {
			double t = 0;
			for (int i=0; i<numStepsODE_; i++)
				t += ODESolver_.step();
			return t;
			
		} else if (SDESolver_ != null)
			return SDESolver_.step();
		else
			throw new RuntimeException("Solver not correctly initialized");
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Return the current state vector.
	 */
	public double[] getState() {
		
		if (ODE_ != null)
			return ODE_.getState();
		else if (SDE_ != null)
			return SDESolver_.getX().toArray();
		else
			throw new RuntimeException("Solver not correctly initialized");
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Return true if the solution converged
	 */
	public boolean converged() {
		
		if (ODE_ != null)
			return ODE_.converged();
		else if (SDE_ != null)
			return SDESolver_.converged();
		else
			throw new RuntimeException("Solver not correctly initialized");
	}
	
	
	
	// ============================================================================
	// PRIVATE METHODS

	/** Initialize the solver for deterministic simulation using ODEs */
	private void initializeODE(GeneNetwork grn, double[] xy0) {
		
		GnwSettings set = GnwSettings.getInstance();
		
		ODE_ = new GeneNetworkODE(grn, xy0);
		ODESolver_ = new ODEMultistepSolver(ODE_);
		
		ODESolver_.setTolerance(set.getRelativePrecision());
		// See introductory comment for class above
		ODESolver_.initialize(set.getDt()/numStepsODE_);		
		ODESolver_.setMaxIterations(1000);
		
		// Set SDE stuff to null
		SDE_ = null;
		SDESolver_ = null;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Initialize the solver for stochastic simulation using SDEs */
	private void initializeSDE(GeneNetwork grn, double[] xy0) {
		
		// Set SDE, solver, settings
		SDE_ = new GeneNetworkSDE(grn, Sde.STRATONOVICH, new DenseDoubleMatrix1D(xy0));
		SDESolver_ = SdeSolverFactory.createSolver(SdeSolverFactory.MILSTEIN); // integrate according to the scheme of the SDE
		SDESolver_.setSystem(SDE_);
		SDESolver_.setXPositiveOnly(true); // take care to not have negative concentration
		
		GnwSettings set = GnwSettings.getInstance(); 
		SdeSettings sdeSettings = SdeSettings.getInstance();
		// Set Wiener path step size
		sdeSettings.setDt(set.getTimeStepSDE());
		// Set relation between Wiener path step size and integration step size
		sdeSettings.setMultiplier(1);
		// Set the seed used to generate Wiener path
		sdeSettings.setSeed(set.getRandomSeed());
		// Set maxt
		//sdeSettings.setMaxt(maxt_);
		
		// Initialize only after having set all the necessary parameters in SDESettings
		SDESolver_.initialize(set.getDt());
		
		// Set ODE stuff to null
		ODE_ = null;
		ODESolver_ = null;
	}
	
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public SdeSolver getSDESolver() { return SDESolver_; }
	
}
