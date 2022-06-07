package otus.homework.customview

import android.graphics.PointF

class Polygon private constructor(
    private val sides: List<Line>,
    private val _boundingBox: BoundingBox?
) {

    class Builder {
        private var _vertexes: MutableList<PointF> = ArrayList()
        private val _sides: MutableList<Line> = ArrayList()
        private var _boundingBox: BoundingBox? = null
        private var _firstPoint = true
        private var _isClosed = false

        /**
         * Add vertex points of the polygon.
         * It is very important to add the vertexes by order, like you were drawing them one by one.
         *  1 ---> 2
         *  ↑      |
         *  |      ↓
         *  4 <--- 3
         */
        fun addVertex(point: PointF): Builder {
            if (_isClosed) {
                // each hole we start with the new array of vertex points
                _vertexes = ArrayList()
                _isClosed = false
            }
            updateBoundingBox(point)
            _vertexes.add(point)

            // add line (edge) to the polygon
            if (_vertexes.size > 1) {
                val Line = Line(_vertexes[_vertexes.size - 2], point)
                _sides.add(Line)
            }
            return this
        }

        /**
         * Close the polygon shape. This will create a new side (edge) from the **last** vertex point to the **first** vertex point.
         */
        fun close(): Builder {
            validate()

            // add last Line
            _sides.add(Line(_vertexes[_vertexes.size - 1], _vertexes[0]))
            _isClosed = true
            return this
        }

        fun build(): Polygon {
            validate()

            // in case you forgot to close
            if (!_isClosed) {
                // add last Line
                _sides.add(
                    Line(
                        _vertexes[_vertexes.size - 1],
                        _vertexes[0]
                    )
                )
            }
            return Polygon(_sides, _boundingBox)
        }

        /**
         * Update bounding box with a new point.
         */
        private fun updateBoundingBox(point: PointF) {
            if (_firstPoint) {
                _boundingBox = BoundingBox()
                _boundingBox!!.xMax = point.x
                _boundingBox!!.xMin = point.x
                _boundingBox!!.yMax = point.y
                _boundingBox!!.yMin = point.y
                _firstPoint = false
            } else {
                // set bounding box
                if (point.x > _boundingBox!!.xMax) {
                    _boundingBox!!.xMax = point.x
                } else if (point.x < _boundingBox!!.xMin) {
                    _boundingBox!!.xMin = point.x
                }
                if (point.y > _boundingBox!!.yMax) {
                    _boundingBox!!.yMax = point.y
                } else if (point.y < _boundingBox!!.yMin) {
                    _boundingBox!!.yMin = point.y
                }
            }
        }

        private fun validate() {
            if (_vertexes.size < 3) {
                throw RuntimeException("Polygon must have at least 3 points")
            }
        }
    }

    /**
     * Check if the the given point is inside of the polygon.
     */
    operator fun contains(point: PointF): Boolean {
        if (inBoundingBox(point)) {
            val ray = createRay(point)
            var intersection = 0
            for (side in sides) {
                if (intersect(ray, side)) {
                    intersection++
                }
            }

            /*
             * If the number of intersections is odd, then the point is inside the polygon
			 */if (intersection % 2 != 0) {
                return true
            }
        }
        return false
    }

    /**
     * By given ray and one side of the polygon, check if both lines intersect.
     *
     */
    private fun intersect(ray: Line, side: Line): Boolean {
        val intersectPoint: PointF?

        // if both vectors aren't from the kind of x=1 lines then go into
        if (!ray.isVertical && !side.isVertical) {
            // check if both vectors are parallel. If they are parallel then no intersection point will exist
            if (ray.a - side.a == 0f) {
                return false
            }
            val x = ((side.b - ray.b) / (ray.a - side.a)).toFloat() // x = (b2-b1)/(a1-a2)
            val y = (side.a * x + side.b).toFloat() // y = a2*x+b2
            intersectPoint = PointF(x, y)
        } else if (ray.isVertical && !side.isVertical) {
            val x = ray.start.x
            val y = (side.a * x + side.b).toFloat()
            intersectPoint = PointF(x, y)
        } else if (!ray.isVertical && side.isVertical) {
            val x = side.start.x
            val y = (ray.a * x + ray.b).toFloat()
            intersectPoint = PointF(x, y)
        } else {
            return false
        }

        return side.isInside(intersectPoint) && ray.isInside(intersectPoint)
    }

    /**
     * Create a ray. The ray will be created by given point and on point outside of the polygon.<br></br>
     * The outside point is calculated automatically.
     */
    private fun createRay(point: PointF): Line {
        // create outside point
        val epsilon = (_boundingBox!!.xMax - _boundingBox.xMin) / 10e6
        val outsidePoint = PointF((_boundingBox.xMin - epsilon).toFloat(), _boundingBox.yMin)
        return Line(outsidePoint, point)
    }

    /**
     * Check if the given point is in bounding box
     */
    private fun inBoundingBox(point: PointF): Boolean {
        return !(point.x < _boundingBox!!.xMin || point.x > _boundingBox.xMax || point.y < _boundingBox.yMin || point.y > _boundingBox.yMax)
    }

    private class BoundingBox {
        var xMax = Float.NEGATIVE_INFINITY
        var xMin = Float.NEGATIVE_INFINITY
        var yMax = Float.NEGATIVE_INFINITY
        var yMin = Float.NEGATIVE_INFINITY
    }
}