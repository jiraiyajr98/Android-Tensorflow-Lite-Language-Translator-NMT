# Android-Tensorflow-Lite-Language-Translator-NMT

## Goal of the Project

Making an Offline Translator in Android.

## Requirements

1. Tensorflow (Latest Version)
2. Tensorflow Nightly (Latest Version)
3. Android Studio

## What we are translating 

English to Bengali.

**Source Language** - *ENGLISH*.

**Translated Language** - *BENGALI*.

## Installation

Use the package manager [pip](https://pip.pypa.io/en/stable/) to install TensorFlow, Tensorflow Nightly.

```bash
pip install tensorflow
```
```bash
pip install tf-nightly
```

## Neural Machine Translation using Keras

I have taken the file from [here](https://github.com/prateekjoshi565/machine_translation)
and edited the things which I would require. The file which I have edited is also provided on this repository.

## How it is Working?

Here I am generating two JSON files.

1. word_dict_beng.json 
2. word_dict_eng.json

This JSON's are the Tokenized version of the Bengali and English dataset we are using.

The Model is trained and is having an accuracy of 93% and is finally is being converted into a TensorFlowLite format.

To convert into TensorflowLite Format I am using the code:-
 
``` python
import tensorflow as tf

filename = 'model_05_12_2019_v1_OUT_1_Beng'
model = tf.saved_model.load(filename)

concrete_func = model.signatures[
  tf.saved_model.DEFAULT_SERVING_SIGNATURE_DEF_KEY]

concrete_func.inputs[0].set_shape([1,8])
converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func])
converter.experimental_new_converter = True

Tflite_model = converter.convert()
open("nmt_05_12_19_test_beng.tflite", "wb").write(Tflite_model)

```

The *.tf file is kept as an asset. In the "assets" folder.

### Implementation in Android Studio

1. We will be using an EditText to take the input from the user.

2. We will be separating the word from the user input sentence.

3. We will be creating a 2D float array and getting the token number from the JSON of the word. If we don't have that word we will be simply returning 0 as the token.

4. We will run the model with the generated input.

5. We will get a  result after doing so. The array size is dependent on the dataset you will be training. 

In my case, it is 3329

```java

float[][][] outputVal = new float[1][8][3329];

```
Because if u check my model summery 

```

Model: "sequential"
_________________________________________________________________
Layer (type)                 Output Shape              Param #   
=================================================================
embedding (Embedding)        (None, 8, 512)            963584    
_________________________________________________________________
lstm (LSTM)                  (None, 512)               2099200   
_________________________________________________________________
repeat_vector (RepeatVector) (None, 8, 512)            0         
_________________________________________________________________
lstm_1 (LSTM)                (None, 8, 512)            2099200   
_________________________________________________________________
dense (Dense)                (None, 8, 3329)           1707777   
=================================================================
Total params: 6,869,761
Trainable params: 6,869,761
Non-trainable params: 0
_________________________________________________________________

```
My output Dense layer is having 3329.

6. After doing so we are getting the arg max value.

```java

 private static int argMax(float[] floatArray) {

        float max = floatArray[0];
        int index = 0;

        for (int i = 0; i < floatArray.length; i++)
        {
            if (max < floatArray[i])
            {
                max = floatArray[i];
                index = i;
            }
        }
        return index;
    }

```

7. After getting the argMax value we will be using the "word_dict_beng.json" to get the words from the JSON and making a string out of it.

8. Finally, show the translation to the user.

## Limations

Here we are considering only 8 words to translate.

If the length is greater than 8 we will be ignoring that.

If it is less than 8 we will be padding it with 0.

## Summary 

Here I have tried to make an offline translation application that could be used where we won't be having enough network coverage.

This is just a Proof of Concept.

## Finally

If this project is of any help, please add a star.

And a special thanks to [Haoliang Zhang](https://github.com/haozha111) who has helped me a lot during this project.

## Screenshots

![Screenshot_20191210-140201_NLP](https://user-images.githubusercontent.com/35003965/70512690-1b74d380-1b56-11ea-9c8c-2cc4539cef7f.jpg)
![Screenshot_20191210-140225_NLP](https://user-images.githubusercontent.com/35003965/70512691-1c0d6a00-1b56-11ea-8686-ba7913fa6b5c.jpg)
