    private class ItemAnimatorRestoreListener implements ItemAnimator.ItemAnimatorListener {
        ItemAnimatorRestoreListener() {
        }
        @Override
        public void onAnimationFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.mShadowedHolder != null && item.mShadowingHolder == null) { // old vh
                item.mShadowedHolder = null;
            }
            item.mShadowingHolder = null;
            if (!item.shouldBeKeptAsChild()) {
                if (!removeAnimatingView(item.itemView) && item.isTmpDetached()) {
                    removeDetachedView(item.itemView, false);
                }
            }
        }
    }