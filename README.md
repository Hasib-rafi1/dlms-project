# dlms-project

To Run this project you need to run codes
.

Client/Client.java
FrontEnd/FrontEnd.java
Sequencer/Sequencer.java
ReplicaManagerOne/RmOne.java
ReplicaManagerOne/ConcordiaServer.java
ReplicaManagerOne/McGillServer.java
ReplicaManagerOne/MontrealServer.java
ReplicaManagerTwo/RmTwo.java
ReplicaManagerTwo/Server.java (Have to run this server 3 times.With 3 different inputs. input Types "CON","MCG","MON")
ReplicaManagerThree/RmThree.java
ReplicaManagerThree/DLMS_Concordia_Server.java
ReplicaManagerThree/DLMS_McGhill_Server.java
ReplicaManagerThree/DLMS_Montreal_Server.java

For Checking Fault replace 
Login as Concordia Manager Ex(CONM1111) and execute the List of items by pressing 3 for 4 times (3 times consequtive and in the 4 th time it will give you the correct result). it Will give you the wrong result for 3 times.

For Crash Senario
Login as Concordia Manager and the userId will be CONM0000. and execute the List of items by pressing 3. It will crush the specific server. 

