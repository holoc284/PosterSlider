package com.asura.library.views.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asura.library.events.IVideoPlayListener;
import com.asura.library.events.OnPosterClickListener;
import com.asura.library.posters.BitmapImage;
import com.asura.library.posters.DrawableImage;
import com.asura.library.posters.ImagePoster;
import com.asura.library.posters.Poster;
import com.asura.library.posters.RawVideo;
import com.asura.library.posters.RemoteImage;
import com.asura.library.posters.RemoteVideo;
import com.asura.library.posters.VideoPoster;
import com.asura.library.views.AdjustableImageView;
import com.asura.library.views.PosterSlider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import static com.google.android.exoplayer2.Player.STATE_ENDED;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class PosterFragment extends Fragment implements Player.Listener {

    private Poster poster;
    private IVideoPlayListener videoPlayListener;

    private ExoPlayer player2;
    private boolean isLooping;

    public PosterFragment() {
        // Required empty public constructor
    }

    public static PosterFragment newInstance(@NonNull Poster poster, IVideoPlayListener videoPlayListener) {
        PosterFragment fragment = new PosterFragment();
        fragment.setVideoPlayListener(videoPlayListener);
        Bundle args = new Bundle();
        args.putParcelable("poster", poster);
        fragment.setArguments(args);
        return fragment;
    }

    public void setVideoPlayListener(IVideoPlayListener videoPlayListener) {
        this.videoPlayListener = videoPlayListener;
        isLooping = ((PosterSlider) videoPlayListener).getMustLoopSlides();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        poster = getArguments().getParcelable("poster");
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (poster != null) {
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            // create TextView
            TextView txtTitle = new TextView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 10, 0, 15);
            txtTitle.setLayoutParams(layoutParams);
            txtTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            txtTitle.setTextSize(16f);

            linearLayout.addView(txtTitle);

            if(poster instanceof ImagePoster){
                final ImageView imageView = new ImageView(getActivity());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setAdjustViewBounds(true);
                ImagePoster imagePoster = (ImagePoster) poster;
                imageView.setScaleType(imagePoster.getScaleType());
                if(imagePoster instanceof DrawableImage){
                    DrawableImage image = (DrawableImage) imagePoster;
                    Glide.with(getActivity())
                            .load(image.getDrawable())
                            .into(imageView);
                }else if(imagePoster instanceof BitmapImage){
                    BitmapImage image = (BitmapImage) imagePoster;
                    Glide.with(getActivity())
                            .load(image.getBitmap())
                            .into(imageView);
                }else {
                    final RemoteImage image = (RemoteImage) imagePoster;
                    txtTitle.setText(image.getName());
                    if (image.getErrorDrawable() == null && image.getPlaceHolder() == null) {
                        Glide.with(getActivity()).load(image.getUrl()).into(imageView);
                    } else {
                        if (image.getPlaceHolder() != null && image.getErrorDrawable() != null) {
                            Glide.with(getActivity())
                                    .load(image.getUrl())
                                    .apply(new RequestOptions()
                                            .placeholder(image.getPlaceHolder()))
                                    .into(imageView);
                        } else if (image.getErrorDrawable() != null) {
                            Glide.with(getActivity())
                                    .load(image.getUrl())
                                    .apply(new RequestOptions()
                                            .error(image.getErrorDrawable()))
                                    .into(imageView);
                        } else if (image.getPlaceHolder() != null) {
                            Glide.with(getActivity())
                                    .load(image.getUrl())
                                    .apply(new RequestOptions()
                                            .placeholder(image.getPlaceHolder()))
                                    .into(imageView);
                        }
                    }
                }
                imageView.setOnTouchListener(poster.getOnTouchListener());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OnPosterClickListener onPosterClickListener = poster.getOnPosterClickListener();
                        if(onPosterClickListener!=null){
                            onPosterClickListener.onClick(poster.getPosition());
                        }
                    }
                });
                linearLayout.addView(imageView);
                return linearLayout;
            } else if (poster instanceof VideoPoster) {
                final StyledPlayerView playerView = new StyledPlayerView(getActivity());
                playerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                DefaultTrackSelector trackSelector = new DefaultTrackSelector(getActivity());
                player2 = new ExoPlayer.Builder(getActivity()).setTrackSelector(trackSelector).build();

                playerView.setPlayer(player2);
                playerView.setControllerShowTimeoutMs(1000);
//                if (isLooping) {
//                    playerView.setUseController(false);
//                }

                if (poster instanceof RawVideo) {
                    RawVideo video = (RawVideo) poster;
                    DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(video.getRawResource()));
                    final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(getActivity());
                    try {
                        rawResourceDataSource.open(dataSpec);
                    } catch (RawResourceDataSource.RawResourceDataSourceException e) {
                        e.printStackTrace();
                    }
                    DataSource.Factory factory = new DataSource.Factory() {
                        @NonNull
                        @Override
                        public DataSource createDataSource() {
                            return rawResourceDataSource;
                        }
                    };
                    assert rawResourceDataSource.getUri() != null;
                    ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(rawResourceDataSource.getUri()));
                    player2.setMediaSource(mediaSource);
                } else if (poster instanceof RemoteVideo) {
                    RemoteVideo video = (RemoteVideo) poster;
                    txtTitle.setText(video.getName());
                    String videoUrl = video.getUri().toString();
                    boolean ResultUri = videoUrl.contains("https://www.youtube.com/");
                    if (ResultUri) {
                        new YouTubeExtractor(requireActivity()) {
                            @Override
                            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                                if (ytFiles != null) {
                                    int videoTag = 22; //137
                                    int audioTag = 140;
                                    DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
                                    // Create a progressive media source pointing to a stream uri.
                                    MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                                            .createMediaSource(MediaItem.fromUri(ytFiles.get(audioTag).getUrl()));
                                    MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                                            .createMediaSource(MediaItem.fromUri(ytFiles.get(videoTag).getUrl()));

//                                    player2.setMediaSource(new MergingMediaSource(
//                                                    true,
//                                                    videoSource,
//                                                    audioSource),
//                                            true
//                                    );
                                    player2.setMediaSource(videoSource);
                                    player2.prepare();
                                }
                            }
                        }.extract(videoUrl, false, true);
                    } else {
                        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
                        // Create a progressive media source pointing to a stream uri.
                        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(video.getUri()));
                        player2.setMediaSource(mediaSource);
                        player2.prepare();
                    }
                }

                linearLayout.addView(playerView);
                return linearLayout;
            } else {
                throw new RuntimeException("Unknown Poster kind");
            }
        } else {
            throw new RuntimeException("Poster cannot be null");
        }
    }

    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (isLooping && playbackState == STATE_ENDED) {
            videoPlayListener.onVideoStopped(true);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (player2 != null) {
            Log.e("onDetach", "release exoplayer ...");
            player2.setPlayWhenReady(false);
            player2.removeListener(this);
            player2.stop();
            player2.release();
            player2 = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player2 != null) {
            Log.e("onDestroyView", "release exoplayer ...");
            player2.setPlayWhenReady(false);
            player2.removeListener(this);
            player2.stop();
            player2.release();
            player2 = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player2 != null) {
            Log.e("onDestroy", "release exoplayer ...");
            player2.setPlayWhenReady(false);
            player2.removeListener(this);
            player2.stop();
            player2.release();
            player2 = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isLooping && player2 != null) {
            videoPlayListener.onVideoStarted();
            if (player2.getPlaybackState() == STATE_ENDED) {
                player2.seekTo(0);
            }
            player2.setPlayWhenReady(true);
            player2.addListener(this);
        }
        if (player2 != null) {
            if (player2.getPlaybackState() == Player.STATE_READY) {
                player2.setPlayWhenReady(isVisibleToUser);
                player2.addListener(this);
                if (!isVisibleToUser) videoPlayListener.onVideoStopped(false);
                else videoPlayListener.onVideoStarted();
            }
        }
    }
}
