package com.yagubogu.presentation.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.yagubogu.R
import com.yagubogu.databinding.ItemStadiumSpinnerDropdownBinding
import com.yagubogu.databinding.ItemStadiumSpinnerSelectedBinding

class StadiumSpinnerAdapter(
    context: Context,
    private val items: List<String>,
) : ArrayAdapter<String>(context, R.layout.item_stadium_spinner_selected, items) {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view: View
        val binding: ItemStadiumSpinnerSelectedBinding

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = ItemStadiumSpinnerSelectedBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ItemStadiumSpinnerSelectedBinding
        }

        binding.tvStadiumName.text =
            context.getString(R.string.home_stadium_pie_chart_title, items[position])
        return view
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view: View
        val binding: ItemStadiumSpinnerDropdownBinding

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = ItemStadiumSpinnerDropdownBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ItemStadiumSpinnerDropdownBinding
        }

        binding.tvStadiumName.text = items[position]
        return view
    }
}
