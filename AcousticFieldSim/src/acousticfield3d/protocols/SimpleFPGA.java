package acousticfield3d.protocols;

import acousticfield3d.simulation.Transducer;
import java.util.List;

/**
 *
 * @author am14010
 */
public class SimpleFPGA extends DeviceConnection{
          
    public byte getStartPhasesCommand(){
        return (byte) (0xFF & 254);
    }
    
    public byte getSwapCommand(){
        return (byte) (0xFF & 253);
    }
    
    public byte getMultiplexCommand(){
        return (byte) (0xFF & 252);
    }
    
    public byte getStartAmplitudesCommand(){
        return (byte) (0xFF & 251);
    }
    
    public byte getOnClocksCommand(){
        return (byte) (0xFF & 151);
    }
    
    public byte getOffClocksCommand(){
        return (byte) (0xFF & 150);
    }
    
    public byte getEnableboardCommand(){
        return (byte) (0xFF & 192); // 192 enables all boards, +1 for board 1, +2 for board 2
    }
    
    public int getnTransducers(){
        return 256;
    }
    
    @Override
    public int getDivs() {
        return 32;
    }

    @Override
    public int getSpeed() {
        //return 2000000;
        //115200
        //230400 -- FASTEST POSSIBLE FOR MACOS (java limitation rxtx)
        //250000
        return 200000;
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
       final byte[] phaseDataPlusHeader = new byte[nTrans + 1];
       final byte[] ampDataPlusHeader = new byte[nTrans + 1];
       final int divs = getDivs();
      
       final byte PHASE_OFF = (byte) (0xFF & getDivs());
       //Arrays.fill(data, PHASE_OFF);
       boolean ampModulationNeeded = false;
       
        phaseDataPlusHeader[0] = getStartPhasesCommand(); 
        ampDataPlusHeader[0] = getStartAmplitudesCommand();
        
        for (Transducer t : transducers) {
            final int n = t.getOrderNumber() - number;
            //final int n = t.getDriverPinNumber();
            if (n >= 0 && n < nTrans) { //is it within range
                int phase = t.getDiscPhase(divs);
                int amplitude = t.getDiscAmplitude(divs); 
                
                if (t.getpAmplitude() == 0){
                    phase = PHASE_OFF;
                }else if (t.getAmplitude() < 0.99f){
                    ampModulationNeeded = true;
                }
                        
                phaseDataPlusHeader[n+1] = (byte) (phase & 0xFF);
                ampDataPlusHeader[n+1] = (byte) (amplitude & 0xFF);
            }
        }
       serial.write(phaseDataPlusHeader);
       if ( ampModulationNeeded ){
           serial.write(ampDataPlusHeader);
       }
       serial.flush();
    }
    
    
    @Override
    public void sendToogleQuickMultiplexMode(){
       serial.writeByte(getMultiplexCommand());
       serial.flush();
    }
}
