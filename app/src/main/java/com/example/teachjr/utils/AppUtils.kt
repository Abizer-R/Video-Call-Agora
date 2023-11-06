package com.example.teachjr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.WindowManager
import kotlin.math.roundToInt

object AppUtils {

    /**
     * Pixel copy to copy SurfaceView/VideoView into BitMap
     * Work with Surface View, Video View
     * Won't work on Normal View
     */
    fun getBitMapFromSurfaceView(
        videoView: SurfaceView,
        callback: (Bitmap?) -> Unit,
    ) {
        try {
            val bitmap: Bitmap = Bitmap.createBitmap(
                if (videoView.width > 0) videoView.width else getSmallWindowWidth(videoView.context),
                if (videoView.height > 0) videoView.height else getSmallWindowHeight(videoView.context),
                Bitmap.Config.ARGB_8888
            )
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()
            if (androidNaugatAndAbove) {
                PixelCopy.request(
                    videoView, bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            callback(null)
                        }
                        handlerThread.quitSafely()
                    },
                    Handler(handlerThread.looper)
                )
            } else {
                callback(null)
            }
        } catch (e: IllegalArgumentException) {
            callback(null)
            e.printStackTrace()
        }
    }

    private const val BITMAP_SCALE = 0.5f
    private const val BLUR_RADIUS = 25f

    fun blur(context: Context?, image: Bitmap?, bitmapScale: Float = BITMAP_SCALE): Bitmap? {
        if (image == null)
            return null
        val width = (image.width * bitmapScale).roundToInt()
        val height = (image.height * bitmapScale).roundToInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val intrinsicBlur =
            ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        intrinsicBlur.setRadius(BLUR_RADIUS)
        intrinsicBlur.setInput(tmpIn)
        intrinsicBlur.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    fun getSmallWindowHeight(context: Context): Int {
        return (getScreenHeight(context) * 0.22).toInt()
    }

    fun getSmallWindowWidth(context: Context): Int {
        return (getScreenWidth(context) * 0.30).toInt()
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val realSize = Point()
        val realDisplay = windowManager.defaultDisplay
        realDisplay?.getRealSize(realSize)
        return realSize.y
    }

    fun getScreenWidth(context: Context): Int {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val realSize = Point()
        val realDisplay = windowManager.defaultDisplay
        realDisplay?.getRealSize(realSize)
        return realSize.x
    }

    val androidNaugatAndAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}