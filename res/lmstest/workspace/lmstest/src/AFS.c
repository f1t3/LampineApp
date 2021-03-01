
#include "AFS.h"

uint16_t AFS_calc16bit(char* data, uint32_t len)
{
    uint32_t sum1 = 0;
    uint32_t sum2 = 0;
    for (uint32_t i = 0; i < len; i++)
    {
        // Modulo 2^16-1
        sum1 = sum1 + sum2 + data[i];
        sum1 = (sum1 & 0xFFFF) + (sum1 >> 16);
        sum2 = sum1 + sum2;
        sum2 = (sum2 & 0xFFFF) + (sum2 >> 16);
    }
    // Modulo 2^8-1
    sum1 = ((sum1 & 0xFF) + (sum1 >> 8));
    sum2 = ((sum2 & 0xFF) + (sum2 >> 8));
    return (uint16_t)((sum1 << 8) | (sum2));
}

uint32_t AFS_calc32bit(char* data, uint32_t len)
{
    uint32_t sum1 = 0;
    uint32_t sum2 = 0;
    for (uint32_t i = 0; i < len; i++)
    {
        // Modulo 2^16-1
        sum1 = sum1 + sum2 + data[i];
        sum1 = (sum1 & 0xFFFF) + (sum1 >> 16);
        sum2 = sum1 + sum2;
        sum2 = (sum2 & 0xFFFF) + (sum2 >> 16);
    }
    // Modulo 2^8-1
    sum1 = ((sum1 & 0xFF) + (sum1 >> 8));
    sum2 = ((sum2 & 0xFF) + (sum2 >> 8));
    return (uint16_t)((sum1 << 8) | (sum2));
}

uint32_t AFS_calc32bitOnMultipleArrays(char* data[], uint32_t dataLen[], uint32_t len)
{
    uint64_t sum1 = 0;
    uint64_t sum2 = 0;
    for (uint32_t arrIndex = 0; arrIndex < len; arrIndex++)
    {
		for (uint32_t i = 0; i < dataLen[arrIndex]; i++)
		{
			// Modulo 2^32-1
			sum1 = sum1 + sum2 + data[arrIndex][i];
			sum1 = (sum1 & 0xFFFFFFFF) + (sum1 >> 32);
			sum2 = sum1 + sum2;
			sum2 = (sum2 & 0xFFFFFFFF) + (sum2 >> 32);
		}
    }
    // Modulo 2^16-1
    sum1 = ((sum1 & 0xFFFF) + (sum1 >> 16));
    sum2 = ((sum2 & 0xFFFF) + (sum2 >> 16));
    return (uint32_t)((sum1 << 16) | (sum2));
}

