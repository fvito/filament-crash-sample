package com.example.crashsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.itemList)
        val adapter = ItemsAdapter(::onItemClicked)
        recyclerView.adapter = adapter

        val items = (1..100).map {
            Item("Item $it")
        }
        adapter.submitList(items)

    }

    private fun onItemClicked(position: Int) {
        val item = (recyclerView.adapter as ItemsAdapter).get(position)
        val intent = DetailActivity.newIntent(this, item)
        startActivity(intent)
    }

}

class ItemsAdapter(private val onClick: (Int) -> Unit) :
    ListAdapter<Item, ItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ItemViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun get(position: Int): Item = getItem(position)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }

        }
    }
}

class ItemViewHolder(view: View, onClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {

    private val textView: TextView = view.findViewById(android.R.id.text1)

    init {
        view.setOnClickListener {
            onClick(adapterPosition)
        }
    }

    fun bind(item: Item) {
        textView.text = item.title
    }
}