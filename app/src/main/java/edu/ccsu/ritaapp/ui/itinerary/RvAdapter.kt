import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import edu.ccsu.ritaapp.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.rv_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class ItemRecyclerAdapter(private val item: ArrayList<ItemCard>): RecyclerView.Adapter<ItemRecyclerAdapter.EventHolder>() {

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ItemRecyclerAdapter.EventHolder, position: Int) {
        val itemEvent = item[position]
        holder.bindItem(itemEvent)
    }
    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes,this,attachToRoot)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemRecyclerAdapter.EventHolder{
        val inflatedView = parent.inflate(R.layout.rv_item)
        return EventHolder(inflatedView)
    }

    class EventHolder(v:View) : RecyclerView.ViewHolder(v) , View.OnClickListener {

        private var view : View = v
        private var item : ItemCard? = null

        init {
            view.setOnClickListener {onClick(view)}
        }

        override fun onClick(v: View?) {
            Log.d("Item Card latlng:" + item!!.latLng, "Click Event")
            Log.d("RecyclerView", "CLICK!")
            // leaving origin empty defaults to users default location
            if(item!!.latLng != null) {
                val googleMapsUrl = "https://www.google.com/maps/dir/?api=1" + "&origin=&destination=" + item!!.latLng!!.latitude + "," + item!!.latLng!!.longitude;
                val directionsIntent = Intent(Intent.ACTION_VIEW)
                directionsIntent.setData(Uri.parse(googleMapsUrl))
                startActivity(view.context, directionsIntent, null)
            }
        }

        fun bindItem(item: ItemCard) {
            Log.i(item.toString(), "event card binding")
            this.item = item
            // Picasso.with(view.context).load(event.event_image_url).into(view.event_image)
            view.event_name.text = item.item_name
            view.event_date.text = item.item_date
            view.event_address.text = item.item_image_url   //TODO : change to real image
        }
    }
}