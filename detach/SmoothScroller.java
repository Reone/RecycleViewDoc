public abstract static class SmoothScroller {
        private int mTargetPosition = RecyclerView.NO_POSITION;
        private RecyclerView mRecyclerView;
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private boolean mRunning;
        private View mTargetView;
        private final Action mRecyclingAction;
        private boolean mStarted;
        public SmoothScroller() {
            mRecyclingAction = new Action(0, 0);
        }
        void start(RecyclerView recyclerView, LayoutManager layoutManager) {
            recyclerView.mViewFlinger.stop();

            if (mStarted) {
                Log.w(TAG, "An instance of " + this.getClass().getSimpleName() + " was started "
                        + "more than once. Each instance of" + this.getClass().getSimpleName() + " "
                        + "is intended to only be used once. You should create a new instance for "
                        + "each use.");
            }

            mRecyclerView = recyclerView;
            mLayoutManager = layoutManager;
            if (mTargetPosition == RecyclerView.NO_POSITION) {
                throw new IllegalArgumentException("Invalid target position");
            }
            mRecyclerView.mState.mTargetPosition = mTargetPosition;
            mRunning = true;
            mPendingInitialRun = true;
            mTargetView = findViewByPosition(getTargetPosition());
            onStart();
            mRecyclerView.mViewFlinger.postOnAnimation();

            mStarted = true;
        }

        public void setTargetPosition(int targetPosition) {
            mTargetPosition = targetPosition;
        }
        @Nullable
        public PointF computeScrollVectorForPosition(int targetPosition) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof ScrollVectorProvider) {
                return ((ScrollVectorProvider) layoutManager)
                        .computeScrollVectorForPosition(targetPosition);
            }
            Log.w(TAG, "You should override computeScrollVectorForPosition when the LayoutManager"
                    + " does not implement " + ScrollVectorProvider.class.getCanonicalName());
            return null;
        }
        @Nullable
        public LayoutManager getLayoutManager() {
            return mLayoutManager;
        }
        protected final void stop() {
            if (!mRunning) {
                return;
            }
            mRunning = false;
            onStop();
            mRecyclerView.mState.mTargetPosition = RecyclerView.NO_POSITION;
            mTargetView = null;
            mTargetPosition = RecyclerView.NO_POSITION;
            mPendingInitialRun = false;
            mLayoutManager.onSmoothScrollerStopped(this);
            mLayoutManager = null;
            mRecyclerView = null;
        }
        public boolean isPendingInitialRun() {
            return mPendingInitialRun;
        }
        public boolean isRunning() {
            return mRunning;
        }
        public int getTargetPosition() {
            return mTargetPosition;
        }

        void onAnimation(int dx, int dy) {
            final RecyclerView recyclerView = mRecyclerView;
            if (mTargetPosition == RecyclerView.NO_POSITION || recyclerView == null) {
                stop();
            }
            if (mPendingInitialRun && mTargetView == null && mLayoutManager != null) {
                PointF pointF = computeScrollVectorForPosition(mTargetPosition);
                if (pointF != null && (pointF.x != 0 || pointF.y != 0)) {
                    recyclerView.scrollStep(
                            (int) Math.signum(pointF.x),
                            (int) Math.signum(pointF.y),
                            null);
                }
            }
            mPendingInitialRun = false;
            if (mTargetView != null) {
                if (getChildPosition(mTargetView) == mTargetPosition) {
                    onTargetFound(mTargetView, recyclerView.mState, mRecyclingAction);
                    mRecyclingAction.runIfNecessary(recyclerView);
                    stop();
                } else {
                    Log.e(TAG, "Passed over target position while smooth scrolling.");
                    mTargetView = null;
                }
            }
            if (mRunning) {
                onSeekTargetStep(dx, dy, recyclerView.mState, mRecyclingAction);
                boolean hadJumpTarget = mRecyclingAction.hasJumpTarget();
                mRecyclingAction.runIfNecessary(recyclerView);
                if (hadJumpTarget) {
                    if (mRunning) {
                        mPendingInitialRun = true;
                        recyclerView.mViewFlinger.postOnAnimation();
                    }
                }
            }
        }
        public int getChildPosition(View view) {
            return mRecyclerView.getChildLayoutPosition(view);
        }
        public int getChildCount() {
            return mRecyclerView.mLayout.getChildCount();
        }
        public View findViewByPosition(int position) {
            return mRecyclerView.mLayout.findViewByPosition(position);
        }
        @Deprecated
        public void instantScrollToPosition(int position) {
            mRecyclerView.scrollToPosition(position);
        }

        protected void onChildAttachedToWindow(View child) {
            if (getChildPosition(child) == getTargetPosition()) {
                mTargetView = child;
                if (DEBUG) {
                    Log.d(TAG, "smooth scroll target view has been attached");
                }
            }
        }
        protected void normalize(@NonNull PointF scrollVector) {
            final float magnitude = (float) Math.sqrt(scrollVector.x * scrollVector.x
                    + scrollVector.y * scrollVector.y);
            scrollVector.x /= magnitude;
            scrollVector.y /= magnitude;
        }
        protected abstract void onStart();
        protected abstract void onStop();
        protected abstract void onSeekTargetStep(@Px int dx, @Px int dy, @NonNull State state,
                @NonNull Action action);
        protected abstract void onTargetFound(@NonNull View targetView, @NonNull State state,
                @NonNull Action action);
        public interface ScrollVectorProvider {
            @Nullable
            PointF computeScrollVectorForPosition(int targetPosition);
        }
    }