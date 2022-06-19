package com.tools.rftool.ui.chart

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.roundToInt

class FastLineRenderer(chart: LineDataProvider,
                       animator: ChartAnimator, viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private var mLineBuffer = FloatArray(4)

    override fun drawLinear(c: Canvas, dataSet: ILineDataSet) {


        val entryCount = dataSet.entryCount

        val isDrawSteppedEnabled = dataSet.isDrawSteppedEnabled
        val pointsPerEntryPair = if (isDrawSteppedEnabled) 4 else 2

        val trans: Transformer = mChart.getTransformer(dataSet.axisDependency)

        val phaseY = mAnimator.phaseY

        mRenderPaint.style = Paint.Style.STROKE

        // if the data-set is dashed, draw on bitmap-canvas

        // if the data-set is dashed, draw on bitmap-canvas
        var canvas: Canvas = if (dataSet.isDashedLineEnabled) {
            mBitmapCanvas
        } else {
            c
        }

        mXBounds[mChart] = dataSet

        // if drawing filled is enabled

        // if drawing filled is enabled
        if (dataSet.isDrawFilledEnabled && entryCount > 0) {
            drawLinearFill(c, dataSet, trans, mXBounds)
        }

        // more than 1 color

        // more than 1 color
        if (dataSet.colors.size > 1) {
            val numberOfFloats = pointsPerEntryPair * 2
            if (mLineBuffer.size <= numberOfFloats) mLineBuffer = FloatArray(numberOfFloats * 2)
            val max = mXBounds.min + mXBounds.range
            for (j in mXBounds.min until max) {
                var e: Entry = dataSet.getEntryForIndex(j) ?: continue
                mLineBuffer[0] = e.x
                mLineBuffer[1] = e.y * phaseY
                if (j < mXBounds.max) {
                    e = dataSet.getEntryForIndex(j + 1)
                    if (e == null) break
                    if (isDrawSteppedEnabled) {
                        mLineBuffer[2] = e.x
                        mLineBuffer[3] = mLineBuffer[1]
                        mLineBuffer[4] = mLineBuffer[2]
                        mLineBuffer[5] = mLineBuffer[3]
                        mLineBuffer[6] = e.x
                        mLineBuffer[7] = e.y * phaseY
                    } else {
                        mLineBuffer[2] = e.x
                        mLineBuffer[3] = e.y * phaseY
                    }
                } else {
                    mLineBuffer[2] = mLineBuffer[0]
                    mLineBuffer[3] = mLineBuffer[1]
                }

                // Determine the start and end coordinates of the line, and make sure they differ.
                val firstCoordinateX = mLineBuffer[0]
                val firstCoordinateY = mLineBuffer[1]
                val lastCoordinateX = mLineBuffer[numberOfFloats - 2]
                val lastCoordinateY = mLineBuffer[numberOfFloats - 1]
                if (firstCoordinateX == lastCoordinateX &&
                    firstCoordinateY == lastCoordinateY
                ) continue
                trans.pointValuesToPixel(mLineBuffer)
                if (!mViewPortHandler.isInBoundsRight(firstCoordinateX)) break

                // make sure the lines don't do shitty things outside
                // bounds
                if (!mViewPortHandler.isInBoundsLeft(lastCoordinateX) ||
                    !mViewPortHandler.isInBoundsTop(firstCoordinateY.coerceAtLeast(lastCoordinateY)) ||
                    !mViewPortHandler.isInBoundsBottom(firstCoordinateY.coerceAtMost(lastCoordinateY))
                ) continue

                // get the color that is set for this line-segment
                mRenderPaint.color = dataSet.getColor(j)
                canvas!!.drawLines(mLineBuffer, 0, pointsPerEntryPair * 2, mRenderPaint)
            }
        } else { // only one color per dataset
            if (mLineBuffer.size < (entryCount * pointsPerEntryPair).coerceAtLeast(
                    pointsPerEntryPair
                ) * 2
            ) mLineBuffer = FloatArray(
                (entryCount * pointsPerEntryPair).coerceAtLeast(pointsPerEntryPair) * 4
            )
            var e1: Entry
            var e2: Entry
            e1 = dataSet.getEntryForIndex(mXBounds.min)
            if (e1 != null) {
                var j = 0
                val decimationFactor = ((mXBounds.range - mXBounds.min)/(2*canvas.width.toFloat())).roundToInt().coerceAtLeast(1)
                    .coerceAtMost((mXBounds.range - mXBounds.min) / 250)
                //for (x in mXBounds.min..mXBounds.range + mXBounds.min) {
                for(x in mXBounds.min .. (mXBounds.range + mXBounds.min)) {
                    e1 = dataSet.getEntryForIndex(if (x == 0) 0 else x - 1)
                    e2 = dataSet.getEntryForIndex(x)
                    if (e1 == null || e2 == null) continue
                    mLineBuffer[j++] = e1.x
                    mLineBuffer[j++] = e1.y * phaseY
                    if (isDrawSteppedEnabled) {
                        mLineBuffer[j++] = e2.x
                        mLineBuffer[j++] = e1.y * phaseY
                        mLineBuffer[j++] = e2.x
                        mLineBuffer[j++] = e1.y * phaseY
                    }
                    mLineBuffer[j++] = e2.x
                    mLineBuffer[j++] = e2.y * phaseY
                }
                if (j > 0) {
                    trans.pointValuesToPixel(mLineBuffer)
                    val size =
                        ((mXBounds.range + 1) * pointsPerEntryPair).coerceAtLeast(pointsPerEntryPair) * 2
                    mRenderPaint.color = dataSet.color
                    canvas.drawLines(mLineBuffer, 0, size, mRenderPaint)
                }
            }
        }

        mRenderPaint.pathEffect = null
    }

}