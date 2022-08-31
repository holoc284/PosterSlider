package com.asura.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.asura.library.R
import com.asura.library.events.IVideoPlayListener
import com.asura.library.events.OnPosterClickListener
import com.asura.library.posters.Poster
import com.asura.library.views.indicators.IndicatorShape
import java.util.*

class MhySlider : FrameLayout, OnPageChangeListener, IAttributeChange, IVideoPlayListener {
    private val TAG = "PosterSlider"
    private var posters: MutableList<Poster> = ArrayList()
    private var hostActivity: AppCompatActivity? = null
    private var viewPager: CustomViewPager? = null

    //CustomAttributes
    private var selectedSlideIndicator: Drawable? = null
    private var unSelectedSlideIndicator: Drawable? = null
    private var defaultIndicator = 0
    private var indicatorSize = 0
    private var mustAnimateIndicators = false
    var mustLoopSlides = false
        private set
    private var defaultPoster = 0
    private var imageSlideInterval = 5000
    private var hideIndicators = false
    private val mustWrapContent = false
    private var slideIndicatorsGroup: SlideIndicatorsGroup? = null

    companion object {
        private var handlerThread: HandlerThread? = null

        init {
            handlerThread = HandlerThread("TimerThread")
            handlerThread!!.start()
        }
    }

    private var setupIsCalled = false
    var posterQueue: MutableList<Poster> = ArrayList()
    private var onPosterClickListener: OnPosterClickListener? = null
    private var timer: Timer? = null
    private var posterAdapter: MhyAdapter? = null
    private var videoStartedinAutoLoop = false

    @LayoutRes
    private val emptyView = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        parseCustomAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        parseCustomAttributes(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        parseCustomAttributes(attrs)
    }

    private fun parseCustomAttributes(attributeSet: AttributeSet?) {
        if (attributeSet != null) {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PosterSlider)
            try {
                selectedSlideIndicator =
                    typedArray.getDrawable(R.styleable.PosterSlider_selectedSlideIndicator)
                unSelectedSlideIndicator =
                    typedArray.getDrawable(R.styleable.PosterSlider_unSelectedSlideIndicator)
                defaultIndicator = typedArray.getInteger(
                    R.styleable.PosterSlider_defaultIndicator,
                    IndicatorShape.DASH
                )
                indicatorSize = typedArray.getDimensionPixelSize(
                    R.styleable.PosterSlider_indicatorSize,
                    resources.getDimensionPixelSize(R.dimen.default_indicator_size)
                )
                mustAnimateIndicators =
                    typedArray.getBoolean(R.styleable.PosterSlider_animateIndicators, true)
                mustLoopSlides = typedArray.getBoolean(R.styleable.PosterSlider_loopSlides, false)
                defaultPoster = typedArray.getInt(R.styleable.PosterSlider_defaultPoster, 0)
                imageSlideInterval =
                    typedArray.getInt(R.styleable.PosterSlider_imageSlideInterval, 0)
                hideIndicators =
                    typedArray.getBoolean(R.styleable.PosterSlider_hideIndicators, false)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }
        if (!isInEditMode) {
            setup()
        }
    }

    private fun setup() {
        if (!isInEditMode) {
            post {
                hostActivity = if (context is AppCompatActivity) {
                    context as AppCompatActivity
                } else {
                    throw RuntimeException("Host activity must extend AppCompatActivity")
                }
                val mustMakeViewPagerWrapContent =
                    layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT
                viewPager = CustomViewPager(context, mustMakeViewPagerWrapContent)
                viewPager!!.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                viewPager!!.id = generateViewId()
                viewPager!!.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                viewPager!!.addOnPageChangeListener(this@MhySlider)
                addView(viewPager)
                slideIndicatorsGroup = SlideIndicatorsGroup(
                    context,
                    selectedSlideIndicator,
                    unSelectedSlideIndicator,
                    defaultIndicator,
                    indicatorSize,
                    mustAnimateIndicators
                )
                if (!hideIndicators) {
                    addView(slideIndicatorsGroup)
                }
                setupTimer()
                setupIsCalled = true
                renderRemainingPosters()
            }
        }
    }

