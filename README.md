# Ultraino
Acoustic Field simulation for phased-array controllers. The content of the folders is as follows:
- AcousticField3D. The simulator that runs on the PC and can control the driver boards
- Arduino MEGA 64 driver board code. Source code that goes in the Arduino Mega 64-channel driver board.
- DriverNano16. Source code for making an Arduino Nano a 16-channel driver board.
- Arduino phase detector. Code that goes in an Arduino Nano to serve as a tool to assign the channels.
- arrays. Bases for example arrays.
- driver board. BOM and PCB files for the driver board.

# How to Run the Software
* Download the source as a zip file or checkout the repository.

* Be sure that you have installed JDK 11 https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
* Install Netbeans Integrated Development Environment (you can also use Eclipse if you want). The Java SE pack is enough https://netbeans.org/downloads/

* Import the Source code in Netbeans. Run Netbeans, File->Open Project, Select the folder with the sourcecode.
* Click Run. You can click Run->Run project. It will take some time the first time since it needs to compile the sourcecode.

# Install Arduino Software
* Install the Arduino IDE https://www.arduino.cc/en/Main/Software
* Run the Arduino IDE and connect the Arduino Board to your computer.
* In Tools->Board select Arduino/Genuino Mega or Mega 2560. In Tools->Ports select the port for your Arduino. You may need to install the drivers for your Arduino Board (specially if it is not a branded one). For the chip CH340 http://sparks.gogo.co.nz/ch340.html
* In the public interface of Program Files (x86)\Arduino\hardware\arduino\avr\cores\arduino\HardwareSerial.h
add the following inline functions
inline bool _dataAvailable() {return _rx_buffer_head != _rx_buffer_tail; }
inline byte _peekData() { return _rx_buffer[_rx_buffer_tail]; }
inline void _discardByte() { _rx_buffer_tail = (rx_buffer_index_t)(_rx_buffer_tail + 1) % SERIAL_RX_BUFFER_SIZE; } 
* Click the Icon for Upload

# Install the Channel detector (tiny device used to assign channels semiautomatically)
* There is a schematic in the folder
* Install the software in Arduino Nano

# How to Assemble and example Applications
Paper: http://ieeexplore.ieee.org/document/8094247/
Instructables: https://www.instructables.com/id/Ultrasonic-array/
Video: https://youtu.be/h0Mh0bIv9Fk
