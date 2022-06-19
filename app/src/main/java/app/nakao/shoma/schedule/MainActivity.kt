package app.nakao.shoma.schedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.RemoteViews
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.nakao.shoma.schedule.databinding.ActivityMainBinding
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import org.bson.BSON.toInt
import java.time.Month

class MainActivity : AppCompatActivity() {

    val realm:Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityMainBinding
    var Year = ""
    var Month = ""
    var Day = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(this.root) }
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.date = System.currentTimeMillis()
        //val floatingActionButton = findViewById<Button>(R.id.floatingActionButton)
        val RV = findViewById<RecyclerView>(R.id.RV)

        val memo:RealmResults<Memo> = read()
        val memoArray: Array<Memo> = memo.toTypedArray()
        val viewList: MutableList<Memo> = mutableListOf()


        calendarView.setOnDateChangeListener{ view,year,month,dayofmonth ->
            val month2 = month+1
            Year = "$year"
            Month = "$month2"
            Day = "$dayofmonth"
            val date = "$year/$month2/$dayofmonth"

            Log.d("DateChange", Year + Month + Day)
            Toast.makeText(this, date, Toast.LENGTH_SHORT).show()

            for(m in memo){// 拡張for
                if(m.year == Year && m.month == Month && m.day == Day){
                    viewList.add(m)
                    Log.d("add", m.day + " add view")
                }
            }

            for (view in viewList){
                Log.d("list", view.day)
            }
        }

        binding.floatingActionButton.setOnClickListener {
            val scheduleEdit = Intent(this,scheduleEdit::class.java).run {
                // Log.d("floatingActionButton", Year + Month + Day)
                putExtra("year",Year)
                putExtra("month",Month)
                putExtra("day",Day)
            }

            startActivity(scheduleEdit)
        }

        /*val memo: Memo? = read()

        val adapter = MemoAdapter(this)
        RV.layoutManager = LinearLayoutManager(this)
        RV.adapter = adapter

        val MemoData: RealmResults<Memo> = realm.where(Memo::class.java).findAll()
        var MemoDataList: List<Memo> = MemoData.subList(0,MemoData.size)

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).getOrientation())
        RV.addItemDecoration(dividerItemDecoration)

        if(memo != null){
            adapter.addall(MemoDataList)
        }*/

        val adapter = MemoAdapter(this)//memo
        RV.layoutManager = LinearLayoutManager(this)
        RV.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).getOrientation())
        RV.addItemDecoration(dividerItemDecoration)

        if (memo != null){
            adapter.addall(viewList)
        }
    }

    fun read(): RealmResults<Memo> {
        return realm.where(Memo::class.java).findAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}