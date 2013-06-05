package network;

import java.net.Socket;
import java.io.*;

import preferences.Content;



// Класс, описывающий сетевое взаимодействие
public class Network implements Runnable  {

	// Сокет
	Socket mSocket;
	// Класс, отвечающий за отправку пакетов
	PackageSender mPackageSender;
	Thread mPackageSenderThread;
	// Класс, отвечающий за получение пакетов
	PackageReader mPackageReader;
	Thread mPackageReaderThread;

	// Данные
	Content mContent;

	// Флаг готовности к обмену
	boolean mIsReady;
	// Адрес сервера
	String mHost;
	// Порт сервера
	int mPort;

	// Имя отображаемого в память файла
	String mMMFName;

	// Слушатель изменений в выпадающем списке
	PackageReceivedListener mPackageReceivedListener;
	// Слушатель изменений состояния сокета
	NetworkStateChangeListener mNetworkStateChangeListener;

	// Данные о режимах работы (количество запрошенных, количество полученных)
	int mModesReceivedCount;
	int mModesCount;
	
	// Флаг для остановки подключения
	boolean mIsStop;


	/**
	 * Конструктор объекта клиента
	 * @param host - IP адрес или localhost или доменное имя
	 * @param port - порт, на котором висит сервер
	 */
	public Network(Content content, String host, int port) {

		// Инициализация полей
		mContent = content;
		mHost = host;
		mPort = port;
		// Значение имени MMF по умолчанию 
		mMMFName = "Global\\BINS.mkar";

		mModesCount = 0;
		mModesReceivedCount = 0;	
		
		mIsStop = false;
	}


	// Главный цикл класса
	public void run() {

		try {
			mNetworkStateChangeListener.OnChangeSocketState(SocketState.Connecting);
			mSocket = new Socket(mHost, mPort);
		} catch (Exception e) {
			e.printStackTrace();
			mNetworkStateChangeListener.OnChangeSocketState(SocketState.Disconnected);
			return;
		}

		// Обработка аварийного выхода
		if (mIsStop) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		mNetworkStateChangeListener.OnChangeSocketState(SocketState.Connected);

		mPackageSender = new PackageSender(mSocket, mContent);
		mPackageSenderThread = new Thread(mPackageSender, "sender");
		mPackageSenderThread.start();

		mPackageReader = new PackageReader(mSocket);
		mPackageReader.setNetworkListener(mNetworkListener);
		mPackageReader.setNetworkStateChangeListener(mNetworkStateChangeListener);
		mPackageReaderThread = new Thread(mPackageReader, "reader");
		mPackageReaderThread.start();

		connect();
	}


	// Подключение к указанному серверу
	public void connect() {

		if (mPackageSender == null)
			return;

		sendRegisterInfo();

		return;
	}


	private void sendRegisterInfo() {
		// Отправка имени MMF
		mPackageSender.sendMMFPackage(mMMFName);

		// Отправка регистрационных данных, куда будут записываться данных из программы
		// Регистрационные данные строго фиксированы
		int elementID = 0;
		final int recordsCount = 1;
		int[] startBytes = new int [recordsCount];
		int[] bytesCount = new int [recordsCount];

		// Номер режима: 0 - 1
		elementID = 0;
		startBytes[0] = 0;
		bytesCount[0] = 1;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);

