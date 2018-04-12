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
public class SimpleFPGA_128 extends SimpleFPGA{

    @Override
    public byte getStartPhasesCommand(){
        return (byte) (0xFF & 255);
    }
    
    @Override
    public byte getSwapCommand(){
        return (byte) (0xFF & 254);
    }
    
    
    @Override
    public int getnTransducers(){
        return 64;
    }
    
    @Override
    public int getDivs() {
        return 128;
    }

    @Override
    public int getSpeed() {
        return 500000;
    } 
    
  
}
