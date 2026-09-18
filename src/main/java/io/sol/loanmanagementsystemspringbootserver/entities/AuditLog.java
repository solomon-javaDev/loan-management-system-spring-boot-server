package io.sol.loanmanagementsystemspringbootserver.entities;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 2000)
    private String details;

    public AuditLog(User user, String action, String entityType, String entityId, String details) {
        this.user = user;
        this.username = user == null ? "SYSTEM" : user.getUsername();
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.occurredAt = LocalDateTime.now();
        this.details = details;
    }
}
