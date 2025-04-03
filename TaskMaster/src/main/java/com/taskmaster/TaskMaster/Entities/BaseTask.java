package com.taskmaster.TaskMaster.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseTask {

    @Column(nullable = false, length = 255)
    protected String title;

    @Lob // Dùng cho các trường text dài
    @Column(columnDefinition = "TEXT")
    protected String description;

    @Enumerated(EnumType.STRING) // Lưu trữ tên enum trong DB (PENDING, DONE, ...)
    @Column(nullable = false, length = 50)
    protected TaskStatus status = TaskStatus.PENDING; // Giá trị mặc định

    @Column(name = "start_date")
    protected LocalDateTime startDate;

    @Column(name = "end_date")
    protected LocalDateTime endDate;

    // Sử dụng @CreationTimestamp/@UpdateTimestamp của Hibernate hoặc cơ chế tương tự của JPA
    // Hoặc dùng @PrePersist/@PreUpdate như dưới đây nếu không muốn phụ thuộc Hibernate specifics

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA Lifecycle Callbacks để tự động cập nhật thời gian
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor với các trường cơ bản (có thể thêm nếu cần)
    protected BaseTask(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = TaskStatus.PENDING; // Mặc định
    }
}
