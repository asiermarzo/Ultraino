DriverMEGA is the normal code. It can upload up to 32 patterns and then a script is sent that indicates how many periods each pattern should be played.
DriverMEGAAnim is designed to upload 32 patterns at the same time and then send only one byte for the desired pattern
DriverMEGAStatic is designed for operating without a computer since the 32 patterns are predefined and it is possible to cycle through them by external buttons. These patterns can be generated with the simulator.


Please do not forget to add in the public interface of Program Files (x86)\Arduino\hardware\arduino\avr\cores\arduino\HardwareSerial.h
the following inline functions. For MAC the path may be "HD -> Applications -> Arduino -> Contents -> Java -> hardware -> arduino -> avr -> cores -> arduino"

inline bool _dataAvailable() {return _rx_buffer_head != _rx_buffer_tail; }
inline byte _peekData() { return _rx_buffer[_rx_buffer_tail]; }
inline void _discardByte() { _rx_buffer_tail = (rx_buffer_index_t)(_rx_buffer_tail + 1) % SERIAL_RX_BUFFER_SIZE; }
