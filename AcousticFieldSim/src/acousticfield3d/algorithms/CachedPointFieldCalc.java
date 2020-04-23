package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.gui.misc.ForcePlotsFrame;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.renderer.Renderer;
import java.nio.FloatBuffer;

/**
 *
 * @author Asier
 */
public class CachedPointFieldCalc {
    Vector3f position = new Vector3f();
    boolean isReflector;
    
    Vector2f pre = new Vector2f();
    Vector2f gx = new Vector2f(), gy = new Vector2f(), gz = new Vector2f();
    Vector2f gxy = new Vector2f(), gxz = new Vector2f(), gyz = new Vector2f();
    Vector2f gxx = new Vector2f(), gyy = new Vector2f(), gzz = new Vector2f();
    
    Vector2f gxxx = new Vector2f(), gyyy = new Vector2f(), gzzz = new Vector2f();
    Vector2f gxyy = new Vector2f(), gxzz = new Vector2f();
    Vector2f gyxx = new Vector2f(), gyzz = new Vector2f();
    Vector2f gzxx = new Vector2f(), gzyy = new Vector2f();
         
    Vector2f swap = new Vector2f(), tmp = new Vector2f();
    Vector3f nor = new Vector3f();
    Vector3f diffVec = new Vector3f();
    Vector3f diffVecS = new Vector3f();
    Vector3f tPos = new Vector3f();
    
    double K1, K2;
    double M1, M2;
    
    final int nTotalTrans, nTrans;
    final float h;
    final boolean useDirectivity;
    
    double[] a,b, ka, kb; //per transducers
    double[] xa,xb, xka, xkb;
    double[] ya,yb, yka, ykb;
    double[] za,zb, zka, zkb;
    
    double[] xya,xyb, xyka, xykb;
    double[] xza,xzb, xzka, xzkb;
    double[] yza,yzb, yzka, yzkb;
    double[] xxa,xxb, xxka, xxkb;
    double[] yya,yyb, yyka, yykb;
    double[] zza,zzb, zzka, zzkb;
    
    double[] xxxa,xxxb, xxxka, xxxkb;
    double[] yyya,yyyb, yyyka, yyykb;
    double[] zzza,zzzb, zzzka, zzzkb;
    double[] xyya,xyyb, xyyka, xyykb;
    double[] xzza,xzzb, xzzka, xzzkb;
    double[] yxxa,yxxb, yxxka, yxxkb;
    double[] yzza,yzzb, yzzka, yzzkb;
    double[] zxxa,zxxb, zxxka, zxxkb;
    double[] zyya,zyyb, zyyka, zyykb;

    double lowPressureK = 1;
    
    public static CachedPointFieldCalc create(final Vector3f pos, MainForm mf){
        //final boolean reflection = mf.simulation.isReflection();
        //final boolean directivity = !mf.miscPanel.isAnalyticalNoDirShaders();
        final boolean reflection = false;
        final boolean directivity = true;
        
        final float h = mf.simulation.getWavelenght()/ mf.miscPanel.getFiniteDiffH();
        CachedPointFieldCalc cp = new CachedPointFieldCalc(pos, reflection,directivity, h, mf.renderer);
        return cp;        
    }
    
    public CachedPointFieldCalc(final Vector3f pos, boolean reflectorEnabled, boolean useDirectivity, float h, Renderer r) {
        this.isReflector = reflectorEnabled;
        this.useDirectivity = useDirectivity;
        this.h = h;
        nTotalTrans = r.getnTransducers();
        nTrans = reflectorEnabled ? nTotalTrans/2 : nTotalTrans;
        position.set( pos );
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }

    
    public int getNTrans() {
        return nTrans;
    }

    public double getLowPressureK() {
        return lowPressureK;
    }

    public void setLowPressureK(double lowPressureK) {
        this.lowPressureK = lowPressureK;
    }
    
    //<editor-fold defaultstate="collapsed" desc="pressure">
    
    public void updatePressure(final double[] v){
        pre.set(0,0);
        for(int i = 0; i < nTrans; ++i){
            final double cosP = Math.cos( v[i] );
            final double sinP = Math.sin( v[i] );
            a[i] = ka[i]*cosP - kb[i]*sinP;
            b[i] = ka[i]*sinP + kb[i]*cosP;
            pre.x += a[i];
            pre.y += b[i];
        }
    }
    
    public void evalField(Vector2f field){
        field.set( pre );
    }
    public double evalPressure(){
        return pre.length();
    }
    public void gradientPressure(double[] g){
        final double p = pre.length();
        final double aDivP = pre.x / p;
        final double bDivP = pre.y / p;
        for(int i = 0; i < nTrans; ++i){
            g[i] = bDivP * a[i] - aDivP * b[i];
        }
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="gorkov">  
   
    public void updateGorkov(final double[] v){
        pre.set(0);
        gx.set(0); gy.set(0); gz.set(0);
        for(int i = 0; i < nTrans; ++i){
            final double cosP = Math.cos( v[i] );
            final double sinP = Math.sin( v[i] );
            
            a[i] = ka[i]*cosP - kb[i]*sinP;
            b[i] = ka[i]*sinP + kb[i]*cosP;
            pre.x += a[i]; pre.y += b[i];
            
            xa[i] = xka[i]*cosP - xkb[i]*sinP;
            xb[i] = xka[i]*sinP + xkb[i]*cosP;
            gx.x += xa[i]; gx.y += xb[i];
            
            ya[i] = yka[i]*cosP - ykb[i]*sinP;
            yb[i] = yka[i]*sinP + ykb[i]*cosP;
            gy.x += ya[i]; gy.y += yb[i];
            
            za[i] = zka[i]*cosP - zkb[i]*sinP;
            zb[i] = zka[i]*sinP + zkb[i]*cosP;
            gz.x += za[i]; gz.y += zb[i];
        }
    }
    
    public double evalGorkov(){
        return  M1 * lowPressureK * pre.dot(pre) - M2*( gx.dot(gx) + gy.dot(gy) + gz.dot(gz));
    }
    public void gradientGorkov(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * lowPressureK *( pre.x*-b[i] + pre.y*a[i]) - 
                   K2*( gx.x*-xb[i]+gx.y*xa[i] + gy.x*-yb[i]+gy.y*ya[i] + gz.x*-zb[i]+gz.y*za[i]);
        }
    }
    //</editor-fold>
   
    //<editor-fold defaultstate="collapsed" desc="gorkovGradient">  
 
