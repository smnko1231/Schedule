package app.nakao.shoma.schedule

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import app.nakao.shoma.schedule.databinding.ActivityDetailBinding
import com.airbnb.lottie.LottieAnimationView
import io.realm.Realm
import java.time.LocalDate

class MemoAdapter(private var context: Context):RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val titleText: TextView = view.findViewById(R.id.titleText)
        val contentText: TextView = view.findViewById(R.id.contentText)
        val completionButton: Button = view.findViewById(R.id.completionButton)
        val container: CardView = view.findViewById(R.id.adapter)
        val lottieAnimationCompleteView: LottieAnimationView = view.findViewById(R.id.LottieAnimetionCompleteView)
    }

    val realm:Realm = Realm.getDefaultInstance()

    val items: MutableList<Memo> = mutableListOf()

    var isComplete:Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    val dt = LocalDate.now()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_recycler_view_adapter,parent,false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.contentText.text = item.content

        if(item.isComplete == true){
            holder.completionButton.text = "未完了"
            holder.lottieAnimationCompleteView.visibility = View.VISIBLE
            holder.lottieAnimationCompleteView.playAnimation()
            isComplete = true
        }else{
            holder.completionButton.text = "完了"
            holder.lottieAnimationCompleteView.visibility = View.INVISIBLE
            isComplete = false
        }

        holder.completionButton.setOnClickListener {
            item.isComplete = !item.isComplete

            if(item.isComplete == true){
                holder.completionButton.text = "未完了"
                holder.lottieAnimationCompleteView.visibility = View.VISIBLE
                holder.lottieAnimationCompleteView.playAnimation()
                isComplete = true
            }else{
                holder.completionButton.text = "完了"
                holder.lottieAnimationCompleteView.visibility = View.INVISIBLE
                isComplete = false
            }

            /*if (dt>LocalDate.of(item.year.toInt(),item.month.toInt(),item.day.toInt())){
                val mainIntent = Intent(context,MainActivity::class.java).run {
                    putExtra("year",item.year)
                    putExtra("month",item.month)
                    putExtra("day",item.day)
                }
                context.startActivity(mainIntent)
            }*/

            updateRealm(item.id,isComplete)
        }
        holder.container.setOnClickListener {
            Log.d("repetitionRule",item.repetitionRule.toString())
            Log.d("repeatWay",item.repeatWay)
            Log.d("schedule_day",item.day)
            val detailIntent = Intent(context,DetailActivity::class.java).run {
                putExtra("year",item.year)
                putExtra("month",item.month)
                putExtra("day",item.day)
                putExtra("title",item.title)
                putExtra("content",item.content)
                putExtra("isComplete",item.isComplete)
                putExtra("repetitionRule",item.repetitionRule)
                putExtra("repeatWay",item.repeatWay)
            }
            context.startActivity(detailIntent)
        }
    }

    fun addall(items: List<Memo>){
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun itemClear(){
        this.items.clear()
        notifyDataSetChanged()
    }

    fun read(): Memo?{
        return realm.where(Memo::class.java).findFirst()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun updateRealm(id:Int, newCompletestate: Boolean){
        val target = realm.where(Memo::class.java)
            .equalTo("id",id)
            .findFirst()

        realm.executeTransaction {
            target?.isComplete = newCompletestate
        }
    }
}