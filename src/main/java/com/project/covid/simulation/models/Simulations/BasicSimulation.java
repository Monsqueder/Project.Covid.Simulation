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
public class BasicSimulation {

    private List<DayStatistic> dayStatistics;

    private String n;

    private int i;

    private int p;

    private int ts;

    private int r;

    private double m;

    private int ti;

    private int tm;
}
