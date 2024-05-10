# pyrebase4
# opencv-python
# time

import cv2
import time


thres = 0.58 # Threshold to detect object


#cap = cv2.VideoCapture(0)
cap = cv2.VideoCapture('rtsp://192.168.137.150:8554/mjpeg/1')
cap.set(3, 1280)
cap.set(4, 720)
cap.set(10, 70)

classNames = []
classFile = 'coco.names'
with open(classFile, 'rt') as f:
    classNames = f.read().rstrip('\n').split('\n')

configPath = 'ssd_mobilenet_v3_large_coco_2020_01_14.pbtxt'
weightsPath = 'frozen_inference_graph.pb'

net = cv2.dnn_DetectionModel(weightsPath, configPath)
net.setInputSize(320, 320)
net.setInputScale(1.0 / 127.5)
net.setInputMean((127.5, 127.5, 127.5))
net.setInputSwapRB(True)
pt = ot = time.time() * 1000.0
while True:
    success, img = cap.read()
    classIds, confs, bbox = net.detect(img, confThreshold=thres)
    # print(classIds, bbox)
    objectNames = []
    objectNamesList = ""
    i = 0
    if len(classIds) != 0:
        for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
            cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
            cv2.putText(img, classNames[classId - 1].upper(), (box[0] + 10, box[1] + 30),
                        cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
            cv2.putText(img, str(round(confidence * 100, 2)), (box[0] + 200, box[1] + 30),
                        cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

            objectNames = str(classNames[classId - 1].upper())
            if i != 0:
                objectNamesList += '_'
            objectNamesList += objectNames
            i += 1
    pt = time.time() * 1000.0
    if (pt - ot) > 3000:
        ot = pt
        print("DB ReFresh")
        if i > 0:
            print(objectNamesList)
    cv2.imshow("Output", img)
    cv2.waitKey(1)
