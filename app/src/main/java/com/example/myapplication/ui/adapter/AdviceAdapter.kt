package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.models.Advice
import com.example.myapplication.ui.models.ImageLoader

class AdviceAdapter(
    private val isAdmin: Boolean,
    private val onItemClicked: (Advice) -> Unit,
    private val onEditClicked: (Advice) -> Unit
) : ListAdapter<Advice, AdviceAdapter.AdviceViewHolder>(AdviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_advice, parent, false)
        return AdviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        val advice = getItem(position)
        holder.bind(advice)
    }

    inner class AdviceViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.adviceTitle)
        private val shortDescription: TextView = view.findViewById(R.id.adviceShortDescription)
        private val image: ImageView = view.findViewById(R.id.adviceImage)
        private val editButton: ImageButton = view.findViewById(R.id.editButton)

        fun bind(advice: Advice) {
            title.text = advice.title

            val shortDesc = advice.description
                .split(".")
                .firstOrNull()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "No description available"
            shortDescription.text = shortDesc

            if (!advice.imageUri.isNullOrEmpty() && advice.imageUri.startsWith("http")) {
                image.visibility = View.VISIBLE
                ImageLoader.loadImage(image, advice.imageUri)
            } else {
                image.visibility = View.VISIBLE
                image.setImageResource(R.drawable.default_advice_image)
            }


            editButton.visibility = if (isAdmin) View.VISIBLE else View.GONE

            view.setOnClickListener { onItemClicked(advice) }
            editButton.setOnClickListener { onEditClicked(advice) }
        }

    }
        class AdviceDiffCallback : DiffUtil.ItemCallback<Advice>() {
        override fun areItemsTheSame(oldItem: Advice, newItem: Advice): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Advice, newItem: Advice): Boolean =
            oldItem.title == newItem.title &&
                    oldItem.description == newItem.description &&
                    oldItem.category == newItem.category &&
                    oldItem.imageUri == newItem.imageUri
    }
}
