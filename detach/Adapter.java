public abstract static class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();
        private boolean mHasStableIds = false;
        @NonNull
        public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
        public abstract void onBindViewHolder(@NonNull VH holder, int position);
        public void onBindViewHolder(@NonNull VH holder, int position,
                @NonNull List<Object> payloads) {
            onBindViewHolder(holder, position);
        }
        @NonNull
        public final VH createViewHolder(@NonNull ViewGroup parent, int viewType) {
            try {
                TraceCompat.beginSection(TRACE_CREATE_VIEW_TAG);
                final VH holder = onCreateViewHolder(parent, viewType);
                if (holder.itemView.getParent() != null) {
                    throw new IllegalStateException("ViewHolder views must not be attached when"
                            + " created. Ensure that you are not passing 'true' to the attachToRoot"
                            + " parameter of LayoutInflater.inflate(..., boolean attachToRoot)");
                }
                holder.mItemViewType = viewType;
                return holder;
            } finally {
                TraceCompat.endSection();
            }
        }
        public final void bindViewHolder(@NonNull VH holder, int position) {
            holder.mPosition = position;
            if (hasStableIds()) {
                holder.mItemId = getItemId(position);
            }
            holder.setFlags(ViewHolder.FLAG_BOUND,
                    ViewHolder.FLAG_BOUND | ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID
                            | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN);
            TraceCompat.beginSection(TRACE_BIND_VIEW_TAG);
            onBindViewHolder(holder, position, holder.getUnmodifiedPayloads());
            holder.clearPayload();
            final ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof RecyclerView.LayoutParams) {
                ((LayoutParams) layoutParams).mInsetsDirty = true;
            }
            TraceCompat.endSection();
        }
        public int getItemViewType(int position) {
            return 0;
        }
        public void setHasStableIds(boolean hasStableIds) {
            if (hasObservers()) {
                throw new IllegalStateException("Cannot change whether this adapter has "
                        + "stable IDs while the adapter has registered observers.");
            }
            mHasStableIds = hasStableIds;
        }
        public long getItemId(int position) {
            return NO_ID;
        }
        public abstract int getItemCount();
        public final boolean hasStableIds() {
            return mHasStableIds;
        }
        public void onViewRecycled(@NonNull VH holder) {
        }
        public boolean onFailedToRecycleView(@NonNull VH holder) {
            return false;
        }
        public void onViewAttachedToWindow(@NonNull VH holder) {
        }
        public void onViewDetachedFromWindow(@NonNull VH holder) {
        }
        public final boolean hasObservers() {
            return mObservable.hasObservers();
        }
        public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            mObservable.registerObserver(observer);
        }
        public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            mObservable.unregisterObserver(observer);
        }
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        }
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        }
        public final void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }
        public final void notifyItemChanged(int position) {
            mObservable.notifyItemRangeChanged(position, 1);
        }
        public final void notifyItemChanged(int position, @Nullable Object payload) {
            mObservable.notifyItemRangeChanged(position, 1, payload);
        }
        public final void notifyItemRangeChanged(int positionStart, int itemCount) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount);
        }
        public final void notifyItemRangeChanged(int positionStart, int itemCount,
                @Nullable Object payload) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount, payload);
        }
        public final void notifyItemInserted(int position) {
            mObservable.notifyItemRangeInserted(position, 1);
        }
        public final void notifyItemMoved(int fromPosition, int toPosition) {
            mObservable.notifyItemMoved(fromPosition, toPosition);
        }
        public final void notifyItemRangeInserted(int positionStart, int itemCount) {
            mObservable.notifyItemRangeInserted(positionStart, itemCount);
        }
        public final void notifyItemRemoved(int position) {
            mObservable.notifyItemRangeRemoved(position, 1);
        }
        public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mObservable.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }