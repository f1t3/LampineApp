
#include <unistd.h>
#include "LMSLayer2SAP.h"
#include "LMSFrame.h"
#include "LMSFrameAssembler.h"
#include "AFS.h"

static void tryToTransmitUntilAckOrTimeout(LMStack* stack, char* data, uint32_t len);
static bool beginsWithSOF(LMStack* stack, char* data, uint32_t len);
static uint32_t extractLen(LMStack* stack);
static uint16_t extractChecksum(LMStack* stack);
static bool hasRxBufReachedTargetLen(LMStack* stack);
static bool hasRxBufExceededTargetLen(LMStack* stack);
static void sendAck(LMStack* stack);
static void resetRxBuf(LMStack* stack);
static bool hasFrameInBufValidChecksum(LMStack* stack);
static void sendNack(LMStack* stack);

char*    txDatQueue[256];
uint32_t txLenQueue[256];
uint8_t pWrite = 0;
uint8_t pRead = 0;
uint32_t size = 0;

void putToTransmitQueue(LMStack* stack, char* data, uint32_t len)
{
	// TODO??
	if (size < 256)
	{
		txDatQueue[pWrite] = data;
		txLenQueue[pWrite] = len;
		pWrite++;
		size++;
	}
}

uint32_t getQueueSize(LMStack* stack)
{
	return size;
}

void LMSL2_transmit(LMStack* stack, char* data, uint32_t len)
{
	putToTransmitQueue(stack, data, len);
}

void peekNextFromQueueAndTransmit(LMStack* stack)
{
	char* data = txDatQueue[pRead];
	uint32_t len = txLenQueue[pRead];

	const uint32_t L1MTU = stack->layer1SAP->mtu;
	uint32_t bytesSend = 0;
	uint32_t bytesLeft = len;

	// Transmit preamble
	char preamble[] = {LMSL2_SOF_BYTE, len};
	LMSL1_transmit(stack, preamble, LMSL2_LEN_PREAMBLE);
	// Transmit data
	while (bytesLeft > L1MTU)
	{
		LMSL1_transmit(stack, &data[bytesSend], L1MTU);
		bytesSend += L1MTU;
		bytesLeft -= L1MTU;
	}
	LMSL1_transmit(stack, &data[bytesSend], bytesLeft);

	// Transmit checksum
	const uint16_t cs = AFS_calc16bit(data, len);
	char checksum[] = {(cs >> 8) & 0xFF, cs & 0xFF};
	LMSL1_transmit(stack, &checksum, LMSL2_LEN_CS);
}

void removeNextFromQueue(LMStack* stack)
{
	if (size > 0)
	{
		pRead++;
		size--;
	}
}

void clearQueue(LMStack* stack)
{
	pRead = 0;
	pWrite = 0;
	size = 0;
}

LMSL2_periodicRunnerTx(LMStack* stack, uint32_t timeNow_ms)
{
	LMSLayer2ServiceState* sstate = &(stack->layer2SAP->serviceState);
	switch (*sstate)
	{
	case LMSL2_SSTATE_RECEIVED_ACK:
		stack->layer2SAP->waitingForAckTimeoutCntr_ms += 0;
		removeNextFromQueue(stack);
		*sstate = LMSL2_SSTATE_READY;
		break;
	case LMSL2_SSTATE_RECEIVED_NACK:
		stack->layer2SAP->txRetryCounter++;
		*sstate = LMSL2_SSTATE_READY;
		break;
	case LMSL2_SSTATE_WAITING_FOR_ACK:
		stack->layer2SAP->waitingForAckTimeoutCntr_ms += 1;
		if (stack->layer2SAP->waitingForAckTimeoutCntr_ms >= 2000) // TODO: TIMEBASE
		{
			stack->layer2SAP->waitingForAckTimeoutCntr_ms = 0;
			stack->layer2SAP->txRetryCounter++;
			*sstate = LMSL2_SSTATE_READY;
		}
		break;
	case LMSL2_SSTATE_READY:
		if (getQueueSize(stack) > 0)
		{
			*sstate = LMSL2_SSTATE_WAITING_FOR_ACK;
			stack->layer2SAP->waitingForAckTimeoutCntr_ms = 0;
			peekNextFromQueueAndTransmit(stack);
		}
		break;
	}
	if (stack->layer2SAP->txRetryCounter >= 10)
	{
		clearQueue(stack);
		stack->layer2SAP->txRetryCounter = 0;
		*sstate = LMSL2_SSTATE_READY;
	}
}


