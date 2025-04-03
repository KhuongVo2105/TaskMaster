package com.taskmaster.TaskMaster.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tasks") // Tên bảng số nhiều, underscore
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task extends BaseTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID số, tự động tăng
    Long id;

    // Mối quan hệ Many-to-One với User
    @ManyToOne(fetch = FetchType.LAZY) // LAZY để tránh tải User khi không cần
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại tới bảng users
    User user;

    // Mối quan hệ One-to-Many với Subtask
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC") // Sắp xếp subtask theo thời gian tạo
    Set<SubTask> subtasks = new HashSet<>();

    // Constructor (sử dụng super và thêm các field của Task nếu có)
    @Builder // Cho phép tạo Task bằng Builder Pattern
    public Task(String title, String description, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, User user, Set<SubTask> subtasks) {
        super(title, description, startDate, endDate);
        this.user = user;
        if (subtasks != null) {
            this.subtasks = subtasks;
            // Đảm bảo subtask cũng tham chiếu ngược lại task này
            subtasks.forEach(subtask -> subtask.setParentTask(this));
        } else {
            this.subtasks = new HashSet<>();
        }
    }


    // Phương thức tính toán phần trăm hoàn thành (không lưu vào DB)
    // Đây là một ví dụ về việc thêm logic nghiệp vụ vào Entity (mặc dù đôi khi nên để ở Service)
    @Transient // Đánh dấu không mapping trường này vào DB
    public double getCompletionPercentage() {
        if (subtasks == null || subtasks.isEmpty()) {
            // Nếu không có subtask, hoàn thành 100% nếu status là DONE, ngược lại là 0%
            return this.getStatus() == TaskStatus.DONE ? 1.0 : 0.0;
        } else {
            // Nếu có subtask, tính dựa trên số subtask DONE
            long totalSubtasks = subtasks.size();
            if (totalSubtasks == 0) {
                return 0.0; // Trường hợp đặc biệt (mặc dù kiểm tra isEmpty ở trên)
            }
            long doneSubtasks = subtasks.stream()
                    .filter(subtask -> subtask.getStatus() == TaskStatus.DONE)
                    .count();
            return (double) doneSubtasks / totalSubtasks;
        }
    }

    // Tiện ích để thêm Subtask (quản lý 2 chiều)
    public void addSubtask(SubTask subtask) {
        if (this.subtasks == null) {
            this.subtasks = new HashSet<>();
        }
        // Kiểm tra ràng buộc thời gian trước khi thêm (nên thực hiện ở Service Layer)
        // if (subtask.getStartDate() != null && this.getStartDate() != null && subtask.getStartDate().isBefore(this.getStartDate())) {
        //     throw new IllegalArgumentException("Subtask start date cannot be before parent task start date.");
        // }
        // if (subtask.getEndDate() != null && this.getEndDate() != null && subtask.getEndDate().isAfter(this.getEndDate())) {
        //     throw new IllegalArgumentException("Subtask end date cannot be after parent task end date.");
        // }
        subtasks.add(subtask);
        subtask.setParentTask(this);
    }

    // Tiện ích để xóa Subtask (quản lý 2 chiều)
    public void removeSubtask(SubTask subtask) {
        if (this.subtasks != null) {
            subtasks.remove(subtask);
            subtask.setParentTask(null);
        }
    }


    // Equals & HashCode chỉ dựa trên ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        if (!super.equals(o)) return false; // Optional: consider if BaseTask fields affect equality
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        // Sử dụng getClass().hashCode() để nhất quán với equals khi ID null (trước khi persist)
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + getTitle() + '\'' +
                ", status=" + getStatus() +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                // Không nên include user và subtasks trong toString mặc định để tránh vòng lặp
                '}';
    }
}