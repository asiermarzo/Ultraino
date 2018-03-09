/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.workers;

import acousticfield3d.gui.MainForm;
import acousticfield3d.simulation.Animation;
import static java.lang.Thread.interrupted;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author am14010
 */
public class UpdateThread extends Thread{
    final MainForm mf;

    int milliSeconds = 0;
    boolean playing = false;
    
    public UpdateThread(MainForm mf) {
        this.mf = mf;
    }

    public int getMilliSeconds() {
        return milliSeconds;
    }

    public void setMilliSeconds(int milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
    
    
    public synchronized void playOrPause(){
        notify();
    }
   
    @Override
    public void run() {
        while(!interrupted()){
            if ( playing ){
                mf.needUpdate();
                        
                //wait time
                try {
                    Thread.sleep( milliSeconds );
                }catch (InterruptedException ex) {}
            }else{
                synchronized(this){
                    try {
                        wait();
                    } catch (InterruptedException ex) {}
                }
            }
        }
    }
}