void LMSL2_init(LMStack* stack)
{
	stack->layer2SAP->mtu = stack->layer1SAP->mtu - LMSL2_LEN_PREAMBLE - LMSL2_LEN_CS;
	stack->layer2SAP->serviceState = LMSL2_SSTATE_READY;
	stack->layer2SAP->txRetryCounter = 0;
	stack->layer2SAP->waitingForAckTimeoutCntr_ms = 0;
}

void LMSL2_receive(LMStack* stack, char* data, uint32_t len)
{
	char* buf = stack->layer2SAP->rxBuf;

	if (data[0] == LMSL2_ACK_BYTE && len == 1)
	{
		stack->layer2SAP->serviceState = LMSL2_SSTATE_RECEIVED_ACK;
		return;
	}

	if (data[1] == LMSL2_NACK_BYTE && len == 1)
	{
		stack->layer2SAP->serviceState = LMSL2_SSTATE_RECEIVED_NACK;
		return;
	}

	if (beginsWithSOF(stack, data, len))
	{
		memcpy(&buf[0], data, len);
		stack->layer2SAP->rxBufTargetLen = extractLen(stack) + LMSL2_LEN_PREAMBLE + LMSL2_LEN_CS;
		stack->layer2SAP->rxBufLen = len;
	}
	else
	{
		memcpy(&buf[stack->layer2SAP->rxBufLen], data, len);
		stack->layer2SAP->rxBufLen += len;
	}

	if (hasRxBufReachedTargetLen(stack))
	{
		if(hasFrameInBufValidChecksum(stack))
		{
			sendAck(stack);
			char* frameDat = &buf[LMSL2_POS_DAT];
			uint32_t frameDatLen = extractLen(stack);
			LMSL3_receive(stack, frameDat, frameDatLen);
			resetRxBuf(stack);
		}
		else
			sendNack(stack);
			resetRxBuf(stack);
	}
	else if (hasRxBufExceededTargetLen(stack))
	{
		sendNack(stack);
		resetRxBuf(stack);
	}
}

static bool beginsWithSOF(LMStack* stack, char* data, uint32_t len)
{
	if (data[LMSL2_POS_SOF] == LMSL2_SOF_BYTE)
		return true;
	return false;
}

static uint32_t extractLen(LMStack* stack)
{
	return stack->layer2SAP->rxBuf[LMSL2_POS_LEN];
}

static uint16_t extractChecksum(LMStack* stack)
{
	char* buf = stack->layer2SAP->rxBuf;
	uint32_t dataLen = extractLen(stack);
	uint32_t dataEnd = LMSL2_LEN_PREAMBLE + dataLen + LMSL2_POS_CS_AFTER_DAT;
	uint16_t cs = (buf[dataEnd] << 8) & 0xFF00;
	cs |= (buf[dataEnd + 1] & 0xFF);
	return cs;
}


static bool hasRxBufReachedTargetLen(LMStack* stack)
{
	if (stack->layer2SAP->rxBufLen == stack->layer2SAP->rxBufTargetLen)
		return true;
	return false;
}

static bool hasRxBufExceededTargetLen(LMStack* stack)
{
	if (stack->layer2SAP->rxBufLen > stack->layer2SAP->rxBufTargetLen)
		return true;
	return false;
}

static void sendAck(LMStack* stack)
{
	// Transmit
	char ack[] = {LMSL2_ACK_BYTE};
	LMSL1_transmit(stack, ack, 1);
}

static void sendNack(LMStack* stack)
{
	// Transmit
	char nack[] = {LMSL2_NACK_BYTE};
	LMSL1_transmit(stack, nack, 1);
}

static void resetRxBuf(LMStack* stack)
{
	stack->layer2SAP->rxBufTargetLen = 0;
	stack->layer2SAP->rxBufLen = 0;
}

static bool hasFrameInBufValidChecksum(LMStack* stack)
{
	const uint16_t checksumFromFrame = extractChecksum(stack);
	char* data = &(stack->layer2SAP->rxBuf[LMSL2_POS_DAT]);
	const uint32_t dataLen = extractLen(stack);
	const uint16_t checksumCalc = AFS_calc16bit(data, dataLen);
	if (checksumFromFrame == checksumCalc)
		return true;
	return false;
}

