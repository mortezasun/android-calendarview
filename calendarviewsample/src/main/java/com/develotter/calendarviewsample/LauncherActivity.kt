package com.develotter.calendarviewsample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.develotter.calendarviewsample.databinding.LauncherActivityBinding
import java.util.Vector

class LauncherActivity: AppCompatActivity()  {
    private lateinit var binding: LauncherActivityBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = LauncherActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val adapter = MyAdapter(getData())

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        binding.list.layoutManager = layoutManager

    }




    private fun getData(): Vector<MyData> {
        val data = Vector<MyData>()
        data.add(MyData(1,"Calendar View", R.drawable.baseline_calendar_month_24))
        data.add(MyData(2,"Heatmap Calendar", R.drawable.baseline_map_24))

        return data
    }

    data class MyData(val index: Int,val title: String, val imageResource: Int)

    inner class MyAdapter(private val data: Vector<MyData>) :
        RecyclerView.Adapter<MyViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MyViewHolder {
            val itemView = layoutInflater.inflate(R.layout.row_launcher, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(data[position])
            holder.itemView.setOnClickListener {
                when(data[position].index){
                    1->{
                        val intent = Intent(this@LauncherActivity, CalendarViewActivity::class.java)
                        startActivity(intent)
                    }
                    2->{
                        val intent = Intent(this@LauncherActivity, HeatMapViewActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

        }
        override fun getItemCount() = data.size
    }
    inner class MyViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: android.widget.TextView = itemView.findViewById(R.id.titleTextView)
        private val imageView: android.widget.ImageView = itemView.findViewById(R.id.imageView)

        fun bind(myData: MyData) {
            titleTextView.text = myData.title
            imageView.setImageResource(myData.imageResource)

        }
    }
}