🛠 Công nghệ sử dụng
Backend: Java 17+, Spring Boot

Cơ sở dữ liệu: MySQL / PostgreSQL

Build Tool: Maven

Tài liệu API: Swagger UI / OpenAPI 3

📋 Yêu cầu hệ thống
Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt:

Java Development Kit (JDK) 17 trở lên.

Apache Maven 3.6+.

MySQL Server (hoặc hệ quản trị cơ sở dữ liệu tương ứng).

🚀 Cài đặt và Khởi chạy
1. Clone Project
Bash
git clone https://github.com/kienphatanh-beep/JavaWeb.git
cd JavaWeb
2. Cấu hình Database
Mở file src/main/resources/application.properties và cập nhật thông tin kết nối database của bạn:

Properties
spring.datasource.url=jdbc:mysql://localhost:3306/ten_database_cua_ban
spring.datasource.username=root
spring.datasource.password=mat_khau_cua_ban
spring.jpa.hibernate.ddl-auto=update
3. Build và Chạy ứng dụng
Sử dụng Maven để cài đặt các phụ thuộc và chạy project:

Bash
mvn clean install
mvn spring-boot:run
Ứng dụng sẽ mặc định chạy tại: http://localhost:8080

📚 API Documentation
Dự án tích hợp Swagger để tự động tạo tài liệu hướng dẫn sử dụng API. Sau khi ứng dụng đã chạy, bạn có thể truy cập vào:

Swagger UI: http://localhost:8080/swagger-ui/index.html

Một số Endpoint chính:

GET /api/v1/products: Lấy danh sách sản phẩm.

POST /api/v1/users/register: Đăng ký tài khoản mới.

💾 Hướng dẫn Seed Data (Dữ liệu mẫu)
Để thử nghiệm các tính năng mà không cần nhập liệu thủ công:

Dùng SQL: Bạn có thể tìm thấy file seed_data.sql (nếu có) trong thư mục src/main/resources. Hãy import file này vào database của bạn.

Tự động: Project có cấu hình lớp DataSeeder (sử dụng CommandLineRunner). Khi bạn khởi chạy lần đầu, hệ thống sẽ tự động tạo một số tài khoản admin và sản phẩm mẫu nếu database đang trống.

🌿 Quy trình sử dụng Git
Để làm việc nhóm hiệu quả và quản lý code an toàn, hãy tuân thủ quy trình sau:

1. Tạo nhánh mới (Branching)
Không làm việc trực tiếp trên nhánh main. Tạo nhánh mới cho mỗi tính năng hoặc sửa lỗi:

Bash
git checkout -b feature/ten-tinh-nang
2. Commit Code
Lưu lại các thay đổi với thông điệp rõ ràng:

Bash
git add .
git commit -m "feat: thêm chức năng tìm kiếm sản phẩm"
3. Đẩy code và Pull Request (PR)
Đẩy nhánh của bạn lên GitHub:

Bash
git push origin feature/ten-tinh-nang
Truy cập GitHub, chọn nhánh vừa push và nhấn "New Pull Request".

Mô tả các thay đổi và nhờ thành viên khác review trước khi Merge vào nhánh main.
