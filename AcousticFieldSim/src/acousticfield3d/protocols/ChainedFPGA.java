package acousticfield3d.protocols;

import acousticfield3d.simulation.Transducer;
import java.util.List;

/**
 *
 * @author am14010
 */
public class ChainedFPGA extends SimpleFPGA{
    
    @Override
    public void sendPattern(final List<Transducer> transducers) {
        final int nTrans = transducers.size();
        final int nTransPerBoard = getnTransducers();
        final int nBoards = (int) Math.ceil( nTrans / (double)nTransPerBoard );
        final int maxTrans = nBoards * nTransPerBoard;
        final int divs = getDivs();
        final byte PHASE_OFF = (byte) (0xFF & divs);
        
        final byte[] data = new byte[nBoards * (1 + nTransPerBoard)];
        
        //add the control messages to enable the boards before sending the data for it
        for (int board = 0; board < nBoards; board++) {
            data[board * (nTransPerBoard+1)] = (byte) ( 0xFF & (getEnableboardCommand() + board) );
        }
        //add the data from the phases
        for (Transducer t : transducers) {
            final int n = t.getOrderNumber();
            if (n >= 0 && n < maxTrans) { //is it within range
                int phase = t.getDiscPhase(divs);
               
                if (t.getpAmplitude() == 0){
                    phase = PHASE_OFF;
                }
                final int targetBoard = n / nTransPerBoard;      
                data[n + (targetBoard+1)] = (byte) (phase & 0xFF);
            }
        }
        
        
        //send just the data, commit will be done by the Controller
        serial.write(data);
        serial.flush();
    }
    
}
