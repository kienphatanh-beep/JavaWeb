package com.example.didong2jv.payloads; //

import lombok.*; //

@Data //
@NoArgsConstructor //
@AllArgsConstructor //
public class APIResponse {
    private String message;
    private boolean status;
}