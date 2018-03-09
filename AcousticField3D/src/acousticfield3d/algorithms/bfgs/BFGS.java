/* Copyright (C) 2005 Vladimir Roubtsov. All rights reserved.
    Modified by Asier Marzo 2014
 */
package acousticfield3d.algorithms.bfgs;

public class BFGS
{
    int cMaxIterations;
    double cTolX;
    double cTolGradient;
    
    int lastNumberOfIterations;
    boolean minTolX, minTolGradient;
    BFGSProgressListener listener;
    int hessianUpdates;
    double alpha;
    
    public BFGS (){   
        alpha = 1.0;
        lastNumberOfIterations = 0;
        cMaxIterations = -1;
        cTolX = Double.NaN;
        cTolGradient = Double.NaN;  
        
        cMaxIterations = 200;
        cTolX = 1.0E-8;
        cTolGradient = 1.0E-8;
    }

    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    
    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public BFGSProgressListener getListener() {
        return listener;
    }

    public void setListener(BFGSProgressListener listener) {
        this.listener = listener;
    }
    
    public int getcMaxIterations() {
        return cMaxIterations;
    }

    public void setcMaxIterations(int cMaxIterations) {
        this.cMaxIterations = cMaxIterations;
    }

    public double getcTolX() {
        return cTolX;
    }

    public void setcTolX(double cTolX) {
        this.cTolX = cTolX;
    }

    public double getcTolGradient() {
        return cTolGradient;
    }

    public void setcTolGradient(double cTolGradient) {
        this.cTolGradient = cTolGradient;
    }

    public int getLastNumberOfIterations() {
        return lastNumberOfIterations;
    }

    public boolean isMinTolX() {
        return minTolX;
    }

    public boolean isMinTolGradient() {
        return minTolGradient;
    }

    public int getHessianUpdates() {
        return hessianUpdates;
    }

    
//</editor-fold>
    
    private static final double ZERO_PRODUCT = 1.0E-19;
     
