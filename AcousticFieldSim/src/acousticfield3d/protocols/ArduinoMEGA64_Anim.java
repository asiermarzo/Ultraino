package acousticfield3d.protocols;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.simulation.AnimKeyFrame;
import acousticfield3d.simulation.TransState;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.TextFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author am14010
 */
public class ArduinoMEGA64_Anim extends ArduinoNano{
 
    final static byte COMMAND_DURATION = (byte)0x30; //XX110000
    
    @Override
    public int getDivs() {
        return 10;
    }

    @Override
    public int getSpeed() {
        return 115200;
    }
    
    public int getNPorts() {
        return 10;
    }
    
    public int getSignalsPerBoard(){
        return 64;
    }
    
    public byte[] calcDataBytes(final AnimKeyFrame key){
        return calcDataBytes(key, getSignalsPerBoard(), getDivs(), getNPorts());
    }
            
    public static byte[] calcDataBytes(final AnimKeyFrame key, final int signalsPerBoard, final int divs, final int ports) {
        //TODO refactor this shameful repetition of code
        final int nTrans = Transducer.getMaxPin(key.getTransAmplitudes().keySet()) + 1;
        final int nSignalsPerBoard = signalsPerBoard;
        final int nBoards = (nTrans - 1) / nSignalsPerBoard + 1;
        assert (nBoards < 15);

        final int nDivs = divs;
        final int nPorts = ports;

        final int bytesPerBoard = nDivs * nPorts;
        byte[] data = new byte[nBoards * bytesPerBoard];
        for (Transducer t : key.getTransAmplitudes().keySet()) {
            final int n = t.getDriverPinNumber();
            if (n >= 0) {
               final float fphase = key.getTransPhases().get(t);
               final float famp = key.getTransAmplitudes().get(t);
               
                final int board = n / nSignalsPerBoard;
                final int softwarePin = n % nSignalsPerBoard;

                final int hardwarePin = PORT_MAPPING[softwarePin];
                final int phaseCompensation = PHASE_COMPENSATION[softwarePin];

                final int targetByte = hardwarePin / 8;
                final byte value = (byte) ((1 << (hardwarePin % 8)) & 0xFF);
                final int phase = Transducer.calcDiscPhase(fphase + t.getPhaseCorrection(), nDivs);

                //TODO the divs to amplitude is not going to be linear but it will do for the moment
                final int ampDivs = M.iclamp(Math.round(famp * nDivs / 2), 0, nDivs);

                for (int i = 0; i < ampDivs; ++i) {
                    final int d = (i + phase + phaseCompensation) % nDivs;
                    data[board * bytesPerBoard + targetByte + d * nDivs] |= value;
                }
            }
        }
        
        return data;
    }
    
    @Override
    public void sendAnim(final List<AnimKeyFrame> keyFrames) {
        if(serial == null){
            return;
        }
        
        //send patterns
        final List<AnimKeyFrame> frames = keyFrames;
        final ArrayList<Integer> durations = new ArrayList<>();
 
        for (AnimKeyFrame k : frames) {
            final float duration = k.getDuration();
            //if (duration > 0.0f){
            durations.add((int) duration);

            final AnimKeyFrame akf = k;
          
            final int nTrans = Transducer.getMaxPin(akf.getTransAmplitudes().keySet()) + 1;
            final int signalsPerBoard = getSignalsPerBoard();
            final int nBoards = (nTrans - 1) / signalsPerBoard + 1;
            assert (nBoards < 15);

            final int nDivs = getDivs();
            final int nPorts = getNPorts();

            final int bytesPerBoard = nDivs * nPorts;

            byte[] data = calcDataBytes(akf);

            for (int i = 0; i < nBoards; ++i) {
                for (int j = 0; j < bytesPerBoard; ++j) {
                    int dataIndex = i * bytesPerBoard + j;
                    serial.writeByte((data[dataIndex] & 0xF0) | (i + 1));
                    serial.writeByte(((data[dataIndex] << 4) & 0xF0) | (i + 1));
                }
            }

        }
        
    }
    
