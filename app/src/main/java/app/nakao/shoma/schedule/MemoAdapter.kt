package app.nakao.shoma.schedule

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class MemoAdapter(private var context: Context):RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val titleText: TextView = view.findViewById(R.id.titleText)
        val contentText: TextView = view.findViewById(R.id.contentText)
        val completionButton: Button = view.findViewById(R.id.completionButton)
        val container: CardView = view.findViewById(R.id.container)
    }

    val items: MutableList<Memo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_recycler_view_adapter,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.contentText.text = item.content
        holder.completionButton.setOnClickListener {

        }
        holder.container.setOnClickListener {
            val detailIntent = Intent(context,DetailActivity::class.java).run {
                putExtra("title",item.title)
                putExtra("content",item.content)
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

    override fun getItemCount(): Int {
        return items.size
    }
}