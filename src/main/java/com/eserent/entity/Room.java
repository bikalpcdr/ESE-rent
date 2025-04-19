package com.eserent.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Room entity class.
 * Represents rooms that can be rented in the system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @Column(length = 1000)
    private String description;


    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal pricePerNight;

    private String address;

    private double size; // No of rooms

    private int capacity; // number of people

    private boolean available = true;

    @Column(length = 2000)
    private String amenities; // comma-separated list of amenities

    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> images = new ArrayList<>();

    public void addImage(RoomImage image) {
        image.setRoom(this);
        images.add(image);
    }

    public enum RoomType {
        APARTMENT, PRIVATE_ROOM, SHARED_ROOM, HOUSE, HOTEL, STUDIO
    }

    public Room(List<String> imageUrls, User landlord,List<Booking> bookings) {
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
        this.landlord = landlord;
        if (bookings != null) {
            this.bookings = bookings;
        }
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", address='" + address + '\'' +
                ", roomType=" + roomType +
                '}';
    }
}
