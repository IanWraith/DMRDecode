PLEASE NOTE : This project is obsolete and no longer supported. It has been superseded by other better DMR decoders but I'm leaving the repository on here as people seem to be interested.

###

The aim of this project is to provide a user friendly DMR data decoder for hobbyists.
It is Java based so should run under Microsoft Windows , Apple and Linux PC's. The
only hardware needed will be a radio scanner with a discriminator audio output.

A binary version of this program which will run without being compiled is available from ..

https://drive.google.com/file/d/0B48eZhmoMv5nZUlpdTRNamNrQkk/view?usp=share_link&resourcekey=0-e1snv0OJnYG3bPY8_TfsDg

This was however compiled for an earlier version of Java so may or may not still run.

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
           
From build 57 onwards Java 7 is required to run DMRDecode.
           
Build 57 - Decode Connect Plus CSBKO 01 FID=6 PDUs

Build 58 - Add a link to the new download page. Also decode Rate ¾ Data Continuation PDUs as raw binary.

Build 59 - Display the Time Slot in Connect Plus Channel Grants (CSBKO 03 FID=6). This information was kindly
           provided by W8EMX on the Radioreference forums. 
           
Build 60 - Allows the user to select their audio source.

Build 61 - Now saves the selected audio source in the settings file

Build 62 - Disable the capture and debug menu items. Also change some code so virtual audio cables can be selected.

Build 63 - Grey out the capture and debug items. Catch errors when the audio source is set from the settings file and
           better display errors when changing audio sources.
           
Build 64 - Ensure that only sound capture devices can be selected when choosing a mixer. Also better display information
           from any errors during mixer selection.
           
Build 65 - Saves half rate to data to debug.csv for later analysis

Build 68 - Decodes rate 3/4 data and has a unified method of handling data.

Build 69 - Improve the decoding of Capacity Plus CSBKO=62 PDUs. This information was kindly
           provided by Eric Cottrell on the Radioreference forums. 
           Add the Linkedin menu item.
           
Build 70 - Fix a bug in the big_m_csbko62() method.

Build 71 - Change so that if a quick log file exists ask the user if they want to overwrite it or append to the existing file.
           Change so that if log file exists ask the user if they want to overwrite it or append data to the existing file.
      	   Add support for CSBKO=31 FID=16 (Call Alert) and CSBKO=32 FID=16 (Call Alert Ack) this information kindly provided 
      	   by bben95 on the Radioreference forums.
      	   Decode Tier III Sys_Parms Short LCs
      	   
Build 72 - Fix bugs in the csbko31fid16() and csbko32fid16() methods were the to and from idents were transposed. 

Build 73 - Add basic support for monitoring MS and Direct mode.

Build 74 - The program now supports nearly all Tier III CSBKOs
      
Ian Wraith (12th November 2022)


