const unsigned int POT_PIN = A0;
const int trigPin = 6;           //connects to the trigger pin on the distance sensor
const int echoPin = 7;    

const byte MAGIC_NUMBER = 0x21;

const byte INFO_KEY = 0x30;
const byte ERROR_KEY = 0x31;
const byte TIMESTAMP_KEY = 0x32;
const byte POT_KEY=0x33;
const byte SENSOR_KEY=0x34;

unsigned long samplePeriod = 1000;
unsigned long nextSampleTime = 0;
const String info = "Hello World!";
String error = "";

	// final byte INFO_KEY = 0x30;
	// final byte ERROR_KEY = 0x31;
	// final byte TIMESTAMP_KEY = 0x32;
	// final byte POT_KEY = 0x33;
	// final byte SENSOR_KEY = 0x34;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  // Set the analog reference to 5V.  This will allow us to detect voltages from 0 to 5V on the pin.
  analogReference(DEFAULT);
}

void loop() {
  // put your main code here, to run repeatedly:

  // Get the current time.
  unsigned long currentTime = millis();

  // Use delta-timing to sample the voltage every sample period.
  if (currentTime >= nextSampleTime) {
    nextSampleTime += samplePeriod;

    // Readings
    unsigned int pot_read = analogRead(POT_PIN);
    unsigned long sensor_read = getSensorTime();


    // Send timestamp.
    sendTimestamp(currentTime);

    // Send Info
    sendInfo();
    
    // Send Error (if any)
    sendError(pot_read);
    
    // Send Potentiometer
    sendPot(pot_read);
    // Send Sensor
      sendSensor(sensor_read);
  }
}

void sendMagicNumber() {
  Serial.write(MAGIC_NUMBER);
}

void sendULong(unsigned long value) {
  // Note, Serial.write only sends 8-bit numbers, so the higher order bits (i.e. >= 8) are ignored.
  Serial.write(value >> 24);
  Serial.write(value >> 16);
  Serial.write(value >> 8);
  Serial.write(value);
}

void sendTimestamp(unsigned long timestamp) {
  // Send magic number.
  sendMagicNumber();

  // Send timestamp key.
  Serial.write(TIMESTAMP_KEY);

  // Send timestamp value.
  sendULong(timestamp);
}

void sendInfo(){
  sendMagicNumber();

  Serial.write(INFO_KEY);

  unsigned int infoLength = info.length();
  Serial.write(infoLength >> 8);
  Serial.write(infoLength);

  for(int i = 0; i <= infoLength; i++){
    Serial.write(info[i]);
  }

}

void sendError(unsigned int value){
  if(value > 700){
    error = "Potentiometer is too high!";
  } else{
    error = "No Error";
  }
  sendMagicNumber();

  Serial.write(ERROR_KEY);

  unsigned int errorLength = error.length();
  Serial.write(errorLength >> 8);
  Serial.write(errorLength);

  for(int i = 0; i <= errorLength; i++){
    Serial.write(error[i]);
  }

}

void sendPot(unsigned int value){
  sendMagicNumber();

  Serial.write(POT_KEY);

  Serial.write(value >> 8);
  Serial.write(value);
}

void sendSensor(unsigned long value){
  sendMagicNumber();
  Serial.write(SENSOR_KEY);
  sendULong(value);
}


unsigned long getSensorTime(){
  unsigned long echoTime;           //variable to store the time it takes for a ping to bounce off an object
  // float calculatedDistance;         //variable to store the distance calculated from the echo time

  //send out an ultrasonic pulse that's 10us long
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  echoTime = pulseIn(echoPin, HIGH);      //use the pulsein command to see how long it takes for the
                                          //pulse to bounce back to the sensor

  //calculate the distance of the object that reflected the pulse (half the bounce time multiplied by the speed of sound)
  // calculatedDistance = echoTime * 1/2 * 340 * .0001;

  return echoTime;              //send back the distance that was calculated
}

