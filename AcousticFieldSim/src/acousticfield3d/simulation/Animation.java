package acousticfield3d.simulation;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.scene.MeshEntity;
import acousticfield3d.utils.GenericListModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Asier
 */
public class Animation {
    public String name;
    private int number;
    public GenericListModel<AnimKeyFrame> keyFrames;

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
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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
    
    /*
    public boolean applyAtTime(float dur){
        float prevStep = 0;
        float nextStep = 0;
        
        int index = 0;
        final int size = keyFrames.getSize();
        for(AnimKeyFrame key : keyFrames.getElements()){
            nextStep += key.duration;
            if (dur >= prevStep && dur <= nextStep && index < size-1){
                float p = (dur-prevStep)/(nextStep-prevStep);
                key.applyInter(keyFrames.getAt(index+1), p);
                return true;
            }
            prevStep = nextStep;
            index++;
        }
        return false;
    }
    */
    
    public void applyAtFrame(int frame, Simulation simulation) {
        final int size = keyFrames.getSize();
        if (frame >= 0 && frame < size){
            keyFrames.getAt(frame).apply();
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

    public AnimKeyFrame lastKeyFrame() {
        final ArrayList<AnimKeyFrame> keys = keyFrames.getElements();
        
        if (keys.isEmpty()){
            return null;
        }
        return keys.get( keys.size() - 1);
    }
   
    public byte[] exportRaw(final Simulation s) throws IOException{
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos =  new DataOutputStream( bos );
        
        final ArrayList<Transducer> trans = s.getTransducers();
        final ArrayList<MeshEntity> points = s.getControlPoints();
        
        final ArrayList<AnimKeyFrame> keys = keyFrames.getElements();
        if (keys.isEmpty() || keys.get(0).getTransAmplitudes().isEmpty()){
            return null;
        }
        
        final int nKeys = keys.size();
        final int nTrans = keys.get(0).getTransAmplitudes().size();
        final int nPoints = keys.get(0).getPointsPositions().size();
        
        dos.writeInt( nKeys );
        dos.writeInt( nTrans );
        dos.writeInt( nPoints );
        
        for(AnimKeyFrame key : keys ){
            for(Transducer t : trans){
                dos.writeFloat( key.getTransAmplitudes().get(t) );
                dos.writeFloat( key.getTransPhases().get(t) );
            }
            for(Entity cp : points){
                final Vector3f pos = key.getPointsPositions().get(cp);
                if(pos == null){
                    dos.writeFloat(0); dos.writeFloat(0); dos.writeFloat(0);
                }else{
                    dos.writeFloat(pos.x); dos.writeFloat(pos.y); dos.writeFloat(pos.z);
                }
            }
        }
        
        return bos.toByteArray();
    }
    
    
    public void importRaw(final byte[] data, final MainForm mf) throws IOException{
        final Simulation s = mf.simulation;
        final DataInputStream dis =  new DataInputStream(new ByteArrayInputStream(data));
       
        final ArrayList<Transducer> trans = s.getTransducers();
        final ArrayList<MeshEntity> points = s.getControlPoints();
       
        final int nKeys = dis.readInt();
        final int nTrans = dis.readInt();
        final int nPoints = dis.readInt();

        //ensure that the simulation has the same amount of points
        mf.pointsPanel.deleteAllPoints();
        mf.pointsPanel.createPoints(nPoints);
        
        int index = 0;
        for(int i = 0; i < nKeys; ++i){
            final AnimKeyFrame key = new AnimKeyFrame();
            key.setDuration(1);
            key.setNumber(index++);
            for(int j = 0; j < nTrans; ++j){
                final Transducer t = trans.get(j);
                final float amp = dis.readFloat();
                final float phase = dis.readFloat();
                key.getTransAmplitudes().put(t, amp);
                key.getTransPhases().put(t, phase);
            }
            
            for(int j = 0; j < nPoints; ++j){
                final Entity cp = points.get(j);
                final float x = dis.readFloat();
                final float y = dis.readFloat();
                final float z = dis.readFloat();
                key.getPointsPositions().put(cp, new Vector3f(x, y, z));
            }
            keyFrames.add(key);
        }       
    }

    public Animation createCopy() {
        final Animation copy = new Animation();
        copy.setName( getName() );
        for (AnimKeyFrame key : keyFrames.getElements()){
            copy.keyFrames.add( key.createCopy() );
        }
        return copy;
    }

    public void interpolate() {
        final ArrayList<AnimKeyFrame> keys = keyFrames.getElements();
        final int n = keys.size();
        if (n <= 1) { return; }
        
        final ArrayList<AnimKeyFrame> keysCopy = new ArrayList<>( keys );
        keys.clear();
        
        for (int i = 0; i < n-1; ++i){
            final AnimKeyFrame current = keysCopy.get(i);
            final AnimKeyFrame next = keysCopy.get(i+1);
            keys.add( current );
            final AnimKeyFrame interp = current.createCopy();
            interp.interpolate(next, 0.5f);
            keys.add(interp);
        }
        keys.add( keysCopy.get(n-1) );
        keyFrames.updateAll();
    }
    
}
