package academy.learnprogramming.top10downloader

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate() called")
        val downloadData = DownloadData()
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
        Log.d(TAG, "onCreate(): done")
    }

    // First parameter is the String URL
    // Second parameter is for a progress bar, but we are not using it, so Void
    // Third parameter is the string result (XML)
    companion object {
        private class DownloadData : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.d(TAG, "onPostExecute(): parameter is $result")
            }

            override fun doInBackground(vararg url: String?): String {
                Log.d(TAG, "doInBackground(): starts with ${url[0]}")
                val rssFeed = downloadXML(url[0])
                if (rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground(): Error downloading")
                }
                return rssFeed
            }

            private fun downloadXML(urlPath: String?): String {
                val xmlResult = StringBuilder()

                try {
                    val url = URL(urlPath)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    val response = connection.responseCode
                    Log.d(TAG, "downloadXML(): response code was $response")

                    val reader = BufferedReader(InputStreamReader(connection.inputStream))

                    val inputBuffer = CharArray(500)
                    var charsRead = 0
                    while (charsRead >= 0) {
                        charsRead = reader.read(inputBuffer) // End of stream returns a -1
                        if (charsRead > 0) {
                            xmlResult.append(String(inputBuffer, 0, charsRead))
                        }
                    }
                    reader.close()

                    Log.d(TAG, "Received ${xmlResult.length} bytes")
                    return xmlResult.toString()
                } catch (e: MalformedURLException) {
                    Log.e(TAG, "downloadXML(): Invalid URL ${e.message}")
                } catch (e: IOException) {
                    Log.e(TAG, "downloadXML(): IOException reading data: ${e.message}")
                } catch (e: SecurityException) {
                    Log.e(TAG, "downloadXML(): Security Exception. Needs permission? ${e.message}")
                } catch (e: Exception) {
                    Log.e(TAG, "downloadXML(): Unknown error occurred ${e.message}")
                }

                return ""
            }
        }
    }


}
