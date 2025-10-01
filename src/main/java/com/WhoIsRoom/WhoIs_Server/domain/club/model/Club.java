package com.WhoIsRoom.WhoIs_Server.domain.club.model;

import com.WhoIsRoom.WhoIs_Server.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clubs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter @Getter
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id", nullable = false)
    private Long id;

    @Column(name = "name", length = 200, nullable = false, unique = true)
    private String name;

    @Column(name = "club_number", length = 100, nullable = false, unique = true)
    private String clubNumber;

    @Builder
    public Club(String name, String clubNumber) {
        this.name = name;
        this.clubNumber = clubNumber;
    }

}
