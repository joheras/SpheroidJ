from __future__ import absolute_import
from . import spheroids
import sys

def main():
    arg1 = sys.argv[1]
    arg2 = sys.argv[2]
    spheroids.predictImage(arg1,arg2)

def main_folder():
    arg1 = sys.argv[1]
    arg2 = sys.argv[2]
    arg3 = sys.argv[3]
    spheroids.predictFolder(arg1,arg2,arg3)
