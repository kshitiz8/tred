package org.tred.base.concurrent;

/**
 *
 * Imagine at the end of a political conference, republicans and democrats are trying to leave the venue and ordering
 * Uber rides at the same time. However, to make sure no fight breaks out in an Uber ride, the software developers at
 * Uber come up with an algorithm whereby either an Uber ride can have all democrats or republicans or two Democrats and
 * two Republicans. All other combinations can result in a fist-fight.
 *
 * Your task as the Uber developer is to model the ride requestors as threads. Once an acceptable combination of riders
 * is possible, threads are allowed to proceed to ride. Each thread invokes the method seated() when selected by the
 * system for the next ride. When all the threads are seated, any one of the four threads can invoke the method drive()
 * to inform the driver to start the ride.

 */
public class UberRide {

}
