package edu.ccsu.ritaapp.ui.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import kotlin.collections.ArrayList
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import edu.ccsu.ritaapp.ui.friends.FriendsFragment
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.friend_item.view.*
import org.json.JSONObject


class FriendAdapter (private val item: ArrayList<FriendCard>): RecyclerView.Adapter<FriendAdapter.FriendHolder>() {

    private lateinit var mListener : OnItemClickListener


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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendAdapter.FriendHolder{
        val inflatedView = parent.inflate(R.layout.friend_item)
        return FriendHolder(inflatedView,mListener)
    }

    interface OnItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setOnItemClickedListener(listener:OnItemClickListener){
        mListener = listener
    }

    class FriendHolder(v: View, listener : OnItemClickListener) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        private var item: FriendCard? = null
        private var friendName = view.friendName
        private var friendEmail = view.friendEmail
        private var deletefriendBtn = view.deletefriend
        private var listener = listener

        fun bindFriend(item: FriendCard, position:Int){
            this.item = item
            friendName.text = item.friend_name
            friendEmail.text = item.friend_email

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
        }
    }
}
