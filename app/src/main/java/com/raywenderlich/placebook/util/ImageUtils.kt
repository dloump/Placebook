import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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

    fun loadBitmapFromFile(context: Context, filename: String):
            Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

}