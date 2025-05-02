package com.example.register.register.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "saved_data")
public class SavedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "process_id", nullable = false)
    private BusinessProcess process;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @CreationTimestamp
    @Temporal(TemporalType.DATE)
    @Column(name = "todays_date", updatable = false)
    private LocalDate todaysDate;

    @Transient
    @JsonProperty("process_id")
    private Long processId;

    public Long getProcessId() {
        return process != null ? process.getId() : processId;
    }

    @Transient
    @JsonProperty("user_id")
    private Long userId;

    public Long getUserId() {
        return user != null ? user.getId() : userId;
    }

}

