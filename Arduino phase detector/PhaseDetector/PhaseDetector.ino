void setup() {
  //config PINS
  pinMode(2, INPUT_PULLUP); //Sync (from the board) -> D2 (PD2)

  //set the ADC reference to 5v
  //analogReference(EXTERNAL);
  
  //set the ADC as fast as possible, setting the prescale to 16
  ADCSRA &= ~(bit (ADPS0) | bit (ADPS1) | bit (ADPS2)); // clear prescaler bits
  ADCSRA |= bit (ADPS1);                               //   4 
  
  //initialize serial
  Serial.begin(1000000);
}

#define N_SAMPLES 32
void loop() {
   int samples[N_SAMPLES];
 
  //wait for one byte from the serial to start capture
  if(Serial.available() > 0){
    while (PIND & 0b00000100); //wait for the sync to go up
    while (! (PIND & 0b00000100) ); //wait for the sync to go down

    //capture samples
    for(int i = 0; i < N_SAMPLES; ++i){
      samples[i] = analogRead(A0);
    }
    
    //send them
    for(int i = 0; i<N_SAMPLES; ++i){
      Serial.write( (samples[i] >> 8) & 0xFF );
      Serial.write( samples[i] & 0xFF );
      //Serial.println(samples[i]);
    }
    
    Serial.read(); //discard the serial byte
  }
}
