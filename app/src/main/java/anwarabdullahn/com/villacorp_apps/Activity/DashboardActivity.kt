package anwarabdullahn.com.villacorp_apps.Activity

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import anwarabdullahn.com.villacorp_apps.Activity.Fragment.*
import anwarabdullahn.com.villacorp_apps.R
import kotlinx.android.synthetic.main.activity_dashboard.*
import anwarabdullahn.com.villacorp_apps.API.AnwAPI
import anwarabdullahn.com.villacorp_apps.API.AnwCallback
import anwarabdullahn.com.villacorp_apps.API.AnwError
import anwarabdullahn.com.villacorp_apps.Model.Pesans
import anwarabdullahn.com.villacorp_apps.Request.PesanRequest
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import org.jetbrains.anko.toast
import kotlinx.android.synthetic.main.dialog_home_menu_bottomsheet.*
import org.jetbrains.anko.find


class DashboardActivity : AppCompatActivity() {

    val manager = supportFragmentManager
    val body = PesanRequest()
    var page: Int? = 1

    lateinit var bottomSheetHome : ConstraintLayout
    lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                fragmentHome()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_info -> {
                fragmentInfo()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_message -> {
                fragmentNotif()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_panduan -> {
                fragmentPanduan()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                fragmentProfile()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        if (Build.VERSION.SDK_INT >= 23)
        {
            if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),1)
            }
        }
        bottomSheetHome = find(R.id.bottomSheetHome)
        bottomSheetBehavior = from(bottomSheetHome)
        bottomSheetBehavior.state = STATE_HIDDEN
        minimizeBtn.setOnClickListener {
            bottomSheetBehavior.state = STATE_HIDDEN
        }

        fragmentHome()
        displayData()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onResume() {
        displayData()
        super.onResume()
        val bundle = intent!!.getStringExtra("result")
        if (bundle != null && bundle != ""){
            Handler().postDelayed(Runnable {
                SnackbarManager.show(
                    Snackbar.with(applicationContext)
                        .text(bundle)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .actionLabel("OK"),
                    this@DashboardActivity
                )
            }, 1500)
            intent.putExtra("result", "")
        }
    }

    override fun onPause() {
        super.onPause()
    }

    fun fragmentHome(){
        val transaction = manager.beginTransaction()
        val fragment = HomeFragment()
        transaction.replace(R.id.holderFragment,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun fragmentProfile(){
        val transaction = manager.beginTransaction()
        val fragment = ProfileFragment()
        transaction.replace(R.id.holderFragment,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun fragmentNotif(){
        val transaction = manager.beginTransaction()
        val fragment = PesanFragment()
        transaction.replace(R.id.holderFragment,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun fragmentInfo(){
        val transaction = manager.beginTransaction()
        val fragment = InfoFragment()
        transaction.replace(R.id.holderFragment,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun fragmentPanduan(){
        val transaction = manager.beginTransaction()
        val fragment = PanduanFragment()
        transaction.replace(R.id.holderFragment,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    fun displayData(){
        body.page = page
        AnwAPI.service().pesan(body).enqueue(object : AnwCallback<Pesans>() {
            override fun onSuccess(t: Pesans) {
                if(t.jumlah_pesan == "0" ){
                    badgeTxt.visibility = View.GONE
                } else {
                    badgeTxt.visibility = View.VISIBLE
                    badgeTxt.text = t.jumlah_pesan
                }
            }

            override fun onError(error: AnwError?) {
                toast(error!!.msg)
            }

        })
    }
}
