
#include "LMStack.h"
#include "LMSLayer1HardwareInterface.h"
#include "Log.h"

static const char* TAG = "LMSL1ServiceProvider";

void LMSL1_transmit(LMStack* stack, char* data, uint32_t len)
{
	const uint32_t HWMTU = stack->hwi->mtu;
	uint32_t bytesSend = 0;
	uint32_t bytesLeft = len;
	while (bytesLeft > HWMTU)
	{
		LMSHWI_transmit(&data[bytesSend], HWMTU);
		bytesSend += HWMTU;
		bytesLeft -= HWMTU;
		// TODO: ENSURE RECEIVER HAS TIME TO PROCESS PACK
	}
	LMSHWI_transmit(&data[bytesSend], bytesLeft);
}

void LMSL1_init(LMStack* stack)
{
	stack->layer1SAP->mtu = stack->hwi->mtu;
}

void LMSL1_receive(LMStack* stack, char* data, uint32_t len)
{
	// Pass directly to layer 2
	LMSL2_receive(stack, data, len);
}
