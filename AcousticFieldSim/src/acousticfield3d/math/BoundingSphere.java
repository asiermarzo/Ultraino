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

import acousticfield3d.utils.BufferUtils;
import java.nio.FloatBuffer;

/**
 * <code>BoundingSphere</code> defines a sphere that defines a container for a
 * group of vertices of a particular piece of geometry. This sphere defines a
 * radius and a center. <br>
 * <br>
 * A typical usage is to allow the class define the center and radius by calling
 * either <code>containAABB</code> or <code>averagePoints</code>. A call to
 * <code>computeFramePoint</code> in turn calls <code>containAABB</code>.
 *
 * @author Mark Powell
 * @version $Id: BoundingSphere.java,v 1.59 2007/08/17 10:34:26 rherlitz Exp $
 */
public class BoundingSphere {
    Vector3f center = new Vector3f();

    float radius;
    private static final float RADIUS_EPSILON = 1f + 0.00001f;

    /**
     * Default contstructor instantiates a new <code>BoundingSphere</code>
     * object.
     */
    public BoundingSphere() {
    }

    /**
     * Constructor instantiates a new <code>BoundingSphere</code> object.
     *
     * @param r
     *            the radius of the sphere.
     * @param c
     *            the center of the sphere.
     */
    public BoundingSphere(float r, Vector3f c) {
        this.center.set(c);
        this.radius = r;
    }


    /**
     * <code>getRadius</code> returns the radius of the bounding sphere.
     *
     * @return the radius of the bounding sphere.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * <code>setRadius</code> sets the radius of this bounding sphere.
     *
     * @param radius
     *            the new radius of the bounding sphere.
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * <code>computeFromPoints</code> creates a new Bounding Sphere from a
 given copyTo of points. It uses the <code>calcWelzl</code> method as
     * default.
     *
     * @param points
     *            the points to contain.
     */
    public void computeFromPoints(FloatBuffer points) {
        calcWelzl(points);
    }

  
    /**
     * Calculates a minimum bounding sphere for the copyTo of points. The algorithm
     * was originally found in C++ at
     * <p><a href="http://www.flipcode.com/cgi-bin/msg.cgi?showThread=COTD-SmallestEnclosingSpheres&forum=cotd&id=-1">
     * http://www.flipcode.com/cgi-bin/msg.cgi?showThread=COTD-SmallestEnclosingSpheres&forum=cotd&id=-1</a><br><strong>broken link</strong></p>
     * <p>and translated to java by Cep21</p>
     *
     * @param points
     *            The points to calculate the minimum bounds from.
     */
    public void calcWelzl(FloatBuffer points) {
        if (center == null) {
            center = new Vector3f();
        }
        FloatBuffer buf = BufferUtils.createFloatBuffer(points.limit());
        points.rewind();
        buf.put(points);
        buf.flip();
        recurseMini(buf, buf.limit() / 3, 0, 0);
    }

    /**
     * Used from calcWelzl. This function recurses to calculate a minimum
     * bounding sphere a few points at a time.
     *
     * @param points
     *            The array of points to look through.
     * @param p
     *            The size of the list to be used.
     * @param b
     *            The number of points currently considering to include with the
     *            sphere.
     * @param ap
     *            A variable simulating pointer arithmatic from C++, and offset
     *            in <code>points</code>.
     */
    private void recurseMini(FloatBuffer points, int p, int b, int ap) {
        //TempVars vars = TempVars.get();

        Vector3f tempA = new Vector3f(); //vars.vect1;
        Vector3f tempB = new Vector3f(); //vars.vect2;
        Vector3f tempC = new Vector3f(); //vars.vect3;
        Vector3f tempD = new Vector3f(); //vars.vect4;

        switch (b) {
            case 0:
                this.radius = 0;
                this.center.set(0, 0, 0);
                break;
            case 1:
                this.radius = 1f - RADIUS_EPSILON;
                BufferUtils.populateFromBuffer(center, points, ap - 1);
                break;
            case 2:
                BufferUtils.populateFromBuffer(tempA, points, ap - 1);
                BufferUtils.populateFromBuffer(tempB, points, ap - 2);
                setSphere(tempA, tempB);
                break;
            case 3:
                BufferUtils.populateFromBuffer(tempA, points, ap - 1);
                BufferUtils.populateFromBuffer(tempB, points, ap - 2);
                BufferUtils.populateFromBuffer(tempC, points, ap - 3);
                setSphere(tempA, tempB, tempC);
                break;
            case 4:
                BufferUtils.populateFromBuffer(tempA, points, ap - 1);
                BufferUtils.populateFromBuffer(tempB, points, ap - 2);
                BufferUtils.populateFromBuffer(tempC, points, ap - 3);
                BufferUtils.populateFromBuffer(tempD, points, ap - 4);
                setSphere(tempA, tempB, tempC, tempD);
                //vars.release();
                return;
        }
        for (int i = 0; i < p; i++) {
            BufferUtils.populateFromBuffer(tempA, points, i + ap);
            if (tempA.distanceSquared(center) - (radius * radius) > RADIUS_EPSILON - 1f) {
                for (int j = i; j > 0; j--) {
                    BufferUtils.populateFromBuffer(tempB, points, j + ap);
                    BufferUtils.populateFromBuffer(tempC, points, j - 1 + ap);
                    BufferUtils.setInBuffer(tempC, points, j + ap);
                    BufferUtils.setInBuffer(tempB, points, j - 1 + ap);
                }
                recurseMini(points, i, b + 1, ap + 1);
            }
        }
        //vars.release();
    }

