package acousticfield3d.protocols;

import acousticfield3d.simulation.AnimKeyFrame;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.uartComm.SerialComms;
import java.util.List;

/**
 *
 * @author am14010
 */
public class DeviceConnection implements SerialComms.Listener{
    protected int number;
    protected SerialComms serial;

    public DeviceConnection(){
        this.number = 0;
    }
    
    public void connect(int port){
        disconnect();
        serial = new SerialComms(port, getSpeed(), this);
    }
    
    public int getSpeed(){
        return 115200;
    }
    
    public int getDivs(){
        return 20;
    }
    
    public void disconnect(){
        if (serial != null) {
            try {
                serial.disconnect();
            } catch (Exception e) {
            }
            serial = null;
        }
    }
	
    
    public void setFrequency(float frequency){}
    public void switchBuffers(){}
    
    public void sendDurations(final int[] durations){}
    
    public void sendPattern(final List<Transducer> transducers){}
    
    public void sendAnim(final List<AnimKeyFrame> keyFrames){}
  
    public void sendToogleQuickMultiplexMode(){}
    
    public byte[] calcSignals01(final int nTrans, final List<Transducer> transducers){
        return calcSignals01(nTrans, transducers, getDivs());
    }
    
    public static byte[] calcSignals01(final int nTrans, final List<Transducer> transducers, final int divs){
        final int transPerByte = 8;
        final int divsHalf = divs / 2;
        final int nBytes = nTrans * divs / transPerByte;
        final int bytesPerDiv = nTrans / transPerByte;
        
        final byte[] data = new byte[nBytes];
        
        for(Transducer t : transducers){
            final int n = t.getDriverPinNumber();
            if (t.getAmplitude() > 0.0f){
                if (n >= 0 && n < nTrans){ //is it within range
                    final int phase = t.getDiscPhase(divs);
                    int targetByte = n / transPerByte;

                    final int value = 1 << (n - targetByte * transPerByte);
                   
                    for (int i = 0; i < divsHalf; ++i) {
                        final int d = (i + phase) % divs;
                        data[ targetByte + d*bytesPerDiv ] |= value;
                    }
                    
                }
                
            }
        }
        
        return data;
    }
    
    public static byte[] calcSignals01AnimFrame(final int nTrans, final AnimKeyFrame key, final int divs){
        final int transPerByte = 8;
        final int divsHalf = divs / 2;
        final int nBytes = nTrans * divs / transPerByte;
        final int bytesPerDiv = nTrans / transPerByte;
        
        final byte[] data = new byte[nBytes];
        
        for(Transducer t : key.getTransAmplitudes().keySet() ){
            final int n = t.getDriverPinNumber();
            
            if (key.getTransAmplitudes().get(t) > 0.0f){
                if (n >= 0 && n < nTrans){ //is it within range
                    final int phase = Transducer.calcDiscPhase( key.getTransPhases().get(t) , divs);
                    int targetByte = n / transPerByte;

                    final int value = 1 << (n - targetByte * transPerByte);
                   
                    for (int i = 0; i < divsHalf; ++i) {
                        final int d = (i + phase) % divs;
                        data[ targetByte + d*bytesPerDiv ] |= value;
                    }
                    
                }
                
            }
        }
        
        return data;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    
    @Override
    public void rxMsg(byte[] data, int len) {
  
    }
    
}
