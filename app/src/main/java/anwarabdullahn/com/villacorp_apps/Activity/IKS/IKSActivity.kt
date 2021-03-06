package anwarabdullahn.com.villacorp_apps.Activity.IKS

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import anwarabdullahn.com.villacorp_apps.API.AnwAPI
import anwarabdullahn.com.villacorp_apps.API.AnwCallback
import anwarabdullahn.com.villacorp_apps.API.AnwError
import anwarabdullahn.com.villacorp_apps.Adapter.IKS.IKSAdapter
import anwarabdullahn.com.villacorp_apps.Model.IKSs
import anwarabdullahn.com.villacorp_apps.R
import anwarabdullahn.com.villacorp_apps.Utils.AnwLoadingHelper
import kotlinx.android.synthetic.main.activity_iks.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class IKSActivity : AppCompatActivity() {

    internal var isLoading: Boolean = false
    internal var pastVisibleItems : Int = 0
    internal var visibleItemCount : Int = 0
    internal var totalItemCount : Int = 0
    internal var previousTotal : Int = 0
    internal var viewThreshold: Int = 12
    var page: Int = 1
    var totalPage: Int? = 0
    lateinit var progressBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    var loadingScreen: DialogFragment = AnwLoadingHelper.getInstance()
    lateinit var adapter: IKSAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iks)

        toolbar.title = "Izin Keluar Sementara"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        recyclerView = find(R.id.recyclerView)
        progressBar = find(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this@IKSActivity, LinearLayout.VERTICAL,false)
        recyclerView.setHasFixedSize(true)

        loadingScreen.show(supportFragmentManager,"loading")
        content()
        swipeDown()

        swipeUp.setOnRefreshListener{
            swipeUp.scrollTo(0,0)
            swipeUp.isRefreshing = false
            content()
            reset()
            swipeDown()
        }

        izinKeluarSementaraBtn.setOnClickListener {
            val intent = Intent(this@IKSActivity, DoIKS::class.java)
            startActivity(intent)
        }

    }

    internal fun reset(){
        page = 1
        isLoading = false
        pastVisibleItems = 0
        visibleItemCount = 0
        totalItemCount = 0
        previousTotal = 0
        viewThreshold = 12
    }

    fun content(){
        val pageparams: Int = page

        page = when {
            pageparams >= totalPage!! -> 1
            else -> page
        }

        AnwAPI.service().iks(page.toString()).enqueue(object : AnwCallback<IKSs>(){
            override fun onSuccess(t: IKSs) {
                if(t.IKS.size == 0){
                    frameKosong.visibility = View.VISIBLE
                }
                loadingScreen.dismiss()
                totalPage = t.TotalPage.toInt()
                adapter = IKSAdapter(t.IKS)
                recyclerView.adapter = adapter
            }

            override fun onError(error: AnwError) {
                loadingScreen.dismiss()
                toast(error.msg)
            }

        })
    }


    fun swipeDown(){
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                visibleItemCount = recyclerView.layoutManager!!.childCount
                totalItemCount = recyclerView.layoutManager!!.itemCount
                pastVisibleItems = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                if(dy>0){
                    if (isLoading){
                        if (totalItemCount>previousTotal){
                            isLoading = false
                            previousTotal = totalItemCount
                        }
                    }
                    if (!isLoading&&(totalItemCount-visibleItemCount)<=(pastVisibleItems+viewThreshold)){
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    fun loadMore(){
        page += 1
        progressBar.visibility = View.VISIBLE
        AnwAPI.service().iks(page.toString()).enqueue(object : AnwCallback<IKSs>(){
            override fun onSuccess(t: IKSs) {
                progressBar.visibility = View.GONE
                if (t.IKS.size >= 0){
                    adapter.addmore(t.IKS)
                } else {
                    toast("Nothing to Load!")
                }
            }
            override fun onError(error: AnwError?) {
                progressBar.visibility = View.GONE
                toast(error!!.msg)
            }

        })
        page = page
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
