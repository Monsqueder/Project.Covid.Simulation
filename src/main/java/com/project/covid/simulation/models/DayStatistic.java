package com.project.covid.simulation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.covid.simulation.models.Simulations.BasicSimulation;
import com.project.covid.simulation.models.Simulations.GroupedSimulation;
import com.project.covid.simulation.models.Simulations.RandomizedSimulation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DayStatistic {

    private int dayNumber;

    private int pi;

    private int pv;

    private int pm;

    private int pr;

}
