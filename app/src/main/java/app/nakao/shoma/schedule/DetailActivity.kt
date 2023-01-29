package app.nakao.shoma.schedule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import io.realm.Realm
import androidx.appcompat.app.AlertDialog
import app.nakao.shoma.schedule.databinding.ActivityDetailBinding
import java.time.LocalDate
import java.time.Month
import kotlin.time.days

class DetailActivity : AppCompatActivity() {

    val realm: Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityDetailBinding

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        var year = intent.getStringExtra("year")
        var month = intent.getStringExtra("month")
        var day = intent.getStringExtra("day")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val isComplete = intent.getBooleanExtra("isComplete",false)
        val repetitionRule = intent.getIntExtra("repetitionRule",0)
        val repeatWay = intent.getStringExtra("repeatWay")
        val date = LocalDate.of(year!!.toInt(),month!!.toInt(),day!!.toInt())
        var dayOfYear = date.dayOfYear
        var condition:Int

        val dt = LocalDate.now()
        val today_date = LocalDate.of(dt.year,dt.month,dt.dayOfMonth)
        Log.v("today_date",today_date.toString())
        Log.v("schedule_date",date.toString())

        binding.dateTextView.text = year.toString()+"年"+month.toString()+"月"+day.toString()+"日"
        binding.titleTextView.text = intent.getStringExtra("title")
        binding.contentTextView.text = intent.getStringExtra("content")

        binding.deleteButton.setOnClickListener {
            val firstBuilder = AlertDialog.Builder(this)
                firstBuilder// FragmentではActivityを取得して生成
                .setTitle("タイトル:"+binding.titleTextView.text+"\n"+"内容:"+binding.contentTextView.text)
                .setMessage("削除しますか?")
                .setPositiveButton("はい", { dialog, which ->
                    if (repetitionRule != 0){
                        val secondBuilder = AlertDialog.Builder(this)
                            secondBuilder
                            .setTitle("繰り返すタスクを削除しますか？")
                            .setPositiveButton("はい",{dialog,which ->
                                realm.executeTransaction{
                                    if (repeatWay == "日"){
                                        Log.v("repetitionRule",(100/repetitionRule).toString())
                                        for (i in 0..100/repetitionRule){ // 予定は100日後までしか繰り返さない
                                            Log.d("dayofyear",dayOfYear.toString())
                                            Log.d("dayofmonth",day.toString())

                                            month = getMonthAsDayOfYear(dayOfYear)
                                            day = getDayAsDayOfYear(dayOfYear)

                                            Log.d("month",month.toString())

                                            taskDelete(content.toString(),title.toString(),day.toString(),month.toString(),year.toString())
                                            dayOfYear = dayOfYear!! + repetitionRule
                                        }
                                    }
                                    mainIntent(year,month.toString(),day.toString(),title.toString(),content.toString(),isComplete)
                                }
                            }
                            )
                            .setNegativeButton("いいえ",{dialog,which ->
                                realm.executeTransaction{
                                    taskDelete(content.toString(),title.toString(),day.toString(),month.toString(),year.toString())
                                    mainIntent(year,month.toString(),day.toString(),title.toString(),content.toString(),isComplete)
                                }
                            })
                                .show()
                    }else{
                        realm.executeTransaction{
                            taskDelete(content.toString(),title.toString(),day.toString(),month.toString(),year.toString())
                            mainIntent(year,month.toString(),day.toString(),title.toString(),content.toString(),isComplete)
                        }
                    }
                })
                .setNegativeButton("いいえ", { dialog, which ->

                })
                .show()
        }

        binding.editButton.setOnClickListener {
            condition = 3
            val scheduleIntent = Intent(this,scheduleEdit::class.java).run {
                putExtra("year",year)
                putExtra("month",month)
                putExtra("day",day)
                putExtra("title",title)
                putExtra("content",content)
                putExtra("condition",condition)
            }

            realm.executeTransaction{
                taskDelete(content.toString(),title.toString(),day.toString(),month.toString(),year.toString())
                val mainIntent = Intent(this,MainActivity::class.java)
                startActivity(mainIntent)
            }

            startActivity(scheduleIntent)
        }

        binding.backButton.setOnClickListener {
            mainIntent(year,month.toString(),day.toString(),title.toString(),content.toString(),isComplete)
        }

        if (today_date>date && isComplete == false){
            binding.backIncompleteTasksButton.visibility = View.VISIBLE
        }else{
            binding.backIncompleteTasksButton.visibility = View.INVISIBLE
        }

