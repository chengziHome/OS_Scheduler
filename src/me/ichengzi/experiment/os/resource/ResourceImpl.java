package me.ichengzi.experiment.os.resource;

import me.ichengzi.experiment.os.ReturnUtil;
import me.ichengzi.experiment.os.process.Process;
import me.ichengzi.experiment.os.process.ProcessImpl;
import me.ichengzi.experiment.os.process.ReadyQueue;
import me.ichengzi.experiment.os.scheduler.Scheduler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * Coding is pretty charming when you love it!
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:18
 */
public class ResourceImpl implements Resource {

    private int RID;
    private int max;
    private int remaining;
    private Deque<BlockProcess> blockDeque;

    private static final ReadyQueue readyQueue = ReadyQueue.getReadyQueue();

    public ResourceImpl(int max){
        this.RID = ResourceManager.getManager().generateRID();
        this.max = max;
        this.remaining = max;
        blockDeque = new LinkedList<>();
    }

    @Override
    public int getRID() {
        return RID;
    }

    /**
     *
     * 当前正在执行的进程请求一定数量的资源
     * 注意仅有阻塞的情况下需要重新调度进程
     *
     * @param process 应该是当前执行的进程
     * @param need
     * @return
     */
    @Override
    public ReturnUtil request(Process process, int need) {
        if (need>max){
            return ReturnUtil.resourceError();
        }else if(need>remaining && !"init".equals(process.getName())){//非init进程，需要阻塞

            blockDeque.addLast(new BlockProcess(process,need));
            process.setState(ProcessImpl.State.BLOCKED);
            process.setBlockResource(this);
            Scheduler.scheduler();

            return ReturnUtil.resourceFailure();
        }else if(need>remaining && "init".equals(process.getName())){//init进程，采用非阻塞方式
            return ReturnUtil.resourceFailure();
        } else{//可分配
            remaining = remaining - need;
            Map<Resource,Integer> resourceMap = process.getResourceMap();
            if (resourceMap.containsKey(this)){
                Integer alreadyNum = resourceMap.get(this);
                resourceMap.put(this,alreadyNum+need);
            }else{
                resourceMap.put(this,need);
            }
            return ReturnUtil.success();
        }
    }

    /**
     *
     * 当前正在执行的进程释放一定数量的资源
     *
     * @param process
     * @return
     */
    @Override
    public ReturnUtil release(Process process) {
        int num = 0;
        for (Map.Entry<Resource,Integer> entry :process.getResourceMap().entrySet()){
            Resource resource = entry.getKey();
            if (resource.getRID() == getRID()){
                num = entry.getValue();
                break;
            }
        }
        if (num==0) return ReturnUtil.success();

        remaining = remaining + num;
        while(!blockDeque.isEmpty()){
            BlockProcess blockProcess = blockDeque.peekFirst();
            int need = blockProcess.getNeed();
            if(remaining>need){//可以唤醒阻塞队列队头的一个进程
                Process readyProcess = blockProcess.getProcess();
                request(readyProcess,need);//必然会执行到最后的else代码块,更新了PCB的资源数据区
                blockDeque.removeFirst();
                readyQueue.addProcess(readyProcess);
                readyProcess.setState(ProcessImpl.State.READY);
                readyProcess.setBlockResource(null);
            }else{
                break;
            }
        }

        //这个release方法可能有两种情况，一种是直接调用释放资源，另一种就是杀进程的时候引起的资源释放。
        //杀进程的话是等到杀完整个进程数之后再进行一个调度，所以记得如果单独释放资源，之后要紧跟scheduler命令。

        return ReturnUtil.success();
    }


    /**
     * 阻塞队列中无条件删除某个进程，这个主要是在杀进程的时候调用
     * @param process
     * @return
     */
    @Override
    public boolean removeBlockProcess(Process process) {

        for (BlockProcess bProcess:blockDeque){
            if(bProcess.getProcess()==process){
                blockDeque.remove(bProcess);
                return true;
            }
        }
        return false;
    }


    /**
     * 将该资源初始化
     */
    @Override
    public void clear() {
        remaining = max;
        blockDeque.clear();
    }




    /**
     * 打印该资源现在的状态
     */
    @Override
    public void printCurrentStauts() {
        StringBuilder sb =  new StringBuilder();
        sb.append("res-")
                .append(RID)
                .append("{max=")
                .append(max)
                .append(",remaining:")
                .append(remaining)
                .append(",")
                .append("blockDeque[");
        for (BlockProcess bProcess:blockDeque){
            sb.append(",{")
                    .append(bProcess.getProcess().getName())
                    .append(":")
                    .append(bProcess.getNeed())
                    .append("}");
        }
        sb.append("]}");
        String result = sb.toString();
        System.out.println(result.replace("[,","["));
    }





    /**
     * 阻塞进程的一个简单封装，因为要携带该阻塞进程需要的资源数目。
     */
    class BlockProcess{
        private Process process;
        private int need;

        public BlockProcess(Process process, int need) {
            this.process = process;
            this.need = need;
        }

        public Process getProcess() {
            return process;
        }

        public void setProcess(Process process) {
            this.process = process;
        }

        public int getNeed() {
            return need;
        }

        public void setNeed(int need) {
            this.need = need;
        }
    }
}
