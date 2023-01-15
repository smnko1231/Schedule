package app.nakao.shoma.schedule

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import androidx.appcompat.app.AlertDialog
import app.nakao.shoma.schedule.databinding.ActivityDetailBinding
import app.nakao.shoma.schedule.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.Month

class DetailActivity : AppCompatActivity() {

    val realm: Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityDetailBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        val year = intent.getStringExtra("year")
        val month = intent.getStringExtra("month")
        val day = intent.getStringExtra("day")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val isComplete = intent.getBooleanExtra("isComplete",false)
        val repetitionRule = intent.getIntExtra("repetitionRule",0)
        val repeatWay = intent.getStringExtra("repeatWay")
        var condition = 0

        val dt = LocalDate.now()
        val today_date = LocalDate.of(dt.year,dt.month,dt.dayOfMonth)
        Log.v("today_date",today_date.toString())
        val schedule_date = year?.toInt()?.let { month?.toInt()?.let { it1 -> day?.toInt()?.let { it2 -> LocalDate.of(it, it1, it2) } } }
        Log.v("schedule_date",schedule_date.toString())

        binding.dateTextView.text = intent.getStringExtra("year")+"年"+intent.getStringExtra("month")+"月"+intent.getStringExtra("day")+"日"
        binding.titleTextView.text = intent.getStringExtra("title")
        binding.contentTextView.text = intent.getStringExtra("content")

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("タイトル:"+binding.titleTextView.text+"\n"+"内容:"+binding.contentTextView.text)
                .setMessage("削除しますか?")
                .setPositiveButton("はい", { dialog, which ->
                    val task_delete_tmp = realm.where(Memo::class.java).equalTo("content",content).findAll()
                    val task_delete_tmp2 = task_delete_tmp.where().equalTo("title",title).findAll()
                    val task_delete_tmp3 = task_delete_tmp2.where().equalTo("day",day).findAll()
                    val task_delete_tmp4 = task_delete_tmp3.where().equalTo("month",month).findAll()
                    val task_delete = task_delete_tmp4.where().equalTo("year",year).findAll()

                    realm.executeTransaction{
                        task_delete.deleteFromRealm(0)
                        val mainIntent = Intent(this,MainActivity::class.java).run {
                            condition = 2
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

                    Log.v("repetitionRule",repetitionRule.toString())
                    Log.v("repeatWay",repeatWay.toString())
                    /*if (repetitionRule != null && repeatWay != null){
                        AlertDialog.Builder(this)
                            .setTitle("繰り返すタスクを削除しますか？")
                            .setPositiveButton("はい",{dialog,which ->
                                realm.executeTransaction{
                                    task_delete.deleteFromRealm(0)
                                    val mainIntent = Intent(this,MainActivity::class.java).run {
                                        condition = 2
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
                            )
                            .setNegativeButton("いいえ",{dialog,which ->
                                realm.executeTransaction{
                                    task_delete.deleteFromRealm(0)
                                    val mainIntent = Intent(this,MainActivity::class.java).run {
                                        condition = 2
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
                            })
                    }else{
                        realm.executeTransaction{
                            task_delete.deleteFromRealm(0)
                            val mainIntent = Intent(this,MainActivity::class.java).run {
                                condition = 2
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
                    }*/
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

            val task_delete_tmp = realm.where(Memo::class.java).equalTo("content",content).findAll()
            val task_delete_tmp2 = task_delete_tmp.where().equalTo("title",title).findAll()
            val task_delete_tmp3 = task_delete_tmp2.where().equalTo("day",day).findAll()
            val task_delete_tmp4 = task_delete_tmp3.where().equalTo("month",month).findAll()
            val task_delete = task_delete_tmp4.where().equalTo("year",year).findAll()

            realm.executeTransaction{
                task_delete.deleteFromRealm(0)
                val mainIntent = Intent(this,MainActivity::class.java)
                startActivity(mainIntent)
            }

            startActivity(scheduleIntent)
        }

        binding.backButton.setOnClickListener {
            val mainIntent = Intent(this,MainActivity::class.java).run {
                putExtra("year",year)
                putExtra("month",month)
                putExtra("day",day)
            }
            startActivity(mainIntent)
        }

        if (today_date>schedule_date && isComplete == false){
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
}