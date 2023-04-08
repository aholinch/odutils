# odutils
Orbit Determination Utilities is a project intended to make it easier to work with the official version of SGP4 and SGP4-XP provided by USSF.

odutils allows you to convert between TLEs and Cartesian state vectors.  It can perform orbit determination on a set of vectors returning TLEs as either SGP4 or SGP4-XP.

To build the project you need Apache Ant 1.7.1 or later.  Simple type "ant" from the project directory to compile the code.  To get the orekit-data zip file and expand it you can type "ant getdata".  Otherwise, if you have previously downloaded the orekit-data to your home directory the software will look for it there.  If you have previously downloaded orekit-data to somewhere else you can specificy the location via the orekit_data environment variable or using a JVM -D parameter for orekit.data.

## SGP4 Binaries
You need to have the binaries from USSF.  If you have an account, you can download them from [Space-Track.org](https://www.space-track.org/documentation#/sgp4).  As of this writing the available file is Sgp4Prop_small_v8.1.zip.  Download the zip if you agree to the terms in the user agreement, and extract it somewhere on your system.  
From the extracted folders copy the platform folder for your OS to the lib directory in this project.  For Windows, copy \<SGP4 Dir\>\Lib\Win64 to lib\ussfsgp4\Win64.  For Linux copy \<SGP4 Dir\>/Lib/Linux64 to lib/ussfsgp4/Linux64. 

##New in v9
USSF has included binaries for MacOS supporting Intel and M1.  I have gotten the Intel (IFORT) binaries to work on MacOS Big Sur.  The LD_LIBRARY_PATH must include the directory for the dylib files.  However some were compiled against "../../MACOS" so I had to add a softlink for that as well relative to the directory where I run.

The JNA utilities for loading libraries changed so if you take the new afspc-9.0.jar you have to download the new binaries from space-track.org.  Otherwise the v8.1 jar should work with older binaries.

# Hipparchus
This project uses Hipparchus for a least-squares solver used in the orbit determination process.

# OREKIT
This project uses OREKIT for coordinate transformations and for a high-precision, numeric propagator.

# odutils-web
The [odutils-web](https://github.com/aholinch/odutils-web) project wraps odutils in a set of simple web services.
