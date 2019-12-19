package edu.ccsu.ritaapp.ui.itinerary

import android.view.LayoutInflater
import android.view.View
import kotlin.collections.ArrayList
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import edu.ccsu.ritaapp.R
import kotlinx.android.synthetic.main.friend_item.view.deletefriend
import kotlinx.android.synthetic.main.itinerary_list_item.view.*


class ItnAdapter(private val item: ArrayList<ItineraryCard>): RecyclerView.Adapter<ItnAdapter.FriendHolder>() {

    private lateinit var mListener : OnItemClickListener
    private lateinit var viewListener : OnViewClickListener

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val itemEvent = item[position]
        holder.bindFriend(itemEvent,position)
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes,this,attachToRoot)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItnAdapter.FriendHolder{
        val inflatedView = parent.inflate(R.layout.itinerary_list_item)
        return FriendHolder(inflatedView,mListener, viewListener)
    }

    interface OnItemClickListener{
        fun onItemClick(position:Int)
    }

    interface OnViewClickListener{
        fun onViewClick(position:Int)
    }


    fun setOnItemClickedListener(listener:OnItemClickListener){
        mListener = listener
    }


    fun setOnViewClickedListener(listener:OnViewClickListener){
        viewListener = listener
    }
    class FriendHolder(v: View, listener : OnItemClickListener, viewlistener:OnViewClickListener) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        private var item: ItineraryCard? = null
        private var itineraryId = view.itineraryId
        private var deletefriendBtn = view.deletefriend
        private var viewItnBtn = view.viewItnContent

        private var listener = listener
        private var viewlistener = viewlistener

        fun bindFriend(item: ItineraryCard, position:Int){

            this.item = item
            itineraryId.text = "Itinerary :"+item.id.toString()

            deletefriendBtn.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                    if(listener != null) {
                        val position = getAdapterPosition()
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position)
                        }
                    }
                }

            })

            viewItnBtn.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                    if(viewlistener != null) {
                        val position = getAdapterPosition()
                        if(position != RecyclerView.NO_POSITION) {
                            viewlistener.onViewClick(position)
                        }
                    }
                }

            })
        }
    }
    data class ItineraryCard(
        val id:Long=0
    )
}
