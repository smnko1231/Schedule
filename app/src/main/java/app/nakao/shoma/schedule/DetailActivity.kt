package app.nakao.shoma.schedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import androidx.appcompat.app.AlertDialog
import app.nakao.shoma.schedule.databinding.ActivityDetailBinding
import app.nakao.shoma.schedule.databinding.ActivityMainBinding

class DetailActivity : AppCompatActivity() {

    val realm: Realm = Realm.getDefaultInstance()
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        val year = intent.getStringExtra("year")
        val month = intent.getStringExtra("month")
        val day = intent.getStringExtra("day")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val isComplete = intent.getBooleanExtra("isComplete",false)
        var condition = 0

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
    }

    fun read():Memo?{
        return realm.where(Memo::class.java).findFirst()
    }
}