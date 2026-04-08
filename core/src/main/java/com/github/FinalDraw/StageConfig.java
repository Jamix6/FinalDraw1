package com.github.FinalDraw;

public class StageConfig {
    public final int stageNumber;
    public final String stageName;
    public final String description;
    public final float aiPowerupChance;
    public final int playerRoundPowerupCount;
    public final int aiRoundPowerupCount;
    public final int aiInventoryPowerupCount;
    public final int playerTargetDelta;
    public final int aiTargetDelta;
    public final PowerupType[] availablePowerups;

    public StageConfig(int stageNumber, String stageName, String description,
                       float aiPowerupChance,
                       int playerRoundPowerupCount,
                       int aiRoundPowerupCount,
                       int aiInventoryPowerupCount,
                       int playerTargetDelta,
                       int aiTargetDelta,
                       PowerupType[] availablePowerups) {
        this.stageNumber = stageNumber;
        this.stageName = stageName;
        this.description = description;
        this.aiPowerupChance = aiPowerupChance;
        this.playerRoundPowerupCount = playerRoundPowerupCount;
        this.aiRoundPowerupCount = aiRoundPowerupCount;
        this.aiInventoryPowerupCount = aiInventoryPowerupCount;
        this.playerTargetDelta = playerTargetDelta;
        this.aiTargetDelta = aiTargetDelta;
        this.availablePowerups = availablePowerups;
    }

    public static StageConfig forStage(int stage) {
        switch (stage) {
            case 2:
                return new StageConfig(
                    2,
                    "Stage II",
                    "AI keeps one powerup between rounds and chooses stronger tactics.",
                    0.45f,
                    2,
                    2,
                    1,
                    0,
                    0,
                    new PowerupType[] {
                        PowerupType.GO_FOR_17,
                        PowerupType.GO_FOR_24,
                        PowerupType.PROTECTION,
                        PowerupType.RESHUFFLE,
                        PowerupType.DOUBLE_DOWN,
                        PowerupType.FORESIGHT
                    }
                );
            case 3:
                return new StageConfig(
                    3,
                    "Stage III",
                    "AI gains better powerup synergy and the round feels more tactical.",
                    0.55f,
                    2,
                    2,
                    1,
                    0,
                    1,
                    new PowerupType[] {
                        PowerupType.GO_FOR_17,
                        PowerupType.GO_FOR_24,
                        PowerupType.PROTECTION,
                        PowerupType.RESHUFFLE,
                        PowerupType.DOUBLE_DOWN,
                        PowerupType.FORESIGHT,
                        PowerupType.SWAP,
                        PowerupType.OVERLOAD
                    }
                );
            case 4:
                return new StageConfig(
                    4,
                    "Stage IV",
                    "More aggressive AI powerups and deeper hand control make each decision matter.",
                    0.65f,
                    2,
                    2,
                    2,
                    0,
                    1,
                    new PowerupType[] {
                        PowerupType.GO_FOR_17,
                        PowerupType.GO_FOR_24,
                        PowerupType.PROTECTION,
                        PowerupType.RESHUFFLE,
                        PowerupType.DOUBLE_DOWN,
                        PowerupType.FORESIGHT,
                        PowerupType.SWAP,
                        PowerupType.OVERLOAD,
                        PowerupType.DISCARD
                    }
                );
            case 5:
                return new StageConfig(
                    5,
                    "Stage V",
                    "Final test: the AI uses powerful powerups and keeps you on your toes.",
                    0.75f,
                    2,
                    2,
                    2,
                    0,
                    2,
                    new PowerupType[] {
                        PowerupType.GO_FOR_17,
                        PowerupType.GO_FOR_24,
                        PowerupType.PROTECTION,
                        PowerupType.RESHUFFLE,
                        PowerupType.DOUBLE_DOWN,
                        PowerupType.FORESIGHT,
                        PowerupType.SWAP,
                        PowerupType.OVERLOAD,
                        PowerupType.DISCARD,
                        PowerupType.TOSS,
                        PowerupType.CLEAR
                    }
                );
            case 1:
            default:
                return new StageConfig(
                    1,
                    "Stage I",
                    "Intro stage: learn the basics and get comfortable with powerups.",
                    0.35f,
                    2,
                    2,
                    0,
                    0,
                    0,
                    new PowerupType[] {
                        PowerupType.GO_FOR_17,
                        PowerupType.GO_FOR_24,
                        PowerupType.PROTECTION,
                        PowerupType.RESHUFFLE,
                        PowerupType.FORESIGHT
                    }
                );
        }
    }

    public PowerupType getRandomPowerup() {
        if (availablePowerups == null || availablePowerups.length == 0) {
            return PowerupType.GO_FOR_17;
        }
        int idx = (int) (Math.random() * availablePowerups.length);
        if (idx < 0) idx = 0;
        if (idx >= availablePowerups.length) idx = availablePowerups.length - 1;
        return availablePowerups[idx];
    }
}
