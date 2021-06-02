@file:Suppress("DEPRECATION")

package com.laksana.myquranapp.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.laksana.myquranapp.BuildConfig
import com.laksana.myquranapp.adapter.AyatAdapter
import com.laksana.myquranapp.databinding.ActivityDetailSurahBinding
import com.laksana.myquranapp.model.ModelAyat
import com.laksana.myquranapp.model.ModelSurah
import com.laksana.myquranapp.networking.Api
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException


class DetailSurahActivity : AppCompatActivity() {

    private var nomor: String? = null
    private var nama: String? = null
    private var arti: String? = null
    private var type: String? = null
    private var ayat: String? = null
    private var keterangan: String? = null
    private var audio: String? = null
    private var modelSurah: ModelSurah? = null
    private var ayatAdapter: AyatAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelAyat: MutableList<ModelAyat> = ArrayList()
    private var mHandler: Handler? = null

    private lateinit var detailSurahBinding: ActivityDetailSurahBinding

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_detail_surah)
        detailSurahBinding = ActivityDetailSurahBinding.inflate(layoutInflater)
        setContentView(detailSurahBinding.root)

        //set toolbar
        //toolbar_detail.title = null
        detailSurahBinding.toolbarDetail.title = null

        //setSupportActionBar(toolbar_detail)
        setSupportActionBar(detailSurahBinding.toolbarDetail)
        if (BuildConfig.DEBUG && supportActionBar == null) {
            error("Assertion failed")
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mHandler = Handler()

        //get data dari ListSurah
        modelSurah = intent.getSerializableExtra("detailSurah") as ModelSurah
        if (modelSurah != null) {
            nomor = modelSurah!!.nomor
            nama = modelSurah!!.nama
            arti = modelSurah!!.arti
            type = modelSurah!!.type
            ayat = modelSurah!!.ayat
            audio = modelSurah!!.audio
            keterangan = modelSurah!!.keterangan

            //fabStop.visibility = View.GONE
            detailSurahBinding.fabStop.visibility = View.GONE
            //fabPlay.visibility = View.VISIBLE
            detailSurahBinding.fabPlay.visibility = View.VISIBLE

            //Set text
            /* deprecated
            tvHeader.text = nama
            tvTitle.text = nama
            tvSubTitle.text = arti
            tvInfo.text = "$type - $ayat Ayat "
             */
            //diganti
            detailSurahBinding.tvHeader.text = nama
            detailSurahBinding.tvTitle.text = nama
            detailSurahBinding.tvSubTitle.text = arti
            detailSurahBinding.tvInfo.text = "$type - $ayat Ayat"


            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) tvKet.text = Html.fromHtml(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) detailSurahBinding.tvKet.text = Html.fromHtml(
                keterangan,
                Html.FROM_HTML_MODE_COMPACT
            )
            else {
                //tvKet.text = Html.fromHtml(keterangan)
                detailSurahBinding.tvKet.text = Html.fromHtml(keterangan)
            }

            //get & play Audio
            val mediaPlayer = MediaPlayer()
            //fabPlay.setOnClickListener {
            detailSurahBinding.fabPlay.setOnClickListener {
                try {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer.setDataSource(audio)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                /*
                fabPlay.visibility = View.GONE
                fabStop.visibility = View.VISIBLE
                 */
                detailSurahBinding.fabPlay.visibility = View.GONE
                detailSurahBinding.fabStop.visibility = View.VISIBLE
            }

            //fabStop.setOnClickListener {
            detailSurahBinding.fabStop.setOnClickListener {
                mediaPlayer.stop()
                mediaPlayer.reset()
                /*
                fabPlay.visibility = View.VISIBLE
                fabStop.visibility = View.GONE
                 */
                detailSurahBinding.fabPlay.visibility = View.VISIBLE
                detailSurahBinding.fabStop.visibility = View.GONE
            }
        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data...")

        /*
        rvAyat.layoutManager = LinearLayoutManager(this)
        rvAyat.setHasFixedSize(true)
         */
        detailSurahBinding.rvAyat.layoutManager = LinearLayoutManager(this)
        detailSurahBinding.rvAyat.setHasFixedSize(true)

        //Methods get data
        listAyat()
    }

    private fun listAyat () {
        progressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_AYAT)
            .addPathParameter("nomor", nomor)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()) {
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelAyat()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.arab = jsonObject.getString("ar")
                            dataApi.indo = jsonObject.getString("id")
                            dataApi.terjemahan = jsonObject.getString("tr")
                            modelAyat.add(dataApi)
                            showListAyat()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@DetailSurahActivity, "Gagal menampilkan data!",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onError(anError: ANError) {
                    progressDialog!!.dismiss()
                    Toast.makeText(this@DetailSurahActivity, "Tidak ada jaringan internet!",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showListAyat() {
        ayatAdapter = AyatAdapter(modelAyat)
        //rvAyat!!.adapter = ayatAdapter
        detailSurahBinding.rvAyat.adapter = ayatAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}