package com.ankan.nlp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Interpreter tfLite;

    private Button translateButton;
    private EditText userInput;
    private TextView outputSentence;
    private static final int maxBeng=3328;
    private static final int maxEng=1881;
    private ArrayList<String> englishTokenList = new ArrayList<>();
    private ArrayList<String> bengaliTokenList = new ArrayList<>();

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        translateButton =findViewById(R.id.button2);
        userInput = findViewById(R.id.editText);
        outputSentence = findViewById(R.id.textView);

        try {
            tfLite = new Interpreter(loadModelFile(this.getAssets(),"nmt_05_12_19_test_beng.tflite"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadJson();


        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    String[] words = userInput.getText().toString().split("\\s+");

                    int wordsLength = words.length;

                    float[][] inputArray = new float[1][8];

                    if(wordsLength > 8)
                    {
                        for(int i = 0; i < 8; i++)
                            inputArray[0][i] = getTokenNumber(englishTokenList,words[i]);
                    }
                    else
                    {
                        for(int i = 0; i < 8; i++) {
                            if(i >= wordsLength)
                                inputArray[0][i] = 0.0f;
                            else
                                inputArray[0][i] = getTokenNumber(englishTokenList, words[i]);
                        }
                    }

                    String res = runModel(inputArray,bengaliTokenList);
                    outputSentence.setText(res);
            }
        });

    }

    private void loadJson(){

        ProgressDialog pd = new ProgressDialog(MainActivity.this);

        pd.setMessage("Loading Data..");

        pd.show();

        JSONObject bengaliJsonObject = null;
        JSONObject englishJsonObject =  null;
        try {
            bengaliJsonObject = new JSONObject(loadJSONFromAsset("word_dict_beng.json"));
            englishJsonObject = new JSONObject(loadJSONFromAsset("word_dict_eng.json"));
            for(int i = 1; i< maxBeng; i++)
                bengaliTokenList.add((String)bengaliJsonObject.get(String.valueOf(i)));

            for(int i = 1; i< maxEng; i++)
                englishTokenList.add((String)englishJsonObject.get(String.valueOf(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pd.dismiss();

    }

    /**
     *
     * @param list = All the List of words we have from the JSON
     * @param key = The Word which we want to find
     * @return = Token number from JSON
     */

    private int getTokenNumber(ArrayList<String> list,String key){

        if(list.contains(key)) {
            return list.indexOf(key) + 1;
        }
        else {
            return 0;
        }
    }

    /**
     *
     * @param list = List  of all Words from the JSON File
     * @param key = The Token Number
     * @return = The word which is in the JSON File
     */

    private String getWordFromToken(ArrayList<String> list,int key){

        if(key == 0)
            return "";
        else
            return list.get(key-1);

    }


    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     *
     * @param name = File Name
     * @return = JSON String
     */

    public String loadJSONFromAsset(String name) {
        String json = null;
        try {
            InputStream is = MainActivity.this.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * @param inputVal = input tokenize array
     * @param list = ArrayList to which want to convert
     * @return = Converted String
     */

    private String runModel(float[][] inputVal,ArrayList<String> list){

        float[][][] outputVal = new float[1][8][3329];

        tfLite.run(inputVal,outputVal);

        StringBuilder stringBuilder = new StringBuilder();

        for (float[][] floats : outputVal) {
            for (float[] aFloat : floats) {

                stringBuilder.append(getWordFromToken(list,argMax(aFloat)));
                stringBuilder.append(" ");
            }

        }

        return stringBuilder.toString();
    }

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
}
