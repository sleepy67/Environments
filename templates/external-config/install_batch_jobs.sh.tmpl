#!/usr/bin/env bash
##Run as fuseadm

##--Editable Section---------
batchJobs_version=???
config_version=???
##---------------------------

CI_SERVER={{TFC.CI_SERVER}}
CONTAINERS={{TFC.CONTAINERS}}

folder0=/appl/builddir/batchjobs
folder1=/appl/tfc/portal/EOICheckerForCIC/
folder2=/appl/tfc/portal/IncompletePayRecon/
folder3=/appl/tfc/portal/ParentAppCleanup/
folder4=/appl/tfc/portal/ReconfirmationReminder/
folder5=/appl/tfc/portal/SessionCleanup/

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

rm $folder0/*
mkdir $folder0
checker  "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=SessionCleanup&v=$batchJobs_version&r=public&p=zip -O $folder0/SessionCleanup-$batchJobs_version.zip"
checker  "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=IncompletePayRecon&v=$batchJobs_version&r=public&p=zip -O $folder0/IncompletePayRecon-$batchJobs_version.zip"
checker  "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=ParentAppCleanup&v=$batchJobs_version&r=public&p=zip -O $folder0/ParentAppCleanup-$batchJobs_version.zip"
checker  "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=ReconfirmationReminder&v=$batchJobs_version&r=public&p=zip -O $folder0/ReconfirmationReminder-$batchJobs_version.zip"
checker  "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc&a=EOICheckerForCIC&v=$batchJobs_version&r=public&p=zip -O $folder0/EOICheckerForCIC-$batchJobs_version.zip"
checker  "--content-disposition http://$CI_SERVER/service/local/artifact/maven/redirect?g=net.atos.tfc.environments.fs-nft&a=batch-jobs-config&v=$config_version&r=public&p=zip -O $folder0/batch-jobs-config-$config_version.zip"


for i in "${CONTAINERS[@]}"
do
ssh $i "rm -rf $folder0 && \
mkdir $folder0"

scp $folder0/*.zip @$i:$folder0

ssh $i "unzip -o $folder0/EOICheckerForCIC-$batchJobs_version.zip -d $folder1 && \
        unzip -o $folder0/IncompletePayRecon-$batchJobs_version.zip -d $folder2 && \
        unzip -o $folder0/ParentAppCleanup-$batchJobs_version.zip -d $folder3 && \
        unzip -o $folder0/ReconfirmationReminder-$batchJobs_version.zip -d $folder4 && \
        unzip -o $folder0/SessionCleanup-$batchJobs_version.zip -d $folder5 && \
        unzip -o $folder0/batch-jobs-config-$config_version -d $folder0 && \
		echo $folder1 $folder2 $folder3 $folder4 $folder5 | xargs -n 1 cp $folder0/job.properties && \
		chmod 774 $folder1/*.sh && \
		chmod 774 $folder2/*.sh && \
		chmod 774 $folder3/*.sh && \
		chmod 774 $folder4/*.sh && \
		chmod 774 $folder5/*.sh"
done
