package acousticfield3d.gui;

import acousticfield3d.math.M;
import java.awt.Color;

/**
 *
 * @author Asier
 */
public class Gradients {
    final static int GRADIENT_RES = 257;
    final static int GRADIENT_RES_HALF = GRADIENT_RES / 2;
    final static int GRADIENT_RES_L1 = GRADIENT_RES-1;
    final static int GRADIENT_RES_HALF_L1 = GRADIENT_RES_HALF-1;
    int[] gradientFire; //255
    int[] gradientHue; //255
    int[][] gradientHB; //255,255
    
    
    private static Gradients _instance;
    public static Gradients get(){
        if(_instance == null){ //do not worry about sync
            _instance = new Gradients();
        }
        return _instance;
    }
    
    private Gradients() {
        initGradients();
    }
    
    public int getGradientAmp(float p){
        p = M.clamp(p, 0, 1);
        int i = (int)(p * GRADIENT_RES_L1);
        return gradientFire[i];
    }
    
    public int getGradientPhase(float p){
        int i = (int)(p * GRADIENT_RES_L1);
        return gradientHue[i];
    }
    
    public int getGradientAmpAndPhase(float amp, float phase){
        int iAmp = (int)( (3.0f*amp+1.0f)/4.0f * GRADIENT_RES_L1);
        int iPhase = (int)(phase * GRADIENT_RES_L1);
        iAmp = M.iclamp(iAmp, 0, GRADIENT_RES_L1);
        iPhase = M.iclamp(iPhase, 0, GRADIENT_RES_L1);
        return gradientHB[iPhase][iAmp];
    }
    
    private void initGradients() {
        gradientFire = new int[GRADIENT_RES]; //255
        gradientHue = new int[GRADIENT_RES];
        gradientHB = new int[GRADIENT_RES][GRADIENT_RES];
        
        for(int i = 0; i < GRADIENT_RES; ++i){
            float ip = i / (float)GRADIENT_RES;
          
            Color fireColor = new Color(
                (float) Math.min(Math.max(3.0*ip, 0),1) ,
                (float) Math.min(Math.max(3.0*ip-1, 0),1) ,
                (float) Math.min(Math.max(3.0*ip-2, 0),1) );
            gradientFire[i] = acousticfield3d.utils.Color.argb2rgba( fireColor.getRGB() );
            
            gradientHue[i] = acousticfield3d.utils.Color.argb2rgba( 
                    Color.HSBtoRGB(ip, 1.0f, 1.0f) 
            );
            
            for(int j = 0; j < GRADIENT_RES; ++j){
                gradientHB[i][j] = acousticfield3d.utils.Color.argb2rgba( 
                        Color.HSBtoRGB(ip, 1.0f, j / (float)GRADIENT_RES)
                );
            }
        }
        
    }
}
