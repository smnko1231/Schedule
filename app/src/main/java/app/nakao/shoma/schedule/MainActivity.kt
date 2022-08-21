package app.nakao.shoma.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    val realm: Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityMainBinding
    var Year = ""
    var Month = ""
    var Day = ""
    var IsComplete = false
    var dayOfYear = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        binding.calendarView.date = System.currentTimeMillis()

        val memo: RealmResults<Memo> = read()
        val viewList: MutableList<Memo> = mutableListOf()

        val adapter = MemoAdapter(this)
        binding.RV.layoutManager = LinearLayoutManager(this)
        binding.RV.adapter = adapter
        val dividerItemDecoration =
            DividerItemDecoration(this, LinearLayoutManager(this).getOrientation())
        binding.RV.addItemDecoration(dividerItemDecoration)

        val dt = LocalDate.now()
        val today_year = dt.year
        val today_month = dt.monthValue
        val today_day = dt.dayOfMonth

        val intent_year = intent.getStringExtra("year")
        val intent_month = intent.getStringExtra("month")
        val intent_day = intent.getStringExtra("day")
        val intent_title = intent.getStringExtra("title")
        val intent_content = intent.getStringExtra("content")
        val intent_isComplete = intent.getBooleanExtra("isComplete", false)
        var intent_date = dt
        val intent_condition = intent.getIntExtra("condition", 0)
        Log.d("condition", intent_condition.toString())
        val csvFormat = DateTimeFormatter.ofPattern("yyyy/[]M/dd")
        if (intent_day != null && intent_month != null && intent_year != null) {
            intent_date = LocalDate.parse(
                "$intent_year/$intent_month/$intent_day",
                csvFormat
            )
        }

        var condition = 0

        val CHANNEL_ID = "channel_id"
        val channel_name = "channel_name"
        val channel_description = "channel_description "
        var notificationId = 0

        ///APIレベルに応じてチャネルを作成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channel_name
            val descriptionText = channel_description
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            /// チャネルを登録
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        if (intent_condition != null) {
            if (intent_condition == 1) {
                Snackbar.make(binding.container, "保存しました!", Snackbar.LENGTH_SHORT).show()
            } else if (intent_condition == 2) {
                Snackbar.make(binding.container, "削除しました!", Snackbar.LENGTH_SHORT)
                    .setAction("元に戻す") {
                        condition = 1
                        val scheduleIntent = Intent(this, scheduleEdit::class.java).run {
                            putExtra("year", intent_year)
                            putExtra("month", intent_month)
                            putExtra("day", intent_day)
                            putExtra("title", intent_title)
                            putExtra("content", intent_content)
                            putExtra("isComplete", intent_isComplete)
                            putExtra("reconstruction", 1)
                            putExtra("condition", condition)
                        }
                        startActivity(scheduleIntent)
                    }
                    .show()
            } else if (intent_condition == 3) {
                Snackbar.make(binding.container, "編集しました!", Snackbar.LENGTH_SHORT).show()
            }
        }

        if (intent_year != null && intent_month != null && intent_day != null) {
            Year = intent_year
            Month = intent_month
            Day = intent_day
        } else {
            Year = today_year.toString()
            Month = today_month.toString()
            Day = today_day.toString()
        }

        val zoneId = ZoneId.systemDefault()
        val millis = intent_date.atStartOfDay(zoneId).toEpochSecond() * 1000
        binding.calendarView.date = millis

        val date = "$Year/$Month/$Day"

        Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()
        viewList.clear()
        val uri = Uri.parse("android.resource://$packageName/${R.raw.notification}")

        for (m in memo) {
            if (m.isComplete == false) {
                if (m.year == Year && m.month == Month && m.day == Day) {
                    viewList.add(Memo(m.id, m.year, m.month, m.day, m.title, m.content, m.isComplete))
                    if (m.year == today_year.toString() && m.month == today_month.toString() && m.day == today_day.toString()){
                        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.check_image)
                            .setContentTitle("今日の予定")
                            .setContentText(m.title+" "+m.content)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSound(uri)

                        with(NotificationManagerCompat.from(this)) {
                            notify(notificationId, builder.build())
                            notificationId += 1
                            Log.d("nofitication", builder.toString())
                        }
                    }
                }
            } else {
                if (m.year == Year && m.month == Month && m.day == Day) {
                    viewList.add(Memo(m.id, m.year, m.month, m.day, m.title, m.content, m.isComplete))
                }
            }
        }
        adapter.itemClear()
        adapter.addall(viewList)

        val tomorrow = today_day+1

        val today_time = LocalDateTime.now()
        val hour = today_time.hour
        Log.d("hour",hour.toString())

        if (hour > 20){
            for (m in memo){
                if (m.isComplete == false){
                    if (m.year == today_year.toString() && m.month == today_month.toString() && m.day == tomorrow.toString()){
                        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.check_image)
                            .setContentTitle("明日の予定")
                            .setContentText(m.title+" "+m.content)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        with(NotificationManagerCompat.from(this)) {
                            notify(notificationId, builder.build())
                            notificationId += 1
                            Log.d("nofitication", builder.toString())
                        }
                    }
                }
            }
        }

        binding.calendarView.setOnDateChangeListener { view, year, month, dayofmonth ->
            val month2 = month + 1
            Year = "$year"
            Month = "$month2"
            Day = "$dayofmonth"

            val date = "$year/$month2/$dayofmonth"

            Toast.makeText(this, date, Toast.LENGTH_LONG).show()

            viewList.clear()

            for (m in memo) {
                if (m.isComplete == false) {
                    if (m.year == Year && m.month == Month && m.day == Day) {
                        viewList.add(Memo(m.id, m.year, m.month, m.day, m.title, m.content, m.isComplete))
                    }
                } else {
                    if (m.year == Year && m.month == Month && m.day == Day) {
                        viewList.add(Memo(m.id, m.year, m.month, m.day, m.title, m.content, m.isComplete))
                    }
                }
            }
            adapter.itemClear()
            adapter.addall(viewList)
        }

        binding.floatingActionButton.setOnClickListener {
            val scheduleEdit = Intent(this, scheduleEdit::class.java).run {
                condition = 1
                putExtra("year", Year)
                putExtra("month", Month)
                putExtra("day", Day)
                putExtra("isComplete", IsComplete)
                putExtra("dayOfYear", dayOfYear)
                putExtra("reconstruction", 0)
                putExtra("condition", condition)
            }
            startActivity(scheduleEdit)
        }

        binding.returnTodayButton.setOnClickListener {
            val millis = dt.atStartOfDay(zoneId).toEpochSecond() * 1000 // あんまりよくないけど
            binding.calendarView.date = millis
            val date = "$today_year/$today_month/$today_day"
            Year = today_year.toString()
            Month = today_month.toString()
            Day = today_day.toString()
            Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
            viewList.clear()
            for (m in memo) {
                if (m.year == today_year.toString() && m.month == today_month.toString() && m.day == today_day.toString()) {
                    viewList.add(Memo(m.id, m.year, m.month, m.day, m.title, m.content, m.isComplete))
                }
            }
            adapter.itemClear()
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