		// Состояние режима: 1 - 1
		elementID = 1;
		startBytes[0] = 1;
		bytesCount[0] = 1;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);

		// Широта: 2 - 4
		elementID = 2;
		startBytes[0] = 2;
		bytesCount[0] = 4;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);
	
		// Долгота: 6 - 4
		elementID = 3;
		startBytes[0] = 6;
		bytesCount[0] = 4;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);
		
		// Высота: 10 - 4
		elementID = 4;
		startBytes[0] = 10;
		bytesCount[0] = 4;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);
		
		// Курс, градусы: 14 - 4
		elementID = 5;
		startBytes[0] = 14;
		bytesCount[0] = 4;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);

		// Скорость, м/c: 18 - 4
		elementID = 6;
		startBytes[0] = 18;
		bytesCount[0] = 4;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);
		
		// Джойстик, У (тангаж): 22 - 1
		elementID = 7;
		startBytes[0] = 22;
		bytesCount[0] = 1;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);

		// Джойстик, Х (крен): 23 - 1
		elementID = 8;
		startBytes[0] = 23;
		bytesCount[0] = 1;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);
		
		// Джойстик, тяга: 25 - 1
		elementID = 9;
		startBytes[0] = 25;
		bytesCount[0] = 1;
		mPackageSender.sendRegInfoPackageWithReverseMode(elementID, startBytes, bytesCount);
		
		// Флаг управления со станции: 1 - 1
		elementID = 10;
		startBytes[0] = 1;
		bytesCount[0] = 1;
		mPackageSender.sendRegInfoPackageWithDirectMode(elementID, startBytes, bytesCount);

		// Запрос информации о числе строк в списке режимов БИНС
		final int requestID = 0;
		startBytes[0] = 329;
		bytesCount[0] = 1;
		mPackageSender.sendDataRequestPackage(requestID, startBytes[0], bytesCount[0]);
	}


	// Закрытие сокета
	public synchronized void close() {
		mIsStop = true;
		try {
			if (mSocket != null && !mSocket.isClosed()) {

				mPackageReader.stop();
				mPackageSender.setIsStop(true);
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}


	// Флаг готовности к обмену
	public boolean isReady() {
		return mIsReady;
	}


	// Установка слушателя для данных о режимах работы БИНС
	public void setAdapterChangeListener(PackageReceivedListener packageReceivedListener) {
		mPackageReceivedListener = packageReceivedListener;
		return;
	}


	// Установка слушателя для отслеживания работы сети
	public void setNetworkStateChangeListener(NetworkStateChangeListener networkStateChangeListener) {
		mNetworkStateChangeListener = networkStateChangeListener;		
		return;
	}


	// Обработка событий получения данных
	NetworkListener mNetworkListener = new NetworkListener() {

		public void packageReceived(NetworkEvent networkEvent) {
			
			final int adapterData = 0;
			final int remoteControlData = 1;
			final int isRunnedData = 2;

			if (networkEvent.getPackageType() == PackageType.SingleDataResponse) {
				// Получены данные о количестве режимов работы БИНС
				if (networkEvent.getID() == 0) {
					final int startByte = 330;
					final int bytesCount = 25;

					// Количество режимов
					mModesCount = networkEvent.getData()[0];				

					// Запрос названия режимов (расположены 330 + 25 * i, где i - номер режима)
					for (int i = 0; i < mModesCount; i++)
						mPackageSender.sendDataRequestPackage(i + 1, startByte + i * bytesCount, bytesCount);
				}
				// Получено название режима БИНС
				else {
					// Добавление названия режима в список адаптера
					if (mPackageReceivedListener != null) {
						try {
							mPackageReceivedListener.OnReceivedPackage(adapterData, networkEvent.getID() + ". " + new String(networkEvent.getData(), "cp1251"));
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
					}

					mModesReceivedCount++;

					// Названия всех режимов получены
					if (mModesCount == mModesReceivedCount)
					{
						mPackageSender.sendCommandPackage(CommandPackageType.IsReady);
						mPackageSender.setIsReady(true);
					}
				}
			}
			else if (networkEvent.getPackageType() == PackageType.Data) {
				
				// Флаг управления с базовой станции
				boolean isRemoteControlActive = (networkEvent.getData()[0] & 2) > 0 ? true : false;
				mPackageReceivedListener.OnReceivedPackage(remoteControlData, isRemoteControlActive);
				
				// Состояние 
				boolean isRunned = (networkEvent.getData()[0] & 1) > 0 ? true : false;
				mPackageReceivedListener.OnReceivedPackage(isRunnedData, isRunned);
			}
		}
	};

}