        binding.backIncompleteTasksButton.setOnClickListener {
            var incompleteTasksMonth = 0
            if (dt.month.toString() == "JANUARY"){
                incompleteTasksMonth = 1
            }else if (dt.month.toString() == "FEBRUARY"){
                incompleteTasksMonth = 2
            }else if (dt.month.toString() == "MARCH"){
                incompleteTasksMonth = 3
            }else if (dt.month.toString() == "APRIL"){
                incompleteTasksMonth = 4
            }else if (dt.month.toString() == "MAY"){
                incompleteTasksMonth = 5
            }else if (dt.month.toString() == "JUNE"){
                incompleteTasksMonth = 6
            }else if (dt.month.toString() == "JULY"){
                incompleteTasksMonth = 7
            }else if (dt.month.toString() == "AUGUST"){
                incompleteTasksMonth = 8
            }else if (dt.month.toString() == "SEPTEMBER"){
                incompleteTasksMonth = 9
            }else if (dt.month.toString() == "OCTOBER"){
                incompleteTasksMonth = 10
            }else if (dt.month.toString() == "NOVEMBER"){
                incompleteTasksMonth = 11
            }else if (dt.month.toString() == "DECEMBER"){
                incompleteTasksMonth = 12
            }

            val incompleteTasksIntent = Intent(this,ViewIncompleteTasksActivity::class.java).run {
                putExtra("today_year",dt.year)
                putExtra("today_month",incompleteTasksMonth)
                putExtra("today_day",dt.dayOfMonth)
                putExtra("year",dt.year.toString())
                putExtra("month",incompleteTasksMonth.toString())
                putExtra("day",dt.dayOfMonth.toString())
            }
            Log.v("today_month",incompleteTasksMonth.toString())
            startActivity(incompleteTasksIntent)
        }
    }

    fun read():Memo?{
        return realm.where(Memo::class.java).findFirst()
    }

    fun getMonthAsDayOfYear(dayOfYear: Int): String {
        var month = ""
        if (dayOfYear != null){
            if (dayOfYear!! >= 336){
                month = "12"
            }else if (dayOfYear!! >= 306) {
                month= "11"
            }else if (dayOfYear!! >= 275) {
                month = "10"
            }else if (dayOfYear!! >= 245) {
                month = "9"
            }else if (dayOfYear!! >= 214) {
                month = "8"
            }else if (dayOfYear!! >= 183) {
                month = "7"
            }else if (dayOfYear!! >= 153) {
                month = "6"
            }else if (dayOfYear!! >= 122) {
                month = "5"
            }else if (dayOfYear!! >= 92) {
                month = "4"
            }else if (dayOfYear!! >= 61) {
                month = "3"
            }else if (dayOfYear!! >= 32) {
                month = "2"
            }else{
                month = "1"
            }
        }
        return month
    }

    fun getDayAsDayOfYear(dayOfYear: Int): String{
        var day = ""
        if (dayOfYear != null){
            if (dayOfYear!! >= 336){
                day = (dayOfYear!!-335).toString()
            }else if (dayOfYear!! >= 306) {
                day = (dayOfYear!! - 305).toString()
            }else if (dayOfYear!! >= 275) {
                day = (dayOfYear!! - 274).toString()
            }else if (dayOfYear!! >= 245) {
                day = (dayOfYear!! - 244).toString()
            }else if (dayOfYear!! >= 214) {
                day = (dayOfYear!! - 213).toString()
            }else if (dayOfYear!! >= 183) {
                day = (dayOfYear!! - 182).toString()
            }else if (dayOfYear!! >= 153) {
                day = (dayOfYear!! - 152).toString()
            }else if (dayOfYear!! >= 122) {
                day = (dayOfYear!! - 121).toString()
            }else if (dayOfYear!! >= 92) {
                day = (dayOfYear!! - 91).toString()
            }else if (dayOfYear!! >= 61) {
                day = (dayOfYear!! - 60).toString()
            }else if (dayOfYear!! >= 32) {
                day = (dayOfYear!! - 31).toString()
            }else{
                day = dayOfYear.toString()!!
            }
        }
        return day
    }

    fun taskDelete(content:String,title:String,day:String,month:String,year:String){
        val task_delete_tmp = realm.where(Memo::class.java).equalTo("content",content).findAll()
        val task_delete_tmp2 = task_delete_tmp.where().equalTo("title",title).findAll()
        val task_delete_tmp3 = task_delete_tmp2.where().equalTo("day",day.toString()).findAll()
        val task_delete_tmp4 = task_delete_tmp3.where().equalTo("month",month.toString()).findAll()
        val task_delete = task_delete_tmp4.where().equalTo("year",year.toString()).findAll()
        task_delete.deleteFromRealm(0)
    }

    fun mainIntent(year:String,month: String,day:String,title: String,content: String,isComplete:Boolean){
        val mainIntent = Intent(this,MainActivity::class.java).run {
            val condition = 2
            putExtra("year",year)
            putExtra("month",month)
            putExtra("day",day)
            putExtra("title",title)
            putExtra("content",content)
            putExtra("isComplete",isComplete)
            putExtra("condition",condition)
        }
        startActivity(mainIntent)
    }
}