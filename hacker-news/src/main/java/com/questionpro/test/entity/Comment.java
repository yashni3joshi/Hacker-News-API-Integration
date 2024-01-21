package com.questionpro.test.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comment implements Serializable {
    private static final long serialVersionUID = -1627291979624493226L;
    private String by;
    private int id;
    private int parent;
    private int[] kids;
    private String text;
    private int time;
}
