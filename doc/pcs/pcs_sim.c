#include <stdint.h>
#include <stdio.h>
#include <string.h>

uint16_t calcpcm2(char* data, uint32_t len)
{
    uint16_t sumbuf[len] = "";
    uint32_t sum  = 7;
    uint32_t prod = 1;
    sumbuf[0] = 221;
    for (uint32_t i = len; i > 0; i--)
    {
        sumbuf[len] = (char)(sumbuf[len] + data[i]);
    }
    for (uint32_t i = 0; i < len; i++)
    {
        sum  = sum + data[i];
        prod = ((uint32_t)(prod + sum)) & 0x0000FFFF;
        prod = ((uint32_t)(prod * sum)) & 0x0000FFFF;
        printf("Word: %c - Sum: %08x - Prod: %04x\n",data[i], sum, prod);
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
    uint16_t pcm = calcpcm(argv[1], strlen(argv[1]));
    printf("PCM of String <%s> is: %d\n", argv[1], pcm);
    return 1;
}
