package com.asura.posterslider

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asura.library.posters.Poster
import com.asura.library.posters.RemoteImage
import com.asura.library.posters.RemoteVideo
import com.asura.library.views.PosterSlider

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val posterSlider = findViewById<PosterSlider>(R.id.poster_slider)
        val posters: MutableList<Poster> = ArrayList()
        posters.add(
            RemoteImage(
                "https://www.pngitem.com/pimgs/m/185-1850014_free-sample-hd-png-download.png",
                "",
                "Header"
            )
        )
        posters.add(
            RemoteVideo(
                Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"),
                "",
                "This is a header 1"
            )
        )
        posters.add(
            RemoteVideo(
                Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                "",
                "This is a header 2"
            )
        )
        posters.add(
            RemoteVideo(
                Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
                "",
                "This is a header 3"
            )
        )
        posters.add(
            RemoteImage(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/640px-Image_created_with_a_mobile_phone.png",
                "",
                "This is a header 4"
            )
        )
        posterSlider.setPosters(posters)
    }
}