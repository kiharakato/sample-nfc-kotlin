package com.kihare.app.checkintom

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.UnsupportedEncodingException

class TwitterLoginActivity : AppCompatActivity() {

    private val TAG = "CheckInTomStudy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitter_login)

        // nfcが使える端末かのcheck
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
            return
        }

        AsyncHttpRequest().execute()
    }

    inner class AsyncHttpRequest() : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            var name = ""

            // nfcデータの取得
            val idm: String? = getIdm()
            if (idm != null) Log.d(TAG, "Get nfc id: $idm")
            val rawMsgs = intent!!.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            Log.d(TAG, rawMsgs.size.toString())
            if (rawMsgs != null) {
                if (rawMsgs.size != 0) {
                    name = readText((rawMsgs[0] as NdefMessage).records[0])
                }
            } else {
                Log.d(TAG, "message is null.")
            }

            // tweet処理
            Log.d(TAG, name + ".")
            try {
                val cb = ConfigurationBuilder()
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey("****")
                        .setOAuthConsumerSecret("****")
                        .setOAuthAccessToken("****-****")
                        .setOAuthAccessTokenSecret("****")

                val tf = TwitterFactory(cb.build())
                val twitter = tf.instance
                twitter.verifyCredentials()
                twitter.updateStatus("$name, test")
            } catch(e: Exception) {
                Log.d(TAG, e.message)
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            finish()
        }

        private fun getIdm(): String {
            var idm: String = ""
            val idmByte = StringBuffer()
            val rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            if (rawIdm != null) {
                for (i in 0..rawIdm.size - 1) {
                    idmByte.append(Integer.toHexString(rawIdm[i].toInt()))
                }
                idm = idmByte.toString()
            }
            return idm
        }

        @Throws(UnsupportedEncodingException::class)
        private fun readText(record: NdefRecord): String {
            val payload = record.payload
            val languageCodeLength = 4
            return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 8, "UTF-8")
        }
    }

}

