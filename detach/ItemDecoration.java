    public abstract static class ItemDecoration {
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
            onDraw(c, parent);
        }
        @Deprecated
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent) {
        }
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent,
                @NonNull State state) {
            onDrawOver(c, parent);
        }
        @Deprecated
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent) {
        }
        @Deprecated
        public void getItemOffsets(@NonNull Rect outRect, int itemPosition,
                @NonNull RecyclerView parent) {
            outRect.set(0, 0, 0, 0);
        }
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                @NonNull RecyclerView parent, @NonNull State state) {
            getItemOffsets(outRect, ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition(),
                    parent);
        }
    }