package preferences;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkPreferences implements Parcelable {

	private String mServerAddress;
	private int mPort;
	private String mMMFName;


	public NetworkPreferences() {
		mServerAddress = "192.168.0.1";
		mPort = 5525;
		mMMFName = "Global\\BINS.mkar";
	}


	public NetworkPreferences(Parcel parcel) {
		mServerAddress = parcel.readString();
		mPort = parcel.readInt();
		mMMFName = parcel.readString();
	}
	
	
	public void setServerAddress(String serverAddress) {
		mServerAddress = serverAddress;
		return;
	}

	
	public void setPort(int port) {
		mPort = port;
		return;
	}
	
	
	public String getServerAddress() {
		return mServerAddress;
	}
	
	
	public int getPort() {
		return mPort;
	}
	
	
	public void setMMFName(String mmfName) {
		mMMFName = mmfName;
		return;
	}
	
	
	public String getMMFName() {
		return mMMFName;
	}


	public int describeContents() {
		return 0;
	}


	public void writeToParcel(Parcel parcel, int flags) {

		parcel.writeString(mServerAddress);
		parcel.writeInt(mPort);
		parcel.writeString(mMMFName);
		return;
	}


	public static final Parcelable.Creator<NetworkPreferences> CREATOR =
			new Parcelable.Creator<NetworkPreferences>() {

		public NetworkPreferences createFromParcel(Parcel in) {
			return new NetworkPreferences(in);
		}

		public NetworkPreferences[] newArray(int size) {
			return new NetworkPreferences[size];
		}
	};
}
