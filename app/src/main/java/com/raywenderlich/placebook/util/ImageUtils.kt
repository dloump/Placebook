import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

//declaring object
object ImageUtils {
    //saving Bitmap to permanent storage
    fun saveBitmapToFile(context: Context, bitmap: Bitmap,
                         filename: String) {
        //creating val to hold the image data
        val stream = ByteArrayOutputStream()
        //writing image bitmap to stream object
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        //converting stream into an array of bytes
        val bytes = stream.toByteArray()
        //writing the bytes to a file
        saveBytesToFile(context, bytes, filename)
    }
    //saving the bytes to a file
    private fun saveBytesToFile(context: Context, bytes:
    ByteArray, filename: String) {
        val outputStream: FileOutputStream
        //try/catch to prevent crashes from possible thrown exceptions
        try {
            //opening a fileoutput stream & writing data to private area
            outputStream = context.openFileOutput(filename,
                Context.MODE_PRIVATE)
            //bytes written to output stream & then closing stream
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateInSampleSize(
            width: Int,
            height: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                    halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun decodeFileToSize(
            filePath: String,
            width: Int,
            height: Int
    ): Bitmap {
        //loading size of image
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        //requested width & height options updated with insamplesize
        options.inSampleSize = calculateInSampleSize(
                options.outWidth, options.outHeight, width, height)
        //setting to false to load the full image
        options.inJustDecodeBounds = false
        //loading the downsampled image from the file & returning it
        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width,
                img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    @Throws(IOException::class)
    fun rotateImageIfRequired(context: Context, img: Bitmap,
                              selectedImage: Uri): Bitmap {
        val input: InputStream? =
                context.contentResolver.openInputStream(selectedImage)
        val path = selectedImage.path
        val ei: ExifInterface = when {
            Build.VERSION.SDK_INT > 23 && input != null ->
                ExifInterface(input)
            path != null -> ExifInterface(path)
            else -> null
        } ?: return img
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img,
                    90.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img,
                    180.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img,
                    270.0f) ?: img
            else -> img
        }
    }

    fun decodeUriStreamToSize(
            uri: Uri,
            width: Int,
            height: Int,
            context: Context
    ): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options: BitmapFactory.Options
            //opening inputstream for the Uri
            inputStream = context.contentResolver.openInputStream(uri)
            //if inputStream is not null, processing continues
            if (inputStream != null) {
                //determining image size
                options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(inputStream, null, options)
                //input stream is closed & opened again, & checked for null
                inputStream.close()
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    //loading from the stream using downsampling options &
                    //returning to the caller
                    options.inSampleSize = calculateInSampleSize(
                            options.outWidth, options.outHeight,
                            width, height)
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                            inputStream, null, options)
                    inputStream.close()
                    return bitmap
                }
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            //closing the inputStream once it’s opened, even if an exception is thrown
            inputStream?.close()
        }
    }

    fun loadBitmapFromFile(context: Context, filename: String):
            Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File {
        val timeStamp =
                SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timeStamp + "_"
        val filesDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpg", filesDir)
    }

}