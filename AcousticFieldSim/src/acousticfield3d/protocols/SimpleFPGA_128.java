package acousticfield3d.protocols;

import acousticfield3d.gui.MainForm;
import acousticfield3d.simulation.AnimKeyFrame;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.TextFrame;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author am14010
 */
public class SimpleFPGA_128 extends SimpleFPGA{
    @Override
    public int getnTransducers(){
        return 64 + 8;
    }
    
    @Override
    public int getDivs() {
        return 128;
    }

    @Override
    public int getSpeed() {
        return 1500000;
    } 
    
    public static void exportAnimationMatlab(final MainForm mf) {
        final SimpleFPGA_128 ins = new SimpleFPGA_128();
        final int divs = ins.getDivs();
        final int nTrans = ins.getnTransducers();
        final int phaseOff = divs;
        
        final List<AnimKeyFrame> frames = mf.animPanel.getCurrentAnimation().getKeyFrames().getElements();

        final StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        //send patterns
        final int[] phases =  new int[nTrans];
        for (AnimKeyFrame k : frames) {
            Arrays.fill(phases, phaseOff);
            for(Transducer t : k.getTransAmplitudes().keySet()){
                final int n = t.getOrderNumber();
                final float amp = k.getTransAmplitudes().get(t);
                final float phase = k.getTransPhases().get(t);
                int iPhase = phaseOff;
                if (n >= 0 && n < nTrans){
                    if (amp > 0.0f){
                        iPhase = Transducer.calcDiscPhase(phase, divs);
                    }
                    phases[n] = iPhase;
                }
            }
            for(int i = 0; i < nTrans; ++i){
                sb.append( phases[i] + " ");
            }
            sb.setLength( sb.length() - 1);
            sb.append(";");
        }
        sb.setLength( sb.length() - 1);
        sb.append("]\n"); // switch
        
        TextFrame.showText("animation matlab export", sb.toString(), mf);
    }
  
}
