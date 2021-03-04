
#ifndef LMSTACK_H
#define LMSTACK_H

#include <stdbool.h>
#include <stdint.h>

#define LMStack_NUM_MAX_PREAMBLE_BYTES 8

// HWI
typedef struct {
	void (*onReceive)(char* data, uint32_t len);
	uint32_t mtu;
} LMSLayer1HwInterface;

// Layer 1
typedef struct {
	uint32_t mtu;
} LMSLayer1SAP;

// Layer 2
typedef enum {
	LMSL2_SSTATE_READY,
	LMSL2_SSTATE_WAITING_FOR_ACK,
	LMSL2_SSTATE_RECEIVED_ACK,
	LMSL2_SSTATE_RECEIVED_NACK

} LMSLayer2ServiceState;

typedef struct {
	uint32_t mtu;
	char rxBuf[256]; // TODO LEN
	uint32_t rxBufTargetLen;
	uint32_t rxBufLen;
	uint32_t txRetryCounter;
	uint32_t waitingForAckTimeoutCntr_ms;
	LMSLayer2ServiceState serviceState;
} LMSLayer2SAP;

// Layer 3
typedef enum {
	LMSL3_MSG_TYPE_SHORT, LMSL3_MSG_TYPE_LONG
} LMSLayer3MessageType;

typedef enum {
	LMSL3_SERVICE_STATE_READY, LMSL3_SERVICE_STATE_WAITING_FOR_ACK
} LMSLayer3ServiceState;

typedef struct {
} LMSLayer3SAP;

// Stack
typedef enum {
	LMS_SSTATE_READY, LMS_SSTATE_BUSY
} LMStackServiceState;

typedef struct {
	void (*onReceive)(char* data, uint32_t len);
	LMSLayer1HwInterface* hwi;
	LMSLayer1SAP* layer1SAP;
	LMSLayer2SAP* layer2SAP;
	LMSLayer3SAP* layer3SAP;
} LMStack;

void LMS_sendMessageShort     	  (LMStack* stack, char* data, uint32_t len);
void LMStack_sendMessageLong      (LMStack* stack, char* data, uint32_t len);
void LMStack_setOnReceiveListener (LMStack* stack, void (*onReceive)(char* data, uint32_t len));
void LMS_init				  (LMStack* stack);

#endif
