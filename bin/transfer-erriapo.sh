#!/bin/bash
echo "Entering..."
ssh    -i ~/.ssh/id_rsa_erriapo planete@erriapo.pl.sophia.inria.fr 'rm -fr /home/planete/pssh-1.4.3/home/test/*'
scp -r -i ~/.ssh/id_rsa_erriapo /cygdrive/d/PFE/remote/code/gui/dist/* planete@erriapo.pl.sophia.inria.fr:/home/planete/pssh-1.4.3/home/test/
echo "Done..."
pause 2