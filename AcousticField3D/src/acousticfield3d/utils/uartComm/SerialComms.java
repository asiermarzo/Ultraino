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


    public SerialComms(int port, int speed, Listener listener) {
        network = new Network(0, this);
        List<String> ports = network.getPortList();
        this.listener = listener;
        
        if (port == -1){
            // Get list of available serial ports.
            if (ports.isEmpty()) {
                DialogUtils.showError(null, "No Serial Ports", "No serial ports where found");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ports.size(); ++i) {
                sb.append( (i+1) + " -> " + ports.get(i) + " || ");
            }

            String numberStr = DialogUtils.getStringDialog(null, "Select Port: " + sb.toString(), "1");
            if(numberStr != null){
                port = Integer.parseInt( numberStr );
            }else{
                return;
            }
        }else{
            
        }
        
        
        if (network.connect(ports.get(port - 1), speed)) {
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
        //System.out.print( b + " ");
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

    /**
     * @brief Implementing {@link comms.NetworkIface::networkDisconnected}.
     * @details Called when the connection has been closed.
     * @see {@link comms.NetworkIface::networkDisconnected}
     */
    @Override
    public void networkDisconnected(int id) {
    }

    /**
     * @brief Implementing {@link comms.NetworkIface::parseInput}.
     * @details Handles messages received over the serial port. Currently just
     * prints it to the terminal.
     * @see {@link comms.NetworkIface::parseInput}
     */
    @Override
    public void parseInput(int id, byte[] message, int numBytes) {
        if (listener != null){
            listener.rxMsg(message, numBytes);
        }
    }

    /**
     * @brief Implements {@link comms.NetworkIface::writeLog}.
     * @details Used to write information concerning the connection. Currently,
     * all information is written to the command line.
     * @see {@link comms.NetworkIface::writeLog}
     */
    @Override
    public void writeLog(int id, String text) {
        //System.out.println("   log:  |" + text + "|");
    }
    
    
}
