// Example Arduino program for reading the Pololu Distance Sensor with Pulse Width Output, 130cm Max
 
// Change this to match the Arduino pin connected to the sensor's OUT pin.
const uint8_t sensorPin = 2;
 
void setup()
{
  Serial.begin(115200);
}
 
void loop()
{
  int16_t t = pulseIn(sensorPin, HIGH);
  //int16_t t = 0;
 
  if (t == 0)
  {
    // pulseIn() did not detect the start of a pulse within 1 second.
    //Serial.println("timeout");
  }
  else if (t > 1850)
  {
    // No detection.
    Serial.println(0);
  }
  else
  {
    // Valid pulse width reading. Convert pulse width in microseconds to distance in millimeters.
    int16_t d = (t - 1000) * 2;
 
    // Limit minimum distance to 0.
    if (d < 0) { d = 0; } 
    
    Serial.print("S");
    Serial.println(d);
    //Serial.println(" mm");
  }

  delay(10);
}
