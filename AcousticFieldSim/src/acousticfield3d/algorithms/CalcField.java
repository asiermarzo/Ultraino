package acousticfield3d.algorithms;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.M;
import acousticfield3d.math.Vector2f;
import acousticfield3d.math.Vector3f;
import acousticfield3d.simulation.Simulation;
import acousticfield3d.simulation.Transducer;

/**
 *
 * @author am14010
 */
public class CalcField {
    final static float H_DIV = 64;
    
    public static Vector2f calcFieldAt(final Vector3f position, final MainForm mf){
        return calcFieldAt(position.x, position.y, position.z, mf);
    }
    
    public static Vector2f calcFieldAt(final float px, final float py, final float pz, final MainForm mf){
        final boolean discAmp = mf.miscPanel.isAmpDiscretizer();
        final boolean discPhase = mf.miscPanel.isPhaseDiscretizer();
        final float ampDiscStep = 1.0f / mf.miscPanel.getAmpDiscretization();
        final float phaseDiscStep = 1.0f / mf.miscPanel.getPhaseDiscretization();
        
        final Vector3f nor = new Vector3f();
        final Vector3f tPos = new Vector3f();
        final Vector3f diffVec = new Vector3f();
        
        final Vector2f field = new Vector2f();
       
        final float mSpeed = mf.simForm.getMediumSpeed();
        
        for(Transducer t : mf.simulation.transducers){

            tPos.set( t.getTransform().getTranslation() );
            t.getTransform().getRotation().mult( Vector3f.UNIT_Y, nor);
            diffVec.set(px, py, pz).subtractLocal(tPos);
            final float dist = diffVec.length();
            final float nn = nor.length();
            diffVec.divideLocal( dist );
          
            float angle = M.acos(diffVec.dot(nor) / nn);

            final float ap = t.getApperture();
            final float omega = M.TWO_PI * t.getFrequency();      // angular frequency
            final float k = omega / mSpeed;        // wavenumber
            float dum =  ap * 0.5f * k * M.sin( angle );
            float directivity = M.sinc(dum);
                     
            float ampDirAtt = t.calcRealDiscAmplitude(discAmp, ampDiscStep ) * directivity / dist;
            float kdPlusPhase = k * dist + t.calcRealDiscPhase(discPhase, phaseDiscStep);
            field.x += ampDirAtt * M.cos(kdPlusPhase);
            field.y += ampDirAtt * M.sin(kdPlusPhase);
        }
        
        return field;
    }
    
    public static Vector2f calcFieldForTrans(final Transducer t, final float phase, final float px, final float py, final float pz, final Simulation s){
        final Vector3f nor = new Vector3f();
        final Vector3f tPos = new Vector3f();
        final Vector3f diffVec = new Vector3f();
        
        final Vector2f field = new Vector2f();
       
        final float mSpeed = s.getMediumSpeed();
        
        tPos.set(t.getTransform().getTranslation());
        t.getTransform().getRotation().mult(Vector3f.UNIT_Y, nor);
        diffVec.set(px, py, pz).subtractLocal(tPos);
        final float dist = diffVec.length();
        final float nn = nor.length();
        diffVec.divideLocal(dist);

        float angle = M.acos(diffVec.dot(nor) / nn);

        final float ap = t.getApperture();
        final float omega = M.TWO_PI * t.getFrequency();      // angular frequency
        final float k = omega / mSpeed;        // wavenumber
        float dum = ap * 0.5f * k * M.sin(angle);
        float directivity = M.sinc(dum);

        float ampDirAtt = 1 * directivity / dist;
        float kdPlusPhase = k * dist + phase;
        field.x = ampDirAtt * M.cos(kdPlusPhase);
        field.y = ampDirAtt * M.sin(kdPlusPhase);
    
        return field;
    }
    
    
    public static double calcFieldGradientDot(final float x, final float y, final float z,
            final float dx,final float dy,final float dz,
            final float h, final MainForm mf){
        
        final Vector2f N2 = calcFieldAt(x - dx*h*2, y - dy*h*2, z - dz*h*2, mf);
        final Vector2f N1 = calcFieldAt(x - dx*h*1, y - dy*h*1, z - dz*h*1, mf);
        final Vector2f P1 = calcFieldAt(x + dx*h*1, y + dy*h*1, z + dz*h*1, mf);
        final Vector2f P2 = calcFieldAt(x + dx*h*2, y + dy*h*2, z + dz*h*2, mf);
        
        final Vector2f total = new Vector2f();
        N2.multLocal(+1);
        N1.multLocal(-8);
        P1.multLocal(+8);
        P2.multLocal(-1);
        total.addLocal(N2).addLocal(N1).addLocal(P1).addLocal(P2).divideLocal( 12 * h);
        
        return total.dot(total);
    }
    
