
#ifndef AFS_H
#define AFS_H

#include <stdint.h>
#include "AFS.h"

uint16_t AFS_calc16bit(char* data, uint32_t len);
uint32_t AFS_calc32bit(char* data, uint32_t len);
uint32_t AFS_calc32bitOnMultipleArrays(char* data[], uint32_t dataLen[], uint32_t len);


#endif
