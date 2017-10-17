#!/usr/bin/env bash
cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

export BASEDIR=$(dirname "$0")
export COMPRESSED_FILE=$1
export DESTINATION_DIR=$2
export EXCLUDE_FILES=$3
export EXCLUDE_PARAM;
if [ "$EXCLUDE_FILES" != "" ]; then
    export EXCLUDE_PARAM = "-x ${EXCLUDE_FILES}";
fi

process_for_error_code(){
  UNZIP_RETURN_CODE=$?
  compressed_file=$1
  destination_dir=$2
    if [ $UNZIP_RETURN_CODE != 0 ]; then
        echo "Failure to unzip file"
        rm $compressed_file
        #To make sure we delete only empty folder we use rmdir
        if [ ${#destination_dir} != 0 ]; then
            rmdir $destination_dir
        fi
        exit $UNZIP_RETURN_CODE
    fi
}

if $linux; then
    if [[ ${COMPRESSED_FILE: -3} == ".gz" ]]; then
        tar xvf ${COMPRESSED_FILE} -C ${DESTINATION_DIR}
    else
        echo unzip -q -o "${COMPRESSED_FILE}" -d "${DESTINATION_DIR}" ${EXCLUDE_PARAM}
        unzip -q -o "${COMPRESSED_FILE}" -d "${DESTINATION_DIR}" ${EXCLUDE_PARAM}
    fi

    #Linux does not create an empty directory
    process_for_error_code ${COMPRESSED_FILE}
    echo rm ${COMPRESSED_FILE}
    rm ${COMPRESSED_FILE}
fi

if $cygwin; then
    echo "${BASEDIR}/unzip.exe" -q -o "${COMPRESSED_FILE}" -d "${DESTINATION_DIR}" ${EXCLUDE_PARAM}
    "${BASEDIR}/unzip.exe" -q -o "${COMPRESSED_FILE}" -d "${DESTINATION_DIR}" ${EXCLUDE_PARAM}

    process_for_error_code ${COMPRESSED_FILE} ${DESTINATION_DIR}
    echo rm ${COMPRESSED_FILE}
    rm ${COMPRESSED_FILE}
fi