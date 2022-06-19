package app.nakao.shoma.schedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import app.nakao.shoma.schedule.databinding.ActivityMainBinding
import app.nakao.shoma.schedule.databinding.ActivityScheduleEditBinding
import io.realm.Realm
import io.realm.RealmConfiguration
import java.time.LocalDate

class scheduleEdit : AppCompatActivity() {

    val realm:Realm = Realm.getDefaultInstance()

    private lateinit var binding: ActivityScheduleEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_edit)
        binding = ActivityScheduleEditBinding.inflate(layoutInflater).apply { setContentView(this.root) }
        val titleEdit = findViewById<EditText>(R.id.titleEdit)
        val contentsEdit = findViewById<EditText>(R.id.contentsEdit)
        //val saveButton = findViewById<Button>(R.id.savebutton)

        val memo: Memo? = read()

        binding.savebutton.setOnClickListener {
            val title: String = titleEdit.text.toString()
            val content: String = contentsEdit.text.toString()
            val year = intent.getStringExtra("year")
            val month = intent.getStringExtra("month")
            val day = intent.getStringExtra("day")
            Log.d("date",year+month+day)
            if (year != null && month != null && day != null) {
                save(year,month,day ,title,content) 
            }

            val mainIntent = Intent(this,MainActivity::class.java)
            startActivity(mainIntent)
        }
    }

    fun save(year:String,month:String, day:String, title:String,content:String){
        val memo: Memo? = read()
        realm.executeTransaction {
            val memo: Memo = it.createObject(Memo::class.java)
            //


            memo.year = year
            memo.month = month
            memo.day = day
            memo.title = title
            memo.content = content

            Log.d("save", memo.year+ memo.month+memo.day)
        }
    }

    fun read():Memo? {
        return realm.where(Memo::class.java).findFirst()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}