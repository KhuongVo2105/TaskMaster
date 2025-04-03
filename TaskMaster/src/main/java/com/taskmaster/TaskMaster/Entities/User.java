package com.taskmaster.TaskMaster.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode; // Cần thiết cho kiểu JSON
import org.hibernate.type.SqlTypes;          // Cần thiết cho kiểu JSON

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Builder Pattern để tạo đối tượng User dễ dàng hơn
@Entity
@Table(name = "users") // Tên bảng số nhiều, underscore
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Tự động tạo UUID
    @Column(updatable = false, nullable = false)
    UUID id;

    // Lưu trữ thông tin JSON từ Google
    // Sử dụng @Lob hoặc kiểu dữ liệu JSON của CSDL nếu hỗ trợ (ví dụ PostgreSQL)
    // @JdbcTypeCode(SqlTypes.JSON) yêu cầu hibernate-community-dialects hoặc cấu hình tương tự
    @Lob
    @Column(name = "google_user_info_json", columnDefinition = "TEXT")
    String googleUserInfoJson;

    // Thông tin cơ bản khác có thể lấy từ JSON hoặc yêu cầu thêm
    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "picture_url")
    String pictureUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    // Mối quan hệ One-to-Many với Task
    // mappedBy trỏ đến tên thuộc tính 'user' trong class Task
    // cascade = CascadeType.ALL: Các thay đổi (persist, remove, merge, refresh) trên User sẽ áp dụng cho Task liên quan.
    // orphanRemoval = true: Nếu một Task bị xóa khỏi collection 'tasks' của User, nó cũng sẽ bị xóa khỏi DB.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Khởi tạo giá trị mặc định cho Builder
    private Set<Task> tasks = new HashSet<>();

    // Callback để tự set thời gian tạo
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Có thể trích xuất email, fullName, pictureUrl từ googleUserInfoJson ở đây nếu muốn
    }

    // Tiện ích để thêm Task vào User (quản lý 2 chiều)
    public void addTask(Task task) {
        tasks.add(task);
        task.setUser(this);
    }

    // Tiện ích để xóa Task khỏi User (quản lý 2 chiều)
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setUser(null);
    }

    // Equals & HashCode chỉ dựa trên ID để tránh vấn đề với collection trong JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // Hoặc return Objects.hash(id); nếu id không bao giờ null sau khi persist
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", createdAt=" + createdAt +
                // Không nên include googleUserInfoJson và tasks trong toString mặc định
                '}';
    }
}
