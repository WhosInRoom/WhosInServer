package com.WhoIsRoom.WhoIs_Server.domain.member.model;

import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import com.WhoIsRoom.WhoIs_Server.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter @Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "is_exist")
    private Boolean isExist;

    @Builder
    public Member(User user, Club club, Boolean isExist) {
        this.user = user;
        this.club = club;
        this.isExist = isExist;
    }

    public void setExist(){
        this.isExist = true;
    }
}
