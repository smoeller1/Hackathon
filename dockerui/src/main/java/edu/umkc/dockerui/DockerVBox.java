package edu.umkc.dockerui;


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
     */
    public Map<String, Boolean> getMachineState() {
        List<IMachine> machines = vbox.getMachines();
        Map<String, Boolean> upStates = new HashMap<String, Boolean>();
        Iterator mit = machines.iterator();
        while (mit.hasNext()) {
            IMachine machine = (IMachine) mit.next();
            upStates.put(machine.getId(), machine.getCurrentStateModified());
        }
        return upStates;
    }

    public static void main( String[] args ) {
        DockerVBox dvb = new DockerVBox();

        System.out.println("VirtualBoxManager: " + mgr.toString());
        System.out.println("VirtualBox version: " + vbox.getVersion() + "\n");
// start it
        //mgr.startVm(m, null, 7000);
        dvb.close();
    }
}
