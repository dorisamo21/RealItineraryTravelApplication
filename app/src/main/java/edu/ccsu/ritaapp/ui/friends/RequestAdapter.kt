package edu.ccsu.ritaapp.ui.friends

import android.view.LayoutInflater
import android.view.View
import kotlin.collections.ArrayList
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.R
import kotlinx.android.synthetic.main.request_item.view.*

class RequestAdapter (private val item: ArrayList<RequestCard>): RecyclerView.Adapter<RequestAdapter.RequestHolder>(){

    private lateinit var mListener : OnItemClickListener
    private lateinit var delListener : OnDeleteClickListener

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: RequestHolder, position: Int) {
        val requestEvent = item[position]
        holder.bindFriend(requestEvent)
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes,this,attachToRoot)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestAdapter.RequestHolder{
        val inflatedView = parent.inflate(R.layout.request_item)
        return RequestHolder(inflatedView, mListener, delListener)
    }

    interface OnItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setOnItemClickedListener(listener:OnItemClickListener){
        mListener = listener
    }

    interface OnDeleteClickListener{
        fun onDeleteClick(position:Int)
    }

    fun setOnDeleteClickedListener(listener:OnDeleteClickListener){
        delListener = listener
    }

    class RequestHolder(v: View,  listener : OnItemClickListener, dlistener : OnDeleteClickListener) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        private var item: RequestCard? = null
        private var friendName = view.requestName
        private var friendEmail = view.requestEmail
        private var acceptBtn = view.accept
        private var rejectBtn = view.reject
        private var listener = listener
        private var dlistener = dlistener

        fun bindFriend(item: RequestCard){
            this.item = item
            view.requestName.text = item.request_name
            view.requestEmail.text = item.request_email

            acceptBtn.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                    if(listener != null) {
                        val position = getAdapterPosition()
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position)
                        }
                    }
                }
            })


            rejectBtn.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                    if(dlistener != null) {
                        val position = getAdapterPosition()
                        if(position != RecyclerView.NO_POSITION) {
                            dlistener.onDeleteClick(position)
                        }
                    }
                }

            })
        }
    }
}