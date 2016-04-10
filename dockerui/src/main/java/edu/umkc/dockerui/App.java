package edu.umkc.dockerui;


import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.*;

import org.virtualbox_4_1.IVirtualBox;
import org.virtualbox_4_1.VirtualBoxManager;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App
{
    private static final String containerImage = "busybox";

      public static void main( String[] args ) throws DockerCertificateException {
        DockerCertificates dockerCertificates = new DockerCertificates(Paths.get("C:\\Users\\smoeller\\.docker\\machine\\machines\\agent1"));
        DockerClient docker = DefaultDockerClient.builder()
                .uri("https://192.168.99.101:2376")
                .dockerCertificates(dockerCertificates)
                .build();
        System.out.println("Docker: " + docker.toString());
        try {
            System.out.println("Pulling " + containerImage);
            docker.pull(containerImage);
            System.out.println("Finished pulling " + containerImage);
        } catch (DockerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Info info = docker.info();
            System.out.println(info.toString());
        } catch (DockerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String[] ports = {"8080", "2222"};
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
        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(containerImage)
                .exposedPorts(ports)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();
        try {
            ContainerCreation creation = docker.createContainer(containerConfig);
            String id = creation.id();
            ContainerInfo info = docker.inspectContainer(id);
            docker.startContainer(id);
            System.out.println("Container ID: " + id);
        } catch (DockerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        docker.close();
    }
}