package com.project.covid.simulation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.project.covid.simulation.models.DayStatistic;
import com.project.covid.simulation.models.JsonEntity;
import com.project.covid.simulation.models.Simulations.BasicSimulation;
import com.project.covid.simulation.models.Simulations.GroupedSimulation;
import com.project.covid.simulation.repositories.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SimulationService {

    @Autowired
    private JsonRepository jsonRepository;
    
    public List<DayStatistic> createSimulation(BasicSimulation simulation) {
        ArrayList<DayStatistic> dayStatistics = new ArrayList<>();
        HashMap<Integer, Integer> preDeadMap = new HashMap<>();
        HashMap<Integer, Integer> preImmunizedMap = new HashMap<>();

        //start day
        DayStatistic firstDayStatistic = new DayStatistic();
        firstDayStatistic.setDayNumber(1);
        firstDayStatistic.setPi(simulation.getI());
        firstDayStatistic.setPv(simulation.getP()-simulation.getI());
        int preDead = (int) Math.round(simulation.getI() * simulation.getM());
        int preImmunized = simulation.getI() - preDead;
        preDeadMap.put(1, preDead);
        preImmunizedMap.put(1, preImmunized);
        dayStatistics.add(firstDayStatistic);

        //next days
        DayStatistic previousDay = firstDayStatistic;
        for (int i = 2; i <= simulation.getTs(); i++) {
            DayStatistic currentDay = this.simulateDay(i, previousDay, preDeadMap, preImmunizedMap, simulation);
            previousDay = currentDay;
            dayStatistics.add(currentDay);
        }

        simulation.setDayStatistics(dayStatistics);
        save(simulation);
        return dayStatistics;
    }

    private void save(BasicSimulation simulation) {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            String json = writer.writeValueAsString(simulation);
            JsonEntity jsonEntity = new JsonEntity();
            jsonEntity.setJson(json);
            jsonRepository.save(jsonEntity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private DayStatistic simulateDay(int dayNumber,
                                     DayStatistic previousDay,
                                     HashMap<Integer, Integer> preDeadMap,
                                     HashMap<Integer, Integer> preImmunizedMap,
                                     BasicSimulation simulation) {

        DayStatistic dayStatistic = new DayStatistic();
        dayStatistic.setPv(previousDay.getPv());
        dayStatistic.setPm(previousDay.getPm());
        dayStatistic.setPi(previousDay.getPi());
        dayStatistic.setPr(previousDay.getPr());
        dayStatistic.setDayNumber(dayNumber);

        //let people die
        int deathKey = dayNumber-simulation.getTm();
        if (preDeadMap.containsKey(deathKey)) {
            dayStatistic.setPm(dayStatistic.getPm()+preDeadMap.get(deathKey));
            dayStatistic.setPi(dayStatistic.getPi()-preDeadMap.get(deathKey));
            preDeadMap.remove(deathKey);
        }

        //let people get immunity
        int immunizedKey = dayNumber-simulation.getTi();
        if (preImmunizedMap.containsKey(immunizedKey)) {
            dayStatistic.setPr(dayStatistic.getPr()+preImmunizedMap.get(immunizedKey));
            dayStatistic.setPi(dayStatistic.getPi()-preImmunizedMap.get(immunizedKey));
            preImmunizedMap.remove(immunizedKey);
        }

        //let people get infected
        int newInfected = dayStatistic.getPi()*simulation.getR();
        if (newInfected > dayStatistic.getPv()) {
            newInfected = dayStatistic.getPv();
        }
        dayStatistic.setPv(dayStatistic.getPv()-newInfected);
        dayStatistic.setPi(dayStatistic.getPi()+newInfected);

        //divide people to preDead and preImmunized
        int preDead = (int) (newInfected * simulation.getM());
        int preImmunized = newInfected - preDead;

        preDeadMap.put(dayNumber, preDead);
        preImmunizedMap.put(dayNumber, preImmunized);

        return dayStatistic;
    }
}