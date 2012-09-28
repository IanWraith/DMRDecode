The aim of this project is to provide a user friendly DMR data decoder for hobbyists.
It is Java based so should run under Microsoft Windows , Apple and Linux PC's. The
only hardware needed will be a radio scanner with a discriminator audio output.

The core of the program is based around the open source DSD program by an unknown
author. This program was written in C and runs under Linux only so my first job was to
convert that code to Java and remove all sections that don't relate to DMR.

Milestone 1 - To detect voice and data sync words reached (3rd September 2010)

Milestone 2 - To reliably detect voice and data sync words. (Reached 15th October 2010)

Milestone 3 - To decode the CACH TACT (Reached 22nd October 2010)

Milestone 3.5 (!) - To decode the SLOT TYPE PDU (Reached 4th November 2010)

Milestone 4 - To decode the Short LCs that make up the CACH payload. (Reached 25th November 2010)

Milestone 5 - To work out why and then fix the problem that is causing single bit errors in frames. (Reached 14th February 2011)

Milestone 6 - Add a feature where all DMR users are logged. (Reached 19th February 2011)

Milestone 7 - To fix a memory leak type problem where the heap memory used by the program keeps growing until it slows down and crashes. (Reached 28th February 2011)

Milestone 8 - To pass certain data on to other programs via TCP/IP socket. (Reached 1st March 2011)

Build 48 - Convert the Connect Plus site ID field in SLCO 10 packets from 3 bits to 8 bits
           Add the Utilities class to remove duplicated code.
           Display the manufacturers name in Proprietary Data Headers.
           Start adding build change details in this README file.
           
Build 49 - Display the payload in half rate data packets both as binary and ASCII.

Build 50 - Correctly display an 8 bit Site ID in Connect Plus SLCO 9 packets.

Build 51 - Display Privacy Header PDUs as raw binary

Build 52 - Fix a bug which meant that PI Header PDUs in embedded frames weren't being displayed

Build 53 - Allows the users settings to be saved in DMRDecode_settings.xml and reloaded on start up.

Build 54 - Adds a CPU utilization fix contributed by Chris Sams

Build 55 - Adds an option to not view voice frames and also replaces all instances of StringBuffer
           with StringBuilder
           
Build 56 - Adds the date to the quick log.
           Displays the data filters at the start of each log and shows any filter changes in the log file.
      
Ian Wraith (28th September 2012)


