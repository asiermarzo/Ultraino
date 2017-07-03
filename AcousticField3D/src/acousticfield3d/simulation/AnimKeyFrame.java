/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.simulation;

import acousticfield3d.math.M;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Asier
 */
public class AnimKeyFrame {
    public int number;
    HashMap<Transducer, TransState> transStates;
    public float duration;

    public AnimKeyFrame() {
        transStates = new HashMap<>();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    

    public HashMap<Transducer, TransState> getTransStates() {
        return transStates;
    }

    public void setTransStates(HashMap<Transducer, TransState> transStates) {
        this.transStates = transStates;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return number + " " + duration;
    }
    
    public void deleteTrans(Transducer t){
        transStates.remove(t);
    }
    
    public void deleteTrans(List<Transducer> trans){
        for(Transducer t : trans ){
            transStates.remove(t);
        }
    }
    
    public void snap(Simulation s){
        transStates.clear();
        for(Transducer t : s.transducers){
            TransState ts = new TransState();
            transStates.put(t, ts);
            ts.transducer = t;
            ts.snap();
        }
    }
    
    public void apply(Simulation s){
        for(Transducer t : transStates.keySet()){
            TransState ts = transStates.get(t);
            ts.apply();
        }
    }
    
    public void applyInter(Simulation s, AnimKeyFrame b, float p){
        for(Transducer t : transStates.keySet()){
            TransState tsA = transStates.get(t);
            TransState tsB = b.transStates.get(t);
            if (tsA != null && tsB != null){
                tsA.applyMixed(tsB, p);
            }else if (tsB != null){
                tsA.apply();
            }
            
        }
    }

    void addTrans(Transducer t) {
        TransState ts = new TransState();
        ts.transducer = t;
        ts.snap();
    }
  
    
}
