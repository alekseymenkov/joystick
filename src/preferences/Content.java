package preferences;

public class Content {

    // Коэффициенты для расчета
    final static double COEFF_LATITUDE = 1.46291807926716E-09;
    final static double COEFF_LONGITUDE = 1.46291807926716E-09;
    final static double COEFF_COURSE = 1.46291807926716E-09;
    final static double DEG_TO_RAD_COEFF = 0.01745329251994328;

    // Номер режима
    byte mMode;
    boolean mIsModeChanged;
    byte mFlags;
    boolean mIsFlagsChanged;
    // Курс
    int mCourseInRad;
    boolean mIsCourseChanged;
    // Скорость
    int mSpeed;
    boolean mIsSpeedChanged;
    // Положение джойстика
    byte mX;
    boolean mIsJoystickPositionXChanged;
    byte mY;
    boolean mIsJoystickPositionYChanged;
    // Тяга
    byte mForce;
    boolean mIsForceChanged;
    // Географические координаты
    int mLatitudeInRad;
    boolean mIsLatitudeChanged;
    int mLongitudeInRad;
    boolean mIsLongitudeChanged;
    // Высота
    int mHeight;
    boolean mIsHeightChanged;


    public Content() {
        // Инициализация полей
        mMode = 0;
        mIsModeChanged = true;
        mFlags = 0;
        mIsFlagsChanged = true;

        mCourseInRad = 0;
        mIsCourseChanged = true;

        mSpeed = 0;
        mIsSpeedChanged = true;

        mX = 100; // 100 - центральное положение
        mIsJoystickPositionXChanged = true;
        mY = 100; // 100 - центральное положение
        mIsJoystickPositionYChanged = true;
        mForce = 0;
        mIsForceChanged = true;

        mLatitudeInRad = 0;
        mIsLatitudeChanged = true;
        mLongitudeInRad = 0;
        mIsLongitudeChanged = true;

        mHeight = 0;
        mIsHeightChanged = true;
    }


    public boolean isModeChanged() {
        return mIsModeChanged;
    }


    public boolean isFlagsdChanged() {
        return mIsFlagsChanged;
    }


    public boolean isCourseChanged() {
        return mIsCourseChanged;
    }


    public boolean isSpeedChanged() {
        return mIsSpeedChanged;
    }


    public boolean isJoystickPositionXChanged() {
        return mIsJoystickPositionXChanged;
    }


    public boolean isJoystickPositionYChanged() {
        return mIsJoystickPositionYChanged;
    }


    public boolean isForceChanged() {
        return mIsForceChanged;
    }


    public boolean isLatitudeChanged() {
        return mIsLatitudeChanged;
    }


    public boolean isLongitudeChanged() {
        return mIsLongitudeChanged;
    }


    public boolean isHeightChanged() {
        return mIsHeightChanged;
    }


    public void setMode(byte mode) {
        mMode = mode;
        mIsModeChanged = true;
        return;
    }


    public void setFlags(boolean isModeEnabled, boolean isRemoteControlActive, boolean isReset) {
        mFlags = 0;
        if (isModeEnabled)
            mFlags += 1;
        if (isRemoteControlActive)
            mFlags += 2;
        if (isReset)
            mFlags += 4;
        mIsFlagsChanged = true;
        return;
    }


    public byte getMode() {
        mIsModeChanged = false;
        return mMode;
    }


    public byte getFlags() {
        mIsFlagsChanged = false;
        return mFlags;
    }


    public void setCourse(int degrees, int minutus) {
        mCourseInRad = calculateCourse(degrees, minutus);
        mIsCourseChanged = true;
        return;
    }


    public int getCourse() {
        mIsCourseChanged = false;
        return mCourseInRad;
    }


    public void setSpeed(int speed) {
        mSpeed = speed;
        mIsSpeedChanged = true;
        return;
    }


    public int getSpeed() {
        mIsSpeedChanged = false;
        return mSpeed;
    }


