
#ifndef LMSLAYER2SAP_H
#define LMSLAYER2SAP_H

#include <stdint.h>
#include "LMStack.h"

#define LMSL2_MTU 	16

#define LMSL2_SOF_BYTE				0x02
#define LMSL2_ACK_BYTE				0x06
#define LMSL2_NACK_BYTE				0x15

#define LMSL2_POS_SOF				0
#define LMSL2_POS_LEN				1
#define LMSL2_POS_DAT				2
#define LMSL2_POS_CS_AFTER_DAT		0

#define LMSL2_LEN_PREAMBLE			(LMSL2_POS_DAT)
#define LMSL2_LEN_CS				(2)

void LMSL2_transmit				(LMStack* stack, char* data, uint32_t len);
void LMSL2_init					(LMStack* stack);
void LMSL2_receive				(LMStack* stack, char* data, uint32_t len);

#endif