    public double oldMinimize (final IFunction function, final double[] initial, double[] result){
        lastNumberOfIterations = 0;
        hessianUpdates = 0;
        minTolX = false;
        minTolGradient = false;
        final int N = function.getDimensions(); //number of variables
        final double [] start = initial; //initial position
        final double [] optimum = result; //where to place the result
        
        if (N != initial.length || N != result.length){
            throw new IllegalArgumentException("function, initial or result have different dimensionality " 
                    + N + ", " + initial.length + ", " + result.length);
        }
         
        //allocate variables
        final double [] direction = new double [N]; // x_k+1 = x_k + alpha_k*direction_k
        
        double [] x = optimum; //current position
        double [] xPrev = (double []) start.clone (); //previous position
        final double [] xDiff = new double [N];  // x - xPrev
        
        double [] grad = new double [N]; //current gradient
        double [] gradPrev = new double [N]; //previous gradient
        final double [] gradDiff = new double [N];  // gfx - gfxPrev
        
        final double [][] iHessian = new double [N][N]; // inverse Hessian approximation
        final double [] Dqi = new double [N]; // Dq_i = |D_i|.q_i:
        
        //start f(x) and Gf(x))
        double fx = function.evaluate (xPrev); // starting value of f
        function.gradient(xPrev, gradPrev); // starting gradient of f
        
        //initialize inverse hessian aproximation and direction of the search
        for (int d = 0; d < N; ++ d){
            // initialize D to identity
            iHessian [d][d] = 1.0;
            // set initial direction to opposite of the starting gradient
            direction [d] = - gradPrev [d];
        }
        
        final int maxiterations = cMaxIterations;
        final double tolX = cTolX;
        final double tolGradient = cTolGradient;
        
        double deltaX = 0, deltaG = 0, temp1, temp2; //aux variables
        // perform quasi-Newton iteration steps:
        for (int i = 0; i < maxiterations; ++ i){
            
            if(listener != null){
                listener.bfgsOnStep(i, maxiterations, deltaX, deltaG, hessianUpdates);
            }
            
            // do the line search in the current direction:
            try{
                fx = LineSearch.search (function, fx, gradPrev, xPrev, direction, x, alpha); // this updates fx and x
            }catch(IllegalArgumentException ex){
                break;
            }
            
            //compute xDiff, if it is lower than the tolerance -> FINISH
            deltaX = 0.0;
            for (int d = 0; d < N; ++ d){
                xDiff[d] = x [d] - xPrev [d];
                
                temp1 = xDiff[d];
                temp2 = Math.abs (temp1) / Math.max (Math.abs (x [d]), 1.0);
                if (temp2 > deltaX) deltaX = temp2;
            }
            if (deltaX < tolX){
                minTolX = true;
                break;
            }
            
            // get the current gradient
            function.gradient (x, grad);
            
            // if the current gradient (normalized by the current x and fx) is below tolerance -> FINISH
            deltaG = 0.0;
            temp1 = Math.max (fx, 1.0);
            for (int d = 0; d < N; ++ d){
                temp2 = Math.abs (grad [d]) * Math.max (Math.abs (x [d]), 1.0) / temp1;
                if (temp2 > deltaG) deltaG = temp2;
            }
            if (deltaG < tolGradient){
                minTolGradient = true;
                break;
            }
            
            
            // compute gradDiff
            for (int d = 0; d < N; ++ d){
                gradDiff [d] = grad [d] - gradPrev [d];
            }
            // compute Dqi
            for (int m = 0; m < N; ++ m){
                Dqi [m] = 0.0;
                for (int n = 0; n < N; ++ n){
                    Dqi [m] += iHessian [m][n] * gradDiff [n];
                }
            }
            
            //compute norms to check for linear dependency
            // IH should not be updated when successive pi's are almost linearly dependant
            double piqi = 0.0;
            double qiDqi = 0.0;
            double pi_norm = 0.0, qi_norm = 0.0;
            for (int d = 0; d < N; ++ d){
                temp1 = gradDiff [d];
                temp2 = xDiff [d];
                piqi += temp2 * temp1;
                qiDqi += temp1 * Dqi [d];
                qi_norm += temp1 * temp1;
                pi_norm += temp2 * temp2;
            }
            
            // update D using BFGS formula if it is not linearly dependant
            if (piqi > ZERO_PRODUCT * Math.sqrt (qi_norm * pi_norm)){
                hessianUpdates++;
                // re-use qi vector to compute v in Bertsekas:
                for (int d = 0; d < N; ++ d){
                    gradDiff [d] = xDiff [d] / piqi - Dqi [d] / qiDqi; 
                }
                for (int m = 0; m < N; ++ m){
                    for (int n = m; n < N; ++ n){
                        iHessian [m][n] += xDiff [m] * xDiff [n] / piqi  - Dqi [m] * Dqi [n] / qiDqi + qiDqi * gradDiff [m] * gradDiff [n];
                        iHessian [n][m] = iHessian [m][n];
                    }
                }
            }
            
            // set current direction for the next iteration as -|D|.Vfx 
            for (int m = 0; m < N; ++ m){
                direction [m] = 0.0;
                for (int n = 0; n < N; ++ n){
                    direction [m] -= iHessian [m][n] * grad [n]; 
                }
            }
            
            // switch current point and current gradient
            if (i != maxiterations - 1) {
                double [] temp = grad;
                grad = gradPrev;
                gradPrev = temp;
                
                temp = x;
                x = xPrev;
                xPrev = temp;
            }
            
            lastNumberOfIterations++;
        } //end of for nIterations
        
        // copy the final position into results if necessary
        if (optimum != x){
            for (int d = 0; d < N; ++ d){
                optimum [d] = x [d];
            }
        }
        
        return fx;
    }
   
