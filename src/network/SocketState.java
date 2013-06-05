package network;


public enum SocketState {
	
	// Ошибка при подключении
	Disconnected,
	// Подключение (в настоящий момент)
	Connecting,  
	// Подключение установлено
	Connected,
	// Соединение разорвано
	Aborted;
}
