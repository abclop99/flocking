/*
 * Filename: Flock.java
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

/**
 *
 */
class Flock(private val canvas: PApplet)
{
    private val boids: ArrayList<Boid?>
    private val boidTree: QuadTree<Boid?>
    private val obstacles: QuadTree<Obstacle?>
    private val obstaclesToRemove: ArrayList<Obstacle>
    private val target: Target
    fun update()
    {
        boidTree.reMake(canvas.width.toFloat(), canvas.height.toFloat())
        for (b in boids)
        {
            b!!.update(boidTree, obstacles, target)
        }

        // update obstacles
        for (obstacle in obstacles.points!!)
        {
            obstacle!!.update()
        }

        // actually remove obstacles
        while (!obstaclesToRemove.isEmpty())
        {
            obstacles.remove(obstaclesToRemove.removeAt(0))
        }

        // update target
        target.update(boidTree, obstacles, target)
    }

    fun draw()
    {
        boidTree.draw()
        obstacles.draw()

        // target.draw();
    }

    fun addObstacle(x: Float, y: Float)
    {
        obstacles.insert(Obstacle(canvas, x, y))
    }

    fun removeObstacle(obstacle: Obstacle)
    {
        obstaclesToRemove.add(obstacle)
    }

    companion object
    {
        private const val QUAD_CAPACITY = 4
        private const val NUM_BOIDS = 100
    }

    init
    {
        boids = ArrayList()

        // generate points
        for (i in 0 until NUM_BOIDS)
        {
            boids.add(Boid(canvas))
        }

        // Add to tree
        boidTree = QuadTree(
            canvas,
            0f,
            0f,
            canvas.width.toFloat(),
            canvas.height.toFloat(),
            QUAD_CAPACITY
        )
        boidTree.insert(boids)
        obstacles = QuadTree(
            canvas,
            0f,
            0f,
            canvas.width.toFloat(),
            canvas.height.toFloat(),
            18
        )
        obstaclesToRemove = ArrayList()
        target = Target(canvas)
    }
}