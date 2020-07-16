class ViewFlinger implements Runnable {
        private int mLastFlingX;
        private int mLastFlingY;
        OverScroller mOverScroller;
        Interpolator mInterpolator = sQuinticInterpolator;
        private boolean mEatRunOnAnimationRequest = false;
        private boolean mReSchedulePostAnimationCallback = false;
        ViewFlinger() {
            mOverScroller = new OverScroller(getContext(), sQuinticInterpolator);
        }
        @Override
        public void run() {
            if (mLayout == null) {
                stop();
                return; // no layout, cannot scroll.
            }
            mReSchedulePostAnimationCallback = false;
            mEatRunOnAnimationRequest = true;
            consumePendingUpdateOperations();
            final OverScroller scroller = mOverScroller;
            if (scroller.computeScrollOffset()) {
                final int x = scroller.getCurrX();
                final int y = scroller.getCurrY();
                int unconsumedX = x - mLastFlingX;
                int unconsumedY = y - mLastFlingY;
                mLastFlingX = x;
                mLastFlingY = y;
                int consumedX = 0;
                int consumedY = 0;
                mReusableIntPair[0] = 0;
                mReusableIntPair[1] = 0;
                if (dispatchNestedPreScroll(unconsumedX, unconsumedY, mReusableIntPair, null,
                        TYPE_NON_TOUCH)) {
                    unconsumedX -= mReusableIntPair[0];
                    unconsumedY -= mReusableIntPair[1];
                }
                if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
                    considerReleasingGlowsOnScroll(unconsumedX, unconsumedY);
                }
                if (mAdapter != null) {
                    mReusableIntPair[0] = 0;
                    mReusableIntPair[1] = 0;
                    scrollStep(unconsumedX, unconsumedY, mReusableIntPair);
                    consumedX = mReusableIntPair[0];
                    consumedY = mReusableIntPair[1];
                    unconsumedX -= consumedX;
                    unconsumedY -= consumedY;
                    SmoothScroller smoothScroller = mLayout.mSmoothScroller;
                    if (smoothScroller != null && !smoothScroller.isPendingInitialRun()
                            && smoothScroller.isRunning()) {
                        final int adapterSize = mState.getItemCount();
                        if (adapterSize == 0) {
                            smoothScroller.stop();
                        } else if (smoothScroller.getTargetPosition() >= adapterSize) {
                            smoothScroller.setTargetPosition(adapterSize - 1);
                            smoothScroller.onAnimation(consumedX, consumedY);
                        } else {
                            smoothScroller.onAnimation(consumedX, consumedY);
                        }
                    }
                }
                if (!mItemDecorations.isEmpty()) {
                    invalidate();
                }
                mReusableIntPair[0] = 0;
                mReusableIntPair[1] = 0;
                dispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, null,
                        TYPE_NON_TOUCH, mReusableIntPair);
                unconsumedX -= mReusableIntPair[0];
                unconsumedY -= mReusableIntPair[1];
                if (consumedX != 0 || consumedY != 0) {
                    dispatchOnScrolled(consumedX, consumedY);
                }
                if (!awakenScrollBars()) {
                    invalidate();
                }
                boolean scrollerFinishedX = scroller.getCurrX() == scroller.getFinalX();
                boolean scrollerFinishedY = scroller.getCurrY() == scroller.getFinalY();
                final boolean doneScrolling = scroller.isFinished()
                        || ((scrollerFinishedX || unconsumedX != 0)
                        && (scrollerFinishedY || unconsumedY != 0));
                SmoothScroller smoothScroller = mLayout.mSmoothScroller;
                boolean smoothScrollerPending =
                        smoothScroller != null && smoothScroller.isPendingInitialRun();
                if (!smoothScrollerPending && doneScrolling) {
                    if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
                        final int vel = (int) scroller.getCurrVelocity();
                        int velX = unconsumedX < 0 ? -vel : unconsumedX > 0 ? vel : 0;
                        int velY = unconsumedY < 0 ? -vel : unconsumedY > 0 ? vel : 0;
                        absorbGlows(velX, velY);
                    }

                    if (ALLOW_THREAD_GAP_WORK) {
                        mPrefetchRegistry.clearPrefetchPositions();
                    }
                } else {
                    postOnAnimation();
                    if (mGapWorker != null) {
                        mGapWorker.postFromTraversal(RecyclerView.this, consumedX, consumedY);
                    }
                }
            }
            SmoothScroller smoothScroller = mLayout.mSmoothScroller;
            if (smoothScroller != null && smoothScroller.isPendingInitialRun()) {
                smoothScroller.onAnimation(0, 0);
            }
            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                internalPostOnAnimation();
            } else {
                setScrollState(SCROLL_STATE_IDLE);
                stopNestedScroll(TYPE_NON_TOUCH);
            }
        }
        void postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true;
            } else {
                internalPostOnAnimation();
            }
        }
        private void internalPostOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(RecyclerView.this, this);
        }
        public void fling(int velocityX, int velocityY) {
            setScrollState(SCROLL_STATE_SETTLING);
            mLastFlingX = mLastFlingY = 0;
            if (mInterpolator != sQuinticInterpolator) {
                mInterpolator = sQuinticInterpolator;
                mOverScroller = new OverScroller(getContext(), sQuinticInterpolator);
            }
            mOverScroller.fling(0, 0, velocityX, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }
        public void smoothScrollBy(int dx, int dy, int duration,
                @Nullable Interpolator interpolator) {
            if (duration == UNDEFINED_DURATION) {
                duration = computeScrollDuration(dx, dy, 0, 0);
            }
            if (interpolator == null) {
                interpolator = sQuinticInterpolator;
            }
            if (mInterpolator != interpolator) {
                mInterpolator = interpolator;
                mOverScroller = new OverScroller(getContext(), interpolator);
            }
            mLastFlingX = mLastFlingY = 0;
            setScrollState(SCROLL_STATE_SETTLING);
            mOverScroller.startScroll(0, 0, dx, dy, duration);
            if (Build.VERSION.SDK_INT < 23) {
                mOverScroller.computeScrollOffset();
            }
            postOnAnimation();
        }
        private float distanceInfluenceForSnapDuration(float f) {
            f -= 0.5f; // center the values about 0.
            f *= 0.3f * (float) Math.PI / 2.0f;
            return (float) Math.sin(f);
        }
        private int computeScrollDuration(int dx, int dy, int vx, int vy) {
            final int absDx = Math.abs(dx);
            final int absDy = Math.abs(dy);
            final boolean horizontal = absDx > absDy;
            final int velocity = (int) Math.sqrt(vx * vx + vy * vy);
            final int delta = (int) Math.sqrt(dx * dx + dy * dy);
            final int containerSize = horizontal ? getWidth() : getHeight();
            final int halfContainerSize = containerSize / 2;
            final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
            final float distance = halfContainerSize + halfContainerSize
                    * distanceInfluenceForSnapDuration(distanceRatio);

            final int duration;
            if (velocity > 0) {
                duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
            } else {
                float absDelta = (float) (horizontal ? absDx : absDy);
                duration = (int) (((absDelta / containerSize) + 1) * 300);
            }
            return Math.min(duration, MAX_SCROLL_DURATION);
        }
        public void stop() {
            removeCallbacks(this);
            mOverScroller.abortAnimation();
        }
    }