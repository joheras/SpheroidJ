# ML
import torchvision.transforms as transforms
import torch

# Show Images
from PIL import Image, ImageOps
from skimage import color
import io
import numpy as np
import base64

# Biomedical Images
from nd2reader import ND2Reader
from skimage import exposure, img_as_ubyte

# Temp file
from tempfile import NamedTemporaryFile

# List dir
import os

# Suppress all models warnings
import warnings
warnings.filterwarnings('ignore')

from imutils.paths import list_files
import os 
from tqdm import tqdm
import urllib.request
modelWeights = "HRNet Seg-best.pth"

#------------Download weights--------------------------------------

class DownloadProgressBar(tqdm):
    def update_to(self, b=1, bsize=1, tsize=None):
        if tsize is not None:
            self.total = tsize
        self.update(b * bsize - self.n)


url = "https://www.dropbox.com/s/i4tf24peizanjbb/HRNet%20Seg-best.pth?dl=1"
if  not os.path.exists(modelWeights):
    with DownloadProgressBar(unit='B', unit_scale=True,
                             miniters=1, desc=url.split('/')[-1]) as t:
        urllib.request.urlretrieve(url, filename=modelWeights, reporthook=t.update_to)


# ---------------------Models-----------------------------------
models = {}

device = torch.device("cuda" if torch.cuda.is_available() else "cpu") 

mypath=""


"""for model_name in ['HRNet Seg-best']:
    model = torch.jit.load("models/HRNet Seg-best.pth")
    model = model.cpu()
    model.eval()

    models[model_name]=model"""


# ---------------------Best Model---------------------------------
"""best_model = ""
for model_name in onlyfiles:
    if "best" in model_name:
        best_model = model_name.replace("-best","")
        models[best_model] = models.pop(model_name)
"""


def normPRED(d):
    ma = torch.max(d)
    mi = torch.min(d)

    dn = (d-mi)/(ma-mi)

    return dn

def transform_image(image):
    my_transforms = transforms.Compose([transforms.ToTensor(),
                                        transforms.Normalize(
                                            [0.485, 0.456, 0.406],
                                            [0.229, 0.224, 0.225])])
    image_aux = image
    return my_transforms(image_aux).unsqueeze(0).to(device)

u2net_name = "u^2-net"
maskrcnn_name = "mask-rcnn"

def inference(model_name, input):
    model=models[model_name]

    # Moving to GPU if available
    model = model.to(device)

    with torch.no_grad():
        if u2net_name in model_name.lower():
            outputs, _, _, _, _, _, _ = model(input)
        elif maskrcnn_name in model_name.lower():
            _,outputs = model([input[0]])
        else:
            outputs = model(input)

    if u2net_name in model_name.lower():
        outputs=torch.sigmoid(outputs)
        outputs=normPRED(outputs)
        outputs=outputs>0.5
        outputs=outputs.type(torch.uint8)
        outputs=outputs[0]
    elif maskrcnn_name in model_name.lower():
        outputs=outputs[0]
        outputs=outputs["masks"]
        if outputs.shape[0]>0:
            outputs=outputs[0]
            outputs=outputs>0.5
            outputs=outputs.type(torch.uint8)
    else:
        outputs = torch.argmax(outputs,1)

    # Moving to CPU
    model = model.cpu()
    
    return outputs

def mask_into_image(image, mask,width,height,name,outputPath):
    mask = mask*255
    alpha=0.8

    prediction = Image.fromarray(np.uint8(mask[0]),"L")

    # Save Mask to buffer
    buff = io.BytesIO()
    image = np.array(image)
    output = color.grey2rgb(np.array(prediction))

    # Selecting color of tumor
    output[np.where((output==[255,255,255]).all(axis=2))] = [0,0,255]

    # Changing background to white
    output[np.where((output==[0,0,0]).all(axis=2))] = [255,255,255]

    # Blending image with it's mask and saving also the mask
    out_img = np.zeros(image.shape, dtype=image.dtype)
    out_img2 = np.zeros(image.shape, dtype=image.dtype)
    out_img2[:,:,:] = (alpha * image[:,:,:]) + ((1-alpha) * output[:,:,:])
    out_img[:,:,:] = output[:,:,:]
    out_img = Image.fromarray(out_img)
    out_img = transforms.Resize((height,width))(out_img)    
    out_img.save(outputPath+name+'_pred', "PNG")
    out_img2 = Image.fromarray(out_img2)
    out_img2 = transforms.Resize((height,width))(out_img2)    
    out_img2.save(outputPath+name+'_blend', "PNG")

    return buff




def get_prediction_several_models(filePath, outputPath, model_names):
    

    if filePath.endswith(".nd2"):
        
        with ND2Reader(filePath) as image_reader:
            nd2Image = image_reader[0]

            # Image Conversion --------------------------------------

            # Transforming image from 16bits into 8 bit
            nd2Image = img_as_ubyte(nd2Image)

            # Reescaling color intensity, if not image gets very dark
            nd2Image = exposure.rescale_intensity(nd2Image)

            # Converting 1 channel image to 3 channels
            nd2Image = color.grey2rgb(nd2Image)
            image = Image.fromarray(nd2Image)
    else:
        image = Image.open(filePath)
    width, height = image.size
    # We need to make it before    
    #if filePath.endswith(".tif"):
    #    array = np.uint8(np.array(image) / 256)
    #    image = Image.fromarray(array)
    image = transforms.Resize((1002,1002))(image)
    tensor = transform_image(image=image)

    data = {}
    data["filename"]=filePath
    buff = io.BytesIO()
    image.save("test2", "PNG")
    data["image"]= base64.encodebytes(buff.getvalue()).decode('ascii')
    data["mask"] = []

    masks = []
    for model_name in model_names:
        mask = inference(model_name=model_name, input=tensor)
        mask_ndarray = mask.detach().cpu().numpy()
        # Releasing CUDA Memory
        del mask
        masks.append(mask_ndarray)

        buff = mask_into_image(image, mask_ndarray,width,height,filePath[filePath.rfind('/')+1:filePath.rfind('.')],outputPath)

           
    return data

# /


modelName = 'HRNet Seg-best'
model = torch.jit.load(modelName+".pth")
model = model.cpu()
model.eval()
models[modelName]=model



def predictImage(imagePath,outputPath):
    os.makedirs(outputPath,exist_ok=True)
    get_prediction_several_models(imagePath,outputPath, [modelName])


def predictFolder(inputPath,outputPath,extension='.nd2'):
    os.makedirs(outputPath,exist_ok=True)
    images = list_files(inputPath,validExts=extension)
    for imagePath in tqdm(images):
        get_prediction_several_models(imagePath,outputPath,[modelName])

