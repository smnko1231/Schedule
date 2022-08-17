package app.nakao.shoma.schedule

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.CaseMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import app.nakao.shoma.schedule.databinding.ActivityScheduleEditBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import java.time.DayOfWeek
import java.time.Month
import java.time.ZoneId
import java.util.*

class scheduleEdit : AppCompatActivity() {

    val realm:Realm = Realm.getDefaultInstance()

    private lateinit var binding: ActivityScheduleEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_edit)
        binding = ActivityScheduleEditBinding.inflate(layoutInflater).apply { setContentView(this.root) }
        val titleEdit = findViewById<EditText>(R.id.titleEdit)
        val contentsEdit = findViewById<EditText>(R.id.contentsEdit)
        val backButton = findViewById<Button>(R.id.backbutton)
        val container = findViewById<ConstraintLayout>(R.id.container)
        val repeatDaySwich = findViewById<Switch>(R.id.repeatDaySwich)
        val repeatMonthSwich = findViewById<Switch>(R.id.repeatMonthSwich)
        val repeatWeekSwich = findViewById<Switch>(R.id.repeatWeekSwich)

        val memo: Memo? = read()

        val intent_title = intent.getStringExtra("title")
        val intent_content = intent.getStringExtra("content")

        var repeat = 0

        if (intent_title != null && intent_content != null){
            titleEdit.setText(intent_title.toString())
            contentsEdit.setText(intent_content.toString())
        }

        binding.savebutton.setOnClickListener {
            val title: String = titleEdit.text.toString()
            val content: String = contentsEdit.text.toString()
            var year = intent.getStringExtra("year")
            var month = intent.getStringExtra("month")
            var day = intent.getStringExtra("day")
            var day_int = day?.toInt()
            var month_int = month?.toInt()
            val isComplete = intent.getBooleanExtra("isComplete",false)
            var intent_condition = intent.getIntExtra("condition",1)

            if (title.equals("")){
                AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                    .setTitle("タイトルが入力されていません")
                    .setMessage("タイトルを入力してください")
                    .setPositiveButton("OK", { dialog, which ->

                    })
                    .show()
            }else if (content.equals("")){
                AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                    .setTitle("内容が入力されていません")
                    .setMessage("内容を入力してください")
                    .setPositiveButton("OK", { dialog, which ->

                    })
                    .show()
            }else{
                if (year != null && month != null && day != null) {
                    if (repeatDaySwich.isChecked == false &&  repeatWeekSwich.isChecked == false && repeatMonthSwich.isChecked == false){
                        save(year.toString(),month.toString(),day.toString() ,title,content,isComplete)
                    }else if (repeatDaySwich.isChecked == true &&  repeatWeekSwich.isChecked == false && repeatMonthSwich.isChecked == false){
                        Log.d("repeat",repeat.toString())
                        for (i in 1..31){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete)
                            if (day_int != null){
                                day_int++
                            }
                            day = day_int.toString()
                            Log.d("day",day.toString())
                        }
                    }else if (repeatDaySwich.isChecked == false &&  repeatWeekSwich.isChecked == true && repeatMonthSwich.isChecked == false){
                        for (i in 1..10){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete)
                            if (day_int != null){
                                day_int = day_int+7
                            }
                            day = day_int.toString()
                        }
                    }else if (repeatDaySwich.isChecked == false &&  repeatWeekSwich.isChecked == false && repeatMonthSwich.isChecked == true){
                        for (i in 1..12){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete)
                            if (month_int != null){
                                month_int++
                            }
                            month = month_int.toString()
                        }
                    }
                }

                val mainIntent = Intent(this,MainActivity::class.java).run {
                    putExtra("year",year)
                    putExtra("month",month)
                    putExtra("day",day)
                    putExtra("condition",intent_condition)
                }
                startActivity(mainIntent)
            }
        }

        binding.backbutton.setOnClickListener {
            val year = intent.getStringExtra("year")
            val month = intent.getStringExtra("month")
            val day = intent.getStringExtra("day")
            val isComplete = intent.getBooleanExtra("isComplete",false)

            if (year != null && month != null && day != null && intent_title != null && intent_content != null) {
                save(year,month,day ,intent_title.toString(),intent_content.toString(),isComplete)
            }

            val mainIntent = Intent(this,MainActivity::class.java).run {
                putExtra("year",year)
                putExtra("month",month)
                putExtra("day",day)
            }
            startActivity(mainIntent)
        }

        /*if (titleEdit.text == null || contentsEdit.text == null){
            Log.d("isEnabled",titleEdit.text.toString())
            binding.savebutton.isEnabled = false
            binding.savebutton.setBackgroundColor(Color.rgb(125,125,125))
        }else{
            Log.d("isEnabled",titleEdit.text.toString())
            binding.savebutton.isEnabled = true
            binding.savebutton.setBackgroundColor(Color.rgb(33,150,243))
        }*/

        repeatDaySwich.setOnCheckedChangeListener { compoundButton, isChecked ->
            Log.d("repeatDay",repeatDaySwich.isChecked.toString())
            Log.d("repeatWeek",repeatWeekSwich.isChecked.toString())
            Log.d("repeatMonth",repeatMonthSwich.isChecked.toString())
            //repeat = 1
            repeatMonthSwich.isChecked = false
            repeatWeekSwich.isChecked = false
        }

        repeatWeekSwich.setOnCheckedChangeListener { compoundButton, isChecked ->
            Log.d("repeatDay",repeatDaySwich.isChecked.toString())
            Log.d("repeatWeek",repeatWeekSwich.isChecked.toString())
            Log.d("repeatMonth",repeatMonthSwich.isChecked.toString())
            //repeat = 2
            repeatMonthSwich.isChecked = false
            repeatDaySwich.isChecked = false
        }

        repeatMonthSwich.setOnCheckedChangeListener { compoundButton, isChecked ->
            Log.d("repeatDay",repeatDaySwich.isChecked.toString())
            Log.d("repeatWeek",repeatWeekSwich.isChecked.toString())
            Log.d("repeatMonth",repeatMonthSwich.isChecked.toString())
            //repeat = 3
            repeatDaySwich.isChecked = false
            repeatWeekSwich.isChecked = false
        }
    }

    fun save(year:String,month:String, day:String, title:String,content:String,isComplete:Boolean){
        val memo: Memo? = read()

        val sharedPreferences = getSharedPreferences("saveId", Context.MODE_PRIVATE)
        val saveId = sharedPreferences.getInt("saveId",0)
        val id = saveId + 1
        /*if (title != null && content != null){
            binding.savebutton.isEnabled = false
            binding.savebutton.setBackgroundColor(Color.rgb(125,125,125))
        }else{
            binding.savebutton.isEnabled = true
            binding.savebutton.setBackgroundColor(Color.rgb(33,150,243))
        }*/
        realm.executeTransaction {
            val memo: Memo = it.createObject(Memo::class.java)

            memo.id = id
            memo.year = year
            memo.month = month
            memo.day = day
            memo.title = title
            memo.content = content
            memo.isComplete = isComplete

            Log.d("save", id.toString() + ":" +memo.year+ memo.month+memo.day)
        }
        sharedPreferences.edit().putInt("saveId",id).apply()
    }

    fun read():Memo? {
        return realm.where(Memo::class.java).findFirst()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}

