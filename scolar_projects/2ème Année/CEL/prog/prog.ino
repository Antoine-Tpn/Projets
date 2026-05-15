const int I1 = 2, E = 3, LedR = 13, LedJ = 12, LedV = 11, BP1 = 5;
#define alpha 40
#define beta 60
int A, B, result;
const int periodeMicrosecondes = 50000; // 1 seconde divisée par 20

void setup() {
  Serial.begin(250000); 
  pinMode(I1, INPUT); // interrupteur I1
  pinMode(LedR, OUTPUT);
  pinMode(LedJ, OUTPUT);
  pinMode(LedV, OUTPUT);
  pinMode(BP1, INPUT);
  pinMode(E, OUTPUT);
}

void loop() {
  int I = digitalRead(I1);
  int BP = digitalRead(BP1);
  
  if (BP == HIGH) { // BP1 est enfoncé
    if (I == LOW) {
      digitalWrite(E, HIGH); // État haut
      delayMicroseconds(periodeMicrosecondes * alpha / 100); // Durée du rapport cyclique
      digitalWrite(E, LOW); // État bas
      delayMicroseconds(periodeMicrosecondes * (100 - alpha) / 100); // Durée de l'espace
    } else {
      digitalWrite(E, HIGH); // État haut
      delayMicroseconds(periodeMicrosecondes * beta / 100); // Durée du rapport cyclique
      digitalWrite(E, LOW); // État bas
      delayMicroseconds(periodeMicrosecondes * (100 - beta) / 100); // Durée de l'espace
    }

    A = analogRead(A0); // point A
    B = analogRead(A1); // point B
    result = 5 * (A - B) / (1024 - 1);
    Serial.println(result);
  
    if (result == 5) {
      Serial.println("Diode bloqué, Led bloqué ou circuit ouvert");
    } else if (result <= 0.65 && result >= 0.5) {
      Serial.println("Diode Passante");
    } else if (result <= 1.60 && result >= 1.70) {
      Serial.println("Led Passante");
    } else if (result <= 0.10) {
      Serial.println("court-circuit");
    
  }

  delay(700);
}
