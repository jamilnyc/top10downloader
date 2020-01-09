package academy.learnprogramming.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

private const val URL_KEY = "feedUrlKey"
private const val LIMIT_KEY = "feedLimitKey"

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    // Must be private since the DownloadData class is also private
    // Using "by lazy" so that the xmlListView exists by the first time we need this field
    private var downloadData: DownloadData? = null

    private var lastUrl: String = ""
    private var refresh = false

    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(URL_KEY, feedUrl)
            feedLimit = savedInstanceState.getInt(LIMIT_KEY, feedLimit)
        }

        downloadUrl(feedUrl.format(feedLimit))
        Log.d(TAG, "onCreate is done")
    }

    private fun downloadUrl(feedUrl: String) {
        if (lastUrl == feedUrl && !refresh) {
            // Don't download, since the URL hasn't changed
            Log.d(TAG, "downloadUrl: URL Unchanged - Not downloading again")
            return
        }

        Log.d(TAG, "downloadUrl starting AsyncTask")
        downloadData = DownloadData(this, xmlListView)
        downloadData?.execute(feedUrl)
        Log.d(TAG, "downloadUrl done")
        lastUrl = feedUrl
        refresh = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // called when it is time to inflate the activity's menu
        menuInflater.inflate(R.menu.feeds_menu, menu)
        if (feedLimit == 10) {
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else {
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnuFree -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25 -> {
                // Android doesn't check the item in the UI for you when it is tapped
                // You have to check it yourself
                if (!item.isChecked) {
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                } else {
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit unchanged")
                }
            }
            R.id.mnuRefresh -> refresh = true
            else -> return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_KEY, feedUrl)
        outState.putInt(LIMIT_KEY, feedLimit)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelling the task is important that it doesn't pass data to the activity that is being destroyed
        downloadData?.cancel(true)
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
                val feedAdapter =  FeedAdapter(propContext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter
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
