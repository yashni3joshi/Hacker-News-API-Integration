package com.questionpro.test.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PeakComment  {
    private String text;
    private String userId;
    private int ProfileAge;
    @JsonIgnore
    private int totalComments;


}
