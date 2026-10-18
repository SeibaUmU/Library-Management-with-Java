package com.library.common;

public final class TrangThai {

    private TrangThai() {
    }

    // CuonSach.TrangThai
    public static final String CUON_AVAILABLE = "AVAILABLE";
    public static final String CUON_BORROWED = "BORROWED";
    public static final String CUON_RESERVED = "RESERVED";
    public static final String CUON_LOST = "LOST";
    public static final String CUON_DAMAGED = "DAMAGED";

    // DatTruoc.TrangThai
    public static final String DATTRUOC_WAITING = "WAITING";
    public static final String DATTRUOC_RESERVED = "RESERVED";
    public static final String DATTRUOC_NOTIFIED = "NOTIFIED";
    public static final String DATTRUOC_COMPLETED = "COMPLETED";
    public static final String DATTRUOC_CANCELLED = "CANCELLED";

    // PhieuMuon.TrangThai
    public static final String PHIEUMUON_BORROWING = "BORROWING";
    public static final String PHIEUMUON_RETURNED = "RETURNED";

    // TheDocGia.TrangThai (lưu trong DB: ACTIVE, LOCKED)
    public static final String THE_ACTIVE = "ACTIVE";
    public static final String THE_LOCKED = "LOCKED";

    // Chỉ dùng trong response, không lưu DB
    public static final String THE_EXPIRED = "EXPIRED";
    public static final String THE_NONE = "NONE";
    // Loại vi phạm khi trả sách / lập phiếu phạt
    public static final String VI_PHAM_DAMAGED = "DAMAGED";
    public static final String VI_PHAM_LOST = "LOST";
    public static final String LOAI_PHAT_LATE = "LATE";
}
