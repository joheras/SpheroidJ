# SpheroidJ

SpheroidJ is an [ImageJ](http://imagej.net/Welcome) plugin for spheroid segmentation. 

## Table of contents

1. [Installation](#installation)
2. [Segmentation method](#segmentation-method)
3. [Datasets and results](#datasets-and-results)
4. [Video](#video)
5. [Acknowledgments](#Acknowledgments)

## Installation

### ImageJ

Download the [latest released jar](https://github.com/joheras/SpheroidJ/releases) into the _plugins_ folder.

### Fiji

You just need to add the SpheroidJ update site:

1. Select _Help > Update..._ from the Fiji menu to start the updater.
2. Click on _Manage update sites_. This brings up a dialog where you can activate additional update sites.
3. Activate the IJPB-plugins update site and close the dialog. Now you should see an additional jar file for download.
4. Click _Apply changes_ and restart Fiji.

### Graphical Interface

A graphical interface built on top of ImageJ to employ SpheroidJ can be downloaded from [here](https://github.com/Wioland/Esferoides-master/).

## Segmentation method

## Datasets and results

### Datasets

Our plugin has been tested with several datasets of images acquired under different experimental conditions. 

| Dataset | Method | #Images  |  Image size | Microscope | Magnification | Format | Type | Culture |
|--------|--------|--------|--------|--------|--------|--------|--------|--------|
| [BL5S](https://github.com/joheras/SpheroidJ/releases/download/Datasets/BL5S.zip) | Brightfield | 50 | 1296x966 | Leica | 5x | TIFF | RGB | Suspension | 
| [BN2S](https://github.com/joheras/SpheroidJ/releases/download/Datasets/BN2S.zip) | Brightfield | 154 | 1002x1004 | Nikon | 2x | ND2 | Gray 16bits | Suspension | 
| [BN10S](https://github.com/joheras/SpheroidJ/releases/download/Datasets/BN10S.zip) | Brightfield | 105 | 1002x1004 | Nikon | 10x | ND2 | Gray 16bits | Suspension | 
| [FL5C](https://github.com/joheras/SpheroidJ/releases/download/Datasets/FL5C.zip) | Fluorescence | 19 | 1296x966  | Leica | 5x | TIFF | RGB | Collagen | 
| [FL5S](https://github.com/joheras/SpheroidJ/releases/download/Datasets/FL5S.zip) | Fluorescence | 50 | 1296X966 | Leica | 5x | TIFF | RGB | Suspension | 
| [FN2S](https://github.com/joheras/SpheroidJ/releases/download/Datasets/FN2S.zip) | Fluorescence | 34 | 1002x1004 | Nikon | 2x | ND2 | Gray 16bits | Suspension  |
| [BO10S](http://imagej.1557.x6.nabble.com/A-macro-for-automated-spheroid-size-analysis-td5009205.html) | Brightfield | 64 | 3136x2152 | Olympus | 10x | JPG | RGB | Suspension |

### Results brightfield datasets

Mean (and standard deviation) for the brightfield datasets. The best result for each dataset is highlighted in bold face, *** <0.001, > significant difference between methods. In and Iv stand for Insidia and Ivanov, respectively.

|| Insidia | Ivanov | A1 | A2 | A3 | A4 | Friedman Test | Dunn test | 
|---------| --------- | --------- | --------- | --------- | --------- | --------- | --------- | --------- |
| BL5S | 0(0) | 0(0) | 0.55(0.33) | 0.31(0.42) |  **0.63(0.39)** | 0(0) | 154.756*** | A3>A1>A2,A4,In,Iv | 
| BN2S | 0.65(0.35) | 0.2(0.36) | 0.93(0.04) | **0.94(0.02)** | 0.72(0.35) | 0.73(0.35) | 427.632***  | A2,A1>A4>A3>In>Iv |
| BN10S | 0.84(0.07) | 0.03(0.18) | 0.65(0.38) | 0.69(0.42) | 0.6(0.42) | **0.95(0.01)** |  190.462*** | A4>In>A2,A1>A3>Iv |
| BO10S | 0.91(0.09) | 0.94(0.17) |  **0.94(0.03)** | 0.42(0.42) | 0.79(0.36) | 0.88(0.10) | 224.473*** | A1,Iv>In,A4,A3>A2 | 
| Combined | 0.64(0.37) | 0.28(0.43) |  **0.81(0.27)** | 0.68(0.41) | 0.7(0.39) | 0.74(0.35) | 385.751*** | A1>A4,A3,A2>In,Iv |

### Results fluorescence datasets

Mean (and standard deviation) for the fluorescence datasets. The best result for each dataset is highlighted in bold face, *** <0.001, > significant difference between methods. In and Iv stand for Insidia and Ivanov, respectively.

|| Insidia | Ivanov | A1 | A2 | A3 | A4 | Friedman Test | Dunn test | 
|---------| --------- | --------- | --------- | --------- | --------- | --------- | --------- | --------- |
| FL5C  |  0.12(0.24)  |  0.09(0.28)  |  0.53(0.37)  |  0(0)  |  0.4(0.37)  |  0(0)  |  **0.67(0.17)** | 74.530***  | A5,A1,A3>In,Iv,A2,A4|
| FL5S  |  0.51(0.24)  |  0.04(0.1)  |  0.31(0.21)  |  0.04(0.14)  |  0.42(0.27)  |  0(0)  |  **0.89(0.07)** | 191.062***  |  A5>In,A3,A1>A2,Iv,A4|
| FN2S  |  0.03(0.02)  |  0(0)  |  0.65(0.3)  |  0.47(0.36)  |  0.02(0.16)  |  0.05(0.04)  |  **0.82(0.17)** | 148.081*** |  A5>A1,A2>A4,In,A3,Iv|
| Combined  |  0.25(0.29)  |  0.03(0.15)  |  0.48(0.32)  |  0.19(0.32)  |  0.27(0.32)  |  0.03(0.10)  |  **0.82(0.16)** | 2.78.983***  | A5>A1,In,A3>A2,A4,Iv |


## Video

## Acknowledgments 

This work was partially supported by Ministerio de Economía y Competitividad (MTM2017-88804-P), and Agencia de Desarrollo Económico de La Rioja (2017-I-IDD-00018).
