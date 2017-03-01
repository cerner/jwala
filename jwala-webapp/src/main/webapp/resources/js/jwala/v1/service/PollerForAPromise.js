/**
 * A data poller that uses window object's setInterval to fire a browser event to execute a method (runner) that returns
 * a promise. The reason for using a browser event is to prevent IE from not polling for data after some time when using
 * a recursive method to do polling. The event firing replaces the recursive method polling pattern.
 *
 * @param interval - time interval to run poller in milliseconds.
 * @runner - the method that sends a request. The method should return a PROMISE.
 * Please see https://github.com/petkaantonov/bluebird.
 * @theCallback - callback method to run when the response comes in.
 * @caughtCallback - callback method to run when the request comes back on an error.
 *
 * Usage example: var pollerForAPromise = new PollerForAPromise(1000, getState, processState, processError);
 *
 * Created by Jedd Cuison on 10/23/2015.
 */
function PollerForAPromise(interval, runner, thenCallback, caughtCallback) {
    this.interval = interval;
    this.isStarted = false;
    this.isRunning = false;
    this.runner = runner;
    var self = this;
    this.runnerWrapper = function() {
        if (!self.isRunning) {
            self.isRunning = true;
            var self2 = self;
            runner().then(thenCallback).caught(caughtCallback).lastly(function() {self2.isRunning = false});
        }
     }
}

/**
 * Start polling.
 */
PollerForAPromise.prototype.start = function() {
    this.intervalHandle = setInterval(this.runnerWrapper, this.interval);
    this.isStarted = true;
}

/**
 * Stop polling.
 */
PollerForAPromise.prototype.stop = function() {
    clearInterval(this.intervalHandle);
    this.isStarted = false;
}

PollerForAPromise.prototype.isActive = function() {
    return this.isStarted;
}