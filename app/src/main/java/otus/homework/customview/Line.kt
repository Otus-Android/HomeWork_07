package otus.homework.customview

import android.graphics.PointF

class Line(start: PointF, end: PointF) {
    private val _start: PointF
    private val _end: PointF

    /**
     * y = **A**x + B
     */
    var a = Float.NaN

    /**
     * y = Ax + **B**
     */
    var b = Float.NaN

    /**
     * Indicate whereas the line is vertical.
     * For example, line like x=1 is vertical, in other words parallel to axis Y.
     * In this case the A is (+/-)infinite.
     */
    var isVertical = false

    init {
        _start = start
        _end = end
        if (_end.x - _start.x != 0f) {
            a = ((_end.y - _start.y) / (_end.x - _start.x))
            b = _start.y - a * _start.x
        } else {
            isVertical = true
        }
    }

    /**
     * Indicate whereas the point lays on the line.
     */
    fun isInside(point: PointF): Boolean {
        val maxX: Float = if (_start.x > _end.x) _start.x else _end.x
        val minX: Float = if (_start.x < _end.x) _start.x else _end.x
        val maxY: Float = if (_start.y > _end.y) _start.y else _end.y
        val minY: Float = if (_start.y < _end.y) _start.y else _end.y
        return point.x in minX..maxX && point.y >= minY && point.y <= maxY
    }

    val start: PointF
        get() = _start

    val end: PointF
        get() = _end

    override fun toString(): String {
        return java.lang.String.format("%s-%s", _start.toString(), _end.toString())
    }
}