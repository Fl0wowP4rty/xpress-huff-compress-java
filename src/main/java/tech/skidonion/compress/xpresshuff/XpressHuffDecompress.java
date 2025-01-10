package tech.skidonion.compress.xpresshuff;

import java.nio.ByteBuffer;

import static tech.skidonion.compress.xpresshuff.HuffmanDecoder.INVALID_SYMBOL;
import static tech.skidonion.compress.xpresshuff.MSCompStatus.*;

public class XpressHuffDecompress {
    public static final int CHUNK_SIZE = 0x10000;
    public static final int STREAM_END = 0x100;
    public static final int SYMBOLS = 0x200;
    public static final int HALF_SYMBOLS = 0x100;
    public static final int HUFF_BITS_MAX = 15;
    public static final int MIN_DATA = HALF_SYMBOLS + 4;

    private static MSCompStatus chunk(ByteBuffer _in, ByteBuffer _out, HuffmanDecoder decoder) {
        InputBitStream bstr = new InputBitStream(_in);
        long len;
        int off;
        short sym;
        int out_end_chunk = _out.position() + CHUNK_SIZE;
        while (_out.position() < out_end_chunk || !bstr.maskIsZero()) {
            sym = decoder.decodeSymbol(bstr);
            if ((sym == INVALID_SYMBOL)) {
                System.err.print("XPRESS Huffman Decompression Error: Invalid data: Unable to read enough bits for symbol\n");
                return MSCOMP_DATA_ERROR;
            }
            if (sym == STREAM_END && bstr.remainingRawBytes() == 0 && bstr.maskIsZero()) {
                _in.position(bstr.position());
                return MSCOMP_STREAM_END;
            }
            if (sym < 0x100) {
                _out.put((byte) sym);
            } else {
                int off_bits = ((sym >>> 4) & 0xF);
                if ((len = sym & 0xF) == 0xF) {
                    if ((len = (bstr.readRawByte() & 0xFF)) == 0xFF) {
                        if (((len = (bstr.readRawInt16()) & 0xFFFF) == 0)) {
                            if ((bstr.remainingRawBytes() < 4)) {
                                System.err.print("XPRESS Huffman Decompression Error: Invalid data: Unable to read four bytes for length\n");
                                return MSCOMP_DATA_ERROR;
                            }
                            len = (bstr.readRawInt32() & 0xFFFF_FFFFL);
                        }
                        if ((len < 0xF)) {
                            System.err.print("XPRESS Huffman Decompression Error: Invalid data: Invalid length specified\n");
                            return MSCOMP_DATA_ERROR;
                        }
                        len -= 0xF;
                    }
                    len += 0xF;
                }
                len += 3;
                off = bstr.readBits(off_bits) + (1 << off_bits);
                if (off == 1) {
                    byte val = _out.get(_out.position() - 1);
                    for (int i = 0; i < len; i++) {
                        _out.put(val);
                    }
                } else {
                    long end;
                    for (end = _out.position() + len; _out.position() < end; ) {
                        _out.put(_out.get(_out.position() - off));
                    }
                }
            }
        }
        _in.position(bstr.position());
        if (decoder.decodeSymbol(bstr) == STREAM_END && bstr.remainingRawBytes() == 0 && bstr.maskIsZero()) {
            _in.position(bstr.position());
            return MSCOMP_STREAM_END;
        }
        return MSCOMP_OK;
    }

    /**
     * @param in it is position sensitive
     * @param out you need to manually ensure the capacity
     * */
    public static MSCompStatus decompress(ByteBuffer in, ByteBuffer out) {
        MSCompStatus status;
        HuffmanDecoder decoder = new HuffmanDecoder();
        byte[] code_lengths = new byte[SYMBOLS];
        do {
            if ((in.capacity() - in.position() < MIN_DATA)) {
                if (in.position() != in.capacity()) {
                    System.err.printf("Xpress Huffman Decompression Error: Invalid Data: Less than %d input bytes\n", MIN_DATA);
                    return MSCOMP_DATA_ERROR;
                }
                break;
            }
            for (int i = 0, i2 = 0; i < HALF_SYMBOLS; ++i) {
                int val = in.get();
                code_lengths[i2++] = (byte) ((val & 0xFF) & 0xF);
                code_lengths[i2++] = (byte) ((val & 0xFF) >>> 4);
            }
            if ((!decoder.setCodeLengths(code_lengths))) {
                System.err.print("Xpress Huffman Decompression Error: Invalid Data: Unable to resolve Huffman codes\n");
                return MSCOMP_DATA_ERROR;
            }
            ByteBuffer _in = in.slice();
            status = chunk(_in, out, decoder);
            in.position(in.position() + _in.position());
            if ((status.getStatus() < MSCOMP_OK.getStatus())) {
                return status;
            }
        } while (status != MSCOMP_STREAM_END);
        return MSCOMP_OK;
    }
}
