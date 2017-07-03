/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package acousticfield3d.utils;

import acousticfield3d.math.M;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Asier
 */
public class ImagesUtils {
    
    public static void colorizeImage(BufferedImage bi){
        int w = bi.getWidth();
        int h = bi.getHeight();
        int pixels = w * h;
        int[] data = new int[pixels];
        bi.getRGB(0, 0, w, h, data, 0, w);
        colorize(data, w, h);
        bi.setRGB(0, 0, w, h, data, 0, w);
    }
   
               
   public static void extractColorSig(BufferedImage bi, float[] sig){
        //sig is mean (r g b a) std(r g b a)
        long[] sigInt = new long[8];
        for(int i = 0; i < 8; ++i){
            sig[i] = 0;
            sigInt[i] = 0;
        }
        
        int w = bi.getWidth();
        int h = bi.getHeight();
        int pixels = w * h;
        int[] data = new int[pixels];
        bi.getRGB(0, 0, w, h, data, 0, w);
        for(int i = 0; i < pixels; ++i){
            int sourceColor = data[i];
            sigInt[0] += (sourceColor >> 16) & 0x000000FF;
            sigInt[1] += (sourceColor >> 8) & 0x000000FF;
            sigInt[2] += (sourceColor) & 0x000000FF;
            sigInt[3] += (sourceColor >> 24) & 0x000000FF;  
        }
        for(int i = 0; i < 4; ++i){
            sigInt[i] /= pixels;
            sig[i] = sigInt[i] / 256.0f;
        }
        for(int i = 0; i < pixels; ++i){
            int sourceColor = data[i];
            long diffRed = ((sourceColor >> 16) & 0x000000FF) - sigInt[0];
            long diffGreen = ((sourceColor >> 8) & 0x000000FF) - sigInt[1];
            long diffBlue = ((sourceColor) & 0x000000FF) - sigInt[2];
            long diffAlpha = ((sourceColor >> 24) & 0x000000FF) - sigInt[3];
            sigInt[4] += diffRed * diffRed;
            sigInt[5] += diffGreen * diffGreen;
            sigInt[6] += diffBlue * diffBlue;
            sigInt[7] += diffAlpha * diffAlpha;  
        }
        for(int i = 4; i < 8; ++i){
            sig[i] = (float)Math.sqrt(sigInt[i] / 256.0f / pixels);
        }
    }                                     

    
    //watch out rgba <--> argb
    public static void colorize(int[] raw, int w, int h) {
        int index = 0;
        for(int iy = 0; iy < h; iy++){
            for(int ix = 0; ix < w; ix++){
                int color = raw[index];
                if(Color.red(color) <= 0){ 
                    raw[index] = colorizePixel(raw,ix,iy,w,h);
                }
                index++;
            }
        }
    }

    private static int getPixelAt(int[] raw, int x, int y, int w, int h){
        if(x >= 0 && x < w && y >= 0 && y < h){
            return raw[x + y*w];
        }
        return 0;
    }
    
    private static int colorizePixel(int[] raw, int ix, int iy, int w, int h) {
        int[] neightbours = new int[8];
        double totalAlpha = 0;
        double red = 0, green = 0, blue = 0;
        neightbours[0] = getPixelAt(raw, ix+1, iy, w,h);
        totalAlpha += Color.red(neightbours[0]);
        neightbours[1] = getPixelAt(raw, ix+1, iy-1, w,h);
        totalAlpha += Color.red(neightbours[1]);
        neightbours[2] = getPixelAt(raw, ix, iy-1, w,h);
        totalAlpha += Color.red(neightbours[2]);
        neightbours[3] = getPixelAt(raw, ix-1, iy-1, w,h);
        totalAlpha += Color.red(neightbours[3]);
        neightbours[4] = getPixelAt(raw, ix-1, iy, w,h);
        totalAlpha += Color.red(neightbours[4]);
        neightbours[5] = getPixelAt(raw, ix-1, iy+1, w,h);
        totalAlpha += Color.red(neightbours[5]);
        neightbours[6] = getPixelAt(raw, ix, iy+1, w,h);
        totalAlpha += Color.red(neightbours[6]);
        neightbours[7] = getPixelAt(raw, ix+1, iy+1, w,h);
        totalAlpha += Color.red(neightbours[7]);
        if (totalAlpha <= 0){
            return getPixelAt(raw, ix, iy, w,h);
        }
        for(int i = 0; i < 8; i++){
            double proportion = Color.alpha(neightbours[i]) / totalAlpha;
            red   += proportion * Color.green(neightbours[i]);
            green += proportion * Color.blue(neightbours[i]);
            blue  += proportion * Color.alpha(neightbours[i]);
        }
        int iRed = Math.max(Math.min((int)red, 255), 0);
        int iGreen = Math.max(Math.min((int)green, 255), 0);
        int iBlue = Math.max(Math.min((int)blue, 255), 0);
        return Color.create(0,iRed,iGreen,iBlue);
    }
    
    public static BufferedImage resizeImage(BufferedImage source, int sx, int sy) {
        int width = Math.max(sx, 1);
        int height = Math.max(sy, 1);
        
        boolean useLaczos = source.getWidth() > 4
                && source.getHeight() > 4
                && sx < source.getWidth()
                && sy < source.getHeight();

        useLaczos = false; //it does not work very well
        BufferedImage forReturn = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        if (useLaczos) {
           // ResampleOp op = new ResampleOp(width, height);
           // op.filter(source, forReturn);
        } else {
            Graphics2D graphics2D = forReturn.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics2D.drawImage(source, 0, 0, sx, sy, null);
            graphics2D.dispose();
        }
        return forReturn;
    }

