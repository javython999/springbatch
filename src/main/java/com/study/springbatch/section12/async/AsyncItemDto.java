package com.study.springbatch.section12.async;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AsyncItemDto {
    private long id;
    private String firstName;
    private String lastName;
    private String birthDate;
}
