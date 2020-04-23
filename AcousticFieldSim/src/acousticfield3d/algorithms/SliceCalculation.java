package acousticfield3d.algorithms;

import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector2i;
import acousticfield3d.math.Vector3f;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import java.util.ArrayList;

/**
 *
 * @author am14010
 */
public class SliceCalculation {
    final int nTrans;
    final int n,m;
    final double[][] amp, ta, tb;
    final double[][][] ka, kb;
    final double[] phasesA;
    final double[] phasesB;
    
    float sx,sy,sz,sw,sh; //position and dimensions fo the slice
    
    public SliceCalculation(int nTrans, int n, int m) {
        this.nTrans = nTrans;
        this.n = n;
        this.m = m;
        phasesA = new double[nTrans];
        phasesB = new double[nTrans];
        amp = new double[n][m];
        ta = new double[n][m];
        tb = new double[n][m];
        ka = new double[nTrans][n][m];
        kb = new double[nTrans][n][m];
    }
   
    public void initSliceXZ(final Simulation s, Vector3f slicePosition, float width, float height){
        final int minTrans = M.min(nTrans, s.transducers.size());
        
        sx = slicePosition.x;
        sy = slicePosition.y;
        sz = slicePosition.z;
        sw = width;
        sh = height;
        
        final float dw = sw/n;
        final float dh = sh/m;
        final float halfW = sw/2;
        final float halfH = sh/2;
        
        for (int it = 0; it < minTrans; it++) {
            //get position of the transducer
            final Transducer t = s.transducers.get(it);
            for (int in = 0; in < n; in++) {
                for (int im = 0; im < m; im++) {
                    final Vector2f prop = CalcField.calcFieldForTrans(t, 0, 
                            sx + in*dw - halfW, 
                            sy, 
                            sz + im*dh - halfH, s);
                    ka[it][in][im] = prop.x;
                    kb[it][in][im] = prop.y;
                }
            }
        }
        
    }
    
    
    public void calcAmp(final Simulation s){
        final int trans = M.min(nTrans, s.transducers.size());
        for (int it = 0; it < trans; it++) {
            final float angle = s.transducers.get(it).getPhase() * M.PI;
            phasesA[it] = M.cos(angle);
            phasesB[it] = M.sin(angle);
        }
        
        for (int in = 0; in < n; in++) {
            for (int im = 0; im < m; im++) {
                amp[in][im] = ta[in][im] = tb[in][im] = 0;
                for (int it = 0; it < trans; it++) {
                    ta[in][im] += ka[it][in][im] * phasesA[it] - kb[it][in][im] * phasesB[it];
                    tb[in][im] += ka[it][in][im] * phasesB[it] + kb[it][in][im] * phasesA[it];
                }
                amp[in][im] = Math.sqrt(ta[in][im]*ta[in][im] + tb[in][im]*tb[in][im]);
            }
        }
    }
    
    public ArrayList<Vector2i> findPeaks(){
        final ArrayList<Vector2i> list = new ArrayList<>();
        
        for (int in = 1; in < n-1; in++) {
            for (int im = 1; im < m-1; im++) {
                final double c = amp[in][im];
                if (c > amp[in-1][im] &&
                        c > amp[in+1][im] &&
                        c > amp[in][im-1] &&
                        c > amp[in][im+1]){
                    list.add( new Vector2i(in, im));
                }
            }
        }
        
        return list;
    }

    public void transform2DTo3D(final Vector2i p2, final Vector3f p3){
        final float nw = sw / n;
        final float nh = sh / m;

        p3.x = sx + p2.x * nw - sw / 2;
        p3.y = sy;
        p3.z = sz + p2.y * nh - sh / 2;
    }
    
    public ArrayList<Vector3f> transformPeaksTo3D(final ArrayList<Vector2i> points){
        final ArrayList<Vector3f> list = new ArrayList<>();
        
        final float nw = sw / n;
        final float nh = sh / m;
        
        for (Vector2i point : points) {
            final float x = sx + point.x * nw - sw/2;
            final float y = sy;
            final float z = sz + point.y * nh - sh/2;
            list.add( new Vector3f(x, y, z) );
        }
        
        return list;
    }
    
    
    public int getnTrans() {
        return nTrans;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    public double[][] getAmp() {
        return amp;
    }
    
    
}
