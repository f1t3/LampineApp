
#ifndef LMSFRAME_H
#define LMSFRAME_H

#include <stdbool.h>

#define LMSFrame_SOF_BYTE 0x02
#define LMSFrame_EOF_BYTE 0x03
#define LMSFrame_ACK_BYTE 0x06
#define LMSFrame_NACK_BYTE 0x15

#define LMSFrame_MAX_FRAME_LEN       (20)
#define LMSFrame_MAX_NUM_DATA_BYTES  (16)
#define LMSFrame_NUM_PREAMBLE_BYTES  (1)
#define LMSFrame_NUM_CHECKSUM_BYTES  (2)
#define LMSFrame_NUM_POSTAMBLE_BYTES (LMSFrame_NUM_CHECKSUM_BYTES + 1)

typedef struct {
	char* preamble;
	char* data;
	uint32_t dataLen;
	char checksum[LMSFrame_NUM_CHECKSUM_BYTES];
} LMSFrame;

bool     LMSFrame_isValid(LMSFrame* frame);
char*    LMSFrame_toBytes(LMSFrame* frame);
uint32_t LMSFRame_getFrameLen(LMSFrame* frame);
char*    LMSFrame_getDataBytes(LMSFrame* frame, char* buf);
uint32_t LMSFrame_getMaxNumDataBytes();

#endif /* LMSFRAME_H_ */
