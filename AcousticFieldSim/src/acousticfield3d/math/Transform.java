/*
 * Copyright (c) 2009-2012 jMonkeyEngine
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
package acousticfield3d.math;

/**
 * Started Date: Jul 16, 2004<br><br>
 * Represents a translation, rotation and scale in one object.
 * 
 * @author Jack Lindamood
 * @author Joshua Slack
 */
public final class Transform implements Cloneable, java.io.Serializable {

    static final long serialVersionUID = 1;

    public static final Transform IDENTITY = new Transform();

    private Quaternion rot = new Quaternion();
    private Vector3f translation = new Vector3f();
    private Vector3f scale = new Vector3f(1,1,1);

    public static Transform create(){
        return new Transform();
    }
    
    public Transform(Vector3f translation, Quaternion rot){
        this.translation.set(translation);
        this.rot.set(rot);
    }
    
    public Transform(Vector3f translation, Quaternion rot, Vector3f scale){
        this(translation, rot);
        this.scale.set(scale);
    }

    public Transform(Vector3f translation){
        this(translation, Quaternion.IDENTITY);
    }

    public Transform(Quaternion rot){
        this(Vector3f.ZERO, rot);
    }

    public Transform(){
        this(Vector3f.ZERO, Quaternion.IDENTITY);
    }

    /**
     * Sets this rotation to the given Quaternion value.
     * @param rot The new rotation for this matrix.
     * @return this
     */
    public void setRotation(Quaternion rot) {
        this.rot.set(rot);
    }

    /**
     * Sets this translation to the given value.
     * @param trans The new translation for this matrix.
     * @return this
     */
    public void setTranslation(Vector3f trans) {
        this.translation.set(trans);
    }

    /**
     * Return the translation vector in this matrix.
     * @return translation vector.
     */
    public Vector3f getTranslation() {
        return translation;
    }

    /**
     * Sets this scale to the given value.
     * @param scale The new scale for this matrix.
     * @return this
     */
    public void setScale(Vector3f scale) {
        this.scale.set(scale);
    }

    /**
     * Sets this scale to the given value.
     * @param scale The new scale for this matrix.
     * @return this
     */
    public Transform setScale(float scale) {
        this.scale.set(scale, scale, scale);
        return this;
    }

    /**
     * Return the scale vector in this matrix.
     * @return scale vector.
     */
    public Vector3f getScale() {
        return scale;
    }

  
    
    /**
     * Return the rotation quaternion in this matrix.
     * @return rotation quaternion.
     */
    public Quaternion getRotation() {
        return rot;
    } 
    
    
    public void setLerp(Transform t1, Transform t2, float delta) {
        this.rot.slerp(t1.rot,t2.rot,delta);
        this.translation.interpolateLocal(t1.translation,t2.translation,delta);
        this.scale.interpolateLocal(t1.scale,t2.scale,delta);
    }

    /**
     * Changes the values of this matrix acording to it's parent.  Very similar to the concept of Node/Spatial transforms.
     * @param parent The parent matrix.
     * @return This matrix, after combining.
     */
    public Transform combineWithParent(Transform parent) {
        scale.multLocal(parent.scale);
        parent.rot.mult(rot, rot);

        translation.multLocal(parent.scale);
        parent
            .rot
            .multLocal(translation)
            .addLocal(parent.translation);
        return this;
    }

     public Transform combineWithParentNoScale(Transform parent) {
        parent.rot.mult(rot, rot);

        parent
            .rot
            .multLocal(translation)
            .addLocal(parent.translation);
        return this;
    }
        
    /**
     * Sets this matrix's translation to the given x,y,z values.
     * @param x This matrix's new x translation.
     * @param y This matrix's new y translation.
     * @param z This matrix's new z translation.
     * @return this
     */
    public Transform setTranslation(float x,float y, float z) {
        translation.set(x,y,z);
        return this;
    }

    /**
     * Sets this matrix's scale to the given x,y,z values.
     * @param x This matrix's new x scale.
     * @param y This matrix's new y scale.
     * @param z This matrix's new z scale.
     * @return this
     */
    public Transform setScale(float x, float y, float z) {
        scale.set(x,y,z);
        return this;
    }

    public Vector3f transformPoint(final Vector3f in, Vector3f store){
        if (store == null)
            store = new Vector3f();

        // multiply with scale first, then rotate, finally translate (cf.
        // Eberly)
        return rot.mult(store.set(in).multLocal(scale), store).addLocal(translation);
    }

