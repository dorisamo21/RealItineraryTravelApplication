import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import edu.ccsu.ritaapp.R
import edu.ccsu.ritaapp.ui.itinerary.EventListCard
import edu.ccsu.ritaapp.ui.itinerary.ManualItineraryFragment
import kotlinx.android.synthetic.main.el_item.view.*
import kotlin.collections.ArrayList

class RecyclerAdapter(private val item: ArrayList<EventListCard>): RecyclerView.Adapter<RecyclerAdapter.EventHolder>() {

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        val itemEvent = item[position]
        holder.bindItem(itemEvent)
    }
    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes,this,attachToRoot)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.EventHolder{
        val inflatedView = parent.inflate(R.layout.el_item)
        return EventHolder(inflatedView)
    }

    class EventHolder(v:View) : RecyclerView.ViewHolder(v) , View.OnClickListener {
        private var view : View = v
        private var item : EventListCard? = null

        init {
            view.delete.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if(item!!.item_snippet != null) {
                var del_str = item!!.item_snippet?.split(",")
                val del_element = ManualItineraryFragment.UserChosenEvent(
                    item!!.item_title.toString(),   //title
                    del_str!![0],                   //id
                    del_str[1]                      //service
                )
                deleteEvent(del_element)

                this.itemView.title.setVisibility(View.GONE)
                this.itemView.content.setVisibility(View.GONE)
                this.itemView.delete.setVisibility(View.GONE)
            }
            Log.d("adapter position",this.adapterPosition.toString())
        }


        fun deleteEvent(element : ManualItineraryFragment.UserChosenEvent){
            if(ManualItineraryFragment.userChosenEventList.contains(element)){
                ManualItineraryFragment.userChosenEventList.remove(element)

            }
        }

        fun bindItem(item: EventListCard) {
            Log.i(item.toString(), "event card binding")
            this.item = item
            // Picasso.with(view.context).load(event.event_image_url).into(view.event_image)
            view.title.text = item.item_title
            view.content.text = item.item_snippet
        }
    }
}

