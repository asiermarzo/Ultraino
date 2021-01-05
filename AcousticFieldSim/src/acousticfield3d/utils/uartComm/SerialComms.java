/**
 * @file SerialComms.java
 */
/**
 * @package com.ultrafaptics.api
 * @brief The package for the UltraFaptics API
 * Created by Tom and Asier : )
 */

package acousticfield3d.utils.uartComm;

import acousticfield3d.utils.DialogUtils;
import java.util.List;

public class SerialComms implements NetworkInterface {

    public interface Listener{
        public void rxMsg(byte[] data, int len);
    }
    
    private final Listener listener;
    private final Network network;

    public static int listAndSelectPortIndex(){
        List<String> ports = Network.getPortList();
        // Get list of available serial ports.
        if (ports.isEmpty()) {
            DialogUtils.showError(null, "No Serial Ports", "No serial ports where found");
            return -1;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ports.size(); ++i) {
            sb.append((i + 1) + " -> " + ports.get(i) + " || ");
        }
        String numberStr = DialogUtils.getStringDialog(null, "Select Port: " + sb.toString(), "1");

        if (numberStr != null) {
            return Integer.parseInt(numberStr);
        }
        return -1;
    } 

    public SerialComms(int portIndex, int speed, Listener listener) {
        network = new Network(0, this);
        this.listener = listener;
        List<String> ports = Network.getPortList();
        
        if (network.connect(ports.get(portIndex - 1), speed)) {
            System.out.println("Connected.");
        } else {
            DialogUtils.showError(null, "Error", "There was an error connecting");
        }
    }
    
    public void disconnect(){
        network.disconnect();
    }

    public void writeByte(int b){
        network.writeByte(b);
    }
    public void writeUShort(int ushort){
        final byte[] message = { (byte) ((ushort >> 8) & 0xFF), (byte) (ushort & 0xFF)};
        network.writeSerial(message, 0, 2);
    }
    
    public void write(final byte message[], final int offset, final int len) {
        network.writeSerial(message, offset, len);
    }
    
     public void write(final byte message[]) {
         network.writeSerial(message, 0, message.length);
     }
     
     public void flush(){
         network.flush();
     }

   
    @Override
    public void networkDisconnected(int id) {
    }

    @Override
    public void parseInput(int id, byte[] message, int numBytes) {
        if (listener != null){
            listener.rxMsg(message, numBytes);
        }
    }
    @Override
    public void writeLog(int id, String text) {
        //System.out.println("   log:  |" + text + "|");
    }
    
    
}
