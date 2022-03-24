/*
 * Filename: Boid.java
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
import java.util.ArrayList

open class Boid(protected val canvas: PApplet) : P2DF
{
    protected var position: PVector
    protected var velocity: PVector
    protected var acceleration: PVector
    protected var force: PVector
    protected var shape: PShape

    // factors for things
    protected var perceptionradius = 100f
    protected var targetRadius = 250f
    protected var maxForce = .7f
    protected var maxSpeed = 6f
    protected var targetSpeed = 3f

    // Factors for behavior
    protected var alignFactor = 0.04f
    protected var cohesionFactor = 0.03f
    protected var separateFactor = 3.5f
    protected var obstacleFactor = 1.1f
    protected var targetFactor = 0.00f
    protected var randomFactor = 0.1f
    fun update(boids: QuadTree<Boid?>, obstacles: QuadTree<Obstacle?>, target: Target)
    {

        // almost reset acceleration
        acceleration.mult(0.1f)
        force[0f] = 0f

        // add some randomness
        acceleration.add(PVector.random2D().setMag(canvas.random(0f, randomFactor)))

        // flocking behavior
        flock(boids, obstacles, target)

        // add acceleration and limit velocity
        acceleration.limit(maxForce)
        velocity.add(acceleration).limit(maxSpeed)
        if (velocity.mag() < MIN_SPEED)
        {
            velocity.setMag(MIN_SPEED)
        }
        velocity.add(force)
        position.add(velocity)


        // attempt to reach target velocity
        if (velocity.mag() < maxSpeed)
        {
            acceleration.add(velocity.copy().setMag(.1f))
        }
        position.add(current(position))

        // wrap position
        position.x = (position.x + canvas.width) % canvas.width
        position.y = (position.y + canvas.height) % canvas.height
    }

    private fun flock(boids: QuadTree<Boid?>, obstacles: QuadTree<Obstacle?>, target: Target)
    {
        val boidsInRange: List<Boid?>? = boids.getPointsInRadiusWrapped(
            xCoord, yCoord,
            perceptionradius
        )
        val boidsInRangeAndAngle: MutableList<Boid?> = ArrayList()

        // remove boids that are behind current boid
        for (b in boidsInRange!!)
        {
            val aVector = velocity
            val bVector = PVector.sub(b!!.position, position)
            val angle = Math.abs(PVector.angleBetween(aVector, bVector))
            if (angle <= 3)
            {
                boidsInRangeAndAngle.add(b)
            }
        }

        // flocking behaviors
        align(boidsInRangeAndAngle)
        cohesion(boidsInRangeAndAngle)
        separation(boidsInRangeAndAngle)
        target(target)
        avoid(obstacles.getPointsInRadiusWrapped(xCoord, yCoord, OBSTACLE_RADIUS))
    }

    private fun align(boids: List<Boid?>)
    {

        // find average velocity around
        val avgVel = PVector()
        for (boid in boids)
        {
            avgVel.add(boid!!.velocity)
            // take current into account
            avgVel.add(current(boid.position))
        }
        if (boids.size != 0)
        {
            avgVel.div(boids.size.toFloat())

            // add to acceleration
            acceleration.add(PVector.mult(PVector.sub(avgVel, velocity), alignFactor))
        }
    }

    private fun cohesion(boids: List<Boid?>)
    {

        // find average position to move toward
        val avgPos = PVector()
        for (boid in boids)
        {
            avgPos.add(sub(boid!!.position, position))
        }
        if (boids.size != 0)
        {
            avgPos.div(boids.size.toFloat())

            // add to acceleration
            acceleration.add(PVector.mult(avgPos, cohesionFactor))
        }
    }

    private fun separation(boids: List<Boid?>)
    {

        // add up separation forces
        val separationForce = PVector()
        for (boid in boids)
        {
            val singleSeparationForce = sub(position, boid!!.position)
            if (singleSeparationForce.mag() != 0f)
            {
                singleSeparationForce.setMag(500 / PApplet.pow(singleSeparationForce.mag(), 2.2f))
                separationForce.add(singleSeparationForce)
            }
        }

        // add to acceleration
        acceleration.add(PVector.mult(separationForce, separateFactor))
    }

    private fun avoid(obstacles: List<Obstacle?>?)
    {
        val avoidingForce = PVector()
        val closeAvoidingForce = PVector()
        for (obstacle in obstacles!!)
        {
            val difference = sub(position, obstacle!!.position)
            val singleAvoidanceForce = difference.copy().setMag(0.2f)

            // Turning away
            if (velocity.mag() > 0)
            {
                // check if facing toward obstacle
                if (PVector.dot(velocity, difference) < 0)
                {
                    // Use projection to find normal vector
                    // @formatter:off
                    singleAvoidanceForce.add(
                        PVector
                            .sub(
                                difference,
                                PVector.mult(
                                    velocity,
                                    PVector.dot(difference, velocity) / velocity.magSq()
                                )
                            )
                            .normalize()
                    )
                    // @formatter+on
                    if (singleAvoidanceForce.mag() > 0)
                    {
                        val x = if (difference.mag() - 10 < 0.01) 0.01f else (difference.mag() - 10) / 60
                        singleAvoidanceForce.setMag(1 / PApplet.pow(x, 2f))
                        avoidingForce.add(singleAvoidanceForce)
                    }
                } else
                {
                    // Use projection to find normal vector
                    // @formatter:off
                    singleAvoidanceForce.sub(
                        PVector
                            .sub(
                                difference,
                                PVector.mult(
                                    velocity,
                                    PVector.dot(difference, velocity) / velocity.magSq()
                                )
                            )
                            .normalize()
                    )
                    // @formatter+on
                    if (singleAvoidanceForce.mag() > 0)
                    {
                        val x = if (difference.mag() - 10 < 0.01) 0.01f else (difference.mag() - 10) / 60
                        singleAvoidanceForce.setMag(0.6f / PApplet.pow(x, 2f))
                        avoidingForce.add(singleAvoidanceForce)
                    }
                }
            }

            // Move away...
            run {
                val avoid = difference.copy().mult(0.08f)
                if (avoid.mag() > 0)
                {
                    avoid.setMag(1 / PApplet.pow(avoid.mag(), 2f))
                }
                avoidingForce.add(avoid)
            }
        }

        // average
        if (obstacles.size != 0)
        {
            avoidingForce.div(obstacles.size.toFloat())

            // add to acceleration
            acceleration.add(PVector.mult(avoidingForce, obstacleFactor))
        }
    }

    protected fun target(target: Target)
    {
        if (distance(target) < targetRadius)
        {
            acceleration.add(PVector.mult(sub(target.position, position), targetFactor))
        }
    }

    override fun draw()
    {
        canvas.pushMatrix()
        canvas.translate(position.x, position.y)
        canvas.scale(.8f)
        canvas.rotate(velocity.heading() - PConstants.HALF_PI)
        canvas.shape(shape, 0f, 0f)
        canvas.popMatrix()
    }

    /***************************************************************************
     * OTHER METHODS BELOW:
     */
    fun current(x: Float, y: Float): PVector
    {
        val current = PVector()
        current.add(0f, .1f * PApplet.sin(PConstants.TWO_PI * x / canvas.width))
        return current
    }

    fun current(position: PVector): PVector
    {
        return current(position.x, position.y)
    }

    /**
     * @return
     * @see flocking.QuadTree.P2DF.getXCoord
     */
    override val xCoord: Float
        get() = position.x

    /**
     * @return
     * @see flocking.QuadTree.P2DF.getYCoord
     */
    override val yCoord: Float
        get() = position.y

    /**
     * @param other
     * @return
     * @see flocking.QuadTree.P2DF.distance
     */
    override fun distance(other: P2DF): Float
    {
        return distance(other.xCoord, other.yCoord)
    }

    override fun distance(x: Float, y: Float): Float
    {
        return position.dist(PVector(x, y))
    }

    /**
     * @param other
     * @return
     * @see flocking.QuadTree.P2DF.sub
     */
    override fun sub(other: P2DF): P2DF
    {
        return sub(other.xCoord, other.yCoord)
    }

    /**
     * @param x
     * @param y
     * @return
     * @see flocking.QuadTree.P2DF.sub
     */
    override fun sub(x: Float, y: Float): P2DF
    {
        return QuadTree.Point(xCoord - x, yCoord - y)
    }

    fun sub(v1: PVector?, v2: PVector?): PVector
    {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val offsets = arrayOf( // @formatter:off
            PVector(-width, -height), PVector(0.000f, -height),
            PVector(+width, -height), PVector(-width, 0.0000f),
            PVector(0.000f, 0.0000f), PVector(+width, 0.0000f),
            PVector(-width, +height), PVector(0.000f, +height),
            PVector(+width, +height) // @formatter:on
        )
        var result = PVector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        var diff: PVector

        // check all offsets
        for (offset in offsets)
        {
            diff = PVector.sub(v1, PVector.add(v2, offset))
            if (diff.mag() < result.mag())
            {
                result = diff
            }
        }
        return result
    }

    companion object
    {
        protected const val RANDOMIZE = true
        protected const val MIN_SPEED = 2f
        protected const val OBSTACLE_RADIUS = 200f
    }

    init
    {
        position = PVector(canvas.random(0f, canvas.width.toFloat()), canvas.random(0f, canvas.height.toFloat()))
        velocity = PVector.random2D()
        velocity.setMag(canvas.random(2f, 5f))
        acceleration = PVector()
        force = PVector()

        // shape of boid
        shape = canvas.createShape()
        shape.beginShape()
        shape.noStroke()
        shape.fill(255)
        shape.vertex(0f, 10f)
        shape.vertex(-4f, -8f)
        shape.vertex(0f, -10f)
        shape.vertex(4f, -8f)
        shape.endShape(PConstants.CLOSE)
        if (RANDOMIZE)
        {
            perceptionradius = canvas.random(80f, 110f)
            targetRadius = canvas.random(230f, 270f)
            maxForce = canvas.random(.6f, .8f)
            maxSpeed = canvas.random(5f, 7f)
            targetSpeed = canvas.random(2.8f, 4f)

            // randomize behavior factors
            alignFactor *= canvas.random(0.8f, 1.2f)
            cohesionFactor *= canvas.random(0.8f, 1.2f)
            separateFactor *= canvas.random(0.8f, 1.2f)
            obstacleFactor *= canvas.random(0.8f, 1.2f)
            randomFactor *= canvas.random(0.8f, 1.2f)
        }
    }
}