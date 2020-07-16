@SuppressWarnings("UnusedParameters")
    public abstract static class ItemAnimator {
        public static final int FLAG_CHANGED = ViewHolder.FLAG_UPDATE;
        public static final int FLAG_REMOVED = ViewHolder.FLAG_REMOVED;
        public static final int FLAG_INVALIDATED = ViewHolder.FLAG_INVALID;
        public static final int FLAG_MOVED = ViewHolder.FLAG_MOVED;
        public static final int FLAG_APPEARED_IN_PRE_LAYOUT =
                ViewHolder.FLAG_APPEARED_IN_PRE_LAYOUT;
        @IntDef(flag = true, value = {
                FLAG_CHANGED, FLAG_REMOVED, FLAG_MOVED, FLAG_INVALIDATED,
                FLAG_APPEARED_IN_PRE_LAYOUT
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface AdapterChanges {}
        private ItemAnimatorListener mListener = null;
        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners =
                new ArrayList<ItemAnimatorFinishedListener>();

        private long mAddDuration = 120;
        private long mRemoveDuration = 120;
        private long mMoveDuration = 250;
        private long mChangeDuration = 250;
        public long getMoveDuration() {
            return mMoveDuration;
        }
        public void setMoveDuration(long moveDuration) {
            mMoveDuration = moveDuration;
        }
        public long getAddDuration() {
            return mAddDuration;
        }
        public void setAddDuration(long addDuration) {
            mAddDuration = addDuration;
        }
        public long getRemoveDuration() {
            return mRemoveDuration;
        }
        public void setRemoveDuration(long removeDuration) {
            mRemoveDuration = removeDuration;
        }
        public long getChangeDuration() {
            return mChangeDuration;
        }
        public void setChangeDuration(long changeDuration) {
            mChangeDuration = changeDuration;
        }
        void setListener(ItemAnimatorListener listener) {
            mListener = listener;
        }
        public @NonNull ItemHolderInfo recordPreLayoutInformation(@NonNull State state,
                @NonNull ViewHolder viewHolder, @AdapterChanges int changeFlags,
                @NonNull List<Object> payloads) {
            return obtainHolderInfo().setFrom(viewHolder);
        }
        public @NonNull ItemHolderInfo recordPostLayoutInformation(@NonNull State state,
                @NonNull ViewHolder viewHolder) {
            return obtainHolderInfo().setFrom(viewHolder);
        }
        public abstract boolean animateDisappearance(@NonNull ViewHolder viewHolder,
                @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo);
        public abstract boolean animateAppearance(@NonNull ViewHolder viewHolder,
                @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);
        public abstract boolean animatePersistence(@NonNull ViewHolder viewHolder,
                @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);
        public abstract boolean animateChange(@NonNull ViewHolder oldHolder,
                @NonNull ViewHolder newHolder,
                @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);

        @AdapterChanges static int buildAdapterChangeFlagsForAnimations(ViewHolder viewHolder) {
            int flags = viewHolder.mFlags & (FLAG_INVALIDATED | FLAG_REMOVED | FLAG_CHANGED);
            if (viewHolder.isInvalid()) {
                return FLAG_INVALIDATED;
            }
            if ((flags & FLAG_INVALIDATED) == 0) {
                final int oldPos = viewHolder.getOldPosition();
                final int pos = viewHolder.getAdapterPosition();
                if (oldPos != NO_POSITION && pos != NO_POSITION && oldPos != pos) {
                    flags |= FLAG_MOVED;
                }
            }
            return flags;
        }
        public abstract void runPendingAnimations();
        public abstract void endAnimation(@NonNull ViewHolder item);
        public abstract void endAnimations();
        public abstract boolean isRunning();
        public final void dispatchAnimationFinished(@NonNull ViewHolder viewHolder) {
            onAnimationFinished(viewHolder);
            if (mListener != null) {
                mListener.onAnimationFinished(viewHolder);
            }
        }
        public void onAnimationFinished(@NonNull ViewHolder viewHolder) {
        }
        public final void dispatchAnimationStarted(@NonNull ViewHolder viewHolder) {
            onAnimationStarted(viewHolder);
        }
        public void onAnimationStarted(@NonNull ViewHolder viewHolder) {

        }
        public final boolean isRunning(@Nullable ItemAnimatorFinishedListener listener) {
            boolean running = isRunning();
            if (listener != null) {
                if (!running) {
                    listener.onAnimationsFinished();
                } else {
                    mFinishedListeners.add(listener);
                }
            }
            return running;
        }
        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder) {
            return true;
        }
        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder,
                @NonNull List<Object> payloads) {
            return canReuseUpdatedViewHolder(viewHolder);
        }
        public final void dispatchAnimationsFinished() {
            final int count = mFinishedListeners.size();
            for (int i = 0; i < count; ++i) {
                mFinishedListeners.get(i).onAnimationsFinished();
            }
            mFinishedListeners.clear();
        }
        @NonNull
        public ItemHolderInfo obtainHolderInfo() {
            return new ItemHolderInfo();
        }
        interface ItemAnimatorListener {
            void onAnimationFinished(@NonNull ViewHolder item);
        }
        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }
        public static class ItemHolderInfo {
            public int left;
            public int top;
            public int right;
            public int bottom;
            @AdapterChanges
            public int changeFlags;
            public ItemHolderInfo() {
            }
            @NonNull
            public ItemHolderInfo setFrom(@NonNull RecyclerView.ViewHolder holder) {
                return setFrom(holder, 0);
            }
            @NonNull
            public ItemHolderInfo setFrom(@NonNull RecyclerView.ViewHolder holder,
                    @AdapterChanges int flags) {
                final View view = holder.itemView;
                this.left = view.getLeft();
                this.top = view.getTop();
                this.right = view.getRight();
                this.bottom = view.getBottom();
                return this;
            }
        }
    }