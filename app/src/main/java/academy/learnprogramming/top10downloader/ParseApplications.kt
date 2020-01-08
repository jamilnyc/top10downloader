package academy.learnprogramming.top10downloader

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ParseApplications {
    private val TAG = "ParseApplication"
    val applications = ArrayList<FeedEntry>()

    // Parse XML data and create the list of applications
    fun parse(xmlData:String): Boolean {
        Log.d(TAG, "parse() called with $xmlData")
        var status = true // Data was successfully parsed
        var inEntry = false // We want to make sure we're processing data in an <entry>
        var textValue = ""

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = FeedEntry()
            while (eventType != XmlPullParser.END_DOCUMENT) { // While we haven't reached the end
                // Get the name of the current tag
                val tagName = xpp.name?.toLowerCase() // Use the safe-call operator because name is null until it is filled

                when (eventType) {
                    // Beginning of the tag
                    XmlPullParser.START_TAG -> {
                        if (tagName == "entry") {
                            inEntry = true // We're only interested in entry tags
                        }
                    }

                    // Inside the tag
                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {
                        // Only check the textValue at the END_TAG event to make sure
                        // the text is data that we actually want
                        if (inEntry) {
                            // Fill out the data fields
                            when (tagName) {
                                "entry" -> { // Entry is closed and all data should be filled
                                    applications.add(currentRecord)
                                    inEntry = false
                                    currentRecord = FeedEntry()
                                }
                                "name" -> currentRecord.name = textValue
                                "artist" -> currentRecord.artist = textValue
                                "releasedate" -> currentRecord.releaseDate = textValue // Tag name is in lower case
                                "summary" -> currentRecord.summary = textValue
                                "image" -> currentRecord.imageURL = textValue
                            }
                        }
                    }
                }

                eventType = xpp.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }

        return status
    }
}