package com.project.covid.simulation.controller;

import com.project.covid.simulation.models.JsonEntity;
import com.project.covid.simulation.models.Simulations.BasicSimulation;
import com.project.covid.simulation.models.DayStatistic;
import com.project.covid.simulation.models.Simulations.GroupedSimulation;
import com.project.covid.simulation.models.Simulations.RandomizedSimulation;
import com.project.covid.simulation.repositories.JsonRepository;
import com.project.covid.simulation.service.GroupedSimulationService;
import com.project.covid.simulation.service.RandomizedSimulationService;
import com.project.covid.simulation.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/simulation")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private RandomizedSimulationService randomizedSimulationService;

    @Autowired
    private GroupedSimulationService groupedSimulationService;

    @Autowired
    private JsonRepository jsonRepository;

    @GetMapping("/{id}")
    public String getSimulation(@PathVariable Long id) {
       if (jsonRepository.existsById(id)) {
           return jsonRepository.getById(id).getJson();
       }
       return null;
    }

    @PostMapping("/")
    public List<DayStatistic> createSimulation(@RequestBody BasicSimulation simulation) {
        return simulationService.createSimulation(simulation);
    }

    @PostMapping("/randomized")
    public List<DayStatistic> createSimulation(@RequestBody RandomizedSimulation simulation) {
        return randomizedSimulationService.createRandomizedSimulation(simulation);
    }

    @PostMapping("/grouped")
    public List<ArrayList<DayStatistic>> createSimulation(@RequestBody GroupedSimulation simulation) {
        return groupedSimulationService.createGroupedSimulation(simulation);
    }

}