    public void updateGorkovGradient(final double[] v){
        pre.set(0);
        gx.set(0); gy.set(0); gz.set(0);
        gxx.set(0); gyy.set(0); gzz.set(0);
        gxy.set(0); gxz.set(0); gyz.set(0);
        for(int i = 0; i < nTrans; ++i){
            final double cosP = Math.cos( v[i] );
            final double sinP = Math.sin( v[i] );
            
            a[i] = ka[i]*cosP - kb[i]*sinP;
            b[i] = ka[i]*sinP + kb[i]*cosP;
            pre.x += a[i]; pre.y += b[i];
            
            xa[i] = xka[i]*cosP - xkb[i]*sinP;
            xb[i] = xka[i]*sinP + xkb[i]*cosP;
            gx.x += xa[i]; gx.y += xb[i];
            
            ya[i] = yka[i]*cosP - ykb[i]*sinP;
            yb[i] = yka[i]*sinP + ykb[i]*cosP;
            gy.x += ya[i]; gy.y += yb[i];
            
            za[i] = zka[i]*cosP - zkb[i]*sinP;
            zb[i] = zka[i]*sinP + zkb[i]*cosP;
            gz.x += za[i]; gz.y += zb[i];
            
            xya[i] = xyka[i]*cosP - xykb[i]*sinP;
            xyb[i] = xyka[i]*sinP + xykb[i]*cosP;
            gxy.x += xya[i]; gxy.y += xyb[i];
            
            xza[i] = xzka[i]*cosP - xzkb[i]*sinP;
            xzb[i] = xzka[i]*sinP + xzkb[i]*cosP;
            gxz.x += xza[i]; gxz.y += xzb[i];
            
            yza[i] = yzka[i]*cosP - yzkb[i]*sinP;
            yzb[i] = yzka[i]*sinP + yzkb[i]*cosP;
            gyz.x += yza[i]; gyz.y += yzb[i];
            
            xxa[i] = xxka[i]*cosP - xxkb[i]*sinP;
            xxb[i] = xxka[i]*sinP + xxkb[i]*cosP;
            gxx.x += xxa[i]; gxx.y += xxb[i];
            
            yya[i] = yyka[i]*cosP - yykb[i]*sinP;
            yyb[i] = yyka[i]*sinP + yykb[i]*cosP;
            gyy.x += yya[i]; gyy.y += yyb[i];
            
            zza[i] = zzka[i]*cosP - zzkb[i]*sinP;
            zzb[i] = zzka[i]*sinP + zzkb[i]*cosP;
            gzz.x += zza[i]; gzz.y += zzb[i];
        }
    }
    
    public void calcGorkovGradient(Vector3f position, Vector3f forces, MainForm mf, double[] phases){
        setPosition(position);
        initFieldConstants(mf);
        updateGorkovGradient(phases);
        forces.x = (float) evalGorkovGradientX();
        forces.y = (float) evalGorkovGradientY();
        forces.z = (float) evalGorkovGradientZ();
    }
    
    public double evalGorkovGradientXProportion(){
        return  (K1*pre.dot(gx)) / (K2*( gx.dot(gxx) + gy.dot(gxy) + gz.dot(gxz)));
    }
    public double evalGorkovGradientYProportion(){
        return  (K1*pre.dot(gy)) / (K2*( gx.dot(gxy) + gy.dot(gyy) + gz.dot(gyz)));
    }
    public double evalGorkovGradientZProportion(){
        return  (K1*pre.dot(gz)) / (K2*( gx.dot(gxz) + gy.dot(gyz) + gz.dot(gzz)));
    }
    
