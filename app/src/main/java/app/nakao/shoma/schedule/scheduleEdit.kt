package app.nakao.shoma.schedule

import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import app.nakao.shoma.schedule.databinding.ActivityScheduleEditBinding
import io.realm.Realm

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
        //val saveButton = findViewById<Button>(R.id.savebutton)

        val memo: Memo? = read()

        val intent_title = intent.getStringExtra("title")
        val intent_content = intent.getStringExtra("content")

        if (intent_title != null && intent_content != null){
            titleEdit.setText(intent_title.toString())
            contentsEdit.setText(intent_content.toString())
        }

        binding.savebutton.setOnClickListener {
            val title: String = titleEdit.text.toString()
            val content: String = contentsEdit.text.toString()
            val year = intent.getStringExtra("year")
            val month = intent.getStringExtra("month")
            val day = intent.getStringExtra("day")
            val isComplete = intent.getBooleanExtra("isComplete",false)

            Log.d("date",year+month+day)
            if (year != null && month != null && day != null) {
                save(year,month,day ,title,content,isComplete)
            }

            val mainIntent = Intent(this,MainActivity::class.java).run {
                putExtra("year",year)
                putExtra("month",month)
                putExtra("day",day)
            }
            startActivity(mainIntent)
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
    }

    fun save(year:String,month:String, day:String, title:String,content:String,isComplete:Boolean){
        val memo: Memo? = read()

        val sharedPreferences = getSharedPreferences("saveId", Context.MODE_PRIVATE)
        val saveId = sharedPreferences.getInt("saveId",0)
        val id = saveId + 1
        realm.executeTransaction {
            val memo: Memo = it.createObject(Memo::class.java)
            
            memo.id = id
            memo.year = year
            memo.month = month
            memo.day = day
            memo.title = title
            memo.content = content
            memo.isComplete = isComplete

            Log.d("save", id.toString() + ":" + memo.year+ memo.month+memo.day)
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

