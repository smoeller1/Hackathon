package edu.umkc.dockerui;

import com.spotify.docker.client.DockerClient;

public class Node {
	private int id;
	private DockerClient dc;
	private String currentHealth;
	private String previousHealth;
	private boolean statusChange;
	private boolean highLoad;
	
	public Node(int node_id){
		this.setId(node_id);
	}
	public Node(DockerClient dockerClient) {
		this.dc = dockerClient;
		this.id+=this.id;
		// TODO Auto-generated constructor stub
	}
	public DockerClient getClientConnection(){
		return this.dc;
	}
	public void setClientConnection(DockerClient dc){
		this.dc = dc;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCurrentHealth() {
		return currentHealth;
	}
	public void setCurrentHealth(String currentHealth) {
		this.currentHealth = currentHealth;
	}
	public String getPreviousHealth() {
		return previousHealth;
	}
	public void setPreviousHealth(String previousHealth) {
		this.previousHealth = previousHealth;
	}
	public boolean getStatusChange() {
		return statusChange;
	}
	public void setStatusChange(boolean statusChange) {
		this.statusChange = statusChange;
	}
	public void setHighLoad(boolean highLoad){
		this.highLoad = true;
	}
	public boolean isHighLoad(){
		return this.highLoad;
	}
	
}
