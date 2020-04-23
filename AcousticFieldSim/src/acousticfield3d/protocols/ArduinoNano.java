package acousticfield3d.protocols;

import acousticfield3d.gui.MainForm;
import acousticfield3d.simulation.AnimKeyFrame;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.TextFrame;
import java.util.List;

/**
 *
 * @author am14010
 */
public class ArduinoNano extends DeviceConnection{

    
    
    @Override
    public int getDivs() {
        return 24;
    }

    @Override
    public int getSpeed() {
        return 115200;
    }
    
    public int getTransducers(){
        return 8;
    }
    
    @Override
    public void sendPattern(final List<Transducer> transducers) {
       if(serial == null){
            return;
        }
       
        final byte[] data = calcSignals01( getTransducers(), transducers);
        final int size = data.length;
        //send data
        for(int i = 0; i < size; ++i){
            serial.writeByte( (data[i] & 0xF0) | 0x1 );
            serial.writeByte( ((data[i] << 4) & 0xF0) | 0x1  );
        }
    }

    @Override
    public void switchBuffers() {
        if (serial == null){
            return;
        }
        serial.writeByte( 0x00 );
        serial.flush();
    }
    
    public static void exportAnimation(final MainForm mf) {
        final List<AnimKeyFrame> frames = mf.animPanel.getCurrentAnimation().getKeyFrames().getElements();

       
        final StringBuilder sb = new StringBuilder();
        sb.append("{");

        final int nKeys = frames.size();
        int ik = 0;
        for (AnimKeyFrame k : frames) {
            sb.append("{");
            final byte[] data = calcSignals01AnimFrame(8, k, 24);
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
    
}
