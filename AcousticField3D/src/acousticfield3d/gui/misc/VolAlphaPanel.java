/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.gui.misc;

import acousticfield3d.math.M;
import acousticfield3d.utils.BufferUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.nio.FloatBuffer;
import javax.swing.JPanel;

/**
 *
 * @author Asier
 */
public class VolAlphaPanel extends JPanel{
    private final int xResolution;
    private final FloatBuffer values;
    private float totalValue;

    public VolAlphaPanel(int xResolution) {
        this.xResolution = xResolution;
        values = BufferUtils.createFloatBuffer( xResolution );
        for(int i = 0; i < xResolution; ++i){
            values.put(i, 1.0f);
        }
        totalValue = xResolution ;
    }
    
    public void setValueAt(int x, int y){
        final int w = getWidth();
        final int h = getHeight();
        
        int xPos = x * xResolution / w;
        xPos = M.iclamp(xPos, 0, xResolution-1);
        float value = 1.0f - (y / (float)h);
        value = M.clamp(value, 0.0f, 1.0f);
        values.put(xPos, value);
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        final int w = getWidth();
        final int h = getHeight();
        g.clearRect(0, 0, w, h);
        
        g.setColor(Color.BLACK);
        float widthPerBar = w / (float)xResolution;
        float startX = 0;
        synchronized(this){
            totalValue = 0;
            for(int i = 0; i < xResolution; ++i){
                float endX = startX + widthPerBar;
                float v = values.get(i);
                totalValue += v;
                float height = v * h;

                g.fillRect((int)startX, h - (int)height, (int)widthPerBar, (int)height);

                startX = endX;
            }
        }
    }

    public int getxResolution() {
        return xResolution;
    }

    public FloatBuffer getValues() {
        return values;
    }

    public float getTotalValue() {
        return totalValue;
    }
    
    
}
