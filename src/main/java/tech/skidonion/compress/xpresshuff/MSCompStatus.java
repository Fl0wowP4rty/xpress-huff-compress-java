package tech.skidonion.compress.xpresshuff;

public enum MSCompStatus {
    MSCOMP_OK(0),
    MSCOMP_STREAM_END(1),
    MSCOMP_POSSIBLE_STREAM_END(2),

    MSCOMP_ERRNO(-1),
    MSCOMP_ARG_ERROR(-2),
    MSCOMP_DATA_ERROR(-3),
    MSCOMP_MEM_ERROR(-4),
    MSCOMP_BUF_ERROR(-5);

    private final int status;

    MSCompStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}
