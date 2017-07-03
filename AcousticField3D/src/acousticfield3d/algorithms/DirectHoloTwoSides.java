/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Asier
 */
public class DirectHoloTwoSides {
    
    public static void calcWithTransducers(final MainForm mf, final Entity target, final int nTrans, final boolean addHoloPI, final boolean useZ){
        final Vector3f pos = target.getTransform().getTranslation();
        
        //sort transducer by distance to he bead (ignoring y)
        final ArrayList<Transducer> trans = new ArrayList<>( mf.getSimulation().getTransducers());
        if(useZ){
            Collections.sort(trans, new Comparator<Transducer>() {
                @Override
                public int compare(Transducer o1, Transducer o2) {
                    final Vector3f p1 = o1.getTransform().getTranslation();
                    final Vector3f p2 = o2.getTransform().getTranslation();
                    final float d1 = M.sqrt(M.sqr(p1.x - pos.x) + M.sqr(p1.z - pos.z) );
                    final float d2 = M.sqrt(M.sqr(p2.x - pos.x) + M.sqr(p2.z - pos.z) );
                    return Float.compare(d1, d2);
                }
            });
        }else{
            Collections.sort(trans, new Comparator<Transducer>() {
                @Override
                public int compare(Transducer o1, Transducer o2) {
                    final Vector3f p1 = o1.getTransform().getTranslation();
                    final Vector3f p2 = o2.getTransform().getTranslation();
                    final float d1 = 2.0f*M.sqrt(M.sqr(p1.x - pos.x) + M.sqr(p1.z - pos.z) );
                    final float d2 = 2.0f*M.sqrt(M.sqr(p2.x - pos.x) + M.sqr(p2.z - pos.z) );
                    return Float.compare(d1, d2);
                }
            });
        }
        
        //get the n closest transducers 
        final ArrayList<Transducer> selTrans = new ArrayList<>();
        for(int i = 0; i < nTrans; ++i){
            selTrans.add( trans.get(i));
        }
        
        //apply the method
        DirectHoloTwoSides.calc(mf, pos, selTrans, addHoloPI);
    }
    
    public static void calc(final MainForm mf, final Vector3f target, final ArrayList<Transducer> transducers, final boolean addHoloPI){
        final float mSpeed = mf.simForm.getMediumSpeed();
        final int n = transducers.size();
        
        //get the middle point of the transducers
        float midY = 0;
        for(Transducer t : transducers){
            midY += t.getTransform().getTranslation().y;
        }
        midY /= n;
        
        if (addHoloPI){
            //set the phase of top transducers to pi and the bottom ones to 0
            for(Transducer t : transducers){
                if (t.getTransform().getTranslation().y > midY){
                    t.setPhase(1.0f);
                }else{
                    t.setPhase(0.0f);
                }
            }
        }else{
            //set the phase to 0, so only focus
            for(Transducer t : transducers){
                t.setPhase(0.0f);
            }
        }
        
        //add the focalizing element
        for(Transducer t : transducers){
            final float distance = target.distance( t.getTransform().getTranslation() );
            final float waveLength = mSpeed / t.getFrequency();
            final float targetPhase = (1.0f - M.decPart(distance / waveLength) ) * 2.0f * M.PI;
            t.setPhase(t.getPhase() + targetPhase / M.PI );
        }
    }
}
