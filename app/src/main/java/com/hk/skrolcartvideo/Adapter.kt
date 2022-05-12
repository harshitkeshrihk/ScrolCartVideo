package com.hk.skrolcartvideo

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class Adapter(
    private var context: Context,
    private var videoArrayList: ArrayList<ModelVideo>?
) : RecyclerView.Adapter<Adapter.VideoHolder>(){

    class VideoHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var videoView: VideoView = itemView.findViewById(R.id.videoView)
        var titleTv: TextView = itemView.findViewById(R.id.titleTv)
        var timeTv: TextView = itemView.findViewById(R.id.timeTv)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_video,parent,false)
        return VideoHolder(view)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val modelVideo = videoArrayList!![position]

        val id:String? = modelVideo.id
        val title:String? = modelVideo.title
//        val timestamp:String? = modelVideo.timeStamp
        val videoUri:String? = modelVideo.videoUri


//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = timestamp!!.toLong()
//        val formattedDateTime = DateFormat.format("dd/MM/yyyy K:mm a",calendar).toString()

        holder.titleTv.text = title
//        holder.timeTv.text = formattedDateTime
        setVideoUrl(modelVideo, holder)
    }

    private fun setVideoUrl(modelVideo: ModelVideo,holder: VideoHolder) {
        holder.progressBar.visibility = View.VISIBLE
        val videoUrl:String? = modelVideo.videoUri

        val mediaController = MediaController(context)
        mediaController.setAnchorView(holder.videoView)
        val videoUri = Uri.parse(videoUrl)

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(videoUri)
        holder.videoView.requestFocus()

        holder.videoView.setOnPreparedListener{mediaPlayer ->
            mediaPlayer.start()
        }

        holder.videoView.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START->{
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START->{
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END->{
                    holder.progressBar.visibility = View.GONE
                    return@OnInfoListener true
                }
            }

            false
        })

        holder.videoView.setOnCompletionListener { mediaPlayer->
           mediaPlayer.start()
        }
    }

    override fun getItemCount(): Int {
        return videoArrayList!!.size
    }
}