    @Override
    public void sendPattern(final List<Transducer> transducers) {
       if(serial == null){
            return;
        }
      
       final int nTrans = Transducer.getMaxPin(transducers) + 1;
       final int signalsPerBoard = getSignalsPerBoard();
       final int nBoards = (nTrans-1) / signalsPerBoard + 1;
       if (nBoards >= 15){
           //TODO log error
           return;
       }
       
       final int nDivs = getDivs();
       final int nPorts = getNPorts();
       
       final int bytesPerBoard = nDivs * nPorts;
       byte[] data = new byte[nBoards * bytesPerBoard];
       for(Transducer t : transducers){
           final int n = t.getDriverPinNumber();
           if(n >= 0){
               final int board = n / signalsPerBoard;
               final int softwarePin = n % signalsPerBoard;
               
               final int hardwarePin = PORT_MAPPING[softwarePin];
               final int phaseCompensation = PHASE_COMPENSATION[softwarePin];
               
               final int targetByte = hardwarePin / 8;
               final byte value = (byte)((1 << (hardwarePin % 8)) & 0xFF);
               final int phase = Transducer.calcDiscPhase(t.getPhase() + t.getPhaseCorrection(), nDivs);
               //TODO the divs to amplitude is not going to be linear but it will do for the moment
               final int ampDivs = M.iclamp(Math.round(t.getpAmplitude() * nDivs / 2), 0, nDivs);
               
               for (int i = 0; i < ampDivs; ++i) {
                   final int d = (i + phase + phaseCompensation) % nDivs;
                   data[board*bytesPerBoard + targetByte + d * nDivs] |= value;
               }
           }
       }
       for(int i = 0; i < nBoards; ++i){
           for(int j = 0; j < bytesPerBoard; ++j){
               int dataIndex = i*bytesPerBoard + j;
                serial.writeByte( (data[dataIndex] & 0xF0) | (i+1) );
                serial.writeByte( ((data[dataIndex] << 4) & 0xF0) | (i+1)  );
           }
       }
    }
    
    
    @Override
    public void sendDurations(final int[] durations){
        final int n = durations.length;
        
         
        //(bReceived & 0x11000000) >> (durationsWrittingIndex % 4 * 2)
        
        for(int i = 0; i < n; ++i){
            final int d = durations[i];
            serial.writeByte( COMMAND_DURATION | ((d << 0) & 0xC0) );
            serial.writeByte( COMMAND_DURATION | ((d << 2) & 0xC0) );
            serial.writeByte( COMMAND_DURATION | ((d << 4) & 0xC0) );
            serial.writeByte( COMMAND_DURATION | ((d << 6) & 0xC0) );
        }
        
        //commit durations
        serial.writeByte( 0x10);
        
        serial.flush();
    }
    
    //sorry, repeated code everywhere
    public static void exportAnimation(MainForm mf) {
        final List<AnimKeyFrame> frames = mf.animPanel.getCurrentAnimation().getKeyFrames().getElements();

        ArduinoMEGA64_Anim ins = new ArduinoMEGA64_Anim();

        final StringBuilder sb = new StringBuilder();
        sb.append("{");

        final int nKeys = frames.size();
        int ik = 0;
        for (AnimKeyFrame k : frames) {
            final byte[] data = ins.calcDataBytes( k );
            final int l = data.length;
            for (int in = 0; in < l; ++in) {
                sb.append("0x" + Integer.toHexString(data[in] & 0xFF));
                if (in != l - 1) {
                    sb.append(",");
                }

            }
            if (ik != nKeys - 1) {
                sb.append("},\n");
            } else {
                sb.append("}}");
            }
            ++ik;
        }

        TextFrame.showText("Animation Data", sb.toString(), mf);
    }
     
    // even more repetition now
    public static void exportAnimationMatlab(MainForm mf) {
        final List<AnimKeyFrame> frames = mf.animPanel.getCurrentAnimation().getKeyFrames().getElements();

        ArduinoMEGA64_Anim ins = new ArduinoMEGA64_Anim();

        final StringBuilder sb = new StringBuilder();
        sb.append("[");

        //send patterns
        for (AnimKeyFrame k : frames) {
          
            final int nTrans = Transducer.getMaxPin( k.getTransAmplitudes().keySet()) + 1;
            final int signalsPerBoard = ins.getSignalsPerBoard();
            final int nBoards = (nTrans - 1) / signalsPerBoard + 1;
            assert (nBoards < 15);
            final int nDivs = ins.getDivs();
            final int nPorts = ins.getNPorts();
            final int bytesPerBoard = nDivs * nPorts;

            byte[] data = ins.calcDataBytes(k);

            for (int i = 0; i < nBoards; ++i) {
                for (int j = 0; j < bytesPerBoard; ++j) {
                    int dataIndex = i * bytesPerBoard + j;
                    sb.append( (data[dataIndex] & 0xF0) | (i + 1) );
                    sb.append(" ");
                    sb.append( ((data[dataIndex] << 4) & 0xF0) | (i + 1) );
                    sb.append(" ");
                }
            }
        }
        sb.append("0]\n"); // switch
        
        //frame set
        sb.append("[");
        for(int d = 0; d < 32; ++d){
            sb.append( COMMAND_DURATION | ((d << 0) & 0xC0) ); sb.append(" ");
            sb.append( COMMAND_DURATION | ((d << 2) & 0xC0) ); sb.append(" ");
            sb.append( COMMAND_DURATION | ((d << 4) & 0xC0) ); sb.append(" ");
            sb.append( COMMAND_DURATION | ((d << 6) & 0xC0) ); sb.append(" ");
            sb.append(";");
        }
        sb.append("]");
        TextFrame.showText("Matlab Data", sb.toString(), mf);
    }
    
    public static final int[] PORT_MAPPING = {51, 52, 53, 54, 28, 29, 30, 31, 47, 46, 45, 44, 43, 42, 41, 40, 56, 57, 58, 59, 48, 49, 72, 69, 39, 38, 37, 36, 35, 34, 33, 32, 21, 23, 65, 63, 9, 11, 13, 15, 20, 22, 64, 66, 8, 10, 12, 14, 24, 25, 26, 27, 16, 17, 18, 19, 7, 6, 5, 4, 3, 2, 1, 0};
    //public static final int[] PHASE_COMPENSATION = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    public static final int[] PHASE_COMPENSATION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
}
