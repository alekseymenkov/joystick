package network;


public enum PackageType { 
	
    Command(0),  
    RegInfo(1),  
    MMFName(2),
    Data(3),
    DescriptionRequest(4),
    DescriptionResponse(5),
    CoefficientRequest(6),
    CoefficientResponse(7),
    SingleDataRequest(8),
    SingleDataResponse(9);
   
    private int mType;  
   
    private PackageType(int type) {  
        mType = type;  
    }  
   
    public byte getType() {  
        return (byte)(mType & 0xff);
    }  
}  