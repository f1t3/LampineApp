// C library headers
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <pthread.h>

// Linux headers
#include <fcntl.h> // Contains file controls like O_RDWR
#include <errno.h> // Error integer and strerror() function
#include <termios.h> // Contains POSIX terminal control definitions
#include <unistd.h> // write(), read(), close()

#define LMS_SOF  0x02
#define LMS_EOF  0x03
#define LMS_ACK  0x06
#define LMS_NACK 0x15

#define SOM_POS 1

#define LMS_SOM 0x04
#define LMS_EOM 0x05
#define LMS_TYPE_SHORT 0x01

unsigned char tx_buf[256];
#define TX_BUF_OFFSET 10

static int serial_port = 0;
    
uint32_t newInputLength = 0;
char input_buf[200000];
char read_buf[1024];


//////////////////////////////////////////////////////////////////////////////////////////////////////


#include "LMStack.h"

LMSLayer1HwInterface hwi;
LMSLayer1SAP sapL1;
LMSLayer2SAP sapL2;
LMSLayer3SAP sapL3;

// TODO: TO INIT
LMStack stack = {
		.hwi = &hwi,
		.layer1SAP = &sapL1,
		.layer2SAP = &sapL2,
		.layer3SAP = &sapL3,
};



/////////////////////////////////////////////////////////////////////////////////////////////////////

void* ReadFromKeyboardThread(void *vargp)
{ 
    while (1) 
    {
        // Read keybord  
        fgets(input_buf, 200000, stdin);
        int i;
        for (i = 0; i < 200000; i++)
        {
            if (input_buf[i] < 32 || input_buf[i] > 126)
            {
                input_buf[i] = 0;
                break;
            }
        }
        while (newInputLength != 0);
        newInputLength = i;
    }
} 

void* ReadFromSerialThread(void *vargp)
{
	while (1)
	{
		// Read serial
		if (serial_port != 0)
		{
			uint32_t num_bytes;
	        num_bytes = read(serial_port, &read_buf, sizeof(read_buf));
	        char* frame = read_buf;
	        if (num_bytes != 0)
	        {
                onSerialReceive(frame, num_bytes);
	        }

		}

	}
}
    
void* TimerThread(void *vargp)
{
	while (1)
	{
		LMS_periodicRunner(&stack, 0);
		usleep(1000);
	}
}



int main(int argc, char *argv[])
{
	LMS_init(&stack);

    pthread_t thread_id1; 
    pthread_t thread_id2;
    pthread_t thread_id3;
    pthread_create(&thread_id1, NULL, ReadFromKeyboardThread, NULL); 
    pthread_create(&thread_id2, NULL, ReadFromSerialThread, NULL);
    pthread_create(&thread_id3, NULL, TimerThread, NULL);

    //printf("CS of String <%s> is: %d\n", argv[1], sum);
    
    // Open the serial port. Change device path as needed (currently set to an standard FTDI USB-UART cable type device)
    serial_port = open("/dev/ttyUSB0", O_RDWR);

    // Create new termios struc, we call it 'tty' for convention
    struct termios tty;

    // Read in existing settings, and handle any error
    if(tcgetattr(serial_port, &tty) != 0) {
        printf("Error %i from tcgetattr: %s\n", errno, strerror(errno));
        return 1;
    }

    tty.c_cflag &= ~PARENB; // Clear parity bit, disabling parity (most common)
    tty.c_cflag &= ~CSTOPB; // Clear stop field, only one stop bit used in communication (most common)
    tty.c_cflag &= ~CSIZE; // Clear all bits that set the data size 
    tty.c_cflag |= CS8; // 8 bits per byte (most common)
    tty.c_cflag &= ~CRTSCTS; // Disable RTS/CTS hardware flow control (most common)
    tty.c_cflag |= CREAD | CLOCAL; // Turn on READ & ignore ctrl lines (CLOCAL = 1)

    tty.c_lflag &= ~ICANON;
    tty.c_lflag &= ~ECHO; // Disable echo
    tty.c_lflag &= ~ECHOE; // Disable erasure
    tty.c_lflag &= ~ECHONL; // Disable new-line echo
    tty.c_lflag &= ~ISIG; // Disable interpretation of INTR, QUIT and SUSP
    tty.c_iflag &= ~(IXON | IXOFF | IXANY); // Turn off s/w flow ctrl
    tty.c_iflag &= ~(IGNBRK|BRKINT|PARMRK|ISTRIP|INLCR|IGNCR|ICRNL); // Disable any special handling of received bytes

    tty.c_oflag &= ~OPOST; // Prevent special interpretation of output bytes (e.g. newline chars)
    tty.c_oflag &= ~ONLCR; // Prevent conversion of newline to carriage return/line feed
    // tty.c_oflag &= ~OXTABS; // Prevent conversion of tabs to spaces (NOT PRESENT ON LINUX)
    // tty.c_oflag &= ~ONOEOT; // Prevent removal of C-d chars (0x004) in output (NOT PRESENT ON LINUX)

    tty.c_cc[VTIME] = 10;    // Wait for up to 1s (10 deciseconds), returning as soon as any data is received.
    tty.c_cc[VMIN] = 0;

    // Set in/out baud rate to be 9600
    cfsetispeed(&tty, B115200);
    cfsetospeed(&tty, B115200);

    // Save tty settings, also checking for error
    if (tcsetattr(serial_port, TCSANOW, &tty) != 0) {
        printf("Error %i from tcsetattr: %s\n", errno, strerror(errno));
        return 1;
    }

    // Allocate memory for read buffer, set size according to your needs
    char read_buf [256];


    // Read bytes. The behaviour of read() (e.g. does it block?,
    // how long does it block for?) depends on the configuration
    // settings above, specifically VMIN and VTIME
    int num_bytes = 0;
    while (1) 
    {
        if (newInputLength > 0)
        {
            LMS_sendMessageShort(&stack, input_buf, newInputLength);
            newInputLength = 0;
        }
    }
    // n is the number of bytes read. n may be 0 if no bytes were received, and can also be -1 to signal an error.
    if (num_bytes < 0) {
        printf("Error reading: %s", strerror(errno));
        return 1;
    }

    // Here we assume we received ASCII data, but you might be sending raw bytes (in that case, don't try and
    // print it to the screen like this!)
    //printf("Read %i bytes\n", num_bytes, read_buf);

    close(serial_port);
    return 0; // success
}