    public Vector3f transformInversePoint(final Vector3f in, Vector3f store){
        if (store == null)
            store = new Vector3f();

        in.subtract(translation, store);
        rot.inverse().mult(store, store);
        store.divideLocal(scale);

        return store;
    }
    
    public Vector3f transformVector(final Vector3f in, Vector3f store){
        if (store == null)
            store = new Vector3f();

    
        return rot.mult(store.set(in).multLocal(scale), store);
    }

    public Vector3f transformInverseVector(final Vector3f in, Vector3f store){
        if (store == null)
            store = new Vector3f();

        rot.inverse().mult(store, store);
        store.divideLocal(scale);

        return store;
    }

    public void invertLocal(){
        Matrix4f m = new Matrix4f();
        copyTo( m );
        m.invertLocal();
        
        m.toTranslationVector( translation );
        m.toRotationQuat( rot );
        m.toScaleVector( scale );
    }
    
    public void invertLocalNoScale(){
        Matrix4f m = new Matrix4f();
        copyToNoScale( m );
        m.invertLocal();
        
        m.toTranslationVector( translation );
        m.toRotationQuat( rot );
    }
    
    /**
     * Loads the identity.  Equal to translation=0,0,0 scale=1,1,1 rot=0,0,0,1.
     */
    public void loadIdentity() {
        translation.set(0,0,0);
        scale.set(1,1,1);
        rot.set(0,0,0,1);
    }

    @Override
    public String toString(){
        return translation.x + " " + translation.y + " " + translation.z + "\n"
                                          +  rot.x + " " + rot.y + " " + rot.z + " " + rot.w + "\n"
                                          +  scale.x + " " + scale.y + " " + scale.z + "";
    }

    /**
     * Sets this transform to be equal to the given transform.
     * @param t The transform to be equal to.
     * @return this
     */
    public Transform set(Transform t) {
        this.translation.set(t.translation);
        this.rot.set(t.rot);
        this.scale.set(t.scale);
        return this;
    }
    
    //no scale please
    public void copyFrom(final Matrix4f t){
        t.toTranslationVector( translation );
        t.toRotationQuat( rot );
    }
    
    public void copyTo(Matrix4f m){
        TempVars tv = TempVars.get();
        Matrix3f m3 = tv.tempMat3;
        m.setTransform(translation, scale, rot.toRotationMatrix(m3));
        tv.release();
    }
    
    public void copyToNoScale(Matrix4f m){
        TempVars tv = TempVars.get();
        Matrix3f m3 = tv.tempMat3;
        m.setTransform(translation, Vector3f.UNIT_XYZ, rot.toRotationMatrix(m3));
        tv.release();
    }

    
    @Override
    public Transform clone() {
        try {
            Transform tq = (Transform) super.clone();
            tq.rot = rot.clone();
            tq.scale = scale.clone();
            tq.translation = translation.clone();
            return tq;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public Quaternion getRot() {
        return rot;
    }

    public void setRot(Quaternion rot) {
        this.rot = rot;
    }
    
    public void moveLocalSpace(float x, float y, float z){
        Vector3f v = new Vector3f(x,y,z);
        getRotation().multLocal(v);
        getTranslation().addLocal(v);
    }

    
    public void rotate(float rx, float ry, float rz){
        getRotation().rotate(rx, ry, rz);
    }
    
    public void rotateLocal( float rx, float ry, float rz ){
        getRotation().rotateLocalSpace(rx, ry, rz);
    }
    
 
    public void lookAt(Vector3f observationPoint) {
        Vector3f dir = observationPoint.subtract( getTranslation() );
        dir.negateLocal();
        getRotation().lookAt(dir, Vector3f.UNIT_Y);
    }
    
    public void rotateAround(final Vector3f center, float rx, float ry, float rz){
        //move to the position
        translation.subtractLocal( center );
        
        Quaternion q = new Quaternion(rx, ry, rz);
        
        //rotate position
        q.mult( translation, translation);
        
        //rotate orientation
        q.mult(rot, rot);
       
        //undo move
        translation.addLocal( center );
    }
    
    
       public void connectTwoPoints(final Vector3f a,final Vector3f b,final float thickness) {
            final float dist = a.distance(b);
            
            //place it in the middle of the two points
            final Vector3f pos = getTranslation();
            pos.set(a).addLocal(b).divideLocal(2.0f);
            
            lookAt( b );
            
            getScale().set(thickness, thickness, dist);
        }
}