    public double minimize (final IFunction function, final double[] initial, double[] result){
        lastNumberOfIterations = 0;
        hessianUpdates = 0;
        minTolX = false;
        minTolGradient = false;
        final int N = function.getDimensions(); //number of variables
        final double [] start = initial; //initial position
        final double [] optimum = result; //where to place the result
        
        if (N != initial.length || N != result.length){
            throw new IllegalArgumentException("function, initial or result have different dimensionality " 
                    + N + ", " + initial.length + ", " + result.length);
        }
         
        //allocate variables
        final double [] direction = new double [N]; // x_k+1 = x_k + alpha_k*direction_k
        
        double [] x = optimum; //current position
        double [] xPrev = (double []) start.clone (); //previous position
        final double [] xDiff = new double [N];  // x - xPrev
        
        double [] grad = new double [N]; //current gradient
        double [] gradPrev = new double [N]; //previous gradient
        final double [] gradDiff = new double [N];  // gfx - gfxPrev
        
        final double [][] iHessian = new double [N][N]; // inverse Hessian approximation
        final double [] Dqi = new double [N]; // Dq_i = |D_i|.q_i:
        
        //start f(x) and Gf(x))
        double fx = function.evaluate (xPrev); // starting value of f
        function.gradient(xPrev, gradPrev); // starting gradient of f
        
        //initialize inverse hessian aproximation and direction of the search
        for (int d = 0; d < N; ++ d){
            // initialize D to identity
            iHessian [d][d] = 1.0;
            // set initial direction to opposite of the starting gradient
            direction [d] = - gradPrev [d];
        }
        
        final int maxiterations = cMaxIterations;
        final double tolX = cTolX;
        final double tolGradient = cTolGradient;
        
        double deltaX = 0, deltaG = 0, temp1, temp2; //aux variables
        // perform quasi-Newton iteration steps:
        for (int i = 0; i < maxiterations; ++ i){
            
            if(listener != null){
                listener.bfgsOnStep(i, maxiterations, deltaX, deltaG, hessianUpdates);
            }
            
            // do the line search in the current direction:
            try{
                fx = LineSearch.search (function, fx, gradPrev, xPrev, direction, x, alpha); // this updates fx and x
            }catch(IllegalArgumentException ex){
                break;
            }
            
            //compute xDiff, if it is lower than the tolerance -> FINISH
            deltaX = 0.0;
            for (int d = 0; d < N; ++ d){
                xDiff[d] = x [d] - xPrev [d];
                
                temp1 = xDiff[d];
                temp2 = Math.abs (temp1) / Math.max (Math.abs (x [d]), 1.0);
                if (temp2 > deltaX) deltaX = temp2;
            }
            if (deltaX < tolX){
                minTolX = true;
                break;
            }
            
            // get the current gradient
            function.gradient (x, grad);
            
            // if the current gradient (normalized by the current x and fx) is below tolerance -> FINISH
            deltaG = 0.0;
            temp1 = Math.max (fx, 1.0);
            for (int d = 0; d < N; ++ d){
                temp2 = Math.abs (grad [d]) * Math.max (Math.abs (x [d]), 1.0) / temp1;
                if (temp2 > deltaG) deltaG = temp2;
            }
            if (deltaG < tolGradient){
                minTolGradient = true;
                break;
            }
            
            
            // compute gradDiff
            for (int d = 0; d < N; ++ d){
                gradDiff [d] = grad [d] - gradPrev [d];
            }
            // compute Dqi
            for (int m = 0; m < N; ++ m){
                Dqi [m] = 0.0;
                for (int n = 0; n < N; ++ n){
                    Dqi [m] += iHessian [m][n] * gradDiff [n];
                }
            }
            
            //compute norms to check for linear dependency
            // IH should not be updated when successive pi's are almost linearly dependant
            double piqi = 0.0;
            double qiDqi = 0.0;
            double pi_norm = 0.0, qi_norm = 0.0;
            for (int d = 0; d < N; ++ d){
                temp1 = gradDiff [d];
                temp2 = xDiff [d];
                piqi += temp2 * temp1;
                qiDqi += temp1 * Dqi [d];
                qi_norm += temp1 * temp1;
                pi_norm += temp2 * temp2;
            }
            
            // update D using BFGS formula if it is not linearly dependant
            if (piqi > ZERO_PRODUCT * Math.sqrt (qi_norm * pi_norm)){
                hessianUpdates++;
                // re-use qi vector to compute v in Bertsekas:
                for (int d = 0; d < N; ++ d){
                    gradDiff [d] = xDiff [d] / piqi - Dqi [d] / qiDqi; 
                }
                for (int m = 0; m < N; ++ m){
                    for (int n = m; n < N; ++ n){
                        iHessian [m][n] += xDiff [m] * xDiff [n] / piqi  - Dqi [m] * Dqi [n] / qiDqi + qiDqi * gradDiff [m] * gradDiff [n];
                        iHessian [n][m] = iHessian [m][n];
                    }
                }
            }
            
            // set current direction for the next iteration as -|D|.Vfx 
            for (int m = 0; m < N; ++ m){
                direction [m] = 0.0;
                for (int n = 0; n < N; ++ n){
                    direction [m] -= iHessian [m][n] * grad [n]; 
                }
            }
            
            // switch current point and current gradient
            if (i != maxiterations - 1) {
                double [] temp = grad;
                grad = gradPrev;
                gradPrev = temp;
                
                temp = x;
                x = xPrev;
                xPrev = temp;
            }
            
            lastNumberOfIterations++;
        } //end of for nIterations
        
        // copy the final position into results if necessary
        if (optimum != x){
            for (int d = 0; d < N; ++ d){
                optimum [d] = x [d];
            }
        }
        
        return fx;
    }
    
}