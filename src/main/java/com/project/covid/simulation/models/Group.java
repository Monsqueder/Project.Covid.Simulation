package com.project.covid.simulation.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    private String groupName;

    private double percentOfPopulation;

    private double m;

    private double o;

    private double t;

    private int[] to;

    private int[] ti;

    private int[] tm;
}