    public static void calcGorkovConstants(final float particleR, final MainForm mf, Vector2f consts){
        final float rohP = mf.simulation.getParticleDensity();
        final float roh = mf.simulation.getMediumDensity();
        final float cP = mf.simulation.getParticleSpeed();
        final float c = mf.simulation.getMediumSpeed();
        final float omega = mf.simulation.getFrequency() * M.TWO_PI;
        
        final float kapa = 1.0f / (roh * (c*c));
        final float kapa_p = 1.0f / (rohP * (cP*cP));
        final float k_tilda = kapa_p / kapa;
        final float f_1_bruus = 1.0f - k_tilda;
        
        final float roh_tilda = rohP / roh;
        final float f_2_bruus = (2.0f * (roh_tilda - 1.0f)) / ((2.0f * roh_tilda) + 1.0f);
        
        final float vkPreToVel = 1.0f / (roh*omega);
        final float vkPre = f_1_bruus*0.5f*kapa*0.5f;
        final float vkVel = f_2_bruus*(3.0f/4.0f)*roh*0.5f;
        final float vpVol = (4.0f/3.0f)*M.PI*(particleR*particleR*particleR);
        
        final double M1 = vpVol * vkPre;
        final double M2 = vpVol * vkVel*vkPreToVel*vkPreToVel;   
        
        consts.set((float)M1,(float)M2);
    }
    
    public static double calcGorkovAt(final float x, final float y, final float z, float particleR,  final MainForm mf){
        
        final Vector2f pre = calcFieldAt(x,y,z, mf);
        final float waveLength =  mf.simulation.getWavelenght();
        final double gx = calcFieldGradientDot(x,y,z, 1, 0, 0, waveLength / H_DIV, mf);
        final double gy = calcFieldGradientDot(x,y,z, 0, 1, 0, waveLength / H_DIV, mf);
        final double gz = calcFieldGradientDot(x,y,z, 0, 0, 1, waveLength / H_DIV, mf);
        final Vector2f consts = new Vector2f();
        
        calcGorkovConstants(particleR, mf, consts);
                
        return  consts.x * pre.dot(pre) - consts.y * (gx + gy + gz);
    }
    
    public static double calcGorkovGradient(final float x, final float y, final float z,
             final float dx,final float dy,final float dz, 
             final float h, final float particleR,
            final MainForm mf){
        
        final double N2 = calcGorkovAt(x - dx*h*2, y - dy*h*2, z - dz*h*2, particleR, mf);
        final double N1 = calcGorkovAt(x - dx*h*1, y - dy*h*1, z - dz*h*1, particleR, mf);
        final double P1 = calcGorkovAt(x + dx*h*1, y + dy*h*1, z + dz*h*1, particleR, mf);
        final double P2 = calcGorkovAt(x + dx*h*2, y + dy*h*2, z + dz*h*2, particleR, mf);
        
        return (N2 - 8*N1 + 8*P1 - P2) / (12*h);
    }
    
    public static Vector3f calcForceAt(final float x, final float y, final float z, final float particleR, final MainForm mf){
        final float waveLength =  mf.simulation.getWavelenght();
        
        final double fx = calcGorkovGradient(x,y,z,1,0,0, waveLength/H_DIV, particleR, mf);
        final double fy = calcGorkovGradient(x,y,z,0,1,0, waveLength/H_DIV, particleR, mf);
        final double fz = calcGorkovGradient(x,y,z,0,0,1, waveLength/H_DIV, particleR, mf);
        
        return new Vector3f( (float)-fx, (float)-fy, (float)-fz );
    }
        
    public static Vector3f calcForceGradients(final float x, final float y, final float z, final float particleR, final MainForm mf){
        final float waveLength =  mf.simulation.getWavelenght();
        
        final float h = waveLength/H_DIV;
        final Vector3f N2 = new Vector3f(
                -calcGorkovGradient(x-2*h,y,z,1,0,0, h, particleR, mf),
                -calcGorkovGradient(x,y-2*h,z,0,1,0, h, particleR, mf),
                -calcGorkovGradient(x,y,z-2*h,0,0,1, h, particleR, mf));
        final Vector3f N1 = new Vector3f(
                -calcGorkovGradient(x-h,y,z,1,0,0, h, particleR, mf),
                -calcGorkovGradient(x,y-h,z,0,1,0, h, particleR, mf),
                -calcGorkovGradient(x,y,z-h,0,0,1, h, particleR, mf));
        final Vector3f P1 = new Vector3f(
                -calcGorkovGradient(x+h,y,z,1,0,0, h, particleR, mf),
                -calcGorkovGradient(x,y+h,z,0,1,0, h, particleR, mf),
                -calcGorkovGradient(x,y,z+h,0,0,1, h, particleR, mf));
        final Vector3f P2 = new Vector3f(
                -calcGorkovGradient(x+2*h,y,z,1,0,0, h, particleR, mf),
                -calcGorkovGradient(x,y+2*h,z,0,1,0, h, particleR, mf),
                -calcGorkovGradient(x,y,z+2*h,0,0,1, h, particleR, mf));
        
        final Vector3f forceG = new Vector3f();
        N2.multLocal(+1);
        N1.multLocal(-8);
        P1.multLocal(+8);
        P2.multLocal(-1);
        forceG.addLocal(N2).addLocal(N1).addLocal(P1).addLocal(P2).divideLocal(12 * h);
        
        return new Vector3f( forceG.x, forceG.y, forceG.z );
    }
}
