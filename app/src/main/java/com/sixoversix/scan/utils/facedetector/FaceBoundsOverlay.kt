package com.sixoversix.scan.utils.facedetector

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat


/**
 * A [View] that renders the results of a face detection operation. It receives a list of face
 * bounds (represented by a list of [RectF]) and draws them, along with their tracking ids.
 */
class FaceBoundsOverlay @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) :
    View(ctx, attrs) {

    private val facesBounds = mutableListOf<FaceBounds>()
    private val anchorPaint = Paint()
    private val idPaint = Paint()
    private val boundsPaint = Paint()
    private var firstDetection : Boolean = false

    /** Listener that gets notified when a face detection result is ready. */
    private var onFaceDetectionResultListener: OnFaceDetectionResultListener? = null


    /** Sets a listener to receive face detection result callbacks. */
    fun setOnFaceDetectionListener(listener: OnFaceDetectionResultListener) {
        onFaceDetectionResultListener = listener
    }

    init {
        anchorPaint.color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)

        idPaint.color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        idPaint.textSize = 40f

        boundsPaint.style = Paint.Style.STROKE
        boundsPaint.color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        boundsPaint.strokeWidth = 4f
    }

    internal fun updateFaces(bounds: List<FaceBounds>) {
        facesBounds.clear()
        if(!firstDetection && bounds.isNotEmpty()) {
            onFaceDetectionResultListener?.onSuccess(bounds)
        }
        facesBounds.addAll(bounds)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        facesBounds.forEach { faceBounds ->
            canvas.drawAnchor(faceBounds.box.center())
            canvas.drawId(faceBounds.id.toString(), faceBounds.box.center())
            canvas.drawBounds(faceBounds.box)
        }
    }

    /** Draws an anchor (dot) at the center of a face. */
    private fun Canvas.drawAnchor(center: PointF) {
     //   drawCircle(center.x, center.y, ANCHOR_RADIUS, anchorPaint)
    }

    /** Draws (Writes) the face's id. */
    private fun Canvas.drawId(faceId: String, center: PointF) {
      //  drawText("face id $faceId", center.x - ID_OFFSET, center.y + ID_OFFSET, idPaint)
    }

    /** Draws bounds around a face as a rectangle. */
    private fun Canvas.drawBounds(box: RectF) {
        drawRect(box, boundsPaint)
    }

    private fun RectF.center(): PointF {
        val centerX = left + (right - left) / 2
        val centerY = top + (bottom - top) / 2
        return PointF(centerX, centerY)
    }

    private fun onError(exception: Exception) {
        onFaceDetectionResultListener?.onFailure(exception)
        Log.e("TAG", "An error occurred while running a face detection", exception)
    }

    /**
     * Interface containing callbacks that are invoked when the face detection process succeeds or
     * fails.
     */
    interface OnFaceDetectionResultListener {
        /**
         * Signals that the face detection process has successfully completed for a camera frame.
         * It also provides the result of the face detection for further potential processing.
         *
         * @param faceBounds Detected faces from a camera frame
         */
        fun onSuccess(faceBounds: List<FaceBounds>) {}

        /**
         * Invoked when an error is encountered while attempting to detect faces in a camera frame.
         *
         * @param exception Encountered [Exception] while attempting to detect faces in a camera
         * frame.
         */
        fun onFailure(exception: Exception) {}
    }

    companion object {
        private const val ANCHOR_RADIUS = 10f
        private const val ID_OFFSET = 50f
    }
}