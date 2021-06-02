package com.laksana.myquranapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.laksana.myquranapp.R
import com.laksana.myquranapp.model.ModelSurah

class SurahAdapter(
    private val items: List<ModelSurah>,
    private val onSelectData: OnSelectedData
) : RecyclerView.Adapter<SurahAdapter.ViewHolder>(){
    interface OnSelectedData {
        fun onSelected(modelSurah: ModelSurah?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_surah, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.txtNumber.text = data.nomor
        holder.txtAyat.text = data.nama
        holder.txtInfo.text = data.type + " - " + data.ayat + " Ayat "
        holder.txtName.text = data.asma
        holder.cvSurah.setOnClickListener { onSelectData.onSelected(data) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    //Class Holder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvSurah: CardView = itemView.findViewById(R.id.cvSurah)
        var txtNumber: TextView = itemView.findViewById(R.id.txtNumber)
        var txtAyat: TextView = itemView.findViewById(R.id.txtAyat)
        var txtInfo: TextView = itemView.findViewById(R.id.txtInfo)
        var txtName: TextView = itemView.findViewById(R.id.txtName)

    }

}
