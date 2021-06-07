package com.project.covid.simulation.models.Simulations;

import com.project.covid.simulation.models.DayStatistic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RandomizedSimulation {

    private List<DayStatistic> dayStatistics;

    private String n;

    private int i;

    private int p;

    private int ts;

    private int[] r;

    private double m;

    private double o;

    private double t;

    private int[] to;

    private int[] ti;

    private int[] tm;
}
