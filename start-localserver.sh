#!/usr/bin/env bash
export URL=https://github.com/Runscope/httpbin#installing-and-running-from-pypi
python -m httpbin.core || open ${URL}