package com.project.covid.simulation.service;

import org.springframework.stereotype.Component;

@Component
public class Randomizer {

    public double getRandomNumber(double a, double b) {
        return a + Math.random() * (b - a);
    }

    public double getRandomNumber(int a, int b) {
        return a + Math.random() * (b - a);
    }

}
