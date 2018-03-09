/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.workers;

import acousticfield3d.gui.panels.AnimPanel;
import acousticfield3d.simulation.Animation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Asier
 */
public class PlayerThread extends Thread{
    private final AnimPanel form;
    float currentTime;
    boolean goingDown;
    
    public boolean shouldIgnorePlayerSlider = false;
    public PlayerThread(AnimPanel form, float fps) {
        this.form = form;
        currentTime = 0.0f;
        goingDown = false;
    }

    public float getCurrentTime() {
        return currentTime;
    }
    
    public synchronized void playOrPause(){
        notify();
    }
    
    public synchronized  void stopReproduction(){
        notify();
        currentTime = 0.0f;
        goingDown = false;
    }
    
    public void prev() {
        currentTime -= 1.0f;
        float duration = form.currentAnimation.getDuration();
        apply(true, form.currentAnimation);
        updateTimeIndicator(duration);
    }

    public void next() {
        currentTime += 1.0f;
        apply(true, form.currentAnimation);
        float duration = form.currentAnimation.getDuration();
        updateTimeIndicator(duration);
    }
    
    public void applyAtPercetange(float p){
        Animation anim = form.currentAnimation;
        if(anim != null){
            float duration = anim.getDuration();
            anim.applyAtTime(duration * p, form.mf.simulation);
        }
    }
    
    public void applyFrame(int frame) {
        Animation anim = form.currentAnimation;
        if(anim != null){
            anim.applyAtFrame(frame, form.mf.simulation);
        }

    }
    
    private void updateTimeIndicator(float duration){
        form.shouldIgnorePlayerSlider = true;
        form.updateTimeSlider(currentTime / duration);
        form.setCurrentFrame( currentTime );
    }
    
    public int getCurrentFrame(){
        return Math.round( currentTime );
    }
    
    private void apply(final boolean isStepAnim, Animation anim) {
        //apply
        synchronized(form.mf){
            if( isStepAnim ){
                applyFrame(Math.round( currentTime ) );
            }else{
                anim.applyAtTime(currentTime, form.mf.simulation);
            }
            form.mf.transControlPanel.animFrame( Math.round( currentTime )  );
        }
        form.mf.needUpdate();
    }
        
    @Override
    public void run() {
        while(!interrupted()){
            if (form.isPlaying()){
                final long beginMillis = System.currentTimeMillis();
                
                final Animation anim = form.currentAnimation;
                final float duration = anim.getDuration();
                final boolean isStepAnim = form.isStepAnim();
                if(isStepAnim){
                    currentTime = Math.round( currentTime );
                }
                
                updateTimeIndicator(duration);
                
                apply(isStepAnim, anim);
                
              
                //advance
                final float speed = form.getStepSpeed();
                if(form.isWrapPingPong()){
                    if (goingDown){
                        currentTime -= speed;
                        if(currentTime < 0){
                            goingDown = false;
                            currentTime = -currentTime;
                        }
                    }else{
                        currentTime += speed;
                        if(currentTime > duration){
                            goingDown = true;
                            currentTime -= (currentTime-duration);
                        }
                    }
                }else{
                    currentTime += speed;
                    if (currentTime > duration){
                        if( form.isWrapStop()){
                            stopReproduction();
                            form.setPlaying(false);
                        }else if(form.isWrapRepeat()){
                            currentTime -= duration;
                        }
                    }
                }
                
                updateTimeIndicator(duration);
                
                //wait time
                try {
                    float sleepTime;
                    if(form.isStepAnim()){
                        sleepTime = form.getWaitTime();
                    }else{
                        sleepTime = form.getWaitTime();
                    }
                    final long endMillis = System.currentTimeMillis();
                    final long sleepMillis = ((long)sleepTime) - (endMillis - beginMillis);
                    if(sleepMillis > 0){
                        Thread.sleep( sleepMillis );
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlayerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
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
