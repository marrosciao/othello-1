#!/bin/bash

for file in gen*.txt; do
    cat $file >> allgens.txt
done