    private fun renderRemainingPosters() {
        setPosters(posterQueue)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setPosters(posters: MutableList<Poster>) {
        if (setupIsCalled) {
            this.posters = posters
            for (i in posters.indices) {
                posters[i].position = i
                posters[i].onPosterClickListener = onPosterClickListener
                posters[i].onTouchListener = OnTouchListener { view, motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        stopTimer()
                    } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                        setupTimer()
                    }
                    false
                }
                slideIndicatorsGroup!!.onSlideAdd()
            }
            posterAdapter = MhyAdapter(
                hostActivity!!.supportFragmentManager, mustLoopSlides,
                layoutDirection, posters
            )
            posterAdapter!!.setVideoPlayListener(this)
            viewPager!!.adapter = posterAdapter
            if (mustLoopSlides) {
                if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                    viewPager!!.setCurrentItem(1, false)
                    slideIndicatorsGroup!!.onSlideChange(0)
                } else {
                    viewPager!!.setCurrentItem(posters.size, false)
                    slideIndicatorsGroup!!.onSlideChange(posters.size - 1)
                }
            }
        } else {
            posterQueue.addAll(posters)
        }
    }

    private fun setupTimer() {
        if (imageSlideInterval > 0 && mustLoopSlides) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    (context as AppCompatActivity).runOnUiThread {
                        if (!mustLoopSlides) {
                            if (viewPager!!.currentItem == posters.size - 1) {
                                viewPager!!.setCurrentItem(0, true)
                            } else {
                                viewPager!!.setCurrentItem(viewPager!!.currentItem + 1, true)
                            }
                        } else {
                            if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                                viewPager!!.setCurrentItem(viewPager!!.currentItem + 1, true)
                            } else {
                                viewPager!!.setCurrentItem(viewPager!!.currentItem - 1, true)
                            }
                        }
                    }
                }
            }, imageSlideInterval.toLong(), imageSlideInterval.toLong())
        }
    }

    private fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    override fun onPageSelected(position: Int) {
        if (mustLoopSlides) {
            if (position == 0) {
                postDelayed({ viewPager!!.setCurrentItem(posters.size, false) }, 400)
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup!!.onSlideChange(posters.size - 1)
                }
            } else if (position == posters.size + 1) {
                postDelayed({ viewPager!!.setCurrentItem(1, false) }, 400)
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup!!.onSlideChange(0)
                }
            } else {
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup!!.onSlideChange(position - 1)
                }
            }
        } else {
            slideIndicatorsGroup!!.onSlideChange(position)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageScrollStateChanged(state: Int) {
        when (state) {
            ViewPager.SCROLL_STATE_DRAGGING -> stopTimer()
            ViewPager.SCROLL_STATE_IDLE -> if (timer == null && !videoStartedinAutoLoop) {
                setupTimer()
            }
        }
    }

    fun setOnPosterClickListener(onPosterClickListener: OnPosterClickListener?) {
        this.onPosterClickListener = onPosterClickListener
        for (poster in posters) {
            poster.onPosterClickListener = onPosterClickListener
        }
    }

    fun setDefaultIndicator(indicator: Int) {
        post {
            defaultIndicator = indicator
            slideIndicatorsGroup!!.changeIndicator(indicator)
            if (mustLoopSlides) {
                if (viewPager!!.currentItem == 0) {
                    postDelayed({ viewPager!!.setCurrentItem(posters.size, false) }, 400)
                    if (slideIndicatorsGroup != null) {
                        slideIndicatorsGroup!!.onSlideChange(posters.size - 1)
                    }
                } else if (viewPager!!.currentItem == posters.size + 1) {
                    postDelayed({ viewPager!!.setCurrentItem(1, false) }, 400)
                    if (slideIndicatorsGroup != null) {
                        slideIndicatorsGroup!!.onSlideChange(0)
                    }
                } else {
                    if (slideIndicatorsGroup != null) {
                        slideIndicatorsGroup!!.onSlideChange(viewPager!!.currentItem - 1)
                    }
                }
            } else {
                slideIndicatorsGroup!!.onSlideChange(viewPager!!.currentItem)
            }
        }
    }

    fun setCustomIndicator(selectedSlideIndicator: Drawable?, unSelectedSlideIndicator: Drawable?) {
        this.selectedSlideIndicator = selectedSlideIndicator
        this.unSelectedSlideIndicator = unSelectedSlideIndicator
        slideIndicatorsGroup!!.changeIndicator(selectedSlideIndicator, unSelectedSlideIndicator)
        if (mustLoopSlides) {
            if (viewPager!!.currentItem == 0) {
                postDelayed({ viewPager!!.setCurrentItem(posters.size, false) }, 400)
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup!!.onSlideChange(posters.size - 1)
                }
            } else if (viewPager!!.currentItem == posters.size + 1) {
                postDelayed({ viewPager!!.setCurrentItem(1, false) }, 400)
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup!!.onSlideChange(0)
                }
            } else {
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup!!.onSlideChange(viewPager!!.currentItem - 1)
                }
            }
        } else {
            slideIndicatorsGroup!!.onSlideChange(viewPager!!.currentItem)
        }
    }

    fun setCurrentSlide(position: Int) {
        post {
            if (viewPager != null) {
                viewPager!!.currentItem = position
            }
        }
    }

    fun setInterval(interval: Int) {
        imageSlideInterval = interval
        onIntervalChange()
    }

    fun setIndicatorSize(indicatorSize: Int) {
        this.indicatorSize = indicatorSize
        onIndicatorSizeChange()
    }

    fun setLoopSlides(loopSlides: Boolean) {
        mustLoopSlides = loopSlides
    }

    fun setMustAnimateIndicators(mustAnimateIndicators: Boolean) {
        this.mustAnimateIndicators = mustAnimateIndicators
        onAnimateIndicatorsChange()
    }

    fun setHideIndicators(hideIndicators: Boolean) {
        this.hideIndicators = hideIndicators
        onHideIndicatorsValueChanged()
    }

    val currentSlidePosition: Int
        get() = if (viewPager == null) -1 else viewPager!!.currentItem

    // Events
    ///////////////////////////////////////////////////////////////////////////
    override fun onIndicatorSizeChange() {
        if (!hideIndicators) {
            if (slideIndicatorsGroup != null) {
                removeView(slideIndicatorsGroup)
            }
            slideIndicatorsGroup = SlideIndicatorsGroup(
                context,
                selectedSlideIndicator,
                unSelectedSlideIndicator,
                defaultIndicator,
                indicatorSize,
                mustAnimateIndicators
            )
            addView(slideIndicatorsGroup)
            for (i in posters.indices) {
                slideIndicatorsGroup!!.onSlideAdd()
            }
        }
    }

    override fun onSelectedSlideIndicatorChange() {}
    override fun onUnselectedSlideIndicatorChange() {}
    override fun onDefaultIndicatorsChange() {}
    override fun onAnimateIndicatorsChange() {
        if (slideIndicatorsGroup != null) {
            slideIndicatorsGroup!!.setMustAnimateIndicators(mustAnimateIndicators)
        }
    }

    override fun onIntervalChange() {
        stopTimer()
        setupTimer()
    }

    override fun onLoopSlidesChange() {}
    override fun onDefaultBannerChange() {}
    override fun onEmptyViewChange() {}
    override fun onHideIndicatorsValueChanged() {
        if (slideIndicatorsGroup != null) {
            removeView(slideIndicatorsGroup)
        }
        if (!hideIndicators) {
            slideIndicatorsGroup = SlideIndicatorsGroup(
                context,
                selectedSlideIndicator,
                unSelectedSlideIndicator,
                defaultIndicator,
                indicatorSize,
                mustAnimateIndicators
            )
            addView(slideIndicatorsGroup)
            for (i in posters.indices) {
                slideIndicatorsGroup!!.onSlideAdd()
            }
        }
    }

    fun removeAllPosters() {
        posters.clear()
        slideIndicatorsGroup!!.removeAllViews()
        slideIndicatorsGroup!!.setSlides(0)
        invalidate()
        requestLayout()
    }

    override fun onVideoStarted() {
        videoStartedinAutoLoop = true
        stopTimer()
    }

    override fun onVideoStopped() {
        setupTimerWithNoDelay()
        videoStartedinAutoLoop = false
    }

    private fun setupTimerWithNoDelay() {
        if (imageSlideInterval > 0) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    (context as AppCompatActivity).runOnUiThread {
                        if (!mustLoopSlides) {
                            if (viewPager!!.currentItem == posters.size - 1) {
                                viewPager!!.setCurrentItem(0, true)
                            } else {
                                viewPager!!.setCurrentItem(viewPager!!.currentItem + 1, true)
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                                    viewPager!!.setCurrentItem(viewPager!!.currentItem + 1, true)
                                } else {
                                    viewPager!!.setCurrentItem(viewPager!!.currentItem - 1, true)
                                }
                            } else {
                                viewPager!!.setCurrentItem(viewPager!!.currentItem - 1, true)
                            }
                        }
                    }
                }
            }, 0, imageSlideInterval.toLong())
        }
    }
}