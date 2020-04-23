package acousticfield3d.gui.misc;

import acousticfield3d.math.M;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Asier
 */
public class SliderPanel extends JPanel{
    private static final int SUBDIVISIONS = 10;
    private final int nButtons;
    private boolean showDivisions;
    
    boolean show;
    
    final float[] current;
    float start;
    int lastX, lastY;
    int lastButton;

    public SliderPanel(int nButtons, boolean showDivisions) {
        this.nButtons = nButtons;
        this.showDivisions = showDivisions;
        show = false;
        current = new float[nButtons];
    }
 
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        if (!show) { return; }
        
        final int w = getWidth();
        final int h = getHeight();
        
        if(showDivisions){
            int xStart = (int)(start * w);
            g.setColor( Color.BLACK );
            final float subdivSize = w / (float)(SUBDIVISIONS-1);
            for(float x = xStart; x >= 0; x -= subdivSize){
                g.drawLine((int)x, 0, (int)x, h);
            }
            for(float x = xStart; x <= w; x += subdivSize){
                g.drawLine((int)x, 0, (int)x, h);
            } 

            g.setColor( Color.RED );
            g.drawLine(xStart, 0, xStart, h); //start
        }
        
        for(int i = 0; i < nButtons; ++i){
            int xCurrent = (int)(current[i] * w);
            g.setColor( Color.BLUE );
            g.drawLine(xCurrent, 0, xCurrent, h); //current
        }
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public void touchDown(int x, int y, int button) {
        lastButton = button;
        if (button >= nButtons) {return;}
        lastX = x;
        lastY = y;
        setShow( true );
        
        start = current[button] = x / (float)getWidth();
        repaint();
    }
     
    public float touchDrag(int x, int y) {
        int button = lastButton;
        if (button >= nButtons) {return 0.0f;}
        int currentX = x;
        int currentY = y;
        
        float diffX = (currentX - lastX) / (float)getWidth();
        current[button] = currentX / (float)getWidth();
        for(int i = 0; i < button; ++i){
            current[i] = M.min( current[button], current[i]);
        }
        for(int i = button; i < nButtons; ++i){
            current[i] = M.max( current[button], current[i]);
        }
        repaint();
        
        lastX = currentX;
        lastY = currentY;
        
        return diffX;
    }

    public float getCurrent(int index) {
        return current[index];
    }
    
    public float getCurrent(){
        if (lastButton >= nButtons) {return 0.0f;}
        return current[lastButton];
    }

    public int getLastButton() {
        return lastButton;
    }
    
    
    
}
