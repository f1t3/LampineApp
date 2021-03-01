
#include <unistd.h>
#include "LMSLayer2SAP.h"
#include "LMSFrame.h"
#include "LMSFrameAssembler.h"

static void waitForServiceReady(LMSLayer2SAP* sap);
static void tryToTransmitUntilAckOrTimeout(LMStack* stack, char* data, uint32_t len);

void LMSL2_transmit(LMStack* stack, char* data, uint32_t len)
{
	stack->layer2SAP->serviceState = LMSL2_SSTATE_WAITING_FOR_ACK;
	uint32_t cntr = 0;
	while (stack->layer2SAP->serviceState != LMSL2_SSTATE_READY)
	{
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
		sleep(2);
		cntr++;
		if (cntr > 5) return;
	}
}

void LMSL2_init(LMStack* stack)
{
	stack->layer2SAP->mtu = stack->layer1SAP->mtu - LMSL2_LEN_PREAMBLE - LMSL2_LEN_CS;
	stack->layer2SAP->serviceState = LMSL2_SSTATE_READY;
}

void LMSL2_receive(LMStack* stack, char* data, uint32_t len)
{
	if (data[0] == LMSL2_ACK_BYTE && len == 1)
		stack->layer2SAP->serviceState = LMSL2_SSTATE_READY;
	if (data[1] == LMSL2_NACK_BYTE && len == 1)
		stack->layer2SAP->serviceState = LMSL2_SSTATE_WAITING_FOR_RECEIVED_NACK;
	// TODO: PASS TO LAYER 3
}

static void tryToTransmitUntilAckOrTimeout(LMStack* stack, char* data, uint32_t len)
{
	stack->layer2SAP->serviceState = LMSL2_SSTATE_WAITING_FOR_ACK;
	uint32_t trycntr = 0;
	LMSL1_transmit(stack, data, len);
	while (stack->layer2SAP->serviceState != LMSL2_SSTATE_READY)
	{
		if (stack->layer2SAP->serviceState == LMSL2_SSTATE_WAITING_FOR_RECEIVED_NACK)
		{
			LMSL1_transmit(stack, data, len);
			stack->layer2SAP->serviceState = LMSL2_SSTATE_WAITING_FOR_ACK;
		}
	}
}

static void waitForServiceReady(LMSLayer2SAP* sap)
{
	// TODO: TIMEOUT!!!
	while (sap->serviceState != LMSL2_SSTATE_READY);
}
