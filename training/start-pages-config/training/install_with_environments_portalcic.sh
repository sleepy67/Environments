#!/usr/bin/env bash
#Script should be run as playadm user

##Editable Section----------
Portal_Version=???
CIC_Version=???
Config_Version=???
CI_SERVER={{TFC.CI_SERVER}}
##---------------------------

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

checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=tfc-portal&v=$Portal_Version&r=public&p=zip -O tfc-portal-$Portal_Version.zip"
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=tfc-cic&v=$CIC_Version&r=public&p=zip -O tfc-cic-$CIC_Version.zip"

checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.training&a=portal-cic-config&v=$Config_Version&r=public&p=zip -O portal-cic-config-$Config_Version.zip"
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.training&a=cic-config&v=$Config_Version&r=public&p=zip -O cic-config-$Config_Version.zip"

chmod 660 /appl/builddir/tfc-portal-$Portal_Version.zip
chmod 660 /appl/builddir/tfc-cic-$CIC_Version.zip

unzip /appl/builddir/tfc-portal-$Portal_Version.zip -d /appl/builddir
mv /appl/builddir/tfc-portal-$Portal_Version/ /appl/tfc/play/tfc-portal-cic-$Portal_Version/
ln -sfn /appl/tfc/play/tfc-portal-cic-$Portal_Version /appl/tfc/play/latest-portal-cic

unzip /appl/builddir/tfc-cic-$CIC_Version.zip -d /appl/tfc/play
ln -sfn /appl/tfc/play/tfc-cic-$CIC_Version /appl/tfc/play/latest-cic

unzip -o portal-cic-config-$Config_Version.zip -d /appl/tfc/play/latest-portal-cic/conf/
unzip -o cic-config-$Config_Version.zip -d /appl/tfc/play/latest-cic/conf/

chmod 644 /appl/tfc/play/latest-portal-cic/conf/application.conf
chmod 644 /appl/tfc/play/latest-cic/conf/application.conf

#quick cic resolver
sed -i '/"Module"/s/enabled/#enabled'/ /appl/tfc/play/latest-cic/conf/application.conf
