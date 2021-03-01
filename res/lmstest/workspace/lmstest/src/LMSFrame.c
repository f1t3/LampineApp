//
//#include "LMSFrame.h"
//#include "AFS.h"
//
//static const char* TAG = "LMSFrame";
//
//LMSFrame* LMSFrame_newTxFrame(LMSFrame* frame, char* data, uint32_t len)
//{
//	frame->buf = data,
//	frame->dataLen = len,
//	frame->checksum = AFS_calc16bit(data, len)
//	return frame;
//}
//
//bool LMSFrame_isValid(LMSFrame* frame)
//{
//	if (!isFrameLenValid(frame))
//		return false;
//	if (!hasValidChecksum(frame))
//		return false;
//	return true;
//}
//
//void LMSFrame_getBytes(LMSFrame* frame, char* frame);
//
//void LMSFrame_getDataBytes(LMSFrame* frame, char* frame);
//
//
//
//static bool isFrameLenValid(LMSFrame* frame)
//{
//	// Has at least one data byte?
//	if (frame->dataLen < 1)
//		return false;
//}
//
//static bool hasValidChecksum(LMSFrame* frame)
//{
//	if (!isFrameLenValid(frame))
//		return false;
//	const uint16_t sum = AFS_calc16bit(frame->data, frame->dataLen);
//	const uint16_t sumReceived = (frame->checksum[0] << 8) | (frame->checksum[1]);
//	if (sum != sumReceived)
//		return false;
//	return true;
//}
