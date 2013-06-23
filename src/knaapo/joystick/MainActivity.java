package knaapo.joystick;

import java.util.ArrayList;

import joystick.JoystickMovedListener;
import joystick.JoystickView;

import preferences.CommonPreferences;
import preferences.Content;
import preferences.NetworkPreferences;

import verticalseekbar.VerticalSeekBar;


import network.Network;
import network.NetworkStateChangeListener;
import network.PackageReceivedListener;
import network.SocketState;
import numberpicker.NumberPicker;
import numberpicker.NumberPicker.OnChangedListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;



public class MainActivity extends Activity {


    // Константа для определения режима запуска окна
	final String RUN_MODE = "RUN_MODE";
	// Константа для конфигурации UI
	final String VIEW_TYPE = "VIEW_TYPE";

    static final double KMH_TO_MS_COEFF = 3.6;
	static final int MAX_FORCE = 200;
    static final int MAX_SPEED = 650;
	static final int MAX_TANGAGE = 200;
	static final int MAX_HEELING = 200;
	static final int MIN_DEGREES = 0;
	static final int MAX_DEGREES = 359;
	static final int MIN_MINUTUS = 0;
	static final int MAX_MINUTUS = 59;

	// Конфигурация элементов упрваления
	int mViewType;

	// Сетевой класс + поток
	Network mNetwork;
	Thread mNetworkThread;

	// Объекты для передачи настроек внутри приложения
	NetworkPreferences mNetworkPreferences;
	CommonPreferences mCommonPreferences;

	// Диалоговые окна
	static ProgressDialog mConnectToServerDialog;
	static Dialog mRemoteControlDialog;
	static Dialog mProcessingOverDialog;
	
	// Контекст для работы с UI из других потоков
	static Context mMainActivityContext;

	// Настройки
	SharedPreferences mPreferences;

	// Элементы UI
	TextView mTextViewDegrees;
	TextView mTextViewMinutus;
	static VerticalSeekBar mSeekBarForce;
	VerticalSeekBar mSeekBarTangage;
	SeekBar mSeekBarHeeling;
	static JoystickView mJoystickView;
	static Spinner mSpinner;
	static ToggleButton mToggleButton;
	NumberPicker mNumberPickerDegrees;
	NumberPicker mNumberPickerMinutus;
	LinearLayout mMainLayout;
	Button mResetButton;
	Button mViewButton;

	static boolean mIsFirstRun;
	static boolean mIsConnected;

	// Адаптер для выпадающего списка
	static ArrayAdapter<String> mArrayAdapter;
	static ArrayList<String> mModesList;
	static Content mContent;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mIsFirstRun = true;
		mIsConnected = false;

		// Данные
		mContent = new Content();

		// Сеть
		mNetwork = null;

		// Сохранение контекста главного окна для работа с элементами UI из параллельных потоков
		mMainActivityContext = this;

