# README.md
Use Case: IBM BDA Use Case 1
Team: Hurricane Hackers

Description: Design a 3rd party tool that can act as a failover and load re-balancing engine for a cluster of docker nodes

Solution: The solution is to design a modular system of handling failover code separately and re-balancing engine separately; while still the containers being initiated are able to be moved from each module to the other. 
 - Failover: The failover module will keep track of all the nodes(polling via Virtual-Box) and the containers that each node is running. In case of a node failure, the containers that were running are restarted on other nodes based on their health factor. A node is said to be 'healthy' if it is available and its load is below the threshold. 

 - Re-balancing engine: The re-balancing engine will check node stats as a pre-condition before running a container. In case if the node is not healthy (very high load interms of CPU & RAM usage) one of the container is moved to a healty(less loaded) node. For a schdeuled time the health check of all available nodes is checked by the re-balancing engine. If any node is noticed to be over-loaded then its containers are moved to other nodes untill this node is returned to normal state.

FlowImage:
https://drive.google.com/file/d/0B4GL-N4NJdHGa2k3NUZRcVlGeUdoWV9oVkE1Q3ZRS0VVQ0ZV/preview

Solution-Implementation: Docker-client java API & Virtual-Box java API was used to implement the solution.

Execution: The solution is implemented to provide a Command-Line Interface which will first list all the available nodes. The user selects one of the node to run the images and the node re-balancer will check if the selected node is healthy. If the image uses more system resources on that node, then the re-balancer will automatically stop the container on which the image is running and the same image is initiated on lesser loaded node.

Projecsdk contains the VirtualBox API, provided by VirtualBoxt layout:
Readme.md 	- this file
dockerui 	- contains source code 
sdk 		- contains the VirtualBox API, provided by VirtualBox
Video		- demo video
doc		- contains documents of the project
  -Docker Use Case Presentation.pdf - Presentation on the project, original source is at https://docs.google.com/presentation/d/16naXiucAqpWPsD1VIVCQ4u-XV6-1aWlmEWCXth36M_o/edit?usp=sharing
  -Hackathon Project Design.pdf - Working document of software design, original source is at https://docs.google.com/document/d/1DUDTjWIm2-89zzqzaS6dPD1N_uqofGQKJSxc4EBqSX4/edit?usp=sharing
  -Node status change event.vsdx - Node event change work flow diagram
  -dockercommands.txt - a quick reference for some handy Docker commands


