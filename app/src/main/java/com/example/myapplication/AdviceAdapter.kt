package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Advice(val title: String, val shortDescription: String,  val fullContent: String)

class AdviceAdapter(
    private var adviceList: List<Advice>,
    private val onItemClicked: (Advice) -> Unit
) : RecyclerView.Adapter<AdviceAdapter.AdviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_advice, parent, false)
        return AdviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        val advice = adviceList[position]
        holder.title.text = advice.title
        holder.shortDescription.text = advice.shortDescription
        holder.view.setOnClickListener { onItemClicked(advice) }
    }

    override fun getItemCount() = adviceList.size

    fun updateAdviceList(newAdviceList: List<Advice>) {
        adviceList = newAdviceList
        notifyDataSetChanged()
    }

    class AdviceViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.adviceTitle)
        val shortDescription = view.findViewById<TextView>(R.id.adviceShortDescription)
    }
}




