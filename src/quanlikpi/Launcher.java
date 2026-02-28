package quanlikpi;

/**
 * Lớp khởi chạy trung gian để tránh lỗi JavaFX runtime components are missing
 * khi không sử dụng module-info.java.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
