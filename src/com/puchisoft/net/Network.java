package com.puchisoft.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.FrameworkMessage.Ping;

public class Network {

	static public final int port = 6464;

	// This registers objects that are going to be sent over the network.
	static public void register (EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(Ping.class);
		kryo.register(RegisterName.class);
		kryo.register(LogMessage.class);
	}
	
	static public class RegisterName {
		public String name;
	}

	static public class LogMessage {
		public String message;
		
		public LogMessage(){}
		public LogMessage(String message) {
			this.message = message;
		}

	}
}
