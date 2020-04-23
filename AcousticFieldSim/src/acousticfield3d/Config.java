package acousticfield3d;

import java.util.HashMap;

/**
 *
 * @author am14010
 */
public class Config {
    public String lastPath;
    public String lastSimFile;
   
    public int frameX, frameY, frameWidth, frameHeigh;
    public HashMap<String,String> guiValues = new HashMap<>();
    
    public Config() {
    }

    public int getFrameX() {
        return frameX;
    }

    public void setFrameX(int frameX) {
        this.frameX = frameX;
    }

    public int getFrameY() {
        return frameY;
    }

    public void setFrameY(int frameY) {
        this.frameY = frameY;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeigh() {
        return frameHeigh;
    }

    public void setFrameHeigh(int frameHeigh) {
        this.frameHeigh = frameHeigh;
    }

    
    public String getLastPath() {
        return lastPath;
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public String getLastSimFile() {
        return lastSimFile;
    }

    public void setLastSimFile(String lastSimFile) {
        this.lastSimFile = lastSimFile;
    }

    public HashMap<String, String> getGuiValues() {
        return guiValues;
    }

    public void setGuiValues(HashMap<String, String> guiValues) {
        this.guiValues = guiValues;
    }
    
    
    
}
