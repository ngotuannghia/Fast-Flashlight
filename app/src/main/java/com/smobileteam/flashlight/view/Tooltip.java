package com.smobileteam.flashlight.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.smobileteam.flashlight.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.smobileteam.flashlight.view.Tooltip.Gravity.BOTTOM;
import static com.smobileteam.flashlight.view.Tooltip.Gravity.CENTER;
import static com.smobileteam.flashlight.view.Tooltip.Gravity.LEFT;
import static com.smobileteam.flashlight.view.Tooltip.Gravity.RIGHT;
import static com.smobileteam.flashlight.view.Tooltip.Gravity.TOP;

/**
 * Created by Alessandro Crugnola on 12/12/15.
 * alessandro.crugnola@gmail.com
 */
public final class Tooltip {

    private Tooltip() {
        // empty
    }

    @SuppressWarnings ("unused")
    public static TooltipView make(Context context, Builder builder) {
        return new TooltipViewImpl(context, builder);
    }

    @SuppressWarnings ("unused")
    public static boolean remove(Context context, final int tooltipId) {
        final Activity act = TooltipUtils.getActivity(context);
        if (act != null) {
            ViewGroup rootView;
            rootView = (ViewGroup) (act.getWindow().getDecorView());
            for (int i = 0; i < rootView.getChildCount(); i++) {
                final View child = rootView.getChildAt(i);
                if (child instanceof TooltipView) {
                    if (((TooltipView) child).getTooltipId() == tooltipId) {
                        ((TooltipView) child).remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings ("unused")
    public static boolean removeAll(Context context) {
        final Activity act = TooltipUtils.getActivity(context);
        if (act != null) {
            ViewGroup rootView;
            rootView = (ViewGroup) (act.getWindow().getDecorView());
            for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
                final View child = rootView.getChildAt(i);
                if (child instanceof TooltipView) {
                    ((TooltipView) child).remove();
                }
            }
        }
        return false;
    }

    @SuppressWarnings ("unused")
    public static class ClosePolicy {
        static final int NONE = 0;
        static final int TOUCH_INSIDE = 1 << 1;
        static final int TOUCH_OUTSIDE = 1 << 2;
        static final int CONSUME_INSIDE = 1 << 3;
        static final int CONSUME_OUTSIDE = 1 << 4;
        private int policy;
        public static final ClosePolicy TOUCH_NONE = new ClosePolicy(NONE);
        public static final ClosePolicy TOUCH_INSIDE_CONSUME = new ClosePolicy(TOUCH_INSIDE | CONSUME_INSIDE);
        public static final ClosePolicy TOUCH_INSIDE_NO_CONSUME = new ClosePolicy(TOUCH_INSIDE);
        public static final ClosePolicy TOUCH_OUTSIDE_CONSUME = new ClosePolicy(TOUCH_OUTSIDE | CONSUME_OUTSIDE);
        public static final ClosePolicy TOUCH_OUTSIDE_NO_CONSUME = new ClosePolicy(TOUCH_OUTSIDE);
        public static final ClosePolicy TOUCH_ANYWHERE_NO_CONSUME = new ClosePolicy(TOUCH_INSIDE | TOUCH_OUTSIDE);
        public static final ClosePolicy TOUCH_ANYWHERE_CONSUME =
                new ClosePolicy(TOUCH_INSIDE | TOUCH_OUTSIDE | CONSUME_INSIDE | CONSUME_OUTSIDE);

        public ClosePolicy() {
            policy = NONE;
        }

        ClosePolicy(final int policy) {
            this.policy = policy;
        }

        public ClosePolicy insidePolicy(boolean close, boolean consume) {
            policy = close ? policy | TOUCH_INSIDE : policy & ~TOUCH_INSIDE;
            policy = consume ? policy | CONSUME_INSIDE : policy & ~CONSUME_INSIDE;
            return this;
        }

        public ClosePolicy outsidePolicy(boolean close, boolean consume) {
            policy = close ? policy | TOUCH_OUTSIDE : policy & ~TOUCH_OUTSIDE;
            policy = consume ? policy | CONSUME_OUTSIDE : policy & ~CONSUME_OUTSIDE;
            return this;
        }

        public ClosePolicy clear() {
            policy = NONE;
            return this;
        }

        public int build() {
            return policy;
        }

        public int getPolicy() {
            return policy;
        }

        public static boolean touchInside(final int value) {
            return (value & TOUCH_INSIDE) == TOUCH_INSIDE;
        }

        public static boolean touchOutside(final int value) {
            return (value & TOUCH_OUTSIDE) == TOUCH_OUTSIDE;
        }

        public static boolean consumeInside(final int value) {
            return (value & CONSUME_INSIDE) == CONSUME_INSIDE;
        }

        public static boolean consumeOutside(final int value) {
            return (value & CONSUME_OUTSIDE) == CONSUME_OUTSIDE;
        }

    }

    public enum Gravity {
        LEFT, RIGHT, TOP, BOTTOM, CENTER
    }

    @SuppressWarnings ("unused")
    public interface TooltipView {
        void show();

        void hide();

        void remove();

        int getTooltipId();

        void offsetTo(int x, int y);

        void offsetBy(int x, int y);

        void offsetXBy(float x);

        void offsetXTo(float x);

        boolean isAttached();

        boolean isShown();

        void setText(final CharSequence text);

        void setText(@StringRes int resId);

        void setTextColor(final int color);

        void setTextColor(final ColorStateList color);

        void requestLayout();
    }

    public interface Callback {
        /**
         * Tooltip is being closed
         *
         * @param tooltip       the tooltip being closed
         * @param fromUser      true if the close operation started from a user click
         * @param containsTouch true if the original touch came from inside the tooltip
         */
        void onTooltipClose(final TooltipView tooltip, final boolean fromUser, final boolean containsTouch);

        /**
         * Tooltip failed to show (not enough space)
         */
        void onTooltipFailed(final TooltipView view);

        void onTooltipShown(final TooltipView view);

        void onTooltipHidden(final TooltipView view);
    }

    @SuppressLint ("ViewConstructor")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    static class TooltipViewImpl extends ViewGroup implements TooltipView {
        public static final int TOLERANCE_VALUE = 10;
        private static final List<Gravity> GRAVITY_LIST = new ArrayList<>(Arrays.asList(LEFT, RIGHT, TOP, BOTTOM, CENTER));
        private final List<Gravity> viewGravities = new ArrayList<>(GRAVITY_LIST);
        private final long mShowDelay;
        private final int mTextAppearance;
        private final int mTextGravity;
        private final int mToolTipId;
        private final Rect mDrawRect;
        private final long mShowDuration;
        private final int mClosePolicy;
        private final Point mPoint;
        private final int mTextResId;
        private final int mTopRule;
        private final int mMaxWidth;
        private final boolean mHideArrow;
        private final long mActivateDelay;
        private final boolean mRestrict;
        private final long mFadeDuration;
        private final TooltipTextDrawable mDrawable;
        private final Rect mTempRect = new Rect();
        private final int[] mTempLocation = new int[2];
        private final Handler mHandler = new Handler();
        private final Rect mScreenRect = new Rect();
        private final Point mTmpPoint = new Point();
        private final Rect mHitRect = new Rect();
        private final float mTextViewElevation;
        private Callback mCallback;
        private int[] mOldLocation;
        private Gravity mGravity;
        private Animator mShowAnimation;
        private boolean mShowing;
        private WeakReference<View> mViewAnchor;
        private boolean mAttached;

        private final OnAttachStateChangeListener mAttachedStateListener = new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(final View v) {
            }

            @Override
            @TargetApi (17)
            public void onViewDetachedFromWindow(final View v) {
                removeViewListeners(v);

                if (!mAttached) {
                    return;
                }

                Activity activity = TooltipUtils.getActivity(getContext());

                if (null != activity) {
                    if (activity.isFinishing()) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
                        return;
                    }
                    onClose(false, false, true);
                }
            }
        };
        private Runnable hideRunnable = new Runnable() {
            @Override

            public void run() {
                onClose(false, false, false);
            }
        };
        private boolean mInitialized;
        private boolean mActivated;
        Runnable activateRunnable = new Runnable() {
            @Override
            public void run() {
                mActivated = true;
            }
        };
        private int mPadding;
        private CharSequence mText;
        private Rect mViewRect;
        private View mView;
        private TooltipOverlay mViewOverlay;
        private final ViewTreeObserver.OnPreDrawListener mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onPreDraw() {
                if (!mAttached) {
                    removePreDrawObserver(null);
                    return true;
                }

                if (null != mViewAnchor) {
                    View view = mViewAnchor.get();
                    if (null != view) {
                        view.getLocationOnScreen(mTempLocation);

                        if (mOldLocation == null) {
                            mOldLocation = new int[]{mTempLocation[0], mTempLocation[1]};
                        }

                        if (mOldLocation[0] != mTempLocation[0] || mOldLocation[1] != mTempLocation[1]) {
                            mView.setTranslationX(mTempLocation[0] - mOldLocation[0] + mView.getTranslationX());
                            mView.setTranslationY(mTempLocation[1] - mOldLocation[1] + mView.getTranslationY());

                            if (null != mViewOverlay) {
                                mViewOverlay.setTranslationX(mTempLocation[0] - mOldLocation[0] + mViewOverlay.getTranslationX());
                                mViewOverlay.setTranslationY(mTempLocation[1] - mOldLocation[1] + mViewOverlay.getTranslationY());
                            }

                        }

                        mOldLocation[0] = mTempLocation[0];
                        mOldLocation[1] = mTempLocation[1];
                    }
                }
                return true;
            }
        };
        private TextView mTextView;
        private int mSizeTolerance;
        private ValueAnimator mAnimator;
        private AnimationBuilder mFloatingAnimation;
        private boolean mAlreadyCheck;
        private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!mAttached) {
                            removeGlobalLayoutObserver(null);
                            return;
                        }

                        if (null != mViewAnchor) {
                            View view = mViewAnchor.get();

                            if (null != view) {
                                view.getHitRect(mTempRect);
                                view.getLocationOnScreen(mTempLocation);

                                if (!mTempRect.equals(mHitRect)) {
                                    mHitRect.set(mTempRect);

                                    mTempRect.offsetTo(mTempLocation[0], mTempLocation[1]);
                                    mViewRect.set(mTempRect);
                                    calculatePositions();
                                }
                            }
                        }
                    }
                };

        private boolean mIsCustomView;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        public TooltipViewImpl(Context context, final Builder builder) {
            super(context);

            TypedArray theme =
                    context.getTheme()
                            .obtainStyledAttributes(null, R.styleable.TooltipLayout, builder.defStyleAttr, builder.defStyleRes);
            this.mPadding = theme.getDimensionPixelSize(R.styleable.TooltipLayout_ttlm_padding, 30);
            this.mTextAppearance = theme.getResourceId(R.styleable.TooltipLayout_android_textAppearance, 0);
            this.mTextGravity = theme
                    .getInt(R.styleable.TooltipLayout_android_gravity, android.view.Gravity.TOP | android.view.Gravity.START);
            this.mTextViewElevation = theme.getDimension(R.styleable.TooltipLayout_ttlm_elevation, 0);
            int overlayStyle = theme.getResourceId(R.styleable.TooltipLayout_ttlm_overlayStyle, R.style.ToolTipOverlayDefaultStyle);

            theme.recycle();

            this.mToolTipId = builder.id;
            this.mText = builder.text;
            this.mGravity = builder.gravity;
            this.mTextResId = builder.textResId;
            this.mMaxWidth = builder.maxWidth;
            this.mTopRule = builder.actionbarSize;
            this.mClosePolicy = builder.closePolicy;
            this.mShowDuration = builder.showDuration;
            this.mShowDelay = builder.showDelay;
            this.mHideArrow = builder.hideArrow;
            this.mActivateDelay = builder.activateDelay;
            this.mRestrict = builder.restrictToScreenEdges;
            this.mFadeDuration = builder.fadeDuration;
            this.mCallback = builder.closeCallback;
            this.mFloatingAnimation = builder.floatingAnimation;
            this.mSizeTolerance = (int) (context.getResources().getDisplayMetrics().density * TOLERANCE_VALUE);

            setClipChildren(false);
            setClipToPadding(false);

            if (null != builder.point) {
                this.mPoint = new Point(builder.point);
                this.mPoint.y += mTopRule;
            } else {
                this.mPoint = null;
            }

            this.mDrawRect = new Rect();

            if (null != builder.view) {
                mViewRect = new Rect();

                builder.view.getHitRect(mHitRect);
                builder.view.getLocationOnScreen(mTempLocation);

                mViewRect.set(mHitRect);
                mViewRect.offsetTo(mTempLocation[0], mTempLocation[1]);

                mViewAnchor = new WeakReference<>(builder.view);

                if (builder.view.getViewTreeObserver().isAlive()) {
                    builder.view.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
                    builder.view.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
                    builder.view.addOnAttachStateChangeListener(mAttachedStateListener);
                }
            }

            if (builder.overlay) {
                mViewOverlay = new TooltipOverlay(getContext(), null, 0, overlayStyle);
                mViewOverlay.setAdjustViewBounds(true);
                mViewOverlay.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            }

            if (!builder.isCustomView) {
                this.mDrawable = new TooltipTextDrawable(context, builder);
            } else {
                this.mDrawable = null;
                this.mIsCustomView = true;
            }
            setVisibility(INVISIBLE);
        }

        @Override
        public void show() {
            if (getParent() == null) {
                final Activity act = TooltipUtils.getActivity(getContext());
                LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
                if (act != null) {
                    ViewGroup rootView;
                    rootView = (ViewGroup) (act.getWindow().getDecorView());
                    rootView.addView(this, params);
                }
            }
        }

        @Override
        public void hide() {
            hide(mFadeDuration);
        }

        private void hide(long fadeDuration) {

            if (!isAttached()) {
                return;
            }
            fadeOut(fadeDuration);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        protected void fadeOut(long fadeDuration) {
            if (!isAttached() || !mShowing) {
                return;
            }

            if (null != mShowAnimation) {
                mShowAnimation.cancel();
            }

            mShowing = false;

            if (fadeDuration > 0) {
                float alpha = getAlpha();
                mShowAnimation = ObjectAnimator.ofFloat(this, "alpha", alpha, 0);
                mShowAnimation.setDuration(fadeDuration);
                mShowAnimation.addListener(
                        new Animator.AnimatorListener() {
                            boolean cancelled;

                            @Override
                            public void onAnimationStart(final Animator animation) {
                                cancelled = false;
                            }

                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                if (cancelled) {
                                    return;
                                }

                                // hide completed
                                if (null != mCallback) {
                                    mCallback.onTooltipHidden(TooltipViewImpl.this);
                                }

                                remove();
                                mShowAnimation = null;
                            }

                            @Override
                            public void onAnimationCancel(final Animator animation) {
                                cancelled = true;
                            }

                            @Override
                            public void onAnimationRepeat(final Animator animation) {

                            }
                        }
                );
                mShowAnimation.start();
            } else {
                setVisibility(View.INVISIBLE);
                remove();
            }
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        void removeFromParent() {
            ViewParent parent = getParent();
            removeCallbacks();

            if (null != parent) {
                ((ViewGroup) parent).removeView(TooltipViewImpl.this);

                if (null != mShowAnimation && mShowAnimation.isStarted()) {
                    mShowAnimation.cancel();
                }
            }
        }

        private void removeCallbacks() {
            mHandler.removeCallbacks(hideRunnable);
            mHandler.removeCallbacks(activateRunnable);
        }

        @Override
        public void remove() {
            if (isAttached()) {
                removeFromParent();
            }
        }

        @Override
        public int getTooltipId() {
            return mToolTipId;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void offsetTo(final int x, final int y) {
            mView.setTranslationX(x + mDrawRect.left);
            mView.setTranslationY(y + mDrawRect.top);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void offsetBy(final int x, final int y) {
            mView.setTranslationX(x + mView.getTranslationX());
            mView.setTranslationY(y + mView.getTranslationY());
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void offsetXBy(final float x) {
            mView.setTranslationX(x + mView.getTranslationX());
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void offsetXTo(final float x) {
            mView.setTranslationX(x + mDrawRect.left);
        }

        @Override
        public void setText(@StringRes final int resId) {
            if (null != mView) {
                setText(getResources().getString(resId));
            }
        }

        @Override
        public void setTextColor(final int color) {
            if (null != mTextView) {
                mTextView.setTextColor(color);
            }
        }

        @Override
        public void setTextColor(final ColorStateList color) {
            if (null != mTextView) {
                mTextView.setTextColor(color);
            }
        }

        @Override
        public boolean isAttached() {
            return mAttached;
        }

        @SuppressWarnings ("unused")
        public boolean isShowing() {
            return mShowing;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            mAttached = true;
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            android.view.Display display = wm.getDefaultDisplay();
            display.getRectSize(mScreenRect);
            initializeView();
            showInternal();
        }

        @Override
        protected void onDetachedFromWindow() {
            removeListeners();
            stopFloatingAnimations();
            mAttached = false;
            mViewAnchor = null;
            super.onDetachedFromWindow();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onVisibilityChanged(@NonNull final View changedView, final int visibility) {
            super.onVisibilityChanged(changedView, visibility);

            if (null != mAnimator) {
                if (visibility == VISIBLE) {
                    mAnimator.start();
                } else {
                    mAnimator.cancel();
                }
            }
        }

        @Override
        protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
            if (null != mView) {
                mView.layout(mView.getLeft(), mView.getTop(), mView.getMeasuredWidth(), mView.getMeasuredHeight());
            }

            if (null != mViewOverlay) {
                mViewOverlay.layout(
                        mViewOverlay.getLeft(),
                        mViewOverlay.getTop(),
                        mViewOverlay.getMeasuredWidth(),
                        mViewOverlay.getMeasuredHeight()
                );
            }

            if (changed) {
                if (mViewAnchor != null) {
                    View view = mViewAnchor.get();
                    if (null != view) {
                        view.getHitRect(mTempRect);
                        view.getLocationOnScreen(mTempLocation);
                        mTempRect.offsetTo(mTempLocation[0], mTempLocation[1]);
                        mViewRect.set(mTempRect);
                    }
                }
                calculatePositions();
            }
        }

        private void removeListeners() {
            mCallback = null;

            if (null != mViewAnchor) {
                View view = mViewAnchor.get();
                removeViewListeners(view);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void stopFloatingAnimations() {
            if (null != mAnimator) {
                mAnimator.cancel();
                mAnimator = null;
            }
        }

        private void removeViewListeners(final View view) {
            removeGlobalLayoutObserver(view);
            removePreDrawObserver(view);
            removeOnAttachStateObserver(view);
        }

        @SuppressWarnings ("deprecation")
        private void removeGlobalLayoutObserver(@Nullable View view) {
            if (null == view && null != mViewAnchor) {
                view = mViewAnchor.get();
            }
            if (null != view && view.getViewTreeObserver().isAlive()) {
                if (Build.VERSION.SDK_INT >= 16) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
                }
            }
        }

        private void removePreDrawObserver(@Nullable View view) {
            if (null == view && null != mViewAnchor) {
                view = mViewAnchor.get();
            }
            if (null != view && view.getViewTreeObserver().isAlive()) {
                view.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        private void removeOnAttachStateObserver(@Nullable View view) {
            if (null == view && null != mViewAnchor) {
                view = mViewAnchor.get();
            }
            if (null != view) {
                view.removeOnAttachStateChangeListener(mAttachedStateListener);
            }
        }

        @SuppressWarnings ("deprecation")
        private void initializeView() {
            if (!isAttached() || mInitialized) {
                return;
            }
            mInitialized = true;


            LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            mView = LayoutInflater.from(getContext()).inflate(mTextResId, this, false);
            mView.setLayoutParams(params);

            mTextView = (TextView) mView.findViewById(android.R.id.text1);
            mTextView.setText(Html.fromHtml((String) this.mText));
            if (mMaxWidth > -1) {
                mTextView.setMaxWidth(mMaxWidth);
            }

            if (0 != mTextAppearance) {
                mTextView.setTextAppearance(getContext(), mTextAppearance);
            }

            mTextView.setGravity(mTextGravity);

            if (null != mDrawable) {
                mTextView.setBackgroundDrawable(mDrawable);
                if (mHideArrow) {
                    mTextView.setPadding(mPadding / 2, mPadding / 2, mPadding / 2, mPadding / 2);
                } else {
                    mTextView.setPadding(mPadding, mPadding, mPadding, mPadding);
                }
            }
            this.addView(mView);

            if (null != mViewOverlay) {
                this.addView(mViewOverlay);
            }

            if (!mIsCustomView && mTextViewElevation > 0 && Build.VERSION.SDK_INT >= 21) {
                setupElevation();
            }
        }

        private void showInternal() {
            if (!isAttached()) {
                return;
            }
            fadeIn(mFadeDuration);
        }

        @SuppressLint ("NewApi")
        private void setupElevation() {
            mTextView.setElevation(mTextViewElevation);
            mTextView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        protected void fadeIn(final long fadeDuration) {
            if (mShowing) {
                return;
            }

            if (null != mShowAnimation) {
                mShowAnimation.cancel();
            }


            mShowing = true;

            if (fadeDuration > 0) {
                mShowAnimation = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
                mShowAnimation.setDuration(fadeDuration);
                if (this.mShowDelay > 0) {
                    mShowAnimation.setStartDelay(this.mShowDelay);
                }
                mShowAnimation.addListener(
                        new Animator.AnimatorListener() {
                            boolean cancelled;

                            @Override
                            public void onAnimationStart(final Animator animation) {
                                setVisibility(View.VISIBLE);
                                cancelled = false;
                            }

                            @Override
                            public void onAnimationEnd(final Animator animation) {

                                if (!cancelled) {
                                    if (null != mCallback) {
                                        mCallback.onTooltipShown(TooltipViewImpl.this);
                                    }

                                    postActivate(mActivateDelay);
                                }
                            }

                            @Override
                            public void onAnimationCancel(final Animator animation) {
                                cancelled = true;
                            }

                            @Override
                            public void onAnimationRepeat(final Animator animation) {

                            }
                        }
                );
                mShowAnimation.start();
            } else {
                setVisibility(View.VISIBLE);
                //            mTooltipListener.onShowCompleted(TooltipView.this);
                if (!mActivated) {
                    postActivate(mActivateDelay);
                }
            }

            if (mShowDuration > 0) {
                mHandler.removeCallbacks(hideRunnable);
                mHandler.postDelayed(hideRunnable, mShowDuration);
            }
        }

        void postActivate(long ms) {
            if (ms > 0) {
                if (isAttached()) {
                    mHandler.postDelayed(activateRunnable, ms);
                }
            } else {
                mActivated = true;
            }
        }

        private void calculatePositions() {
            calculatePositions(mRestrict);
        }

        private void calculatePositions(boolean restrict) {
            viewGravities.clear();
            viewGravities.addAll(GRAVITY_LIST);
            viewGravities.remove(mGravity);
            viewGravities.add(0, mGravity);
            calculatePositions(viewGravities, restrict);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @SuppressWarnings ("checkstyle:cyclomaticcomplexity")
        private void calculatePositions(List<Gravity> gravities, final boolean checkEdges) {
            if (!isAttached()) {
                return;
            }

            // failed to display the tooltip due to
            // something wrong with its dimensions or
            // the target position..
            if (gravities.size() < 1) {
                if (null != mCallback) {
                    mCallback.onTooltipFailed(this);
                }
                setVisibility(View.GONE);
                return;
            }

            Gravity gravity = gravities.remove(0);

            int statusbarHeight = mScreenRect.top;

            final int overlayWidth;
            final int overlayHeight;

            if (null != mViewOverlay && gravity != CENTER) {
                int margin = mViewOverlay.getLayoutMargins();
                overlayWidth = (mViewOverlay.getWidth() / 2) + margin;
                overlayHeight = (mViewOverlay.getHeight() / 2) + margin;
            } else {
                overlayWidth = 0;
                overlayHeight = 0;
            }

            if (mViewRect == null) {
                mViewRect = new Rect();
                mViewRect.set(mPoint.x, mPoint.y + statusbarHeight, mPoint.x, mPoint.y + statusbarHeight);
            }

            final int screenTop = mScreenRect.top + mTopRule;

            int width = mView.getWidth();
            int height = mView.getHeight();

            // get the destination mPoint

            if (gravity == BOTTOM) {
                if (calculatePositionBottom(checkEdges, overlayHeight, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                    return;
                }
            } else if (gravity == TOP) {
                if (calculatePositionTop(checkEdges, overlayHeight, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                    return;
                }
            } else if (gravity == RIGHT) {
                if (calculatePositionRight(checkEdges, overlayWidth, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                    return;
                }
            } else if (gravity == LEFT) {
                if (calculatePositionLeft(checkEdges, overlayWidth, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                    return;
                }
            } else if (gravity == CENTER) {
                calculatePositionCenter(checkEdges, screenTop, width, height);
            }

            if (gravity != mGravity) {

                mGravity = gravity;

                if (gravity == CENTER && null != mViewOverlay) {
                    removeView(mViewOverlay);
                    mViewOverlay = null;
                }
            }

            if (null != mViewOverlay) {
                mViewOverlay.setTranslationX(mViewRect.centerX() - mViewOverlay.getWidth() / 2);
                mViewOverlay.setTranslationY(mViewRect.centerY() - mViewOverlay.getHeight() / 2);
            }

            // translate the text view
            mView.setTranslationX(mDrawRect.left);
            mView.setTranslationY(mDrawRect.top);

            if (null != mDrawable) {
                getAnchorPoint(gravity, mTmpPoint);
                mDrawable.setAnchor(gravity, mHideArrow ? 0 : mPadding / 2, mHideArrow ? null : mTmpPoint);
            }

            if (!mAlreadyCheck) {
                mAlreadyCheck = true;
                startFloatingAnimations();
            }
        }

        private void calculatePositionCenter(final boolean checkEdges, final int screenTop, final int width, final int height) {
            mDrawRect.set(
                    mViewRect.centerX() - width / 2,
                    mViewRect.centerY() - height / 2,
                    mViewRect.centerX() + width / 2,
                    mViewRect.centerY() + height / 2
            );

            if (checkEdges && !TooltipUtils.rectContainsRectWithTolerance(mScreenRect, mDrawRect, mSizeTolerance)) {
                if (mDrawRect.bottom > mScreenRect.bottom) {
                    mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
                } else if (mDrawRect.top < screenTop) {
                    mDrawRect.offset(0, screenTop - mDrawRect.top);
                }
                if (mDrawRect.right > mScreenRect.right) {
                    mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
                } else if (mDrawRect.left < mScreenRect.left) {
                    mDrawRect.offset(mScreenRect.left - mDrawRect.left, 0);
                }
            }
        }

        private boolean calculatePositionLeft(
                final boolean checkEdges, final int overlayWidth, final int screenTop,
                final int width, final int height) {
            mDrawRect.set(
                    mViewRect.left - width,
                    mViewRect.centerY() - height / 2,
                    mViewRect.left,
                    mViewRect.centerY() + height / 2
            );

            if ((mViewRect.width() / 2) < overlayWidth) {
                mDrawRect.offset(-(overlayWidth - (mViewRect.width() / 2)), 0);
            }

            if (checkEdges && !TooltipUtils.rectContainsRectWithTolerance(mScreenRect, mDrawRect, mSizeTolerance)) {
                if (mDrawRect.bottom > mScreenRect.bottom) {
                    mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
                } else if (mDrawRect.top < screenTop) {
                    mDrawRect.offset(0, screenTop - mDrawRect.top);
                }
                if (mDrawRect.left < mScreenRect.left) {
                    // this means there's no enough space!
                    return true;
                } else if (mDrawRect.right > mScreenRect.right) {
                    mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
                }
            }
            return false;
        }

        private boolean calculatePositionRight(
                final boolean checkEdges, final int overlayWidth, final int screenTop,
                final int width, final int height) {
            mDrawRect.set(
                    mViewRect.right,
                    mViewRect.centerY() - height / 2,
                    mViewRect.right + width,
                    mViewRect.centerY() + height / 2
            );

            if ((mViewRect.width() / 2) < overlayWidth) {
                mDrawRect.offset(overlayWidth - mViewRect.width() / 2, 0);
            }

            if (checkEdges && !TooltipUtils.rectContainsRectWithTolerance(mScreenRect, mDrawRect, mSizeTolerance)) {
                if (mDrawRect.bottom > mScreenRect.bottom) {
                    mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
                } else if (mDrawRect.top < screenTop) {
                    mDrawRect.offset(0, screenTop - mDrawRect.top);
                }
                if (mDrawRect.right > mScreenRect.right) {
                    // this means there's no enough space!
                    return true;
                } else if (mDrawRect.left < mScreenRect.left) {
                    mDrawRect.offset(mScreenRect.left - mDrawRect.left, 0);
                }
            }
            return false;
        }

        private boolean calculatePositionTop(
                final boolean checkEdges, final int overlayHeight, final int screenTop,
                final int width, final int height) {
            mDrawRect.set(
                    mViewRect.centerX() - width / 2,
                    mViewRect.top - height,
                    mViewRect.centerX() + width / 2,
                    mViewRect.top
            );

            if ((mViewRect.height() / 2) < overlayHeight) {
                mDrawRect.offset(0, -(overlayHeight - (mViewRect.height() / 2)));
            }

            if (checkEdges && !TooltipUtils.rectContainsRectWithTolerance(mScreenRect, mDrawRect, mSizeTolerance)) {
                if (mDrawRect.right > mScreenRect.right) {
                    mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
                } else if (mDrawRect.left < mScreenRect.left) {
                    mDrawRect.offset(-mDrawRect.left, 0);
                }
                if (mDrawRect.top < screenTop) {
                    // this means there's no enough space!
                    return true;
                } else if (mDrawRect.bottom > mScreenRect.bottom) {
                    mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
                }
            }
            return false;
        }

        private boolean calculatePositionBottom(
                final boolean checkEdges, final int overlayHeight, final int screenTop,
                final int width, final int height) {
            mDrawRect.set(
                    mViewRect.centerX() - width / 2,
                    mViewRect.bottom,
                    mViewRect.centerX() + width / 2,
                    mViewRect.bottom + height
            );

            if (mViewRect.height() / 2 < overlayHeight) {
                mDrawRect.offset(0, overlayHeight - mViewRect.height() / 2);
            }

            if (checkEdges && !TooltipUtils.rectContainsRectWithTolerance(mScreenRect, mDrawRect, mSizeTolerance)) {
                if (mDrawRect.right > mScreenRect.right) {
                    mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
                } else if (mDrawRect.left < mScreenRect.left) {
                    mDrawRect.offset(-mDrawRect.left, 0);
                }
                if (mDrawRect.bottom > mScreenRect.bottom) {
                    // this means there's no enough space!
                    return true;
                } else if (mDrawRect.top < screenTop) {
                    mDrawRect.offset(0, screenTop - mDrawRect.top);
                }
            }
            return false;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void startFloatingAnimations() {
            if (mTextView == mView || null == mFloatingAnimation) {
                return;
            }

            final float endValue = mFloatingAnimation.radius;
            final long duration = mFloatingAnimation.duration;

            final int direction;

            if (mFloatingAnimation.direction == 0) {
                direction = mGravity == TOP || mGravity == BOTTOM ? 2 : 1;
            } else {
                direction = mFloatingAnimation.direction;
            }

            final String property = direction == 2 ? "translationY" : "translationX";
            mAnimator = ObjectAnimator.ofFloat(mTextView, property, -endValue, endValue);
            mAnimator.setDuration(duration);
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.setRepeatMode(ValueAnimator.REVERSE);

            mAnimator.start();
        }

        void getAnchorPoint(final Gravity gravity, Point outPoint) {

            if (gravity == BOTTOM) {
                outPoint.x = mViewRect.centerX();
                outPoint.y = mViewRect.bottom;
            } else if (gravity == TOP) {
                outPoint.x = mViewRect.centerX();
                outPoint.y = mViewRect.top;
            } else if (gravity == RIGHT) {
                outPoint.x = mViewRect.right;
                outPoint.y = mViewRect.centerY();
            } else if (gravity == LEFT) {
                outPoint.x = mViewRect.left;
                outPoint.y = mViewRect.centerY();
            } else if (this.mGravity == CENTER) {
                outPoint.x = mViewRect.centerX();
                outPoint.y = mViewRect.centerY();
            }

            outPoint.x -= mDrawRect.left;
            outPoint.y -= mDrawRect.top;

            if (!mHideArrow) {
                if (gravity == LEFT || gravity == RIGHT) {
                    outPoint.y -= mPadding / 2;
                } else if (gravity == TOP || gravity == BOTTOM) {
                    outPoint.x -= mPadding / 2;
                }
            }
        }

        @Override
        public void setText(final CharSequence text) {
            this.mText = text;
            if (null != mTextView) {
                mTextView.setText(Html.fromHtml((String) text));
            }
        }

        @SuppressWarnings ("checkstyle:cyclomaticcomplexity")
        @Override
        public boolean onTouchEvent(@NonNull final MotionEvent event) {
            if (!mAttached || !mShowing || !isShown() || mClosePolicy == ClosePolicy.NONE) {
                return false;
            }

            final int action = event.getActionMasked();

            if (!mActivated && mActivateDelay > 0) {
                //onClose(true, false, true);
                return false;
            }

            if (action == MotionEvent.ACTION_DOWN) {

                Rect outRect = new Rect();
                mView.getGlobalVisibleRect(outRect);

                boolean containsTouch = outRect.contains((int) event.getX(), (int) event.getY());

                if (null != mViewOverlay) {
                    mViewOverlay.getGlobalVisibleRect(outRect);
                    containsTouch |= outRect.contains((int) event.getX(), (int) event.getY());
                }

                if (containsTouch) {
                    if (ClosePolicy.touchInside(mClosePolicy)) {
                        onClose(true, true, false);
                    }
                    return ClosePolicy.consumeInside(mClosePolicy);
                }

                if (ClosePolicy.touchOutside(mClosePolicy)) {
                    onClose(true, false, false);
                }
                return ClosePolicy.consumeOutside(mClosePolicy);

            }
            return false;
        }

        @Override
        protected void onDraw(final Canvas canvas) {
            if (!mAttached) {
                return;
            }
            super.onDraw(canvas);
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int myWidth = 0;
            int myHeight = 0;

            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            // Record our dimensions if they are known;
            if (widthMode != MeasureSpec.UNSPECIFIED) {
                myWidth = widthSize;
            }

            if (heightMode != MeasureSpec.UNSPECIFIED) {
                myHeight = heightSize;
            }

            if (null != mView) {
                if (mView.getVisibility() != GONE) {
                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(myWidth, MeasureSpec.AT_MOST);
                    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(myHeight, MeasureSpec.AT_MOST);
                    mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                } else {
                    myWidth = 0;
                    myHeight = 0;
                }
            }

            if (null != mViewOverlay && mViewOverlay.getVisibility() != GONE) {

                final int childWidthMeasureSpec;
                final int childHeightMeasureSpec;
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
                mViewOverlay.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            }

            setMeasuredDimension(myWidth, myHeight);
        }

        private void onClose(boolean fromUser, boolean containsTouch, boolean immediate) {

            if (!isAttached()) {
                return;
            }

            if (null != mCallback) {
                mCallback.onTooltipClose(this, fromUser, containsTouch);
            }

            hide(immediate ? 0 : mFadeDuration);
        }
    }

    public static final class AnimationBuilder {
        int radius;
        int direction;
        long duration;
        boolean completed;
        @SuppressWarnings ("unused")
        public static final AnimationBuilder DEFAULT = new AnimationBuilder().build();
        @SuppressWarnings ("unused")
        public static final AnimationBuilder SLOW = new AnimationBuilder().setDuration(600).setRadius(4).build();

        public AnimationBuilder() {
            radius = 8;
            direction = 0;
            duration = 400;
        }

        public AnimationBuilder setRadius(int value) {
            throwIfCompleted();
            this.radius = value;
            return this;
        }

        private void throwIfCompleted() {
            if (completed) {
                throw new IllegalStateException("Builder cannot be modified");
            }
        }

        /**
         * @param value 0 for auto, 1 horizontal, 2 vertical
         */
        @SuppressWarnings ("unused")
        public AnimationBuilder setDirection(int value) {
            throwIfCompleted();
            this.direction = value;
            return this;
        }

        public AnimationBuilder setDuration(long value) {
            throwIfCompleted();
            this.duration = value;
            return this;
        }

        public AnimationBuilder build() {
            throwIfCompleted();
            completed = true;
            return this;
        }
    }

    public static final class Builder {
        private static int sNextId = 0;
        int id;
        CharSequence text;
        View view;
        Gravity gravity;
        int actionbarSize = 0;
        int textResId = R.layout.tooltip_textview;
        int closePolicy = ClosePolicy.NONE;
        long showDuration;
        Point point;
        long showDelay = 0;
        boolean hideArrow;
        int maxWidth = -1;
        int defStyleRes = R.style.ToolTipLayoutDefaultStyle;
        int defStyleAttr = R.attr.ttlm_defaultStyle;
        long activateDelay = 0;
        boolean isCustomView;
        boolean restrictToScreenEdges = true;
        long fadeDuration = 200;
        Callback closeCallback;
        boolean completed;
        boolean overlay = true;
        AnimationBuilder floatingAnimation;
        Typeface typeface;

        public Builder(int id) {
            this.id = id;
        }

        @SuppressWarnings ("unused")
        public Builder() {
            this.id = sNextId++;
        }

        @SuppressWarnings ("unused")
        public Builder withCustomView(int resId) {
            throwIfCompleted();
            return withCustomView(resId, true);
        }

        private void throwIfCompleted() {
            if (completed) {
                throw new IllegalStateException("Builder cannot be modified");
            }
        }

        /**
         * Use a custom View for the tooltip. Note that the custom view
         * must include a TextView which id is `@android:id/text1`.<br />
         * Moreover, when using a custom view, the anchor arrow will not be shown
         *
         * @param resId             the custom layout view.
         * @param replaceBackground if true the custom view's background won't be replaced
         * @return the builder for chaining.
         */
        public Builder withCustomView(int resId, boolean replaceBackground) {
            this.textResId = resId;
            this.isCustomView = replaceBackground;
            return this;
        }

        @SuppressWarnings ("unused")
        public Builder withStyleId(int styleId) {
            throwIfCompleted();
            this.defStyleAttr = 0;
            this.defStyleRes = styleId;
            return this;
        }

        public Builder fitToScreen(boolean value) {
            throwIfCompleted();
            restrictToScreenEdges = value;
            return this;
        }

        public Builder fadeDuration(long ms) {
            throwIfCompleted();
            fadeDuration = ms;
            return this;
        }

        public Builder withCallback(Callback callback) {
            throwIfCompleted();
            this.closeCallback = callback;
            return this;
        }

        public Builder text(Resources res, @StringRes int resId) {
            return text(res.getString(resId));
        }

        public Builder text(CharSequence text) {
            throwIfCompleted();
            this.text = text;
            return this;
        }

        @SuppressWarnings ("unused")
        public Builder typeface(Typeface typeface) {
            throwIfCompleted();
            this.typeface = typeface;
            return this;
        }

        @SuppressWarnings ("unused")
        public Builder maxWidth(Resources res, @DimenRes int dimension) {
            return maxWidth(res.getDimensionPixelSize(dimension));
        }

        public Builder maxWidth(int maxWidth) {
            throwIfCompleted();
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder floatingAnimation(AnimationBuilder builder) {
            throwIfCompleted();
            this.floatingAnimation = builder;
            return this;
        }

        /**
         * Enable/disable the default overlay view
         *
         * @param value false to disable the overlay view. True by default
         */
        public Builder withOverlay(boolean value) {
            throwIfCompleted();
            this.overlay = value;
            return this;
        }

        public Builder anchor(View view, Gravity gravity) {
            throwIfCompleted();
            this.point = null;
            this.view = view;
            this.gravity = gravity;
            return this;
        }

        @SuppressWarnings ("unused")
        public Builder anchor(final Point point, final Gravity gravity) {
            throwIfCompleted();
            this.view = null;
            this.point = new Point(point);
            this.gravity = gravity;
            return this;
        }

        /**
         * @deprecated use {#withArrow} instead
         */
        @Deprecated
        public Builder toggleArrow(boolean show) {
            return withArrow(show);
        }

        /**
         * Hide/Show the tooltip arrow (trueby default)
         *
         * @param show true to show the arrow, false to hide it
         * @return the builder for chaining.
         */
        public Builder withArrow(boolean show) {
            throwIfCompleted();
            this.hideArrow = !show;
            return this;
        }

        public Builder actionBarSize(Resources resources, int resId) {
            return actionBarSize(resources.getDimensionPixelSize(resId));
        }

        public Builder actionBarSize(final int actionBarSize) {
            throwIfCompleted();
            this.actionbarSize = actionBarSize;
            return this;
        }

        public Builder closePolicy(ClosePolicy policy, long milliseconds) {
            throwIfCompleted();
            this.closePolicy = policy.build();
            this.showDuration = milliseconds;
            return this;
        }

        public Builder activateDelay(long ms) {
            throwIfCompleted();
            this.activateDelay = ms;
            return this;
        }

        public Builder showDelay(long ms) {
            throwIfCompleted();
            this.showDelay = ms;
            return this;
        }

        public Builder build() {
            throwIfCompleted();
            if (floatingAnimation != null) {
                if (!floatingAnimation.completed) {
                    throw new IllegalStateException("Builder not closed");
                }
            }
            completed = true;
            overlay = overlay && gravity != CENTER;
            return this;
        }
    }
}