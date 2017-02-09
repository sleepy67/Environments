#!/usr/bin/env bash

##Run as fuseadm

##Editable Section----------
esm_version=???
esm_config_version=???
##--------------------------

CI_SERVER=10.65.166.128:8081
CONTAINERS=(10.65.166.240)

checker()
{
wget $1
if [ $? -eq 0 ]
then
echo "-----------got that one------------"
else
echo "-----------failed------------------"
rm -f *.zip
exit $?
fi
}

if [ ! -d /appl/builddir/${esm_version} ]; then
  mkdir /appl/builddir/${esm_version}
fi



checker "--content-disposition http://${CI_SERVER}/service/local/artifact/maven/redirect?r=releases&g=net.atos.tfc&a=ExternalServicesMock&v=${esm_version}&e=zip -P /appl/builddir/${esm_version}"
checker "--content-disposition http://${CI_SERVER}/service/local/artifact/maven/redirect?r=releases&g=net.atos.tfc.environments.training&a=esm-config&v=${esm_config_version}&e=zip -P /appl/builddir/${esm_version}"

for i in "${CONTAINERS[@]}"
do
ssh $i "mkdir -p /appl/tfc/fuse/esm"
ssh $i "mkdir -p /appl/builddir/${esm_version}"
scp /appl/builddir/${esm_version}/ExternalServicesMock-${esm_version}.zip /appl/builddir/${esm_version}/esm-config-${esm_config_version}.zip fuseadm@$i:/appl/builddir/${esm_version}
ssh $i "unzip -jo /appl/builddir/${esm_version}/ExternalServicesMock-${esm_version}.zip -d /appl/tfc/fuse/esm && \
 chmod 770 /appl/tfc/fuse/esm/ExternalServicesMock-${esm_version}.sh && \
 unzip -jo /appl/builddir/${esm_version}/esm-config-${esm_config_version}.zip -d /appl/tfc/fuse/esm && \
 pushd /appl/tfc/fuse/esm && \
 ./ExternalServicesMock-${esm_version}.sh stop && \
 nohup ./ExternalServicesMock-${esm_version}.sh start > /dev/null 2>&1 && \
 popd"
done
