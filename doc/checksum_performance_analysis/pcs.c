#include <stdint.h>

uint16_t pcm(const char const* data, uint32_t len)
{
    uint32_t sum  = 7;
    uint32_t prod = 1;
    for (uint32_t i = 0; i < len; i++)
    {
        sum  = sum + data[i];
        prod = ((uint32_t)(prod * sum)) & 0x0000FFFF;
    }
    return (uint16_t)prod;
}