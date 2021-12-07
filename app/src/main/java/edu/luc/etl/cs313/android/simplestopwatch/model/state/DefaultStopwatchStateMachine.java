package edu.luc.etl.cs313.android.simplestopwatch.model.state;

import edu.luc.etl.cs313.android.simplestopwatch.common.StopwatchModelListener;
import edu.luc.etl.cs313.android.simplestopwatch.model.clock.ClockModel;
import edu.luc.etl.cs313.android.simplestopwatch.model.clock.TickListener;
import edu.luc.etl.cs313.android.simplestopwatch.model.time.TimeModel;

/**
 * An implementation of the state machine for the stopwatch.
 *
 * @author laufer
 */
// SJ Need to converge start/stop and reset button per functional requirements
public class DefaultStopwatchStateMachine implements StopwatchStateMachine {

    public DefaultStopwatchStateMachine(final TimeModel timeModel, final ClockModel clockModel) {
        this.timeModel = timeModel;
        this.clockModel = clockModel;
    }

    private final TimeModel timeModel;

    private final ClockModel clockModel;

    int delay = 0;

    /**
     * The internal state of this adapter component. Required for the State pattern.
     */
    private StopwatchState state;

    protected void setState(final StopwatchState state) {
        this.state = state;
        listener.onStateUpdate(state.getId());
    }

    private StopwatchModelListener listener;

    @Override
    public void setModelListener(final StopwatchModelListener listener) {
        this.listener = listener;
    }

    // forward event uiUpdateListener methods to the current state
    // these must be synchronized because events can come from the
    // UI thread or the timer thread
    @Override public synchronized void onStartStop() { state.onStartStop(); }
    @Override public synchronized void onTick()      { state.onTick(); }

    @Override public void updateUIRuntime() { listener.onTimeUpdate(timeModel.getRuntime()); }

    // known states
    private final StopwatchState STOPPED     = new StoppedState(this);
    private final StopwatchState RUNNING     = new RunningState(this);
    private final StopwatchState INCREMENT   = new IncrementState(this);
    private final StopwatchState STOPPED_RESET  = new StoppedResetState(this);


    // transitions
    @Override public void toRunningState()    { setState(RUNNING); }
    @Override public void toStoppedState()    { setState(STOPPED); }
    @Override public void toStoppedResetState() { setState(STOPPED_RESET); }
    @Override public void toIncrementState()    { setState(INCREMENT); }



    // actions
    @Override public void actionInit()       { toStoppedResetState(); actionReset(); }
    @Override public void actionReset()      { timeModel.resetRuntime(); actionUpdateView(); }
    @Override public void actionStart()      { clockModel.start(); actionInc(); }
    @Override public void actionStop()       { clockModel.stop(); }
    @Override public void actionDec()        { timeModel.decRuntime(); actionUpdateView(); }
    @Override public void actionInc()        { timeModel.incRuntime(); delay = 0; actionUpdateView();}
    @Override public void actionUpdateView() { state.updateView(); }
    @Override public void actionAlarm()      { StopwatchModelListener.playNotification1();} // SJ Can't get around static reference error for alarm sound



    @Override public int getDelay(){
        return delay;
    }

    @Override public void setDelay(int t) {delay = t;}

    @Override public boolean reachMax(){
        return timeModel.isFull();
    }

    @Override public boolean countedDown(){
        return timeModel.isEmpty();
    }
}
