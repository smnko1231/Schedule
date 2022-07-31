package app.nakao.shoma.schedule

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.nakao.shoma.schedule.databinding.ActivityMainBinding
import com.airbnb.lottie.LottieAnimationView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.createObject
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    val realm:Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityMainBinding
    var Year = ""
    var Month = ""
    var Day = ""
    var IsComplete = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        //val lottieAnimationCompleteView = findViewById<LottieAnimationView>(R.id.LottieAnimetionCompleteView)
        //val completionButton = findViewById<Button>(R.id.completionButton)
        calendarView.date = System.currentTimeMillis()
        //val floatingActionButton = findViewById<Button>(R.id.floatingActionButton)
        val RV = findViewById<RecyclerView>(R.id.RV)

        val memo:RealmResults<Memo> = read()
        val memoArray: Array<Memo> = memo.toTypedArray()
        val viewList: MutableList<Memo> = mutableListOf()

        val adapter = MemoAdapter(this)
        binding.RV.layoutManager = LinearLayoutManager(this)
        binding.RV.adapter = adapter

        val dt = LocalDate.now()
        val today_year = dt.year//calendarView.get(Calendar.YEAR)
        val today_month = dt.monthValue
        val today_day = dt.dayOfMonth

        val intent_year = intent.getStringExtra("year")
        val intent_month = intent.getStringExtra("month")
        val intent_day = intent.getStringExtra("day")
        var intent_date = dt
        if(intent_day!=null && intent_month!=null && intent_year!=null){
            intent_date = LocalDate.of(intent_year.toInt(),intent_month.toInt(),intent_day.toInt())

        }
        val zoneId = ZoneId.systemDefault()
        val millis = intent_date.atStartOfDay(zoneId).toEpochSecond() * 1000 // あんまりよくないけど
        Log.d("intent_date",millis.toString())
        Log.d("intent_date",System.currentTimeMillis().toString())
        calendarView.date = millis


        if (intent_year != null && intent_month != null && intent_day != null){
            Year = intent_year
            Month = intent_month
            Day = intent_day
        }else{
            Year = today_year.toString()
            Month = today_month.toString()
            Day = today_day.toString()
        }

        val date = "$Year/$Month/$Day"
        Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
        viewList.clear()
        for(m in memo){// 拡張for
            if (m.isComplete == false){
                if(m.year == Year && m.month == Month && m.day == Day){
                    viewList.add(Memo(m.id,m.year,m.month,m.day,m.title,m.content,m.isComplete))
                }
            }else{
                if(m.year == Year && m.month == Month && m.day == Day){
                    viewList.add(Memo(m.id,m.year,m.month,m.day,m.title,m.content,m.isComplete))
                }
            }
        }
        adapter.itemClear()
        adapter.addall(viewList)

        //lottieAnimationCompleteView.setAnimation(R.raw.completion)

        calendarView.setOnDateChangeListener{ view,year,month,dayofmonth ->
            val month2 = month+1
            Year = "$year"
            Month = "$month2"
            Day = "$dayofmonth"
            val date = "$year/$month2/$dayofmonth"

            Log.d("DateChange", Year + Month + Day)
            Toast.makeText(this, date, Toast.LENGTH_SHORT).show()

            viewList.clear()

            for(m in memo){// 拡張for
                if (m.isComplete == false){
                    if(m.year == Year && m.month == Month && m.day == Day){
                        viewList.add(Memo(m.id,m.year,m.month,m.day,m.title,m.content,m.isComplete))

                        //lottieAnimationCompleteView.visibility = View.INVISIBLE

                        Log.d("add", m.day)
                        Log.d("add view", Day)
                    }
                }else{
                    if(m.year == Year && m.month == Month && m.day == Day){
                        viewList.add(Memo(m.id,m.year,m.month,m.day,m.title,m.content,m.isComplete))
                        //lottieAnimationCompleteView.visibility = View.VISIBLE

                        //lottieAnimationCompleteView.playAnimation()

                        Log.d("add", m.day)
                        Log.d("add view", Day)
                    }
                }

            }
            adapter.itemClear()
            adapter.addall(viewList)
        }

        binding.floatingActionButton.setOnClickListener {
            val scheduleEdit = Intent(this,scheduleEdit::class.java).run {
                // Log.d("floatingActionButton", Year + Month + Day)
                putExtra("year",Year)
                putExtra("month",Month)
                putExtra("day",Day)
                putExtra("isComplete",IsComplete)
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
    }

    fun read(): RealmResults<Memo> {
        return realm.where(Memo::class.java).findAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}