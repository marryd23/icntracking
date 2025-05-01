/*
 * Copyright (C) Aer Networks 2024
 * Author: Anders Dam Kofoed
 */

package com.example.icntracking;

import java.nio.ByteBuffer;


public class ZMeshFrameHelper {
    
    public final static int ZMESH_MAX_NAME_LEN              = 64;
    public final static int ZMESH_FRAME_MAX_PAYLOAD_LEN     = 64;
    public final static int ZMESH_FHDR_LEN                  = 1;
    public final static int ZMESH_NETID_LEN                 = 4;
    public final static int ZMESH_NAME_HASH_LEN             = 6;
    public final static int ZMESH_FCTRL_LEN                 = 1;
    public final static int ZMESH_FSEQ_LEN                  = 3;
    public final static byte[] ZMESH_FSEQ_MAX               = ZMeshUtils.hexToBytes("ffffff");
    public final static int ZMESH_MAC_LEN                   = 4;
    public final static int ZMESH_HDR_LEN                   = (ZMESH_FHDR_LEN + ZMESH_NAME_HASH_LEN + ZMESH_FCTRL_LEN + ZMESH_FSEQ_LEN);
    public final static int ZMESH_MAX_PAYLOAD_LEN           = (ZMESH_FRAME_MAX_PAYLOAD_LEN - ZMESH_HDR_LEN - ZMESH_NETID_LEN - ZMESH_MAC_LEN);
    public final static int ZMESH_NWK_KEY_MAX_NUM           = 4;
    public final static int ZMESH_FEATURE_NAME_MAX_LEN      = 8;
    public final static int ZMESH_FEATURE_PROPS_LEN         = 1;
    public final static int ZMESH_FEATURE_CFGLEN_LEN        = 1;
    public final static int ZMESH_FEATURE_EVENT_INTVAL_LEN  = 2;
    public final static int ZMESH_STATUS_BATT_LEN           = 1;
    public final static int ZMESH_STATUS_FEATURE_LEN        = 1;
    public final static int ZMESH_DID_LEN                   = 8;
    public final static int ZMESH_DID_STR_LEN               = (ZMESH_DID_LEN*2);
    public final static int ZMESH_ENC_IV_LEN                = 9;
    public final static int ZMESH_ENC_KEY_LEN               = 16;
    public final static int ZMESH_FRAME_VERSION             = 0;
    public final static int ZMESH_NWK_KEYPROPS_LEN          = 1;
    public final static short ZMESH_TTL_MIN                 = 1;
    public final static short ZMESH_TTL_MAX                 = 7;
    
    public final static int ZMESH_ENCMETHOD_NONE            = (1 << 0);
    public final static int ZMESH_ENCMETHOD_AES128_CTR      = (1 << 1);

    public final static int PACKET_TYPE_INTEREST            = 0;
    public final static int PACKET_TYPE_CONTENT             = 1;
    public final static int PACKET_TYPE_INTEREST_RETURN     = 2;
    public final static int PACKET_TYPE_CONTENT_ANNOUNCEMENT = 3;

    public final static int INTEREST_FSEQ_LAST              = 0;
    public final static int INTEREST_FSEQ_SUBSCRIBE         = 0xffffff;
    public final static int ZMESH_INTEREST_EXPIRE_LEN       = 2;
    public final static int ZMESH_TIMESTAMP_LEN             = 6;
    public final static long ZMESH_NETID_LOCALNET           = 0L;
    public final static long ZMESH_NETID_PUBLIC               = 1L;
    public final static int ZMESH_UDP_PAYLOAD_LEN           = 1280;
    public final static int ZMESH_UDP_HEADER_LEN            = 8;

    // public final static int ZMESH_LOCAL_FACE_NUM = 127;


    public static int getFhdrVersion(byte[] frame) {
        return (frame[0] >> 6) & 2;
    }

    public static int getFhdrNetId(byte[] frame) {
        return (frame[0] >> 5) & 1;
    }

    public static int getFhdrProxyMe(byte[] frame) {
        return (frame[0] >> 4) & 1;
    }

    public static void setFhdrTtl(byte[] frame, short ttl) {
        if (ttl > 7) {
            return;
        }
        byte fhdr = (byte) ((frame[0] & 0xF8) | (byte) ttl);
        frame[0] = fhdr;
    }

    public static short getFhdrTtl(byte[] frame) {
        return (short) (frame[0] & 7);
    }

    /**
     * @param frame
     * @return Returns netId or -1 if not present
     */
    public static long getNetId(byte[] frame) {
        if (getFhdrNetId(frame) == 0) {
            return -1;
        }
        long netId = 
            ((frame[ZMESH_FHDR_LEN]     & 0xFF) << 24) |
            ((frame[ZMESH_FHDR_LEN + 1] & 0xFF) << 16) |
            ((frame[ZMESH_FHDR_LEN + 2] & 0xFF) << 8) |
            ((frame[ZMESH_FHDR_LEN + 3] & 0xFF) << 0);
        return netId;
    }
    
