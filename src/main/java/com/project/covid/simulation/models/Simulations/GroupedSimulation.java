package com.project.covid.simulation.models.Simulations;

import com.project.covid.simulation.models.DayStatistic;
import com.project.covid.simulation.models.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupedSimulation {

    private ArrayList<ArrayList<DayStatistic>> dayStatistics;

    private List<Group> groups;

    private String n;

    private int i;

    private int p;

    private int ts;

    private int[] r;
}
