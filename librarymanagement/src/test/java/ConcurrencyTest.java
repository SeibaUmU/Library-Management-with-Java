import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.library.common.JsonUtil;

import jakarta.json.JsonObject;

public class ConcurrencyTest {

    // ===== Cấu hình (sửa nếu dữ liệu của bạn khác) =====
    static final String HOST = "localhost";
    static final int PORT = 8888;
    static final String PASS = "123456HashPass";

    static final String THU_THU_1 = "user6@thuvien.edu.vn";
    static final String THU_THU_2 = "user7@thuvien.edu.vn";
    static final String THU_THU_3 = "user8@thuvien.edu.vn";

    static final int MA_DS = 2; // đầu sách có 2 cuốn đang AVAILABLE
    static final String CUON_1 = "BS-2-01";
    static final String CUON_2 = "BS-2-02";
    static final int THE_A = 85; // 2 thẻ độc giả đủ điều kiện mượn
    static final int THE_B = 2;
    static final int SO_VONG = 10;

    // Một client = một socket + một phiên đăng nhập riêng
    static class Client implements AutoCloseable {
        final Socket socket;
        final BufferedReader in;
        final PrintWriter out;

        Client(String email) throws IOException {
            socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true);

            if (email != null) {
                JsonObject r = send("{\"action\":\"LOGIN\",\"data\":{\"username\":\""
                        + email + "\",\"password\":\"" + PASS + "\"}}");
                if (!ok(r)) {
                    throw new IOException("Đăng nhập thất bại: " + email);
                }
            }
        }

