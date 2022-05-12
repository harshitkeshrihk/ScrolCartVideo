   package com.hk.skrolcartvideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hk.skrolcartvideo.databinding.ActivityMainBinding
import com.littlemango.stacklayoutmanager.StackLayoutManager


   class MainActivity : AppCompatActivity() {

       private lateinit var videoArray: ArrayList<ModelVideo>
       private lateinit var adapterVideo : Adapter
       private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        title = "Videos"

        val linearSnapHelper: LinearSnapHelper = SnapHelperOneByOne()
        linearSnapHelper.attachToRecyclerView(binding.videosRv)


        videosFirebase()
        binding.addVideoFab.setOnClickListener{
            startActivity(Intent(this,AddVideoActivity::class.java))
        }

    }

       private fun videosFirebase(){
           videoArray = ArrayList()

           val ref = FirebaseDatabase.getInstance().getReference("Videos")
           ref.addValueEventListener(object: ValueEventListener{
               override fun onDataChange(snapshot: DataSnapshot) {
                   videoArray.clear()
                   for (item in snapshot.children){
                       val video = item.getValue(ModelVideo::class.java)
                       videoArray.add(video!!)
                   }
                   adapterVideo = Adapter(this@MainActivity,videoArray)
                   binding.videosRv.adapter = adapterVideo
//                   val layoutManager = StackLayoutManager(StackLayoutManager.ScrollOrientation.BOTTOM_TO_TOP)
//                   layoutManager.setPagerMode(true)
//                   layoutManager.setPagerFlingVelocity(1000)
//                   binding.videosRv.layoutManager = layoutManager
               }

               override fun onCancelled(error: DatabaseError) {

               }
           })
       }
}