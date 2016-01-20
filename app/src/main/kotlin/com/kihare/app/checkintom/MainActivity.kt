package com.kihare.app.checkintom

import android.app.PendingIntent
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text
import java.nio.charset.Charset

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fabric.with(this, Crashlytics())

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = findViewById(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter
    }

    private final val SMART_POSTER_URL = "http://otakumode.com/"
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val tag = (intent?.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as? Tag) ?: return
        val ndefTag = Ndef.get(tag) ?: return

        val message = createSmartPoster(SMART_POSTER_URL)
        if (writeNdefMessage(ndefTag, message)) {
            Toast.makeText(this, "URLを書き込みました。", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "書き込みに失敗しました。", Toast.LENGTH_LONG).show()
        }
    }

    fun createSmartPoster(url:String):NdefMessage {
        val rs = arrayOf(createUriRecord(url), createActionRecord())
        val spPayload = NdefMessage(rs)
        val spRecord = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_SMART_POSTER, ByteArray(0), spPayload.toByteArray())
        return NdefMessage(arrayOf(spRecord))
    }

    private fun createUriRecord(url:String):NdefRecord {
        return NdefRecord.createUri(url)
    }

    private fun  createActionRecord(): NdefRecord {
        val typeField = "act".toByteArray(Charset.forName("US-ASCII"))
        val payload = byteArrayOf(0x00.toByte())
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, typeField, null, payload)
    }

    private fun writeNdefMessage(ndefTag: Ndef, ndefMessage: NdefMessage): Boolean {
        if (!ndefTag.isWritable) return false

        val messageSize = ndefMessage.toByteArray().size
        if (messageSize > ndefTag.maxSize) return false

        try {
            if (!ndefTag.isConnected) {
                ndefTag.connect()
            }
            ndefTag.writeNdefMessage(ndefMessage)
            return true;
        } catch (e: TagLostException ) {
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

    class PlaceholderFragment : Fragment() {
        private var nfcAdapter: NfcAdapter? = null

        override fun onResume() {
            super.onResume()
            val i = Intent(activity, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pi = PendingIntent.getActivity(activity, 0, i, 0)
            nfcAdapter?.enableForegroundDispatch(activity, pi, null, null)
        }

        override fun onPause() {
            super.onPause()
            nfcAdapter?.disableForegroundDispatch(activity)
        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView: View

            nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
            if(nfcAdapter == null || !nfcAdapter!!.isEnabled){
                Toast.makeText(activity, "failed", Toast.LENGTH_SHORT).show()
                return null
            }

            if(arguments.getInt(ARG_SECTION_NUMBER) != 0) {
                // 読み込み
                rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
                Log.d("NFC", "createFragment")
                val activityIntent = activity.intent
                if (TextUtils.equals(activityIntent.action, NfcAdapter.ACTION_TECH_DISCOVERED)) {
                    val idm: String? = getIdm(activityIntent)
                    if (idm != null) {
                        Log.d("NFC", "$idm")
                        (rootView.findViewById(R.id.idm) as TextView).text = idm
                    }

                    val rawMsgs = activityIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    if (rawMsgs != null) {
                        val msgs = arrayListOf<NdefMessage>()
                        for (i in 0..rawMsgs.size - 1) {
                            msgs[i] = rawMsgs[i] as NdefMessage
                        }
                        (rootView.findViewById(R.id.message) as TextView).text = msgs.toString()
                    } else {
                        Log.d("NFC", "message is null.")
                    }
                }

            } else {

                rootView = inflater!!.inflate(R.layout.fragment_main2, container, false)

            }
            return rootView

        }

        private fun getIdm(intent: Intent): String {
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

        companion object {
            private val ARG_SECTION_NUMBER = "section_number"
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "SECTION 1"
                1 -> return "SECTION 2"
            }
            return null
        }
    }
}
