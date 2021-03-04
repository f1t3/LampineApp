
#include <stdio.h>

#include "LMStack.h"
#include "LMSLayer1HardwareInterface.h"
#include "Log.h"

static const char* TAG = "LMSL1ServiceProvider";

bool isPrintable(char c)
{
	if ((c >= ' ') && (c <= '~'))
		return true;
	return false;
}

void LMSL1_transmit(LMStack* stack, char* data, uint32_t len)
{
//	printf("TX: ");
//	uint32_t i = 0;
//	unsigned char c;
//	for (; i < len-1; i++)
//	{
//		c = data[i];
//		if (isPrintable(c))
//			printf("%c (%0Xh,%dd), ", c, c ,c);
//		else
//			printf("(%0Xh,%dd), ", c ,c);
//	}
//	c = data[i];
//	if (isPrintable(c))
//		printf("%c (%0Xh,%dd)\n", c, c ,c);
//	else
//		printf("(%0xh, %dd)\n", c ,c);
	const uint32_t HWMTU = stack->hwi->mtu;
	uint32_t bytesSend = 0;
	uint32_t bytesLeft = len;
	while (bytesLeft > HWMTU)
	{
		LMSHWI_transmit(&data[bytesSend], HWMTU);
		bytesSend += HWMTU;
		bytesLeft -= HWMTU;
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
