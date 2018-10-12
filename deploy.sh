#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
    gcloud --quiet components update kubectl
    # Build & push to Google container registry
    docker build -t eu.gcr.io/$CLOUDSDK_CORE_PROJECT/$MICROSERVICE_NAME:v1 .
    gcloud auth configure-docker
    # next line is not needed, but is an example of an alternative way of logging in to Google container registry using the service account
    # docker login -u _json_key -p "$(cat gcloud.json)" https://gcr.io
    docker push eu.gcr.io/$CLOUDSDK_CORE_PROJECT/$MICROSERVICE_NAME:v1
    gcloud container clusters get-credentials shop
    #  kubectl apply -f config/k8s/mongo.yml
    kubectl apply -f k8s/$MICROSERVICE_NAME.yml
    kubectl rolling-update $MICROSERVICE_NAME-controller --image=eu.gcr.io/$CLOUDSDK_CORE_PROJECT/$MICROSERVICE_NAME:v1 --image-pull-policy Always
    echo "done !"
fi
