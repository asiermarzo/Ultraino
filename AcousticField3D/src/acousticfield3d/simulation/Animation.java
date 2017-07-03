/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.simulation;

import acousticfield3d.scene.MeshEntity;
import acousticfield3d.utils.GenericListModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Asier
 */
public class Animation {
    public String name;
    private int number;
    public GenericListModel<AnimKeyFrame> keyFrames;
    public ArrayList<MeshEntity> controlPoints;

    public static Animation createEmpty(final int frames, final List<Transducer> trans){
        final Animation r = new Animation();
        for(int i = 0; i < frames; ++i){
            AnimKeyFrame key = new AnimKeyFrame();
            key.duration = 0;
            r.getKeyFrames().add(key);
        }
        
        for(Transducer t : trans){
            r.addTransducer(t);
        }
        
        return r;
    }
    
    public Animation() {
        name = "no name";
        keyFrames = new GenericListModel<>();
        controlPoints = new ArrayList<>();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public ArrayList<MeshEntity> getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(ArrayList<MeshEntity> controlPoints) {
        this.controlPoints = controlPoints;
    }

    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenericListModel<AnimKeyFrame> getKeyFrames() {
        return keyFrames;
    }

    public void setKeyFrames(GenericListModel<AnimKeyFrame> keyFrames) {
        this.keyFrames = keyFrames;
    }
    
    public void deleteTransducers(List<Transducer> trans){
        for(AnimKeyFrame ak : keyFrames.getElements()){
            ak.deleteTrans(trans);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void addTransducer(Transducer t) {
        for(AnimKeyFrame key : keyFrames.getElements()){
            key.addTrans(t);
        }
    }
    
    public float getDuration(){
        float dur = 0;
        float lastDur = 0;
        for(AnimKeyFrame key : keyFrames.getElements()){
            lastDur = key.duration;
            dur += lastDur;
        }
        dur -= lastDur; //the last keyframe should not be counted
        return dur;
    }
    
    public boolean applyAtTime(float dur, Simulation s){
        float prevStep = 0;
        float nextStep = 0;
        
        int index = 0;
        final int size = keyFrames.getSize();
        for(AnimKeyFrame key : keyFrames.getElements()){
            nextStep += key.duration;
            if (dur >= prevStep && dur <= nextStep && index < size-1){
                float p = (dur-prevStep)/(nextStep-prevStep);
                key.applyInter(s, keyFrames.getAt(index+1), p);
                return true;
            }
            prevStep = nextStep;
            index++;
        }
        return false;
    }
    
    public void applyAtFrame(int frame, Simulation simulation) {
        final int size = keyFrames.getSize();
        if (frame >= 0 && frame < size){
            keyFrames.getAt(frame).apply( simulation );
        }
    }
    
    public void sortKeyFrames(){
        Collections.sort(keyFrames.getElements(), new Comparator<AnimKeyFrame>() {
            @Override
            public int compare(AnimKeyFrame o1, AnimKeyFrame o2) {
                return Integer.compare(o1.number, o2.number);
            }
        });
    }
    
    public String saveAsTable(Simulation s){
        StringBuilder sb = new StringBuilder();
        final List<Transducer> trans = s.getTransducers();
        final List<AnimKeyFrame> keys = getKeyFrames().getElements();
        
        final int nT = trans.size();
        final int nK = keys.size();
        
        for(int ik = 0; ik < nK; ++ik){
            final AnimKeyFrame ak = keys.get(ik);
            for(int it = 0; it < nT; ++it){
                final Transducer t = trans.get(it);
                final TransState ts = ak.getTransStates().get( t );
                float amp = 0.0f;
                float phase = 0.0f;
                if (ts != null){
                    amp = (float)ts.getAmplitude();
                    phase = (float)ts.getPhase();
                }
                sb.append(amp).append(":").append(phase).append("\t");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    
    
    public void loadTable(String content, Simulation s) {
        String[] lines = content.split("\\n");
        String[] lineS = lines[0].split("\\t");
        int nPatterns = lines.length;
        int nTransducers = lineS.length;
       
        int ip = 0;
        for (String l : lines) {
            AnimKeyFrame keyFrame = new AnimKeyFrame();
            keyFrame.setNumber(ip);
            keyFrame.setDuration(1.0f);
            keyFrames.add(keyFrame);

            lineS = l.trim().split("\\t");
            final int nTrans = lineS.length;
            for (int it = 0; it < nTrans; ++it) {
                String[] ampAndPhase = lineS[it].trim().split(":");
                 
                final float amp = Float.parseFloat(ampAndPhase[0]);
                final float phase = Float.parseFloat(ampAndPhase[1]);
                TransState ts = new TransState();
                ts.setAmplitude(amp);
                ts.setPhase(phase);
                ts.setTransducer(s.getTransducers().get(it));
                keyFrame.transStates.put(ts.getTransducer(), ts);
            }
            ip++;
        }

    }

  

    
}
