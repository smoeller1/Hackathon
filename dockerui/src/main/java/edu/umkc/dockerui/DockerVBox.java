package edu.umkc.dockerui;


import com.sun.org.apache.xerces.internal.impl.dv.xs.BooleanDV;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.virtualbox_4_1.*;
import org.virtualbox_4_1.jaxws.MachineState;
import org.virtualbox_4_1.jaxws.VboxPortType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
     */
    public Map<String, Boolean> getMachineState() {
        List<IMachine> machines = vbox.getMachines();
        updateMachineMap(machines);
        Map<String, Boolean> upStates = new HashMap<String, Boolean>();
        Iterator mit = machines.iterator();
        while (mit.hasNext()) {
            IMachine machine = (IMachine) mit.next();
            upStates.put(machine.getId(), machine.getState().equals(org.virtualbox_4_1.MachineState.Running));
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
            machineMap.put(machine.getId(), machine);
        }
    }

    //Get loading/health information for all machines that are active
    public void getMachineLoads() {
        Map<String, Boolean> machines = getMachineState();
        Map<String, String> loads = new HashMap<String, String>();
        Iterator machineIter = machines.entrySet().iterator();
        while (machineIter.hasNext()) {
            Map.Entry machine = (Map.Entry) machineIter.next();

            //Only query those machines that are actually running
            if ((Boolean) machine.getValue()) {
                IMachine thisMachine = machineMap.get(machine.getKey());
                IPerformanceCollector ipc = IPerformanceCollector.queryInterface(thisMachine);
                System.out.println("Metrics for " + machine.getKey() + ": " + ipc.toString());
            }
        }
    }

    public static void main( String[] args ) {
        DockerVBox dvb = new DockerVBox();
        Map<String, Boolean> states = dvb.getMachineState();
        Iterator statesIter = states.entrySet().iterator();
        while (statesIter.hasNext()) {
            Map.Entry item = (Map.Entry) statesIter.next();
            System.out.println(item.getKey().toString() + " state: " + item.getValue().toString());
        }
        dvb.getMachineLoads();
        dvb.close();
    }
}
