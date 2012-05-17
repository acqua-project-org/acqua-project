#!/bin/bash
echo "Entering..."
ssh    mjost@moon.inria.fr 'rm -fr /auto/sop-nas2a/u/sop-nas2a/vol/home_planete/mjost/Projects/test/*'
scp -r /cygdrive/d/PFE/remote/code/gui/dist/* mjost@moon.inria.fr:/auto/sop-nas2a/u/sop-nas2a/vol/home_planete/mjost/Projects/test/
echo "Done..."
pause 2