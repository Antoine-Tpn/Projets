const int I1 = 2, E = 3, LedR = 13, LedJ = 12, LedV = 11, BP1 = 5;
int A, B, result;
unsigned long previousMillis = 0;
// Période du signal carré en microsecondes (20 Hz)
 // 1 seconde divisée par 20

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
  unsigned long currentMillis = millis();
  int I = digitalRead(I1);
  int BP = digitalRead(BP1);
  float alpha = 40 ;
  float beta = 60;
  float periode40 = 50000 * alpha / 100;
  float periode60 = 50000 * beta / 100;
  digitalWrite(LedR, LOW);
  digitalWrite(LedJ, LOW);
  digitalWrite(LedV, LOW);
  
  if (I == LOW && (currentMillis - previousMillis) >= periode40/1000 ) {  
  Serial.println("boucle 40");
  digitalWrite(E, LOW); // État bas
  delayMicroseconds(periode60); // Durée du rapport cyclique
  digitalWrite(E, HIGH);
  previousMillis = currentMillis;
  
 }

 else if (I == HIGH && (currentMillis - previousMillis) >= periode60/1000 ) {
   
  Serial.println("boucle 60");
  digitalWrite(E, LOW); // État bas
  delayMicroseconds(periode40); // Durée du rapport cyclique
  digitalWrite(E, HIGH); // État bas
  previousMillis = currentMillis;
  } 

  if (BP == HIGH) {

    // Signal carré avec rapport cyclique fixe
    A = analogRead(A0); // point A
    B = analogRead(A1); // point B
    float result = 5.0 * (A - B) / (1024 - 1);
    float Ve = analogRead(E) * 5.0 / 1023; // Convertir la lecture en tension
    
    Serial.print("Etat de Ve ");
    Serial.println(Ve);
    Serial.print("result ");
    Serial.println(result);
    
    if (result == 5) {
        Serial.println("Diode ou Led bloque"); 
        digitalWrite(LedR, HIGH);}
    else if (result <= 0.65 && result >= 0.5) {
        Serial.println("Diode Passante");
        digitalWrite(LedV, HIGH);} 
    else if (result <= 1.70 && result >= 1.5){
      Serial.println("Led Passante"); 
      digitalWrite(LedJ, HIGH);}
    else if (result <= 0.10) {
      Serial.println("Fil, Defectueux");
      digitalWrite(LedR, HIGH);}

   
  }
  
}
