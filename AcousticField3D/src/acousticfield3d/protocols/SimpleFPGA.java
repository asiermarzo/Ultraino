/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.protocols;

import acousticfield3d.math.M;
import acousticfield3d.simulation.Transducer;
import java.util.List;

/**
 *
 * @author am14010
 */
public class SimpleFPGA extends DeviceConnection{
    @Override
    public int getDivs() {
        return 32;
    }

    @Override
    public int getSpeed() {
        return 250000;
    } 
    
    @Override
    public void sendPattern(final List<Transducer> transducers) {
       if(serial == null){
            return;
        }
       
       final int nTrans = M.nearestPowerOfTwo( transducers.size() );
       final byte[] data = new byte[nTrans + 2];
       final int divs = getDivs();
       
        data[0] = (byte) (0xFF & 255);
        for (Transducer t : transducers) {
            final int n = t.getOrderNumber();
            if (n >= 0 && n < nTrans) { //is it within range
                int phase = t.getDiscPhase(divs);

                if (t.getpAmplitude() == 0){
                    phase = 32;
                }
                        
                data[n+1] = (byte) (phase & 0xFF);
            }
        }
       data[nTrans + 1] = (byte) (0xFF & 254);
       serial.write(data);
       serial.flush();
    }
}
