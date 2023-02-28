//package otus.homework.customview
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Paint
//import android.graphics.Path
//import android.util.AttributeSet
//import android.view.MotionEvent
//import android.view.View
//import otus.homework.customview.data.Segment
//
//class CircleDiagramView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
//
//    private val segments = listOf<Segment>()
//    var centerX: Float = 0F
//    var centerY: Float = 0F
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        centerX = (width / 2).toFloat()
//        centerY = (height / 2).toFloat()
//        // Calculate total value of segments
//        val totalValue = segments.map { it.value }.sum()
//        // Define start and sweep angles
//        var startAngle = 0f
//        var sweepAngle: Float
//        // Iterate through segments and draw each one
//        segments.forEach { segment ->
//            sweepAngle = 360f * (segment.value / totalValue)
//            // Create path for segment
//            val path = Path()
//            path.arcTo(
//                left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(),
//                startAngle, sweepAngle - startAngle,
//                true
//            )
//            path.lineTo(centerX, centerY)
//            path.close()
//            // Create paint for segment
//            val paint = Paint().apply {
//                color = segment.color
//                style = Paint.Style.FILL
//            }
//            // Draw segment
//            canvas.drawPath(path, paint)
//            // Update start angle for next segment
//            startAngle += sweepAngle
//        }
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val touchX = event.x
//        val touchY = event.y
//        val distance = calculateDistance(touchX, touchY)
//        val angle = calculateAngle(touchX, touchY)
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                // Loop through segments and check if touch event is within a segment
//                segments.forEach { segment ->
//                    val segmentDistance = distance - segmentPadding
//                    if (segmentDistance > segment.innerRadius && segmentDistance < segment.outerRadius) {
//                        // Expand segment
//                        segment.expandedRadius = segmentDistance
//                        invalidate()
//                    }
//                }
//            }
//            MotionEvent.ACTION_MOVE -> {
//                // Loop through segments and update expanded radius
//                segments.forEach { segment ->
//                    if (segment.expandedRadius != 0f) {
//                        segment.expandedRadius = distance - segmentPadding
//                        invalidate()
//                    }
//                }
//            }
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                // Reset expanded radius for all segments
//                segments.forEach { it.expandedRadius = 0f }
//                invalidate()
//            }
//        }
//        return true
//    }
//
//    private fun calculateAngle(x: Float, y: Float): Float {
//        return Math.toDegrees(Math.atan2((y - centerY).toDouble(), (x - centerX).toDouble())).toFloat()
//    }
//
//    private fun calculateDistance(x: Float, y: Float): Float {
//        return Math.sqrt(Math.pow((x - centerX).toDouble(), 2.0) + Math.pow((y - centerY).toDouble(), 2.0)).toFloat()
//    }
//}