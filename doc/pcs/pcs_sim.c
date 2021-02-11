#include <stdint.h>
#include <stdio.h>
#include <string.h>

uint16_t calcpcm2(char* data, uint32_t len)
{
    uint16_t qsum = 211;
    uint32_t sum  = 7;
    uint32_t prod = 1;
    for (uint32_t i = len; i > 0; i--)
    {
        qsum += data[i];
    }
    for (uint32_t i = 0; i < len; i++)
    {
        sum  = sum + data[i] * qsum & 0x0000FFFF;
        prod = ((uint32_t)(prod + sum)) & 0x0000FFFF;
        prod = ((uint32_t)(prod * sum)) & 0x0000FFFF;
        printf("Word: %c - qsum: %04x - sum: %04x - prod: %04x\n",data[i], qsum, sum, prod);
    }
    return (uint16_t)prod;

}


uint16_t calcpcm(char* data, uint32_t len)
{
    uint32_t sum  = 7;
    uint32_t prod = 1;
    for (uint32_t i = 0; i < len; i++)
    {
	sum  = sum + data[i];
        prod = ((uint32_t)(prod + sum)) & 0x0000FFFF;
	prod = ((uint32_t)(prod * sum)) & 0x0000FFFF;
        printf("Word: %c - Sum: %08x - Prod: %04x\n",data[i], sum, prod);
    }
    return (uint16_t)prod;

}

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
    	printf("Wrong number of arguments\n");
        return 0;
    }
    uint16_t pcm = calcpcm2(argv[1], strlen(argv[1]));
    printf("PCM of String <%s> is: %d\n", argv[1], pcm);
    return 1;
}
