package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    // TODO: Implement here the TFTP encoder and decoder
    private byte[] bytes = new byte[1 << 10]; // start with 1k
    private int len = 0;
    private int numOfBytes = 0;
    private int opcode = 0;

    public short byteToShort(byte[] b) {
        short b_short = (short) (((short) bytes[0] & 0xFF) << 8 | (short) (bytes[1]));
        return b_short;
    }

    public Byte[] shortToByte(short a) {
        Byte[] a_bytes = new Byte[] { (byte) (a >> 8), (byte) (a & 0xff) };
        return a_bytes;
    }

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this
        byte[] dataSize = new byte[2];
        short size = 0;

        if (numOfBytes == 0) {
            bytes[numOfBytes] = nextByte;
            numOfBytes++;
            return null;
        }
        if (numOfBytes == 1) {
            opcode = Byte.toUnsignedInt(nextByte);
            bytes[numOfBytes] = nextByte;
            numOfBytes++;
            return null;
        }
        if (numOfBytes >= 2) {
            if (Byte.toUnsignedInt(nextByte) == 0 && opcode != 3 && opcode != 4 && opcode != 5) {
                return bytes;
            } else if (opcode == 3) {
                if (numOfBytes == 3) {
                    dataSize[0] = nextByte;
                }
                if (numOfBytes == 4) {
                    dataSize[1] = nextByte;
                    size = byteToShort(dataSize);
                }
                if (numOfBytes == size) {
                    return bytes;
                }
            } else if (opcode == 4 && numOfBytes >= 4) {
                bytes[numOfBytes] = nextByte;
                numOfBytes++;
                return bytes;
            } else if (opcode != 5 && numOfBytes >= 4 && Byte.toUnsignedInt(nextByte) == 0) {
                return bytes;
            }
            bytes[numOfBytes] = nextByte;
            numOfBytes++;
        }
        return null;

    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }

    private void pushByte(Byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

}