public abstract static class ViewHolder {
        @NonNull
        public final View itemView;
        WeakReference<RecyclerView> mNestedRecyclerView;
        int mPosition = NO_POSITION;
        int mOldPosition = NO_POSITION;
        long mItemId = NO_ID;
        int mItemViewType = INVALID_TYPE;
        int mPreLayoutPosition = NO_POSITION;
        ViewHolder mShadowedHolder = null;
        ViewHolder mShadowingHolder = null;
        static final int FLAG_BOUND = 1 << 0;
        static final int FLAG_UPDATE = 1 << 1;
        static final int FLAG_INVALID = 1 << 2;
        static final int FLAG_REMOVED = 1 << 3;
        static final int FLAG_NOT_RECYCLABLE = 1 << 4;
        static final int FLAG_RETURNED_FROM_SCRAP = 1 << 5;
        static final int FLAG_IGNORE = 1 << 7;
        static final int FLAG_TMP_DETACHED = 1 << 8;
        static final int FLAG_ADAPTER_POSITION_UNKNOWN = 1 << 9;
        static final int FLAG_ADAPTER_FULLUPDATE = 1 << 10;
        static final int FLAG_MOVED = 1 << 11;
        static final int FLAG_APPEARED_IN_PRE_LAYOUT = 1 << 12;
        static final int PENDING_ACCESSIBILITY_STATE_NOT_SET = -1;
        static final int FLAG_BOUNCED_FROM_HIDDEN_LIST = 1 << 13;
        int mFlags;
        private static final List<Object> FULLUPDATE_PAYLOADS = Collections.emptyList();
        List<Object> mPayloads = null;
        List<Object> mUnmodifiedPayloads = null;
        private int mIsRecyclableCount = 0;
        Recycler mScrapContainer = null;
        boolean mInChangeScrap = false;
        private int mWasImportantForAccessibilityBeforeHidden =
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        @VisibleForTesting
        int mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;
        RecyclerView mOwnerRecyclerView;
        public ViewHolder(@NonNull View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
        void flagRemovedAndOffsetPosition(int mNewPosition, int offset, boolean applyToPreLayout) {
            addFlags(ViewHolder.FLAG_REMOVED);
            offsetPosition(offset, applyToPreLayout);
            mPosition = mNewPosition;
        }
        void offsetPosition(int offset, boolean applyToPreLayout) {
            if (mOldPosition == NO_POSITION) {
                mOldPosition = mPosition;
            }
            if (mPreLayoutPosition == NO_POSITION) {
                mPreLayoutPosition = mPosition;
            }
            if (applyToPreLayout) {
                mPreLayoutPosition += offset;
            }
            mPosition += offset;
            if (itemView.getLayoutParams() != null) {
                ((LayoutParams) itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }
        void clearOldPosition() {
            mOldPosition = NO_POSITION;
            mPreLayoutPosition = NO_POSITION;
        }
        void saveOldPosition() {
            if (mOldPosition == NO_POSITION) {
                mOldPosition = mPosition;
            }
        }
        boolean shouldIgnore() {
            return (mFlags & FLAG_IGNORE) != 0;
        }
        @Deprecated
        public final int getPosition() {
            return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
        }
        public final int getLayoutPosition() {
            return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
        }
        public final int getAdapterPosition() {
            if (mOwnerRecyclerView == null) {
                return NO_POSITION;
            }
            return mOwnerRecyclerView.getAdapterPositionFor(this);
        }
        public final int getOldPosition() {
            return mOldPosition;
        }
        public final long getItemId() {
            return mItemId;
        }
        public final int getItemViewType() {
            return mItemViewType;
        }
        boolean isScrap() {
            return mScrapContainer != null;
        }
        void unScrap() {
            mScrapContainer.unscrapView(this);
        }
        boolean wasReturnedFromScrap() {
            return (mFlags & FLAG_RETURNED_FROM_SCRAP) != 0;
        }
        void clearReturnedFromScrapFlag() {
            mFlags = mFlags & ~FLAG_RETURNED_FROM_SCRAP;
        }
        void clearTmpDetachFlag() {
            mFlags = mFlags & ~FLAG_TMP_DETACHED;
        }
        void stopIgnoring() {
            mFlags = mFlags & ~FLAG_IGNORE;
        }
        void setScrapContainer(Recycler recycler, boolean isChangeScrap) {
            mScrapContainer = recycler;
            mInChangeScrap = isChangeScrap;
        }
        boolean isInvalid() {
            return (mFlags & FLAG_INVALID) != 0;
        }
        boolean needsUpdate() {
            return (mFlags & FLAG_UPDATE) != 0;
        }
        boolean isBound() {
            return (mFlags & FLAG_BOUND) != 0;
        }
        boolean isRemoved() {
            return (mFlags & FLAG_REMOVED) != 0;
        }
        boolean hasAnyOfTheFlags(int flags) {
            return (mFlags & flags) != 0;
        }
        boolean isTmpDetached() {
            return (mFlags & FLAG_TMP_DETACHED) != 0;
        }
        boolean isAttachedToTransitionOverlay() {
            return itemView.getParent() != null && itemView.getParent() != mOwnerRecyclerView;
        }
        boolean isAdapterPositionUnknown() {
            return (mFlags & FLAG_ADAPTER_POSITION_UNKNOWN) != 0 || isInvalid();
        }
        void setFlags(int flags, int mask) {
            mFlags = (mFlags & ~mask) | (flags & mask);
        }
        void addFlags(int flags) {
            mFlags |= flags;
        }
        void addChangePayload(Object payload) {
            if (payload == null) {
                addFlags(FLAG_ADAPTER_FULLUPDATE);
            } else if ((mFlags & FLAG_ADAPTER_FULLUPDATE) == 0) {
                createPayloadsIfNeeded();
                mPayloads.add(payload);
            }
        }
        private void createPayloadsIfNeeded() {
            if (mPayloads == null) {
                mPayloads = new ArrayList<Object>();
                mUnmodifiedPayloads = Collections.unmodifiableList(mPayloads);
            }
        }
        void clearPayload() {
            if (mPayloads != null) {
                mPayloads.clear();
            }
            mFlags = mFlags & ~FLAG_ADAPTER_FULLUPDATE;
        }
        List<Object> getUnmodifiedPayloads() {
            if ((mFlags & FLAG_ADAPTER_FULLUPDATE) == 0) {
                if (mPayloads == null || mPayloads.size() == 0) {
                    return FULLUPDATE_PAYLOADS;
                }
                return mUnmodifiedPayloads;
            } else {
                return FULLUPDATE_PAYLOADS;
            }
        }
        void resetInternal() {
            mFlags = 0;
            mPosition = NO_POSITION;
            mOldPosition = NO_POSITION;
            mItemId = NO_ID;
            mPreLayoutPosition = NO_POSITION;
            mIsRecyclableCount = 0;
            mShadowedHolder = null;
            mShadowingHolder = null;
            clearPayload();
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
            mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;
            clearNestedRecyclerViewIfNotNested(this);
        }
        void onEnteredHiddenState(RecyclerView parent) {
            // While the view item is in hidden state, make it invisible for the accessibility.
            if (mPendingAccessibilityState != PENDING_ACCESSIBILITY_STATE_NOT_SET) {
                mWasImportantForAccessibilityBeforeHidden = mPendingAccessibilityState;
            } else {
                mWasImportantForAccessibilityBeforeHidden =
                        ViewCompat.getImportantForAccessibility(itemView);
            }
            parent.setChildImportantForAccessibilityInternal(this,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
        void onLeftHiddenState(RecyclerView parent) {
            parent.setChildImportantForAccessibilityInternal(this,
                    mWasImportantForAccessibilityBeforeHidden);
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        }
        @Override
        public String toString() {
            String className =
                    getClass().isAnonymousClass() ? "ViewHolder" : getClass().getSimpleName();
            final StringBuilder sb = new StringBuilder(className + "{"
                    + Integer.toHexString(hashCode()) + " position=" + mPosition + " id=" + mItemId
                    + ", oldPos=" + mOldPosition + ", pLpos:" + mPreLayoutPosition);
            if (isScrap()) {
                sb.append(" scrap ")
                        .append(mInChangeScrap ? "[changeScrap]" : "[attachedScrap]");
            }
            if (isInvalid()) sb.append(" invalid");
            if (!isBound()) sb.append(" unbound");
            if (needsUpdate()) sb.append(" update");
            if (isRemoved()) sb.append(" removed");
            if (shouldIgnore()) sb.append(" ignored");
            if (isTmpDetached()) sb.append(" tmpDetached");
            if (!isRecyclable()) sb.append(" not recyclable(" + mIsRecyclableCount + ")");
            if (isAdapterPositionUnknown()) sb.append(" undefined adapter position");

            if (itemView.getParent() == null) sb.append(" no parent");
            sb.append("}");
            return sb.toString();
        }
        public final void setIsRecyclable(boolean recyclable) {
            mIsRecyclableCount = recyclable ? mIsRecyclableCount - 1 : mIsRecyclableCount + 1;
            if (mIsRecyclableCount < 0) {
                mIsRecyclableCount = 0;
                if (DEBUG) {
                    throw new RuntimeException("isRecyclable decremented below 0: "
                            + "unmatched pair of setIsRecyable() calls for " + this);
                }
                Log.e(VIEW_LOG_TAG, "isRecyclable decremented below 0: "
                        + "unmatched pair of setIsRecyable() calls for " + this);
            } else if (!recyclable && mIsRecyclableCount == 1) {
                mFlags |= FLAG_NOT_RECYCLABLE;
            } else if (recyclable && mIsRecyclableCount == 0) {
                mFlags &= ~FLAG_NOT_RECYCLABLE;
            }
            if (DEBUG) {
                Log.d(TAG, "setIsRecyclable val:" + recyclable + ":" + this);
            }
        }
        public final boolean isRecyclable() {
            return (mFlags & FLAG_NOT_RECYCLABLE) == 0
                    && !ViewCompat.hasTransientState(itemView);
        }
        boolean shouldBeKeptAsChild() {
            return (mFlags & FLAG_NOT_RECYCLABLE) != 0;
        }
        boolean doesTransientStatePreventRecycling() {
            return (mFlags & FLAG_NOT_RECYCLABLE) == 0 && ViewCompat.hasTransientState(itemView);
        }
        boolean isUpdated() {
            return (mFlags & FLAG_UPDATE) != 0;
        }
    }