package app.nakao.shoma.schedule

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.nakao.shoma.schedule.databinding.ActivityViewIncompleteTasksBinding
import io.realm.Realm
import io.realm.RealmResults
import java.time.LocalDate

class ViewIncompleteTasksActivity : AppCompatActivity() {

    val realm: Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityViewIncompleteTasksBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewIncompleteTasksBinding.inflate(layoutInflater).apply { setContentView(this.root) }
        //setContentView(R.layout.activity_view_incomplete_tasks)

        val memo:RealmResults<Memo> = read()
        val viewList: MutableList<Memo> = mutableListOf()

        val adapter = MemoAdapter(this)
        binding.RV.layoutManager = LinearLayoutManager(this)
        binding.RV.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).getOrientation())
        binding.RV.addItemDecoration(dividerItemDecoration)

        val today_year = intent.getIntExtra("today_year",0)
        val today_month = intent.getIntExtra("today_month",0)
        val today_day = intent.getIntExtra("today_day",0)

        val intent_year = intent.getStringExtra("year")
        val intent_month = intent.getStringExtra("month")
        val intent_day = intent.getStringExtra("day")
        Log.d("today_month",today_year.toString()+today_month.toString()+today_day.toString())

        val today_date = LocalDate.of(today_year,today_month,today_day)


        //viewList.clear()

        for (m in memo){
            if (m.isComplete == false){
                val schedule_date = LocalDate.of(m.year.toInt(),m.month.toInt(),m.day.toInt())
                if (today_date>schedule_date){
                    viewList.add(Memo(m.id, m.year, m.month, m.day, m.title, m.content, m.isComplete))
                    adapter.itemClear()
                    adapter.addall(viewList)
                }
            }
        }

        binding.backMainButton.setOnClickListener {
            val mainIntent = Intent(this,MainActivity::class.java).run {
                putExtra("year",intent_year)
                putExtra("month",intent_month)
                putExtra("day",intent_day)
            }
            startActivity(mainIntent)
        }
    }

    fun read(): RealmResults<Memo> {
        return realm.where(Memo::class.java).findAll()
    }
}