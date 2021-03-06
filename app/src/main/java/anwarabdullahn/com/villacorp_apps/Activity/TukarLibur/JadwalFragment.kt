package anwarabdullahn.com.villacorp_apps.Activity.TukarLibur

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import anwarabdullahn.com.villacorp_apps.API.AnwAPI
import anwarabdullahn.com.villacorp_apps.API.AnwCallback
import anwarabdullahn.com.villacorp_apps.API.AnwError
import anwarabdullahn.com.villacorp_apps.Adapter.TukarLiburAdapter.TukarLiburJadwalAdapter
import anwarabdullahn.com.villacorp_apps.Model.TukarLibur
import anwarabdullahn.com.villacorp_apps.R
import anwarabdullahn.com.villacorp_apps.Utils.AnwLoadingHelper
import kotlinx.android.synthetic.main.tukar_libur_jadwal.*
import kotlinx.android.synthetic.main.tukar_libur_jadwal.view.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.toast

class JadwalFragment: Fragment() {

    lateinit var contentView : View
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
    lateinit var adapter: TukarLiburJadwalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = inflater.inflate(R.layout.tukar_libur_jadwal,container,false)

        recyclerView = contentView.find(R.id.recyclerView)
        progressBar = contentView.find(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(contentView.context,LinearLayout.VERTICAL,false)
        recyclerView.setHasFixedSize(true)

        loadingScreen.show(fragmentManager,"loading")
        content()
        swipeDown()

        contentView.swipeUp.setOnRefreshListener{
            contentView.swipeUp.scrollTo(0,0)
            contentView.swipeUp.isRefreshing = false
            content()
            reset()
            swipeDown()
        }

        return contentView
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

        when {
            pageparams >= totalPage!! -> page = 1
            else -> page = page
        }

        AnwAPI.service().jadwallibur(page.toString()).enqueue(object : AnwCallback<TukarLibur>(){
            override fun onSuccess(t: TukarLibur) {
                if(t.jadwal_libur.size == 0){
                    frameKosong.visibility = View.VISIBLE
                }
                loadingScreen.dismiss()
                totalPage = t.totalpage.toInt()
                adapter = TukarLiburJadwalAdapter(t.jadwal_libur)
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
        page = page+1
        progressBar.visibility = View.VISIBLE
        AnwAPI.service().jadwallibur(page.toString()).enqueue(object : AnwCallback<TukarLibur>(){
            override fun onSuccess(t: TukarLibur) {
                progressBar.visibility = View.GONE
                if (t.jadwal_libur.size >= 0){
                    adapter.addmore(t.jadwal_libur)
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
}
