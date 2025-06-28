import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.database.TopicDatabase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Message(val text: String, val idUser: String, val timestamp: Long = System.currentTimeMillis())

class MessageAdapter(
    private val messages: MutableList<Message>,
    private val currentUserId: String,
    private val topicId: String,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    private val topicDb = TopicDatabase.instance
    private val ioScope = CoroutineScope(Dispatchers.IO)

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].idUser == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return if (viewType == VIEW_TYPE_SENT) SentMessageViewHolder(view) else ReceivedMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> {
                holder.messageText.text = message.text
                holder.itemView.setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("Delete message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete") { _, _ ->
                            val pos = holder.adapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                messages.removeAt(pos)
                                notifyItemRemoved(pos)
                                saveMessagesToPrefs()

                                ioScope.launch {
                                    topicDb.decrementMessageCount(topicId)
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            }

            is ReceivedMessageViewHolder -> {
                holder.messageText.text = message.text
                holder.itemView.setOnLongClickListener(null)
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
        saveMessagesToPrefs()

        ioScope.launch {
            topicDb.incrementMessageCount(topicId)
        }
    }

    private fun saveMessagesToPrefs() {
        val prefs = context.getSharedPreferences("messages", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(messages)
        editor.putString(topicId, json).apply()
    }
}
