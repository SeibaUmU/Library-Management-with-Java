package com.library.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.library.dao.EntityManagerUtil;

public class LibraryServer {

    private static final int PORT = 8888;
    private static final int THREAD_POOL_SIZE = 20;

    // Phục vụ tối đa 20 Client đồng thời
    public static void main(String[] args) {

        System.out.println("==================================================");
        System.out.println("🚀 ĐANG KHỞI ĐỘNG LIBRARY SOCKET SERVER...");
        System.out.println("==================================================");


        // 1. Khởi tạo sẵn EntityManagerFactory JPA
        try {
            EntityManagerUtil.getEntityManagerFactory();

            System.out.println(
                    "✅ JPA EntityManagerFactory kết nối SQL Server thành công!"
            );

        } catch (Exception e) {

            System.err.println(
                    "❌ Lỗi kết nối CSDL SQL Server: "
                            + e.getMessage()
            );

            return;
        }


        // 2. Tạo Thread Pool quản lý luồng Client
        ExecutorService threadPool =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);


        // 3. Mở ServerSocket lắng nghe kết nối
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println(
                    "📡 Server đang lắng nghe tại Cổng (Port): "
                            + PORT
            );

            System.out.println(
                    "⌛ Chờ Client kết nối...\n"
            );


            while (true) {

                Socket clientSocket =
                        serverSocket.accept();

                // Giao Client mới cho một Thread trong Pool xử lý
                threadPool.submit(
                        new ClientHandler(clientSocket)
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "❌ Lỗi ServerSocket: "
                            + e.getMessage()
            );

        } finally {

            threadPool.shutdown();

            EntityManagerUtil.close();

            System.out.println(
                    "🛑 Server đã dừng hoàn toàn."
            );
        }
    }
}
