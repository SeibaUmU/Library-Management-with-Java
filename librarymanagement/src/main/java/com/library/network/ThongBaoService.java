package com.library.network;

import java.util.List;

import com.library.common.EmailService;
import com.library.entities.DatTruoc;
import com.library.entities.NguoiDung;
import com.library.repository.CauHinhRepository;
import com.library.repository.DatTruocRepository;

public class ThongBaoService {

    private final DatTruocRepository datTruocRepo = new DatTruocRepository();
    private final CauHinhRepository cauHinhRepo = new CauHinhRepository();

    public static String noiDungSachVe(String tenSach, int soNgayGiu) {
        return "Sách [" + tenSach + "] bạn đặt trước đã có tại quầy. "
                + "Vui lòng đến nhận trong vòng " + soNgayGiu + " ngày";
    }

    // UC-10: gửi thông báo cho độc giả. Lỗi chỉ ghi log, không làm hỏng
    // giao dịch đang gọi (thông báo trong app vẫn lấy được từ GET_MY_RESERVATIONS).
    public void guiSachDatTruocDaVe(Integer maDatTruoc) {
        try {
            DatTruoc dt = datTruocRepo.findByIdFetch(maDatTruoc);

            if (dt == null) {
                return;
            }

            int soNgay = cauHinhRepo.getCauHinh().getSoNgayGiuSachDatTruoc();
            NguoiDung nd = dt.getTheDocGia().getNguoiDung();

            EmailService.gui(
                    nd != null ? nd.getEmail() : null,
                    "Sách đặt trước đã sẵn sàng",
                    noiDungSachVe(dt.getDauSach().getTenSach(), soNgay));

        } catch (Exception e) {
            System.err.println(
                    "⚠️ [Thông báo] Lỗi gửi thông báo cho lượt đặt "
                            + maDatTruoc + ": " + e.getMessage());
        }
    }

    // Hủy các lượt NOTIFIED quá hạn giữ sách, chuyển cuốn cho người chờ kế tiếp
    public void xuLyHetHanGiuSach() {
        int soNgay = cauHinhRepo.getCauHinh().getSoNgayGiuSachDatTruoc();

        List<DatTruoc> quaHan = datTruocRepo.getNotifiedQuaHan(soNgay);

        int dem = 0;

        for (DatTruoc dt : quaHan) {
            try {
                Integer keTiep = datTruocRepo.huyLuotDat(dt.getMaDatTruoc(), null);

                dem++;

                if (keTiep != null) {
                    guiSachDatTruocDaVe(keTiep);
                }

            } catch (Exception e) {
                System.err.println(
                        "⚠️ [Job] Không hủy được lượt đặt "
                                + dt.getMaDatTruoc() + ": " + e.getMessage());
            }
        }

        if (dem > 0) {
            System.out.println(
                    "🔔 [Job] Đã hủy " + dem
                            + " lượt đặt trước quá hạn giữ sách");
        }
    }
}