import com.library.common.ActionType;
import com.library.common.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestSocketClient {

    public static void main(String[] args) {

        String host = "localhost";
        int port = 8888;

        System.out.println(
                "🔄 Đang kết nối tới Library Server ("
                        + host
                        + ":"
                        + port
                        + ")..."
        );

        try (
                Socket socket = new Socket(host, port);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream(),
                                StandardCharsets.UTF_8
                        ),
                        true
                )
        ) {

            System.out.println(
                    "✅ Kết nối Socket thành công!\n"
            );


            // -------------------------------------------------------------
            // KỊCH BẢN 1: Lấy Cấu hình Hệ thống
            // -------------------------------------------------------------

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "1️⃣ TEST ACTION: GET_SYSTEM_CONFIG"
            );

            String req1 =
                    JsonUtil.buildRequest(
                            ActionType.GET_SYSTEM_CONFIG,
                            null
                    );

            out.println(req1);

            System.out.println(
                    "📤 Request : "
                            + req1
            );

            System.out.println(
                    "📥 Response: "
                            + in.readLine()
                            + "\n"
            );


            // -------------------------------------------------------------
            // KỊCH BẢN 2: Đăng nhập tài khoản Admin
            // -------------------------------------------------------------

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "2️⃣ TEST ACTION: LOGIN "
                            + "(Tài khoản admin1@thuvien.vn)"
            );

            JsonObject loginData =
                    Json.createObjectBuilder()
                            .add(
                                    "username",
                                    "admin1@thuvien.vn"
                            )
                            .add(
                                    "password",
                                    "Pass123"
                            )
                            .build();

            String req2 =
                    JsonUtil.buildRequest(
                            ActionType.LOGIN,
                            loginData
                    );

            out.println(req2);

            System.out.println(
                    "📤 Request : "
                            + req2
            );

            System.out.println(
                    "📥 Response: "
                            + in.readLine()
                            + "\n"
            );


            // -------------------------------------------------------------
            // KỊCH BẢN 3: Tìm kiếm sách theo từ khóa "Đầu Sách"
            // -------------------------------------------------------------

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "3️⃣ TEST ACTION: SEARCH_BOOKS "
                            + "(Từ khóa: 'Đầu Sách')"
            );

            JsonObject searchData =
                    Json.createObjectBuilder()
                            .add(
                                    "keyword",
                                    "Đầu Sách"
                            )
                            .add(
                                    "page",
                                    1
                            )
                            .add(
                                    "pageSize",
                                    5
                            )
                            .build();

            String req3 =
                    JsonUtil.buildRequest(
                            ActionType.SEARCH_BOOKS,
                            searchData
                    );

            out.println(req3);

            System.out.println(
                    "📤 Request : "
                            + req3
            );

            System.out.println(
                    "📥 Response: "
                            + in.readLine()
                            + "\n"
            );


            // -------------------------------------------------------------
            // KỊCH BẢN 4: Lấy chi tiết Đầu Sách Mã 1
            // (kèm danh sách Cuốn Sách)
            // -------------------------------------------------------------

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "4️⃣ TEST ACTION: GET_BOOK_DETAIL "
                            + "(maDauSach = 1)"
            );

            JsonObject detailData =
                    Json.createObjectBuilder()
                            .add(
                                    "maDauSach",
                                    1
                            )
                            .build();

            String req4 =
                    JsonUtil.buildRequest(
                            ActionType.GET_BOOK_DETAIL,
                            detailData
                    );

            out.println(req4);

            System.out.println(
                    "📤 Request : "
                            + req4
            );

            System.out.println(
                    "📥 Response: "
                            + in.readLine()
                            + "\n"
            );


            System.out.println(
                    "🎉 HOÀN THÀNH TẤT CẢ KỊCH BẢN KIỂM THỬ!"
            );

        } catch (Exception e) {

            System.err.println(
                    "❌ Lỗi kết nối Socket Client: "
                            + e.getMessage()
            );
        }
    }
}