		// Диалог ожидания подключения к серверу
		mConnectToServerDialog = new ProgressDialog(this);
		mConnectToServerDialog.setTitle("Подключение к серверу");
		mConnectToServerDialog.setMessage("Выполняется подключение к серверу...");
		mConnectToServerDialog.setButton(Dialog.BUTTON_NEGATIVE, "Отмена", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				mIsConnected = false;
				disconnectFromServer();
			}
		});
		mConnectToServerDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				mIsConnected = false;
				disconnectFromServer();
			}
		});

		// Диалог предупреждения об управлении с базовой станции
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Внимание!");
		adb.setMessage("Управление передано главному приложению!");
		adb.setIcon(android.R.drawable.ic_dialog_info);
		adb.setNegativeButton("Выход", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
				return;					
			}
		});

		// Создание диалога
		mRemoteControlDialog = adb.create();

		// Обработчик отмены диалога через кнопку Back
		mRemoteControlDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});

		adb = new AlertDialog.Builder(this);
		adb.setTitle("Внимание!");
		adb.setMessage("Расчет на станции завершен!");
		adb.setIcon(android.R.drawable.ic_dialog_info);
		adb.setNegativeButton("Ок", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				return;					
			}
		});

		mProcessingOverDialog = adb.create();
		
		// Список режимо работы ПО
		mModesList = new ArrayList<String>();
		// Адаптер для mModesList
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mModesList);
		mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Инициализация UI
		mTextViewDegrees = (TextView) findViewById(R.id.textViewDegrees);
		mTextViewMinutus = (TextView) findViewById(R.id.textViewMinutus);

		mSeekBarForce = (VerticalSeekBar) findViewById(R.id.seekBarForce);
		mSeekBarForce.setOnSeekBarChangeListener(onSeekBarChange);
		mSeekBarForce.setMax(MAX_FORCE);
		mSeekBarForce.setProgress(0);

        int margin = (int) getResources().getDimension(R.dimen.commonValue);
		mSeekBarTangage = new VerticalSeekBar(this);
		mSeekBarTangage.setProgressDrawable(getResources().getDrawable(android.R.drawable.progress_indeterminate_horizontal));
		mSeekBarTangage.setMax(MAX_TANGAGE);
		mSeekBarTangage.setProgress(MAX_TANGAGE / 2);
		mSeekBarTangage.setOnSeekBarChangeListener(onSeekBarChange);
		LinearLayout.LayoutParams tangageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
        tangageParams.setMargins(margin, margin, margin, 0);
		mSeekBarTangage.setLayoutParams(tangageParams);

		mSeekBarHeeling = new SeekBar(this);
		mSeekBarHeeling.setProgressDrawable(getResources().getDrawable(android.R.drawable.progress_indeterminate_horizontal));
		mSeekBarHeeling.setMax(MAX_HEELING);
		mSeekBarHeeling.setProgress(MAX_HEELING / 2);
		mSeekBarHeeling.setOnSeekBarChangeListener(onSeekBarChange);
		LinearLayout.LayoutParams heelingParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		heelingParams.setMargins(margin, margin, margin, margin);
		mSeekBarHeeling.setLayoutParams(heelingParams);

		mToggleButton = (ToggleButton) findViewById(R.id.toggleBtnMode);
		mToggleButton.setOnCheckedChangeListener(mOnCheckedChangeListener);

		mResetButton = (Button) findViewById(R.id.btnReset);
		mResetButton.setOnClickListener(mOnResetListener);

		mViewButton = (Button) findViewById(R.id.btnView);
		mViewButton.setOnClickListener(mOnViewChangedListener);

		mNumberPickerDegrees = (NumberPicker) findViewById(R.id.numberPickerDegrees);
		mNumberPickerDegrees.setOnChangeListener(mOnNumberPickerValueChangeListener);
		mNumberPickerDegrees.setRange(MIN_DEGREES, MAX_DEGREES);
		mNumberPickerDegrees.setCurrentAndNotify(MIN_DEGREES);

		mNumberPickerMinutus = (NumberPicker) findViewById(R.id.numberPickerMinutus);
		mNumberPickerMinutus.setOnChangeListener(mOnNumberPickerValueChangeListener);
		mNumberPickerMinutus.setRange(MIN_MINUTUS, MAX_MINUTUS);
		mNumberPickerMinutus.setCurrentAndNotify(MIN_MINUTUS);

		mJoystickView = new JoystickView(this);
		mJoystickView.setOnJostickMovedListener(joystickListener);

		mSpinner = (Spinner) findViewById(R.id.spinnerMode);
		mSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
		mSpinner.setPrompt("Режим работы");
		mSpinner.setAdapter(mArrayAdapter);

		mMainLayout = (LinearLayout) findViewById(R.id.layoutContent);

		loadSettings();

		// Генерация вида
		createView(mViewType);


		// Вызов окна с настройками
		final boolean isBlockingMode = false;
		startSettingsActivity(isBlockingMode);
	}


	private void createView(int viewType) {

		mMainLayout.removeAllViews();

		if (ViewType.JoystickView.getType() == viewType) {
			mMainLayout.addView(mJoystickView);
		} else if (ViewType.SeekBarView.getType() == viewType) {
            mMainLayout.addView(mSeekBarHeeling);
			mMainLayout.addView(mSeekBarTangage);
		}

		return;
	}








































































	private android.view.View.OnClickListener mOnViewChangedListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final int viewsCount = 2;
			mViewType = (mViewType + 1) % viewsCount;

			saveSettings();

			createView(mViewType);

			return;
		}
	};

	
	private void saveSettings() {
		// Режим работы с настройками
		mPreferences = getPreferences(MODE_PRIVATE);
		Editor editor = mPreferences.edit();
		editor.putInt(VIEW_TYPE, mViewType);
		editor.apply();
	}
	
	
	private void loadSettings() {
		// Режим сохранения настроек
		mPreferences = getPreferences(MODE_PRIVATE);
		mViewType = mPreferences.getInt(VIEW_TYPE, ViewType.JoystickView.getType());
	}
	

	private android.view.View.OnClickListener mOnResetListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mContent.setFlags(mToggleButton.isChecked(), mRemoteControlDialog.isShowing(), true);
            mSpinner.setEnabled(!mToggleButton.isChecked());
			return;
		}
	};


	private void startSettingsActivity(boolean mode) {
		Intent settingsIntent = new Intent(mSpinner.getContext(), SettingsActivity.class);
		settingsIntent.putExtra(RUN_MODE, mode);
		startActivityForResult(settingsIntent, 0);
		return;
	}


	// Подключение к серверу
	private void connectToServer(String serverAddress, int port) {

		disconnectFromServer();

		mNetwork = new Network(mContent, serverAddress, port);
		mNetwork.setAdapterChangeListener(mPackageReceivedListener);
		mNetwork.setNetworkStateChangeListener(mNetworkStateChangeListener);

		mNetworkThread = new Thread(mNetwork, "network");
		mNetworkThread.start();

		return;
	}


	// Отключение от сервера
	private void disconnectFromServer() {
		if (mNetwork != null)
			mNetwork.close();
		mNetwork = null;
		return;
	}


	// Обработчик сообщений о изменении списка режимов БИНС
	static Handler mPackageReceivedHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			final int adapterData = 0;
			final int remoteControlData = 1;
			final int isRunnedData = 2;

			switch (msg.what) {
			case adapterData:
				mArrayAdapter.add(msg.obj.toString());
				mArrayAdapter.notifyDataSetChanged();
				break;
			case remoteControlData:
				if ((Boolean)msg.obj) {
					mRemoteControlDialog.show();
				} else {
					mRemoteControlDialog.dismiss();	
				}
				break;
			case isRunnedData:
				if (mToggleButton.isChecked() && (Boolean)msg.obj == false) {
                    setDefaultForce();
					mProcessingOverDialog.show();
				}
				mToggleButton.setChecked((Boolean)msg.obj);
                mSpinner.setEnabled(!(Boolean)msg.obj);

			}

			return;
		};
	};


	// Обработчик сообщений о состоянии подключения
	static Handler mNetworkStateChangeHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			SocketState state = (SocketState)msg.obj;
			switch (state) {
			case Connecting:
				if (mConnectToServerDialog != null && !mConnectToServerDialog.isShowing())
					mConnectToServerDialog.show();
				mIsConnected = false;
				break;
			case Disconnected:
				if (mConnectToServerDialog != null && mConnectToServerDialog.isShowing())
					mConnectToServerDialog.dismiss();
				mIsConnected = false;
                setDefaultForce();
				mToggleButton.setChecked(false);
                mSpinner.setEnabled(true);
				Toast.makeText(mMainActivityContext, R.string.connection_fail, Toast.LENGTH_LONG).show();
				break;
			case Connected:
				if (mConnectToServerDialog != null && mConnectToServerDialog.isShowing())
					mConnectToServerDialog.dismiss();
				mIsConnected = true;
				mIsFirstRun = true;
				Toast.makeText(mMainActivityContext, R.string.connection_success, Toast.LENGTH_LONG).show();
				break;
			case Aborted:
				mArrayAdapter.clear();
				mArrayAdapter.notifyDataSetChanged();
				mIsConnected = false;
                setDefaultForce();
				mToggleButton.setChecked(false);
                mSpinner.setEnabled(true);
				Toast.makeText(mMainActivityContext, R.string.connection_aborted, Toast.LENGTH_LONG).show();
				break;
			}
		};
	};


	// Слушатель изменений в NumberPicker (градусы и минуты)
	OnChangedListener mOnNumberPickerValueChangeListener = new OnChangedListener() {

		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {

			switch(picker.getId()) {
			case R.id.numberPickerDegrees:
				mContent.setCourse(newVal, (mNumberPickerMinutus == null ? 0 : mNumberPickerMinutus.getCurrent()));
				mTextViewDegrees.setText(Integer.toString(newVal) + getString(R.string.degreesSymbol));
				break;
			case R.id.numberPickerMinutus:
				mContent.setCourse((mNumberPickerDegrees == null ? 0 : mNumberPickerDegrees.getCurrent()), newVal);
				mTextViewMinutus.setText(Integer.toString(newVal) + getString(R.string.minutusSymbol));
				break;
			}

			return;
		}
	};


	// Генерация меню
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	};


	// Модификация пункта меню "Подключить / Отключить"
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem item = menu.findItem(R.id.menu_connection);

		if (mIsConnected) {
			item.setTitle(R.string.menu_disconnect);
		} else {
			item.setTitle(R.string.menu_connect);
		}

		return super.onPrepareOptionsMenu(menu);
	}


	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_connection:
			if (mIsConnected) {
				disconnectFromServer();
				// Вывод сообщения о принудительном разрыве связи
				Message msg = mNetworkStateChangeHandler.obtainMessage(0, SocketState.Aborted);
				mNetworkStateChangeHandler.sendMessage(msg);
			} else {
				boolean isBlockingMode = false;
				startSettingsActivity(isBlockingMode);
			}
			break;
		case R.id.menu_settings:
			boolean isBlockingMode = true;
			startSettingsActivity(isBlockingMode);
			break;
		case R.id.menu_exit:
			finish();
		}

		return super.onOptionsItemSelected(item);
	}


	// Событие перемещения джойстика
	private JoystickMovedListener joystickListener = new JoystickMovedListener() {

		public void OnMoved(int x, int y) {
			mContent.setJoystickPositionX(x);
			mContent.setJoystickPositionY(y);
			mSeekBarHeeling.setProgress(x);
			mSeekBarTangage.setProgress(y);
			return;
		}
	};


	// Слушатель изменений данных в адаптере
	PackageReceivedListener mPackageReceivedListener = new PackageReceivedListener() {

		public void OnReceivedPackage(int what, Object item) {
			final int adapterData = 0;
			final int remoteControlData = 1;
			final int isRunnedData = 2;

			Message msg;

			switch (what)
			{
			case adapterData:
				msg = mPackageReceivedHandler.obtainMessage(adapterData, item);
				mPackageReceivedHandler.sendMessage(msg);
				break;
			case remoteControlData:
				msg = mPackageReceivedHandler.obtainMessage(remoteControlData, item);
				mPackageReceivedHandler.sendMessage(msg);
				break;
			case isRunnedData:
				msg = mPackageReceivedHandler.obtainMessage(isRunnedData, item);
				mPackageReceivedHandler.sendMessage(msg);
				break;
			}

		}
	};


	// Слушатель выбора режима работы БИНС
	OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {

			mContent.setMode((byte)(position + 1));
			mContent.setFlags(false, mRemoteControlDialog.isShowing(), false);
			mToggleButton.setChecked(false);
            mSpinner.setEnabled(true);

			if (!mIsFirstRun) {
				final boolean isBlockingMode = true;
				Intent settingsIntent = new Intent(mSpinner.getContext(), SettingsActivity.class);
				settingsIntent.putExtra(RUN_MODE, isBlockingMode);
				startActivityForResult(settingsIntent, 0);
			}

			mIsFirstRun = false;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			return;
		}

	};


	// Событие возврата с альтернативной Activity
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		mNetworkPreferences = (NetworkPreferences)data.getParcelableExtra("NetworkPreferences");
		mCommonPreferences = (CommonPreferences)data.getParcelableExtra("CommonPreferences");

		// Копируем настройки во промежуточную память (Content)
		// Широта
		mContent.setLatitude(mCommonPreferences.getLatitudeDegrees(),
				mCommonPreferences.getLatitudeMinutus(), mCommonPreferences.getLatitudeSeconds());
		// Долгота
		mContent.setLongitude(mCommonPreferences.getLongitudeDegrees(),
				mCommonPreferences.getLongitudeMinutus(), mCommonPreferences.getLongitudeSeconds());
		// Высота
		mContent.setHeight(mCommonPreferences.getHeight());
		// Скорость
		mContent.setSpeed(mCommonPreferences.getSpeed());
		// Курс
		mContent.setCourse(mCommonPreferences.getCourseDegrees(), mCommonPreferences.getCourseMinutus());

		// Устанавливаем стартовый курс для элементов управления в главном окне
		mNumberPickerDegrees.setCurrent(mCommonPreferences.getCourseDegrees());
		mTextViewDegrees.setText(Integer.toString(mCommonPreferences.getCourseDegrees()) + getString(R.string.degreesSymbol));
		mNumberPickerMinutus.setCurrent(mCommonPreferences.getCourseMinutus());
		mTextViewMinutus.setText(Integer.toString(mCommonPreferences.getCourseMinutus()) + getString(R.string.minutusSymbol));

        setDefaultForce();

		// Подключаемся, если в настройках была возможность указать адрес и порт сервера
		if (resultCode == RESULT_FIRST_USER) {
			boolean isBlockingMode = false;
			isBlockingMode = data.getBooleanExtra(RUN_MODE, isBlockingMode);
			if (!isBlockingMode)
				connectToServer(mNetworkPreferences.getServerAddress(), mNetworkPreferences.getPort());
		}

		return;
	}


	// Слушатель изменения положения SeekBar
	OnSeekBarChangeListener onSeekBarChange = new OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean flag) {

			if (seekBar.equals(mSeekBarForce)) {
				mContent.setForce((byte)progress);
			} else if (seekBar.equals(mSeekBarTangage)) {
				mContent.setJoystickPositionY(progress);
				mJoystickView.setPositionY(progress);
			} else if (seekBar.equals(mSeekBarHeeling)) {
				mContent.setJoystickPositionX(progress);
				mJoystickView.setPositionX(progress);
			}
		}

		public void onStartTrackingTouch(SeekBar seekbar) {
			return;
		}

		public void onStopTrackingTouch(final SeekBar seekBar) {
			if (seekBar.getId() != R.id.seekBarForce) {
				Handler handler = new Handler();
				final int numberOfFrames = 4;
				final int intervalsX = (100 - seekBar.getProgress()) / numberOfFrames;

				for (int i = 0; i < numberOfFrames + 1; i++) {
					final int runNumber = i;
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (runNumber == numberOfFrames)
								seekBar.setProgress(100);
							else
								seekBar.setProgress(seekBar.getProgress() + intervalsX);
						}
					}, runNumber * 40);


				}
			}

		}

	};


	// Слушатель состояния сети
	NetworkStateChangeListener mNetworkStateChangeListener = new NetworkStateChangeListener() {

		public void OnChangeSocketState(SocketState state) {

			Message msg = mNetworkStateChangeHandler.obtainMessage(0, state);
			mNetworkStateChangeHandler.sendMessage(msg);

		}
	};



	// Кнопка активации режима работы БИНС
	OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mContent.setMode((byte)(mSpinner.getSelectedItemPosition() + 1));
			mContent.setFlags(isChecked, mRemoteControlDialog.isShowing(), false);
            setDefaultForce();
			return;
		}
	};

    private static void setDefaultForce() {
        int speed = mContent.getSpeed();
        int force = (int) (speed / KMH_TO_MS_COEFF * MAX_FORCE / MAX_SPEED);
        mSeekBarForce.setProgress(force);
        mContent.setForce((byte) force);
        mContent.setSpeed(speed);
    }


    // Событие завершения работы приложения
	protected void onDestroy() {

		disconnectFromServer();
		
		super.onDestroy();
		return;

	};

}
