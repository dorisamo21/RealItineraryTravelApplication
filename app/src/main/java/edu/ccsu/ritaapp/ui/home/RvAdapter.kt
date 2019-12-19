import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import edu.ccsu.ritaapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rv_event.view.*

class EventRecyclerAdapter(private val event: ArrayList<EventCard>): RecyclerView.Adapter<EventRecyclerAdapter.EventHolder>() {

    override fun getItemCount(): Int {
        return event.size
    }

    override fun onBindViewHolder(holder: EventRecyclerAdapter.EventHolder, position: Int) {
        val itemEvent = event[position]
        holder.bindEvent(itemEvent)
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventRecyclerAdapter.EventHolder {
        val inflatedView = parent.inflate(R.layout.rv_event)
        return EventHolder(inflatedView)
    }

    class EventHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var event: EventCard? = null

        init {
            view.setOnClickListener { this }
        }

        override fun onClick(v: View?) {
            Log.d("RecyclerView", "CLICK!")
        }

        fun bindEvent(event: EventCard) {
            Log.i(event.toString(), "event card binding")
            this.event = event
            Log.d(event.event_name + event.event_image_url, "image url")
            if(event.event_image_url == "null"){
                event.event_image_url = "https://via.placeholder.com/300x150"
            }
            Picasso.with(view.context).load(event.event_image_url).into(view.event_image)
            view.event_name.text = event.event_name
            view.event_date.text = event.event_date
            view.event_address.text = event.event_address
            if (event.event_detail == "null") {
                val eventDescription = "This event has no description"
                view.event_details.text = eventDescription
            }
            else view.event_details.text = event.event_detail
            }
        }
    }

