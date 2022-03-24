/*
 * Filename: QuadTree.java
 * Author: Aaron Li
 * UserId: cs11faic
 * Date: 2019-03-01
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
import java.util.function.Function

/**
 *
 */
class QuadTree<P : P2DF?> @JvmOverloads constructor(
    val canvas: PApplet,
    val x: Float,
    val y: Float,
    width: Float,
    height: Float,
    points: List<P>?,
    capacity: Int,
    maxDepth: Int = 10
)
{
    var width = 0f
    var height = 0f
    val capacity: Int
    val maxDepth: Int
    private var depth = 0
    private var northWest: QuadTree<P>? = null
    private var northEast: QuadTree<P>? = null
    private var southWest: QuadTree<P>? = null
    private var southEast: QuadTree<P>? = null
    private var split = false
    var points: ArrayList<P>? = null

    /**
     *
     */
    private constructor(
        canvas: PApplet,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        capacity: Int,
        depth: Int
    ) : this(canvas, x, y, width, height, null, capacity)
    {
        this.depth = depth
    }

    /**
     *
     */
    constructor(
        canvas: PApplet,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        capacity: Int
    ) : this(canvas, x, y, width, height, null, capacity)
    {
    }

    /**
     * Inserts a group of point into this quad.
     *
     * @param point
     * the point to insert
     */
    fun insert(points: List<P>?)
    {
        this.points!!.addAll(points!!)
        split()
    }

    /**
     * Inserts a point into this quad.
     *
     * @param point
     * the point to insert
     */
    fun insert(point: P)
    {
        points!!.add(point)
        split()
    }

    /**
     * Splits the quad if it is above capacity. Inserts points into the
     * correct quad if already split.
     */
    private fun split()
    {
        val w = width / 2
        val h = height / 2

        // check if creating quads is neccesary
        if (points!!.size > capacity && !split && depth <= maxDepth)
        {

            // create sub-quads
            northWest = QuadTree(canvas, x + 0, y + 0, w, h, capacity, depth + 1)
            northEast = QuadTree(canvas, x + w, y + 0, w, h, capacity, depth + 1)
            southWest = QuadTree(canvas, x + 0, y + h, w, h, capacity, depth + 1)
            southEast = QuadTree(canvas, x + w, y + h, w, h, capacity, depth + 1)
            split = true
        }

        // insert to subquads if they exist
        if (split)
        {
            while (!points!!.isEmpty())
            {
                val centerX = x + w
                val centerY = y + h
                if (points!![0]!!.xCoord <= centerX
                    && points!![0]!!.yCoord <= centerY
                )
                {
                    northWest!!.insert(points!!.removeAt(0))
                } else if (points!![0]!!.xCoord > centerX
                    && points!![0]!!.yCoord <= centerY
                )
                {
                    northEast!!.insert(points!!.removeAt(0))
                } else if (points!![0]!!.xCoord <= centerX
                    && points!![0]!!.yCoord > centerY
                )
                {
                    southWest!!.insert(points!!.removeAt(0))
                } else if (points!![0]!!.xCoord > centerX
                    && points!![0]!!.yCoord > centerY
                )
                {
                    southEast!!.insert(points!!.removeAt(0))
                }
            }
        }
    }

    fun draw()
    {

        // draw border
//        canvas.noFill()
//        canvas.stroke(255, 30f)
//        canvas.strokeWeight(1f)
//        canvas.rectMode(PConstants.CORNER)
//        canvas.rect(x, y, width, height)

        // draw points
        for (p in points!!)
        {
            p!!.draw()
        }

        // draw subquads if they exist
        if (split)
        {
            northWest!!.draw()
            northEast!!.draw()
            southWest!!.draw()
            southEast!!.draw()
        }
    }

    fun reMake()
    {
        points = getPoints()
        split = false
        split()
    }

    fun reMake(width: Float, height: Float)
    {
        this.width = width
        this.height = height
        reMake()
    }

    fun remove(point: P): Boolean
    {
        var result = false
        if (points!!.contains(point))
        {
            return points!!.remove(point)
        }
        // search in sub-quadrants
        if (split)
        {
            if (northWest!!.isIn(point!!.xCoord, point.yCoord))
            {
                result = northWest!!.remove(point)
            } else if (northEast!!.isIn(point.xCoord, point.yCoord))
            {
                result = northEast!!.remove(point)
            } else if (southWest!!.isIn(point.xCoord, point.yCoord))
            {
                result = southWest!!.remove(point)
            } else if (southEast!!.isIn(point.xCoord, point.yCoord))
            {
                result = southEast!!.remove(point)
            }
        }
        reMake()
        return result
    }

    /**
     * Get all points in the quad
     *
     * @return list of points
     */
    @JvmName("getPoints1")
    fun getPoints(): ArrayList<P>
    {
        val allPoints = ArrayList<P>()

        // add points in this quad
        allPoints.addAll(points!!)

        // sub-quads
        if (split)
        {
            allPoints.addAll(northWest!!.getPoints())
            allPoints.addAll(northEast!!.getPoints())
            allPoints.addAll(southWest!!.getPoints())
            allPoints.addAll(southEast!!.getPoints())
        }
        return allPoints
    }

    /**
     * Get points within a certain range of coordinates
     *
     * @param x1
     * x value of first corner
     * @param y1
     * y value of first corner
     * @param x2
     * x value of second corner
     * @param y2
     * y value of second corner
     * @return list of all points within the range
     */
    fun getPoints(x1: Float, y1: Float, x2: Float, y2: Float): ArrayList<P>
    {
        val allPoints = ArrayList<P>()
        if (split)
        {

            // lambda expression to recursively get points
            val getPoints = Function<QuadTree<P>?, List<P>> { quadrant: QuadTree<P>? ->
                val result: MutableList<P> = ArrayList()
                val qX1: Float
                val qY1: Float
                val qX2: Float
                val qY2: Float
                if (quadrant!!.isIn(x1, y1) || quadrant.isIn(x2, y2)
                    || quadrant.isIn(x1, y2)
                    || quadrant.isIn(x2, y1)
                )
                {
                    qX1 = Math.max(quadrant.x, x1)
                    qY1 = Math.max(quadrant.y, y1)
                    qX2 = Math.min(quadrant.x + quadrant.width, x2)
                    qY2 = Math.min(quadrant.y + quadrant.height, y2)
                    result.addAll(quadrant.getPoints(qX1, qY1, qX2, qY2))
                }
                result
            }
            allPoints.addAll(getPoints.apply(northWest))
            allPoints.addAll(getPoints.apply(northEast))
            allPoints.addAll(getPoints.apply(southWest))
            allPoints.addAll(getPoints.apply(southEast))
        } else
        {
            // check coords of point against range
            for (p in points!!)
            {
                if (p!!.xCoord >= x1 && p.xCoord <= x2 && p.yCoord >= y1 && p.yCoord <= y2)
                {
                    allPoints.add(p)
                }
            }
        }
        return allPoints
    }

    /**
     * Get points within a specifed range
     *
     * @param x
     * x-coordinate of center
     * @param y
     * y-coordinate of center
     * @param radius
     * 1/2 of the height and width of the range
     * @return All points in the range
     */
    fun getPoints(x: Float, y: Float, radius: Float): ArrayList<P>
    {
        return getPoints(x - radius, y - radius, x + radius, y + radius)
    }

    /**
     * Get points within a certain distance of a point
     *
     * @param x
     * x-coordinate of center
     * @param y
     * y-coordinate of center
     * @param radius
     * distance limit
     * @return All points in the range
     */
    fun getPointsInRadius(x: Float, y: Float, radius: Float): ArrayList<P>
    {
        val points = getPoints(x, y, radius)
        val results = ArrayList<P>()

        // cut off at circular radius
        for (p in points)
        {
            if (p!!.distance(x, y) < radius)
            {
                results.add(p)
            }
        }
        return results
    }

    fun getPointsWrapped(
        x1: Float, y1: Float, x2: Float,
        y2: Float
    ): ArrayList<P>
    {
        val w = width
        val h = height
        val zz = 0f
        val offsets = arrayOf( // @formatter:off
            Point2D.Float(-w, -h), Point2D.Float(zz, -h), Point2D.Float(+w, -h),
            Point2D.Float(-w, zz), Point2D.Float(zz, zz), Point2D.Float(+w, zz),
            Point2D.Float(-w, +h), Point2D.Float(zz, +h), Point2D.Float(+w, +h) // @formatter:on
        )
        val results = ArrayList<P>()

        // add points for all of the offsets
        for (offset in offsets)
        {
            results.addAll(
                getPoints(
                    (x1 + offset.getX()).toFloat(),
                    (y1 + offset.getY()).toFloat(),
                    (x2 + offset.getX()).toFloat(),
                    (y2 + offset.getY()).toFloat()
                )
            )
            // debug printing
            // canvas.rectMode(PConstants.CORNERS);
            // canvas.stroke(255, 0, 255);
            // canvas.strokeWeight(5);
            // canvas.rect((float) (x1 + offset.getX()),
            // (float) (y1 + offset.getY()),
            // (float) (x2 + offset.getX()),
            // (float) (y2 + offset.getY()));
        }
        return results
    }

    fun getPointsWrapped(x: Float, y: Float, radius: Float): ArrayList<P>
    {
        return getPointsWrapped(x - radius, y - radius, x + radius, y + radius)
    }

    fun getPointsInRadiusWrapped(
        x1: Float, y1: Float,
        radius: Float
    ): ArrayList<P>
    {
        val w = width
        val h = height
        val zz = 0f
        val offsets = arrayOf( // @formatter:off
            Point2D.Float(-w, -h), Point2D.Float(zz, -h), Point2D.Float(+w, -h),
            Point2D.Float(-w, zz), Point2D.Float(zz, zz), Point2D.Float(+w, zz),
            Point2D.Float(-w, +h), Point2D.Float(zz, +h), Point2D.Float(+w, +h) // @formatter:on
        )
        val results = ArrayList<P>()

        // add points for all of the offsets
        for (offset in offsets)
        {
            results.addAll(
                getPointsInRadius(
                    (x1 + offset.getX()).toFloat(),
                    (y1 + offset.getY()).toFloat(),
                    radius
                )
            )
            // // debug printing
            // canvas.ellipseMode(PConstants.RADIUS);
            // canvas.stroke(255, 0, 255);
            // canvas.strokeWeight(5);
            // canvas.circle((float) (x1 + offset.getX()),
            // (float) (y1 + offset.getY()),
            // radius);
        }
        return results
    }

    fun isIn(x: Float, y: Float): Boolean
    {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height
    }

    interface P2DF
    {
        val xCoord: Float
        val yCoord: Float
        fun draw()
        fun distance(other: P2DF): Float
        fun distance(x: Float, y: Float): Float
        fun sub(other: P2DF): P2DF
        fun sub(x: Float, y: Float): P2DF
    }

    class Point
    /**
     *
     */
        (x: kotlin.Float, y: kotlin.Float) : Point2D.Float(x, y), P2DF
    {
        /**
         * @return x coordinate
         * @see quadtree.QuadTree.P2DF.getX
         */
        override val xCoord: kotlin.Float
            get() = super.getX().toFloat()

        /**
         * @return y coordinate
         * @see quadtree.QuadTree.P2DF.getY
         */
        override val yCoord: kotlin.Float
            get() = super.getY().toFloat()

        /**
         *
         * @see quadtree.QuadTree.P2DF.draw
         */
        override fun draw()
        {
        }

        /**
         * @param other
         * @return
         * @see quadtree.QuadTree.P2DF.distance
         */
        override fun distance(other: P2DF): kotlin.Float
        {
            return distance(other.xCoord, other.yCoord)
        }

        /**
         * @param x
         * @param y
         * @return
         * @see quadtree.QuadTree.P2DF.distance
         */
        override fun distance(x: kotlin.Float, y: kotlin.Float): kotlin.Float
        {
            return super.distance(x.toDouble(), y.toDouble()).toFloat()
        }

        /**
         * @param other
         * @return
         * @see quadtree.QuadTree.P2DF.sub
         */
        override fun sub(other: P2DF): P2DF
        {
            return sub(other.xCoord, other.yCoord)
        }

        /**
         * @param x
         * @param y
         * @return
         * @see quadtree.QuadTree.P2DF.sub
         */
        override fun sub(x: kotlin.Float, y: kotlin.Float): P2DF
        {
            return Point(xCoord - x, yCoord - y)
        }
    }
    /**
     *
     */
    /**
     *
     */
    init
    {
        this.width = width
        this.height = height
        this.capacity = capacity
        this.maxDepth = maxDepth
        depth = 0
        this.points = ArrayList()
        if (points != null)
        {
            this.points!!.addAll(points)
        }
        split()
    }
}