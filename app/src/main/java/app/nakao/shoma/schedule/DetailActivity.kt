package app.nakao.shoma.schedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.realm.Realm

class DetailActivity : AppCompatActivity() {

    val realm: Realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val memo:Memo? = read()

        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val editButton = findViewById<Button>(R.id.editButton)

        val year = intent.getStringExtra("year")
        val month = intent.getStringExtra("month")
        val day = intent.getStringExtra("day")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        dateTextView.text = intent.getStringExtra("year")+"年"+intent.getStringExtra("month")+"月"+intent.getStringExtra("day")+"日"
        titleTextView.text = intent.getStringExtra("title")
        contentTextView.text = intent.getStringExtra("content")

        deleteButton.setOnClickListener {
            val task_delete = realm.where(Memo::class.java).equalTo("content",content).findAll()

            realm.executeTransaction{
                task_delete.deleteFromRealm(0)
                val mainIntent = Intent(this,MainActivity::class.java)
                startActivity(mainIntent)
            }
        }

        editButton.setOnClickListener {
            val task_delete = realm.where(Memo::class.java).equalTo("content",content).findAll()

            realm.executeTransaction{
                task_delete.deleteFromRealm(0)
                val mainIntent = Intent(this,MainActivity::class.java)
                startActivity(mainIntent)
            }

            val scheduleIntent = Intent(this,scheduleEdit::class.java).run {
                putExtra("year",year)
                putExtra("month",month)
                putExtra("day",day)
                putExtra("title",title)
                putExtra("content",content)
            }
            startActivity(scheduleIntent)
        }
    }

    fun read():Memo?{
        return realm.where(Memo::class.java).findFirst()
    }
}