    /**
     * Calculates the minimum bounding sphere of 4 points. Used in welzl's
     * algorithm.
     *
     * @param O
     *            The 1st point inside the sphere.
     * @param A
     *            The 2nd point inside the sphere.
     * @param B
     *            The 3rd point inside the sphere.
     * @param C
     *            The 4th point inside the sphere.
     * @see #calcWelzl(java.nio.FloatBuffer)
     */
    private void setSphere(Vector3f O, Vector3f A, Vector3f B, Vector3f C) {
        Vector3f a = A.subtract(O);
        Vector3f b = B.subtract(O);
        Vector3f c = C.subtract(O);

        float Denominator = 2.0f * (a.x * (b.y * c.z - c.y * b.z) - b.x
                * (a.y * c.z - c.y * a.z) + c.x * (a.y * b.z - b.y * a.z));
        if (Denominator == 0) {
            center.set(0, 0, 0);
            radius = 0;
        } else {
            Vector3f o = a.cross(b).multLocal(c.lengthSquared()).addLocal(
                    c.cross(a).multLocal(b.lengthSquared())).addLocal(
                    b.cross(c).multLocal(a.lengthSquared())).divideLocal(
                    Denominator);

            radius = o.length() * RADIUS_EPSILON;
            O.add(o, center);
        }
    }

    /**
     * Calculates the minimum bounding sphere of 3 points. Used in welzl's
     * algorithm.
     *
     * @param O
     *            The 1st point inside the sphere.
     * @param A
     *            The 2nd point inside the sphere.
     * @param B
     *            The 3rd point inside the sphere.
     * @see #calcWelzl(java.nio.FloatBuffer)
     */
    private void setSphere(Vector3f O, Vector3f A, Vector3f B) {
        Vector3f a = A.subtract(O);
        Vector3f b = B.subtract(O);
        Vector3f acrossB = a.cross(b);

        float Denominator = 2.0f * acrossB.dot(acrossB);

        if (Denominator == 0) {
            center.set(0, 0, 0);
            radius = 0;
        } else {

            Vector3f o = acrossB.cross(a).multLocal(b.lengthSquared()).addLocal(b.cross(acrossB).multLocal(a.lengthSquared())).divideLocal(Denominator);
            radius = o.length() * RADIUS_EPSILON;
            O.add(o, center);
        }
    }

    /**
     * Calculates the minimum bounding sphere of 2 points. Used in welzl's
     * algorithm.
     *
     * @param O
     *            The 1st point inside the sphere.
     * @param A
     *            The 2nd point inside the sphere.
     * @see #calcWelzl(java.nio.FloatBuffer)
     */
    private void setSphere(Vector3f O, Vector3f A) {
        radius = M.sqrt(((A.x - O.x) * (A.x - O.x) + (A.y - O.y)
                * (A.y - O.y) + (A.z - O.z) * (A.z - O.z)) / 4f) + RADIUS_EPSILON - 1f;
        center.interpolateLocal(O, A, .5f);
    }

