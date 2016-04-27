#!/bin/bash
start=10
stop=100

gen=$start

while [ $gen -lt $stop ]; do
    java Driver $gen
    let gen=gen+1
done
