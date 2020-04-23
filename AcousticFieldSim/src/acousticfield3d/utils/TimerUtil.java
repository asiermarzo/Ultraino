package acousticfield3d.utils;

import java.util.HashMap;

/**
 *
 * @author Asier
 */
public class TimerUtil {
    private static final TimerUtil _instance = new TimerUtil();
    
    public static TimerUtil get() {
        return _instance;
    }
    
    private final HashMap<String, Long> timers;
    private TimerUtil(){
        timers = new HashMap<>();
    }
    
    public void tick(String tag){
        timers.put( tag , System.currentTimeMillis());
    }
    
    public double tack(String tag){
        return tack(tag, true);
    }
    
    public double tack(String tag, final boolean newline){
        Long start = timers.get(tag);
        long end = System.currentTimeMillis();
        if (start != null){
            double time = (end - start) / 1000.0;
            final String s = "Timer " + tag + " " + time;
            if(newline){
                System.out.println(s);
            }else{
                System.out.print(s);
            }
            return time;
        }else{
            return end / 1000.0;
        }
    }
    
}
