package kamilmilik.gps_tracker.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.utils.listeners.OnDataAddedListener
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kamil on 07.08.2018.
 */
class LogUtils(var context: Context) {
    private val TAG = LogUtils::class.java.simpleName

    val LOG_FILE_NAME = "log_gps_tracker_${FirebaseAuth.getInstance().currentUser!!.uid}.txt"
    val LOG_FILE_ROOT = File(context.filesDir, "Logs")
    val LOG_FILE = File(LOG_FILE_ROOT, LOG_FILE_NAME)
    val PHP_UPLOAD_ACTION_NAME = "bill"
    val ECHO_JSON_FILE_RESPONSE_CODE = "echo"
    val UPLOAD_FILE_TO_SERVER_URL = "https://grapexs.000webhostapp.com/log_upload/file_upload.php"

    fun appendLog(tag: String, text: String) {
        Log.i(TAG, "save log to file at: ${LOG_FILE_ROOT.path}")
        if (!LOG_FILE_ROOT.exists()) {
            try {
                LOG_FILE_ROOT.mkdirs()
//                    logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            val buf = BufferedWriter(FileWriter(LOG_FILE, true))
            val currentDate = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH).format(Date())
            buf.append("$currentDate $tag: $text")
            Log.i(tag, text)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // TODO zrobić junit testy do tej metody
    fun deleteLogFileAtTimeBetweenGivenHours(startHour: Int, endHour: Int) {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        Log.i(TAG, "deleteLogFileAtTimeBetweenGivenHours() przed if " + hour)
        if (hour in startHour..endHour) {
            if (LOG_FILE.exists()) {
                val isFileDeleted = LOG_FILE.deleteRecursively()
                Log.i(TAG, "deleteLogFileAtTimeBetweenGivenHours() delete file at " + LOG_FILE.absolutePath)
                Log.i(TAG, "deleteLogFileAtTimeBetweenGivenHours() is success? " + isFileDeleted)
            }
            Log.i(TAG, "deleteLogFileAtTimeBetweenGivenHours()")
        }
    }

    fun writeLogToServerAsync(onDataAddedListener: OnDataAddedListener) {
        LogToServerAsync(context, onDataAddedListener).execute()
    }

    class LogToServerAsync(var context: Context, var onDataAddedListener: OnDataAddedListener) : AsyncTask<Void, Void, String>() {
        private var contextReference: WeakReference<Context>? = null

        init {
            contextReference = WeakReference<Context>(context)
        }


        override fun doInBackground(vararg p0: Void?): String {
            val context = contextReference?.get() ?: return ""
            LogUtils(context).writeLogToServerSync()
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            onDataAddedListener.onDataAdded()
        }
    }

    fun writeLogToServerSync() {
        try {
            val sourceFileUri = LOG_FILE.path
            val lineEnd = "\r\n"
            val twoHyphens = "--"
            val boundary = "*****"
            val maxBufferSize = 1 * 1024 * 1024
            val sourceFile = File(sourceFileUri)

            if (sourceFile.isFile) {
                try {
                    val fileInputStream = FileInputStream(sourceFile)
                    val url = URL(UPLOAD_FILE_TO_SERVER_URL)

                    val conn = url.openConnection() as HttpURLConnection
                    conn.doInput = true // Allow Inputs
                    conn.doOutput = true // Allow Outputs
                    conn.useCaches = false // Don't use a Cached Copy
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Connection", "Keep-Alive")
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data")
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary)
                    conn.setRequestProperty(PHP_UPLOAD_ACTION_NAME, sourceFileUri)

                    val dataOutputStream = DataOutputStream(conn.outputStream)

                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + PHP_UPLOAD_ACTION_NAME + "\";filename=\"" + sourceFileUri + "\"" + lineEnd)
                    dataOutputStream.writeBytes(lineEnd)

                    // create a buffer of maximum size
                    var bytesAvailable = fileInputStream.available()

                    var bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    val buffer = ByteArray(bufferSize)

                    // read file and write it into form...
                    var bytesRead = fileInputStream.read(buffer, 0, bufferSize)

                    while (bytesRead > 0) {
                        dataOutputStream.write(buffer, 0, bufferSize)
                        bytesAvailable = fileInputStream.available()
                        bufferSize = Math.min(bytesAvailable, maxBufferSize)
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize)

                    }
                    dataOutputStream.writeBytes(lineEnd)
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                    getJsonEchoResult(conn)

                    fileInputStream.close()
                    dataOutputStream.flush()
                    dataOutputStream.close()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun getJsonEchoResult(conn: HttpURLConnection) {
        var inputStream: InputStream? = null
        inputStream = if (conn.responseCode in 401..599) {
            BufferedInputStream(conn.errorStream)
        } else {
            BufferedInputStream(conn.inputStream)
        }

        val result = StringBuffer()
        result.append(inputStream.bufferedReader().use(BufferedReader::readText))
        val responseJSON = JSONObject(result.toString())
        val echoMessage = responseJSON.getString(ECHO_JSON_FILE_RESPONSE_CODE)
        appendLog(TAG, "writeLogToServerSync() mess " + echoMessage)
    }
}