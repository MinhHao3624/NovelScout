# NovelScout

Website đọc truyện chữ tích hợp thu thập dữ liệu tự động bằng Jsoup và mô-đun gợi ý lai.

## Công nghệ

- Backend: Java 21, Spring Boot 4, Spring MVC, Spring Data JPA, Spring Security, Flyway.
- Frontend: React 19, React Router, Vite.
- Database: MySQL 8.4.

## Chạy môi trường phát triển

Yêu cầu: Java 21+, Docker Desktop và Node.js 20+.

### 1. Khởi động MySQL

```powershell
docker compose up -d mysql
```

Database local dùng các giá trị mặc định trong `compose.yml`. Nếu dùng MySQL đã cài sẵn, sao chép `backend/.env.example` thành `backend/.env` và cập nhật thông tin kết nối. File `backend/.env` được Git bỏ qua.

### 2. Khởi động backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Flyway tự tạo và cập nhật schema khi backend khởi động.

### 3. Khởi động frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend: http://localhost:5173  
Backend health check: http://localhost:8080/actuator/health

## Kiểm tra source

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run lint
npm run build
```

## Quy tắc database

- Thay đổi schema bằng migration trong `backend/src/main/resources/db/migration`.
- Không sửa migration đã chạy; tạo migration phiên bản mới.
- Hibernate chỉ kiểm tra schema qua `ddl-auto=validate`, không tự tạo hoặc sửa bảng.
