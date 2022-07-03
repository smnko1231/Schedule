package app.nakao.shoma.schedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.nakao.shoma.schedule.databinding.ActivityMainBinding
import com.airbnb.lottie.LottieAnimationView
import io.realm.Realm
import io.realm.RealmResults

class MainActivity : AppCompatActivity() {

    val realm:Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityMainBinding
    var Year = ""
    var Month = ""
    var Day = ""
    var IsComplete = false

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
                        viewList.add(Memo(m.year,m.month,m.day,m.title,m.content))

                        //lottieAnimationCompleteView.visibility = View.INVISIBLE

                        Log.d("add", m.day)
                        Log.d("add view", Day)
                    }
                }else{
                    if(m.year == Year && m.month == Month && m.day == Day){
                        viewList.add(Memo(m.year,m.month,m.day,m.title,m.content))
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