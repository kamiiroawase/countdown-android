package io.github.kaimiroawase.countdown.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import io.github.kaimiroawase.countdown.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** 事件背景图的文件存取:落盘、采样解码、EXIF 旋转、删除。 */
object EventBackgroundStore {
    private const val DIR = "event_bgs"

    private val mutex = Mutex()

    fun backgroundFile(
        context: Context,
        fileName: String,
    ): File = File(File(context.filesDir, DIR), fileName)

    fun deleteFile(
        context: Context,
        fileName: String,
    ) {
        if (fileName.isNotBlank()) backgroundFile(context, fileName).delete()
    }

    suspend fun decode(
        context: Context,
        fileName: String,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            decodeFile(backgroundFile(context, fileName))
        }

    suspend fun save(
        context: Context,
        eventId: Long,
        uri: Uri,
    ) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val bitmap = decodePickedImage(context, uri) ?: return@withLock

                val dir = File(context.filesDir, DIR).apply { mkdirs() }

                val fileName = "bg_${eventId}_${System.currentTimeMillis()}.jpg"

                val file = File(dir, fileName)

                val temp = File(dir, "$fileName.tmp")

                try {
                    temp.outputStream().use { output ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                    }
                } finally {
                    bitmap.recycle()
                }

                if (!temp.renameTo(file)) {
                    temp.delete()

                    return@withLock
                }

                val oldBackground =
                    AppContainer.eventDao.getById(eventId)?.background ?: run {
                        file.delete()

                        return@withLock
                    }

                try {
                    AppContainer.eventDao.updateBackground(eventId, file.name)
                } catch (e: Exception) {
                    file.delete()

                    throw e
                }

                if (oldBackground.isNotBlank() && oldBackground != file.name) {
                    File(dir, oldBackground).delete()
                }
            }
        }
    }

    suspend fun clear(
        context: Context,
        eventId: Long,
    ) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val oldBackground = AppContainer.eventDao.getById(eventId)?.background ?: return@withLock

                if (oldBackground.isNotBlank()) {
                    AppContainer.eventDao.updateBackground(eventId, "")

                    backgroundFile(context, oldBackground).delete()
                }
            }
        }
    }

    private fun decodePickedImage(
        context: Context,
        uri: Uri,
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sample = sampleSizeFor(bounds.outWidth, bounds.outHeight)

        val bitmap =
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return null

        val degrees =
            context.contentResolver.openInputStream(uri)?.use { source ->
                runCatching { rotationDegreesOf(ExifInterface(source)) }.getOrDefault(0)
            } ?: 0

        return rotateIfNeeded(bitmap, degrees)
    }

    private fun decodeFile(file: File): Bitmap? {
        if (!file.exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        BitmapFactory.decodeFile(file.path, bounds)

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sample = sampleSizeFor(bounds.outWidth, bounds.outHeight)

        val bitmap =
            BitmapFactory.decodeFile(
                file.path,
                BitmapFactory.Options().apply { inSampleSize = sample },
            ) ?: return null

        val degrees = runCatching { rotationDegreesOf(ExifInterface(file.path)) }.getOrDefault(0)

        return rotateIfNeeded(bitmap, degrees)
    }

    private fun sampleSizeFor(
        width: Int,
        height: Int,
    ): Int {
        var sample = 1

        while (maxOf(width, height) / (sample * 2) >= 2160) sample *= 2

        return sample
    }

    private fun rotateIfNeeded(
        bitmap: Bitmap,
        degrees: Int,
    ): Bitmap {
        if (degrees == 0) return bitmap

        val rotated =
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                Matrix().apply { postRotate(degrees.toFloat()) },
                true,
            )

        if (rotated != bitmap) bitmap.recycle()

        return rotated
    }

    private fun rotationDegreesOf(exif: ExifInterface): Int =
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
}
