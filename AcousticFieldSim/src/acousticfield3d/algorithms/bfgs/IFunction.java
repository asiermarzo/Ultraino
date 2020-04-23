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
