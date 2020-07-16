public static class LayoutParams extends android.view.ViewGroup.MarginLayoutParams {
        ViewHolder mViewHolder;
        final Rect mDecorInsets = new Rect();
        boolean mInsetsDirty = true;
        boolean mPendingInvalidate = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.LayoutParams) source);
        }
        public boolean viewNeedsUpdate() {
            return mViewHolder.needsUpdate();
        }
        public boolean isViewInvalid() {
            return mViewHolder.isInvalid();
        }
        public boolean isItemRemoved() {
            return mViewHolder.isRemoved();
        }
        public boolean isItemChanged() {
            return mViewHolder.isUpdated();
        }
        @Deprecated
        public int getViewPosition() {
            return mViewHolder.getPosition();
        }
        public int getViewLayoutPosition() {
            return mViewHolder.getLayoutPosition();
        }
        public int getViewAdapterPosition() {
            return mViewHolder.getAdapterPosition();
        }
    }