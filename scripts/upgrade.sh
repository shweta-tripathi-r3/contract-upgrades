#!/usr/bin/env bash

for i in "$@"
do
  case $i in
    -n=*|--node=*)
    NODES="${i#*=}"
    ;;
    -c=*|--contract*)
    CONTRACT="${i#*=}"
    ;;
    -w=*|--workflow*)
    FLOW="${i#*=}"
    ;;
  esac
done


NODE=(${NODES//,/ })
for i in "${!NODE[@]}"
do
    if [[ "${CONTRACT}" != "" ]]; then
        rm  ../build/nodes/${NODE[i]}/cordapps/contracts-*.jar
        cp ../contracts/contracts-v${CONTRACT}/build/libs/contracts-v${CONTRACT}-0.1.jar ../build/nodes/${NODE[i]}/cordapps/
    fi

    if [[ "${FLOW}" != "" ]]; then
        rm  ../build/nodes/${NODE[i]}/cordapps/workflows-*.jar
        cp ../workflows/workflows-v${FLOW}/build/libs/workflows-v${FLOW}-0.1.jar ../build/nodes/${NODE[i]}/cordapps/
    fi
done

