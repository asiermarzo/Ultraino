package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.simulation.Transducer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author am14010
 */
public class DivTransFocus {
    public final static int METHOD_CHECKER = 0;
    public final static int METHOD_LINE = 1;
    public final static int METHOD_RANDOM = 2;
    public final static int METHOD_CLOSEST = 3;
    
    public static void calcMultiFocus(final MainForm mf, 
            final List<? extends Transducer> transducers, 
            final List<? extends Entity> controlPoints,
            final int method){
       
       final int nPoints = controlPoints.size();
       final int nTrans = transducers.size();
       
       final ArrayList<Transducer> orderedTrans = new ArrayList<>(transducers);
       final ArrayList<ArrayList<Transducer>> subsets = new ArrayList<>(nPoints);
      for(int i = 0 ; i< nPoints; ++i){
          subsets.add( new ArrayList<Transducer>() );
      }
       
       orderedTrans.sort(new Comparator<Transducer>() {
            @Override
            public int compare(Transducer o1, Transducer o2) {
                final int aX = Math.round( o1.getTransform().getTranslation().x * 1000 );
                final int aZ = Math.round( o1.getTransform().getTranslation().z * 1000 );
                final int bX = Math.round( o2.getTransform().getTranslation().x * 1000 );
                final int bZ = Math.round( o2.getTransform().getTranslation().z * 1000 );
                
                final int compareX = Integer.compare(aX, bX);
                if (compareX == 0){
                    return Integer.compare(aZ, bZ);
                }else{
                    return compareX;
                }
            }
        });
       
       if (method == METHOD_CHECKER){
           subDivideChecker(subsets, orderedTrans);
       }else if (method == METHOD_LINE){
           subDivideLine(subsets, orderedTrans);
       }else if (method == METHOD_RANDOM){
           subDivideRandom(subsets, orderedTrans);
       }else if (method == METHOD_CLOSEST){
           subDivideClosest(subsets, orderedTrans,controlPoints);
       }

       focus(subsets,controlPoints, mf.simulation.getMediumSpeed());
    }
    
    private static void subDivideChecker(final ArrayList<ArrayList<Transducer>> subsets, final ArrayList<Transducer> trans){
        final int nSets = subsets.size();
        final int nTrans = trans.size();
        final int sqrt = Math.round( M.sqrt( nTrans ));
        int index = 0;
        int cset = 0;
        for (Transducer t : trans){
            subsets.get(cset % nSets).add( t );
            ++index;
            ++cset;
            if (index % sqrt == 0){
                ++cset;
            }
        }
    }
    
    private static void subDivideLine(final ArrayList<ArrayList<Transducer>> subsets, final ArrayList<Transducer> trans){
        final int nSets = subsets.size();
        int index = 0;
        for (Transducer t : trans){
            subsets.get(index % nSets).add( t );
            ++index;
        }
    }
    
    private static void subDivideRandom(final ArrayList<ArrayList<Transducer>> subsets, final ArrayList<Transducer> trans){
        final int nSets = subsets.size();
        int index = 0;
        
       while(! trans.isEmpty()){
           final int r = M.randomInt(0, trans.size());
           final Transducer t = trans.get(r);
           trans.remove(r);
            subsets.get(index % nSets).add( t );
            ++index;
       }
    }
    
    private static void subDivideClosest(final ArrayList<ArrayList<Transducer>> subsets, final ArrayList<Transducer> trans, final List<? extends Entity> controlPoints){
       final int nSets = subsets.size();
       int index = 0;
        
       while(! trans.isEmpty()){
           final int cSet = index % nSets;
           final Vector3f pos = controlPoints.get(cSet).getTransform().getTranslation();
           float minDist = Float.MAX_VALUE;
           int r = -1;
           final int nTrans = trans.size();
           for(int i = 0; i < nTrans; ++i){
               final float cDist = pos.distance( trans.get(i).getTransform().getTranslation());
               if (cDist < minDist){
                   r = i;
                   minDist = cDist;
               }
           }
           
           final Transducer t = trans.get(r);
           trans.remove(r);
           subsets.get( cSet ).add( t );
           ++index;
       }
        
    }
    
 
    private static void focus(final ArrayList<ArrayList<Transducer>> subsets, final List<? extends Entity> controlPoints, final float mSpeed){
        final int n = M.min(subsets.size(), controlPoints.size());
        for (int i = 0; i < n; ++i){
            SimplePhaseAlgorithms.focus(subsets.get(i), controlPoints.get(i).getTransform().getTranslation(), mSpeed);
        }
        
    }
}
