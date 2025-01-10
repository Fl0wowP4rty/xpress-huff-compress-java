package tech.skidonion.compress.xpresshuff;

import java.nio.ByteBuffer;

import static tech.skidonion.compress.xpresshuff.Utils.*;

public class InputBitStream {

    private byte[] in;
    private int position;
    private int mask;
    private int bits;
    private final int offset;

    public InputBitStream(ByteBuffer in) {
        this.in = in.array();
        this.offset = in.arrayOffset();
        this.position = 4;
        this.mask = getUInt16(this.in, this.offset) << 16 | getUInt16(this.in, this.offset + 2);
        this.bits = 32;
    }

    public int position() {
        return this.position;
    }

    public int availableBits() {
        return this.bits;
    }

    public int remainingRawBytes() {
        return this.in.length - this.position - this.offset;
    }

    public int peek(int n) {
        return (this.mask >>> 16) >>> (16 - n);
    }

    public boolean maskIsZero() {
        return this.bits == 0 || this.mask >>> 32 - this.bits == 0;
    }

    public void skip(int n) {
        this.mask <<= n;
        this.bits -= n;
        if (this.bits < 16) {
            this.mask |= getUInt16(this.in, this.offset + this.position) << (16 - this.bits);
            this.bits |= 0x10;
            this.position += 2;
        }
    }

    public int readBits(int n) {
        int x = this.peek(n);
        this.skip(n);
        return x;
    }

    public byte readRawByte() {
        return this.in[this.offset + this.position++];
    }

    public short readRawInt16() {
        short x = getInt16(this.in, this.offset + this.position);
        this.position += 2;
        return x;
    }

    public int readRawInt32() {
        int x = getInt32(this.in, this.offset + this.position);
        this.position += 4;
        return x;
    }
}
