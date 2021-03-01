
#ifndef LMSLAYER23AP_H
#define LMSLAYER23AP_H

#include <stdint.h>

#include "LMStack.h"

#define LMSL3_MAX_LEN_SHORT_MSG			128

#define LMSL3_SOM_BYTE 					0x04
#define LMSL3_EOM_BYTE 					0x05
#define LMSL3_TYPE_SHORT_BYTE 			0x01
#define LMSL3_TYPE_LONG_BYTE 			0x02

#define LMSL3_NUM_SOM_BYTES 			1
#define LMSL3_NUM_EOM_BYTES 			1

#define LMSL3_NUM_TYPE_BYTES 			1
#define LMSL3_NUM_LEN_BYTES 			4
#define LMSL3_NUM_CS_BYTES 				4

#define LMSL3_NUM_PREAMBLE_BYTES_SHORT 	(LMSL3_POS_DAT_SHORT)
#define LMSL3_NUM_POSTAMBLE_BYTES_SHORT (LMSL3_NUM_EOM_BYTES)

#define LMSL3_NUM_PREAMBLE_BYTES_LONG  	(LMSL3_POS_DAT_LONG)
#define LMSL3_NUM_POSTAMBLE_BYTES_LONG	(LMSL3_POS_EOM_AFTER_DAT_LONG)

#define LMSL3_POS_SOM  					0
#define LMSL3_POS_TYPE  				(LMSL3_NUM_SOM_BYTES)

#define LMSL3_POS_DAT_SHORT 			(LMSL3_POS_TYPE + LMSL3_NUM_TYPE_BYTES)
#define LMSL3_POS_EOM_AFTER_DAT_SHORT 	0

#define LMSL3_POS_LEN_LONG				(LMSL3_POS_TYPE + LMSL3_NUM_TYPE_BYTES)
#define LMSL3_POS_DAT_LONG  			(LMSL3_POS_LEN_LONG + LMSL3_NUM_LEN_BYTES)
#define LMSL3_POS_CS_AFTER_DAT_LONG    	0
#define LMSL3_POS_EOM_AFTER_DAT_LONG	(LMSL3_NUM_CS_BYTES)


void LMSL3_transmit(LMStack* stack, LMSLayer3MessageType type, char* data, uint32_t len);
void LMSL3_init(LMStack* stack);
void LMSL3_receive(LMStack* stack, char* data, uint32_t len);

#endif
