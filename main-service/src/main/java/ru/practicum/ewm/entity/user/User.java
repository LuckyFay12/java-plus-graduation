package ru.practicum.ewm.entity.user;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.entity.event.Event;
import ru.practicum.ewm.entity.request.ParticipationRequest;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParticipationRequest> requests = new ArrayList<>();
}