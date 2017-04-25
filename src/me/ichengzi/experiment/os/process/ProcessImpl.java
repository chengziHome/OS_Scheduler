package me.ichengzi.experiment.os.process;


import me.ichengzi.experiment.os.ReturnCode;
import me.ichengzi.experiment.os.ReturnUtil;
import me.ichengzi.experiment.os.resource.Resource;
import me.ichengzi.experiment.os.resource.ResourceManager;
import me.ichengzi.experiment.os.scheduler.Scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Coding is pretty charming when you love it!
 * <p>
 * 进程的具体实现类
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:14
 */
public class ProcessImpl implements Process {

    private int PID;
    private String name;
    private int priority;
    private State state;
    private Resource blockResource;//如果state==Blocked的话，这个属性就指向被阻塞的资源，否则应该为null


    private Map<Resource, Integer> resourceMap;//当前进程持有的资源和相应数量

    private Process parent;
    private List<Process> children;

    private static final ReadyQueue readyQueue = ReadyQueue.getReadyQueue();
    private static final ProcessManager manager = ProcessManager.getManager();


    //访问级别为包内可见，也就是专门为ProcessFactory定义的构造器
    ProcessImpl(int PID, String name, int priority, State state, Map<Resource, Integer> resourceMap, Process parent, List<Process> children) {
        this.PID = PID;
        this.name = name;
        this.priority = priority;
        this.state = state;
        this.resourceMap = resourceMap;
        this.parent = parent;
        this.children = children;
    }

    @Override
    public int getPID() {
        return PID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Map<Resource, Integer> getResourceMap() {
        return resourceMap;
    }

    @Override
    public Process getParent() {
        return parent;
    }

    @Override
    public void setParent(Process parent) {
        this.parent = parent;
    }

    @Override
    public void removeParent() {
        this.parent = null;
    }

    @Override
    public List<Process> getChildren() {
        return children;
    }

    @Override
    public void addChild(Process process) {
        children.add(process);
    }

    @Override
    public void removeChild(Process process) {
        for (Process child : children) {
            if (child == process) {
                children.remove(child);
                return;
            }
        }
    }


    @Override
    public boolean setState(State state) {
        this.state = state;
        return true;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Resource getBlockResource() {
        if (state != State.BLOCKED)//防御性代码，以防某些情况blockResource未能及时清除。
            return null;
        return blockResource;
    }

    @Override
    public void setBlockResource(Resource resource) {
        this.blockResource = resource;
    }


    /**
     * 杀掉以当前进程当前进程为根节点的整个进程树。
     *
     * @return
     */
    @Override
    public ReturnUtil destory() {
        killSubTree();
        Scheduler.scheduler();
        return ReturnUtil.success();
    }

    @Override
    public ReturnUtil killSubTree() {
        if (!children.isEmpty()) {
            //注意哈，这里有个不常见的bug类型，应为killSubTree的操作会对children的内容做修改，所以这里不能将children.size()
            //直接放在for循环里面，而是应该用一个局部变量保存。
            //事实上如果用java的for-each类型的循环的话，还会抛出CurrentModifiedException.(原理就是迭代器fast-fail机制)
            int childNum = children.size();
            for (int i = 0; i < childNum; i++) {
                Process child = children.get(0);
                child.killSubTree();
            }
        }


            /*
                和指导书不一样，我这里必须先删除PCB，在释放资源。主要考虑这样一个bug情景：当要杀的这个进程是阻塞队列
                的队头，如果我先释放资源，那么在releaseResource方法中可能调度阻塞队列队头，查看need数量，结果这个进程到了readyList
                中，就算在readyList中我同样把它PCB删除了，但请注意，它携带了need数量的资源死掉了。也就是说，出现了
                “资源泄露”的情况。
                所以我的避免方法就是，先删除PCB，从block队列里面删除，然后释放资源的时候资源调度程序就不会考虑到这个进程了
                (注意readyList里面和currentProcess不存在这样的bug，这里主要讨论blocked的情况)
             */

        if (this.getState() == State.TERMINATED) {//因为下面我已经断开父子节点的连接了，按道理讲，不会出现这种情况
            return ReturnUtil.success();
        } else if (this.getState() == State.READY) {

            readyQueue.removeProcess(this);
            manager.addDeadProcess(this);

            this.setState(State.TERMINATED);
        } else if (this.getState() == State.BLOCKED) {

            Resource blockResource = this.getBlockResource();
            blockResource.removeBlockProcess(this);
            manager.addDeadProcess(this);
            this.setState(State.TERMINATED);

        } else if (this.getState() == State.RUNNING) {
            //注意，这里要kill的进程虽然是在CPU上运行，但是我并没有移动这个进程的位置
            //因为更新currentProcess的工作统一是由scheduler来完成的(规则2)
            this.setState(State.TERMINATED);
        }

        //上面仅仅修改状态，这里统一将parent指针和children里面的指针清除
        parent.removeChild(this);
        parent = null;


        //释放资源
        for (Resource resource:resourceMap.keySet()){
            resource.release(this);
        }


        return ReturnUtil.success();


    }

    /**
     * 所谓的杀掉主进程就是系统初始化的过程，
     * 相当于第一打开程序的时候的状态。
     * <p>
     * 实现方式的话就简单粗暴点，清空所有队列，资源复位等。
     */
    @Override
    public void destoryInit() {
        manager.refresh();
        readyQueue.clear();
        ResourceManager.getManager().clear();
    }


    /**
     * 详见规则3
     */


    public enum State {

        NEW,

        RUNNING,

        BLOCKED,

        READY,

        TERMINATED

    }


}
