package com.taskmaster.TaskMaster.Entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subtasks") // Tên bảng số nhiều, underscore
public class SubTask extends BaseTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID số, tự động tăng
    private Long id;

    // Mối quan hệ Many-to-One với Task (parent task)
    @ManyToOne(fetch = FetchType.LAZY) // LAZY để tránh tải Task cha khi không cần
    @JoinColumn(name = "parent_task_id", nullable = false) // Khóa ngoại tới bảng tasks, không được null
    private Task parentTask;

    // Constructor (sử dụng super và thêm các field của Subtask nếu có)
    @Builder // Cho phép tạo Subtask bằng Builder Pattern
    public SubTask(String title, String description, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, Task parentTask) {
        super(title, description, startDate, endDate);
        // Ràng buộc thời gian nên được kiểm tra ở lớp Service khi tạo/cập nhật Subtask
        // Tuy nhiên, bạn cũng có thể đặt nó ở đây nếu muốn chặt chẽ ngay từ constructor,
        // nhưng cần xử lý trường hợp parentTask hoặc ngày tháng của nó là null.
        // Ví dụ kiểm tra cơ bản:
        // if (parentTask != null) {
        //     if (startDate != null && parentTask.getStartDate() != null && startDate.isBefore(parentTask.getStartDate())) {
        //         throw new IllegalArgumentException("Subtask start date cannot be before parent task start date.");
        //     }
        //     if (endDate != null && parentTask.getEndDate() != null && endDate.isAfter(parentTask.getEndDate())) {
        //         throw new IllegalArgumentException("Subtask end date cannot be after parent task end date.");
        //     }
        // }
        this.parentTask = parentTask;
    }

    // Ghi chú: Ràng buộc logic "Thời gian kết thúc task nhỏ không được xảy ra sau ngày kết thúc task lớn
    // và thời gian bắt đầu task nhỏ không được trước ngày bắt đầu task lớn"
    // KHÔNG thể thực thi hoàn toàn bằng annotation JPA chuẩn ở mức DB (trừ khi dùng trigger/check constraint phức tạp).
    // Cách tốt nhất là kiểm tra logic này trong Service Layer trước khi lưu (persist/merge) Subtask.
    // Bạn có thể thêm annotation Bean Validation (@AssertTrue) để kiểm tra ở mức ứng dụng nếu dùng Spring Boot.

    // Ví dụ về phương thức kiểm tra ràng buộc (sử dụng với @AssertTrue nếu có Bean Validation)
    // @AssertTrue(message = "Subtask start date must be on or after parent task start date")
    // public boolean isStartDateValid() {
    //     if (parentTask == null || parentTask.getStartDate() == null || startDate == null) {
    //         return true; // Bỏ qua nếu thiếu thông tin
    //     }
    //     return !startDate.isBefore(parentTask.getStartDate());
    // }

    // @AssertTrue(message = "Subtask end date must be on or before parent task end date")
    // public boolean isEndDateValid() {
    //     if (parentTask == null || parentTask.getEndDate() == null || endDate == null) {
    //         return true; // Bỏ qua nếu thiếu thông tin
    //     }
    //     return !endDate.isAfter(parentTask.getEndDate());
    // }


    // Equals & HashCode chỉ dựa trên ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubTask subtask)) return false;
        if (!super.equals(o)) return false; // Optional
        return id != null && id.equals(subtask.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + id +
                ", title='" + getTitle() + '\'' +
                ", status=" + getStatus() +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                // Không include parentTask trong toString mặc định
                '}';
    }
}