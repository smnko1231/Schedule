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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.nakao.shoma.schedule.databinding.ActivityMainBinding
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
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
        val container = findViewById<ConstraintLayout>(R.id.container)

        val memo:RealmResults<Memo> = read()
        val memoArray: Array<Memo> = memo.toTypedArray()
        val viewList: MutableList<Memo> = mutableListOf()

        val adapter = MemoAdapter(this)
        binding.RV.layoutManager = LinearLayoutManager(this)
        binding.RV.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).getOrientation())
        binding.RV.addItemDecoration(dividerItemDecoration)

        val dt = LocalDate.now()
        val today_year = dt.year//calendarView.get(Calendar.YEAR)
        val today_month = dt.monthValue
        val today_day = dt.dayOfMonth

        val intent_year = intent.getStringExtra("year")
        val intent_month = intent.getStringExtra("month")
        val intent_day = intent.getStringExtra("day")
        val intent_title = intent.getStringExtra("title")
        val intent_content = intent.getStringExtra("content")
        val intent_isComplete = intent.getBooleanExtra("isComplete",false)
        var intent_date = dt
        val intent_condition = intent.getIntExtra("condition",0)
        if(intent_day!=null && intent_month!=null && intent_year!=null){
            intent_date = LocalDate.of(intent_year.toInt(),intent_month.toInt(),intent_day.toInt())
        }

        if (intent_condition != null){
            if (intent_condition == 1){
                Snackbar.make(container,"保存しました!",Snackbar.LENGTH_SHORT).show()
            }else if (intent_condition == 2){
                Snackbar.make(container,"削除しました!",Snackbar.LENGTH_SHORT)
                    .setAction("元に戻す"){
                        val scheduleIntent = Intent(this,scheduleEdit::class.java).run {
                            putExtra("year",intent_year)
                            putExtra("month",intent_month)
                            putExtra("day",intent_day)
                            putExtra("title",intent_title)
                            putExtra("content",intent_content)
                            putExtra("isComplete",intent_isComplete)
                        }
                        startActivity(scheduleIntent)
                    }
                    .show()
            }else if (intent_condition == 3){
                Snackbar.make(container,"編集しました!",Snackbar.LENGTH_SHORT).show()
            }
        }
        val zoneId = ZoneId.systemDefault()
        val millis = intent_date.atStartOfDay(zoneId).toEpochSecond() * 1000 // あんまりよくないけど
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
            Toast.makeText(this, date, Toast.LENGTH_LONG).show()

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
    }

    fun read(): RealmResults<Memo> {
        return realm.where(Memo::class.java).findAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}