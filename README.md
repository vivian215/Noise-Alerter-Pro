# Noise-Alerter-Pro
Noise Alerter Pro is an Android-based home security application that performs real-time sound analysis and categorizes sounds (i.e. smoke detectors, knocking, glass breaking, doorbells, dog barking, and other noises) for residential security purposes. Noise Alerter Pro’s GUI includes a decibel meter, visual representations of audio signals, frequency domain features (i.e. FFT graph, spectrogram, MFCCs), and classification results with confidence scores. 

### Languages/Tools Used
I utilized Java and Android Studio for app development, audio signal processing, and user interface design. I also employed Python and its Tensorflow and Keras libraries to train the neural network model, as well as the Librosa library to execute audio analyses including feature extraction. To use the neural network model in my app, I included the Java library TensorFlow Lite for Android in my project. I also utilized third-party code to compute the FFT, spectrogram, and MFCCs.

### 2-Minute Demonstration:
[https://youtu.be/JsNId_VzClo](url)

### Technical Report:
[https://docs.google.com/document/d/16GOUSwe4h6WMc7tATdP25d35FHBkQJJ4Pfpp7-h51Y4/edit?usp=sharing](url)

### App Features
*Audio Signal Source*

Noise Alerter Pro processes audio input in real-time and includes a decibel meter to measure the intensity of the incoming sound. The system also displays a graphical representation of the audio signal. When a loud sound is detected, Noise Alerter Pro initiates a machine learning classification process that lasts for a period of 5 seconds, as indicated by the red regions in the audio signal graph.

![image](https://github.com/vivian215/Noise-Alerter-Pro/assets/56425860/1213600b-09c9-43b0-ac2e-d26c3adf6671)


*Audio Signal Processing & Feature Extraction*

Since machine learning has been extensively used in image classification, I implemented sound classification by using visual representations of sound as input for the neural network model. Noise Alerter Pro extracts Mel-frequency cepstral coefficients (MFCCs) as audio features for input. I also included togglable graphical displays of the real-time Fast Fourier Transform (FFT) graph, spectrogram, and MFCCs.
![image](https://github.com/vivian215/Noise-Alerter-Pro/assets/56425860/3e2f71cc-2c01-4a9a-a9fe-ac7959e652cd)

*Machine Learning Classification*

Noise Alerter Pro displays the results of the sound classification in real-time. The right section with the gray background shows the results of each individual prediction (1 per second, 5 per trigger). The taller and greener the bar, the more confident it is. The left section with the white background and category names shows the overall prediction for the 5-second duration by combining the individual predictions based on their confidence score and weighting with volume.

![image](https://github.com/vivian215/Noise-Alerter-Pro/assets/56425860/fd032aea-7414-4860-82b1-19101363ab0c)

<br></br>
### UML Diagram
![image](https://github.com/vivian215/Noise-Alerter-Pro/assets/56425860/4375f539-a02c-4e14-b2ba-c6318574d6e6)

### Convolutional Neural Network Model Architecture
![image](https://github.com/vivian215/Noise-Alerter-Pro/assets/56425860/989ea414-b587-4fc8-9878-0d6763b47947)

