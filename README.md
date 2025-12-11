A Finite state machine protocol design using the jssc library to send bytes of different Arduino Uno readings to Java.

# Protocol:

0x21: Magic number, resembles the beginning of a message

INFO TYPE:

- 0x30: info string in UTF-8 format, maximum of 100 characters long
- 0x31: error string in UTF-8 format, maximum of 100 characters long
- 0x32: timestamp, 4-byte integer, milliseconds since reset
- 0x33: potentiometer reading, 2-byte integer A/D counts
- 0x34: raw (unconverted) ultrasonic sensor reading (i.e., time), 4-byte unsigned integer in μs.

# General FSM Flow:

Using a SerialComm object to read availability and incoming bytes from the stream,
we look for the magic number until it is found, then deciphering the next byte into one of the 0x30-0x34 keys, which will then direct us
to  retrieve the next amount of bytes depending on the info type. This is done simply with an enum. If a message is malformed, the FSM will fail to read a correct info/magic number string, and go back to its default state of looking for the next magic number.


