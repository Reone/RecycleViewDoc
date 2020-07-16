public static class State {
        static final int STEP_START = 1;
        static final int STEP_LAYOUT = 1 << 1;
        static final int STEP_ANIMATIONS = 1 << 2;

        void assertLayoutStep(int accepted) {
            if ((accepted & mLayoutStep) == 0) {
                throw new IllegalStateException("Layout state should be one of "
                        + Integer.toBinaryString(accepted) + " but it is "
                        + Integer.toBinaryString(mLayoutStep));
            }
        }
        int mTargetPosition = RecyclerView.NO_POSITION;
        private SparseArray<Object> mData;
        int mPreviousLayoutItemCount = 0;
        int mDeletedInvisibleItemCountSincePreviousLayout = 0;
        @IntDef(flag = true, value = {
                STEP_START, STEP_LAYOUT, STEP_ANIMATIONS
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface LayoutState {}
        @LayoutState
        int mLayoutStep = STEP_START;
        int mItemCount = 0;
        boolean mStructureChanged = false;
        boolean mInPreLayout = false;
        boolean mTrackOldChangeHolders = false;
        boolean mIsMeasuring = false;
        boolean mRunSimpleAnimations = false;
        boolean mRunPredictiveAnimations = false;
        int mFocusedItemPosition;
        long mFocusedItemId;
        int mFocusedSubChildId;
        int mRemainingScrollHorizontal;
        int mRemainingScrollVertical;
        void prepareForNestedPrefetch(Adapter adapter) {
            mLayoutStep = STEP_START;
            mItemCount = adapter.getItemCount();
            mInPreLayout = false;
            mTrackOldChangeHolders = false;
            mIsMeasuring = false;
        }
        public boolean isMeasuring() {
            return mIsMeasuring;
        }
        public boolean isPreLayout() {
            return mInPreLayout;
        }
        public boolean willRunPredictiveAnimations() {
            return mRunPredictiveAnimations;
        }
        public boolean willRunSimpleAnimations() {
            return mRunSimpleAnimations;
        }
        public void remove(int resourceId) {
            if (mData == null) {
                return;
            }
            mData.remove(resourceId);
        }
        @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
        public <T> T get(int resourceId) {
            if (mData == null) {
                return null;
            }
            return (T) mData.get(resourceId);
        }
        public void put(int resourceId, Object data) {
            if (mData == null) {
                mData = new SparseArray<Object>();
            }
            mData.put(resourceId, data);
        }
        public int getTargetScrollPosition() {
            return mTargetPosition;
        }
        public boolean hasTargetScrollPosition() {
            return mTargetPosition != RecyclerView.NO_POSITION;
        }
        public boolean didStructureChange() {
            return mStructureChanged;
        }
        public int getItemCount() {
            return mInPreLayout
                    ? (mPreviousLayoutItemCount - mDeletedInvisibleItemCountSincePreviousLayout)
                    : mItemCount;
        }
        public int getRemainingScrollHorizontal() {
            return mRemainingScrollHorizontal;
        }
        public int getRemainingScrollVertical() {
            return mRemainingScrollVertical;
        }

        @Override
        public String toString() {
            return "State{"
                    + "mTargetPosition=" + mTargetPosition
                    + ", mData=" + mData
                    + ", mItemCount=" + mItemCount
                    + ", mIsMeasuring=" + mIsMeasuring
                    + ", mPreviousLayoutItemCount=" + mPreviousLayoutItemCount
                    + ", mDeletedInvisibleItemCountSincePreviousLayout="
                    + mDeletedInvisibleItemCountSincePreviousLayout
                    + ", mStructureChanged=" + mStructureChanged
                    + ", mInPreLayout=" + mInPreLayout
                    + ", mRunSimpleAnimations=" + mRunSimpleAnimations
                    + ", mRunPredictiveAnimations=" + mRunPredictiveAnimations
                    + '}';
        }
    }