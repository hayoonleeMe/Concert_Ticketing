package com.concertticket.venue;

import com.concertticket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private Integer totalCapacity;

    public static Venue create(String name, String address, Integer totalCapacity) {
        Venue venue = new Venue();
        venue.name = name;
        venue.address = address;
        venue.totalCapacity = totalCapacity;
        return venue;
    }
}