    public static byte[] getName(byte[] frame) {
        int offset = ZMESH_FHDR_LEN;
        if (getFhdrNetId(frame) == 1) {
            offset += ZMESH_NETID_LEN;
        }
        byte[] name = new byte[ZMESH_NAME_HASH_LEN];
        System.arraycopy(frame, offset, name, 0, ZMESH_NAME_HASH_LEN);
        return name;
    }
    
    public static int getFctrlNwkKeyId(byte[] frame) {
        int offset = ZMESH_FHDR_LEN + ZMESH_NAME_HASH_LEN;
        if (getFhdrNetId(frame) == 1) {
            offset += ZMESH_NETID_LEN;
        }
        return (frame[offset] >> 6) & 3;
    }

    public static int getFctrlPacketType(byte[] frame) {
        int offset = ZMESH_FHDR_LEN + ZMESH_NAME_HASH_LEN;
        if (getFhdrNetId(frame) == 1) {
            offset += ZMESH_NETID_LEN;
        }
        return frame[offset] & 3;
    }

    public static int getFseq(byte[] frame) {
        int offset = ZMESH_FHDR_LEN + ZMESH_NAME_HASH_LEN + ZMESH_FCTRL_LEN;
        if (getFhdrNetId(frame) == 1) {
            offset += ZMESH_NETID_LEN;
        }
        int fseq = 
            ((frame[offset]     & 0xFF) << 16) |
            ((frame[offset + 1] & 0xFF) << 8)  |
            ((frame[offset + 2] & 0xFF) << 0);
        return fseq;
    }


    public static byte[] getPayload(byte[] frame) {
        int fhdrLen = ZMESH_FHDR_LEN + ZMESH_NAME_HASH_LEN + ZMESH_FCTRL_LEN + ZMESH_FSEQ_LEN;
        if (getFhdrNetId(frame) == 1) {
            fhdrLen += ZMESH_NETID_LEN;
        }
        int contentLength = frame.length - fhdrLen - ZMESH_MAC_LEN;
        byte[] content = new byte[contentLength];
        System.arraycopy(frame, fhdrLen, content, 0, contentLength);
        return content;
    }


    public static byte[] getMac(byte[] frame) {
        int macPos = frame.length - ZMESH_MAC_LEN;
        byte[] mac = new byte[ZMESH_MAC_LEN];
        System.arraycopy(frame, macPos, mac, 0, ZMESH_MAC_LEN);
        return mac;
    }

    // AES-128-CMAC function
    public static byte[] verifyZMeshCmac(byte[] frame, byte[] key) {
        int fhdrLen = ZMESH_FHDR_LEN;
        if (getFhdrNetId(frame) == 1) {
            fhdrLen += ZMESH_NETID_LEN;
        }
        // Generate CMAC
        Aes128Cmac aes128Cmac = new Aes128Cmac();
        aes128Cmac.doInit(key);
        // Offset the FHDR - MAC calculation is done from topicHash
        aes128Cmac.doUpdate(frame, fhdrLen, frame.length - ZMESH_MAC_LEN - (fhdrLen));
        byte[] cmac = aes128Cmac.doFinal();
        // Copy it into ZMesh MAC format (last 4 bytes of the CMAC)
        byte[] zmeshCmac = new byte[ZMeshFrameHelper.ZMESH_MAC_LEN];
        System.arraycopy(cmac, 12, zmeshCmac, 0, ZMeshFrameHelper.ZMESH_MAC_LEN);
        
        return zmeshCmac;
    }
    

    // AES-128-CMAC function
    public static byte[] generateZMeshCmac(byte[] data, int dataOffset, int dataLen, byte[] key) {
        // Generate CMAC
        Aes128Cmac aes128Cmac = new Aes128Cmac();
        aes128Cmac.doInit(key);
        // Offset the FHDR - MAC calculation is done from topicHash
        aes128Cmac.doUpdate(data, dataOffset, dataLen);
        byte[] cmac = aes128Cmac.doFinal();

        // Copy it into ZMesh MAC format (last 4 bytes of the CMAC)
        byte[] zmeshCmac = new byte[ZMeshFrameHelper.ZMESH_MAC_LEN];
        System.arraycopy(cmac, 12, zmeshCmac, 0, ZMeshFrameHelper.ZMESH_MAC_LEN);
        
        return zmeshCmac;
    }
    
