package org.inspirecenter.amazechallenge.model;

import java.io.Serializable;

/**
 * Models a challenge by describing its properties (e.g. grid, num of players, whether its
 * repeatable, etc.)
 *
 * @author Nearchos
 *         Created: 23-Oct-17
 */

@com.googlecode.objectify.annotation.Entity
public class Challenge implements Serializable {

    public static final int DEFAULT_MIN_ACTIVE_PLAYERS = 1;
    public static final int DEFAULT_MAX_ACTIVE_PLAYERS = 10;

    @com.googlecode.objectify.annotation.Id
    public Long id;
    private String name;
    private int apiVersion; // used to determine if the client/player can 'support' this challenge
    private String description; // brief description of the challenge
    private boolean canRepeat; // whether a player can play again and again
    private boolean canJoinAfterStart; // whether a player can join after the start of the challenge
    private boolean canStepOnEachOther; // whether players can step on each other
    private int minActivePlayers = DEFAULT_MIN_ACTIVE_PLAYERS; // minimum number of players needed to start game
    private int maxActivePlayers = DEFAULT_MAX_ACTIVE_PLAYERS; // maximum number of players allowed to be active at a time (the rest are in a queue)
    private long startTimestamp; // when the challenge starts being available, or zero if available from the beginning of time (timestamp in UTC)
    private long endTimestamp; // when the challenge ends being available, or zero if available forever (timestamp in UTC)
    private boolean hasQuestionnaire; // true when the challenge requires answering a questionnaire before/after the game

    private int maxRewards = 0;
    private int maxPenalties = 0;

    private Grid grid;
    private String lineColor;
    private String difficulty;
    private String createdBy;
    private long createdOn;
    private String backgroundAudioName;
    private Audio.AudioFormat backgroundAudioFormat;
    private String backgroundImageName;
    private BackgroundImage.BackgroundImageType backgroundImageType;

    public Challenge() {
        super();
    }

    public Challenge(String name, int apiVersion, String description, boolean canRepeat,
                     boolean canJoinAfterStart, boolean canStepOnEachOther, int minActivePlayers,
                     int maxActivePlayers, long startTimestamp, long endTimestamp, boolean hasQuestionnaire,
                     int maxRewards, int maxPenalties,
                     Grid grid, String lineColor, String difficulty, String createdBy, long createdOn,
                     String backgroundAudioName, String backgroundAudioFormat,
                     String backgroundImageName, BackgroundImage.BackgroundImageType backgroundImageType) {
        this();
        this.name = name;
        this.apiVersion = apiVersion;
        this.description = description;
        this.canRepeat = canRepeat;
        this.canJoinAfterStart = canJoinAfterStart;
        this.canStepOnEachOther = canStepOnEachOther;
        this.minActivePlayers = minActivePlayers;
        this.maxActivePlayers = maxActivePlayers;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.hasQuestionnaire = hasQuestionnaire;

        this.maxRewards = maxRewards;
        this.maxPenalties = maxPenalties;

        this.grid = grid;
        this.lineColor = lineColor;
        this.difficulty = difficulty;
        this.createdBy = createdBy;
        this.createdOn = createdOn;

        this.backgroundAudioFormat = Audio.AudioFormat.fromText(backgroundAudioFormat);
        this.backgroundAudioName = backgroundAudioName;

        this.backgroundImageName = backgroundImageName;
        this.backgroundImageType = backgroundImageType;
    }

    public Challenge(long id, String name, int apiVersion, String description, boolean canRepeat,
                     boolean canJoinAfterStart, boolean canStepOnEachOther, long startTimestamp,
                     long endTimestamp, boolean hasQuestionnaire, int maxRewards, int maxPenalties,
                     Grid grid, String lineColor, String difficulty, String createdBy,
                     long createdOn, String backgroundAudioName, String backgroundAudioFormat,
                     String backgroundImageName, BackgroundImage.BackgroundImageType backgroundImageType) {
        this(name, apiVersion, description, canRepeat, canJoinAfterStart, canStepOnEachOther,
                DEFAULT_MIN_ACTIVE_PLAYERS, DEFAULT_MAX_ACTIVE_PLAYERS, startTimestamp,
                endTimestamp, hasQuestionnaire, maxRewards, maxPenalties, grid, lineColor,
                difficulty, createdBy, createdOn, backgroundAudioName, backgroundAudioFormat,
                backgroundImageName, backgroundImageType);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public String getDescription() {
        return description;
    }

    public ChallengeDifficulty getDifficulty() {
        return ChallengeDifficulty.getChallengeDifficulty(difficulty);
    }

    public boolean canRepeat() {
        return canRepeat;
    }

    public boolean isCanJoinAfterStart() {
        return canJoinAfterStart;
    }

    public boolean isCanStepOnEachOther() {
        return canStepOnEachOther;
    }

    public int getMinActivePlayers() {
        return minActivePlayers;
    }

    public int getMaxActivePlayers() {
        return maxActivePlayers;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public boolean hasStarted() {
        return System.currentTimeMillis() > startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public boolean hasEnded() {
        return System.currentTimeMillis() > endTimestamp;
    }

    public boolean isActive() {
        return hasStarted() && !hasEnded();
    }

    public Grid getGrid() {
        return grid;
    }

    public String getLineColor() {
        return lineColor;
    }

    public BackgroundImage getBackgroundImage() {
        return BackgroundImage.fromString(backgroundImageName);
    }

    public BackgroundImage.BackgroundImageType getBackgroundImageType() {
        return backgroundImageType;
    }

    public int getMaxRewards() {
        return maxRewards;
    }

    public int getMaxPenalties() {
        return maxPenalties;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getBackgroundAudioName() {
        return backgroundAudioName;
    }

    public Audio.AudioFormat getBackgroundAudioFormat() {
        return backgroundAudioFormat;
    }

    public boolean getHasQuestionnaire() {
        return hasQuestionnaire;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", apiVersion=" + apiVersion +
                ", description='" + description + '\'' +
                ", canRepeat=" + canRepeat +
                ", canJoinAfterStart=" + canJoinAfterStart +
                ", canStepOnEachOther=" + canStepOnEachOther +
                ", minActivePlayers=" + minActivePlayers +
                ", maxActivePlayers=" + maxActivePlayers +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", maxRewards=" + maxRewards +
                ", maxPenalties=" + maxPenalties +
                ", grid=" + grid +
                ", lineColor='" + lineColor + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdOn=" + createdOn +
                ", backgroundAudioName='" + backgroundAudioName + '\'' +
                ", backgroundAudioFormat=" + backgroundAudioFormat +
                ", backgroundImageName='" + backgroundImageName + '\'' +
                ", backgroundImageType=" + backgroundImageType +
                '}';
    }
}