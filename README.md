# DLMS-project

This is the project implimentation of the DSD. and all the infomrmation of project description is given in the pdf. This project is a 3 members project.

First assignment : https://github.com/Hasib-rafi1/dlms

Second assignment : https://github.com/Hasib-rafi1/dlms-corba

Third assignment : https://github.com/Hasib-rafi1/dlms-web

### Run Codes In This Order

`ReplicaManagerOne/ConcordiaServer.java`

`ReplicaManagerOne/McGillServer.java`

`ReplicaManagerOne/MontrealServer.java`

`ReplicaManagerOne/RmOne.java`

`ReplicaManagerTwo/Server.java` (Have to run this server 3 times.With 3 different inputs. input Types "CON","MCG","MON")

`ReplicaManagerTwo/RmTwo.java`

`ReplicaManagerThree/DLMS_Concordia_Server.java`

`ReplicaManagerThree/DLMS_McGhill_Server.java`

`ReplicaManagerThree/DLMS_Montreal_Server.java`

`ReplicaManagerThree/RmThree.java`

`Sequencer/Sequencer.java`

`FrontEnd/FrontEnd.java`

`Client/Client.java`




### For Checking Fault replace 
Login as Concordia Manager Ex(CONM1111) and execute the List of items by pressing 3 for 4 times (3 times consequtive and in the 4 th time it will give you the correct result). it Will give you the wrong result for 3 times.

### For Crash Senario
Simulate a process crash by killing that process while the application is running. RM will reboost that replica from next client request. Or login as a Concordia Manager CONM0000 and execute the list of item.


