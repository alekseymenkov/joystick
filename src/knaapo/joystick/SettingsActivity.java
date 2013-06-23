package knaapo.joystick;


import org.apache.http.conn.util.InetAddressUtils;

import preferences.CommonPreferences;
import preferences.NetworkPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class SettingsActivity extends Activity {

    // Константы для сохранения настроек приложения
    public static final String SERVER_ADDRESS = "SERVER_ADDRESS";
    public static final String MMF_NAME = "MMF_NAME";
    public static final String LATITUDE_DEGREES = "LATITUDE_DEGREES";
    public static final String LATITUDE_MINUTUS = "LATITUDE_MINUTUS";
    public static final String LATITUDE_SECONDS = "LATITUDE_SECONDS";
    public static final String LONGITUDE_DEGREES = "LONGITUDE_DEGREES";
    public static final String LONGITUDE_MINUTUS = "LONGITUDE_MINUTUS";
    public static final String LONGITUDE_SECONDS = "LONGITUDE_SECONDS";
    public static final String HEIGHT = "HEIGHT";
    public static final String SPEED = "SPEED";
    public static final String COURSE_DEGREES = "COURSE_DEGREES";
    public static final String COURSE_MINUTUS = "COURSE_MINUTUS";
    private static final double KMH_TO_MS_COEFF = 3.6;

    // Константа для определения режима запуска окна
    final String RUN_MODE = "RUN_MODE";

    // Настройки
    SharedPreferences mPreferences;
    // Объекты для передачи настроек внутри приложения
    NetworkPreferences mNetworkPreferences;
    CommonPreferences mCommonPreferences;

    // Флаг режима запуска
    boolean mIsBlockingMode;

    // Элементы UI
    Button mButtonApply;
    Button mButtonCancel;
    Button mButtonConnect;
    EditText mEditTextServerAddress;
    EditText mEditTextMMFName;
    EditText mEditTextLatitude;
    EditText mEditTextLongitude;
    EditText mEditTextHeight;
    EditText mEditTextSpeed;
    EditText mEditTextCourse;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        mIsBlockingMode = intent.getBooleanExtra(RUN_MODE, false);

        mNetworkPreferences = new NetworkPreferences();
        mCommonPreferences = new CommonPreferences();

        mButtonApply = (Button) findViewById(R.id.buttonApply);
        mButtonApply.setOnClickListener(buttonApplyClick);

        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(buttonCancelClick);

        mButtonConnect = (Button) findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(buttonConnectClick);

        mEditTextServerAddress = (EditText) findViewById(R.id.editTextServerAddress);
        mEditTextMMFName = (EditText) findViewById(R.id.editTextFilename);
        mEditTextLatitude = (EditText) findViewById(R.id.editTextLatitude);
        mEditTextLongitude = (EditText) findViewById(R.id.editTextLongitude);
        mEditTextHeight = (EditText) findViewById(R.id.EditTextHeight);
        mEditTextSpeed = (EditText) findViewById(R.id.EditTextSpeed);
        mEditTextCourse = (EditText) findViewById(R.id.EditTextCourse);

        // Блокировка возможности смены сервера и имени отображаемого файла
        if (mIsBlockingMode) {
            mEditTextServerAddress.setEnabled(false);
            mEditTextMMFName.setEnabled(false);
            mButtonConnect.setEnabled(false);
            mEditTextLatitude.requestFocus();
        }

        loadSettings();
    };


    private OnClickListener buttonApplyClick = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (saveSettings()) {
                closeActivity(RESULT_OK);
                return;
            }
        }
    };


    private OnClickListener buttonCancelClick = new OnClickListener() {

        public void onClick(View view) {
            closeActivity(RESULT_CANCELED);
            return;
        }
    };


    private OnClickListener buttonConnectClick = new OnClickListener() {

        public void onClick(View view) {
            if (saveSettings()) {
                closeActivity(RESULT_FIRST_USER);
            }
            return;
        }
    };

    public void onBackPressed() {
        closeActivity(RESULT_CANCELED);
        return;
    }


    private void closeActivity(int code) {
        Intent intent = new Intent();
        intent.putExtra("NetworkPreferences", mNetworkPreferences);
        intent.putExtra("CommonPreferences", mCommonPreferences);
        intent.putExtra(RUN_MODE, mIsBlockingMode);
        setResult(code, intent);
        finish();
        return;
    }


    // Сохранение настроек приложения в SharedPreferences
    private boolean saveSettings() {

        // Флаг отслеживания ошибок
        boolean isNoError = true;

        // Переменные для парсинга значений из текстовых полей
        int parsingInt = 0;
        String parsingString;
        String[] parsingParts;

        // Режим работы с настройками
        mPreferences = getPreferences(MODE_PRIVATE);
        Editor settingsEditor = mPreferences.edit();

        // Адрес сервера (+ проверка на корректность)
        parsingString = mEditTextServerAddress.getText().toString();
        if (!InetAddressUtils.isIPv4Address(parsingString)) {
            mEditTextServerAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            isNoError = false;
        } else {
            mEditTextServerAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mNetworkPreferences.setServerAddress(parsingString);
            settingsEditor.putString(SERVER_ADDRESS, parsingString);
        }

        // Имя отображаемого файла (не пустое поле)
        parsingString = mEditTextMMFName.getText().toString();
        if (parsingString.isEmpty()) {
            mEditTextMMFName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            isNoError = false;
        } else {
            mEditTextMMFName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mNetworkPreferences.setMMFName(parsingString);
            settingsEditor.putString(MMF_NAME, parsingString);
        }

        // Широта (разбор по точке) + проверка на диапазон значений
        parsingString = mEditTextLatitude.getText().toString();
        parsingParts = parsingString.split("\\.");

        if (parsingParts.length != 3) {
            mEditTextLatitude.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            isNoError = false;
        } else {

            int degrees = Integer.parseInt(parsingParts[0]);
            int minutus = Integer.parseInt(parsingParts[1]);
            int seconds = Integer.parseInt(parsingParts[2]);

            boolean isDegreesCorrect = degrees >= -90 && degrees <= 90;
            boolean isMinutusCorrect = minutus >= 0 && minutus <= 59;
            boolean isSecondsCorrect = seconds >= 0 && seconds <= 59;

            if (isDegreesCorrect && isMinutusCorrect && isSecondsCorrect) {
                mEditTextLatitude.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mCommonPreferences.setLatitude(degrees, minutus, seconds);
                settingsEditor.putInt(LATITUDE_DEGREES, degrees);
                settingsEditor.putInt(LATITUDE_MINUTUS, minutus);
                settingsEditor.putInt(LATITUDE_SECONDS, seconds);
            } else {
                mEditTextLatitude.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
                isNoError = false;
            }
        }

        // Долгота (разбор по точке) + проверка на диапазон значений
        parsingString = mEditTextLongitude.getText().toString();
        parsingParts = parsingString.split("\\.");

        if (parsingParts.length != 3) {
            mEditTextLongitude.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            isNoError = false;
        }
        else {

            int degrees = Integer.parseInt(parsingParts[0]);
            int minutus = Integer.parseInt(parsingParts[1]);
            int seconds = Integer.parseInt(parsingParts[2]);

            boolean isDegreesCorrect = degrees >= -180 && degrees <= 180;
            boolean isMinutusCorrect = minutus >= 0 && minutus <= 59;
            boolean isSecondsCorrect = seconds >= 0 && seconds <= 59;

            if (isDegreesCorrect && isMinutusCorrect && isSecondsCorrect) {
                mEditTextLongitude.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mCommonPreferences.setLongitude(degrees, minutus, seconds);
                settingsEditor.putInt(LONGITUDE_DEGREES, degrees);
                settingsEditor.putInt(LONGITUDE_MINUTUS, minutus);
                settingsEditor.putInt(LONGITUDE_SECONDS, seconds);
            } else {
                mEditTextLongitude.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
                isNoError = false;
            }
        }

        // Высота самолета
        try {
            mEditTextHeight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            parsingInt = Integer.parseInt(mEditTextHeight.getText().toString());
        } catch (NumberFormatException e) {
            mEditTextHeight.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            e.printStackTrace();
            parsingInt = 0;
        }
        mCommonPreferences.setHeight(parsingInt);
        settingsEditor.putInt(HEIGHT, parsingInt);

        // Скорость
        try {
            mEditTextSpeed.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            parsingInt = Integer.parseInt(mEditTextSpeed.getText().toString());
            parsingInt /= KMH_TO_MS_COEFF;
        } catch (NumberFormatException e) {
            mEditTextSpeed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            e.printStackTrace();
            parsingInt = 0;
        }
        mCommonPreferences.setSpeed(parsingInt);
        settingsEditor.putInt(SPEED, parsingInt);

        // Курс - дробное положительное число, проверка на диапазон
        parsingString = mEditTextCourse.getText().toString();
        parsingParts = parsingString.split("\\.");

        if (parsingParts.length != 2) {
            mEditTextCourse.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
            isNoError = false;
        }
        else {
            int degrees = Integer.parseInt(parsingParts[0]);
            int minutus = Integer.parseInt(parsingParts[1]);

            boolean isDegreesCorrect = degrees >= -180 && degrees <= 180;
            boolean isMinutusCorrect = minutus >= 0 && minutus <= 59;

            if (isDegreesCorrect && isMinutusCorrect) {
                mEditTextCourse.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mCommonPreferences.setCourse(degrees, minutus);
                settingsEditor.putInt(COURSE_DEGREES, degrees);
                settingsEditor.putInt(COURSE_MINUTUS, minutus);
            } else {
                mEditTextCourse.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
                isNoError = false;
            }
        }

        if (isNoError)
            settingsEditor.apply();

        return isNoError;
    };


    // Загрузка настроек приложения из SharedPreferences
    private void loadSettings() {

        // Константа - некорректное значение.
        final int incorrectIntValue = 262144;

        // Загружаемые данные
        String loadedString = "";
        int loadedInt = 0;

        // Режим сохранения настроек
        mPreferences = getPreferences(MODE_PRIVATE);

        // Адрес сервера
        loadedString = mPreferences.getString(SERVER_ADDRESS, "");
        mNetworkPreferences.setServerAddress(loadedString);
        if (!loadedString.isEmpty())
            mEditTextServerAddress.setText(loadedString);

        // Имя отображаемого файла
        loadedString = mPreferences.getString(MMF_NAME, "");
        mNetworkPreferences.setMMFName(loadedString);
        if (!loadedString.isEmpty())
            mEditTextMMFName.setText(loadedString);

        // Широта
        loadedInt = mPreferences.getInt(LATITUDE_DEGREES, incorrectIntValue);
        if (loadedInt != incorrectIntValue) {

            int degrees = loadedInt;
            int minutus = mPreferences.getInt(LATITUDE_MINUTUS, 0);
            int seconds = mPreferences.getInt(LATITUDE_SECONDS, 0);

            mCommonPreferences.setLatitude(degrees, minutus, seconds);

            mEditTextLatitude.setText(Integer.toString(degrees));
            mEditTextLatitude.append("." + minutus);
            mEditTextLatitude.append("." + seconds);
        }

        // Долгота
        loadedInt = mPreferences.getInt(LONGITUDE_DEGREES, incorrectIntValue);
        if (loadedInt != incorrectIntValue) {

            int degrees = loadedInt;
            int minutus = mPreferences.getInt(LONGITUDE_MINUTUS, 0);
            int seconds = mPreferences.getInt(LONGITUDE_SECONDS, 0);

            mCommonPreferences.setLongitude(degrees, minutus, seconds);

            mEditTextLongitude.setText(Integer.toString(degrees));
            mEditTextLongitude.append("." + minutus);
            mEditTextLongitude.append("." + seconds);
        }

        // Высота
        loadedInt = mPreferences.getInt(HEIGHT, incorrectIntValue);
        if (loadedInt != incorrectIntValue) {
            mCommonPreferences.setHeight(loadedInt);
            mEditTextHeight.setText(Integer.toString(loadedInt));
        }

        // Скорость
        loadedInt = mPreferences.getInt(SPEED, incorrectIntValue);
        if (loadedInt != incorrectIntValue) {
            mCommonPreferences.setSpeed(loadedInt);
            mEditTextSpeed.setText(Integer.toString((int) (loadedInt * KMH_TO_MS_COEFF)));
        }

        // Курс
        loadedInt = mPreferences.getInt(COURSE_DEGREES, incorrectIntValue);
        if (loadedInt != incorrectIntValue) {

            int degrees = loadedInt;
            int minutus = mPreferences.getInt(COURSE_MINUTUS, loadedInt);

            mCommonPreferences.setCourse(degrees, minutus);

            mEditTextCourse.setText(Integer.toString(degrees));
            mEditTextCourse.append("." + minutus);
        }

        return;
    };
}