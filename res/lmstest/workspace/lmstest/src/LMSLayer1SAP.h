
#ifndef LMSLAYER1SAP_H
#define LMSLAYER1SAP_H

#include "LMSLayer1HardwareInterface.h"
#include "LMStack.h"


void LMSL1_transmit				(LMStack* stack, char* data, uint32_t len);
void LMSL1_init					(LMStack* stack);
void LMSL1_receive				(LMStack* stack, char* data, uint32_t len);

#endif
