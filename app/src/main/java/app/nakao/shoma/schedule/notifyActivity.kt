package app.nakao.shoma.schedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class notifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notify)

        val flag = intent.getStringExtra("flag")
        Log.d("notifyFlag",flag.toString())
        val mainIntent = Intent(this,MainActivity::class.java).run {
            putExtra("flag",flag)
        }
        startActivity(mainIntent)
    }
}