# Transfer MI Datadump files into Canopy CDC FTPS server
# Note replace ENVIRONM variable with correct environment
export ENVIRONM={{TFC.ENVIRONMENT_IDENTIFIER}}
export SOURCEDIR=/appl/filestore/$ENVIRONM/mi_canopy_out
export ARCHIVEDIR=/appl/filestore/$ENVIRONM/mi_canopy_archive
export LOGDIR=/appl/tfc/portal/cdctransfer/scripts/logs
export LOGFILE=$LOGDIR/`basename $0`.$$.`date +%F.%H:%M:%S`
cd $SOURCEDIR
echo $SOURCEDIR >>$LOGFILE 2>&1
AFILES=`ls *.txt`
echo $AFILES >>$LOGFILE 2>&1
for FL in $AFILES
do
  echo curl --upload-file $FL --user tfc_mi_user:Password1 --ftp-ssl --tlsv1.2 --insecure --ftp-ssl-reqd --ftp-skip-pasv-ip --cert /appl/tfc/portal/cdctransfer/scripts/tfc-mi-usercert.pem --key /appl/tfc/portal/cdctransfer/scripts/tfc-mi-user.pem {{TFC.FTP_LOCATION}}  >>$LOGFILE 2>&1
  curl --upload-file $FL --user tfc_mi_user:Password1 --ftp-ssl --tlsv1.2 --insecure --ftp-ssl-reqd --ftp-skip-pasv-ip --cert /appl/tfc/portal/cdctransfer/scripts/tfc-mi-usercert.pem --key /appl/tfc/portal/cdctransfer/scripts/tfc-mi-user.pem {{TFC.FTP_LOCATION}}  >>$LOGFILE 2>&1
  STATUS=$?
  echo $STATUS >>$LOGFILE 2>&1
  if [ $STATUS != "0" ]; then
    echo "FAILED FILE: "$FL >>$LOGFILE 2>&1
    echo $STATUS >>$LOGFILE 2>&1
    exit $STATUS
  fi
  mv $FL $ARCHIVEDIR >>$LOGFILE 2>&1
  sleep 2
done
exit 0
