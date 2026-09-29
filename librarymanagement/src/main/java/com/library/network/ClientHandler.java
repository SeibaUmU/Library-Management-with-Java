package com.library.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestRouter router;


    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.router = new RequestRouter();
    }


    @Override
    public void run() {
        String clientAddress =
                socket.getRemoteSocketAddress().toString();

        System.out.println(
                "🟢 Client kết nối thành công: "
                        + clientAddress
        );

        try (
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

            String inputLine;

            // Đọc liên tục từng dòng chuỗi JSON gửi từ Client
            while ((inputLine = in.readLine()) != null) {

                System.out.println(
                        "📥 [Request từ "
                                + clientAddress
                                + "]: "
                                + inputLine
                );

                // Điều hướng xử lý request và lấy chuỗi JSON response
                String responseJson =
                        router.route(inputLine);

                // Trả kết quả về cho Client
                out.println(responseJson);
                out.flush();

                System.out.println(
                        "📤 [Response tới "
                                + clientAddress
                                + "]: "
                                + responseJson
                );
            }

        } catch (SocketException e) {

            System.out.println(
                    "🔴 Client ngắt kết nối đột ngột: "
                            + clientAddress
            );

        } catch (Exception e) {

            System.err.println(
                    "❌ Lỗi xử lý Client "
                            + clientAddress
                            + ": "
                            + e.getMessage()
            );

        } finally {

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(
                    "🔚 Đã giải phóng tài nguyên cho Client: "
                            + clientAddress
            );
        }
    }
}
