// Authors: Michael Hall and James Orr

//0x30: !0<2 byte length int><100max chars>		str INFO 
//0x31: !1<2 byte length int><100max chars>		str ERROR
//0x32: !2<4 byte int unsigned><milliseconds>	 MILLIS	
//0x33: !3<2 byte int unsigned><A/D Counts(0-1023)> POT
//0x34: !4<4 byte int unsigned><microseconds> SENSOR
//
//case INFO: // look for 2 byte length in, then add next bytes to string, checking string length
//case ERROR: // ^^
//case POT: //  FROM ARD: 2 byte unsigned int -> java int 
//case SENSOR: // FROM ARD: 4 byte unsigned long -> java long
//case MILLIS: // look for 4 byte unsigned int -> java long

//Convert sensor reading to distance
//Strings can only be in UTF-8, no 00 or \0(NULL)
package communication;

import jssc.*;

enum State {
	READ_MAGIC,
	READ_KEY,
	READ_INFO,
	READ_ERROR,
	READ_TIMESTAMP_VALUE,
	READ_POT_VALUE,
	READ_SENSOR_VALUE,
}

public class MsgReceiver {
	// Our protocol magic number and keys.  Note, these must match those we defined in the Arduino!
	final byte MAGIC_NUMBER = 0x21;
	final byte INFO_KEY = 0x30;
	final byte ERROR_KEY = 0x31;
	final byte TIMESTAMP_KEY = 0x32;
	final byte POT_KEY = 0x33;
	final byte SENSOR_KEY = 0x34;

	final private SerialComm port;
	
	public MsgReceiver(String portname) throws SerialPortException {
		port = new SerialComm(portname);
	}

	public void run() throws SerialPortException {
		// insert FSM code here to read msgs from port
		// and write to console
		
		port.setDebug(true);
		boolean textInfo = false;
		
		State state = State.READ_MAGIC;
		int index = 0;
		long value = 0;
		int potValue = 0;
		int length = 100;
		
		
		long timestamp = 0;
		String infoString = "";	
		String errorString = "";
		double sensor = 0;
		int pot = 0;

		while (true) {
			if (port.available()) {
				byte b = port.readByte();

				switch (state) {
				// Read the 1-byte header (i.e. the magic number).
				case READ_MAGIC:
					if (b == MAGIC_NUMBER) {
						state = State.READ_KEY;
					}
					break;

				// Read the key portion of the payload.
				case READ_KEY:
					// Interpret our protocol key.
					switch (b) {
					case INFO_KEY:
						state = State.READ_INFO;
						length = 0;
						index = 0;
						value = 0;
						infoString = "";
						
						break;
					case ERROR_KEY:
						state = State.READ_ERROR;
						length = 0;
						index = 0;
						value = 0;
						errorString = "";
						
						break;
					case TIMESTAMP_KEY:
						state = State.READ_TIMESTAMP_VALUE;
						index = 0;
						value = 0;
						timestamp = 0;
						
						break;
					case POT_KEY:
						state = State.READ_POT_VALUE;
						potValue = 0;
						index = 0;
						value = 0;
						
						break;
					case SENSOR_KEY:
						state = State.READ_SENSOR_VALUE;
						sensor = 0;
						index = 0;
						value = 0;
					
						break;
					default:
						state = State.READ_MAGIC;
						break;
					}
					break;

				// Read the timestamp value in our payload.
				case READ_TIMESTAMP_VALUE:
					value = (value << 8) | (b & 0xff);
					++index;
					if (index == 4) {
						// We've read all 4 bytes, so save the timestamp.  We will print it later.
						timestamp = value;
						state = State.READ_MAGIC;
					}
					break;
	
				case READ_INFO: // ArdC: first two bytes make an integer containing the length 
						// of the string. rest of the string is each character.
					if(index < 2) {
						length = (length << 8) | (b & 0xff);
						index++;
					}else if(index < (length + 2)) {
						infoString += (char) (b & 0xff); 
						index++;
					}else {
						state = State.READ_MAGIC;
					}
					break;
					
				case READ_ERROR: // ArdC: first two bytes make an integer containing the length 
							// of the string. rest of the string is each character.
					if(index < 2) {
						length = (length << 8) | (b & 0xff);
						index++;
					}else if(index < (length + 2)) {
						errorString += (char) (b & 0xFF); 
						index++;
					}else {
						state = State.READ_MAGIC;
					}		// of the string. rest of the string is each character.
					break;
					
				case READ_POT_VALUE: // ArdC: sends 2 bytes to make an unsigned integer
					
					potValue = (potValue << 8) | (b & 0xFF);
					index++;
					
					if(index == 2) {
						//System.out.printf("pot value: %d", potValue);
						state = State.READ_MAGIC;
						pot = potValue;

					}
					break;
					
				case READ_SENSOR_VALUE: // ArdC: sends 4s byte  to make unsigned long.
					value = (value << 8) | (b & 0xff);
					index++;
					
					if(index == 4) {
						//System.out.printf("sensor value: %d", value);
						System.out.println();
						state = State.READ_MAGIC;
						sensor = (double) value * .017;
						if(textInfo) {
							System.out.printf("Time: %d %s %s! Sensor Distance: %.2fcm pot: %d \n", timestamp, infoString, errorString, sensor, pot);
						}

					}
					break;
					
				default:
					state = State.READ_MAGIC;
					break;
				}
			}
			else {
				
				//Do something....
			}

			// We can do other processing here if we want since the above processing is non-blocking.
		}
	}

	public static void main(String[] args) throws SerialPortException {
		MsgReceiver msgr = new MsgReceiver("/dev/cu.usbmodem1301"); // Adjust this to be the right port for your machine
		msgr.run();
	}
}
