package com.example.myapplication

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson


class TopicAdapter(
    private val topicList: MutableList<Topic>,
    private val context: Context,
    private val onItemClick: (Topic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topicList[position]
        holder.bind(topic, onItemClick)

        holder.itemView.setOnClickListener {
            onItemClick(topic)
        }
    }

    override fun getItemCount() = topicList.size

    fun updateList(newTopics: List<Topic>) {
        topicList.clear()
        topicList.addAll(newTopics)
        notifyDataSetChanged()
    }

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val topicNameTextView: TextView = itemView.findViewById(R.id.topicNameTextView)
        private val joinButton: Button = itemView.findViewById(R.id.joinButton)

        fun bind(topic: Topic, onItemClick: (Topic) -> Unit) {
            topicNameTextView.text = topic.name

            joinButton.setOnClickListener {
                Toast.makeText(itemView.context, "You joined ${topic.name}!", Toast.LENGTH_SHORT).show()

                val intent = Intent(itemView.context, TopicActivity::class.java)
                intent.putExtra("TOPIC_ID", topic.id)
                intent.putExtra("TOPIC_NAME", topic.name)
                itemView.context.startActivity(intent)
            }
        }
    }
}

