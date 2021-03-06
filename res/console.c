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

uint16_t calcAdvFletcher(char* data, uint32_t len)
{
    uint32_t sum1 = 0;
    uint32_t sum2 = 0;
    for (uint32_t i = 0; i < len; i++)
    {
        // Modulo 2^16-1
        sum1 = sum1 + sum2 + data[i];
        sum1 = (sum1 & 0xFFFF) + (sum1 >> 16);
        sum2 = sum1 + sum2;
        sum2 = (sum2 & 0xFFFF) + (sum2 >> 16);   
    }
    // Modulo 2^8-1
    sum1 = ((sum1 & 0xFF) + (sum1 >> 8)) & 0xFF;
    sum2 = ((sum2 & 0xFF) + (sum2 >> 8)) & 0xFF;
    return (uint16_t) ((sum1 << 8) | (sum2));
}

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

static int serial_port;
    
void writeAckFrame() 
{
    char msg[] = {LMS_ACK};
    write(serial_port, msg, 1);
}

unsigned char* newMessageType1(char* data, uint32_t len) 
{
    char* msg = &tx_buf[TX_BUF_OFFSET];
    msg[0] = LMS_SOM;
    msg[1] = LMS_TYPE_SHORT; 
    for (int i = 0; i < len; i++)
    {
        msg[2+i] = data[i];
    }
    msg[2+len] = LMS_EOM;
    return msg;
}

void writeDataFrames(char* data, uint32_t len) 
{
    unsigned char* frame = (unsigned char*)(data - 1);
    frame[0] = LMS_SOF;
    uint16_t sum = calcAdvFletcher(data, len);
    frame[1+len] = (uint8_t)((sum >> 8) & 0xFF);
    frame[2+len] = (uint8_t)(sum & 0xFF);     
    frame[3+len] = LMS_EOF; 
    write(serial_port, frame, len + 4);
    //printf("Sending: ");
//     for (int i = 0; i < len+4; i++) 
//     {
//         printf("%d ",frame[i]);
//     }
    //printf("\n");
}

uint8_t newInputLength = 0;
unsigned char input_buf[256];

void *myThreadFun1(void *vargp) 
{ 
    while (1) 
    {
        // Read keybord  
        fgets(input_buf, 256, stdin);
        int i;
        for (i = 0; i < 256; i++) 
        {
            if (input_buf[i] < 32 || input_buf[i] > 126) 
            {
                input_buf[i] = 0;
                break;
            }
        }
        while (newInputLength != 0);
        newInputLength = i;
        //printf("Input of len %i: %s\n", i, input_buf);
    }
} 

void extractMessageFromFrame (char* frame, uint32_t len)
{
    frame = &frame[SOM_POS];
    frame[len-3] = 0;
}

void extractDataFromMessage (char* msg, uint32_t len)
{
    msg = &msg[2];
    msg[len-1] = 0;
}
    
int main(int argc, char *argv[])
{
    
    pthread_t thread_id1; 
    pthread_create(&thread_id1, NULL, myThreadFun1, NULL); 
    
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
    
    char* msg = newMessageType1("ABCD", 4);
    writeDataFrames(msg, 4+3);
    
    sleep(0.2);

    // Allocate memory for read buffer, set size according to your needs
    char read_buf [256];

    // Normally you wouldn't do this memset() call, but since we will just receive
    // ASCII data for this example, we'll set everything to 0 so we can
    // call printf() easily.
    memset(&read_buf, '\0', sizeof(read_buf));

    // Read bytes. The behaviour of read() (e.g. does it block?,
    // how long does it block for?) depends on the configuration
    // settings above, specifically VMIN and VTIME
    int num_bytes = 0;
    while (1) 
    {
        memset(&read_buf, '\0', sizeof(read_buf));
        num_bytes = read(serial_port, &read_buf, sizeof(read_buf));
        char* frame = read_buf;
        if (num_bytes != 0) 
        {
            switch (read_buf[0])
            {
            case LMS_ACK:
                //printf("Received ACK\n");
                break;
            case LMS_NACK:
                //printf("Received NACK\n");
                break;
            default:
                //printf("Read %i bytes: %s\n", num_bytes, read_buf);
                
                extractMessageFromFrame(frame, num_bytes);
                extractDataFromMessage(frame, num_bytes - 4);
                printf("%s\n", frame);
                //printf(" (%iB total, %iB data)\n", num_bytes, num_bytes-7);
                writeAckFrame();
                break;
            }
        }
        if (newInputLength > 0)
        {
            //printf("Process Input of len %i: %s\n", newInputLength, input_buf);
            char* msg = newMessageType1(input_buf, newInputLength);
            writeDataFrames(msg, newInputLength+3);
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
