#!/usr/bin/env bash
#Script should be run as playadm user and from /appl/builddir

##Editable Section----------
Portal_Version=???
Config_Version=???
Start_Pages_Version=$Portal_Version
CCP_Checker_Version=$Portal_Version
##---------------------------

CI_SERVER=10.65.166.128:8081

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
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=tfc-start-pages&v=$Start_Pages_Version&r=public&p=zip -O tfc-start-pages-$Start_Pages_Version.zip"
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=tfc-ccp-checker&v=$CCP_Checker_Version&r=public&p=zip -O tfc-ccp-checker-$CCP_Checker_Version.zip"

checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.training&a=portal-config&v=$Config_Version&r=public&p=zip -O portal-config-$Config_Version.zip"
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.training&a=start-pages-config&v=$Config_Version&r=public&p=zip -O start-pages-config-$Config_Version.zip"
checker "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.training&a=ccp-checker-config&v=$Config_Version&r=public&p=zip -O ccp-checker-config-$Config_Version.zip"

chmod 660 /appl/builddir/tfc-portal-$Portal_Version.zip
chmod 660 /appl/builddir/tfc-start-pages-$Start_Pages_Version.zip
chmod 660 /appl/builddir/tfc-ccp-checker-$CCP_Checker_Version.zip

unzip /appl/builddir/tfc-portal-$Portal_Version.zip -d /appl/tfc/play
ln -sfn /appl/tfc/play/tfc-portal-$Portal_Version /appl/tfc/play/latest-portal

unzip /appl/builddir/tfc-start-pages-$Start_Pages_Version.zip -d /appl/tfc/play
ln -sfn /appl/tfc/play/tfc-start-pages-$Start_Pages_Version /appl/tfc/play/latest-start-pages

unzip /appl/builddir/tfc-ccp-checker-$CCP_Checker_Version.zip -d /appl/tfc/play
ln -sfn /appl/tfc/play/tfc-ccp-checker-$CCP_Checker_Version /appl/tfc/play/latest-ccp-checker

#unzip configs to play
unzip -o portal-config-$Config_Version.zip -d /appl/tfc/play/latest-portal/conf/
unzip -o start-pages-config-$Config_Version.zip -d /appl/tfc/play/latest-start-pages/conf/
unzip -o ccp-checker-config-$Config_Version.zip -d /appl/tfc/play/latest-ccp-checker/conf/


#chmod configs
chmod 644 /appl/tfc/play/latest-portal/conf/application.conf
chmod 644 /appl/tfc/play/latest-start-pages/conf/application.conf
chmod 644 /appl/tfc/play/latest-ccp-checker/conf/application.conf
