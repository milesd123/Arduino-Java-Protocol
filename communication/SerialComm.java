package communication;

import jssc.*;

public class SerialComm {

	SerialPort port;

	private boolean debug;  // Indicator of "debugging mode"
	
	// This function can be called to enable or disable "debugging mode"
	void setDebug(boolean mode) {
		debug = mode;
	}	
	

	// Constructor for the SerialComm class
	public SerialComm(String name) throws SerialPortException {
		port = new SerialPort(name);		
		port.openPort();
		port.setParams(SerialPort.BAUDRATE_9600,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
		
		debug = false; // Default is to NOT be in debug mode
	}
		
	// TODO: Add writeByte() method to write data to serial port
	public void writeByte(byte theByte) throws SerialPortException {
		port.writeByte(theByte);
		if (debug) {
			System.out.print("<" + getHex(theByte) + ">");
		}
	}
	
	public String getHex(byte value) {
		return String.format("%02x", value);
	}
	
	public byte readByte() throws SerialPortException{
		
		byte inputByte = port.readBytes(1)[0];
	
		if (debug) {	
			System.out.print("[" + getHex(inputByte) + "]");
		}
		
		
		
		return inputByte;
	}

	public boolean available() throws SerialPortException{
		if(port.getInputBufferBytesCount() > 0) {
			return true;
		} 
		return false;
	}
	
}
