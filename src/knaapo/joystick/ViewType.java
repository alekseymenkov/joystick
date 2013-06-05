package knaapo.joystick;

public enum ViewType {
	JoystickView(0),
	SeekBarView(1);

	private int mType;  

	private ViewType(int type) {  
		mType = type;  
	}  

	public int getType() {  
		return mType;  
	}  
}
