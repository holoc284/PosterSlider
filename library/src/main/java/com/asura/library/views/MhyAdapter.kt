package com.asura.library.views

import android.annotation.SuppressLint
import android.util.LayoutDirection
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.asura.library.events.IVideoPlayListener
import com.asura.library.posters.Poster
import com.asura.library.views.fragments.EmptyViewFragment
import com.asura.library.views.fragments.PosterFragment
import java.util.*

class MhyAdapter : FragmentStatePagerAdapter {
    private var posters: List<Poster?>
    private var isLooping: Boolean

    @LayoutRes
    private var emptyView = 0
    private var videoPlayListener: IVideoPlayListener? = null

    constructor(
        supportFragmentManager: FragmentManager?,
        isLooping: Boolean,
        posters: List<Poster?>
    ) : super(
        supportFragmentManager!!
    ) {
        this.isLooping = isLooping
        this.posters = posters
    }

    constructor(
        supportFragmentManager: FragmentManager?,
        isLooping: Boolean,
        layoutDirection: Int,
        posters: List<Poster?>
    ) : super(
        supportFragmentManager!!
    ) {
        this.isLooping = isLooping
        this.posters = posters
        if (layoutDirection == LayoutDirection.RTL) {
            Collections.reverse(posters)
        }
    }

    @SuppressLint("ResourceType")
    override fun getItem(position: Int): Fragment {
        if (posters.isEmpty() && emptyView > 0) {
            return EmptyViewFragment.newInstance(emptyView)
        }
        return if (isLooping) {
            if (position == 0) {
                PosterFragment.newInstance(posters[posters.size - 1]!!, videoPlayListener)
            } else if (position == posters.size + 1) {
                PosterFragment.newInstance(posters[0]!!, videoPlayListener)
            } else {
                PosterFragment.newInstance(posters[position - 1]!!, videoPlayListener)
            }
        } else {
            PosterFragment.newInstance(posters[position]!!, videoPlayListener)
        }
    }

    @SuppressLint("ResourceType")
    override fun getCount(): Int {
        if (posters.isEmpty()) {
            return if (emptyView > 0) {
                1
            } else {
                0
            }
        }
        return if (isLooping) {
            posters.size + 2
        } else {
            posters.size
        }
    }

    fun setVideoPlayListener(videoPlayListener: IVideoPlayListener?) {
        this.videoPlayListener = videoPlayListener
    }

    fun setEmptyView(emptyView: Int) {
        this.emptyView = emptyView
        notifyDataSetChanged()
    }
}