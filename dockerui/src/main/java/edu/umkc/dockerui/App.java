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

	public static void main( String[] args ) throws DockerCertificateException, DockerException, InterruptedException {
		App dockerApp = new App();
		String containerImage = null;
		int node = 0;
		List<DockerClient> dockerClientList = new ArrayList<DockerClient>();

		//		Create a default virtualbox and establish docker-client connection to it. 
		//		DockerClient docker = DefaultDockerClient.fromEnv().build();
		DockerCertificates managerCertificates = new DockerCertificates(Paths.get("C:\\Users\\smoeller\\.docker\\machine\\machines\\manager"));
		DockerClient managerdocker = DefaultDockerClient.builder()
				.uri("https://192.168.99.100:2376")
				.dockerCertificates(managerCertificates)
				.build();
		dockerClientList.add(managerdocker);

		DockerCertificates agent1Certificates = new DockerCertificates(Paths.get("C:\\Users\\smoeller\\.docker\\machine\\machines\\agent1"));
		DockerClient agent1docker = DefaultDockerClient.builder()
				.uri("https://192.168.99.101:2376")
				.dockerCertificates(agent1Certificates)
				.build();
		dockerClientList.add(agent1docker);

		String[] ports = {"8080", "2222"};
		HostConfig hostConfig = dockerApp.buildHostConfig(ports);

		//Node List
		for (DockerClient dockerTemp : dockerClientList){
			System.out.println("Aavailable Docker: " + dockerTemp.info().name());
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

		dockerApp.pullImage(containerImage);
		ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).image(containerImage).exposedPorts(ports)
				.cmd("sh", "-c", "while :; do sleep 1; done").build();
		ContainerCreation container = dockerApp.docker.createContainer(containerConfig);
		String containerId = container.id();
		dockerApp.docker.startContainer(containerId);
		System.out.println("Finished executing the image");

		//		Check load on this agent and initiate container
		System.out.println("CPU & RAM Statistics for node: " + dockerApp.docker.info().name());
		ContainerStats stats = dockerApp.docker.stats(container.id());
		Double cpuFree = (stats.cpuStats().systemCpuUsage().doubleValue() / 1000000) / 1000000;
		Long memoryKbUsed = stats.memoryStats().maxUsage() / 1024;
		System.out.println("individual: " + cpuFree + "% free :: " + memoryKbUsed + "KB used");

		//if(nodeLoad <= threshold){
		//}

		//		ContainerCreation container2 = dockerApp.docker.createContainer(ContainerConfig.builder().hostConfig(hostConfig).image(containerImage).exposedPorts(ports)
		//				.cmd("sh", "-c", "echo 'hello!!'").build());
		//		String container2Id = container2.id();
		//		dockerApp.docker.startContainer(container2Id);

		//		Close docker connection
		dockerApp.docker.close();
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
	 */
	private DockerClient SwitchClient(DockerClient dc){
		return this.docker=dc;
	}
}
