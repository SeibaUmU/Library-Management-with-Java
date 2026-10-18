import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestClient {

    public static void main(String[] args) throws IOException {

        String[] requests = {
                "{\"action\":\"LOGIN\",\"data\":{\"username\":\"user1@thuvien.edu.vn\",\"password\":\"123456HashPass\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"limit\":5}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"TOP_BOOKS\",\"tuNgay\":\"2026-10-01\",\"denNgay\":\"2026-10-31\",\"limit\":3}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"BORROW_STATS\",\"groupBy\":\"MONTH\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"BORROW_STATS\",\"groupBy\":\"QUARTER\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"BORROW_STATS\",\"groupBy\":\"YEAR\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"BORROW_STATS\",\"groupBy\":\"WEEK\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"OVERDUE_READERS\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"FINE_REVENUE\",\"groupBy\":\"MONTH\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"FINE_REVENUE\",\"tuNgay\":\"2000-01-01\",\"denNgay\":\"2000-12-31\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"tuNgay\":\"2026-12-31\",\"denNgay\":\"2026-01-01\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{\"loaiBaoCao\":\"XYZ\"}}",
                "{\"action\":\"LOGIN\",\"data\":{\"username\":\"user16@thuvien.edu.vn\",\"password\":\"123456HashPass\"}}",
                "{\"action\":\"GET_REPORTS\",\"data\":{}}"
        };
        try (
                Socket s = new Socket("localhost", 8888);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {
            for (String r : requests) {
                out.println(r);
                System.out.println(">> " + r);
                System.out.println("<< " + in.readLine());
                System.out.println();
            }
        }
    }
}
