package com.example.didong2jv.entity; //

import java.util.ArrayList; //
import java.util.List; //

import jakarta.persistence.Entity; //
import jakarta.persistence.GeneratedValue; //
import jakarta.persistence.GenerationType; //
import jakarta.persistence.Id; //
import jakarta.persistence.ManyToMany; //
import jakarta.persistence.Table; //
import jakarta.validation.constraints.NotBlank; //
import jakarta.validation.constraints.Size; //
import lombok.AllArgsConstructor; //
import lombok.Data; //
import lombok.NoArgsConstructor; //

@Entity //
@Table(name = "addresses") //
@Data // Tự động tạo Getter/Setter để giải quyết lỗi "method is undefined" trong Service
@NoArgsConstructor //
@AllArgsConstructor // Tự động tạo Constructor đầy đủ 8 tham số để giải quyết lỗi ở ảnh image_39eaea.png
public class Address {

    @Id //
    @GeneratedValue(strategy = GenerationType.IDENTITY) //
    private Long addressId; //

    @NotBlank //
    @Size(min = 5, message = "Street name must contain atleast 5 characters") //
    private String street; //

    @NotBlank //
    @Size(min = 5, message = "Building name must contain atleast 5 characters") //
    private String buildingName; //

    @NotBlank //
    @Size(min = 4, message = "City name must contain atleast 4 characters") //
    private String city; //

    @NotBlank //
    @Size(min = 2, message = "State name must contain atleast 2 characters") //
    private String state; //

    @NotBlank //
    @Size(min = 2, message = "Country name must contain atleast 2 characters") //
    private String country; //

    @NotBlank //
    @Size(min = 6, message = "Pincode must contain atleast 6 characters") //
    private String pincode; //

    @ManyToMany(mappedBy = "addresses") // Thiết lập mối quan hệ nhiều-nhiều với User
    private List<User> users = new ArrayList<>(); //

}