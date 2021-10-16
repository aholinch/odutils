# odutils
Orbit Determination Utilities is a project intended to make it easier to work with the official version of SGP4 and SGP4-XP provided by USSF.

odutils allows you to convert between TLEs and Cartesian state vectors.  It can perform orbit determination on a set of vectors returning TLEs as either SGP4 or SGP4-XP.

To build the project you need Apache Ant 1.7.1 or later.  Simple type "ant" from the project directory to compile the code.  To get the orekit-data zip file and expand it you can type "ant getdata".  Otherwise, if you have previously downloaded the orekit-data to your home directory the software will look for it there.  If you have previously downloaded orekit-data to somewhere else you can specificy the location via the orekit_data environment variable or using a JVM -D parameter for orekit.data.

# Hipparchus
This project uses Hipparchus for a least-squares solver used in the orbit determination process.

# OREKIT
This project uses OREKIT for coordinate transformations and for a high-precision, numeric propagator.

# odutils-web
The odutils-web project wrapps odutils in a set of simple web services.
