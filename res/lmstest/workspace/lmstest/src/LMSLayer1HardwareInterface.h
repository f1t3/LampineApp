
#ifndef LMSLAYER1HARDWAREINTERFACE_H
#define LMSLAYER1HARDWAREINTERFACE_H

#include <stdbool.h>
#include <stdint.h>

#include "LMStack.h"

void LMSHWI_transmit				(char* data, uint32_t len);
void LMSHWI_init					(LMSLayer1HwInterface* hwi);
void LMSHWI_receive					(LMSLayer1HwInterface* hwi, char* data, uint32_t len);
void LMSLHWI_setOnReceiveListener	(LMSLayer1HwInterface* sap, void (*onReceive)(char* data, uint32_t len));

#endif
