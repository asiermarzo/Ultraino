/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package acousticfield3d.shapes;

import acousticfield3d.utils.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * <code>Quad</code> represents a rectangular plane in space
 * defined by 4 vertices. The quad's lower-left side is contained
 * at the local space origin (0, 0, 0), while the upper-right
 * side is located at the width/height coordinates (width, height, 0).
 * 
 * @author Kirill Vainer
 */
public class Quad extends Mesh {

    private float width;
    private float height;
    private int divs;
    private boolean doubleSided;

    public Quad(){
    }

    /**
     * Create a quad with the given width and height. The quad
     * is always created in the XY plane.
     * 
     * @param width The X extent or width
     * @param height The Y extent or width
     * @param divs tessellation
     */
    public Quad(float width, float height, int divs, boolean doubleSided){
        this.width = width;
        this.height = height;
        this.divs = divs;
        this.doubleSided = doubleSided;
        updateGeometry();
    }


    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }
    
    public int getDivs(){
        return divs;
    }


    public void updateGeometry() {
        final int p = divs + 1;
        final int d = divs;
        final float w = width / (float)d;
        final float h = height / (float)d;
        final float w2 = width / 2.0f;
        final float h2 = height / 2.0f;
        
        final int nV = p*p;
        final int nI = d*d*2*3 * (doubleSided ? 2 : 1);
        setPosition( BufferUtils.createVector3Buffer(nV) );
        setTexture(BufferUtils.createVector2Buffer(nV) );
        setNormal(BufferUtils.createVector3Buffer(nV) );
        setIndices(BufferUtils.createShortBuffer( nI ) );
        
        final FloatBuffer pos = getPosition();
        final FloatBuffer tex = getTexture();
        final FloatBuffer nor = getNormal();
        //create vertices
        for(int iy = 0; iy < p; ++iy){
            for(int ix = 0; ix < p; ++ix){
                final float x = -w2 + ix*w;
                final float y = h2 - iy*h;
                
                pos.put(x).put(y).put(0.0f);
                tex.put( ix / (float)d  ).put( iy / (float)d );
                nor.put(0.0f).put(0.0f).put(1.0f);
            } 
        }
        //create indices
        final ShortBuffer ind = getIndices();
        for(int iy = 0; iy < d; ++iy){
            for(int ix = 0; ix < d; ++ix){
                final int currentRow = (iy*p + ix);
                final int nextRow = ((iy+1)*p + ix);
                ind.put( (short) currentRow ).put((short) (currentRow + 1)).put((short) nextRow );
                ind.put( (short) (currentRow + 1) ).put((short) (nextRow + 1)).put((short) nextRow );
            }
        }
        if(doubleSided){
            for (int iy = 0; iy < d; ++iy) {
                for (int ix = 0; ix < d; ++ix) {
                    final int currentRow = (iy * p + ix);
                    final int nextRow = ((iy + 1) * p + ix);
                     
                    ind.put((short) nextRow).put((short) (currentRow + 1)).put((short) currentRow);
                    ind.put((short) nextRow).put((short) (nextRow + 1)).put((short) (currentRow + 1));
                }
            }
        }

        updateBound();
        updateCounts();
    }


}