    public void setJoystickPositionX(int x) {
        // Расчет значения координаты в соответствии с полиномиальной функцией
        mX = (byte) (8.0e-5 * Math.pow((x - 100), 3) + 2e-17 * Math.pow((x - 100), 2) + 0.2289 * (x - 100) + 9e-13);

        // Нормализация значения
        if (mX > 100)
            mX = 100;
        else if (mX < -100)
            mX = -100;

        mX += 100;

        mIsJoystickPositionXChanged = true;
        return;
    }


    public void setJoystickPositionY(int y) {
        // Расчет значения координаты в соответствии с полиномиальной функцией
        mY = (byte) (8e-5 * Math.pow((100 - y), 3) + 2e-17 * Math.pow((100 - y), 2) + 0.2289 * (100 - y) + 9e-13);

        // Нормализация значения
        if (mY > 100)
            mY = 100;
        else if (mY < -100)
            mY = -100;

        mY += 100;

        mIsJoystickPositionYChanged = true;
        return;
    }


    public void setForce(byte force) {
        mForce = force;
        mIsForceChanged = true;
        return;
    }


    public byte getJoystickPositionX() {
        mIsJoystickPositionXChanged = false;
        return mX;
    }


    public byte getJoystickPositionY() {
        mIsJoystickPositionYChanged = false;
        return mY;
    }


    public byte getForce() {
        mIsForceChanged = false;
        return mForce;
    }


    public void setLatitude(int degrees, int minutus, int seconds) {
        mLatitudeInRad = calculateLatitude(degrees, minutus, seconds);
        mIsLatitudeChanged = true;
        return;
    }


    public int getLatitude() {
        mIsLatitudeChanged = false;
        return mLatitudeInRad;
    }


    public void setLongitude(int degrees, int minutus, int seconds) {
        mLongitudeInRad = calculateLongitude(degrees, minutus, seconds);
        mIsLongitudeChanged = true;
        return;
    }


    public int getLongitude() {
        mIsLongitudeChanged = false;
        return mLongitudeInRad;
    }


    public void setHeight(int height) {
        mHeight = height;
        mIsHeightChanged = true;
        return;
    }


    public int getHeight() {
        mIsHeightChanged = false;
        return mHeight;
    }

    /***********************************************************************
     * Функция пересчета ШИРОТЫ, заданного как угол, минута, секунда в радианы
     * Входные величины
     * 	double degrees - угол ШИРОТЫ (град)
     * 	double minutus - минута
     * 	double seconds - секунта
     * Выходная величина: широта в радианах
     ************************************************************************/
    private int calculateLatitude(double degrees, double minutus, double seconds)
    {
        return (int)(((degrees + minutus / 60.0 + seconds / 3600.0) * DEG_TO_RAD_COEFF / COEFF_LATITUDE));
    }


    /***********************************************************************
     * Функция пересчета ДОЛГОТЫ, заданного как угол, минута, секунда в радианы
     * Входные величины
     * 	double degrees - угол ДОЛГОТЫ (град)
     * 	double minutus - минута
     * 	double seconds - секунта
     * Выходная величина: долгота в радианах
     ************************************************************************/
    private int calculateLongitude(double degrees, double minutus, double seconds)
    {
        return (int)(((degrees + minutus / 60.0 + seconds / 3600.0) * DEG_TO_RAD_COEFF / COEFF_LONGITUDE));
    }


    /***********************************************************************
     * Функция пересчета курса, заданного как угол, минута, секунда в радианы
     * Входные величины
     * 	 double degrees - угол курса (град)
     * 	 double minutus - минута
     * Выходная величина: курс в радианах
     ************************************************************************/
    private int calculateCourse(double degrees, double minutus)
    {
        double courseInRad = (degrees + minutus / 60.0) * DEG_TO_RAD_COEFF;
        if (courseInRad > Math.PI)
            courseInRad -= 2 * Math.PI;
        return (int)(courseInRad / COEFF_COURSE);
    }
}
