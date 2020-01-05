package academy.learnprogramming.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageURL: String = ""

    override fun toString(): String {
        return """
            name = $name
            artist = $artist
            releaseDate = $releaseDate
            imageURL = $imageURL
        """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    // Must be private since the DownloadData class is also private
    // Using "by lazy" so that the xmlListView exists by the first time we need this field
    private val downloadData by lazy {DownloadData(this, xmlListView)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate() called")
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=20/xml")
        Log.d(TAG, "onCreate(): done")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelling the task is important that it doesn't pass data to the activity that is being destroyed
        downloadData.cancel(true)
    }

    // First parameter is the String URL
    // Second parameter is for a progress bar, but we are not using it, so Void
    // Third parameter is the string result (XML)
    companion object {
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

                // Parameters: Activity, resource that data goes into, list of objects to display
                val arrayAdapter = ArrayAdapter<FeedEntry>(propContext, R.layout.list_item, parseApplications.applications)
                propListView.adapter = arrayAdapter // can also use the setAdapter() method
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
                return URL(urlPath).readText()
            }
        }
    }


}