    /**
     * <code>averagePoints</code> selects the sphere center to be the average
     * of the points and the sphere radius to be the smallest value to enclose
     * all points.
     *
     * @param points
     *            the list of points to contain.
     */
    public void averagePoints(Vector3f[] points) {
        center = points[0];

        for (int i = 1; i < points.length; i++) {
            center.addLocal(points[i]);
        }

        float quantity = 1.0f / points.length;
        center.multLocal(quantity);

        float maxRadiusSqr = 0;
        for (int i = 0; i < points.length; i++) {
            Vector3f diff = points[i].subtract(center);
            float radiusSqr = diff.lengthSquared();
            if (radiusSqr > maxRadiusSqr) {
                maxRadiusSqr = radiusSqr;
            }
        }

        radius = (float) Math.sqrt(maxRadiusSqr) + RADIUS_EPSILON - 1f;

    }

    /**
     * <code>transform</code> modifies the center of the sphere to reflect the
     * change made via a rotation, translation and scale.
     *
     * @param trans
     *            the transform to apply
     * @param store
     *            sphere to store result in
     * @return BoundingVolume
     * @return ref
     */
    public BoundingSphere transform(Transform trans, BoundingSphere store) {
        BoundingSphere sphere;
        if (store == null ) {
            sphere = new BoundingSphere(1, new Vector3f(0, 0, 0));
        } else {
            sphere = (BoundingSphere) store;
        }

        center.mult(trans.getScale(), sphere.center);
        trans.getRotation().mult(sphere.center, sphere.center);
        sphere.center.addLocal(trans.getTranslation());
        sphere.radius = M.abs(getMaxAxis(trans.getScale()) * radius) + RADIUS_EPSILON - 1f;
        return sphere;
    }


    public BoundingSphere transform(Matrix4f trans, BoundingSphere store) {
        BoundingSphere sphere;
        if (store == null ) {
            sphere = new BoundingSphere(1, new Vector3f(0, 0, 0));
        } else {
            sphere = (BoundingSphere) store;
        }

        trans.mult(center, sphere.center);
        Vector3f axes = new Vector3f(1, 1, 1);
        trans.mult(axes, axes);
        float ax = getMaxAxis(axes);
        sphere.radius = M.abs(ax * radius) + RADIUS_EPSILON - 1f;
        return sphere;
    }

    private float getMaxAxis(Vector3f scale) {
        float x = M.abs(scale.x);
        float y = M.abs(scale.y);
        float z = M.abs(scale.z);

        if (x >= y) {
            if (x >= z) {
                return x;
            }
            return z;
        }

        if (y >= z) {
            return y;
        }

        return z;
    }

    /**
     * <code>whichSide</code> takes a plane (typically provided by a view
     * frustum) to determine which side this bound is on.
     *
     * @param plane
     *            the plane to check against.
     * @return side
     */
    public Plane.Side whichSide(Plane plane) {
        float distance = plane.pseudoDistance(center);

        if (distance <= -radius) {
            return Plane.Side.Negative;
        } else if (distance >= radius) {
            return Plane.Side.Positive;
        } else {
            return Plane.Side.None;
        }
    }

