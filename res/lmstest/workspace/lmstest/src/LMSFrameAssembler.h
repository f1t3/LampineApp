
#ifndef LMSLAYER2FRAMEASSEMBLER_H
#define LMSLAYER2FRAMEASSEMBLER_H

typedef struct {

} FrameAssembler;

void LMSFrameAssembler_put(FrameAssembler* assembler, char* data, uint32_t len);
#endif
