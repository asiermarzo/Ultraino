package acousticfield3d.protocols;

/**
 *
 * @author am14010
 */
public class ArduinoNano16 extends ArduinoNano{
    @Override
    public int getDivs() {
        return 16;
    }

    @Override
    public int getTransducers() {
        return 16; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSpeed() {
        return 115200;
    }
}
