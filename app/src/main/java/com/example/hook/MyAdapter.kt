package com.example.hook

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.TextView
import org.jetbrains.anko.AnkoContext

/**
 * Created by abduaziz on 5/25/18.
 */

class MyAdapter(var list: ArrayList<Movie> = arrayListOf()) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView == null) {
            val view = WeUI().createView(AnkoContext.create(parent!!.context, parent))
            val holder = MovieViewHolder(view)
            holder.tvTitle.text = list.get(position).title
            holder.tvYear.text = list.get(position).content
            view.tag = holder
            return view
        } else {
            val holder = convertView.tag as MovieViewHolder
            holder.tvTitle.text = list.get(position).title
            holder.tvYear.text = list.get(position).content
            return convertView
        }
    }

    override fun getItem(position: Int): Any {
        return list.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
       return list.size
    }

    inner class MovieViewHolder(itemView: View) {

        var tvTitle: TextView
        var tvYear: TextView

        init {
            tvTitle = itemView.findViewById(WeUI.tvTitleId)
            tvYear = itemView.findViewById(WeUI.tvYearId)
        }

    }
}