    public double evalGorkovGradientX(){
        return  K1*pre.dot(gx) - K2*( gx.dot(gxx) + gy.dot(gxy) + gz.dot(gxz));
    }
    public double evalGorkovGradientY(){
        return  K1*pre.dot(gy) - K2*( gx.dot(gxy) + gy.dot(gyy) + gz.dot(gyz));
    }
    public double evalGorkovGradientZ(){
        return  K1*pre.dot(gz) - K2*( gx.dot(gxz) + gy.dot(gyz) + gz.dot(gzz));
    }
    public void gradientGorkovGradientX(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * ( 
                    pre.x*-xb[i] + -b[i]*gx.x + pre.y*xa[i] + a[i]*gx.y ) - 
                   K2 * ( 
                    gx.x*-xxb[i] + -xb[i]*gxx.x + gx.y*xxa[i] + xa[i]*gxx.y +
                    gy.x*-xyb[i] + -yb[i]*gxy.x + gy.y*xya[i] + ya[i]*gxy.y +
                    gz.x*-xzb[i] + -zb[i]*gxz.x + gz.y*xza[i] + za[i]*gxz.y);
        }
    }
    public void gradientGorkovGradientY(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * ( 
                    pre.x*-yb[i] + -b[i]*gy.x + pre.y*ya[i] + a[i]*gy.y ) - 
                   K2 * ( 
                    gx.x*-xyb[i] + -xb[i]*gxy.x + gx.y*xya[i] + xa[i]*gxy.y +
                    gy.x*-yyb[i] + -yb[i]*gyy.x + gy.y*yya[i] + ya[i]*gyy.y +
                    gz.x*-yzb[i] + -zb[i]*gyz.x + gz.y*yza[i] + za[i]*gyz.y);
        }
    }
    public void gradientGorkovGradientZ(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * ( 
                    pre.x*-zb[i] + -b[i]*gz.x + pre.y*za[i] + a[i]*gz.y ) - 
                   K2 * ( 
                    gx.x*-xzb[i] + -xb[i]*gxz.x + gx.y*xza[i] + xa[i]*gxz.y +
                    gy.x*-yzb[i] + -yb[i]*gyz.x + gy.y*yza[i] + ya[i]*gyz.y +
                    gz.x*-zzb[i] + -zb[i]*gzz.x + gz.y*zza[i] + za[i]*gzz.y);
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="gorkovLaplacian">  
    public void allocateAndInit(MainForm mf){
        allocate(mf.renderer);
        initFieldConstants(mf);
    }
    
    public void allocate(Renderer r){
        a = new double[nTotalTrans]; b = new double[nTotalTrans];
        ka = new double[nTotalTrans]; kb = new double[nTotalTrans];
        
        xa = new double[nTotalTrans]; xb = new double[nTotalTrans];
        xka = new double[nTotalTrans]; xkb = new double[nTotalTrans];
        
        ya = new double[nTotalTrans]; yb = new double[nTotalTrans];
        yka = new double[nTotalTrans]; ykb = new double[nTotalTrans];
        
        za = new double[nTotalTrans]; zb = new double[nTotalTrans];
        zka = new double[nTotalTrans]; zkb = new double[nTotalTrans];
        
        xya = new double[nTotalTrans]; xyb = new double[nTotalTrans];
        xyka = new double[nTotalTrans]; xykb = new double[nTotalTrans];
        
        xza = new double[nTotalTrans]; xzb = new double[nTotalTrans];
        xzka = new double[nTotalTrans]; xzkb = new double[nTotalTrans];
        
        yza = new double[nTotalTrans]; yzb = new double[nTotalTrans];
        yzka = new double[nTotalTrans]; yzkb = new double[nTotalTrans];
        
        xxa = new double[nTotalTrans]; xxb = new double[nTotalTrans];
        xxka = new double[nTotalTrans]; xxkb = new double[nTotalTrans];
        
        yya = new double[nTotalTrans]; yyb = new double[nTotalTrans];
        yyka = new double[nTotalTrans]; yykb = new double[nTotalTrans];
        
        zza = new double[nTotalTrans]; zzb = new double[nTotalTrans];
        zzka = new double[nTotalTrans]; zzkb = new double[nTotalTrans];
        
        xxxa = new double[nTotalTrans]; xxxb = new double[nTotalTrans];
        xxxka = new double[nTotalTrans]; xxxkb = new double[nTotalTrans];
        yyya = new double[nTotalTrans]; yyyb = new double[nTotalTrans];
        yyyka = new double[nTotalTrans]; yyykb = new double[nTotalTrans];
        zzza = new double[nTotalTrans]; zzzb = new double[nTotalTrans];
        zzzka = new double[nTotalTrans]; zzzkb = new double[nTotalTrans];
        xyya = new double[nTotalTrans]; xyyb = new double[nTotalTrans];
        xyyka = new double[nTotalTrans]; xyykb = new double[nTotalTrans];
        xzza = new double[nTotalTrans]; xzzb = new double[nTotalTrans];
        xzzka = new double[nTotalTrans]; xzzkb = new double[nTotalTrans];
        yxxa = new double[nTotalTrans]; yxxb = new double[nTotalTrans];
        yxxka = new double[nTotalTrans]; yxxkb = new double[nTotalTrans];
        yzza = new double[nTotalTrans]; yzzb = new double[nTotalTrans];
        yzzka = new double[nTotalTrans]; yzzkb = new double[nTotalTrans];
        zxxa = new double[nTotalTrans]; zxxb = new double[nTotalTrans];
        zxxka = new double[nTotalTrans]; zxxkb = new double[nTotalTrans];
        zyya = new double[nTotalTrans]; zyyb = new double[nTotalTrans];
        zyyka = new double[nTotalTrans]; zyykb = new double[nTotalTrans];
    }
   
    
    public void updateGorkovLaplacian(final double[] v){
        pre.set(0);
        gx.set(0); gy.set(0); gz.set(0);
        gxx.set(0); gyy.set(0); gzz.set(0);
        gxy.set(0); gxz.set(0); gyz.set(0);
        for(int i = 0; i < nTrans; ++i){
            final double cosP = Math.cos( v[i] );
            final double sinP = Math.sin( v[i] );
            
            a[i] = ka[i]*cosP - kb[i]*sinP;
            b[i] = ka[i]*sinP + kb[i]*cosP;
            pre.x += a[i]; pre.y += b[i];
            
            xa[i] = xka[i]*cosP - xkb[i]*sinP;
            xb[i] = xka[i]*sinP + xkb[i]*cosP;
            gx.x += xa[i]; gx.y += xb[i];
            
            ya[i] = yka[i]*cosP - ykb[i]*sinP;
            yb[i] = yka[i]*sinP + ykb[i]*cosP;
            gy.x += ya[i]; gy.y += yb[i];
            
            za[i] = zka[i]*cosP - zkb[i]*sinP;
            zb[i] = zka[i]*sinP + zkb[i]*cosP;
            gz.x += za[i]; gz.y += zb[i];
            
            xya[i] = xyka[i]*cosP - xykb[i]*sinP;
            xyb[i] = xyka[i]*sinP + xykb[i]*cosP;
            gxy.x += xya[i]; gxy.y += xyb[i];
            
            xza[i] = xzka[i]*cosP - xzkb[i]*sinP;
            xzb[i] = xzka[i]*sinP + xzkb[i]*cosP;
            gxz.x += xza[i]; gxz.y += xzb[i];
            
            yza[i] = yzka[i]*cosP - yzkb[i]*sinP;
            yzb[i] = yzka[i]*sinP + yzkb[i]*cosP;
            gyz.x += yza[i]; gyz.y += yzb[i];
            
            xxa[i] = xxka[i]*cosP - xxkb[i]*sinP;
            xxb[i] = xxka[i]*sinP + xxkb[i]*cosP;
            gxx.x += xxa[i]; gxx.y += xxb[i];
            
            yya[i] = yyka[i]*cosP - yykb[i]*sinP;
            yyb[i] = yyka[i]*sinP + yykb[i]*cosP;
            gyy.x += yya[i]; gyy.y += yyb[i];
            
            zza[i] = zzka[i]*cosP - zzkb[i]*sinP;
            zzb[i] = zzka[i]*sinP + zzkb[i]*cosP;
            gzz.x += zza[i]; gzz.y += zzb[i];
            xxxa[i] = xxxka[i]*cosP - xxxkb[i]*sinP;
            
            xxxb[i] = xxxka[i]*sinP + xxxkb[i]*cosP;
            gxxx.x += xxxa[i]; gxxx.y += xxxb[i];
            yyya[i] = yyyka[i]*cosP - yyykb[i]*sinP;
            yyyb[i] = yyyka[i]*sinP + yyykb[i]*cosP;
            gyyy.x += yyya[i]; gyyy.y += yyyb[i];
            zzza[i] = zzzka[i]*cosP - zzzkb[i]*sinP;
            zzzb[i] = zzzka[i]*sinP + zzzkb[i]*cosP;
            gzzz.x += zzza[i]; gzzz.y += zzzb[i];
            xyya[i] = xyyka[i]*cosP - xyykb[i]*sinP;
            xyyb[i] = xyyka[i]*sinP + xyykb[i]*cosP;
            gxyy.x += xyya[i]; gxyy.y += xyyb[i];
            xzza[i] = xzzka[i]*cosP - xzzkb[i]*sinP;
            xzzb[i] = xzzka[i]*sinP + xzzkb[i]*cosP;
            gxzz.x += xzza[i]; gxzz.y += xzzb[i];
            yxxa[i] = yxxka[i]*cosP - yxxkb[i]*sinP;
            yxxb[i] = yxxka[i]*sinP + yxxkb[i]*cosP;
            gyxx.x += yxxa[i]; gyxx.y += yxxb[i];
            yzza[i] = yzzka[i]*cosP - yzzkb[i]*sinP;
            yzzb[i] = yzzka[i]*sinP + yzzkb[i]*cosP;
            gyzz.x += yzza[i]; gyzz.y += yzzb[i];
            zxxa[i] = zxxka[i]*cosP - zxxkb[i]*sinP;
            zxxb[i] = zxxka[i]*sinP + zxxkb[i]*cosP;
            gzxx.x += zxxa[i]; gzxx.y += zxxb[i];
            zyya[i] = zyyka[i]*cosP - zyykb[i]*sinP;
            zyyb[i] = zyyka[i]*sinP + zyykb[i]*cosP;
            gzyy.x += zyya[i]; gzyy.y += zyyb[i];

        }
    }
    
    public double evalGorkovLaplacian(){  
        return  K1 * (
                  gx.dot(gx) + pre.dot(gxx) + gy.dot(gy) + pre.dot(gyy) + gz.dot(gz) + pre.dot(gzz)) 
                - K2*
                ( gxx.dot(gxx) + gx.dot(gxxx) + gxy.dot(gxy) + gy.dot(gyxx) + gxz.dot(gxz) + gz.dot(gzxx) +
                  gxy.dot(gxy) + gx.dot(gxyy) + gyy.dot(gyy) + gy.dot(gyyy) + gyz.dot(gyz) + gz.dot(gzyy) + 
                  gxz.dot(gxz) + gx.dot(gxzz) + gyz.dot(gyz) + gy.dot(gyzz) + gzz.dot(gzz) + gz.dot(gzzz));
    }

    public double evalGorkovLaplacianX(){  
        return  K1 * (
                  gx.dot(gx) + pre.dot(gxx)) 
                - K2*
                ( gxx.dot(gxx) + gx.dot(gxxx) +
                  gxy.dot(gxy) + gx.dot(gxyy) + 
                  gxz.dot(gxz) + gx.dot(gxzz) );
    }
    public double evalGorkovLaplacianY(){  
        return  K1 * (
                  gy.dot(gy) + pre.dot(gyy)) 
                - K2*
                ( gxy.dot(gxy) + gy.dot(gyxx) +
                  gyy.dot(gyy) + gy.dot(gyyy) + 
                  gyz.dot(gyz) + gy.dot(gyzz) );
    }
    public double evalGorkovLaplacianZ(){  
        return  K1 * (
                  gz.dot(gz) + pre.dot(gzz)) 
                - K2*
                ( gxz.dot(gxz) + gz.dot(gzxx) +
                  gyz.dot(gyz) + gz.dot(gzyy) + 
                  gzz.dot(gzz) + gz.dot(gzzz));
    }
    public void gradientGorkovLaplacian(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * ( 
                    gx.x*-xb[i] + -xb[i]*gx.x + gx.y*xa[i] + xa[i]*gx.y +
                    pre.x*-xxb[i] + -b[i]*gxx.x + pre.y*xxa[i] + a[i]*gxx.y +
                    gy.x*-yb[i] + -yb[i]*gy.x + gy.y*ya[i] + ya[i]*gy.y +
                    pre.x*-yyb[i] + -b[i]*gyy.x + pre.y*yya[i] + a[i]*gyy.y +
                    gz.x*-zb[i] + -zb[i]*gz.x + gz.y*za[i] + za[i]*gz.y +
                    pre.x*-zzb[i] + -b[i]*gzz.x + pre.y*zza[i] + a[i]*gzz.y ) - 
                   K2 * ( 
                    gxx.x*-xxb[i] + -xxb[i]*gxx.x + gxx.y*xxa[i] + xxa[i]*gxx.y +
                    gx.x*-xxxb[i] + -xb[i]*gxxx.x + gx.y*xxxa[i] + xa[i]*gxxx.y +
                    gxy.x*-xyb[i] + -xyb[i]*gxy.x + gxy.y*xya[i] + xya[i]*gxy.y +
                    gy.x*-yxxb[i] + -yb[i]*gyxx.x + gy.y*yxxa[i] + ya[i]*gyxx.y +
                    gxz.x*-xzb[i] + -xzb[i]*gxz.x + gxz.y*xza[i] + xza[i]*gxz.y +
                    gz.x*-zxxb[i] + -zb[i]*gzxx.x + gz.y*zxxa[i] + za[i]*gzxx.y +
                    gxy.x*-xyb[i] + -xyb[i]*gxy.x + gxy.y*xya[i] + xya[i]*gxy.y +
                    gx.x*-xyyb[i] + -xb[i]*gxyy.x + gx.y*xyya[i] + xa[i]*gxyy.y +
                    gyy.x*-yyb[i] + -yyb[i]*gyy.x + gyy.y*yya[i] + yya[i]*gyy.y +
                    gy.x*-yyyb[i] + -yb[i]*gyyy.x + gy.y*yyya[i] + ya[i]*gyyy.y +
                    gyz.x*-yzb[i] + -yzb[i]*gyz.x + gyz.y*yza[i] + yza[i]*gyz.y +
                    gz.x*-zyyb[i] + -zb[i]*gzyy.x + gz.y*zyya[i] + za[i]*gzyy.y +
                    gxz.x*-xzb[i] + -xzb[i]*gxz.x + gxz.y*xza[i] + xza[i]*gxz.y +
                    gx.x*-xzzb[i] + -xb[i]*gxzz.x + gx.y*xzza[i] + xa[i]*gxzz.y +
                    gyz.x*-yzb[i] + -yzb[i]*gyz.x + gyz.y*yza[i] + yza[i]*gyz.y +
                    gy.x*-yzzb[i] + -yb[i]*gyzz.x + gy.y*yzza[i] + ya[i]*gyzz.y +
                    gzz.x*-zzb[i] + -zzb[i]*gzz.x + gzz.y*zza[i] + zza[i]*gzz.y +
                    gz.x*-zzzb[i] + -zb[i]*gzzz.x + gz.y*zzza[i] + za[i]*gzzz.y);
        }
        
        
    }
    
    public void gradientGorkovLaplacianX(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * ( 
                    gx.x*-xb[i] + -xb[i]*gx.x + gx.y*xa[i] + xa[i]*gx.y +
                    pre.x*-xxb[i] + -b[i]*gxx.x + pre.y*xxa[i] + a[i]*gxx.y) - 
                   K2 * ( 
                    gxx.x*-xxb[i] + -xxb[i]*gxx.x + gxx.y*xxa[i] + xxa[i]*gxx.y +
                    gx.x*-xxxb[i] + -xb[i]*gxxx.x + gx.y*xxxa[i] + xa[i]*gxxx.y +
                    gxy.x*-xyb[i] + -xyb[i]*gxy.x + gxy.y*xya[i] + xya[i]*gxy.y +
                    gx.x*-xyyb[i] + -xb[i]*gxyy.x + gx.y*xyya[i] + xa[i]*gxyy.y +
                    gxz.x*-xzb[i] + -xzb[i]*gxz.x + gxz.y*xza[i] + xza[i]*gxz.y +
                    gx.x*-xzzb[i] + -xb[i]*gxzz.x + gx.y*xzza[i] + xa[i]*gxzz.y);
        }
    }
        
    public void gradientGorkovLaplacianY(double[] g){
        for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * ( 
                    gy.x*-yb[i] + -yb[i]*gy.x + gy.y*ya[i] + ya[i]*gy.y +
                    pre.x*-yyb[i] + -b[i]*gyy.x + pre.y*yya[i] + a[i]*gyy.y ) - 
                   K2 * ( 
                    gxy.x*-xyb[i] + -xyb[i]*gxy.x + gxy.y*xya[i] + xya[i]*gxy.y +
                    gy.x*-yxxb[i] + -yb[i]*gyxx.x + gy.y*yxxa[i] + ya[i]*gyxx.y +
                    gyy.x*-yyb[i] + -yyb[i]*gyy.x + gyy.y*yya[i] + yya[i]*gyy.y +
                    gy.x*-yyyb[i] + -yb[i]*gyyy.x + gy.y*yyya[i] + ya[i]*gyyy.y +
                    gyz.x*-yzb[i] + -yzb[i]*gyz.x + gyz.y*yza[i] + yza[i]*gyz.y +
                    gy.x*-yzzb[i] + -yb[i]*gyzz.x + gy.y*yzza[i] + ya[i]*gyzz.y);
        }
        

    }
            
     public void gradientGorkovLaplacianZ(double[] g){
                for(int i = 0; i < nTrans; ++i){
            g[i] = K1 * (
                    gz.x*-zb[i] + -zb[i]*gz.x + gz.y*za[i] + za[i]*gz.y +
                    pre.x*-zzb[i] + -b[i]*gzz.x + pre.y*zza[i] + a[i]*gzz.y ) - 
                   K2 * ( 
                    gxz.x*-xzb[i] + -xzb[i]*gxz.x + gxz.y*xza[i] + xza[i]*gxz.y +
                    gz.x*-zxxb[i] + -zb[i]*gzxx.x + gz.y*zxxa[i] + za[i]*gzxx.y +
                    gyz.x*-yzb[i] + -yzb[i]*gyz.x + gyz.y*yza[i] + yza[i]*gyz.y +
                    gz.x*-zyyb[i] + -zb[i]*gzyy.x + gz.y*zyya[i] + za[i]*gzyy.y +
                    gzz.x*-zzb[i] + -zzb[i]*gzz.x + gzz.y*zza[i] + zza[i]*gzz.y +
                    gz.x*-zzzb[i] + -zb[i]*gzzz.x + gz.y*zzza[i] + za[i]*gzzz.y);
        }
    }
    
    //</editor-fold>
   
    public void reset(){
        pre.reset();
        gx.reset(); gy.reset(); gz.reset();
        gxy.reset(); gxz.reset(); gyz.reset();
        gxx.reset(); gyy.reset(); gzz.reset();
        gxxx.reset();gyyy.reset();gzzz.reset();
        gxyy.reset();gxzz.reset();
        gyxx.reset();gyzz.reset();
        gzxx.reset();gzyy.reset();
        swap.reset(); tmp.reset();
        diffVec.reset(); diffVecS.reset();
        tPos.reset();
        nor.reset();
    }
    
    public static float calcDir(float tx, float ty, float tz,final Vector3f n, float k){
        final float dot = tx*n.x + ty*n.y + tz*n.z;
        final float d2 = tx*tx + ty*ty + tz*tz;
        //final float sinAngle = FastMath.sqrt(1.0f - (dot*dot / d2) );
        final float angle = M.acos( dot / n.length() / M.sqrt(d2) );
        final float sinAngle = M.sin( angle );
        //System.out.println( "angle " + sinAngle);
        //return 1.0f - 0.25f*sinAngle - 0.6f*sinAngle*sinAngle;
        
        //return FastMath.sinc( 0.5f * k * 0.077f * sinAngle);
        
        final float dum = k * (0.009f/2.0f) * sinAngle;
        return (float)M.j1(dum) / dum;
        
        //return 1.0f / (1.0f + 0.0f*sinAngle + 128.0f*sinAngle*sinAngle);
    }
        
    public void initFieldConstantsOnlyForAmp(Renderer r){
        pre.reset();
        swap.reset(); tmp.reset();
        diffVec.reset(); diffVecS.reset();
        tPos.reset();
        nor.reset();
        
        final FloatBuffer positions = r.getPositions();
        final FloatBuffer specs = r.getSpecs();
        final FloatBuffer normals = r.getNormals();
       
        for(int i = 0; i < nTotalTrans; ++i){
            final int i3 = i*3;
            final int i4 = i*4;
            final float x,y,z;
            
            x = positions.get(i3+0); y = positions.get(i3+1); z = positions.get(i3+2);
            tPos.set(x , y , z);
            nor.set( normals.get(i3+0), normals.get(i3+1), normals.get(i3+2) );
            position.subtract(tPos, diffVec);
            float d = diffVec.length();
          
            float k = specs.get(i4 + 0);
            float kd = k*d;
            float cosKD = M.cos(kd);
            float sinKD = M.sin(kd);

            diffVecS.set(diffVec).multLocal(diffVec);
            float amp = specs.get(i4 + 1);
 
            //0 derivative
            swap.x =  cosKD;
            swap.y =  sinKD;
            swap.multLocal( amp / d );
            ka[i] = swap.x; kb[i] = swap.y;

            //directivity
            if (useDirectivity) {
                final float dir = calcDir(x, y, z, nor, k);
                ka[i] *= dir; kb[i] *= dir;
            }
                        
        }
        
        if(isReflector){
            for(int i = 0; i < nTrans; ++i){
                ka[i] +=  ka[i + nTrans]; 
                kb[i] +=  kb[i + nTrans];
            }
        }
        
    }
    
    public void initFieldConstants(final MainForm mf){
        reset();
        
        final Renderer r = mf.renderer;
        final Vector2f gorkovConsts = new Vector2f();
        
        CalcField.calcGorkovConstants(mf.scene.getParticleRadious() , mf, gorkovConsts);
        M1 = gorkovConsts.x;
        M2 = gorkovConsts.y;
        
        final FloatBuffer positions = r.getPositions();
        final FloatBuffer specs = r.getSpecs();
        final FloatBuffer normals = r.getNormals();
                
        for(int i = 0; i < nTotalTrans; ++i){
            final int i3 = i*3;
            final int i4 = i*4;
            final float x,y,z;
            
            x = positions.get(i3+0); y = positions.get(i3+1); z = positions.get(i3+2);
            tPos.set(x , y , z);
            nor.set( normals.get(i3+0), normals.get(i3+1), normals.get(i3+2) );
            position.subtract(tPos, diffVec);
            float d = diffVec.length();
            float d2 = d*d;
            float d3 = d2*d;
            float d5 = d2*d3;
            float d7 = d5*d2;
          
            float k = specs.get(i4 + 0);
            float kd = k*d;
            float k2 = k*k;
            float d2k2 = d2 * k2;
            float cosKD = M.cos(kd);
            float sinKD = M.sin(kd);
            float KDcosKD = kd * cosKD;
            float KDsinKD = kd * sinKD;

            diffVecS.set(diffVec).multLocal(diffVec);
            float amp = specs.get(i4 + 1);
 
            //0 derivative
            swap.x =  cosKD;
            swap.y =  sinKD;
            swap.multLocal( amp / d );
            ka[i] = swap.x; kb[i] = swap.y;
            
            //1 derivatives
            tmp.x = -(cosKD+KDsinKD);
            tmp.y = KDcosKD-sinKD;
            tmp.multLocal( amp / d3 );
            tmp.mult( diffVec.x, swap);
            xka[i] = swap.x; xkb[i] = swap.y;       
            tmp.mult( diffVec.y, swap);
            yka[i] = swap.x; ykb[i] = swap.y;         
            tmp.mult( diffVec.z, swap);
            zka[i] = swap.x; zkb[i] = swap.y;
                    
            //1,1 derivatives
            swap.x = 3.0f-d2k2;
            swap.y = 3.0f;
            tmp.x =  swap.x*cosKD+swap.y*KDsinKD;
            tmp.y = -(swap.y*KDcosKD-swap.x*sinKD);
            tmp.multLocal(amp / d5);
            tmp.mult( diffVec.x * diffVec.y, swap);
            xyka[i] = swap.x; xykb[i] = swap.y;         
            tmp.mult( diffVec.x * diffVec.z, swap);
            xzka[i] = swap.x; xzkb[i] = swap.y;          
            tmp.mult( diffVec.y * diffVec.z, swap);
            yzka[i] = swap.x; yzkb[i] = swap.y;
            
            //2 derivatives
            tmp.x = d2 + (d2k2 - 3.0f) * diffVecS.x;
            tmp.y = d2 - 3.0f * diffVecS.x;
            swap.x = - (tmp.x*cosKD+tmp.y*KDsinKD);
            swap.y =  tmp.y*KDcosKD-tmp.x*sinKD;
            swap.multLocal(amp / d5);
            xxka[i] = swap.x; xxkb[i] = swap.y;
                     
            tmp.x = d2 + (d2k2 - 3.0f) * diffVecS.y;
            tmp.y = d2 - 3.0f * diffVecS.y;
            swap.x = - (tmp.x*cosKD+tmp.y*KDsinKD);
            swap.y =  tmp.y*KDcosKD-tmp.x*sinKD;
            swap.multLocal(amp / d5);
            yyka[i] = swap.x; yykb[i] = swap.y;
                       
            tmp.x = d2 + (d2k2 - 3.0f) * diffVecS.z;
            tmp.y = d2 - 3.0f * diffVecS.z;
            swap.x = - (tmp.x*cosKD+tmp.y*KDsinKD);
            swap.y =  tmp.y*KDcosKD-tmp.x*sinKD;
            swap.multLocal(amp / d5);
            zzka[i] = swap.x; zzkb[i] = swap.y;
                       
            //1,2 derivatives
            swap.x = -15.0f*diffVecS.x + d2*(3.0f-k2*(d2-6.0f*diffVecS.x));
            swap.y = -15.0f*diffVecS.x + d2*(3.0f + k2*diffVecS.x);
            tmp.x = amp / d7 * (swap.x*cosKD+swap.y*KDsinKD);
            tmp.y = amp / d7 * -(swap.y*KDcosKD-swap.x*sinKD);
            yxxka[i] = tmp.x * diffVec.y; yxxkb[i] = tmp.y * diffVec.y;          
            zxxka[i] = tmp.x * diffVec.z; zxxkb[i] = tmp.y * diffVec.z;          
            swap.x = -15.0f*diffVecS.y + d2*(3.0f-k2*(d2-6.0f*diffVecS.y));
            swap.y = -15.0f*diffVecS.y + d2*(3.0f + k2*diffVecS.y);
            tmp.x = amp / d7 * (swap.x*cosKD+swap.y*KDsinKD);
            tmp.y = amp / d7 * -(swap.y*KDcosKD-swap.x*sinKD);
            xyyka[i] = tmp.x * diffVec.x; xyykb[i] = tmp.y * diffVec.x;           
            zyyka[i] = tmp.x * diffVec.z; zyykb[i] = tmp.y * diffVec.z;          
            swap.x = -15.0f*diffVecS.z + d2*(3.0f-k2*(d2-6.0f*diffVecS.z));
            swap.y = -15.0f*diffVecS.z + d2*(3.0f + k2*diffVecS.z);
            tmp.x = amp / d7 * (swap.x*cosKD+swap.y*KDsinKD);
            tmp.y = amp / d7 * -(swap.y*KDcosKD-swap.x*sinKD);
            xzzka[i] = tmp.x * diffVec.x; xzzkb[i] = tmp.y * diffVec.x;
            yzzka[i] = tmp.x * diffVec.y; yzzkb[i] = tmp.y * diffVec.y;
            
            //3 derivatives
            tmp.x = -3.0f * (5.0f *diffVecS.x + d2 * (k2*(d2 - 2.0f*diffVecS.x)-3.0f));
            tmp.y = -15.0f * diffVecS.x + d2*(9.0f +k2*diffVecS.x);
            swap.x = amp / d7 * diffVec.x * (tmp.x*cosKD+tmp.y*KDsinKD);
            swap.y = amp / d7 * diffVec.x * -(tmp.y*KDcosKD-tmp.x*sinKD);
            xxxka[i] = swap.x; xxxkb[i] = swap.y;
            
            tmp.x = -3.0f * (5.0f *diffVecS.y + d2 * (k2*(d2 - 2.0f*diffVecS.y)-3.0f));
            tmp.y = -15.0f * diffVecS.y + d2*(9.0f +k2*diffVecS.y);
            swap.x = amp / d7 * diffVec.y * (tmp.x*cosKD+tmp.y*KDsinKD);
            swap.y = amp / d7 * diffVec.y * -(tmp.y*KDcosKD-tmp.x*sinKD);
            yyyka[i] = swap.x; yyykb[i] = swap.y;
            
            tmp.x = -3.0f * (5.0f *diffVecS.z + d2 * (k2*(d2 - 2.0f*diffVecS.z)-3.0f));
            tmp.y = -15.0f * diffVecS.z + d2*(9.0f +k2*diffVecS.z);
            swap.x = amp / d7 * diffVec.z * (tmp.x*cosKD+tmp.y*KDsinKD);
            swap.y = amp / d7 * diffVec.z * -(tmp.y*KDcosKD-tmp.x*sinKD);
            zzzka[i] = swap.x; zzzkb[i] = swap.y;
            
            //directivity
            if (useDirectivity) {
                final float dir = calcDir(x, y, z, nor, k);
                final float dirX = (calcDir(x + h, y, z, nor, k) - calcDir(x - h, y, z, nor, k)) / (2.0f * h);
                final float dirY = (calcDir(x, y + h, z, nor, k) - calcDir(x, y - h, z, nor, k)) / (2.0f * h);
                final float dirZ = (calcDir(x, y, z + h, nor, k) - calcDir(x, y, z - h, nor, k)) / (2.0f * h);
                final float dirXY
                        = ((calcDir(x + h, y + h, z, nor, k) - calcDir(x - h, y + h, z, nor, k))
                        - (calcDir(x + h, y - h, z, nor, k) - calcDir(x - h, y - h, z, nor, k))) / (4.0f * h * h);
                final float dirXZ
                        = ((calcDir(x + h, y, z + h, nor, k) - calcDir(x - h, y, z + h, nor, k))
                        - (calcDir(x + h, y, z - h, nor, k) - calcDir(x - h, y, z - h, nor, k))) / (4.0f * h * h);
                final float dirYZ
                        = ((calcDir(x, y + h, z + h, nor, k) - calcDir(x, y - h, z + h, nor, k))
                        - (calcDir(x, y + h, z - h, nor, k) - calcDir(x, y - h, z - h, nor, k))) / (4.0f * h * h);
                final float dirXX
                        = (calcDir(x + h, y, z, nor, k) - 2.0f * calcDir(x, y, z, nor, k) + calcDir(x - h, y, z, nor, k))
                        / (4.0f * h * h);
                final float dirYY
                        = (calcDir(x, y + h, z, nor, k) - 2.0f * calcDir(x, y, z, nor, k) + calcDir(x, y - h, z, nor, k))
                        / (4.0f * h * h);
                final float dirZZ
                        = (calcDir(x, y, z + h, nor, k) - 2.0f * calcDir(x, y, z, nor, k) + calcDir(x, y, z - h, nor, k))
                        / (4.0f * h * h);
                final float dirYXX
                        = ((calcDir(x + h, y + h, z, nor, k) - 2.0f * calcDir(x, y + h, z, nor, k) + calcDir(x - h, y + h, z, nor, k))
                        - (calcDir(x + h, y - h, z, nor, k) - 2.0f * calcDir(x, y - h, z, nor, k) + calcDir(x - h, y - h, z, nor, k)))
                        / (8.0f * h * h * h);
                final float dirZXX
                        = ((calcDir(x + h, y, z + h, nor, k) - 2.0f * calcDir(x, y, z + h, nor, k) + calcDir(x - h, y, z + h, nor, k))
                        - (calcDir(x + h, y, z - h, nor, k) - 2.0f * calcDir(x, y, z - h, nor, k) + calcDir(x - h, y, z - h, nor, k)))
                        / (8.0f * h * h * h);
                final float dirXYY
                        = ((calcDir(x + h, y + h, z, nor, k) - 2.0f * calcDir(x + h, y, z, nor, k) + calcDir(x + h, y - h, z, nor, k))
                        - (calcDir(x - h, y + h, z, nor, k) - 2.0f * calcDir(x - h, y, z, nor, k) + calcDir(x - h, y - h, z, nor, k)))
                        / (8.0f * h * h * h);
                final float dirZYY
                        = ((calcDir(x, y + h, z + h, nor, k) - 2.0f * calcDir(x, y, z + h, nor, k) + calcDir(x, y - h, z + h, nor, k))
                        - (calcDir(x, y + h, z - h, nor, k) - 2.0f * calcDir(x, y, z - h, nor, k) + calcDir(x, y - h, z - h, nor, k)))
                        / (8.0f * h * h * h);
                final float dirXZZ
                        = ((calcDir(x + h, y, z + h, nor, k) - 2.0f * calcDir(x + h, y, z, nor, k) + calcDir(x + h, y, z - h, nor, k))
                        - (calcDir(x - h, y, z + h, nor, k) - 2.0f * calcDir(x - h, y, z, nor, k) + calcDir(x - h, y, z - h, nor, k)))
                        / (8.0f * h * h * h);
                final float dirYZZ
                        = ((calcDir(x, y + h, z + h, nor, k) - 2.0f * calcDir(x, y + h, z, nor, k) + calcDir(x, y + h, z - h, nor, k))
                        - (calcDir(x, y - h, z + h, nor, k) - 2.0f * calcDir(x, y - h, z, nor, k) + calcDir(x, y - h, z - h, nor, k)))
                        / (8.0f * h * h * h);
                final float dirXXX
                        = (calcDir(x + 2 * h, y, z, nor, k) - calcDir(x + h, y, z, nor, k)
                        + calcDir(x - h, y, z, nor, k) - calcDir(x - 2 * h, y, z, nor, k))
                        / (8.0f * h * h * h);
                final float dirYYY
                        = (calcDir(x, y + 2 * h, z, nor, k) - calcDir(x, y + h, z, nor, k)
                        + calcDir(x, y - h, z, nor, k) - calcDir(x, y - 2 * h, z, nor, k))
                        / (8.0f * h * h * h);
                final float dirZZZ
                        = (calcDir(x, y, z + 2 * h, nor, k) - calcDir(x, y, z + h, nor, k)
                        + calcDir(x, y, z - h, nor, k) - calcDir(x, y, z - 2 * h, nor, k))
                        / (8.0f * h * h * h);
                
                xxxka[i] = ka[i]*dirXXX + xxxka[i]*dir + 3*(xka[i]*dirXX + xxka[i]*dirX); 
                xxxkb[i] = kb[i]*dirXXX + xxxkb[i]*dir + 3*(xkb[i]*dirXX + xxkb[i]*dirX); 
                yyyka[i] = ka[i]*dirYYY + yyyka[i]*dir + 3*(yka[i]*dirYY + yyka[i]*dirY);
                yyykb[i] = kb[i]*dirYYY + yyykb[i]*dir + 3*(ykb[i]*dirYY + yykb[i]*dirY); 
                zzzka[i] = ka[i]*dirZZZ + xxxka[i]*dir + 3*(zka[i]*dirZZ + zzka[i]*dirZ);
                zzzkb[i] = kb[i]*dirZZZ + xxxkb[i]*dir + 3*(zkb[i]*dirZZ + zzkb[i]*dirZ); 
                
                yxxka[i] = ka[i]*dirYXX + yxxka[i]*dir + 3*(xka[i]*dirXX + xxka[i]*dirX); 
                yxxkb[i] = kb[i]*dirYXX + yxxkb[i]*dir + 3*(xkb[i]*dirXX + xxkb[i]*dirX); 
                zxxka[i] = ka[i]*dirZXX + zxxka[i]*dir + 3*(xka[i]*dirXX + xxka[i]*dirX); 
                zxxkb[i] = kb[i]*dirZXX + zxxkb[i]*dir + 3*(xkb[i]*dirXX + xxkb[i]*dirX);
                xyyka[i] = ka[i]*dirXYY + xyyka[i]*dir + 3*(yka[i]*dirYY + yyka[i]*dirY); 
                xyykb[i] = kb[i]*dirXYY + xyykb[i]*dir + 3*(ykb[i]*dirYY + yykb[i]*dirY);
                zyyka[i] = ka[i]*dirZYY + zyyka[i]*dir + 3*(yka[i]*dirYY + yyka[i]*dirY); 
                zyykb[i] = kb[i]*dirZYY + zyykb[i]*dir + 3*(ykb[i]*dirYY + yykb[i]*dirY);
                xzzka[i] = ka[i]*dirXZZ + xzzka[i]*dir + 3*(zka[i]*dirZZ + zzka[i]*dirZ); 
                xzzkb[i] = kb[i]*dirXZZ + xzzkb[i]*dir + 3*(zkb[i]*dirZZ + zzkb[i]*dirZ);
                yzzka[i] = ka[i]*dirYZZ + yzzka[i]*dir + 3*(zka[i]*dirZZ + zzka[i]*dirZ); 
                yzzkb[i] = kb[i]*dirYZZ + yzzkb[i]*dir + 3*(zkb[i]*dirZZ + zzkb[i]*dirZ);
                
                xxka[i] = ka[i]*dirXX + 2*xka[i]*dirX + xxka[i]*dir; 
                xxkb[i] = kb[i]*dirXX + 2*xkb[i]*dirX + xxkb[i]*dir;
                yyka[i] = ka[i]*dirYY + 2*yka[i]*dirY + yyka[i]*dir; 
                yykb[i] = kb[i]*dirYY + 2*ykb[i]*dirY + yykb[i]*dir;
                zzka[i] = ka[i]*dirZZ + 2*zka[i]*dirZ + zzka[i]*dir;
                zzkb[i] = kb[i]*dirZZ + 2*zkb[i]*dirZ + zzkb[i]*dir;
                xyka[i] = ka[i]*dirXY + 2*yka[i]*dirY + xyka[i]*dir; 
                xykb[i] = kb[i]*dirXY + 2*ykb[i]*dirY + xykb[i]*dir; 
                xzka[i] = ka[i]*dirXZ + 2*zka[i]*dirZ + xzka[i]*dir; 
                xzkb[i] = kb[i]*dirXZ + 2*zkb[i]*dirZ + xzkb[i]*dir;
                yzka[i] = ka[i]*dirYZ + 2*zka[i]*dirZ + yzka[i]*dir; 
                yzkb[i] = kb[i]*dirYZ + 2*zkb[i]*dirZ + yzkb[i]*dir;

                xka[i] = ka[i]*dirX + xka[i]*dir; 
                xkb[i] = kb[i]*dirX + xkb[i]*dir;
                yka[i] = ka[i]*dirY + yka[i]*dir; 
                ykb[i] = kb[i]*dirY + ykb[i]*dir;
                zka[i] = ka[i]*dirZ + zka[i]*dir; 
                zkb[i] = kb[i]*dirZ + zkb[i]*dir;
                
                ka[i] *= dir; kb[i] *= dir;
            }
                        
        }
        
        if(isReflector){
            for(int i = 0; i < nTrans; ++i){
                ka[i] +=  ka[i + nTrans]; 
                kb[i] +=  kb[i + nTrans];
                
                xka[i] +=  xka[i + nTrans]; 
                xkb[i] +=  xkb[i + nTrans];
                yka[i] +=  yka[i + nTrans]; 
                ykb[i] +=  ykb[i + nTrans];
                zka[i] +=  zka[i + nTrans]; 
                zkb[i] +=  zkb[i + nTrans];
                
                xyka[i] +=  xyka[i + nTrans]; 
                xykb[i] +=  xykb[i + nTrans];
                xzka[i] +=  xzka[i + nTrans]; 
                xzkb[i] +=  xzkb[i + nTrans];
                yzka[i] +=  yzka[i + nTrans]; 
                yzkb[i] +=  yzkb[i + nTrans];
                xxka[i] +=  xxka[i + nTrans]; 
                xxkb[i] +=  xxkb[i + nTrans];
                yyka[i] +=  yyka[i + nTrans]; 
                yykb[i] +=  yykb[i + nTrans];
                zzka[i] +=  zzka[i + nTrans]; 
                zzkb[i] +=  zzkb[i + nTrans];
                
                yxxka[i] +=  yxxka[i + nTrans]; 
                yxxkb[i] +=  yxxkb[i + nTrans];
                zxxka[i] +=  zxxka[i + nTrans]; 
                zxxkb[i] +=  zxxkb[i + nTrans];
                xyyka[i] +=  xyyka[i + nTrans]; 
                xyykb[i] +=  xyykb[i + nTrans];
                zyyka[i] +=  zyyka[i + nTrans]; 
                zyykb[i] +=  zyykb[i + nTrans];
                xzzka[i] +=  xzzka[i + nTrans]; 
                xzzkb[i] +=  xzzkb[i + nTrans];
                yzzka[i] +=  yzzka[i + nTrans]; 
                yzzkb[i] +=  yzzkb[i + nTrans];
                xxxka[i] +=  xxxka[i + nTrans]; 
                xxxkb[i] +=  xxxkb[i + nTrans];
                yyyka[i] +=  yyyka[i + nTrans]; 
                yyykb[i] +=  yyykb[i + nTrans];
                zzzka[i] +=  zzzka[i + nTrans]; 
                zzzkb[i] +=  zzzkb[i + nTrans];
            }
        }
        
        
        
        K1 = M1 * 2.0f;
        K2 = M2 * 2.0f;
    }
}
