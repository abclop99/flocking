/*
 * Filename: Obstacle.java
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
class Obstacle(private val canvas: PApplet, x: Float, y: Float) : P2DF
{
    var position: PVector
    var radius = 10f
    fun update()
    {

        // delete if pressed while holding D
        if (canvas.mousePressed && (canvas as FlockSim).pressed.get('D'.code))
        {
            if (position.dist(
                    PVector(
                        canvas.mouseX.toFloat(),
                        canvas.mouseY.toFloat()
                    )
                ) <= radius * 5
            )
            {
                canvas.flock!!.removeObstacle(this)
            }
        }
    }

    override fun draw()
    {
        canvas.ellipseMode(PConstants.RADIUS)
        canvas.noStroke()
        canvas.fill(255f, 0f, 0f)
        canvas.circle(position.x, position.y, radius)
        // System.out.println("Obstacle at: " + position);
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
        return position.dist(PVector(other.xCoord, other.yCoord))
    }

    /**
     * @param x
     * @param y
     * @return
     * @see flocking.QuadTree.P2DF.distance
     */
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

    init
    {
        position = PVector(x, y)
    }
}