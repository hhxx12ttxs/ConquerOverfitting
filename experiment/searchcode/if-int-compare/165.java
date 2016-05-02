package com.agh.is.android.logdroid.telephony.views;

import java.util.Comparator;


final public class DisplayableComparator {
	public final static Comparator<DisplayableData> dateAscComparator = new Comparator<DisplayableData>() {
		@Override
		public int compare(DisplayableData lhs, DisplayableData rhs) {
			long lTime = lhs.getDate().getTime();
			long rTime = rhs.getDate().getTime();
			return (int)(rTime - lTime) ;
		}
	};
	
	public final static  Comparator<DisplayableData> dateDescComparator = new Comparator<DisplayableData>() {
		@Override
		public int compare(DisplayableData lhs, DisplayableData rhs) {
				long lTime = lhs.getDate().getTime();
				long rTime = rhs.getDate().getTime();
				return (int)(lTime - rTime) ;
		}
	};
	
	public final static Comparator<DisplayableData> typeComparator = new Comparator<DisplayableData>() {
		@Override
		public int compare(DisplayableData lhs, DisplayableData rhs) {
			
			return 0;
		}
	};
	
	public final static Comparator<DisplayableData> nameComparator = new Comparator<DisplayableData>() {
		@Override
		public int compare(DisplayableData lhs, DisplayableData rhs) {
			return lhs.getMainLabelText().compareTo(rhs.getMainLabelText());
		}
	};
	
	public final static Comparator<DisplayableData> dataTypeComparator = new Comparator<DisplayableData>() {
		@Override
		public int compare(DisplayableData lhs, DisplayableData rhs) {
			int leftDataType = 0;
			if (lhs.getDisplayableDataType() == DisplayableDataType.RECEIVED) {
				leftDataType = 1;
			}
			else {
				leftDataType = 0;
			}
			
			int rightDataType = 0;
			if (rhs.getDisplayableDataType() == DisplayableDataType.RECEIVED) {
				rightDataType = 1;
			}
			else {
				rightDataType = 0;
			}
			
			return leftDataType - rightDataType;
		}
	};
	 
	public static final Comparator<DisplayableData> activityTypeComparator = new Comparator<DisplayableData>() {
		@Override
		public int compare(DisplayableData lhs, DisplayableData rhs) {
			return lhs.getDisplayPriority() - rhs.getDisplayPriority();
		}
	};
}