        JsonObject send(String json) throws IOException {
            out.println(json);
            String line = in.readLine();
            if (line == null) {
                throw new IOException("Server đóng kết nối");
            }
            return JsonUtil.parseJson(line);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    static boolean ok(JsonObject r) {
        return "SUCCESS".equals(r.getString("status"));
    }

    static String muon(int maThe, String cuon) {
        return "{\"action\":\"CREATE_BORROW_TICKET\",\"data\":{\"maThe\":" + maThe
                + ",\"danhSachMaCuonSach\":[\"" + cuon + "\"]}}";
    }

    static String tra(int maCTPM) {
        return "{\"action\":\"PROCESS_RETURN_BOOK\",\"data\":{\"danhSachTra\":[{\"maCTPM\":"
                + maCTPM + "}]}}";
    }

    static int maCTPM(JsonObject r) {
        return r.getJsonObject("data").getJsonArray("chiTiet")
                .getJsonObject(0).getInt("maCTPM");
    }

    static int khaDung() throws IOException {
        try (Client c = new Client(null)) { // GET_BOOK_DETAIL là action công khai
            JsonObject r = c.send(
                    "{\"action\":\"GET_BOOK_DETAIL\",\"data\":{\"maDauSach\":" + MA_DS + "}}");
            return r.getJsonObject("data").getInt("soLuongKhaDung");
        }
    }

    static boolean theDuDieuKien(int maThe) throws IOException {
        try (Client c = new Client(THU_THU_1)) {
            JsonObject r = c.send(
                    "{\"action\":\"CHECK_CARD_ELIGIBILITY\",\"data\":{\"maThe\":" + maThe + "}}");
            boolean dat = ok(r) && r.getJsonObject("data").getBoolean("eligible");
            System.out.println("  Thẻ " + maThe + ": "
                    + (dat ? "đủ điều kiện" : "KHÔNG đủ điều kiện - " + r.getString("message")));
            return dat;
        }
    }

    // Trả mọi phiếu mượn vừa lập thành công để dữ liệu về như cũ
    static void traHet(Client c, JsonObject... ketQua) throws IOException {
        for (JsonObject r : ketQua) {
            if (ok(r)) {
                JsonObject t = c.send(tra(maCTPM(r)));
                if (!ok(t)) {
                    System.out.println("    Không trả được: " + t.getString("message"));
                }
            }
        }
    }

    // ---------- Test A: 2 client cùng mượn 1 cuốn ----------
    static boolean testCungMotCuon() throws Exception {
        System.out.println("=== Test A: 2 client cùng mượn cuốn " + CUON_1 + " ===");
        int goc = khaDung();
        int dung = 0;

        for (int i = 1; i <= SO_VONG; i++) {
            try (Client c1 = new Client(THU_THU_1);
                    Client c2 = new Client(THU_THU_2);
                    Client c3 = new Client(THU_THU_3)) {

                CyclicBarrier hang = new CyclicBarrier(2);
                ExecutorService pool = Executors.newFixedThreadPool(2);

                Future<JsonObject> f1 = pool.submit(() -> {
                    hang.await();
                    return c1.send(muon(THE_A, CUON_1));
                });
                Future<JsonObject> f2 = pool.submit(() -> {
                    hang.await();
                    return c2.send(muon(THE_B, CUON_1));
                });

                JsonObject r1 = f1.get();
                JsonObject r2 = f2.get();
                pool.shutdown();

                int soOk = (ok(r1) ? 1 : 0) + (ok(r2) ? 1 : 0);
                boolean vongDung = soOk == 1;
                JsonObject thua = ok(r1) ? r2 : r1;

                System.out.println("  Vòng " + i + ": " + soOk + " thành công"
                        + (vongDung ? "" : "  <-- SAI")
                        + " | " + thua.getString("message"));

                if (vongDung) {
                    dung++;
                }

                traHet(c3, r1, r2);
            }
        }

        int sau = khaDung();
        boolean boDemDung = sau == goc;

        System.out.println("  Kết quả: " + dung + "/" + SO_VONG + " vòng đúng; "
                + "SoLuongKhaDung " + goc + " -> " + sau
                + (boDemDung ? " (khớp)" : "  <-- LỆCH"));

        return dung == SO_VONG && boDemDung;
    }

    // ---------- Test B: 2 client mượn 2 cuốn khác nhau của cùng đầu sách
    // ----------
    static boolean testKhacCuon() throws Exception {
        System.out.println("=== Test B: 2 client mượn 2 cuốn khác nhau của đầu sách " + MA_DS + " ===");
        int goc = khaDung();
        int dung = 0;

        for (int i = 1; i <= SO_VONG; i++) {
            try (Client c1 = new Client(THU_THU_1);
                    Client c2 = new Client(THU_THU_2);
                    Client c3 = new Client(THU_THU_3)) {

                CyclicBarrier hang = new CyclicBarrier(2);
                ExecutorService pool = Executors.newFixedThreadPool(2);

                Future<JsonObject> f1 = pool.submit(() -> {
                    hang.await();
                    return c1.send(muon(THE_A, CUON_1));
                });
                Future<JsonObject> f2 = pool.submit(() -> {
                    hang.await();
                    return c2.send(muon(THE_B, CUON_2));
                });

                JsonObject r1 = f1.get();
                JsonObject r2 = f2.get();
                pool.shutdown();

                int giua = khaDung();
                boolean vongDung = ok(r1) && ok(r2) && giua == goc - 2;

                System.out.println("  Vòng " + i + ": "
                        + (ok(r1) ? "OK" : r1.getString("message")) + " / "
                        + (ok(r2) ? "OK" : r2.getString("message"))
                        + " | SoLuongKhaDung giữa vòng = " + giua
                        + (vongDung ? "" : "  <-- SAI (mong đợi " + (goc - 2) + ")"));

                if (vongDung) {
                    dung++;
                }

                traHet(c3, r1, r2);
            }
        }

        int sau = khaDung();
        boolean boDemDung = sau == goc;

        System.out.println("  Kết quả: " + dung + "/" + SO_VONG + " vòng đúng; "
                + "SoLuongKhaDung " + goc + " -> " + sau
                + (boDemDung ? " (khớp)" : "  <-- LỆCH"));

        return dung == SO_VONG && boDemDung;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Kiểm tra điều kiện trước khi chạy:");

        if (khaDung() < 2) {
            System.out.println("  Đầu sách " + MA_DS
                    + " cần ít nhất 2 cuốn AVAILABLE. Dừng.");
            return;
        }

        if (!theDuDieuKien(THE_A) || !theDuDieuKien(THE_B)) {
            System.out.println("  Hãy chọn thẻ khác (sửa THE_A / THE_B). Dừng.");
            return;
        }

        boolean a = testCungMotCuon();
        boolean b = testKhacCuon();

        System.out.println();
        System.out.println(a && b ? "✅ TẤT CẢ ĐẠT" : "❌ CÓ TEST KHÔNG ĐẠT");
    }
}
