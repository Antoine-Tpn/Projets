int ledV = 9;         // the PWM pin the LED is attached to
int ledR = 12;         // the PWM pin the LED is attached to
const uint8_t sensorPin=2;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);    // permet de fixer le débit de caractère de communication par seconde
  pinMode(ledV, OUTPUT);   // on initialise la led verte comme sortie
  pinMode(ledR, OUTPUT);   // on initialise la led rouge comme sortie
  pinMode (sensorPin,INPUT); // on initialise le capteur comme entrée du système
}

void loop() {
  // put your main code here, to run repeatedly:
  int16_t t = pulseIn(sensorPin, HIGH);  // récupère le temps de pulsation haute du capteur
  int16_t d = (t - 1000) * 2; // on calcul la distance en fonction de ce temps
  if (d <= 50) {          // si la distance est plus petite que 50 mm
    digitalWrite(ledR, HIGH);          // on allume la led rouge
    digitalWrite(ledV, LOW);           // et on éteint la led verte
    delay(30);                         // puis on attend 30 ms
  }

  else if (d <= 1300 and d >= 50){  // si la distance est comprise entre 50 mm et 1300mm 
    // intensité le verte
    digitalWrite(ledR, LOW);        // on éteint la led rouge
    float brightness = (-0.204 * d) + 265.2;  // on calcul l'intensité de l'éclairage émis par la led verte en fonction de la distance 
    analogWrite(ledV, brightness ); // et enfin on allume la led celon brightness
    delay(30);                      // puis on attend 30 ms
  }

  else if (d >= 1300){    // si la distance est supérieur a 1300 mm
    // affichage led rouge
    digitalWrite(ledR, HIGH); // on allume la led rouge
    digitalWrite(ledV, LOW);  // on éteint la led verte
    delay(1000);               // on attend 100 ms
    digitalWrite(ledR, LOW);  // puis on éteint la led rouge
    delay(1000);               // et enfin on attends à nouveau 100 ms
  }
  unsigned long get = micros();
  int time = get / 1000000;
  Serial.print("temps depuis le debut du programme : ");
  Serial.print(time);
  Serial.println(" s");
  Serial.print("distance mesuree : ");
  Serial.print(d);
  Serial.println(" mm");
  delay(10);  
}
