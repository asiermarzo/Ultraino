/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.algorithms.bfgs;

/**
 *
 * @author Asier
 */
public interface BFGSProgressListener {
    void bfgsOnStep(int currentSteps, int totalSteps, double diffX, double diffG, int hessians);
    void bfgsOnFinish(int iters, boolean didExitX, boolean didExitG, int hessians, double fx, double[] x);
}
