void setup() {
  //config PINS
  pinMode(2, INPUT_PULLUP); //Sync (from the driver board) -> D2 (PD2)

  //set the ADC reference to 5v
  //analogReference(EXTERNAL);
  
  //set the ADC as fast as possible, setting the prescale to 4 (around 307ks)
  ADCSRA &= ~(bit (ADPS0) | bit (ADPS1) | bit (ADPS2)); // clear prescaler bits
  ADCSRA |= bit (ADPS1);                               //   4 
  
  //initialize serial
  Serial.begin(1000000);
}

#define N_SAMPLES 32
int samples[N_SAMPLES];
void loop() {
  //wait for one byte from the serial to start the capture
  if(Serial.available() > 0){
    //wait for a falling edge of the sync input
    while (PIND & 0b00000100); //wait for the sync to go up
    while (! (PIND & 0b00000100) ); //wait for the sync to go down

    //capture samples
    for(int i = 0; i < N_SAMPLES; ++i){
      samples[i] = analogRead(A0);
    }
    
    //send them through Serial, each sample as uint16
    for(int i = 0; i<N_SAMPLES; ++i){
      Serial.write( (samples[i] >> 8) & 0xFF );
      Serial.write( samples[i] & 0xFF );
      //Serial.println(samples[i]);
    }
    
    Serial.read(); //discard the byte that was sent to us 
  }
}
