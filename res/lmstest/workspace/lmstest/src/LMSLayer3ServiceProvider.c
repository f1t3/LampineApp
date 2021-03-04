
#include "string.h"

#include "LMSLayer2SAP.h"
#include "LMSLayer3SAP.h"
#include "AFS.h"

static void transmitShortMessage(LMStack* stack, char* data, uint32_t len);
static void transmitLongMessage(LMStack* stack, char* data, uint32_t len);
static void segmentAndTransmit(LMStack* stack, char* data, uint32_t len);
static void assembleShortMessage(char* buf, char* data, uint32_t len);
static void assembleLongMessagePreamble(char* buf, uint32_t len);
static void assembleLongMessagePostamble(char* buf, char* data, uint32_t len);
static void intToCharArray(char* buf, uint32_t intVal, uint32_t numBytes);
static void calcChecksumToCharArray(char* buf, char* data, uint32_t len);
static uint32_t charArrayToInt(char* charArray, uint32_t numBytes);

void LMSL3_transmit(LMStack* stack, LMSLayer3MessageType type, char* data, uint32_t len)
{
	switch (type)
	{
	case LMSL3_MSG_TYPE_SHORT:
		transmitShortMessage(stack, data, len);
		break;
	case LMSL3_MSG_TYPE_LONG:
		transmitLongMessage(stack, data, len);
		break;
	}
}

void LMSL3_receive(LMStack* stack, char* data, uint32_t len)
{
	// TODO: DISASSEMBLE MESSAGE;
	char* msgDat = &data[LMSL3_POS_DAT_SHORT];
	uint32_t msgLen = len - LMSL3_NUM_PREAMBLE_BYTES_SHORT - LMSL3_NUM_POSTAMBLE_BYTES_SHORT;
	LMS_receiveMessage(stack, msgDat, msgLen);
}

static void transmitShortMessage(LMStack* stack, char* data, uint32_t len)
{
	if (len > LMSL3_MAX_LEN_SHORT_MSG)
		return;
	char buf[LMSL3_MAX_LEN_SHORT_MSG + LMSL3_NUM_PREAMBLE_BYTES_SHORT + LMSL3_NUM_POSTAMBLE_BYTES_SHORT];
	assembleShortMessage(buf, data, len);
	segmentAndTransmit(stack, buf, LMSL3_NUM_PREAMBLE_BYTES_SHORT + len + LMSL3_NUM_POSTAMBLE_BYTES_SHORT);
}

static void transmitLongMessage(LMStack* stack, char* data, uint32_t len)
{
	// Transmit preamble
	char preambleBuf[LMSL3_NUM_PREAMBLE_BYTES_LONG];
	assembleLongMessagePreamble(preambleBuf, len);
	segmentAndTransmit(stack, preambleBuf, LMSL3_NUM_PREAMBLE_BYTES_LONG);
	// Transmit data
	segmentAndTransmit(stack, data, len);
	// Transmit postamble
	char postambleBuf[LMSL3_NUM_POSTAMBLE_BYTES_LONG];
	assembleLongMessagePostamble(postambleBuf, data, len);
	segmentAndTransmit(stack, postambleBuf, LMSL3_NUM_POSTAMBLE_BYTES_LONG);
}

static void segmentAndTransmit(LMStack* stack, char* data, uint32_t len)
{
	const uint32_t L2MTU = stack->layer2SAP->mtu;
	uint32_t bytesSend = 0;
	uint32_t bytesLeft = len;
	while (bytesLeft > L2MTU)
	{
		LMSL2_transmit(stack, &data[bytesSend], L2MTU);
		bytesSend += L2MTU;
		bytesLeft -= L2MTU;
	}
	LMSL2_transmit(stack, &data[bytesSend], bytesLeft);
}

static void assembleShortMessage(char* buf, char* data, uint32_t len)
{
	// SOM
	buf[LMSL3_POS_SOM]  = LMSL3_SOM_BYTE;
	// TYPE
	buf[LMSL3_POS_TYPE] = LMSL3_TYPE_SHORT_BYTE;
	// DATA
	memcpy(&buf[LMSL3_POS_DAT_SHORT], data, len);
	// EOM
	buf[LMSL3_POS_DAT_SHORT + len + LMSL3_POS_EOM_AFTER_DAT_SHORT] = LMSL3_EOM_BYTE;
}

static void assembleLongMessagePreamble(char* buf, uint32_t len)
{
	buf[LMSL3_POS_SOM]  = LMSL3_SOM_BYTE;
	buf[LMSL3_POS_TYPE] = LMSL3_TYPE_LONG_BYTE;
	intToCharArray(&buf[LMSL3_POS_LEN_LONG], len, LMSL3_NUM_LEN_BYTES);
}

void assembleLongMessagePostamble(char* buf, char* data, uint32_t len)
{
	// CS
	char postambleBuf[LMSL3_NUM_POSTAMBLE_BYTES_LONG];
	calcChecksumToCharArray(postambleBuf, data, len);
	// EOM
	postambleBuf[LMSL3_POS_EOM_AFTER_DAT_LONG] = LMSL3_EOM_BYTE;
}


static void intToCharArray(char* buf, uint32_t intVal, uint32_t numBytes)
{
	uint32_t bytesProcessed = 0;
	uint32_t bytesLeft = numBytes;
	while (bytesLeft > 0)
	{
		buf[bytesProcessed] = (intVal >> (8*(bytesLeft-1))) & 0xFF;
		bytesLeft--;
		bytesProcessed++;
	}
}

static uint32_t charArrayToInt(char* charArray, uint32_t numBytes)
{
	uint32_t intVal;
	uint32_t bytesProcessed = 0;
	uint32_t bytesLeft = numBytes;
	while (bytesLeft > 0)
	{
		intVal += charArray[bytesProcessed] << (8*(bytesLeft-1));
		bytesLeft--;
		bytesProcessed++;
	}
	return intVal;
}

static void calcChecksumToCharArray(char* buf, char* data, uint32_t len)
{
	const uint32_t checksum = AFS_calc32bit(data, len);
	intToCharArray(buf, checksum, LMSL3_NUM_CS_BYTES);
}

void LMSL3_init(LMStack* stack)
{
}
