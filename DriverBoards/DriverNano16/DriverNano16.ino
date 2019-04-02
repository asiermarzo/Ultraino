#include <avr/sleep.h>
#include <avr/power.h>

#define N_PORTS 2
#define N_DIVS 16

#define COMMAND_SWITCH 0b00000000
#define COMMAND_RESET 0b00010000

#define WAIT_A(a) __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");__asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");__asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");__asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");__asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop")
#define WAIT_B(a) __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");__asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");__asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop")
#define WAIT_C(a) __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop"); __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop");  __asm__ __volatile__ ("nop")


#define OUTPUT_WAVE(pointer, d)  PORTC = pointer[d*N_PORTS + 0]; PORTD = pointer[d*N_PORTS + 1]

static byte bufferA[N_DIVS * N_PORTS];
static byte bufferB[N_DIVS * N_PORTS];

void setup()
{
  //initialize the buffers
  for (int i = 0; i < (N_PORTS*N_DIVS); ++i){
    bufferA[i] = bufferB[0] = 0;
  }

  for (int i = 0; i < (N_PORTS*N_DIVS/2); ++i){
     bufferA[i] = 0b11111111;
  }
  
 /*
  //only for calibrating WAITs
  for(int i = 0; i < N_DIVS; ++i){
    if (i % 2 == 0){
      bufferA[i * N_PORTS] |= 0b00001000;
    }else{
      bufferA[i * N_PORTS] &= 0b11110111;
    }
  }
  */

  // generate a sync signal of 40khz in pin 10
  pinMode (10, OUTPUT);
  noInterrupts();           // disable all interrupts
  TCCR1A = bit (WGM10) | bit (WGM11) | bit (COM1B1); // fast PWM, clear OC1B on compare
  TCCR1B = bit (WGM12) | bit (WGM13) | bit (CS10);   // fast PWM, no prescaler
  OCR1A =  (F_CPU / 40000L) - 1;
  OCR1B = (F_CPU / 40000L) / 2;
  interrupts();             // enable all interrupts

  //sync in signal at pin 3
  pinMode(11, INPUT_PULLUP); //please connect pin 10 to pin 11
  

  // disable everything that we do not need 
  ADCSRA = 0;  // ADC
  power_adc_disable ();
  power_spi_disable();
  power_twi_disable();
  power_timer0_disable();
  //power_usart0_disable();

  Serial.begin(115200);

   DDRC = 0b00111111;
   DDRD = 0b11111100; 
   PORTC = 0b00000000; 
   PORTD = 0b00000000; 
   
 byte bReceived = 0;
 bool byteRead = false;
 bool isSwitch = false;
 bool isReset = false;
 bool isForMe = false;
 byte nextMsg = 0;
 int writtingIndex = 0;

 byte* emittingPointer = & bufferA[0];
 byte* readingPointer = & bufferB[0];

  LOOP:
    while(PINB & 0b00001000); //wait for pin 11 (B3) to go low 
    
    OUTPUT_WAVE(emittingPointer, 0); byteRead = Serial._dataAvailable(); WAIT_C();
    OUTPUT_WAVE(emittingPointer, 1); bReceived = Serial._peekData(); WAIT_C();
    OUTPUT_WAVE(emittingPointer, 2); isSwitch = bReceived == COMMAND_SWITCH; WAIT_B();
    OUTPUT_WAVE(emittingPointer, 3); isReset = bReceived == COMMAND_RESET; WAIT_B();
    OUTPUT_WAVE(emittingPointer, 4); isForMe = (bReceived & 0b00001111) == 1; WAIT_B(); 
    OUTPUT_WAVE(emittingPointer, 5); nextMsg =  bReceived - 1; WAIT_B();
    OUTPUT_WAVE(emittingPointer, 6); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 7); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 8); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 9); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 10); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 11); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 12); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 13); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 14); WAIT_A();
    OUTPUT_WAVE(emittingPointer, 15);

    if (byteRead){
        if ( isSwitch ){
          Serial.write( COMMAND_SWITCH );
          byte* tmpPointer = emittingPointer;
          emittingPointer = readingPointer;
          readingPointer = tmpPointer;
          
          writtingIndex = 0;
        }else if ( isReset ){
          Serial.write( COMMAND_RESET );
          writtingIndex = 0;
        }else if ( isForMe ){
          if (writtingIndex % 2 == 0){
            readingPointer[writtingIndex / 2] = bReceived & 0xF0;
          }else{
            readingPointer[writtingIndex / 2] |= (bReceived >> 4);
          }
          ++writtingIndex;
        }else{
          Serial.write( nextMsg );
        }
        Serial.read();
    }
    
  goto LOOP;
  
}

void loop(){}

