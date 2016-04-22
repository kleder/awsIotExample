package co.kleder.homesecurity.shadow;

/**
 * Created by rafal on 19.04.2016.
 * <pre>
 *     {
 "desired": {
 "window": 1,
 "lock": 0,
 "alarm": 0,
 "alarm_reset": 0
 },
 "reported": {
 "window": 0,
 "lock": 0,
 "alarm": 0,
 "alarm_reset": 0
 },
 "delta": {
 "window": 1
 }
 }
 * </pre>
 */
public class AlarmStatus {
    public State state;

    AlarmStatus() {
        state = new State();
    }

    public class State {
        public Desired reported;
        public Delta delta;

        State() {
            reported = new Desired();
            delta = new Delta();
        }

        public class Desired {
            Desired() {
            }

            public Integer window;
            public Integer lock;
            public Integer alarm;
            public Integer alarm_reset;
        }

        public class Delta {
            Delta() {
            }

            public Integer window;
            public Integer lock;
            public Integer alarm;
            public Integer alarm_reset;
        }
    }

    public Long version;
    public Long timestamp;
}
