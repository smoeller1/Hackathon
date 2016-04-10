package edu.umkc.dockerui;

/*
 * Simplified API to get lists of VirtualBox hosts
 * Before this will work, the VirtualBox webservice will need to be started
 * on the virtualbox host:
 *
 * //Disable authentication:
 * VBoxManage.exe setproperty websrvauthlibrary null
 *
 * //Start the webservice:
 * VBoxWebSrv.exe
 */


import com.sun.org.apache.xerces.internal.impl.dv.xs.BooleanDV;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.virtualbox_4_1.*;
import org.virtualbox_4_1.jaxws.MachineState;
import org.virtualbox_4_1.jaxws.VboxPortType;

import java.util.*;


/**
 * Hello world!
 *
 */
public class DockerVBox
{
    private static String url;
    private static String uname;
    private static String pword;
    private static Integer port;
    private static VirtualBoxManager mgr;
    private static IVirtualBox vbox;
    private static Map<String, IMachine> machineMap;

    //Constructor
    public DockerVBox () {
        this("http://localhost", 18083, "", "");
    }

    //Constructor
    public DockerVBox (String url, Integer port, String uname, String pword) {
        this.url = url;
        this.uname = uname;
        this.pword = pword;
        this.port = port;
        this.mgr = VirtualBoxManager.createInstance(null);
        this.mgr.connect(this.url + ":" + this.port.toString(), this.uname, this.pword);
        this.vbox = this.mgr.getVBox();
        this.machineMap = new HashMap<String, IMachine>();
    }

    //Destructor/cleanup
    public void close () {
        mgr.disconnect();
        mgr.cleanup();
    }

    /* Returns a Map of all machines in the VirtualBox instance
     * The key is the machine ID stored as string
     * The value is the current state of the machine as Boolean
     *   True=running
     *   False=not running
     * This will also rebuild the machineMap each time it is ran
     * If the optional boolean parameter is passed, set to true
     * to report all nodes; set to false to only report Running nodes
     */
    public Map<String, Boolean> getMachineState() {
        return getMachineState(true);
    }
    public Map<String, Boolean> getMachineState(Boolean method) {
        List<IMachine> machines = vbox.getMachines();
        updateMachineMap(machines);
        Map<String, Boolean> upStates = new HashMap<String, Boolean>();
        Iterator mit = machines.iterator();
        while (mit.hasNext()) {
            IMachine machine = (IMachine) mit.next();
            Boolean machineRunning = machine.getState().equals(org.virtualbox_4_1.MachineState.Running);
            if (method) {
                upStates.put(machine.getName(), machineRunning);
            } else if(machineRunning) {
                upStates.put(machine.getName(), machineRunning);
            }
        }
        return upStates;
    }

    //methods for keeping the list of machines current
    private void updateMachineMap() {
        updateMachineMap(vbox.getMachines());
    }

    //methods for keeping the list of machines current
    private void updateMachineMap(List<IMachine> inputMachineList) {
        machineMap.clear();
        Iterator mit = inputMachineList.iterator();
        while (mit.hasNext()) {
            IMachine machine = (IMachine) mit.next();
            machineMap.put(machine.getName(), machine);
        }
    }

    public static void main( String[] args ) {
        DockerVBox dvb = new DockerVBox();
        Map<String, Boolean> states = dvb.getMachineState(false);
        Iterator statesIter = states.entrySet().iterator();
        while (statesIter.hasNext()) {
            Map.Entry item = (Map.Entry) statesIter.next();
            System.out.println("Node " + item.getKey().toString() + " is in state " + ((Boolean) item.getValue() ? "Running" : "Not Running"));
        }
        dvb.close();
    }
}
