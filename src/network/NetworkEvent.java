package network;

import java.util.EventObject;

public class NetworkEvent extends EventObject {
	

	private static final long serialVersionUID = 1L;
	private PackageType mPackageType;
	private int mID;
	private byte[] mData;
	
	
	public NetworkEvent(Object object, PackageType packageType, int id, byte[] data) {
		super(object);
		mPackageType = packageType;
		mID = id;
		mData = data;
	}

	
	public NetworkEvent() {
		super(null);
	}
	
	
	public String toString() {
		return getClass().getName() + "[source = " + getSource() + "]";
	}
	
	
	public PackageType getPackageType() {
		return mPackageType;
	}
	
	
	public int getID() {
		return mID;
	}
	
	
	public byte[] getData() {
		return mData;
	}
	
}
