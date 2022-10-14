package com.asura.posterslider;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.asura.library.posters.Poster;
import com.asura.library.posters.RemoteImage;
import com.asura.library.posters.RemoteVideo;
import com.asura.library.views.PosterSlider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PosterSlider posterSlider = findViewById(R.id.poster_slider);
        List<Poster> posters = new ArrayList<>();
        posters.add(new RemoteVideo(Uri.parse("https://www.youtube.com/watch?v=hLRnu7C6XYg"), "", "Header"));
        posters.add(new RemoteVideo(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"), "", "This is a header 1"));
        posters.add(new RemoteVideo(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"), "", "This is a header 2"));
        posters.add(new RemoteVideo(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"), "", "This is a header 3"));
        posters.add(new RemoteImage("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/640px-Image_created_with_a_mobile_phone.png", "", "This is a header 4"));
        posterSlider.setPosters(posters);

    }
}
