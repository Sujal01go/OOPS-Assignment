package com.fcas;

import java.io.Serial;
import java.io.Serializable;

public interface WorkloadRule extends Serializable {
    int getWorkload();
    boolean checkSchedule();
}
