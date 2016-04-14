package edu.umkc.dockerui;


import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.*;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final String defaultContainerImage = "busybox";
	private DockerClient docker = null;
	private Map<DockerClient, ArrayList<String>> nodeImageMap = new HashMap<DockerClient, ArrayList<String>>();

	public static void main( String[] args ) throws DockerCertificateException, DockerException, InterruptedException {
		App dockerApp = new App();
		String containerImage = null;
		int node = 0;
		List<DockerClient> dockerClientList = new ArrayList<DockerClient>();

		DockerCertificates managerCertificates = new DockerCertificates(Paths.get("/Users/sudhakar/.docker/machine/machines/manager"));
		DockerClient managerdocker = DefaultDockerClient.builder()
				.uri("https://192.168.99.100:2376")
				.dockerCertificates(managerCertificates)
				.build();
		dockerClientList.add(managerdocker);
		dockerApp.nodeImageMap.put(managerdocker, null);
		
		DockerCertificates agent1Certificates = new DockerCertificates(Paths.get("/Users/sudhakar/.docker/machine/machines/agent1"));
		DockerClient agent1docker = DefaultDockerClient.builder()
				.uri("https://192.168.99.101:2376")
				.dockerCertificates(agent1Certificates)
				.build();
		dockerClientList.add(agent1docker);
		dockerApp.nodeImageMap.put(agent1docker, null);

		String[] ports = {"8080", "2222"};
		HostConfig hostConfig = dockerApp.buildHostConfig(ports);

		List<Node> nodeList = new ArrayList<Node>();
		//Node List
		for (int i=0; i<dockerClientList.size(); i++){
			nodeList.add(new Node(dockerClientList.get(i)));
			System.out.println("Aavailable Docker: " + dockerClientList.get(i).info().name());
		}

		System.out.println("Select the node you want to run your image: ");
		Scanner sc = new Scanner(System.in);
		if (sc.hasNext()){
			node = sc.nextInt();
		}

		switch (node) {
		case 1: dockerApp.SwitchClient(managerdocker);
		break;
		case 2: dockerApp.SwitchClient(agent1docker);
		break;
		}
		System.out.println("Docker: " + dockerApp.docker.toString());
		System.out.println("Enter an image name to run: ");
		Scanner sc1 = new Scanner(System.in);
		if (sc1.hasNext()){
			containerImage = sc1.nextLine();
		}

		ArrayList<String> containers = new ArrayList<String>();
		ContainerCreation container = dockerApp.startImage(dockerApp, hostConfig, ports, containerImage);
		containers.add(containerImage);
		
		dockerApp.nodeImageMap.put(dockerApp.docker, containers);

		//		Check load on this agent and initiate container
		System.out.println("Current CPU & RAM Statistics for node: " + dockerApp.docker.info().name());
		ContainerStats stats = dockerApp.docker.stats(container.id());
		Double cpuFree = (stats.cpuStats().systemCpuUsage().doubleValue() / 1000000) / 1000000;
		Long memoryKbUsed = stats.memoryStats().maxUsage() / 1024;
		System.out.println("individual: " + cpuFree + "% used :: " + memoryKbUsed + "KB used");
		System.out.println("Every node stats will be checked for every 20sec..");
		
		//		Check the status of nodes every 20 sec
		//DockerVBox nodeVBox = new DockerVBox();
		int counter=1;
		while(counter < 5){
			Thread.sleep(30);
			for(DockerClient connectedNode:dockerClientList){
				dockerApp.moniterNodeHealth(dockerApp.SwitchClient(connectedNode));
			}
		}

		dockerApp.docker.close();
		System.out.println("Docker Connection Closed");
	}
	
	public void moniterNodeHealth(DockerClient node) throws DockerException, InterruptedException{
		List<Container> containerList = node.listContainers();
		for (Container container : containerList){
			ContainerStats stats = node.stats(container.id());
			Double cpuUsage = (stats.cpuStats().systemCpuUsage().doubleValue() / 1000000) / 1000000;
			Long memoryKbUsed = stats.memoryStats().maxUsage() / 1024;
			if (cpuUsage > 10.0  && memoryKbUsed > 20){
				System.out.println("Move the container with id: "+ container.id());
			}
		}
		
	}
	
	/**
	 * This method will create a container in the specified node(using dockerApp), image
	 * @param dockerApp
	 * @param hostConfig
	 * @param ports
	 * @param containerImage
	 * @return
	 */
	public ContainerCreation startImage(App dockerApp, HostConfig hostConfig, String[] ports, String containerImage){
		dockerApp.pullImage(containerImage);
		ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).image(containerImage).exposedPorts(ports)
				.cmd("sh", "-c", "while :; do sleep 10; done").build();
		ContainerCreation container = null;
		try {
			container = dockerApp.docker.createContainer(containerConfig);
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String containerId = container.id();
		try {
			dockerApp.docker.startContainer(containerId);
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Image execution started.. ");
		return container;
	}
	
	/** This method will pull the image from docker-hub 
	 *  based on the input String from the user.
	 * @param containerImage
	 */
	public void pullImage(String containerImage){
		try {
			System.out.println("Pulling " + containerImage);
			docker.pull(containerImage);
			System.out.println("Finished pulling " + containerImage);
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will build host configurations using the supplied ports
	 * @param ports
	 * @return
	 */
	private HostConfig buildHostConfig(String[] ports){
		Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
		for (String port : ports) {
			List<PortBinding> hostPorts = new ArrayList<PortBinding>();
			hostPorts.add(PortBinding.of("0.0.0.0", port));
			portBindings.put(port, hostPorts);
		}
		List<PortBinding> randomPort = new ArrayList<PortBinding>();
		randomPort.add(PortBinding.randomPort("0.0.0.0"));
		portBindings.put("443", randomPort);

		HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

		return hostConfig;
	}

	/**
	 * This method will be used to switch the docker-client based on the input client
	 * @param docker-client
	 * @return Switched docker-client
	 */
	private DockerClient SwitchClient(DockerClient dc){
		return this.docker=dc;
	}
}

