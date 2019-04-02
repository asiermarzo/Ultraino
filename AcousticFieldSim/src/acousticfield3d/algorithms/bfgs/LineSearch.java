/* Copyright (C) 2005 Vladimir Roubtsov. All rights reserved.
 */
package acousticfield3d.algorithms.bfgs;

/**
 * @author Vlad Roubtsov
 */
public class LineSearch
{
    //Armijo rule
    private static final double SIGMA = 1.0E-4;
    private static final double BETA = 0.5;
    
    private static final double ZERO = 1.0E-19;
    //private static final double ZERO = 1.0E-8;
    
    public static double search (final IFunction f,
                                 final double fx, final double [] grad, final double [] x,
                                 double [] direction, final double [] xOut,
                                 double alpha)
    {
        
        
        direction = (double []) direction.clone ();
        
        final int N = x.length;
        
        // compute direction normalizer:
        double dnorm = 0.0;
        for (int d = 0; d < N; ++ d){
            final double t = direction [d];
            dnorm += t * t;
        }
        dnorm = Math.sqrt (dnorm);
        
        if (dnorm <= ZERO)
            throw new IllegalArgumentException ("'direction' is a zero vector");
        
        // normalize direction (to avoid making the initial step too big):
        for (int d = 0; d < N; ++ d){
            direction [d] /= dnorm;
        }
        
        // compute grad * direction (normalized):
        double p = 0.0;
        for (int d = 0; d < N; ++ d){
            p += grad [d] * direction [d];
        }
        if (p >= 0.0)
            throw new IllegalArgumentException ("'direction' is not a descent direction [p = " + p + "]");
        
          
        for (int i = 0; ; ++ i){
            // take the step:
            
            for (int d = 0; d < N; ++ d){
                xOut [d] = x [d] + alpha * direction [d];
            }
            
            final double fxAlpha = f.evaluate (xOut);
       
            if (fxAlpha < fx + SIGMA * alpha * p){
                return fxAlpha;
            }else{
                if (i == 0){
                    // first step: do quadratic approximation along the direction
                    // line and set alpha to be the minimizer of that approximation:
                    alpha = 0.5 * p / (p + fx - fxAlpha);
                }else{
                    alpha *= BETA; // reduce the step
                }
            }
            
            // prevent alpha from becoming too small
            if (alpha < ZERO) {
                if (fxAlpha > fx){
                    for (int d = 0; d < N; ++ d){
                        xOut [d] = x [d];
                    }
                    
                    return fx;
                }else{
                    return fxAlpha;
                }
            }
        }
    }

}