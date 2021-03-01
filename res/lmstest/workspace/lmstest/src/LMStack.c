
#include "LMStack.h"

void LMS_sendMessageShort(LMStack* stack, char* data, uint32_t len)
{
	LMSL3_transmit(stack, LMSL3_MSG_TYPE_SHORT, data, len);
}

void sendMessageLong(LMStack* stack, char* data, uint32_t len)
{
	LMSL3_transmit(stack->layer3SAP, LMSL3_MSG_TYPE_LONG, data, len);
}

void LMS_init(LMStack* stack)
{
	LMSHWI_init(stack->hwi);
	LMSL1_init(stack);
	LMSL2_init(stack);
	LMSL3_init(stack);
}
