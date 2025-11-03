#!/bin/bash

set -e

source venv/bin/activate

python3 wrapper.py $1 $2
