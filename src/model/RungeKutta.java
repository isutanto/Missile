package model;

public class RungeKutta {

	// The number of steps to use in the interval
	  public static final int STEPS = 100;


	  // The derivative dy/dx at a given value of x and y.
	  public static double deriv(double x, double y){
	    return x * Math.sqrt(1 + y*y);
	  }
	  
	  public double euclid(){
		// `h' is the size of each step.
		    double h = 1.0 / STEPS;
		    double k1, k2, k3, k4;
		    double x, y;
		    int i;
		    //y = 10;

		    // Computation by Euclid's method
		    // Initialize y
		    y = 0;
		    
		    for (i=0; i<STEPS; i++)
		    {
		      // Step through, updating x and incrementing y
		      x = i * h;

		      y += h * deriv(x, y);
		    }

		    return y;
	  }
	  
	  public double fourthorder(){
		  //4th order Runge-Kutta
		  // `h' is the size of each step.
		  double h = 1.0 / STEPS;
		  double k1, k2, k3, k4;
		  double x, y;
		  int i;
		  //y = 10;

		  y = 0;

		  for (i=0; i<STEPS; i++){
			  // Step through, updating x
		      x = i * h;

		      k1 = h * deriv(x, y);
		      k2 = h * deriv(x + h/2, y + k1/2);
		      k3 = h * deriv(x + h/2, y + k2/2);
		      k4 = h * deriv(x + h, y + k3);

		      // Incrementing y
		      y += k1/6 + k2/3+ k3/3 + k4/6;
		    }
		  
		    System.out.println(y);
		  return y;
	  }

}
