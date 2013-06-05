package network;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class PackageReader implements Runnable {

    // Размер сегмента - 1 байт
    final int PACKAGE_SIZE_1BYTE = 1;
    // Размер сегмента - 2 байта
    final int PACKAGE_SIZE_2BYTES = 2;
    // Частота передачи данных
    final int SEND_TIMEOUT = 1000;
    // Режим передачи - прямой
    final int DIRECT_SEND_MODE = 0;
    // Режим передачи - обратный
    final int REVERSE_SEND_MODE = 1;

    // Слушатель событий сети
    private NetworkListener mNetworkListener;
    // Слушатель состояния сокета
    private NetworkStateChangeListener mNetworkStateChangeListener;

    // Сокет
    private Socket mSocket;
    // Поток
    InputStream mInputStream;

    // Флаг остановки
    private volatile boolean mIsStop = false;


    public PackageReader(Socket socket) {
        mSocket = socket;
    }


    // Основной цикл
    public void run() {

        if (mIsStop)
            return;

        boolean isNoError = true;

        try {
            // Поток получения данных
            mInputStream = mSocket.getInputStream();

            // Буфер для парсинга пакетов
            byte[] processedBuffer = new byte [512];
            // Количество данных в буфере для разбора пакетов
            int processedBufferLenght = 0;
            // Буфер для обработки входящих данных
            byte[] buffer = new byte [128];
            // Количество данных в буфере входящих данных
            int bufferLenght = 0;

            while (true)  {

                // Количество считанных данных (в байтах)
                bufferLenght = mInputStream.read(buffer);
                if (bufferLenght > 0) {
                    System.arraycopy(buffer, 0, processedBuffer, processedBufferLenght, bufferLenght);
                    processedBufferLenght += bufferLenght;
                } else if (bufferLenght < 0) {
                    isNoError = false;
                }

                while (isNoError) {

                    // Проверка на условие: приниято мало данных
                    if (processedBufferLenght < PACKAGE_SIZE_2BYTES)
                        break;

                    // Проверка на условие: принят ли весь пакет целиком?
                    short packageSize = byteToShort(processedBuffer[1], processedBuffer[0]);
                    if (processedBufferLenght < packageSize)
                        break;

                    // Получение типа пакета
                    byte packageType = processedBuffer[PACKAGE_SIZE_2BYTES];

                    // Получение содержимого пакета в packageContent
                    byte[] packageContent = new byte [packageSize - PACKAGE_SIZE_2BYTES - PACKAGE_SIZE_1BYTE];
                    System.arraycopy(processedBuffer, PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE,
                            packageContent, 0,
                            packageSize - PACKAGE_SIZE_2BYTES - PACKAGE_SIZE_1BYTE);

                    // Модификация размера данных для обработки
                    processedBufferLenght -= packageSize;
                    // Удаление обработанных данных
                    System.arraycopy(processedBuffer, packageSize, processedBuffer, 0, processedBufferLenght);

                    // Парсинг полученного пакета
                    if (PackageType.Command.getType() == packageType)
                        isNoError = parseCommandPackage(packageContent);
                    else if (PackageType.SingleDataResponse.getType() == packageType)
                        isNoError = parseSingleDataResponsePackage(packageContent);
                    else if (PackageType.Data.getType() == packageType)
                        isNoError = parseDataPackage(packageContent);

                    // Во время парсинга возникли ошибки
                    if (!isNoError)
                        break;
                }

                if (!isNoError)
                {
                    mInputStream.close();
                    shotSocketChanged(SocketState.Aborted);
                    break;
                }
            }
        }
        catch(IOException e) {
            isNoError = false;
            e.printStackTrace();
            shotSocketChanged(SocketState.Aborted);
        }
    }


    // Разбор пакета с управляющими данными
    private boolean parseCommandPackage(byte[] packageContent) {

        shotPackageReceived(PackageType.Command, 0, packageContent);
        return true;
    }


    // Разбор пакета с данными
    private boolean parseSingleDataResponsePackage(byte[] packageContent) {

        byte[] packageData = new byte [packageContent.length - PACKAGE_SIZE_2BYTES];
        System.arraycopy(packageContent, PACKAGE_SIZE_2BYTES,
                packageData, 0,
                packageContent.length - PACKAGE_SIZE_2BYTES);
        int id = byteToShort(packageContent[1], packageContent[0]);

        shotPackageReceived(PackageType.SingleDataResponse, id, packageData);

        return true;
    }


    // Разбор пакета с периодическими данными
    private boolean parseDataPackage(byte[] packageContent) {

        byte[] packageData = new byte [packageContent.length - PACKAGE_SIZE_2BYTES];
        System.arraycopy(packageContent, PACKAGE_SIZE_2BYTES,
                packageData, 0,
                packageContent.length - PACKAGE_SIZE_2BYTES);
        int id = byteToShort(packageContent[1], packageContent[0]);

        shotPackageReceived(PackageType.Data, id, packageData);

        return true;
    }


    // Преобразование типов (byte -> short)
    private short byteToShort(byte high, byte low)
    {
        short value = (short)(((short)high << 8) | (short)low);
        return value;
    }


    public void setNetworkListener(NetworkListener listener) {

        mNetworkListener = listener;
        return;
    }


    public void setNetworkStateChangeListener(NetworkStateChangeListener listener) {

        mNetworkStateChangeListener = listener;
        return;
    }


    protected void shotPackageReceived(PackageType packageType, int id, byte[] data) {

        NetworkEvent event = new NetworkEvent(this, packageType, id, data);
        mNetworkListener.packageReceived(event);
        return;
    }


    protected void shotSocketChanged(SocketState socketState) {

        if (mNetworkStateChangeListener != null)
            mNetworkStateChangeListener.OnChangeSocketState(socketState);
        return;
    }


    public void stop() {

        mIsStop = true;
        return;
    }
}