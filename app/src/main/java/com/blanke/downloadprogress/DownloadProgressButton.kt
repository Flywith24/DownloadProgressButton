package com.blanke.downloadprogress

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

class DownloadProgressButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs), View.OnClickListener {
    interface OnDownLoadClickListener {
        fun waiting()
        fun downloading()
        fun pause()
        fun resume()
        fun installing()
        fun finished()
    }

    //背景画笔
    private val mBackgroundPaint: Paint = Paint()
    //背景边框画笔
    private val mBackgroundBorderPaint: Paint = Paint()
    //按钮文字画笔
    private val mTextPaint: Paint = Paint()
    //背景颜色
    private var mBackgroundColor = 0
    //下载中后半部分后面背景颜色
    private var mBackgroundSecondColor = 0
    //文字颜色
    private var mTextColor = 0
    //覆盖后颜色
    var textCoverColor = 0
    private var mProgress = -1f
    private var mToProgress = 0f
    var maxProgress = 0
    var minProgress = 0
    private var mProgressPercent = 0f
    var buttonRadius = 0f
    private var mBackgroundBounds: RectF? = null
    private var mProgressBgGradient: LinearGradient? = null
    private var mProgressTextGradient: LinearGradient? = null
    private lateinit var mProgressAnimation: ValueAnimator
    private var mCurrentText: CharSequence? = null
    private var mState = -1
    //边框宽度 = 0f
    private var mBackgroundStrokeWidth = 0f
    private var mNormalText: String? = null
    private var mWaitingText: String? = null
    private var mDowningText: String? = null
    private var mFinishText: String? = null
    private var mPauseText: String? = null
    private var mInstalling: String? = null
    private var mAnimationDuration: Long = 0
    var onDownLoadClickListener: OnDownLoadClickListener? = null
    var isEnablePause = false

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DownloadProgressButton)
        mBackgroundColor = a.getColor(R.styleable.DownloadProgressButton_backgroud_color, Color.parseColor("#6699ff"))
        mBackgroundSecondColor = a.getColor(R.styleable.DownloadProgressButton_backgroud_second_color, Color.LTGRAY)
        buttonRadius = a.getFloat(R.styleable.DownloadProgressButton_radius, measuredHeight / 2.toFloat())
        mTextColor = a.getColor(R.styleable.DownloadProgressButton_text_color, mBackgroundColor)
        textCoverColor = a.getColor(R.styleable.DownloadProgressButton_text_covercolor, Color.WHITE)
        mBackgroundStrokeWidth = a.getDimension(R.styleable.DownloadProgressButton_backgroud_strokeWidth, 3f)
        mNormalText = a.getString(R.styleable.DownloadProgressButton_text_normal)
        mDowningText = a.getString(R.styleable.DownloadProgressButton_text_downing)
        mFinishText = a.getString(R.styleable.DownloadProgressButton_text_finish)
        mPauseText = a.getString(R.styleable.DownloadProgressButton_text_pause)
        mAnimationDuration = a.getInt(R.styleable.DownloadProgressButton_animation_duration, 500).toLong()
        a.recycle()
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        mTextPaint.textSize = textSize
        invalidate()
    }

    private fun init() {
        maxProgress = 100
        minProgress = 0
        mProgress = 0f
        if (mNormalText == null) {
            mNormalText = "安装"
        }
        if (mWaitingText == null) {
            mWaitingText = "等待中"
        }
        if (mDowningText == null) {
            mDowningText = ""
        }
        if (mInstalling == null) {
            mInstalling = "安装中"
        }
        if (mFinishText == null) {
            mFinishText = "打开"
        }
        if (mPauseText == null) {
            mPauseText = "继续"
        }
        //设置背景画笔
        mBackgroundPaint.isAntiAlias = true
        mBackgroundPaint.style = Paint.Style.FILL
        mBackgroundBorderPaint.isAntiAlias = true
        mBackgroundBorderPaint.style = Paint.Style.STROKE
        mBackgroundBorderPaint.strokeWidth = mBackgroundStrokeWidth
        mBackgroundBorderPaint.color = mBackgroundColor
        //设置文字画笔
        mTextPaint.isAntiAlias = true
        //解决文字有时候画不出问题
        setLayerType(View.LAYER_TYPE_SOFTWARE, mTextPaint)
        //初始化状态设为NORMAL
        state = NORMAL
        setOnClickListener(this)
    }

    override fun onClick(v: View) {

        when (state) {
            NORMAL -> {
                onDownLoadClickListener?.waiting()
                state = WAITING
                setProgressText(0)
            }
            DOWNLOADING -> if (isEnablePause) {
                onDownLoadClickListener?.pause()
                state = PAUSE
            }
            PAUSE -> {
                onDownLoadClickListener?.resume()
                state = DOWNLOADING
                setProgressText(mProgress.toInt())
            }
            INSTALLING -> onDownLoadClickListener?.installing()
            FINISH -> onDownLoadClickListener?.finished()
        }
    }

    private fun setupAnimations() {
        mProgressAnimation = ValueAnimator.ofFloat(0f, 1f).setDuration(mAnimationDuration)
        mProgressAnimation.addUpdateListener { animation ->
            val timePercent = animation.animatedValue as Float
            mProgress += (mToProgress - mProgress) * timePercent
            setProgressText(mProgress.toInt())
        }
        mProgressAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (mToProgress < mProgress) {
                    mProgress = mToProgress
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (mProgress == maxProgress.toFloat()) {
                    state = INSTALLING
                    onDownLoadClickListener?.installing()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInEditMode) {
            drawing(canvas)
        }
    }

    private fun drawing(canvas: Canvas) {
        drawBackground(canvas)
        drawTextAbove(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        if (mBackgroundBounds == null) {
            mBackgroundBounds = RectF().apply {
                if (buttonRadius == 0f) {
                    buttonRadius = measuredHeight / 2.toFloat()
                }
                left = mBackgroundStrokeWidth
                top = mBackgroundStrokeWidth
                right = measuredWidth - mBackgroundStrokeWidth
                bottom = measuredHeight - mBackgroundStrokeWidth
            }
        }

        when (mState) {
            NORMAL, WAITING, FINISH, INSTALLING -> {
            }
            DOWNLOADING, PAUSE -> {
                mProgressPercent = mProgress / (maxProgress + 0f)
                mProgressBgGradient = LinearGradient(mBackgroundStrokeWidth,
                        0f, measuredWidth - mBackgroundStrokeWidth, 0f, intArrayOf(mBackgroundColor, mBackgroundSecondColor), floatArrayOf(mProgressPercent, mProgressPercent + 0.001f),
                        Shader.TileMode.CLAMP
                )
                mBackgroundPaint.color = mBackgroundColor
                mBackgroundPaint.shader = mProgressBgGradient
                canvas.drawRoundRect(mBackgroundBounds!!, buttonRadius, buttonRadius, mBackgroundPaint)
            }
        }
        canvas.drawRoundRect(mBackgroundBounds!!, buttonRadius, buttonRadius, mBackgroundBorderPaint) //绘制边框
    }

    private fun drawTextAbove(canvas: Canvas) {
        mTextPaint.textSize = textSize
        val y = canvas.height / 2f - (mTextPaint.descent() / 2 + mTextPaint.ascent() / 2)
        if (mCurrentText == null) {
            mCurrentText = ""
        }
        val textWidth = mTextPaint.measureText(mCurrentText.toString())
        when (mState) {
            NORMAL, WAITING, INSTALLING, FINISH -> {
                mTextPaint.shader = null
                mTextPaint.color = mTextColor
                canvas.drawText(mCurrentText.toString(), (measuredWidth - textWidth) / 2, y, mTextPaint)
            }
            DOWNLOADING, PAUSE -> {
                val w = measuredWidth - 2 * mBackgroundStrokeWidth
                //进度条压过距离
                val coverLength = w * mProgressPercent
                //开始渐变指示器
                val indicator1 = w / 2 - textWidth / 2
                //结束渐变指示器
                val indicator2 = w / 2 + textWidth / 2
                //文字变色部分的距离
                val coverTextLength = textWidth / 2 - w / 2 + coverLength
                val textProgress = coverTextLength / textWidth
                if (coverLength <= indicator1) {
                    mTextPaint.shader = null
                    mTextPaint.color = mTextColor
                } else if (indicator1 < coverLength && coverLength <= indicator2) {
                    mProgressTextGradient = LinearGradient((w - textWidth) / 2 + mBackgroundStrokeWidth, 0f,
                            (w + textWidth) / 2 + mBackgroundStrokeWidth, 0f, intArrayOf(textCoverColor, mTextColor), floatArrayOf(textProgress, textProgress + 0.001f),
                            Shader.TileMode.CLAMP)
                    mTextPaint.color = mTextColor
                    mTextPaint.shader = mProgressTextGradient
                } else {
                    mTextPaint.shader = null
                    mTextPaint.color = textCoverColor
                }
                canvas.drawText(mCurrentText.toString(), (w - textWidth) / 2 + mBackgroundStrokeWidth, y, mTextPaint)
            }
        }
    }

    var state: Int
        get() = mState
        set(state) {
            if (mState != state) { //状态确实有改变
                mState = state
                when (state) {
                    FINISH -> {
                        currentText = mFinishText
                        mProgress = maxProgress.toFloat()
                    }
                    NORMAL -> {
                        run {
                            mToProgress = minProgress.toFloat()
                            mProgress = mToProgress
                        }
                        currentText = mNormalText
                    }
                    WAITING -> {
                        run {
                            mToProgress = minProgress.toFloat()
                            mProgress = mToProgress
                        }
                        currentText = mWaitingText
                    }
                    PAUSE -> currentText = mPauseText
                    INSTALLING -> currentText = mInstalling
                    DOWNLOADING -> currentText = mDowningText
                }
                invalidate()
            }
        }

    fun reset() {
        state = NORMAL
    }

    fun finish() {
        state = FINISH
    }

    fun downloading() {
        state = DOWNLOADING
        onDownLoadClickListener?.downloading()
    }

    private var currentText: CharSequence?
        get() = mCurrentText
        set(charSequence) {
            mCurrentText = charSequence
            invalidate()
        }

    var progress: Float
        get() = mProgress
        set(progress) {
            if (progress <= minProgress || progress <= mToProgress || state == FINISH) {
                return
            }
            mToProgress = Math.min(progress, maxProgress.toFloat())
            state = DOWNLOADING
            if (mProgressAnimation.isRunning) {
                mProgressAnimation.end()
                mProgressAnimation.start()
            } else {
                mProgressAnimation.start()
            }
        }

    private fun setProgressText(progress: Int) {
        if (state == DOWNLOADING) {
            currentText = "$progress%"
        }
    }

    fun pause() {
        state = PAUSE
    }

    fun getTextColor(): Int {
        return mTextColor
    }

    override fun setTextColor(textColor: Int) {
        mTextColor = textColor
    }

    var animationDuration: Long
        get() = mAnimationDuration
        set(animationDuration) {
            mAnimationDuration = animationDuration
            mProgressAnimation.duration = animationDuration
        }

    companion object {
        const val NORMAL = 1
        const val WAITING = 2
        const val DOWNLOADING = 3
        const val PAUSE = 4
        const val INSTALLING = 5
        const val FINISH = 6
    }

    init {
        if (!isInEditMode) {
            initAttrs(context, attrs)
            init()
            setupAnimations()
        }
    }
}