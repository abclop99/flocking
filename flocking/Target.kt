/*
 * Filename: Target.java
 * Author: Aaron Li
 * UserId: cs11faic
 * Date: 2019-03-04
 * Sources of help: None
 */
package flocking.flocking

import processing.core.PApplet
import flocking.flocking.QuadTree.P2DF
import processing.core.PVector
import processing.core.PShape
import flocking.flocking.QuadTree
import flocking.flocking.Boid
import flocking.flocking.Obstacle
import processing.core.PConstants
import kotlin.jvm.JvmStatic
import flocking.flocking.FlockSim
import flocking.flocking.Flock
import kotlin.jvm.JvmOverloads
import java.awt.geom.Point2D

/**
 *
 */
class Target(canvas: PApplet) : Boid(canvas)
{
    /**
     * @param canvas
     */
    init
    {

        // shape of boid
        shape = canvas.createShape()
        shape.beginShape()
        shape.noStroke()
        shape.fill(-0x100)
        shape.vertex(0f, 11f)
        shape.vertex(-5f, -9f)
        shape.vertex(0f, -11f)
        shape.vertex(5f, -9f)
        shape.endShape(PConstants.CLOSE)
        perceptionradius = 250f
        targetFactor = 0f
        maxForce *= 1.1f
        targetSpeed *= 1.2f
        maxSpeed *= 1.2f
        alignFactor = 0f
        cohesionFactor = 0f
        separateFactor *= 2f
    }
}