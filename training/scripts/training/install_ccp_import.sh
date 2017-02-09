#!/usr/bin/env bash
##Run as fuseadm

ccp_import_version=???
config_version=???
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

mkdir /appl/builddir/$ccp_import_version
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=ccpimport&v=$ccp_import_version&r=public&p=zip -O /appl/builddir/$ccp_import_version/tfc-ccpimport-$ccp_import_version.zip"
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.training&a=ccpimport-config&v=$config_version&r=public&p=zip -O /appl/builddir/$ccp_import_version/config-properties-$config_version.zip"


unzip -o /appl/builddir/$ccp_import_version/config-properties-$config_version.zip -d /appl/builddir/$ccp_import_version

for i in "${CONTAINERS[@]}"
do
ssh $i "mkdir /appl/builddir/$ccp_import_version"
scp /appl/builddir/$ccp_import_version/tfc-ccpimport-$ccp_import_version.zip /appl/builddir/$ccp_import_version/ccpimport.properties @$i:/appl/builddir/$ccp_import_version
ssh $i "unzip -o /appl/builddir/$ccp_import_version/tfc-ccpimport-$ccp_import_version.zip -d /appl/builddir/$ccp_import_version/ && \
 mv /appl/builddir/$ccp_import_version/ccpimport.sh /appl/builddir/$ccp_import_version/CCPIMPORT.sh && \
 chmod 770 /appl/builddir/$ccp_import_version/CCPIMPORT.sh && \
 chmod 770 /appl/builddir/$ccp_import_version/ccpimport-$ccp_import_version.jar && \
 cp /appl/builddir/$ccp_import_version/ccpimport-$ccp_import_version.jar /appl/tfc/portal/ccpimport/ && \
 cp /appl/builddir/$ccp_import_version/CCPIMPORT.sh /appl/tfc/portal/ccpimport/scripts && \
 chmod 660 /appl/builddir/$ccp_import_version/ccpimport.properties && \
 cp /appl/builddir/$ccp_import_version/ccpimport.properties /appl/tfc/portal/ccpimport/"
done
