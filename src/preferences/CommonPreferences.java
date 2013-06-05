package preferences;

import android.os.Parcel;
import android.os.Parcelable;


public class CommonPreferences implements Parcelable {

	int mLatitudeDegrees;
	int mLatitudeMinutus;
	int mLatitudeSeconds;
	int mLongitudeDegrees;
	int mLongitudeMinutus;
	int mLongitudeSeconds;
	int mHeight;
	int mSpeed;
	int mCourseDegrees;
	int mCourseMinutus;


	public CommonPreferences() {
		mLatitudeDegrees = 0;
		mLatitudeMinutus = 0;
		mLatitudeSeconds = 0;
		mLongitudeDegrees = 0;
		mLongitudeMinutus = 0;
		mLongitudeSeconds = 0;
		mHeight = 0;
		mSpeed = 0;
		mCourseDegrees = 0;
		mCourseMinutus = 0;
	}


	private CommonPreferences(Parcel parcel) {
		mLatitudeDegrees = parcel.readInt();
		mLatitudeMinutus = parcel.readInt();
		mLatitudeSeconds = parcel.readInt();
		mLongitudeDegrees = parcel.readInt();
		mLongitudeMinutus = parcel.readInt();
		mLongitudeSeconds = parcel.readInt();
		mHeight = parcel.readInt();
		mSpeed = parcel.readInt();
		mCourseDegrees = parcel.readInt();
		mCourseMinutus = parcel.readInt();
	}


	public void setLatitude(int degrees, int minutus, int seconds) {
		mLatitudeDegrees = degrees;
		mLatitudeMinutus = minutus;
		mLatitudeSeconds = seconds;
		return;
	}


	public int getLatitudeDegrees() {
		return mLatitudeDegrees;
	}


	public int getLatitudeMinutus() {
		return mLatitudeMinutus;
	}


	public int getLatitudeSeconds() {
		return mLatitudeSeconds;
	}


	public void setLongitude(int degrees, int minutus, int seconds) {
		mLongitudeDegrees = degrees;
		mLongitudeMinutus = minutus;
		mLongitudeSeconds = seconds;
		return;
	}


	public int getLongitudeDegrees() {
		return mLongitudeDegrees;
	}


	public int getLongitudeMinutus() {
		return mLongitudeMinutus;
	}


	public int getLongitudeSeconds() {
		return mLongitudeSeconds;
	}


	public void setHeight(int height) {
		mHeight = height;
		return;
	}


	public int getHeight() {
		return mHeight;
	}


	public void setSpeed(int speed) {
		mSpeed = speed;
		return;
	}


	public int getSpeed() {
		return mSpeed;
	}


	public void setCourse(int degrees, int minutus) {
		mCourseDegrees = degrees;
		mCourseMinutus = minutus;
		return;
	}


	public int getCourseDegrees() {
		return mCourseDegrees;
	}


	public int getCourseMinutus() {
		return mCourseMinutus;
	}

	
	public int describeContents() {
		return 0;
	}


	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(mLatitudeDegrees);
		parcel.writeInt(mLatitudeMinutus);
		parcel.writeInt(mLatitudeSeconds);
		parcel.writeInt(mLongitudeDegrees);
		parcel.writeInt(mLongitudeMinutus);
		parcel.writeInt(mLongitudeSeconds);
		parcel.writeInt(mHeight);
		parcel.writeInt(mSpeed);
		parcel.writeInt(mCourseDegrees);
		parcel.writeInt(mCourseMinutus);
		return;
	}


	public static final Parcelable.Creator<CommonPreferences> CREATOR =
			new Parcelable.Creator<CommonPreferences>() {

		public CommonPreferences createFromParcel(Parcel in) {
			return new CommonPreferences(in);
		}

		public CommonPreferences[] newArray(int size) {
			return new CommonPreferences[size];
		}
	};
}
