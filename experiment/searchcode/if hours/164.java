package com.technoetic.xplanner.metrics;

/**
 * The Class DeveloperMetrics.
 */
public class DeveloperMetrics {
    
    /** The id. */
    private int id;
    
    /** The name. */
    private String name;
    
    /** The iteration id. */
    private int iterationId;
    
    /** The hours. */
    private double hours;
    
    /** The paired hours. */
    private double pairedHours;
    
    /** The accepted task hours. */
    private double acceptedTaskHours;
    
    /** The accepted story hours. */
    private double acceptedStoryHours;
    
    /** The own tasks worked hours. */
    private double ownTasksWorkedHours;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the iteration id.
     *
     * @param iterationId
     *            the new iteration id
     */
    public void setIterationId(final int iterationId) {
        this.iterationId = iterationId;
    }

    /**
     * Gets the iteration id.
     *
     * @return the iteration id
     */
    public int getIterationId() {
        return this.iterationId;
    }

    /**
     * Sets the hours.
     *
     * @param hours
     *            the new hours
     */
    public void setHours(final double hours) {
        this.hours = hours;
    }

    /**
     * Gets the hours.
     *
     * @return the hours
     */
    public double getHours() {
        return this.hours;
    }

    /**
     * Sets the paired hours.
     *
     * @param pairedHours
     *            the new paired hours
     */
    public void setPairedHours(final double pairedHours) {
        this.pairedHours = pairedHours;
    }

    /**
     * Gets the paired hours.
     *
     * @return the paired hours
     */
    public double getPairedHours() {
        return this.pairedHours;
    }

    /**
     * Gets the paired hours percentage.
     *
     * @return the paired hours percentage
     */
    public double getPairedHoursPercentage() {
        if (this.hours == 0) {
            return 0;
        }
        return this.pairedHours * 100 / this.hours;
    }

    /**
     * Gets the unpaired hours.
     *
     * @return the unpaired hours
     */
    public double getUnpairedHours() {
        return this.hours - this.pairedHours;
    }

    /**
     * Gets the accepted hours.
     *
     * @return the accepted hours
     */
    public double getAcceptedHours() {
        return this.getAcceptedStoryHours() + this.getAcceptedTaskHours();
    }

    /**
     * Gets the accepted task hours.
     *
     * @return the accepted task hours
     */
    public double getAcceptedTaskHours() {
        return this.acceptedTaskHours;
    }

    /**
     * Sets the accepted task hours.
     *
     * @param acceptedTaskHours
     *            the new accepted task hours
     */
    public void setAcceptedTaskHours(final double acceptedTaskHours) {
        this.acceptedTaskHours = acceptedTaskHours;
    }

    /**
     * Gets the accepted story hours.
     *
     * @return the accepted story hours
     */
    public double getAcceptedStoryHours() {
        return this.acceptedStoryHours;
    }

    /**
     * Sets the accepted story hours.
     *
     * @param acceptedStoryHours
     *            the new accepted story hours
     */
    public void setAcceptedStoryHours(final double acceptedStoryHours) {
        this.acceptedStoryHours = acceptedStoryHours;
    }

    /**
     * Gets the own task hours.
     *
     * @return the own task hours
     */
    public double getOwnTaskHours() {
        return this.ownTasksWorkedHours;
    }

    /**
     * Sets the own tasks worked hours.
     *
     * @param ownTasksWorkedHours
     *            the new own tasks worked hours
     */
    public void setOwnTasksWorkedHours(final double ownTasksWorkedHours) {
        this.ownTasksWorkedHours = ownTasksWorkedHours;
    }

    /**
     * Gets the remaining task hours.
     *
     * @return the remaining task hours
     */
    public double getRemainingTaskHours() {
        return this.getAcceptedTaskHours() + this.getAcceptedStoryHours()
                - this.getOwnTaskHours();
    }
}

