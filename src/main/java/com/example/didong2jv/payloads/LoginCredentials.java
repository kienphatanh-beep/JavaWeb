package com.example.didong2jv.payloads; //

import lombok.*; //

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCredentials {
    private String email;
    private String password;
}