/**
 * @file Network.java
 * @details Code originally written by Raphael Blatter (raphael@blatter.sg)
*/

package acousticfield3d.utils.uartComm;

import gnu.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Network {

    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedInputStream bufInputStream;
    private BufferedOutputStream bufOutputStream;
    
    private boolean connected = false;
    private boolean end = false;
    
    private Thread reader;
    private SerialPort serialPort;

    private NetworkInterface contact;

    private int id;

    /**
     * @param id
     * <b>int</b> identifying the specific instance of the Network-class. While
     * having only a single instance, {@link #id} is irrelevant. However, having
     * more than one open connection (using more than one instance of Network),
     * {@link #id} helps identifying which Serial connection a message or a log
     * entry came from.
     *
     * @param contact Link to the instance of the class implementing
     * {@link net.NetworkIface}.
     *
     * @param divider A small <b>int</b> representing the number to be used to
     * distinguish between two consecutive packages. It can take a value between
     * 0 and 255. Note that data is only sent to
     * {@link net.NetworkIface#parseInput(int, int, int[])} once the following
     * {@link #divider} could be identified.
     */
    public Network(int id, NetworkInterface contact) {
        this.contact = contact;
        this.id = id;
    }


    /**
     * Just as {@link #Network(int, NetworkIface, int)}, but with a default
     * {@link #divider} of <b>255</b> and a default {@link #id} of 0. This
     * constructor may mainly be used if only one Serial connection is needed at
     * any time.
     *
     * @see #Network(int, NetworkIface, int)
     */
    public Network(NetworkInterface contact) {
        this(0, contact);
    }

    public List<String> getPortList() {
        
        ArrayList<String> portVect = new ArrayList<String>();
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portVect.add(portId.getName());
            }
        }
        contact.writeLog(id, "found the following ports:");
        for (int i = 0; i < portVect.size(); i++) {
            contact.writeLog(id, ("   " + (String) portVect.get(i)));
        }

        return portVect;
    }

    /**
     * Just as {@link #connect(String, int)}, but using 115200 bps as a default
     * speed of the connection.
     *
     * @param portName The name of the port the connection should be opened to
     * (see {@link #getPortList()}).
     * @return <b>true</b> if the connection has been opened successfully,
     * <b>false</b> otherwise.
     * @see #connect(String, int)
     */
    public boolean connect(String portName) {
        return connect(portName, 115200);
    }

    /**
     * Opening a connection to the specified Serial port, using the specified
     * speed. After opening the port, messages can be sent using
     * {@link #writeSerial(String)} and received data will be packed into
     * packets (see {@link #divider}) and forwarded using
     * {@link net.NetworkIface#parseInput(int, int, int[])}.
     *
     * @param portName The name of the port the connection should be opened to
     * (see {@link #getPortList()}).
     * @param speed The desired speed of the connection in bps.
     * @return <b>true</b> if the connection has been opened successfully,
     * <b>false</b> otherwise.
     */
    public boolean connect(String portName, int speed) {
        CommPortIdentifier portIdentifier;
        boolean conn = false;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                contact.writeLog(id, "Error: Port is currently in use");
            } else {
                serialPort = (SerialPort) portIdentifier.open("RTBug_network",
                        2000);
                serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
                bufInputStream = new BufferedInputStream(inputStream);
                bufOutputStream = new BufferedOutputStream(outputStream);

                reader = (new Thread(new SerialReader(bufInputStream)));
                end = false;
                reader.start();
                connected = true;
                contact.writeLog(id, "connection on " + portName + " established");
                conn = true;
            }
        } catch (NoSuchPortException e) {
            contact.writeLog(id, "the connection could not be made");
            e.printStackTrace();
        } catch (PortInUseException e) {
            contact.writeLog(id, "the connection could not be made");
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            contact.writeLog(id, "the connection could not be made");
            e.printStackTrace();
        } catch (IOException e) {
            contact.writeLog(id, "the connection could not be made");
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * A separate class to use as the {@link net.Network#reader}. It is run as a
     * separate {@link Thread} and manages the incoming data, packaging them
     * using {@link net.Network#divider} into arrays of <b>int</b>s and
     * forwarding them using
     * {@link net.NetworkIface#parseInput(int, int, int[])}.
     *
     */
    private class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            
            try {
                while (!end) {
                    if ((in.available()) > 0) {
                        if ((len = this.in.read(buffer)) > 0) {
                            contact.parseInput(id, buffer, len);
                        }
                    }else{
                        Thread.yield();
                    }
                }
            } catch (IOException e) {
                end = true;
                try {
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                
                serialPort.close();
                connected = false;
                contact.networkDisconnected(id);
                contact.writeLog(id, "connection has been interrupted");
            }
        }
    }

    /**
     * Simple function closing the connection held by this instance of
     * {@link net.Network}. It also ends the Thread {@link net.Network#reader}.
     *
     * @return <b>true</b> if the connection could be closed, <b>false</b>
     * otherwise.
     */
    public boolean disconnect() {
        boolean disconn = true;
        end = true;
        try {
            if (reader != null){
                reader.join();
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            disconn = false;
        }
        try {
            bufInputStream.close();
            bufOutputStream.close();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            disconn = false;
        }
        serialPort.close();
        connected = false;
        contact.networkDisconnected(id);
        contact.writeLog(id, "connection disconnected");
        return disconn;
    }

    /**
     * @return Whether this instance of {@link net.Network} has currently an
     * open connection of not.
     */
    public boolean isConnected() {
        return connected;
    }

    

    public void writeByte(final int b){
        if (isConnected()) {
            try {
               bufOutputStream.write(b);
            } catch (IOException ex) {
                disconnect();
                Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void flush(){
        try {
            bufOutputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeSerial(final byte message[], final int offset, final int len) {
        if (isConnected()) {
            try {

                //bufOutputStream.write(message, offset, len);
                bufOutputStream.write(message, offset, len);
            } catch (IOException ex) {
                disconnect();
                Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
