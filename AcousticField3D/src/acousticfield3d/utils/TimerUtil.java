/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.utils;

import java.util.HashMap;

/**
 *
 * @author Asier
 */
public class TimerUtil {
    private static TimerUtil _instance = new TimerUtil();
    
    public static TimerUtil get() {
        return _instance;
    }
    
    private final HashMap<String, Long> timers;
    private boolean showLog;
    private TimerUtil(){
        timers = new HashMap<>();
        showLog = true;
    }
    
    public void tick(String tag){
        timers.put( tag , System.currentTimeMillis());
    }
    
    public double tack(String tag){
        Long start = timers.get(tag);
        long end = System.currentTimeMillis();
        if (start != null){
            double time = (end - start) / 1000.0;
            if(showLog){
                System.out.println("Timer " + tag + " " + time);
            }
            return time;
        }else{
            return end / 1000.0;
        }
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }
    
    
}
