package com.project.covid.simulation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.project.covid.simulation.models.DayStatistic;
import com.project.covid.simulation.models.Group;
import com.project.covid.simulation.models.JsonEntity;
import com.project.covid.simulation.models.Simulations.GroupedSimulation;
import com.project.covid.simulation.repositories.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GroupedSimulationService {

    @Autowired
    private Randomizer randomizer;

    @Autowired
    private JsonRepository jsonRepository;

    public List<ArrayList<DayStatistic>> createGroupedSimulation(GroupedSimulation simulation) {
        ArrayList<ArrayList<DayStatistic>> simulationStatistics = new ArrayList<>();
        ArrayList<ArrayList<HashMap<Integer, Integer>>> groupsMapsList = new ArrayList<>();
        int groupsCount = simulation.getGroups().size();

        for (int i = 0; i < groupsCount; i++) {
            ArrayList<HashMap<Integer, Integer>> list = new ArrayList<>();
            list.add(new HashMap<>());
            list.add(new HashMap<>());
            list.add(new HashMap<>());
            list.add(new HashMap<>());
            groupsMapsList.add(list);


        }

        ArrayList<DayStatistic> firstDayGroupedStatistic = getFirstDayGroupedStatistic(simulation, groupsMapsList, groupsCount);
        simulationStatistics.add(firstDayGroupedStatistic);

        //next days
        ArrayList<DayStatistic> previousDayGroupedStatistic = firstDayGroupedStatistic;
        for (int i = 2; i <= simulation.getTs(); i++) {
            ArrayList<DayStatistic> dayGroupedStatistic = simulateDay(i, groupsCount, previousDayGroupedStatistic, groupsMapsList, simulation);
            previousDayGroupedStatistic = dayGroupedStatistic;
            simulationStatistics.add(dayGroupedStatistic);
        }

        simulation.setDayStatistics(simulationStatistics);
        save(simulation);
        return simulationStatistics;
    }

    private void save(GroupedSimulation simulation) {
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

    private ArrayList<DayStatistic> getFirstDayGroupedStatistic(GroupedSimulation simulation, ArrayList<ArrayList<HashMap<Integer, Integer>>> groupsMapsList, int groupsCount) {
        ArrayList<DayStatistic> firstDayGroupedStatistic = new ArrayList<>();
        int[] infected = new int[groupsCount];
        int population = 0;
        for (int i = 0; i < simulation.getI(); i++) {
            int group = (int) Math.round(Math.random()*(groupsCount-1));
            infected[group] = infected[group] + 1;
        }
        for (int i = 0; i < groupsCount; i++) {
            int healthy;
            if (i != groupsCount-1) {
                healthy = (int) Math.round(simulation.getGroups().get(i).getPercentOfPopulation() * simulation.getP());
                population += healthy;
            } else {
                healthy = simulation.getP() - population;
            }
            DayStatistic firstDayStatistic = getFirstDayStatistic(infected[i], healthy);
            firstDayGroupedStatistic.add(firstDayStatistic);
        }
        divideGroupedInfected(1, groupsMapsList, simulation, infected);
        return firstDayGroupedStatistic;
    }

    private DayStatistic getFirstDayStatistic(int infectedCount, int healthy) {
        DayStatistic firstDayStatistic = new DayStatistic();
        firstDayStatistic.setDayNumber(1);
        firstDayStatistic.setPi(infectedCount);
        firstDayStatistic.setPv(healthy-infectedCount);
        return firstDayStatistic;
    }

    private ArrayList<DayStatistic> simulateDay(
            int dayNumber,
            int groupsCount,
            ArrayList<DayStatistic> previousDayGroupedStatistic,
            ArrayList<ArrayList<HashMap<Integer, Integer>>> groupsMapsList,
            GroupedSimulation simulation) {

        ArrayList<DayStatistic> dayStatistics = inheritGroupsStatistic(dayNumber, groupsCount, previousDayGroupedStatistic);

        checkGroupMaps(dayNumber, groupsCount, groupsMapsList, dayStatistics);

        int[] groupedNewInfected = getInfected(simulation, groupsCount, dayStatistics);

        divideGroupedInfected(dayNumber, groupsMapsList, simulation, groupedNewInfected);

        return dayStatistics;
    }

    private int[] getInfected(GroupedSimulation simulation, int groupsCount, ArrayList<DayStatistic> dayStatistics) {
        int[] groupedNewInfected = new int[groupsCount];
        int infected = 0;
        for (int i = 0; i < groupsCount; i++) {
            DayStatistic dayStatistic = dayStatistics.get(i);
            infected += dayStatistic.getPi();
        }

        fillInfectedGroups(simulation.getR(), groupedNewInfected, groupsCount, infected);
        checkInfectedGroups(groupsCount, dayStatistics, groupedNewInfected);
        return groupedNewInfected;
    }

    private void checkInfectedGroups(int groupsCount, ArrayList<DayStatistic> dayStatistics, int[] groupedNewInfected) {
        for (int j = 0; j < groupsCount; j++) {
            DayStatistic dayStatistic = dayStatistics.get(j);
            if (groupedNewInfected[j] > dayStatistic.getPv()) {
                groupedNewInfected[j] = dayStatistic.getPv();
            }
            dayStatistic.setPv(dayStatistic.getPv() - groupedNewInfected[j]);
            dayStatistic.setPi(dayStatistic.getPi() + groupedNewInfected[j]);
        }
    }

    private void fillInfectedGroups(int[] r, int[] groupedInfected, int groupsCount, int infected) {
        for (int i = 0; i < infected; i++) {
            int infectedByPerson = (int) Math.round(randomizer.getRandomNumber(r[0], r[1]));
            for (int j = 0; j < infectedByPerson; j++) {
                int group = (int) Math.round(Math.random()*(groupsCount-1));
                groupedInfected[group] = groupedInfected[group] + 1;
            }
        }
    }

    private ArrayList<DayStatistic> inheritGroupsStatistic(int dayNumber,
                                                           int groupsCount,
                                                           ArrayList<DayStatistic> previousDayGroupedStatistic) {
        ArrayList<DayStatistic> dayStatistics = new ArrayList<>();
        for (int i = 0; i < groupsCount; i++) {
            DayStatistic dayStatistic = inheritPreviousDayStatistic(dayNumber, previousDayGroupedStatistic, i);
            dayStatistics.add(dayStatistic);
        }
        return dayStatistics;
    }

    private DayStatistic inheritPreviousDayStatistic(int dayNumber, ArrayList<DayStatistic> previousDayGroupedStatistic, int i) {
        DayStatistic dayStatistic = new DayStatistic();
        DayStatistic previousDay = previousDayGroupedStatistic.get(i);

        dayStatistic.setPv(previousDay.getPv());
        dayStatistic.setPm(previousDay.getPm());
        dayStatistic.setPi(previousDay.getPi());
        dayStatistic.setPr(previousDay.getPr());
        dayStatistic.setDayNumber(dayNumber);
        return dayStatistic;
    }

    private void checkGroupMaps(int dayNumber, int groupCount, ArrayList<ArrayList<HashMap<Integer, Integer>>> groupsMapsList, ArrayList<DayStatistic> dayStatistics) {
        for (int i = 0; i < groupCount; i++) {
            checkMapsOfInfected(dayNumber, groupsMapsList.get(i), dayStatistics.get(i));
        }
    }

    private void checkMapsOfInfected(int dayNumber, ArrayList<HashMap<Integer, Integer>> infectedMapsList, DayStatistic dayStatistic) {

        HashMap<Integer, Integer> preDeadMap = infectedMapsList.get(0);
        HashMap<Integer, Integer> preImmunizedMap = infectedMapsList.get(1);
        HashMap<Integer, Integer> preNotImmunizedMap = infectedMapsList.get(2);
        HashMap<Integer, Integer> preTemporaryImmunizedMap = infectedMapsList.get(3);

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

    private void divideGroupedInfected(int dayNumber, ArrayList<ArrayList<HashMap<Integer, Integer>>> groupsMapsList, GroupedSimulation simulation, int[] infected) {
        for (int i = 0; i < simulation.getGroups().size(); i++) {
            divideInfected(dayNumber, groupsMapsList.get(i), simulation.getGroups().get(i), infected[i]);
        }
    }

    private void divideInfected(int dayNumber,
                                ArrayList<HashMap<Integer, Integer>> infectedMapsList,
                                Group group,
                                int infected) {

        HashMap<Integer, Integer> preDeadMap = infectedMapsList.get(0);
        HashMap<Integer, Integer> preImmunizedMap = infectedMapsList.get(1);
        HashMap<Integer, Integer> preNotImmunizedMap = infectedMapsList.get(2);
        HashMap<Integer, Integer> preTemporaryImmunizedMap = infectedMapsList.get(3);

        for (int i = 0; i < infected; i++) {
            double randomPercent = Math.random();
            int day = dayNumber + (int) Math.round(this.randomizer.getRandomNumber(group.getTi()[0], group.getTi()[1]));
            if (randomPercent <= group.getM()) {
                day = dayNumber + (int) Math.round(this.randomizer.getRandomNumber(group.getTm()[0], group.getTm()[1]));
                if (preDeadMap.containsKey(day)) {
                    preDeadMap.put(day, preDeadMap.get(day) + 1);
                } else {
                    preDeadMap.put(day, 1);
                }
            } else if (randomPercent <= group.getM() + group.getO()) {
                if (preImmunizedMap.containsKey(day)) {
                    preImmunizedMap.put(day, preImmunizedMap.get(day) + 1);
                } else {
                    preImmunizedMap.put(day, 1);
                }
            } else if (randomPercent <= group.getM() + group.getO() + group.getT()) {
                if (preImmunizedMap.containsKey(day)) {
                    preImmunizedMap.put(day, preImmunizedMap.get(day) + 1);
                } else {
                    preImmunizedMap.put(day, 1);
                }
                int daysToLoseImmunity = day + (int) Math.round(this.randomizer.getRandomNumber(group.getTo()[0], group.getTo()[1]));
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
