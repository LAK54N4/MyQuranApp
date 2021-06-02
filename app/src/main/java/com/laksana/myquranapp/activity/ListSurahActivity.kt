@file:Suppress("DEPRECATION")
package com.laksana.myquranapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.text.format.DateFormat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.gms.location.*
import com.laksana.myquranapp.adapter.SurahAdapter
import com.laksana.myquranapp.databinding.ActivityListSurahBinding
import com.laksana.myquranapp.fragment.FragmentJadwalSholat.Companion.newInstance
import com.laksana.myquranapp.model.ModelSurah
import com.laksana.myquranapp.networking.Api
import com.laksana.myquranapp.utils.GetAddressIntentService
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class ListSurahActivity : AppCompatActivity(), SurahAdapter.OnSelectedData {

    private var surahAdapter: SurahAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelSurah: MutableList<ModelSurah> = ArrayList()
    private var hariIni: String? = null
    private var tanggal: String? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    private lateinit var listSurahBinding: ActivityListSurahBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_list_surah)
        listSurahBinding = ActivityListSurahBinding.inflate(layoutInflater)
        setContentView(listSurahBinding.root)

        supportActionBar?.hide()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data...")

        addressResultReceiver = LocationAddressResultReceiver(Handler())

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEEE", dateNow) as String
        tanggal = DateFormat.format("d MMMM yyyy", dateNow) as String
        //tvToday.text = "$hariIni,"
        listSurahBinding.tvToday.text = "$hariIni,"
        //tvDate.text = tanggal
        listSurahBinding.tvDate.text = tanggal

        val sendDetail = newInstance("detail")

        //llTime.setOnClickListener {
        listSurahBinding.llTime.setOnClickListener{
            sendDetail.show(supportFragmentManager, sendDetail.tag)
            //startActivity(new Intent(MainActivity.this, FragmentMenu.class));
        }



    //    llMosque.setOnClickListener {
        listSurahBinding.llMosque.setOnClickListener {
           //startActivity(Intent(this@ListSurahActivity, SearchSurahActivity::class.java))
        }

        //rvSurah.layoutManager = LinearLayoutManager(this)
        listSurahBinding.rvSurah.layoutManager = LinearLayoutManager(this)

        //rvSurah.setHasFixedSize(true)
        listSurahBinding.rvSurah.setHasFixedSize(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                address
            }
        }
        startLocationUpdates()

        //Methods get data
        listSurah()
    }


    private fun listSurah() {
        progressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_SURAH)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()) {
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelSurah()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.nama = jsonObject.getString("nama")
                            dataApi.type = jsonObject.getString("type")
                            dataApi.ayat = jsonObject.getString("ayat")
                            dataApi.asma = jsonObject.getString("asma")
                            dataApi.arti = jsonObject.getString("arti")
                            dataApi.audio = jsonObject.getString("audio")
                            dataApi.keterangan = jsonObject.getString("keterangan")
                            modelSurah.add(dataApi)
                            showListSurah()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@ListSurahActivity, "Gagal menampilkan data!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(anError: ANError) {
                    progressDialog!!.dismiss()
                    Toast.makeText(
                        this@ListSurahActivity, "Tidak ada jaringan internet!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showListSurah() {
        surahAdapter = SurahAdapter( modelSurah, this)
        //rvSurah!!.adapter = surahAdapter
        listSurahBinding.rvSurah.adapter = surahAdapter
    }

    override fun onSelected(modelSurah: ModelSurah?) {
        val intent = Intent(this, DetailSurahActivity::class.java)
        intent.putExtra("detailSurah", modelSurah)
        startActivity(intent)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val locationRequest = LocationRequest()
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private val address: Unit
        get() {
            if (!Geocoder.isPresent()) {
                Toast.makeText(
                    this@ListSurahActivity,
                    "Can't find current address, ",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent = Intent(this, GetAddressIntentService::class.java)
            intent.putExtra("add_receiver", addressResultReceiver)
            intent.putExtra("add_location", currentLocation)
            startService(intent)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class LocationAddressResultReceiver internal constructor(handler: Handler?) :
        ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                address
            }
            if (resultCode == 1) {
                Toast.makeText(
                    this@ListSurahActivity,
                    "Address not found, ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd)
        }
    }

    private fun showResults(currentAdd: String?) {
        //txtLocation!!.text = currentAdd
        listSurahBinding.txtLocation.text = currentAdd
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }


}