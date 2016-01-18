package com.kihare.app.checkintom

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    private val MIME_TEXT_PLAIN = "text/plain"

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

    class PlaceholderFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
            if(arguments.getInt(ARG_SECTION_NUMBER) != 0) return rootView

            var nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
            if(nfcAdapter == null || !nfcAdapter.isEnabled){
                Toast.makeText(activity, "failed", Toast.LENGTH_SHORT).show()
            }

            Log.d("NFC", "createFragment")
            val activityIntent = activity.intent
            if(TextUtils.equals(activityIntent.action, NfcAdapter.ACTION_TECH_DISCOVERED)){
                val idm: String? = getIdm(activityIntent)
                if (idm != null) {
                    Log.d("NFC", "$idm")
                    (rootView.findViewById(R.id.idm) as TextView).text = idm
                }

                val rawMsgs = activityIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                if (rawMsgs != null) {
                    val msgs = arrayListOf<NdefMessage>()
                    for (i in 0..rawMsgs.size- 1) {
                        msgs[i] = rawMsgs[i] as NdefMessage
                    }
                    (rootView.findViewById(R.id.message) as TextView).text = msgs.toString()
                } else {
                    Log.d("NFC", "message is null.")
                }
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