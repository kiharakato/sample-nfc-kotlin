package com.kihare.app.checkintom

import android.app.PendingIntent
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.EditText
import android.widget.Toast
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private var editView: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        editView = findViewById(R.id.text) as EditText

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null || !(nfcAdapter as NfcAdapter).isEnabled) {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // nfcからのintentかのチェック
        val tag = (intent?.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as? Tag) ?: return
        val ndefTag = Ndef.get(tag) ?: return

        val message = createSmartPoster() ?: return
        if (writeNdefMessage(ndefTag, message)) {
            Toast.makeText(this, "URLを書き込みました。", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "書き込みに失敗しました。", Toast.LENGTH_LONG).show()
        }
    }

    fun createSmartPoster(): NdefMessage? {
        val typeField = "act".toByteArray(Charset.forName("US-ASCII"))
        val payload = byteArrayOf(0x00.toByte())
        val record = NdefRecord(NdefRecord.TNF_WELL_KNOWN, typeField, null, payload)

        val name = editView!!.text ?: return null
        val rs = arrayOf(NdefRecord.createUri(name.toString()), record)
        val spPayload = NdefMessage(rs)
        val spRecord = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_SMART_POSTER, ByteArray(0), spPayload.toByteArray())
        return NdefMessage(arrayOf(spRecord))
    }

    private fun writeNdefMessage(ndefTag: Ndef, ndefMessage: NdefMessage): Boolean {
        if (!ndefTag.isWritable) return false

        // 書き込み可能かのチェック
        val messageSize = ndefMessage.toByteArray().size
        if (messageSize > ndefTag.maxSize) return false

        try {
            if (!ndefTag.isConnected) {
                ndefTag.connect()
            }
            // 書き込み
            ndefTag.writeNdefMessage(ndefMessage)
            return true;
        } catch (e: TagLostException) {
            return false;
        } catch (e: IOException) {
            return false;
        } catch (e: FormatException) {
            return false;
        } finally {
            try {
                ndefTag.close()
            } catch (e: IOException) {
            }
        }
    }

    private var nfcAdapter: NfcAdapter? = null
    override fun onResume() {
        super.onResume()
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pi = PendingIntent.getActivity(this, 0, i, 0)
        nfcAdapter?.enableForegroundDispatch(this, pi, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }
}