    /**
     * @brief Make a Z-Mesh frame
     * @param ttl
     * @param netid
     * @param name
     * @param packetType
     * @param fseq
     * @param networkKeyId
     * @param nwkKey
     * @param payload
     * @return The frame as a byte array
     */
    public static byte[] makeFrame(int ttl, long netId, byte[] name, int packetType, int fseq, int networkKeyId, NetworkKey nwkKey, byte[] payload) {
        // Check stuff
        if (nwkKey == null) {
            return null;
        }
        
        int payloadLen = 0;
        if (payload == null) {
            payload = new byte[0];
        } else {
            payloadLen = payload.length;
        }

        int fhdrLen = ZMESH_HDR_LEN;
        int nameHashOffset = ZMESH_FHDR_LEN;

        if (netId != ZMESH_NETID_LOCALNET && netId != ZMESH_NETID_PUBLIC) {
            fhdrLen += ZMESH_NETID_LEN;
            nameHashOffset += ZMESH_NETID_LEN;
        }

        int frameLen = fhdrLen + payloadLen + ZMESH_MAC_LEN;
        byte[] frame = new byte[frameLen];
        ByteBuffer frameBuf = ByteBuffer.wrap(frame);

        if (netId == ZMESH_NETID_LOCALNET || netId == ZMESH_NETID_PUBLIC) {
            // FHDR (Version, NetID not present and TTL)
            frameBuf.put((byte) ((ZMESH_FRAME_VERSION << 6) | (0<<5) | ttl));
        } else {
            // FHDR (Version, NetID present and TTL)
            frameBuf.put((byte) ((ZMESH_FRAME_VERSION << 6) | (1<<5) | ttl));
            // Network ID
            frameBuf.put((byte) (netId >>> 24));
            frameBuf.put((byte) (netId >>> 16));
            frameBuf.put((byte) (netId >>> 8));
            frameBuf.put((byte) (netId));
        }
        // Check name length. Must be hash length
        if (name.length != ZMESH_NAME_HASH_LEN) {
            return null;
        }
        // Content Name
        frameBuf.put(name);
        // FCTRL
        frameBuf.put((byte) ((networkKeyId << 6) | packetType));
        // FSEQ
        frameBuf.put((byte) (fseq >>> 16));
        frameBuf.put((byte) (fseq >>> 8));
        frameBuf.put((byte) (fseq));
        // Payload
        frameBuf.put(payload);

        // Generate CMAC
        byte[] nwkKeyBytes = ZMeshUtils.hexToBytes(nwkKey.getKey());
        byte[] cmac = generateZMeshCmac(frame, nameHashOffset, (frame.length - nameHashOffset - ZMESH_MAC_LEN), nwkKeyBytes);
        frameBuf.put(cmac);

        return frame;
    }
    

    public static long getInterestTimestamp(byte[] frame) {
        long interestTimestamp = (frame[ZMESH_HDR_LEN+0] & 0xFFL) << 40 |
                                 (frame[ZMESH_HDR_LEN+1] & 0xFFL) << 32 |
                                 (frame[ZMESH_HDR_LEN+2] & 0xFFL) << 24 |
                                 (frame[ZMESH_HDR_LEN+3] & 0xFFL) << 16 |
                                 (frame[ZMESH_HDR_LEN+4] & 0xFFL) << 8  |
                                 (frame[ZMESH_HDR_LEN+5] & 0xFFL);

        return interestTimestamp;
    }

    /**
     * @brief Get Interest Expire from the Interest Payload
     * @param frame
     * @return
     */
    public static short getInterestExpire(byte[] frame) {
        short interestExpireSecs = (short) (((frame[ZMESH_HDR_LEN+6] & 0xFF) << 8) | ((frame[ZMESH_HDR_LEN+7]) & 0xFF));
        return interestExpireSecs;
    }

    public static byte[] generateInterestExpireByteValue(short expireSecs) {
        byte[] expireSecsByteArr = new byte[2];
        expireSecsByteArr[0] = (byte) (expireSecs >> 8);
        expireSecsByteArr[1] = (byte) expireSecs;
        return expireSecsByteArr;
    }

    public static byte[] generateInterestPayload(long timestamp, short expireSecs) {
        byte[] interestPayloadByteArr = new byte[ZMESH_TIMESTAMP_LEN + ZMESH_INTEREST_EXPIRE_LEN];
        // Timestamp
        interestPayloadByteArr[0] = (byte) (timestamp >>> 40);
        interestPayloadByteArr[1] = (byte) (timestamp >>> 32);
        interestPayloadByteArr[2] = (byte) (timestamp >>> 24);
        interestPayloadByteArr[3] = (byte) (timestamp >>> 16);
        interestPayloadByteArr[4] = (byte) (timestamp >>> 8);
        interestPayloadByteArr[5] = (byte) timestamp;
        // Expire secs
        interestPayloadByteArr[6] = (byte) (expireSecs >>> 8);
        interestPayloadByteArr[7] = (byte) expireSecs;
        return interestPayloadByteArr;
    }
}

