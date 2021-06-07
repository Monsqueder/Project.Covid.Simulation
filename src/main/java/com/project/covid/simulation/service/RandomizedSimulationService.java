package com.project.covid.simulation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.project.covid.simulation.models.DayStatistic;
import com.project.covid.simulation.models.JsonEntity;
import com.project.covid.simulation.models.Simulations.GroupedSimulation;
import com.project.covid.simulation.models.Simulations.RandomizedSimulation;
import com.project.covid.simulation.repositories.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RandomizedSimulationService {

    @Autowired
    private Randomizer randomizer;

    @Autowired
    private JsonRepository jsonRepository;

    public List<DayStatistic> createRandomizedSimulation(RandomizedSimulation simulation) {
        ArrayList<DayStatistic> dayStatistics = new ArrayList<>();

        HashMap<Integer, Integer> preDeadMap = new HashMap<>();
        HashMap<Integer, Integer> preImmunizedMap = new HashMap<>();
        HashMap<Integer, Integer> preNotImmunizedMap = new HashMap<>();
        HashMap<Integer, Integer> preTemporaryImmunizedMap = new HashMap<>();

        //start first day
        DayStatistic firstDayStatistic = new DayStatistic();
        firstDayStatistic.setDayNumber(1);
        firstDayStatistic.setPi(simulation.getI());
        firstDayStatistic.setPv(simulation.getP()-simulation.getI());

        divideInfected(1, preDeadMap, preImmunizedMap, preNotImmunizedMap, preTemporaryImmunizedMap, simulation, simulation.getI());
        dayStatistics.add(firstDayStatistic);

        //next days
        DayStatistic previousDay = firstDayStatistic;
        for (int i = 2; i <= simulation.getTs(); i++) {
            DayStatistic currentDay = this.simulateDay(
                    i,
                    previousDay,
                    preDeadMap,
                    preImmunizedMap,
                    preNotImmunizedMap,
                    preTemporaryImmunizedMap,
                    simulation
            );
            previousDay = currentDay;
            dayStatistics.add(currentDay);
        }

        simulation.setDayStatistics(dayStatistics);
        save(simulation);

        return dayStatistics;
    }

    private void save(RandomizedSimulation simulation) {
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
                                     HashMap<Integer, Integer> preNotImmunizedMap,
                                     HashMap<Integer, Integer> preTemporaryImmunizedMap,
                                     RandomizedSimulation simulation) {
        DayStatistic dayStatistic = inheritPreviousDayStatistic(dayNumber, previousDay);
        checkMapsOfInfected(dayNumber, preDeadMap, preImmunizedMap, preNotImmunizedMap, preTemporaryImmunizedMap, dayStatistic);
        int newInfected = getNewInfected(simulation, dayStatistic);
        divideInfected(dayNumber, preDeadMap, preImmunizedMap, preNotImmunizedMap, preTemporaryImmunizedMap, simulation, newInfected);

        return dayStatistic;
    }

    private int getNewInfected(RandomizedSimulation simulation, DayStatistic dayStatistic) {
        int newInfected = 0;
        for (int i = 0; i < dayStatistic.getPi(); i++) {
            int infectedByPerson = (int) Math.round(this.randomizer.getRandomNumber(simulation.getR()[0], simulation.getR()[1]));
            newInfected = newInfected + infectedByPerson;
        }
        if (newInfected > dayStatistic.getPv()) {
            newInfected = dayStatistic.getPv();
        }
        dayStatistic.setPv(dayStatistic.getPv()-newInfected);
        dayStatistic.setPi(dayStatistic.getPi()+newInfected);
        return newInfected;
    }

    private DayStatistic inheritPreviousDayStatistic(int dayNumber, DayStatistic previousDay) {
        DayStatistic dayStatistic = new DayStatistic();
        dayStatistic.setPv(previousDay.getPv());
        dayStatistic.setPm(previousDay.getPm());
        dayStatistic.setPi(previousDay.getPi());
        dayStatistic.setPr(previousDay.getPr());
        dayStatistic.setDayNumber(dayNumber);
        return dayStatistic;
    }

    private void checkMapsOfInfected(int dayNumber, HashMap<Integer, Integer> preDeadMap, HashMap<Integer, Integer> preImmunizedMap, HashMap<Integer, Integer> preNotImmunizedMap, HashMap<Integer, Integer> preTemporaryImmunizedMap, DayStatistic dayStatistic) {
        //let people die
        if (preDeadMap.containsKey(dayNumber)) {
            dayStatistic.setPm(dayStatistic.getPm()+preDeadMap.get(dayNumber));
            dayStatistic.setPi(dayStatistic.getPi()-preDeadMap.get(dayNumber));
            preDeadMap.remove(dayNumber);
        }

        //let people come back to health
        if (preNotImmunizedMap.containsKey(dayNumber)) {
            dayStatistic.setPv(dayStatistic.getPv()+preNotImmunizedMap.get(dayNumber));
            dayStatistic.setPi(dayStatistic.getPi()-preNotImmunizedMap.get(dayNumber));
            preNotImmunizedMap.remove(dayNumber);
        }

        //let people get immunity
        if (preImmunizedMap.containsKey(dayNumber)) {
            dayStatistic.setPr(dayStatistic.getPr()+preImmunizedMap.get(dayNumber));
            dayStatistic.setPi(dayStatistic.getPi()-preImmunizedMap.get(dayNumber));
            preImmunizedMap.remove(dayNumber);
        }

        //let people lose immunity
        if (preTemporaryImmunizedMap.containsKey(dayNumber)) {
            dayStatistic.setPr(dayStatistic.getPr()-preTemporaryImmunizedMap.get(dayNumber));
            dayStatistic.setPv(dayStatistic.getPv()+preTemporaryImmunizedMap.get(dayNumber));
            preTemporaryImmunizedMap.remove(dayNumber);
        }
    }

    private void divideInfected(int dayNumber,
                                HashMap<Integer, Integer> preDeadMap,
                                HashMap<Integer, Integer> preImmunizedMap,
                                HashMap<Integer, Integer> preNotImmunizedMap,
                                HashMap<Integer, Integer> preTemporaryImmunizedMap,
                                RandomizedSimulation simulation,
                                int infected) {
        for (int i = 0; i < infected; i++) {
            double randomPercent = Math.random();
            int day = dayNumber + (int) Math.round(this.randomizer.getRandomNumber(simulation.getTi()[0], simulation.getTi()[1]));
            if (randomPercent <= simulation.getM()) {
                day = dayNumber + (int) Math.round(this.randomizer.getRandomNumber(simulation.getTm()[0], simulation.getTm()[1]));
                if (preDeadMap.containsKey(day)) {
                    preDeadMap.put(day, preDeadMap.get(day) + 1);
                } else {
                    preDeadMap.put(day, 1);
                }
            } else if (randomPercent <= simulation.getM() + simulation.getO()) {
                if (preImmunizedMap.containsKey(day)) {
                    preImmunizedMap.put(day, preImmunizedMap.get(day) + 1);
                } else {
                    preImmunizedMap.put(day, 1);
                }
            } else if (randomPercent <= simulation.getM() + simulation.getO() + simulation.getT()) {
                if (preImmunizedMap.containsKey(day)) {
                    preImmunizedMap.put(day, preImmunizedMap.get(day) + 1);
                } else {
                    preImmunizedMap.put(day, 1);
                }
                int daysToLoseImmunity = day + (int) Math.round(this.randomizer.getRandomNumber(simulation.getTo()[0], simulation.getTo()[1]));
                if (preTemporaryImmunizedMap.containsKey(daysToLoseImmunity)) {
                    preTemporaryImmunizedMap.put(daysToLoseImmunity, preTemporaryImmunizedMap.get(daysToLoseImmunity) + 1);
                } else {
                    preTemporaryImmunizedMap.put(daysToLoseImmunity, 1);
                }
            } else {
                if (preNotImmunizedMap.containsKey(day)) {
                    preNotImmunizedMap.put(day, preNotImmunizedMap.get(day) + 1);
                } else {
                    preNotImmunizedMap.put(day, 1);
                }
            }
        }
    }
}
