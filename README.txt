The aim of this project is to provide a user friendly DMR data decoder for hobbyists.
It is Java based so should run under Microsoft Windows , Apple and Linux PC's. The
only hardware needed will be a radio scanner with a discriminator audio output.

The core of the program is based around the open source DSD program by an unknown
author. This program was written in C and runs under Linux only so the first job is to
convert that code to Java and remove all sections that don't relate to DMR.

Milestone 1 - To detect voice and data sync words reached (3rd September 2010)

Milestone 2 - To reliably detect voice and data sync words.

Milestone 3 - To decode the CACH

 I'm currently working on a bug which is preventing sync words being detected outside
 of the special syncronisation mode.

Ian Wraith (10th September 2010)


