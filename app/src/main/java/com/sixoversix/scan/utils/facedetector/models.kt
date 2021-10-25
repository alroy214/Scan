package com.sixoversix.scan.utils.facedetector

import android.util.Size
import com.sixoversix.scan.utils.facedetector.LensFacing

data class Frame(
    @Suppress("ArrayInDataClass") val data: ByteArray?,
    val rotation: Int,
    val size: Size,
    val format: Int,
    val lensFacing: LensFacing
)