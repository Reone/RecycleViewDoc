public abstract static class LayoutManager {
        ChildHelper mChildHelper;
        RecyclerView mRecyclerView;
        private final ViewBoundsCheck.Callback mHorizontalBoundCheckCallback =
                new ViewBoundsCheck.Callback() {
                    @Override
                    public View getChildAt(int index) {
                        return LayoutManager.this.getChildAt(index);
                    }

                    @Override
                    public int getParentStart() {
                        return LayoutManager.this.getPaddingLeft();
                    }

                    @Override
                    public int getParentEnd() {
                        return LayoutManager.this.getWidth() - LayoutManager.this.getPaddingRight();
                    }

                    @Override
                    public int getChildStart(View view) {
                        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                                view.getLayoutParams();
                        return LayoutManager.this.getDecoratedLeft(view) - params.leftMargin;
                    }

                    @Override
                    public int getChildEnd(View view) {
                        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                                view.getLayoutParams();
                        return LayoutManager.this.getDecoratedRight(view) + params.rightMargin;
                    }
                };
        private final ViewBoundsCheck.Callback mVerticalBoundCheckCallback =
                new ViewBoundsCheck.Callback() {
                    @Override
                    public View getChildAt(int index) {
                        return LayoutManager.this.getChildAt(index);
                    }

                    @Override
                    public int getParentStart() {
                        return LayoutManager.this.getPaddingTop();
                    }

                    @Override
                    public int getParentEnd() {
                        return LayoutManager.this.getHeight()
                                - LayoutManager.this.getPaddingBottom();
                    }

                    @Override
                    public int getChildStart(View view) {
                        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                                view.getLayoutParams();
                        return LayoutManager.this.getDecoratedTop(view) - params.topMargin;
                    }

                    @Override
                    public int getChildEnd(View view) {
                        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                                view.getLayoutParams();
                        return LayoutManager.this.getDecoratedBottom(view) + params.bottomMargin;
                    }
                };
        ViewBoundsCheck mHorizontalBoundCheck = new ViewBoundsCheck(mHorizontalBoundCheckCallback);
        ViewBoundsCheck mVerticalBoundCheck = new ViewBoundsCheck(mVerticalBoundCheckCallback);
        @Nullable
        SmoothScroller mSmoothScroller;
        boolean mRequestedSimpleAnimations = false;
        boolean mIsAttachedToWindow = false;
        boolean mAutoMeasure = false;
        private boolean mMeasurementCacheEnabled = true;
        private boolean mItemPrefetchEnabled = true;
        int mPrefetchMaxCountObserved;
        boolean mPrefetchMaxObservedInInitialPrefetch;
        private int mWidthMode, mHeightMode;
        private int mWidth, mHeight;
        public interface LayoutPrefetchRegistry {
            void addPosition(int layoutPosition, int pixelDistance);
        }
        void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                mRecyclerView = null;
                mChildHelper = null;
                mWidth = 0;
                mHeight = 0;
            } else {
                mRecyclerView = recyclerView;
                mChildHelper = recyclerView.mChildHelper;
                mWidth = recyclerView.getWidth();
                mHeight = recyclerView.getHeight();
            }
            mWidthMode = MeasureSpec.EXACTLY;
            mHeightMode = MeasureSpec.EXACTLY;
        }
        void setMeasureSpecs(int wSpec, int hSpec) {
            mWidth = MeasureSpec.getSize(wSpec);
            mWidthMode = MeasureSpec.getMode(wSpec);
            if (mWidthMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mWidth = 0;
            }

            mHeight = MeasureSpec.getSize(hSpec);
            mHeightMode = MeasureSpec.getMode(hSpec);
            if (mHeightMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mHeight = 0;
            }
        }
        void setMeasuredDimensionFromChildren(int widthSpec, int heightSpec) {
            final int count = getChildCount();
            if (count == 0) {
                mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
                return;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                final Rect bounds = mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(child, bounds);
                if (bounds.left < minX) {
                    minX = bounds.left;
                }
                if (bounds.right > maxX) {
                    maxX = bounds.right;
                }
                if (bounds.top < minY) {
                    minY = bounds.top;
                }
                if (bounds.bottom > maxY) {
                    maxY = bounds.bottom;
                }
            }
            mRecyclerView.mTempRect.set(minX, minY, maxX, maxY);
            setMeasuredDimension(mRecyclerView.mTempRect, widthSpec, heightSpec);
        }
        public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
            int usedWidth = childrenBounds.width() + getPaddingLeft() + getPaddingRight();
            int usedHeight = childrenBounds.height() + getPaddingTop() + getPaddingBottom();
            int width = chooseSize(wSpec, usedWidth, getMinimumWidth());
            int height = chooseSize(hSpec, usedHeight, getMinimumHeight());
            setMeasuredDimension(width, height);
        }
        public void requestLayout() {
            if (mRecyclerView != null) {
                mRecyclerView.requestLayout();
            }
        }
        public void assertInLayoutOrScroll(String message) {
            if (mRecyclerView != null) {
                mRecyclerView.assertInLayoutOrScroll(message);
            }
        }
        public static int chooseSize(int spec, int desired, int min) {
            final int mode = View.MeasureSpec.getMode(spec);
            final int size = View.MeasureSpec.getSize(spec);
            switch (mode) {
                case View.MeasureSpec.EXACTLY:
                    return size;
                case View.MeasureSpec.AT_MOST:
                    return Math.min(size, Math.max(desired, min));
                case View.MeasureSpec.UNSPECIFIED:
                default:
                    return Math.max(desired, min);
            }
        }
        public void assertNotInLayoutOrScroll(String message) {
            if (mRecyclerView != null) {
                mRecyclerView.assertNotInLayoutOrScroll(message);
            }
        }
        @Deprecated
        public void setAutoMeasureEnabled(boolean enabled) {
            mAutoMeasure = enabled;
        }
        public boolean isAutoMeasureEnabled() {
            return mAutoMeasure;
        }
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
        public final void setItemPrefetchEnabled(boolean enabled) {
            if (enabled != mItemPrefetchEnabled) {
                mItemPrefetchEnabled = enabled;
                mPrefetchMaxCountObserved = 0;
                if (mRecyclerView != null) {
                    mRecyclerView.mRecycler.updateViewCacheSize();
                }
            }
        }
        public final boolean isItemPrefetchEnabled() {
            return mItemPrefetchEnabled;
        }
        public void collectAdjacentPrefetchPositions(int dx, int dy, State state,
                LayoutPrefetchRegistry layoutPrefetchRegistry) {}
        public void collectInitialPrefetchPositions(int adapterItemCount,
                LayoutPrefetchRegistry layoutPrefetchRegistry) {}

        void dispatchAttachedToWindow(RecyclerView view) {
            mIsAttachedToWindow = true;
            onAttachedToWindow(view);
        }

        void dispatchDetachedFromWindow(RecyclerView view, Recycler recycler) {
            mIsAttachedToWindow = false;
            onDetachedFromWindow(view, recycler);
        }
        public boolean isAttachedToWindow() {
            return mIsAttachedToWindow;
        }
        public void postOnAnimation(Runnable action) {
            if (mRecyclerView != null) {
                ViewCompat.postOnAnimation(mRecyclerView, action);
            }
        }
        public boolean removeCallbacks(Runnable action) {
            if (mRecyclerView != null) {
                return mRecyclerView.removeCallbacks(action);
            }
            return false;
        }
        @CallSuper
        public void onAttachedToWindow(RecyclerView view) {
        }
        @Deprecated
        public void onDetachedFromWindow(RecyclerView view) {

        }
        @CallSuper
        public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
            onDetachedFromWindow(view);
        }
        public boolean getClipToPadding() {
            return mRecyclerView != null && mRecyclerView.mClipToPadding;
        }
        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e(TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
        }
        public void onLayoutCompleted(State state) {
        }
        public abstract LayoutParams generateDefaultLayoutParams();
        public boolean checkLayoutParams(LayoutParams lp) {
            return lp != null;
        }
        public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            } else if (lp instanceof MarginLayoutParams) {
                return new LayoutParams((MarginLayoutParams) lp);
            } else {
                return new LayoutParams(lp);
            }
        }
        public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
            return new LayoutParams(c, attrs);
        }
        public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
            return 0;
        }
        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            return 0;
        }
        public boolean canScrollHorizontally() {
            return false;
        }
        public boolean canScrollVertically() {
            return false;
        }
        public void scrollToPosition(int position) {
            if (DEBUG) {
                Log.e(TAG, "You MUST implement scrollToPosition. It will soon become abstract");
            }
        }
        public void smoothScrollToPosition(RecyclerView recyclerView, State state,
                int position) {
            Log.e(TAG, "You must override smoothScrollToPosition to support smooth scrolling");
        }
        public void startSmoothScroll(SmoothScroller smoothScroller) {
            if (mSmoothScroller != null && smoothScroller != mSmoothScroller
                    && mSmoothScroller.isRunning()) {
                mSmoothScroller.stop();
            }
            mSmoothScroller = smoothScroller;
            mSmoothScroller.start(mRecyclerView, this);
        }
        public boolean isSmoothScrolling() {
            return mSmoothScroller != null && mSmoothScroller.isRunning();
        }
        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(mRecyclerView);
        }
        public void endAnimation(View view) {
            if (mRecyclerView.mItemAnimator != null) {
                mRecyclerView.mItemAnimator.endAnimation(getChildViewHolderInt(view));
            }
        }
        public void addDisappearingView(View child) {
            addDisappearingView(child, -1);
        }
        public void addDisappearingView(View child, int index) {
            addViewInt(child, index, true);
        }
        public void addView(View child) {
            addView(child, -1);
        }
        public void addView(View child, int index) {
            addViewInt(child, index, false);
        }
        private void addViewInt(View child, int index, boolean disappearing) {
            final ViewHolder holder = getChildViewHolderInt(child);
            if (disappearing || holder.isRemoved()) {
                mRecyclerView.mViewInfoStore.addToDisappearedInLayout(holder);
            } else {
                mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(holder);
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (holder.wasReturnedFromScrap() || holder.isScrap()) {
                if (holder.isScrap()) {
                    holder.unScrap();
                } else {
                    holder.clearReturnedFromScrapFlag();
                }
                mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);
                if (DISPATCH_TEMP_DETACH) {
                    ViewCompat.dispatchFinishTemporaryDetach(child);
                }
            } else if (child.getParent() == mRecyclerView) {
                int currentIndex = mChildHelper.indexOfChild(child);
                if (index == -1) {
                    index = mChildHelper.getChildCount();
                }
                if (currentIndex == -1) {
                    throw new IllegalStateException("Added View has RecyclerView as parent but"
                            + " view is not a real child. Unfiltered index:"
                            + mRecyclerView.indexOfChild(child) + mRecyclerView.exceptionLabel());
                }
                if (currentIndex != index) {
                    mRecyclerView.mLayout.moveView(currentIndex, index);
                }
            } else {
                mChildHelper.addView(child, index, false);
                lp.mInsetsDirty = true;
                if (mSmoothScroller != null && mSmoothScroller.isRunning()) {
                    mSmoothScroller.onChildAttachedToWindow(child);
                }
            }
            if (lp.mPendingInvalidate) {
                if (DEBUG) {
                    Log.d(TAG, "consuming pending invalidate on child " + lp.mViewHolder);
                }
                holder.itemView.invalidate();
                lp.mPendingInvalidate = false;
            }
        }
        public void removeView(View child) {
            mChildHelper.removeView(child);
        }
        public void removeViewAt(int index) {
            final View child = getChildAt(index);
            if (child != null) {
                mChildHelper.removeViewAt(index);
            }
        }
        public void removeAllViews() {
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                mChildHelper.removeViewAt(i);
            }
        }
        public int getBaseline() {
            return -1;
        }
        public int getPosition(@NonNull View view) {
            return ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }
        public int getItemViewType(@NonNull View view) {
            return getChildViewHolderInt(view).getItemViewType();
        }
        @Nullable
        public View findContainingItemView(@NonNull View view) {
            if (mRecyclerView == null) {
                return null;
            }
            View found = mRecyclerView.findContainingItemView(view);
            if (found == null) {
                return null;
            }
            if (mChildHelper.isHidden(found)) {
                return null;
            }
            return found;
        }
        @Nullable
        public View findViewByPosition(int position) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                ViewHolder vh = getChildViewHolderInt(child);
                if (vh == null) {
                    continue;
                }
                if (vh.getLayoutPosition() == position && !vh.shouldIgnore()
                        && (mRecyclerView.mState.isPreLayout() || !vh.isRemoved())) {
                    return child;
                }
            }
            return null;
        }
        public void detachView(@NonNull View child) {
            final int ind = mChildHelper.indexOfChild(child);
            if (ind >= 0) {
                detachViewInternal(ind, child);
            }
        }
        public void detachViewAt(int index) {
            detachViewInternal(index, getChildAt(index));
        }

        private void detachViewInternal(int index, @NonNull View view) {
            if (DISPATCH_TEMP_DETACH) {
                ViewCompat.dispatchStartTemporaryDetach(view);
            }
            mChildHelper.detachViewFromParent(index);
        }
        public void attachView(@NonNull View child, int index, LayoutParams lp) {
            ViewHolder vh = getChildViewHolderInt(child);
            if (vh.isRemoved()) {
                mRecyclerView.mViewInfoStore.addToDisappearedInLayout(vh);
            } else {
                mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(vh);
            }
            mChildHelper.attachViewToParent(child, index, lp, vh.isRemoved());
            if (DISPATCH_TEMP_DETACH)  {
                ViewCompat.dispatchFinishTemporaryDetach(child);
            }
        }
        public void attachView(@NonNull View child, int index) {
            attachView(child, index, (LayoutParams) child.getLayoutParams());
        }
        public void attachView(@NonNull View child) {
            attachView(child, -1);
        }
        public void removeDetachedView(@NonNull View child) {
            mRecyclerView.removeDetachedView(child, false);
        }
        public void moveView(int fromIndex, int toIndex) {
            View view = getChildAt(fromIndex);
            if (view == null) {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:"
                        + fromIndex + mRecyclerView.toString());
            }
            detachViewAt(fromIndex);
            attachView(view, toIndex);
        }
        public void detachAndScrapView(@NonNull View child, @NonNull Recycler recycler) {
            int index = mChildHelper.indexOfChild(child);
            scrapOrRecycleView(recycler, index, child);
        }
        public void detachAndScrapViewAt(int index, @NonNull Recycler recycler) {
            final View child = getChildAt(index);
            scrapOrRecycleView(recycler, index, child);
        }
        public void removeAndRecycleView(@NonNull View child, @NonNull Recycler recycler) {
            removeView(child);
            recycler.recycleView(child);
        }
        public void removeAndRecycleViewAt(int index, @NonNull Recycler recycler) {
            final View view = getChildAt(index);
            removeViewAt(index);
            recycler.recycleView(view);
        }
        public int getChildCount() {
            return mChildHelper != null ? mChildHelper.getChildCount() : 0;
        }
        @Nullable
        public View getChildAt(int index) {
            return mChildHelper != null ? mChildHelper.getChildAt(index) : null;
        }
        public int getWidthMode() {
            return mWidthMode;
        }
        public int getHeightMode() {
            return mHeightMode;
        }
        @Px
        public int getWidth() {
            return mWidth;
        }
        @Px
        public int getHeight() {
            return mHeight;
        }
        @Px
        public int getPaddingLeft() {
            return mRecyclerView != null ? mRecyclerView.getPaddingLeft() : 0;
        }
        @Px
        public int getPaddingTop() {
            return mRecyclerView != null ? mRecyclerView.getPaddingTop() : 0;
        }
        @Px
        public int getPaddingRight() {
            return mRecyclerView != null ? mRecyclerView.getPaddingRight() : 0;
        }
        @Px
        public int getPaddingBottom() {
            return mRecyclerView != null ? mRecyclerView.getPaddingBottom() : 0;
        }
        @Px
        public int getPaddingStart() {
            return mRecyclerView != null ? ViewCompat.getPaddingStart(mRecyclerView) : 0;
        }
        @Px
        public int getPaddingEnd() {
            return mRecyclerView != null ? ViewCompat.getPaddingEnd(mRecyclerView) : 0;
        }
        public boolean isFocused() {
            return mRecyclerView != null && mRecyclerView.isFocused();
        }
        public boolean hasFocus() {
            return mRecyclerView != null && mRecyclerView.hasFocus();
        }
        @Nullable
        public View getFocusedChild() {
            if (mRecyclerView == null) {
                return null;
            }
            final View focused = mRecyclerView.getFocusedChild();
            if (focused == null || mChildHelper.isHidden(focused)) {
                return null;
            }
            return focused;
        }
        public int getItemCount() {
            final Adapter a = mRecyclerView != null ? mRecyclerView.getAdapter() : null;
            return a != null ? a.getItemCount() : 0;
        }
        public void offsetChildrenHorizontal(@Px int dx) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenHorizontal(dx);
            }
        }
        public void offsetChildrenVertical(@Px int dy) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenVertical(dy);
            }
        }
        public void ignoreView(@NonNull View view) {
            if (view.getParent() != mRecyclerView || mRecyclerView.indexOfChild(view) == -1) {
                throw new IllegalArgumentException("View should be fully attached to be ignored"
                        + mRecyclerView.exceptionLabel());
            }
            final ViewHolder vh = getChildViewHolderInt(view);
            vh.addFlags(ViewHolder.FLAG_IGNORE);
            mRecyclerView.mViewInfoStore.removeViewHolder(vh);
        }
        public void stopIgnoringView(@NonNull View view) {
            final ViewHolder vh = getChildViewHolderInt(view);
            vh.stopIgnoring();
            vh.resetInternal();
            vh.addFlags(ViewHolder.FLAG_INVALID);
        }
        public void detachAndScrapAttachedViews(@NonNull Recycler recycler) {
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View v = getChildAt(i);
                scrapOrRecycleView(recycler, i, v);
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int index, View view) {
            final ViewHolder viewHolder = getChildViewHolderInt(view);
            if (viewHolder.shouldIgnore()) {
                if (DEBUG) {
                    Log.d(TAG, "ignoring view " + viewHolder);
                }
                return;
            }
            if (viewHolder.isInvalid() && !viewHolder.isRemoved()
                    && !mRecyclerView.mAdapter.hasStableIds()) {
                removeViewAt(index);
                recycler.recycleViewHolderInternal(viewHolder);
            } else {
                detachViewAt(index);
                recycler.scrapView(view);
                mRecyclerView.mViewInfoStore.onViewDetached(viewHolder);
            }
        }
        void removeAndRecycleScrapInt(Recycler recycler) {
            final int scrapCount = recycler.getScrapCount();
            // Loop backward, recycler might be changed by removeDetachedView()
            for (int i = scrapCount - 1; i >= 0; i--) {
                final View scrap = recycler.getScrapViewAt(i);
                final ViewHolder vh = getChildViewHolderInt(scrap);
                if (vh.shouldIgnore()) {
                    continue;
                }
                vh.setIsRecyclable(false);
                if (vh.isTmpDetached()) {
                    mRecyclerView.removeDetachedView(scrap, false);
                }
                if (mRecyclerView.mItemAnimator != null) {
                    mRecyclerView.mItemAnimator.endAnimation(vh);
                }
                vh.setIsRecyclable(true);
                recycler.quickRecycleScrapView(scrap);
            }
            recycler.clearScrap();
            if (scrapCount > 0) {
                mRecyclerView.invalidate();
            }
        }
        public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;
            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
                    getPaddingLeft() + getPaddingRight() + widthUsed, lp.width,
                    canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
                    getPaddingTop() + getPaddingBottom() + heightUsed, lp.height,
                    canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }
        boolean shouldReMeasureChild(View child, int widthSpec, int heightSpec, LayoutParams lp) {
            return !mMeasurementCacheEnabled
                    || !isMeasurementUpToDate(child.getMeasuredWidth(), widthSpec, lp.width)
                    || !isMeasurementUpToDate(child.getMeasuredHeight(), heightSpec, lp.height);
        }
        boolean shouldMeasureChild(View child, int widthSpec, int heightSpec, LayoutParams lp) {
            return child.isLayoutRequested()
                    || !mMeasurementCacheEnabled
                    || !isMeasurementUpToDate(child.getWidth(), widthSpec, lp.width)
                    || !isMeasurementUpToDate(child.getHeight(), heightSpec, lp.height);
        }
        public boolean isMeasurementCacheEnabled() {
            return mMeasurementCacheEnabled;
        }
        public void setMeasurementCacheEnabled(boolean measurementCacheEnabled) {
            mMeasurementCacheEnabled = measurementCacheEnabled;
        }

        private static boolean isMeasurementUpToDate(int childSize, int spec, int dimension) {
            final int specMode = MeasureSpec.getMode(spec);
            final int specSize = MeasureSpec.getSize(spec);
            if (dimension > 0 && childSize != dimension) {
                return false;
            }
            switch (specMode) {
                case MeasureSpec.UNSPECIFIED:
                    return true;
                case MeasureSpec.AT_MOST:
                    return specSize >= childSize;
                case MeasureSpec.EXACTLY:
                    return  specSize == childSize;
            }
            return false;
        }
        public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;

            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
                    getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin + widthUsed, lp.width,
                    canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
                    getPaddingTop() + getPaddingBottom()
                            + lp.topMargin + lp.bottomMargin + heightUsed, lp.height,
                    canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }
        @Deprecated
        public static int getChildMeasureSpec(int parentSize, int padding, int childDimension,
                boolean canScroll) {
            int size = Math.max(0, parentSize - padding);
            int resultSize = 0;
            int resultMode = 0;
            if (canScroll) {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else {
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
            } else {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    // TODO this should be my spec.
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
            }
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }
        public static int getChildMeasureSpec(int parentSize, int parentMode, int padding,
                int childDimension, boolean canScroll) {
            int size = Math.max(0, parentSize - padding);
            int resultSize = 0;
            int resultMode = 0;
            if (canScroll) {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    switch (parentMode) {
                        case MeasureSpec.AT_MOST:
                        case MeasureSpec.EXACTLY:
                            resultSize = size;
                            resultMode = parentMode;
                            break;
                        case MeasureSpec.UNSPECIFIED:
                            resultSize = 0;
                            resultMode = MeasureSpec.UNSPECIFIED;
                            break;
                    }
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
            } else {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = parentMode;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    if (parentMode == MeasureSpec.AT_MOST || parentMode == MeasureSpec.EXACTLY) {
                        resultMode = MeasureSpec.AT_MOST;
                    } else {
                        resultMode = MeasureSpec.UNSPECIFIED;
                    }

                }
            }
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }
        public int getDecoratedMeasuredWidth(@NonNull View child) {
            final Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return child.getMeasuredWidth() + insets.left + insets.right;
        }
        public int getDecoratedMeasuredHeight(@NonNull View child) {
            final Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return child.getMeasuredHeight() + insets.top + insets.bottom;
        }
        public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
            final Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            child.layout(left + insets.left, top + insets.top, right - insets.right,
                    bottom - insets.bottom);
        }
        public void layoutDecoratedWithMargins(@NonNull View child, int left, int top, int right,
                int bottom) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final Rect insets = lp.mDecorInsets;
            child.layout(left + insets.left + lp.leftMargin, top + insets.top + lp.topMargin,
                    right - insets.right - lp.rightMargin,
                    bottom - insets.bottom - lp.bottomMargin);
        }
        public void getTransformedBoundingBox(@NonNull View child, boolean includeDecorInsets,
                @NonNull Rect out) {
            if (includeDecorInsets) {
                Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
                out.set(-insets.left, -insets.top,
                        child.getWidth() + insets.right, child.getHeight() + insets.bottom);
            } else {
                out.set(0, 0, child.getWidth(), child.getHeight());
            }

            if (mRecyclerView != null) {
                final Matrix childMatrix = child.getMatrix();
                if (childMatrix != null && !childMatrix.isIdentity()) {
                    final RectF tempRectF = mRecyclerView.mTempRectF;
                    tempRectF.set(out);
                    childMatrix.mapRect(tempRectF);
                    out.set(
                            (int) Math.floor(tempRectF.left),
                            (int) Math.floor(tempRectF.top),
                            (int) Math.ceil(tempRectF.right),
                            (int) Math.ceil(tempRectF.bottom)
                    );
                }
            }
            out.offset(child.getLeft(), child.getTop());
        }
        public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect outBounds) {
            RecyclerView.getDecoratedBoundsWithMarginsInt(view, outBounds);
        }
        public int getDecoratedLeft(@NonNull View child) {
            return child.getLeft() - getLeftDecorationWidth(child);
        }
        public int getDecoratedTop(@NonNull View child) {
            return child.getTop() - getTopDecorationHeight(child);
        }
        public int getDecoratedRight(@NonNull View child) {
            return child.getRight() + getRightDecorationWidth(child);
        }
        public int getDecoratedBottom(@NonNull View child) {
            return child.getBottom() + getBottomDecorationHeight(child);
        }
        public void calculateItemDecorationsForChild(@NonNull View child, @NonNull Rect outRect) {
            if (mRecyclerView == null) {
                outRect.set(0, 0, 0, 0);
                return;
            }
            Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            outRect.set(insets);
        }
        public int getTopDecorationHeight(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.top;
        }
        public int getBottomDecorationHeight(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.bottom;
        }
        public int getLeftDecorationWidth(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.left;
        }
        public int getRightDecorationWidth(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.right;
        }
        @Nullable
        public View onFocusSearchFailed(@NonNull View focused, int direction,
                @NonNull Recycler recycler, @NonNull State state) {
            return null;
        }
        @Nullable
        public View onInterceptFocusSearch(@NonNull View focused, int direction) {
            return null;
        }
        private int[] getChildRectangleOnScreenScrollAmount(View child, Rect rect) {
            int[] out = new int[2];
            final int parentLeft = getPaddingLeft();
            final int parentTop = getPaddingTop();
            final int parentRight = getWidth() - getPaddingRight();
            final int parentBottom = getHeight() - getPaddingBottom();
            final int childLeft = child.getLeft() + rect.left - child.getScrollX();
            final int childTop = child.getTop() + rect.top - child.getScrollY();
            final int childRight = childLeft + rect.width();
            final int childBottom = childTop + rect.height();
            final int offScreenLeft = Math.min(0, childLeft - parentLeft);
            final int offScreenTop = Math.min(0, childTop - parentTop);
            final int offScreenRight = Math.max(0, childRight - parentRight);
            final int offScreenBottom = Math.max(0, childBottom - parentBottom);
            final int dx;
            if (getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
            final int dy = offScreenTop != 0 ? offScreenTop
                    : Math.min(childTop - parentTop, offScreenBottom);
            out[0] = dx;
            out[1] = dy;
            return out;
        }
        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent,
                @NonNull View child, @NonNull Rect rect, boolean immediate) {
            return requestChildRectangleOnScreen(parent, child, rect, immediate, false);
        }
        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent,
                @NonNull View child, @NonNull Rect rect, boolean immediate,
                boolean focusedChildVisible) {
            int[] scrollAmount = getChildRectangleOnScreenScrollAmount(child, rect
            );
            int dx = scrollAmount[0];
            int dy = scrollAmount[1];
            if (!focusedChildVisible || isFocusedChildVisibleAfterScrolling(parent, dx, dy)) {
                if (dx != 0 || dy != 0) {
                    if (immediate) {
                        parent.scrollBy(dx, dy);
                    } else {
                        parent.smoothScrollBy(dx, dy);
                    }
                    return true;
                }
            }
            return false;
        }
        public boolean isViewPartiallyVisible(@NonNull View child, boolean completelyVisible,
                boolean acceptEndPointInclusion) {
            int boundsFlag = (ViewBoundsCheck.FLAG_CVS_GT_PVS | ViewBoundsCheck.FLAG_CVS_EQ_PVS
                    | ViewBoundsCheck.FLAG_CVE_LT_PVE | ViewBoundsCheck.FLAG_CVE_EQ_PVE);
            boolean isViewFullyVisible = mHorizontalBoundCheck.isViewWithinBoundFlags(child,
                    boundsFlag)
                    && mVerticalBoundCheck.isViewWithinBoundFlags(child, boundsFlag);
            if (completelyVisible) {
                return isViewFullyVisible;
            } else {
                return !isViewFullyVisible;
            }
        }
        private boolean isFocusedChildVisibleAfterScrolling(RecyclerView parent, int dx, int dy) {
            final View focusedChild = parent.getFocusedChild();
            if (focusedChild == null) {
                return false;
            }
            final int parentLeft = getPaddingLeft();
            final int parentTop = getPaddingTop();
            final int parentRight = getWidth() - getPaddingRight();
            final int parentBottom = getHeight() - getPaddingBottom();
            final Rect bounds = mRecyclerView.mTempRect;
            getDecoratedBoundsWithMargins(focusedChild, bounds);

            if (bounds.left - dx >= parentRight || bounds.right - dx <= parentLeft
                    || bounds.top - dy >= parentBottom || bounds.bottom - dy <= parentTop) {
                return false;
            }
            return true;
        }
        @Deprecated
        public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull View child,
                @Nullable View focused) {
            return isSmoothScrolling() || parent.isComputingLayout();
        }
        public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull State state,
                @NonNull View child, @Nullable View focused) {
            return onRequestChildFocus(parent, child, focused);
        }
        public void onAdapterChanged(@Nullable Adapter oldAdapter, @Nullable Adapter newAdapter) {
        }
        public boolean onAddFocusables(@NonNull RecyclerView recyclerView,
                @NonNull ArrayList<View> views, int direction, int focusableMode) {
            return false;
        }
        public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        }
        public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart,
                int itemCount) {
        }
        public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart,
                int itemCount) {
        }
        public void onItemsUpdated(@NonNull RecyclerView recyclerView, int positionStart,
                int itemCount) {
        }
        public void onItemsUpdated(@NonNull RecyclerView recyclerView, int positionStart,
                int itemCount, @Nullable Object payload) {
            onItemsUpdated(recyclerView, positionStart, itemCount);
        }
        public void onItemsMoved(@NonNull RecyclerView recyclerView, int from, int to,
                int itemCount) {

        }
        public int computeHorizontalScrollExtent(@NonNull State state) {
            return 0;
        }
        public int computeHorizontalScrollOffset(@NonNull State state) {
            return 0;
        }
        public int computeHorizontalScrollRange(@NonNull State state) {
            return 0;
        }
        public int computeVerticalScrollExtent(@NonNull State state) {
            return 0;
        }
        public int computeVerticalScrollOffset(@NonNull State state) {
            return 0;
        }
        public int computeVerticalScrollRange(@NonNull State state) {
            return 0;
        }
        public void onMeasure(@NonNull Recycler recycler, @NonNull State state, int widthSpec,
                int heightSpec) {
            mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
        }
        public void setMeasuredDimension(int widthSize, int heightSize) {
            mRecyclerView.setMeasuredDimension(widthSize, heightSize);
        }
        @Px
        public int getMinimumWidth() {
            return ViewCompat.getMinimumWidth(mRecyclerView);
        }
        @Px
        public int getMinimumHeight() {
            return ViewCompat.getMinimumHeight(mRecyclerView);
        }
        @Nullable
        public Parcelable onSaveInstanceState() {
            return null;
        }
        public void onRestoreInstanceState(Parcelable state) {

        }
        void stopSmoothScroller() {
            if (mSmoothScroller != null) {
                mSmoothScroller.stop();
            }
        }
        void onSmoothScrollerStopped(SmoothScroller smoothScroller) {
            if (mSmoothScroller == smoothScroller) {
                mSmoothScroller = null;
            }
        }
        public void onScrollStateChanged(int state) {
        }
        public void removeAndRecycleAllViews(@NonNull Recycler recycler) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View view = getChildAt(i);
                if (!getChildViewHolderInt(view).shouldIgnore()) {
                    removeAndRecycleViewAt(i, recycler);
                }
            }
        }
        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {
            onInitializeAccessibilityNodeInfo(mRecyclerView.mRecycler, mRecyclerView.mState, info);
        }
        public void onInitializeAccessibilityNodeInfo(@NonNull Recycler recycler,
                @NonNull State state, @NonNull AccessibilityNodeInfoCompat info) {
            if (mRecyclerView.canScrollVertically(-1) || mRecyclerView.canScrollHorizontally(-1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                info.setScrollable(true);
            }
            if (mRecyclerView.canScrollVertically(1) || mRecyclerView.canScrollHorizontally(1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
                info.setScrollable(true);
            }
            final AccessibilityNodeInfoCompat.CollectionInfoCompat collectionInfo =
                    AccessibilityNodeInfoCompat.CollectionInfoCompat
                            .obtain(getRowCountForAccessibility(recycler, state),
                                    getColumnCountForAccessibility(recycler, state),
                                    isLayoutHierarchical(recycler, state),
                                    getSelectionModeForAccessibility(recycler, state));
            info.setCollectionInfo(collectionInfo);
        }
        public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
            onInitializeAccessibilityEvent(mRecyclerView.mRecycler, mRecyclerView.mState, event);
        }
        public void onInitializeAccessibilityEvent(@NonNull Recycler recycler, @NonNull State state,
                @NonNull AccessibilityEvent event) {
            if (mRecyclerView == null || event == null) {
                return;
            }
            event.setScrollable(mRecyclerView.canScrollVertically(1)
                    || mRecyclerView.canScrollVertically(-1)
                    || mRecyclerView.canScrollHorizontally(-1)
                    || mRecyclerView.canScrollHorizontally(1));

            if (mRecyclerView.mAdapter != null) {
                event.setItemCount(mRecyclerView.mAdapter.getItemCount());
            }
        }
        void onInitializeAccessibilityNodeInfoForItem(View host, AccessibilityNodeInfoCompat info) {
            final ViewHolder vh = getChildViewHolderInt(host);
            // avoid trying to create accessibility node info for removed children
            if (vh != null && !vh.isRemoved() && !mChildHelper.isHidden(vh.itemView)) {
                onInitializeAccessibilityNodeInfoForItem(mRecyclerView.mRecycler,
                        mRecyclerView.mState, host, info);
            }
        }
        public void onInitializeAccessibilityNodeInfoForItem(@NonNull Recycler recycler,
                @NonNull State state, @NonNull View host,
                @NonNull AccessibilityNodeInfoCompat info) {
            int rowIndexGuess = canScrollVertically() ? getPosition(host) : 0;
            int columnIndexGuess = canScrollHorizontally() ? getPosition(host) : 0;
            final AccessibilityNodeInfoCompat.CollectionItemInfoCompat itemInfo =
                    AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(rowIndexGuess, 1,
                            columnIndexGuess, 1, false, false);
            info.setCollectionItemInfo(itemInfo);
        }
        public void requestSimpleAnimationsInNextLayout() {
            mRequestedSimpleAnimations = true;
        }
        public int getSelectionModeForAccessibility(@NonNull Recycler recycler,
                @NonNull State state) {
            return AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_NONE;
        }
        public int getRowCountForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            if (mRecyclerView == null || mRecyclerView.mAdapter == null) {
                return 1;
            }
            return canScrollVertically() ? mRecyclerView.mAdapter.getItemCount() : 1;
        }
        public int getColumnCountForAccessibility(@NonNull Recycler recycler,
                @NonNull State state) {
            if (mRecyclerView == null || mRecyclerView.mAdapter == null) {
                return 1;
            }
            return canScrollHorizontally() ? mRecyclerView.mAdapter.getItemCount() : 1;
        }
        public boolean isLayoutHierarchical(@NonNull Recycler recycler, @NonNull State state) {
            return false;
        }
        boolean performAccessibilityAction(int action, @Nullable Bundle args) {
            return performAccessibilityAction(mRecyclerView.mRecycler, mRecyclerView.mState,
                    action, args);
        }
        public boolean performAccessibilityAction(@NonNull Recycler recycler, @NonNull State state,
                int action, @Nullable Bundle args) {
            if (mRecyclerView == null) {
                return false;
            }
            int vScroll = 0, hScroll = 0;
            switch (action) {
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD:
                    if (mRecyclerView.canScrollVertically(-1)) {
                        vScroll = -(getHeight() - getPaddingTop() - getPaddingBottom());
                    }
                    if (mRecyclerView.canScrollHorizontally(-1)) {
                        hScroll = -(getWidth() - getPaddingLeft() - getPaddingRight());
                    }
                    break;
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD:
                    if (mRecyclerView.canScrollVertically(1)) {
                        vScroll = getHeight() - getPaddingTop() - getPaddingBottom();
                    }
                    if (mRecyclerView.canScrollHorizontally(1)) {
                        hScroll = getWidth() - getPaddingLeft() - getPaddingRight();
                    }
                    break;
            }
            if (vScroll == 0 && hScroll == 0) {
                return false;
            }
            mRecyclerView.smoothScrollBy(hScroll, vScroll, null, UNDEFINED_DURATION, true);
            return true;
        }
        boolean performAccessibilityActionForItem(@NonNull View view, int action,
                @Nullable Bundle args) {
            return performAccessibilityActionForItem(mRecyclerView.mRecycler, mRecyclerView.mState,
                    view, action, args);
        }
        public boolean performAccessibilityActionForItem(@NonNull Recycler recycler,
                @NonNull State state, @NonNull View view, int action, @Nullable Bundle args) {
            return false;
        }
        public static Properties getProperties(@NonNull Context context,
                @Nullable AttributeSet attrs,
                int defStyleAttr, int defStyleRes) {
            Properties properties = new Properties();
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView,
                    defStyleAttr, defStyleRes);
            properties.orientation = a.getInt(R.styleable.RecyclerView_android_orientation,
                    DEFAULT_ORIENTATION);
            properties.spanCount = a.getInt(R.styleable.RecyclerView_spanCount, 1);
            properties.reverseLayout = a.getBoolean(R.styleable.RecyclerView_reverseLayout, false);
            properties.stackFromEnd = a.getBoolean(R.styleable.RecyclerView_stackFromEnd, false);
            a.recycle();
            return properties;
        }
        void setExactMeasureSpecsFrom(RecyclerView recyclerView) {
            setMeasureSpecs(
                    MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), MeasureSpec.EXACTLY)
            );
        }
        boolean shouldMeasureTwice() {
            return false;
        }
        boolean hasFlexibleChildInBothOrientations() {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                final ViewGroup.LayoutParams lp = child.getLayoutParams();
                if (lp.width < 0 && lp.height < 0) {
                    return true;
                }
            }
            return false;
        }
        public static class Properties {
            public int orientation;
            public int spanCount;
            public boolean reverseLayout;
            public boolean stackFromEnd;
        }
    }