    public static class EdgeEffectFactory {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({DIRECTION_LEFT, DIRECTION_TOP, DIRECTION_RIGHT, DIRECTION_BOTTOM})
        public @interface EdgeDirection {}
        public static final int DIRECTION_LEFT = 0;
        public static final int DIRECTION_TOP = 1;
        public static final int DIRECTION_RIGHT = 2;
        public static final int DIRECTION_BOTTOM = 3;
        protected @NonNull EdgeEffect createEdgeEffect(@NonNull RecyclerView view,
                @EdgeDirection int direction) {
            return new EdgeEffect(view.getContext());
        }
    }