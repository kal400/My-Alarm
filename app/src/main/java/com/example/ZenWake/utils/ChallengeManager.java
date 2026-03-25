package com.example.ZenWake.utils;

import java.util.Random;

public class ChallengeManager {

    private String difficulty;
    private int consecutiveSuccesses = 0;
    private int consecutiveFailures = 0;

    public ChallengeManager(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getDifficultyLevel() {
        switch (difficulty.toLowerCase()) {
            case "easy":
                return 1;
            case "medium":
                return 2;
            case "hard":
                return 3;
            case "insane":
                return 4;
            default:
                return 2;
        }
    }

    public void recordSuccess() {
        consecutiveSuccesses++;
        consecutiveFailures = 0;

        // Increase difficulty after 3 consecutive successes
        if (consecutiveSuccesses >= 3) {
            increaseDifficulty();
        }
    }

    public void recordFailure() {
        consecutiveFailures++;
        consecutiveSuccesses = 0;

        // Decrease difficulty after 3 consecutive failures
        if (consecutiveFailures >= 3) {
            decreaseDifficulty();
        }
    }

    private void increaseDifficulty() {
        switch (difficulty) {
            case "easy":
                difficulty = "medium";
                break;
            case "medium":
                difficulty = "hard";
                break;
            case "hard":
                difficulty = "insane";
                break;
        }
    }

    private void decreaseDifficulty() {
        switch (difficulty) {
            case "insane":
                difficulty = "hard";
                break;
            case "hard":
                difficulty = "medium";
                break;
            case "medium":
                difficulty = "easy";
                break;
        }
    }

    public String getNextChallengeType() {
        String[] types = {"math", "shake", "memory"};
        Random random = new Random();
        return types[random.nextInt(types.length)];
    }
}