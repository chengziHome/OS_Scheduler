package me.ichengzi.experiment.os;

import me.ichengzi.experiment.os.process.*;
import me.ichengzi.experiment.os.process.Process;
import me.ichengzi.experiment.os.resource.Resource;
import me.ichengzi.experiment.os.resource.ResourceImpl;
import me.ichengzi.experiment.os.resource.ResourceManager;
import me.ichengzi.experiment.os.scheduler.Scheduler;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final ReadyQueue readyQueue = ReadyQueue.getReadyQueue();
    private static final ProcessManager manager = ProcessManager.getManager();

    private static  int count=0;

    private static final Resource R1 = new ResourceImpl(1);
    private static final Resource R2 = new ResourceImpl(2);
    private static final Resource R3 = new ResourceImpl(3);
    private static final Resource R4 = new ResourceImpl(4);

    static{
        ResourceManager.addResource(R1);
        ResourceManager.addResource(R2);
        ResourceManager.addResource(R3);
        ResourceManager.addResource(R4);
    }



    public static void main(String[] args) {


        System.out.println("请输入命令");

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()){
            String cmdStr = scanner.nextLine();
            if ("".equals(cmdStr)){
                continue;
            }
            String[] commands = null;

            String[] cmdStrs = cmdStr.split("\\s+");
            String operation = cmdStrs[0];

            if (operation.equals("load")){
                if (cmdStrs.length!=2){
                    parameterError();
                }
                try {
                    commands = loadFile(cmdStrs[1]);
                } catch (IOException e) {
                    error(e.getMessage());
                }
            }else{
                commands = new String[]{cmdStr};
            }

            for (String command:commands){
                String[] cmds = command.split("\\s+");
                String opt = cmds[0];
                switch (opt){
                    case "init":
                        if (manager.findProcess("init")!=null){
                            error("Init process has been invoked already!!!");
                        }else{
                            ProcessFactory.createProcess("init",0);
                            success("init");
                        }
                        break;
                    case "cr":
                        if (manager.findProcess("init")==null){
                            initError();
                        }else if (cmds.length!=3){
                            parameterError();
                        }else{
                            String processName = cmds[1];
                            int priority = 0;
                            try {
                                priority = Integer.parseInt(cmds[2]);
                            } catch (NumberFormatException e) {
                                parameterError();
                            }
                            if (priority<0 || priority>2)
                                parameterError();
                            if (manager.exsitName(processName)){
                                error("process name["+processName+"] has existed already! Please replace it!");
                                break;
                            }
                            ProcessFactory.createProcess(processName,priority);
                        }
                        break;
                    case "de":
                        if (manager.findProcess("init")==null){
                            initError();
                        }else if(cmds.length!=2){
                            parameterError();
                        } else{
                            String processName = cmds[1];
                            Process process = manager.findProcess(processName);
                            if (process==null){
                                error("Can't find "+processName+"!It may has not been created or has been killed already!");
                            }else if("init".equals(processName)){
                                process.destoryInit();
                                success("Reset ");
                                System.out.println("Notice: Invoked init process before you want to do anything else !");
                            }else{
                                process.destory();
                                success("kill "+processName+" process ");
                            }
                        }
                        break;
                    case "req":
                        if (manager.findProcess("init")==null){
                            initError();
                        }else if(cmds.length!=3){
                            parameterError();
                        }else{
                            String resourceName = cmds[1];
                            int needNum = 0;
                            try {
                                needNum = Integer.parseInt(cmds[2]);
                            } catch (NumberFormatException e) {
                                parameterError();
                            }

                            Process currentProcess = manager.getCurrentProcess();
                            ReturnUtil result = null;
                            Resource res = null;
                            switch (resourceName){
                                case "R1":
                                    result = R1.request(currentProcess,needNum);
                                    res = R1;
                                    break;
                                case "R2":
                                    result = R2.request(currentProcess,needNum);
                                    res = R2;
                                    break;
                                case "R3":
                                    result = R3.request(currentProcess,needNum);
                                    res = R3;
                                    break;
                                case "R4":
                                    result = R4.request(currentProcess,needNum);
                                    res = R4;
                                    break;
                                default:
                                    parameterError();
                            }
                            if (result!=null){
                                if (result.getRet_code()==0){
                                    success("Request resource ");
                                }else{
                                    error(result.getErr_msg());
                                    res.printCurrentStauts();
                                }
                            }
                        }
                        break;
                    case "rel":
                        if (manager.findProcess("init")==null){
                            initError();
                        }else if(cmds.length!=2){
                            parameterError();
                        }else{
                            String resourceName = cmds[1];


                            Process currentProcess = manager.getCurrentProcess();
                            //这里和获取资源不同，释放资源不会失败(逻辑上)。
                            switch (resourceName){
                                case "R1":
                                    R1.release(currentProcess);
                                    break;
                                case "R2":
                                    R2.release(currentProcess);
                                    break;
                                case "R3":
                                    R3.release(currentProcess);
                                    break;
                                case "R4":
                                    R4.release(currentProcess);
                                    break;
                                default:
                                    parameterError();
                            }
                        }
                        break;
                    case "to":
                        Scheduler.timeout();
                        break;
                    case "ch":
                        if (manager.findProcess("init")==null){
                            initError();
                        }else if(cmds.length!=2){
                            parameterError();
                        }else{
                            String pname = cmds[1];
                            Process currentProcess = manager.getCurrentProcess();
                            Process process = manager.findProcess(pname);
                            if(process == null){
                                error("Can't find the process with specified name !");
                            }else if(pname.equals(currentProcess.getName())){
                                // do nothing !
                            }else if(process.getState() == ProcessImpl.State.BLOCKED){
                                error("The "+pname+" process has been blocked in the blocked queue of resource R"+process.getBlockResource().getRID());
                            }else{
                                manager.checkoutProcess(process);
                                success("Checkout the "+pname+" process");
                            }
                        }
                        break;
                    case "list":
                        if (manager.findProcess("init")==null){
                            initError();
                        }else if(cmds.length<2){
                            parameterError();
                        }else{
                            String option = cmds[1];
                            switch (option){
                                case "-pt":
                                    if(cmds.length!=3){
                                        parameterError();
                                    }else{
                                        String pname = cmds[2];
                                        Process root = manager.findProcess(pname);
                                        if(root==null){
                                            error(" Can't find "+pname+" process!");
                                        }else{
                                            manager.printProcessTree(root);
                                            // TODO: 2017/4/23 这个方法还不能打出比较复杂的树结构 ,有待完善
                                        }
                                    }
                                    break;
                                case "-pd":
                                    if (cmds.length!=3){
                                        parameterError();
                                    }else{
                                        String pname = cmds[2];
                                        Process process = manager.findProcess(pname);
                                        if(process==null){
                                            error(" Can't find "+pname+" process!");
                                        }else{
                                            manager.printProcessDetail(process);
                                        }
                                    }
                                    break;
                                case "-rl":
                                    readyQueue.printStatus();
                                    break;
                                case "-c":
                                    // do nothing!
                                    break;
                                case "-m":
                                    System.out.println("Max ProcessID(totally count) :"+manager.maxPID());
                                    break;
                                case "-r":

                                    if (cmds.length==2){
                                        ResourceManager.getManager().printCurrentStatus();
                                    }else if(cmds.length==3){
                                        String RID = cmds[2];
                                        switch (RID){
                                            case "R1":
                                                R1.printCurrentStauts();
                                                break;
                                            case "R2":
                                                R2.printCurrentStauts();
                                                break;
                                            case "R3":
                                                R3.printCurrentStauts();
                                                break;
                                            case "R4":
                                                R4.printCurrentStauts();
                                                break;
                                            default:
                                                parameterError();
                                        }
                                    }else{
                                        parameterError();
                                    }

                                    break;
                                default:
                                    parameterError();
                            }
                        }
                        break;
                    case "help":
                        printHelpMessage();
                        break;
                    case "exit":
                        System.out.println("Bye!");
                        return;
                    default:
                        System.out.println("Incorrect input! Type help for more detailed information.");

                        break;
                }
            }





            if (manager.getCurrentProcess()!=null){
                System.out.println("Current executing process:"+manager.getCurrentProcess().getName());
            }

        }

    }

    private static String[]  loadFile(String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        LineNumberReader reader = new LineNumberReader(new FileReader(filePath));
        List<String> cmdList = new ArrayList<>();
        String cmd = null;
        while((cmd=reader.readLine())!=null){
            if (!"".equals(cmd)){
                cmdList.add(cmd);
            }
        }
        String[] result = new String[cmdList.size()];
        cmdList.toArray(result);
        System.out.println(Arrays.toString(result));
        return result;
    }


    private static void success(String opteration){
        System.out.println(opteration+" successfully!");
    }

    private static void error(String err_msg){
        System.out.println("Error: "+err_msg);
    }

    private static void initError(){
        error("Init process should be invoked firstly!!!");
    }

    private static void parameterError(){
        error("Unrecognized parameter! Type help for detailed information.");
    }

    private static void printHelpMessage(){
        System.out.println("usage:  operation [one or more options]");
        System.out.println("operations include:");


        System.out.println("     init:               Invoke init process.(You need to do this at the very beginning!");
        System.out.println("     cr pname priority:  Create a new process with specified process name and priority!");
        System.out.println("                         Notice that priority should be a number(0,1,2 only)");
        System.out.println("     de pname:           Kill the process with specified process name");
        System.out.println("                         Notice that if you kill the init process,that means resetting system");
        System.out.println("     req RID num:        Current executing process will request resource with specified");
        System.out.println("                         resourceId and num ");
        System.out.println("     rel RID:            Current executing process will release all resource with specified resourceId");
        System.out.println("     to:                 Current executing process has exhausted the time slice");
        System.out.println("     ch pname:           Checkout the specified process,ignoring the priority");
        System.out.println("                         (Of course the process's state should be ready !)");
        System.out.println("     list [-option]:     Print some statistical information.");
        System.out.println("                         Options include;");
        System.out.println("                         -pt pname    Print the process tree with the specified process as root node");
        System.out.println("                         -pd pname    Print the details of the pname process");
        System.out.println("                         -rl          Print all process in the readyList");
        System.out.println("                         -c           Print current executing process");
        System.out.println("                         -m           Print the max number of processID(also total of process that has been created)");
        System.out.println("                         -r [RID]     Print detail of specified resource(include blocked queue).");
        System.out.println("                                      Print all resources's details if the RID is not specified.");
        System.out.println("     load filePath       Execute the commands in the specified file.");
        System.out.println("                         Notice that the filePath should be absolute!");
        System.out.println("     exit:               Exit the system");
        System.out.println("     help:               Print this help massage!");
        System.out.println("     Welcome to https://github.com/chengziHome/OS_Scheduler");
        System.out.println("help end.");



    }




}
