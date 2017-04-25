package me.ichengzi.experiment.os.resource;

import me.ichengzi.experiment.os.ReturnUtil;
import me.ichengzi.experiment.os.process.Process;

/**
 * Coding is pretty charming when you love it!
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:09
 */
public interface Resource {

    int getRID();

    ReturnUtil request(Process process, int need);
    ReturnUtil release(Process process);

    boolean removeBlockProcess(Process process);
    void printCurrentStauts();

    void clear();
}
