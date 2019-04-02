/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
