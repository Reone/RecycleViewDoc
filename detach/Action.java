public static class Action {
            public static final int UNDEFINED_DURATION = RecyclerView.UNDEFINED_DURATION;
            private int mDx;
            private int mDy;
            private int mDuration;
            private int mJumpToPosition = NO_POSITION;
            private Interpolator mInterpolator;
            private boolean mChanged = false;
            private int mConsecutiveUpdates = 0;
            public Action(@Px int dx, @Px int dy) {
                this(dx, dy, UNDEFINED_DURATION, null);
            }
            public Action(@Px int dx, @Px int dy, int duration) {
                this(dx, dy, duration, null);
            }
            public Action(@Px int dx, @Px int dy, int duration,
                    @Nullable Interpolator interpolator) {
                mDx = dx;
                mDy = dy;
                mDuration = duration;
                mInterpolator = interpolator;
            }
            public void jumpTo(int targetPosition) {
                mJumpToPosition = targetPosition;
            }
            boolean hasJumpTarget() {
                return mJumpToPosition >= 0;
            }
            void runIfNecessary(RecyclerView recyclerView) {
                if (mJumpToPosition >= 0) {
                    final int position = mJumpToPosition;
                    mJumpToPosition = NO_POSITION;
                    recyclerView.jumpToPositionForSmoothScroller(position);
                    mChanged = false;
                    return;
                }
                if (mChanged) {
                    validate();
                    recyclerView.mViewFlinger.smoothScrollBy(mDx, mDy, mDuration, mInterpolator);
                    mConsecutiveUpdates++;
                    if (mConsecutiveUpdates > 10) {
                        Log.e(TAG, "Smooth Scroll action is being updated too frequently. Make sure"
                                + " you are not changing it unless necessary");
                    }
                    mChanged = false;
                } else {
                    mConsecutiveUpdates = 0;
                }
            }
            private void validate() {
                if (mInterpolator != null && mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must"
                            + " set a positive duration");
                } else if (mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }
            @Px
            public int getDx() {
                return mDx;
            }
            public void setDx(@Px int dx) {
                mChanged = true;
                mDx = dx;
            }
            @Px
            public int getDy() {
                return mDy;
            }
            public void setDy(@Px int dy) {
                mChanged = true;
                mDy = dy;
            }
            public int getDuration() {
                return mDuration;
            }
            public void setDuration(int duration) {
                mChanged = true;
                mDuration = duration;
            }
            @Nullable
            public Interpolator getInterpolator() {
                return mInterpolator;
            }
            public void setInterpolator(@Nullable Interpolator interpolator) {
                mChanged = true;
                mInterpolator = interpolator;
            }
            public void update(@Px int dx, @Px int dy, int duration,
                    @Nullable Interpolator interpolator) {
                mDx = dx;
                mDy = dy;
                mDuration = duration;
                mInterpolator = interpolator;
                mChanged = true;
            }
        }