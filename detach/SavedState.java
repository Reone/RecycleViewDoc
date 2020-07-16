@RestrictTo(LIBRARY)
    public static class SavedState extends AbsSavedState {
        Parcelable mLayoutState;
        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            mLayoutState = in.readParcelable(
                    loader != null ? loader : LayoutManager.class.getClassLoader());
        }
        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(mLayoutState, 0);
        }
        void copyFrom(SavedState other) {
            mLayoutState = other.mLayoutState;
        }
        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }