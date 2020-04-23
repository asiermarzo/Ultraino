package acousticfield3d.renderer;

import acousticfield3d.utils.Color;

/**
 *
 * @author Asier
 */
public class Material {
    public float ambient;
    public float diffuse;
    public float specular;
    public float shininess;

    public Material() {
        ambient = 0.5f;
        diffuse = 0.6f;
        specular = 0.7f;
        shininess = 15;
    }

    public float getAmbient() {
        return ambient;
    }

    public void setAmbient(float ambient) {
        this.ambient = ambient;
    }

    public float getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(float diffuse) {
        this.diffuse = diffuse;
    }

    public float getSpecular() {
        return specular;
    }

    public void setSpecular(float specular) {
        this.specular = specular;
    }

    
    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public void set(Material material) {
        ambient = material.ambient;
        diffuse = material.diffuse;
        shininess = material.shininess;
        specular = material.specular;
    }
    
}
