package acousticfield3d.protocols;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.simulation.AnimKeyFrame;
import acousticfield3d.simulation.TransState;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.ArrayUtils;
import acousticfield3d.utils.TextFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author am14010
 */
public class ArduinoMEGA64 extends ArduinoNano{
    final static byte COMMAND_COMMIT_DURATIONS = 0x10;
    
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
    
    
    public byte[] calcDataBytes(final AnimKeyFrame key) {
        //TODO refactor this shameful repetition of code
        final int nTrans = Transducer.getMaxPin(key.getTransAmplitudes().keySet()) + 1;
        final int signalsPerBoard = getSignalsPerBoard();
        final int nBoards = (nTrans - 1) / signalsPerBoard + 1;
        assert (nBoards < 15);

        final int nDivs = getDivs();
        final int nPorts = getNPorts();

        final int bytesPerBoard = nDivs * nPorts;
        byte[] data = new byte[nBoards * bytesPerBoard];
        for ( Transducer t : key.getTransAmplitudes().keySet() ) {
            final int n = t.getDriverPinNumber();
            if (n >= 0) {
                final float amplitude = key.getTransAmplitudes().get(t);
                final float fphase = key.getTransPhases().get(t);
                
                        
                final int board = n / signalsPerBoard;
                final int softwarePin = n % signalsPerBoard;

                final int hardwarePin = PORT_MAPPING[softwarePin];
                final int phaseCompensation = PHASE_COMPENSATION[softwarePin];

                final int targetByte = hardwarePin / 8;
                final byte value = (byte) ((1 << (hardwarePin % 8)) & 0xFF);
                final int phase = Transducer.calcDiscPhase(fphase + t.getPhaseCorrection(), nDivs);

                //TODO the divs to amplitude is not going to be linear but it will do for the moment
                final int ampDivs = M.iclamp(Math.round(amplitude * nDivs / 2), 0, nDivs);

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
            
            final int nTrans = Transducer.getMaxPin(k.getTransAmplitudes().keySet()) + 1;
            final int signalsPerBoard = getSignalsPerBoard();
            final int nBoards = (nTrans - 1) / signalsPerBoard + 1;
            assert (nBoards < 15);

            final int nDivs = getDivs();
            final int nPorts = getNPorts();

            final int bytesPerBoard = nDivs * nPorts;

            byte[] data = calcDataBytes(k);

            for (int i = 0; i < nBoards; ++i) {
                for (int j = 0; j < bytesPerBoard; ++j) {
                    int dataIndex = i * bytesPerBoard + j;
                    serial.writeByte((data[dataIndex] & 0xF0) | (i + 1));
                    serial.writeByte(((data[dataIndex] << 4) & 0xF0) | (i + 1));
                }
            }

        }
        
        
        //send durations
        durations.add(0); //stop frame
        sendDurations( ArrayUtils.toArray(durations) );
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
       
       sendDurations( new int[]{1,0} );
    }
    
    
    @Override
    public void sendDurations(final int[] durations){
        final int n = durations.length;
        final int COMMAND = (byte)0x30; //XX110000
         
        //(bReceived & 0x11000000) >> (durationsWrittingIndex % 4 * 2)
        
        for(int i = 0; i < n; ++i){
            final int d = durations[i];
            serial.writeByte( COMMAND | ((d << 0) & 0xC0) );
            serial.writeByte( COMMAND | ((d << 2) & 0xC0) );
            serial.writeByte( COMMAND | ((d << 4) & 0xC0) );
            serial.writeByte( COMMAND | ((d << 6) & 0xC0) );
        }
        
        //commit durations
        serial.writeByte( COMMAND_COMMIT_DURATIONS );
        
        serial.flush();
    }
    
    //sorry, repeated code everywhere
    public static void exportAnimation(MainForm mf) {
        final List<AnimKeyFrame> frames = mf.animPanel.getCurrentAnimation().getKeyFrames().getElements();

        ArduinoMEGA64 ins = new ArduinoMEGA64();

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
    
    
    public static final int[] PORT_MAPPING = {51, 52, 53, 54, 28, 29, 30, 31, 47, 46, 45, 44, 43, 42, 41, 40, 56, 57, 58, 59, 48, 49, 72, 69, 39, 38, 37, 36, 35, 34, 33, 32, 21, 23, 65, 63, 9, 11, 13, 15, 20, 22, 64, 66, 8, 10, 12, 14, 24, 25, 26, 27, 16, 17, 18, 19, 7, 6, 5, 4, 3, 2, 1, 0};
    //public static final int[] PHASE_COMPENSATION = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    public static final int[] PHASE_COMPENSATION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
}
