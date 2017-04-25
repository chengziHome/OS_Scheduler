package me.ichengzi.experiment.os.process;

import me.ichengzi.experiment.os.ReturnUtil;
import me.ichengzi.experiment.os.resource.Resource;

import java.util.List;
import java.util.Map;

/**
 * Coding is pretty charming when you love it!
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:05
 */
public interface Process {

    int getPID();
    String getName();
    int getPriority();
    Map<Resource,Integer> getResourceMap();
    Process getParent();
    void setParent(Process parent);
    void removeParent();
    List<Process> getChildren();
    void addChild(Process process);
    void removeChild(Process process);


    boolean setState(ProcessImpl.State state);
    ProcessImpl.State getState();

    Resource getBlockResource();
    void setBlockResource(Resource resource);

    ReturnUtil destory();
    ReturnUtil killSubTree();
    void destoryInit();//杀掉init进程就终止程序了。


}