    public static BufferedImage cropImage(BufferedImage bi, int alphaCut, int cutPoint[]){
        int sizex = bi.getWidth();
        int sizey = bi.getHeight();
        int lx=sizex,ly=sizey;
        int rx=-1,ry=-1;

        //quick test of solid image
        if ( ((bi.getRGB(0, 0) >>> 24) & 0xFF) > alphaCut &&
              ((bi.getRGB(sizex-1, sizey-1) >>> 24) & 0xFF) > alphaCut){
            if(cutPoint != null && cutPoint.length >= 2) { cutPoint[0] = 0; cutPoint[1] = 0;}
            if(cutPoint != null && cutPoint.length >= 4) { cutPoint[2] = sizex-1; cutPoint[3] = sizey-1;}
            return bi;
        }

        //find upper left corner
        for(int y=0; y < sizey; y++){
            for(int x=0; x < sizex; x++){
                if ( ((bi.getRGB(x, y) >>> 24) & 0xFF) > alphaCut){
                    lx = Math.min(lx, x);
                    ly = Math.min(ly, y);
                    rx = Math.max(rx, x);
                    ry = Math.max(ry, y);
                }
            }
        }

        if(cutPoint != null && cutPoint.length >= 2) { cutPoint[0] = lx; cutPoint[1] = ly;}
        if(cutPoint != null && cutPoint.length >= 4) { cutPoint[2] = rx; cutPoint[3] = ry;}

        BufferedImage forReturn = new BufferedImage(rx-lx+1, ry-ly+1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = forReturn.createGraphics();
            graphics2D.drawImage(bi, 0, 0, rx-lx+1, ry-ly+1, lx, ly, rx+1, ry+1, null);
            graphics2D.dispose();

      return forReturn;
    }

    public static BufferedImage cloneImage(BufferedImage bi){
        int imageWidth = bi.getWidth();
        int imageHeight = bi.getHeight();
        int[] argb = bi.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);
        BufferedImage biClone = new BufferedImage(imageWidth, imageHeight, bi.getType());
        biClone.setRGB(0, 0, imageWidth, imageHeight, argb, 0, imageWidth);
        return biClone;
    }
    
    public static BufferedImage flipYImage(BufferedImage bi){
        int imageWidth = bi.getWidth();
        int imageHeight = bi.getHeight();
        int[] argb = bi.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);
        BufferedImage flipped = new BufferedImage(imageWidth, imageHeight, bi.getType());
        for(int y = 0; y < imageHeight; y++){
            int half = imageWidth/2;
            int flipX = imageWidth-1;
            int offsetY = y * imageWidth;
            for(int x = 0; x < half; x++){
                int i1 = offsetY + x;
                int i2 = offsetY + flipX;
                int temp = argb[i1];
                argb[i1] = argb[i2];
                argb[i2] = temp;
                flipX--;
            }
        }
        
        flipped.setRGB(0, 0, imageWidth, imageHeight, argb, 0, imageWidth);
        return flipped;
    }

    public static void rescaleMatrix(float[][] data, float[][] scaledData, boolean linear) {
        final int oW = data.length;
        final int oH = data[0].length;
        final int tW = scaledData.length;
        final int tH = scaledData[0].length;
        if(linear){
            for(int ix = 0; ix < tW; ++ix){
                for(int iy = 0; iy < tH; ++iy){
                    scaledData[ix][iy] = bilinearFetch(data, 
                            (float)ix / (float)tW * (float)(oW-1), 
                            (float)iy / (float)tH * (float)(oH-1) );
                }
            }
        }else{
            for(int ix = 0; ix < tW; ++ix){
                for(int iy = 0; iy < tH; ++iy){
                    int x = Math.round( ix / (float)tW * (oW-1) );
                    int y = Math.round( iy / (float)tH * (oH-1) );
                    scaledData[ix][iy] = data[x][y];
                }
            }
        }
    }
    
    public static float bilinearFetch(float[][] data, float x, float y) {
        int xx = (int) Math.floor(x);
        int yy = (int) Math.floor(y);
        float dx = x - xx;
        float dy = y - yy;
       
        float a = data[xx][yy] + dy * (data[xx][yy + 1] - data[xx][yy]);
        float b = data[xx + 1][yy] + dy * (data[xx + 1][yy + 1] - data[xx + 1][yy]);
        
        return a + dx * (b - a);
    }
    
    public static int[][] getARGB( String path ){
        BufferedImage bi;
        try {
            bi = ImageIO.read( new File(path) );
        } catch (IOException ex) {
            Logger.getLogger(ImagesUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        final int w = bi.getWidth();
        final int h = bi.getHeight();
        
        int[] argb = new int[w * h];
        bi.getRGB(0, 0, w, h, argb, 0, w);
       
        int[][] matrix = new int[w][h];
        int index = 0;
        for(int ih = 0; ih < h; ++ih){
            for(int iw = 0; iw < w; ++iw){
                matrix[iw][ih] = argb[index];
                
                ++index;
            }
        }
        
        return matrix;
    }
    
    public static float[][] mapMatrix(final int[][] input, final boolean hueOrGrey){
        final int w = input.length;
        final int h = input[0].length;
        
        final float output[][] = new float[w][h];
        for(int ih = 0; ih < h; ++ih){
            for(int iw = 0; iw < w; ++iw){
                final int c = input[iw][ih];
                output[iw][ih] = hueOrGrey ? Color.getHue(c) : Color.getGrey(c);
            }
        }
        
        return output;
    }
    
    
    
}
