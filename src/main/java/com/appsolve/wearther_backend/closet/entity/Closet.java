package com.appsolve.wearther_backend.closet.entity;


import com.appsolve.wearther_backend.Entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder @Getter
@Table(name="closet")
public class Closet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="closet_id")
    private Long id;

    @OneToOne @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "closet", fetch = EAGER, cascade = CascadeType.ALL)
    private List<ClosetUpper> closetUppers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "closet", fetch = EAGER, cascade = CascadeType.ALL)
    private List<ClosetLower> closetLowers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "closet", fetch = EAGER, cascade = CascadeType.ALL)
    private List<ClosetOther> closetOthers = new ArrayList<>();
}