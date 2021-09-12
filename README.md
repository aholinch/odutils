# odutils
Orbit Determination Utilities is a project intended to make it easier to work with the official version of SGP4 and SGP4-XP provided by USSF.

odutils allows you to convert between TLEs and Cartesian state vectors.  It can perform orbit determination on a set of vectors returning TLEs as either SGP4 or SGP4-XP.

# Hipparchus
This project uses Hipparchus for a least-squares solver used in the orbit determination process.

# OREKIT
This project uses OREKIT for coordinate transformations and for a high-precision, numeric propagator.

# odutils-web
The odutils-web project wrapps odutils in a set of simple web services.