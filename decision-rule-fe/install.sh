#!/bin/bash

echo "********** INSTALL NODEJS **********"
curl -sL https://deb.nodesource.com/setup_16.x -o nodesource_setup.sh
sudo bash nodesource_setup.sh
sudo apt install gcc g++ make nodejs yarn -y
node -v

echo "*********************  Node dependencies INSTALL **************************"
npm install
echo "*********************  RUN BUILD CODE **************************"
npm run build
