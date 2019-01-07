package anwarabdullahn.com.villacorp_apps.Activity.DOP

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import anwarabdullahn.com.villacorp_apps.API.API
import anwarabdullahn.com.villacorp_apps.API.APICallback
import anwarabdullahn.com.villacorp_apps.API.APIError
import anwarabdullahn.com.villacorp_apps.Adapter.DOP.DOPAdapter
import anwarabdullahn.com.villacorp_apps.Model.DOP
import anwarabdullahn.com.villacorp_apps.R
import anwarabdullahn.com.villacorp_apps.Utils.LoadingHelper
import kotlinx.android.synthetic.main.activity_dop.*
import org.jetbrains.anko.toast
import org.jetbrains.anko.find

class DOPActivity : AppCompatActivity() {

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
    var loadingScreen: DialogFragment = LoadingHelper.getInstance()
    lateinit var adapter: DOPAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dop)

        toolbar.title = "DOP"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        recyclerView = find(R.id.recyclerView)
        progressBar = find(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this@DOPActivity, LinearLayout.VERTICAL,false)
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
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onBackPressed()
        return super.onOptionsItemSelected(item)
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

        API.service().dop(page.toString()).enqueue(object : APICallback<DOP>(){
            override fun onSuccess(t: DOP) {
                loadingScreen.dismiss()
                totalPage = t.totalpage.toInt()
                adapter = DOPAdapter(t.DOPFinger)
                recyclerView.adapter = adapter
            }

            override fun onError(error: APIError) {
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
        API.service().dop(page.toString()).enqueue(object : APICallback<DOP>(){
            override fun onSuccess(t: DOP) {
                progressBar.visibility = View.GONE
                if (t.DOPFinger.size >= 0){
                    adapter.addmore(t.DOPFinger)
                } else {
                    toast("Nothing to Load!")
                }
            }
            override fun onError(error: APIError?) {
                progressBar.visibility = View.GONE
                toast(error!!.msg)
            }

        })
        page = page
    }
}