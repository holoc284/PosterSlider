package com.asura.posterslider;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.asura.library.posters.DrawableImage;
import com.asura.library.posters.Poster;
import com.asura.library.posters.RawVideo;
import com.asura.library.posters.RemoteVideo;
import com.asura.library.views.PosterSlider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PosterSlider posterSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        posterSlider = (PosterSlider) findViewById(R.id.poster_slider);
        List<Poster> posters = new ArrayList<>();
        posters.add(new DrawableImage(R.drawable.img_5126));
        posters.add(new RemoteVideo(Uri.parse("http://192.168.2.3/blitarkoi/public/assets_lp/media_komentar/1655434645_1269e9e71e2ce3531b86.mp4")));
        posters.add(new RemoteVideo(Uri.parse("http://192.168.2.3/blitarkoi/public/assets_lp/media_komentar/1655370932_4786b5330b7526258165.mp4")));
        posters.add(new RemoteVideo(Uri.parse("http://192.168.2.3/blitarkoi/public/assets_lp/media_komentar/1655371112_e1e5005950adeec2b812.mp4")));
        posterSlider.setPosters(posters);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
