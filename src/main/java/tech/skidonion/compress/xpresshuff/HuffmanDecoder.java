package tech.skidonion.compress.xpresshuff;

import java.util.Arrays;

import static tech.skidonion.compress.xpresshuff.XpressHuffDecompress.*;

public class HuffmanDecoder {
    public static final short INVALID_SYMBOL = (short) 0xFFFF;
    public static final int NUM_BITS_MAX = HUFF_BITS_MAX;
    public static final int NUM_SYMBOLS = SYMBOLS;

    public static final int NUM_TABLE_BITS = (NUM_BITS_MAX + 1) / 2 + 1;


    private final short[] lims = new short[NUM_BITS_MAX + 1];
    private final short[] poss = new short[NUM_BITS_MAX + 1];
    private final short[] syms = new short[NUM_SYMBOLS];
    private final byte[] lens = new byte[1 << NUM_TABLE_BITS];


    public boolean setCodeLengths(byte[] codeLengths) {
        Arrays.fill(this.syms, INVALID_SYMBOL);

        // Get all length counts
        short[] cnts = new short[NUM_BITS_MAX + 1];

        for (int s = 0; s < NUM_SYMBOLS; ++s) {
            int len = Byte.toUnsignedInt(codeLengths[s]);
            cnts[len] = (short) ((cnts[len] & 0xFFFF) + 1);
        }
        cnts[0] = 0;

        // Get limits and lengths
        int maxValue = 1 << NUM_BITS_MAX;
        int last = 0, index = 0;
        this.lims[0] = 0;
        this.lims[NUM_BITS_MAX] = (short) (maxValue & 0xFFFF);
        for (int len = 1; len <= NUM_TABLE_BITS; ++len) {
            int inc = ((cnts[len] & 0xFFFF) << (NUM_BITS_MAX - len) & 0xFFFF);
            if (last + inc > maxValue) {
                return false;
            }
            this.lims[len] = (short) (last = (last + inc) & 0xFFFF);
            int limit = last >>> (NUM_BITS_MAX - NUM_TABLE_BITS);
            Arrays.fill(this.lens, index, limit, (byte) len);
            index = limit;
        }
        for (int len = NUM_TABLE_BITS + 1; len < NUM_BITS_MAX; ++len) {
            int inc = ((cnts[len] & 0xFFFF) << (NUM_BITS_MAX - len) & 0xFFFF);
            if (last + inc > maxValue) {
                return false;
            }
            this.lims[len] = (short) ((last += inc) & 0xFFFF);
        }
        if ((last + (cnts[NUM_BITS_MAX] & 0xFFFF) > maxValue)) {
            return false;
        }

        // Get positions
        this.poss[0] = 0;
        for (int len = 1; len <= NUM_BITS_MAX; ++len) {
            this.poss[len] = (short) ((this.poss[len - 1] & 0xFFFF) + (cnts[len - 1] & 0xFFFF));
        }

        // Get symbols
        System.arraycopy(this.poss, 0, cnts, 0, this.poss.length);

        for (int s = 0; s < NUM_SYMBOLS; ++s) {
            int len = codeLengths[s] & 0xFF;
            if (len > 0) {
                this.syms[cnts[len]] = (short) s;
                cnts[len] = (short) ((cnts[len] & 0xFFFF) + 1);
            }
        }

        return true;
    }


    public short decodeSymbol(InputBitStream bits) {
        int n, r = bits.availableBits();
        long x = (r < NUM_BITS_MAX) ? (((long) bits.peek(r) << (NUM_BITS_MAX - r)) & 0xFFFFFFFFL) : bits.peek(NUM_BITS_MAX);
        if (x < (this.lims[NUM_TABLE_BITS] & 0xFFFF)) {
            n = this.lens[(int) (x >>> (NUM_BITS_MAX - NUM_TABLE_BITS))] & 0xFF;
        } else {
            for (n = NUM_TABLE_BITS + 1; x >= (this.lims[n] & 0xFFFF); ++n) ;
        }
        if (n > r) {
            return INVALID_SYMBOL;
        }
        bits.skip(n);
        long s = (this.poss[n] & 0xFFFF) + ((x - (this.lims[n - 1] & 0xFFFF)) >>> (NUM_BITS_MAX - n));
        return (s >= NUM_SYMBOLS) ? INVALID_SYMBOL : this.syms[(int) s];
    }
}
