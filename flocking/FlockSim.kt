/*
 * Filename: FlockSim.java
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
class FlockSim : PApplet()
{

    public var pressed = BooleanArray(256)

    private var loop = true
    var flock: Flock? = null
    override fun settings()
    {
        size(900, 650, P2D)
        // fullScreen(P2D);
    }

    override fun setup()
    {
        flock = Flock(this)
    }

    override fun draw()
    {
        background(20)
        flock!!.update()
        flock!!.draw()

        // print fps
        textSize(20f)
        textAlign(LEFT)
        fill(0f, 255f, 0f)
        text(nf(frameRate), 0f, textAscent())
    }

    override fun mouseDragged()
    {
        if (!pressed['D'.toInt()])
        {
            flock!!.addObstacle(mouseX.toFloat(), mouseY.toFloat())
        }
    }

    override fun mouseClicked()
    {
        mouseDragged()
    }

    override fun keyPressed()
    {
        pressed[keyCode] = true
    }

    override fun keyReleased()
    {
        pressed[keyCode] = false
    }

    override fun keyTyped()
    {
        if (key == ' ')
        {
            if (loop)
            {
                noLoop()
            } else
            {
                loop()
            }
            loop = !loop
        }
    }
}

/**
 * @param args
 */
fun main(args: Array<String>)
{
    PApplet.main("flocking.flocking.FlockSim")
}