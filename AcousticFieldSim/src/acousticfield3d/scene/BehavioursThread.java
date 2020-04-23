package acousticfield3d.scene;

import acousticfield3d.gui.MainForm;

/**
 *
 * @author Asier
 */
public class BehavioursThread extends Thread{
    private final MainForm form;
    private final Scene scene;
    float fps;
    
    public BehavioursThread(Scene scene, MainForm form) {
        fps = 30.0f;
        this.scene = scene;
        this.form = form;
    }

    @Override
    public void run() {
        double lastTime = getSeconds();
        
        while(! interrupted()){
            double t1 = getSeconds();
            float dt = (float)(t1 - lastTime);
            lastTime = t1;
            boolean needUpdate = false;
            
            for (Behaviour b : scene.getCamera().getBehaviours()) {
                needUpdate = true;
                b.tick(dt,  scene.getCamera());
            }
            for (Behaviour b : scene.getLight().getBehaviours()) {
                needUpdate = true;
                b.tick(dt, scene.getLight());
            }
            for(Entity e : scene.getEntities()){
                for(Behaviour b : e.getBehaviours()){
                    needUpdate = true;
                    b.tick(dt, e);
                }
            }
            
            if (needUpdate){
                form.needUpdate();
            }
            
            double sleepTime = 1.0/fps - (getSeconds() - t1);
            if(sleepTime > 0.0){
                try {
                    sleep( (long)(sleepTime * 1000.0));
                } catch (InterruptedException ex) {}
            }
        }
    }
    
    private static double getSeconds(){
        return System.currentTimeMillis() / 1000.0;
    }
}