    /**
     * <code>toString</code> returns the string representation of this object.
     * The form is: "Radius: RRR.SSSS Center: <Vector>".
     *
     * @return the string representation of this.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [Radius: " + radius + " Center: "
                + center + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.bounding.BoundingVolume#intersectsSphere(com.jme.bounding.BoundingSphere)
     */
    public boolean intersectsSphere(BoundingSphere bs) {
        assert Vector3f.isValidVector(center) && Vector3f.isValidVector(bs.center);

        TempVars vars = TempVars.get();

        Vector3f diff = center.subtract(bs.center, vars.vect1);
        float rsum = getRadius() + bs.getRadius();
        boolean eq = (diff.dot(diff) <= rsum * rsum);
        vars.release();
        return eq;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.bounding.BoundingVolume#intersectsBoundingBox(com.jme.bounding.BoundingBox)
     */
    public boolean intersectsBoundingBox(BoundingBox bb) {
        assert Vector3f.isValidVector(center) && Vector3f.isValidVector(bb.center);

        if (M.abs(bb.center.x - center.x) < getRadius()
                + bb.xExtent
                && M.abs(bb.center.y - center.y) < getRadius()
                + bb.yExtent
                && M.abs(bb.center.z - center.z) < getRadius()
                + bb.zExtent) {
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.bounding.BoundingVolume#intersectsOrientedBoundingBox(com.jme.bounding.OrientedBoundingBox)
     */
//    public boolean intersectsOrientedBoundingBox(OrientedBoundingBox obb) {
//        return obb.intersectsSphere(this);
//    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.bounding.BoundingVolume#intersects(com.jme.math.Ray)
     */
    public boolean intersects(Ray ray) {
        assert Vector3f.isValidVector(center);

        TempVars vars = TempVars.get();

        Vector3f diff = vars.vect1.set(ray.getOrigin()).subtractLocal(center);
        float radiusSquared = getRadius() * getRadius();
        float a = diff.dot(diff) - radiusSquared;
        if (a <= 0.0) {
            vars.release();
            // in sphere
            return true;
        }

        // outside sphere
        float b = ray.getDirection().dot(diff);
        vars.release();
        if (b >= 0.0) {
            return false;
        }
        return b * b >= a;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.bounding.BoundingVolume#intersectsWhere(com.jme.math.Ray)
     */
    private int collideWithRay(Ray ray) {
        TempVars vars = TempVars.get();

        Vector3f diff = vars.vect1.set(ray.getOrigin()).subtractLocal(
                center);
        float a = diff.dot(diff) - (getRadius() * getRadius());
        float a1, discr, root;
        if (a <= 0.0) {
            // inside sphere
            a1 = ray.direction.dot(diff);
            discr = (a1 * a1) - a;
            root = M.sqrt(discr);

            float distance = root - a1;
            Vector3f point = new Vector3f(ray.direction).multLocal(distance).addLocal(ray.origin);
/*
            CollisionResult result = new CollisionResult(point, distance);
            results.addCollision(result);
            vars.release();*/
            return 1;
        }

        a1 = ray.direction.dot(diff);
        vars.release();
        if (a1 >= 0.0) {
            return 0;
        }

        discr = a1 * a1 - a;
        if (discr < 0.0) {
            return 0;
        } else if (discr >= M.ZERO_TOLERANCE) {
            root = M.sqrt(discr);
            float dist = -a1 - root;
            /*
            Vector3f point = new Vector3f(ray.direction).multLocal(dist).addLocal(ray.origin);
            results.addCollision(new CollisionResult(point, dist));

            dist = -a1 + root;
            point = new Vector3f(ray.direction).multLocal(dist).addLocal(ray.origin);
            results.addCollision(new CollisionResult(point, dist));
            */
            return 2;
            
        } else {
            float dist = -a1;
            /*
            Vector3f point = new Vector3f(ray.direction).multLocal(dist).addLocal(ray.origin);
            results.addCollision(new CollisionResult(point, dist));*/
            return 1;
        }
    }
    
   
    public Vector3f intersectPoint(Ray ray) {
        TempVars vars = TempVars.get();

        Vector3f diff = vars.vect1.set(ray.getOrigin()).subtractLocal(
                center);
        float a = diff.dot(diff) - (getRadius() * getRadius());
        float a1, discr, root;
        if (a <= 0.0) {
            // inside sphere
            a1 = ray.direction.dot(diff);
            discr = (a1 * a1) - a;
            root = M.sqrt(discr);

            float distance = root - a1;
            Vector3f point = new Vector3f(ray.direction).multLocal(distance).addLocal(ray.origin);
            return point;
        }

        a1 = ray.direction.dot(diff);
        vars.release();
        if (a1 >= 0.0) {
            return null;
        }

        discr = a1 * a1 - a;
        if (discr < 0.0) {
            return null;
        } else if (discr >= M.ZERO_TOLERANCE) {
            root = M.sqrt(discr);
            float dist = -a1 - root;
            Vector3f point = new Vector3f(ray.direction).multLocal(dist).addLocal(ray.origin);
            return point;
            
            /*
            dist = -a1 + root;
            point = new Vector3f(ray.direction).multLocal(dist).addLocal(ray.origin);
            */
        } else {
            float dist = -a1;
            Vector3f point = new Vector3f(ray.direction).multLocal(dist).addLocal(ray.origin);
            return point;
        }
    }


    public boolean contains(Vector3f point) {
        return center.distanceSquared(point) < (getRadius() * getRadius());
    }


    public boolean intersects(Vector3f point) {
        return center.distanceSquared(point) <= (getRadius() * getRadius());
    }


    public float distanceToEdge(Vector3f point) {
        return center.distance(point) - radius;
    }


    public float getVolume() {
        return 4 * M.ONE_THIRD * M.PI * radius * radius * radius;
    }

    
}