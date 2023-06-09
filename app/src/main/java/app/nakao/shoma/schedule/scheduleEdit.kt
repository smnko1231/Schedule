package app.nakao.shoma.schedule

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.icu.text.CaseMap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import app.nakao.shoma.schedule.databinding.ActivityScheduleEditBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.ByteArrayOutputStream
import java.sql.Blob
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class scheduleEdit : AppCompatActivity() {

    val realm:Realm = Realm.getDefaultInstance()

    private lateinit var binding: ActivityScheduleEditBinding

    var change_day = 0
    var change_year = 0
    var change_month = 0

    var yeartoadd = 0
    var monthtoadd = 0
    var daytoadd = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_edit)
        binding = ActivityScheduleEditBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        val intent_title = intent.getStringExtra("title")
        val intent_content = intent.getStringExtra("content")
        val reconstruction = intent.getIntExtra("reconstruction",0)
        val condition = intent.getStringExtra("condition")


        binding.titleEdit.setText(intent.getStringExtra("flag"))

        var repeat = ""
        var repetition_rule = 0
        var leap = 0
        var repeatWay = ""

        var year = intent.getStringExtra("year")
        var month = intent.getStringExtra("month")
        var day = intent.getStringExtra("day")
        val intentRepeatWay = intent.getStringExtra("repeatWay")
        val intentRepetitionRule = intent.getIntExtra("repetitionRule",1)

        yeartoadd = year!!.toInt()
        monthtoadd = month!!.toInt()-1
        daytoadd = day!!.toInt()

        binding.dateChangeTextView.text = year+"年"+month+"月"+day+"日"

        binding.repeatSpinner.setSelection(0)

        if (intent_title != null && intent_content != null){
            binding.titleEdit.setText(intent_title.toString())
            binding.contentsEdit.setText(intent_content.toString())
        }

        if (reconstruction?.toInt() == 1){
            Log.d("reconstruction",reconstruction.toString())
            binding.repeatDaySwich.visibility = View.GONE
            binding.repeatWeekSwich.visibility = View.GONE
            binding.repeatMonthSwich.visibility = View.GONE
            binding.repeatYearSwich.visibility = View.GONE
            binding.repeatCustom.visibility = View.GONE
            binding.dateChangeButton.visibility = View.GONE
        }else{
            Log.d("reconstruction",reconstruction.toString())
            binding.repeatDaySwich.visibility = View.VISIBLE
            binding.repeatWeekSwich.visibility = View.VISIBLE
            binding.repeatMonthSwich.visibility = View.VISIBLE
            binding.repeatYearSwich.visibility = View.VISIBLE
            binding.repeatCustom.visibility = View.VISIBLE
            binding.dateChangeButton.visibility = View.VISIBLE
        }

        val spinnerItems = arrayOf("未選択","日","週","月")
        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.repeatSpinner.adapter = spinnerAdapter

        binding.repeatDaySwich.setOnClickListener {
            binding.repeatMonthSwich.isChecked = false
            binding.repeatWeekSwich.isChecked = false
            binding.repeatYearSwich.isChecked = false
            repetition_rule = 1
            repeatWay = "日"
            binding.repeatSpinner.setSelection(0)
            Log.d("repetition_rule",repetition_rule.toString())
        }

        binding.repeatWeekSwich.setOnClickListener {
            binding.repeatMonthSwich.isChecked = false
            binding.repeatDaySwich.isChecked = false
            binding.repeatYearSwich.isChecked = false
            repetition_rule = 1
            repeatWay = "週"
            binding.repeatSpinner.setSelection(0)
            Log.d("repetition_rule",repetition_rule.toString())
        }

        binding.repeatMonthSwich.setOnClickListener {
            binding.repeatDaySwich.isChecked = false
            binding.repeatWeekSwich.isChecked = false
            binding.repeatYearSwich.isChecked = false
            repetition_rule = 1
            repeatWay = "月"
            binding.repeatSpinner.setSelection(0)
            Log.d("repetition_rule",repetition_rule.toString())
        }

        binding.repeatYearSwich.setOnClickListener {
            binding.repeatDaySwich.isChecked = false
            binding.repeatMonthSwich.isChecked = false
            binding.repeatWeekSwich.isChecked = false
            repetition_rule = 1
            repeatWay = "年"
            binding.repeatSpinner.setSelection(0)
            Log.d("repetition_rule",repetition_rule.toString())
        }

        binding.savebutton.setOnClickListener {
            if (change_year != 0){
                year = change_year.toString()
                month = change_month.toString()
                day = change_day.toString()
            }
            val title: String = binding.titleEdit.text.toString()
            val content: String = binding.contentsEdit.text.toString()
            val intent_day = day
            val intent_month = month
            val intent_year = year
            val csvFormat = DateTimeFormatter.ofPattern("yyyy/[]M/[]d")
            val intent_date = LocalDate.parse("$intent_year/$intent_month/$intent_day", csvFormat)
            var dayOfYear = intent_date.dayOfYear
            var day_int = day?.toInt()
            var month_int = month?.toInt()
            var year_int = year?.toInt()
            var current_year = year_int
            val isComplete = intent.getBooleanExtra("isComplete",false)
            var intent_condition = intent.getIntExtra("condition",0)
            Log.d("customedit",binding.customEditText.text.toString())

            if (binding.customEditText.text.toString() != ""){
                repetition_rule = binding.customEditText.text.toString().toInt()
                repeatWay = binding.repeatSpinner.selectedItem.toString()
                Log.d("repetition_rule",repetition_rule.toString())
            }

            createImageData()

            if (reconstruction == 1){
                repeat = intentRepeatWay.toString()
                repeatWay = repeat
                repetition_rule = intentRepetitionRule
                Log.d("returnRepeatWay",repeat)
                Log.d("returnRepetitionRule",repetition_rule.toString())
            }

            if (title.equals("")){
                AlertDialog.Builder(this)
                    .setTitle("タイトルが入力されていません")
                    .setMessage("タイトルを入力してください")
                    .setPositiveButton("OK", { dialog, which ->

                    })
                    .show()
            }else if (content.equals("")){
                AlertDialog.Builder(this)
                    .setTitle("内容が入力されていません")
                    .setMessage("内容を入力してください")
                    .setPositiveButton("OK", { dialog, which ->

                    })
                    .show()
            }else{
                if (year != null && month != null && day != null) {
                    if ((binding.repeatDaySwich.isChecked == true &&  binding.repeatWeekSwich.isChecked == false && binding.repeatMonthSwich.isChecked == false && binding.repeatYearSwich.isChecked == false) || repeat == "日"){
                        Log.v("repetitionRule",repetition_rule.toString())
                        for (i in 1..100/repetition_rule){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete,repetition_rule,repeatWay,createImageData())
                            if (dayOfYear != null){
                                dayOfYear = dayOfYear + repetition_rule
                            }
                            if (current_year != null){
                                if (current_year % 100 == 0){
                                    if (current_year % 400 == 0){
                                        Log.d("current_year",current_year.toString())
                                        leap = 1
                                    }else{
                                        Log.d("current_year",current_year.toString())
                                        leap = 0
                                    }
                                }else if (current_year % 4 == 0){
                                    leap = 1
                                }else{
                                    if ((current_year+1) % 4 == 0){
                                        Log.d("current_year",current_year.toString())
                                        leap = 2
                                    }else{
                                        Log.d("current_year",current_year.toString())
                                        leap = 0
                                    }
                                }
                            }
                            Log.d("leap",leap.toString())

                            if (leap == 1){
                                if (dayOfYear >= 336 && dayOfYear <= 366){
                                    day_int = dayOfYear-335
                                    month_int = 12
                                }else if (dayOfYear >= 306) {
                                    day_int = dayOfYear - 305
                                    month_int = 11
                                }else if (dayOfYear >= 275) {
                                    day_int = dayOfYear - 274
                                    month_int = 10
                                }else if (dayOfYear >= 245) {
                                    day_int = dayOfYear - 244
                                    month_int = 9
                                }else if (dayOfYear >= 214) {
                                    day_int = dayOfYear - 213
                                    month_int = 8
                                }else if (dayOfYear >= 183) {
                                    day_int = dayOfYear - 182
                                    month_int = 7
                                }else if (dayOfYear >= 153) {
                                    day_int = dayOfYear - 152
                                    month_int = 6
                                }else if (dayOfYear >= 122) {
                                    day_int = dayOfYear - 121
                                    month_int = 5
                                }else if (dayOfYear >= 92) {
                                    day_int = dayOfYear - 91
                                    month_int = 4
                                }else if (dayOfYear >= 61) {
                                    day_int = dayOfYear - 60
                                    month_int = 3
                                }else if (dayOfYear >= 32) {
                                    day_int = dayOfYear - 31
                                    month_int = 2
                                }else{
                                    day_int = dayOfYear
                                    month_int = 1
                                }

                                if (dayOfYear >= 457){
                                    day_int = dayOfYear-456
                                    month_int = 4
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 426){
                                    day_int = dayOfYear-425
                                    month_int = 3
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 398){
                                    day_int = dayOfYear-397
                                    month_int = 2
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 367){
                                    day_int = dayOfYear-366
                                    month_int = 1
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }
                            }else if (leap == 0) {
                                if (dayOfYear >= 335 && dayOfYear <= 365){
                                    day_int = dayOfYear-334
                                    month_int = 12
                                }else if (dayOfYear >= 305) {
                                    day_int = dayOfYear-304
                                    month_int = 11
                                }else if (dayOfYear >= 274) {
                                    day_int = dayOfYear-273
                                    month_int = 10
                                }else if (dayOfYear >= 244) {
                                    day_int = dayOfYear-243
                                    month_int = 9
                                }else if (dayOfYear >= 213) {
                                    day_int = dayOfYear-212
                                    month_int = 8
                                }else if (dayOfYear >= 182) {
                                    day_int = dayOfYear-181
                                    month_int = 7
                                }else if (dayOfYear >= 152) {
                                    day_int = dayOfYear-151
                                    month_int = 6
                                }else if (dayOfYear >= 121) {
                                    day_int = dayOfYear-120
                                    month_int = 5
                                }else if (dayOfYear >= 91) {
                                    day_int = dayOfYear-90
                                    month_int = 4
                                }else if (dayOfYear >= 60) {
                                    day_int = dayOfYear-59
                                    month_int = 3
                                }else if (dayOfYear >= 32) {
                                    day_int = dayOfYear-31
                                    month_int = 2
                                }else{
                                    day_int = dayOfYear
                                    month_int = 1
                                }

                                if (dayOfYear >= 456){
                                    day_int = dayOfYear-455
                                    month_int = 4
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 425){
                                    day_int = dayOfYear-424
                                    month_int = 3
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 397){
                                    day_int = dayOfYear-396
                                    month_int = 2
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 366){
                                    day_int = dayOfYear-365
                                    month_int = 1
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }
                            }else{
                                if (dayOfYear >= 335 && dayOfYear <= 365){
                                    day_int = dayOfYear-334
                                    month_int = 12
                                }else if (dayOfYear >= 305) {
                                    day_int = dayOfYear-304
                                    month_int = 11
                                }else if (dayOfYear >= 274) {
                                    day_int = dayOfYear-273
                                    month_int = 10
                                }else if (dayOfYear >= 244) {
                                    day_int = dayOfYear-243
                                    month_int = 9
                                }else if (dayOfYear >= 213) {
                                    day_int = dayOfYear-212
                                    month_int = 8
                                }else if (dayOfYear >= 182) {
                                    day_int = dayOfYear-181
                                    month_int = 7
                                }else if (dayOfYear >= 152) {
                                    day_int = dayOfYear-151
                                    month_int = 6
                                }else if (dayOfYear >= 121) {
                                    day_int = dayOfYear-120
                                    month_int = 5
                                }else if (dayOfYear >= 91) {
                                    day_int = dayOfYear-90
                                    month_int = 4
                                }else if (dayOfYear >= 60) {
                                    day_int = dayOfYear-59
                                    month_int = 3
                                }else if (dayOfYear >= 32) {
                                    day_int = dayOfYear-31
                                    month_int = 2
                                }else{
                                    day_int = dayOfYear
                                    month_int = 1
                                }

                                if (dayOfYear >= 457){
                                    day_int = dayOfYear-456
                                    month_int = 4
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 426){
                                    day_int = dayOfYear-425
                                    month_int = 3
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 397){
                                    day_int = dayOfYear-396
                                    month_int = 2
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }else if (dayOfYear >= 366){
                                    day_int = dayOfYear-365
                                    month_int = 1
                                    if (current_year != null) {
                                        year_int = current_year+1
                                    }
                                }
                            }
                            year = year_int.toString()
                            month = month_int.toString()
                            day = day_int.toString()
                            Log.d("repeatdayofyear",dayOfYear.toString())
                            Log.d("repeatyear",year.toString())
                            Log.d("repeatmonth",month.toString())
                            Log.d("repeatday",day.toString())
                        }
                    }else if ((binding.repeatDaySwich.isChecked == false &&  binding.repeatWeekSwich.isChecked == true && binding.repeatMonthSwich.isChecked == false && binding.repeatYearSwich.isChecked == false) || repeat == "週"){
                        for (i in 1..12/repetition_rule){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete,repetition_rule,repeatWay,createImageData())
                            if (dayOfYear != null){
                                dayOfYear = dayOfYear + repetition_rule*7
                            }
                            if (year_int != null){
                                if (year_int % 100 == 0){
                                    if (year_int % 400 == 0){
                                        leap = 1
                                    }else{
                                        leap = 0
                                    }
                                }else if (year_int % 4 == 0){
                                    leap = 1
                                }else{
                                    leap = 0
                                }
                            }
                            if (leap == 1){
                                if (dayOfYear >= 336){
                                    day_int = dayOfYear-335
                                    month_int = 12
                                }else if (dayOfYear >= 306) {
                                    day_int = dayOfYear - 305
                                    month_int = 11
                                }else if (dayOfYear >= 275) {
                                    day_int = dayOfYear - 274
                                    month_int = 10
                                }else if (dayOfYear >= 245) {
                                    day_int = dayOfYear - 244
                                    month_int = 9
                                }else if (dayOfYear >= 214) {
                                    day_int = dayOfYear - 213
                                    month_int = 8
                                }else if (dayOfYear >= 183) {
                                    day_int = dayOfYear - 182
                                    month_int = 7
                                }else if (dayOfYear >= 153) {
                                    day_int = dayOfYear - 152
                                    month_int = 6
                                }else if (dayOfYear >= 122) {
                                    day_int = dayOfYear - 121
                                    month_int = 5
                                }else if (dayOfYear >= 92) {
                                    day_int = dayOfYear - 91
                                    month_int = 4
                                }else if (dayOfYear >= 61) {
                                    day_int = dayOfYear - 60
                                    month_int = 3
                                }else if (dayOfYear >= 32) {
                                    day_int = dayOfYear - 31
                                    month_int = 2
                                }else{
                                    day_int = dayOfYear
                                    month_int = 1
                                }
                            }else{
                                if (dayOfYear >= 335){
                                    day_int = dayOfYear-334
                                    month_int = 12
                                }else if (dayOfYear >= 305) {
                                    day_int = dayOfYear - 304
                                    month_int = 11
                                }else if (dayOfYear >= 274) {
                                    day_int = dayOfYear - 273
                                    month_int = 10
                                }else if (dayOfYear >= 244) {
                                    day_int = dayOfYear - 243
                                    month_int = 9
                                }else if (dayOfYear >= 213) {
                                    day_int = dayOfYear - 212
                                    month_int = 8
                                }else if (dayOfYear >= 182) {
                                    day_int = dayOfYear - 181
                                    month_int = 7
                                }else if (dayOfYear >= 152) {
                                    day_int = dayOfYear - 151
                                    month_int = 6
                                }else if (dayOfYear >= 121) {
                                    day_int = dayOfYear - 120
                                    month_int = 5
                                }else if (dayOfYear >= 91) {
                                    day_int = dayOfYear - 90
                                    month_int = 4
                                }else if (dayOfYear >= 60) {
                                    day_int = dayOfYear - 59
                                    month_int = 3
                                }else if (dayOfYear >= 32) {
                                    day_int = dayOfYear - 31
                                    month_int = 2
                                }else{
                                    day_int = dayOfYear
                                    month_int = 1
                                }
                            }
                            month = month_int.toString()
                            day = day_int.toString()
                            Log.d("repeatweek",day_int.toString())
                        }
                    }else if ((binding.repeatDaySwich.isChecked == false &&  binding.repeatWeekSwich.isChecked == false && binding.repeatMonthSwich.isChecked == true && binding.repeatYearSwich.isChecked == false) || repeat == "月"){
                        for (i in 1..12/repetition_rule){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete,repetition_rule,repeatWay,createImageData())
                            if (month_int != null){
                                if (month_int + repetition_rule <= 12){
                                    month_int = month_int + repetition_rule
                                }else{
                                    if (year_int != null){
                                        year_int++
                                    }
                                    month_int = month_int + repetition_rule - 12
                                }
                            }
                            year = year_int.toString()
                            month = month_int.toString()
                        }
                    }else if (binding.repeatDaySwich.isChecked == false &&  binding.repeatWeekSwich.isChecked == false && binding.repeatMonthSwich.isChecked == false && binding.repeatYearSwich.isChecked == true){
                        for (i in 1..10){
                            save(year.toString(),month.toString(),day.toString() ,title,content,isComplete,repetition_rule,repeatWay,createImageData())
                            if (year_int != null){
                                year_int++
                            }
                            year = year_int.toString()
                        }
                    }else if (binding.repeatDaySwich.isChecked == false &&  binding.repeatWeekSwich.isChecked == false && binding.repeatMonthSwich.isChecked == false && binding.repeatYearSwich.isChecked == false || repeat == "未選択"){
                        save(year.toString(),month.toString(),day.toString() ,title,content,isComplete,repetition_rule,repeatWay,createImageData())
                    }
                }

                Log.d("intent_condition",intent_condition.toString())

                val mainIntent = Intent(this,MainActivity::class.java).run {
                    putExtra("year",intent_year)
                    putExtra("month",intent_month)
                    putExtra("day",intent_day)
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

            Log.d("condition",condition.toString())
            if (condition?.toInt() == 3){
                if (year != null && month != null && day != null && intent_title != null && intent_content != null) {
                    save(year,month,day ,intent_title.toString(),intent_content.toString(),isComplete,repetition_rule,repeatWay,createImageData())
                }
            }

            val mainIntent = Intent(this,MainActivity::class.java).run {
                putExtra("year",year)
                putExtra("month",month)
                putExtra("day",day)
            }
            startActivity(mainIntent)
        }
        /*

        Log.d("customedit",binding.customEditText.text.toString())
        if (binding.customEditText.text.toString() != ""){
            repetition_rule = binding.customEditText.text.toString().toInt()
        }
        Log.d("repetition_rule",binding.customEditText.text.toString())

        val spinnerItems = arrayOf("日","週","月")
        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.repeatSpinner.adapter = spinnerAdapter

         */

        binding.repeatSpinner.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?,position: Int,id: Long){
                val spinnerParent = parent as Spinner
                repeat = spinnerParent.selectedItem as String
                Log.d("repeatType",repeat)
                if (repeat != "未選択"){
                    binding.repeatDaySwich.isChecked = false
                    binding.repeatMonthSwich.isChecked = false
                    binding.repeatWeekSwich.isChecked = false
                    binding.repeatYearSwich.isChecked = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                TODO("Not yet implemented")
            }
        }

        binding.dateChangeButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.selectPhotoButton.setOnClickListener {
            selectPhoto()
        }
    }

    fun save(year:String,month:String, day:String, title:String,content:String,isComplete:Boolean,repetition_rule:Int,repeatWay:String,Image:ByteArray){
        val memo: Memo? = read()

        val sharedPreferences = getSharedPreferences("saveId", Context.MODE_PRIVATE)
        val saveId = sharedPreferences.getInt("saveId",0)
        val id = saveId + 1

        realm.executeTransaction { realm ->
            val memo: Memo = realm.createObject(Memo::class.java)

            memo.id = id
            memo.year = year
            memo.month = month
            memo.day = day
            memo.title = title
            memo.content = content
            memo.isComplete = isComplete
            memo.repetitionRule = repetition_rule
            memo.repeatWay = repeatWay
            memo.image = Image

            Log.d("repetitionRule",memo.repetitionRule.toString())
            Log.v("item_id",memo.id.toString())
            Log.d("photo",memo.image.toString())
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

    fun showDatePickerDialog() {
        val calendar: Calendar = Calendar.getInstance()

        calendar.set(yeartoadd,monthtoadd,daytoadd)
        //日付ピッカーダイアログを生成および設定
        DatePickerDialog(
            this,
            //ダイアログのクリックイベント設定
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val currentDate =
                    Calendar.getInstance().apply { set(year, monthOfYear, dayOfMonth) }
                binding.dateChangeTextView.text = year.toString()+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日"
                change_year = year
                change_month = monthOfYear+1
                change_day = dayOfMonth
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
        }.show()
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    companion object {
        private const val READ_REQUEST_CODE: Int = 42
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            READ_REQUEST_CODE -> {
                try {
                    resultData?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
                        val image = BitmapFactory.decodeStream(inputStream)
                        val imageView = findViewById<ImageView>(R.id.imageView)
                        imageView.setImageBitmap(image)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun createImageData() : ByteArray {
        val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageByteArray = baos.toByteArray()
        return imageByteArray
    }
}

