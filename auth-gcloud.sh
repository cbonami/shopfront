#!/bin/bash
gcloud --quiet components update kubectl
#echo $GCLOUD_KEY | base64 --decode > gcloud.json
gcloud auth activate-service-account $GCLOUD_EMAIL --key-file gcloud.json
ssh-keygen -f ~/.ssh/google_compute_engine -N ""
gcloud config set project $CLOUDSDK_CORE_PROJECT
gcloud config set compute/zone $CLOUDSDK_COMPUTE_ZONE