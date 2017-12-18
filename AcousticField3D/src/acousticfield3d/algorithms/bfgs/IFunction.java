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
public interface IFunction {
    int getDimensions();
    double evaluate(final double[] vars);
    void gradient(final double[] vars, double[] g);
}
