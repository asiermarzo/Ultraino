/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.protocols;

import acousticfield3d.simulation.Transducer;
import java.util.List;

/**
 *
 * @author am14010
 */
public class SimpleFPGA extends DeviceConnection{
          
    public byte getStartPhasesCommand(){
        return (byte) (0xFF & 255);
    }
    
    public byte getSwapCommand(){
        return (byte) (0xFF & 254);
    }
    
    public byte getMultiplexCommand(){
        return (byte) (0xFF & 253);
    }
    
    public int getnTransducers(){
        return 64;
    }
    
    @Override
    public int getDivs() {
        return 32;
    }

    @Override
    public int getSpeed() {
        return 250000;
    } 

    @Override
    public void switchBuffers() {
       serial.writeByte( getSwapCommand() );
       serial.flush();
    }
    
    
    @Override
    public void sendPattern(final List<Transducer> transducers) {
       if(serial == null){
            return;
        }
       
       final int nTrans = getnTransducers();
       //final int nTrans = M.nearestPowerOfTwo( transducers.size() );
       final byte[] data = new byte[nTrans + 1];
       //Arrays.fill(data, PHASE_OFF);
       final int divs = getDivs();
      
       final byte PHASE_OFF = (byte) (0xFF & getDivs());
       
        data[0] = getStartPhasesCommand(); 
        for (Transducer t : transducers) {
            final int n = t.getOrderNumber() - number;
            //final int n = t.getDriverPinNumber();
            if (n >= 0 && n < nTrans) { //is it within range
                int phase = t.getDiscPhase(divs);

                if (t.getpAmplitude() == 0){
                    phase = PHASE_OFF;
                }
                        
                data[n+1] = (byte) (phase & 0xFF);
            }
        }
       serial.write(data);
       serial.flush();
    }
    
    @Override
    public void sendToogleQuickMultiplexMode(){
       serial.writeByte(getMultiplexCommand());
       serial.flush();
    }
}
