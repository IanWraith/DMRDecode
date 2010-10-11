The aim of this project is to provide a user friendly DMR data decoder for hobbyists.
It is Java based so should run under Microsoft Windows , Apple and Linux PC's. The
only hardware needed will be a radio scanner with a discriminator audio output.

The core of the program is based around the open source DSD program by an unknown
author. This program was written in C and runs under Linux only so the first job is to
convert that code to Java and remove all sections that don't relate to DMR.

Milestone 1 - To detect voice and data sync words reached (3rd September 2010)

Milestone 2 - To reliably detect voice and data sync words.

Milestone 3 - To decode the CACH

Still not at milestone 3 as I'm not happy with the demodulation code. However moving
over to a sound thread did fix the main bug with the basic demodulation. However I
now need to add a root raised cosine filter to improve performance further.

Ian Wraith (11th October 2010)


