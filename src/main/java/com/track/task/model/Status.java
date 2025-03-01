package com.track.task.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "status", schema = "private", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "user_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal = true;

    @JsonIgnore
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
    private User user;


}
