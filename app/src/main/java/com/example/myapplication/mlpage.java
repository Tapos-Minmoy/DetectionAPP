package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.ml.ModelUnquant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class mlpage extends AppCompatActivity {

    Button selectBtn, captureBtn  ;
    CardView PDF ;
    int cnt = 0 ;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth ;
    ImageView imageView;
    String fullName , username ;
    TextView result1 , result2 , result3 ;
    String[] userInfo = new String[5];
    String[] infoArr = new String[] {"User", "Prediction", "Username", "Date", "Time"};
    Bitmap image;
    private String predictionResult1 , userName ;
    int imageSize = 224 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlpage);
        getPermission();
        selectBtn = findViewById(R.id.selectBtn) ;
        captureBtn = findViewById(R.id.captureBtn) ;
        result1 = findViewById(R.id.result1);
        result2 = findViewById(R.id.result2);
        result3 = findViewById(R.id.result3);
        imageView = findViewById(R.id.imageView);
        PDF = findViewById(R.id.crystalReport) ;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();




        // Crystal Report || PDF
        String userID = firebaseUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails userDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(userDetails != null){
                    fullName = userDetails.getFullName();
                    userInfo[0] = fullName;
                    username = userDetails.getUsername() ;
                    userName = username ;
                    PDF.setEnabled(true);
                };
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mlpage.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        });
        PDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //enablePdfButton();
                createpdf();
            }
        });






        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 12);
            }
        });
    }

    void getPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mlpage.this, new String[]{Manifest.permission.CAMERA}, 11);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==11){
            if(grantResults.length>=0){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void classifyImage(Bitmap image ) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(mlpage.this);
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3 ) ;
            byteBuffer.order(ByteOrder.nativeOrder()) ;

            int [] intValues = new int[imageSize*imageSize] ;
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0  ;
            for(int i=0 ;i<imageSize ; i++){
                for(int j=0;j<imageSize;j++) {
                    int val = intValues[pixel++] ;
                    byteBuffer.putFloat(((val>>16) & 0xFF) * (1.f/255.f)) ;
                    byteBuffer.putFloat(((val>>8) & 0xFF) * (1.f/255.f)) ;
                    byteBuffer.putFloat((val & 0xFF)  * (1.f / 255.f)) ;
                }
            }
            inputFeature0.loadBuffer(byteBuffer);
            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            int maxPos = 0 ;                                                             // to find the index of the data where probability is maximum ;
            float maxConfidence = 0 ;
            float[] confidences = outputFeature0.getFloatArray() ;
            String ans = "" ;
            String[] classes = {"AG600" , "E2" , "F16"} ;
            for(int i=0;i<confidences.length;i++){
                if(confidences[i]>maxConfidence) {
                    maxConfidence = confidences[i]  ;
                    maxPos = i ;
                }
                ans += classes[i] + " :  " ;
                ans += Float.toString(confidences[i]) ;
            }

            result1.setText(classes[0]  + "  :  " + confidences[0]*100);
            result2.setText(classes[1]  + "  :  " +  confidences[1]*100);
            result3.setText(classes[2]  + "  :  "  + confidences[2]*100);
            predictionResult1 = classes[maxPos] ;
            // set the text of the value of the index maxPos
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==10){
            if(data!=null){
                Uri uri = data.getData();
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    int dimention = Math.min(image.getWidth(),image.getHeight()) ;
                    image = ThumbnailUtils.extractThumbnail(image,dimention,dimention) ;
                    imageView.setImageBitmap(image);
                    image = Bitmap.createScaledBitmap(image , imageSize , imageSize , false  ) ;
                    classifyImage(image) ;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==12){
            image = (Bitmap) data.getExtras().get("data");
            int dimention = Math.min(image.getWidth(),image.getHeight()) ;
            image = ThumbnailUtils.extractThumbnail(image,dimention,dimention) ;
            imageView.setImageBitmap(image);
            image = Bitmap.createScaledBitmap(image , imageSize , imageSize , false  ) ;
            classifyImage(image) ;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    public void createpdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.DKGRAY); // Darker line color for borders
        linePaint.setStrokeWidth(3f);

        // Create page (A4 size)
        float pageWidthInInches = 8.27f;
        float pageHeightInInches = 11.69f;
        int pageWidth = (int) (pageWidthInInches * 72);
        int pageHeight = (int) (pageHeightInInches * 72);
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page1 = pdfDocument.startPage(pageInfo);
        Canvas canvas = page1.getCanvas();

        // Current Date & Time
        Calendar dateValue = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String currDate = dateFormat.format(dateValue.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String currTime = timeFormat.format(dateValue.getTime());

        // Update user information
        userInfo[1] = predictionResult1;
        userInfo[2] = userName;
        userInfo[3] = currDate;
        userInfo[4] = currTime;

        // Draw Header Title
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28f);
        paint.setColor(Color.rgb(0, 102, 204)); // Blue for title
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
        canvas.drawText("Aircraft Detection Report", pageInfo.getPageWidth() / 2, 70, paint);

        // Draw Section Header: User Information
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(20f);
        paint.setColor(Color.rgb(0, 51, 102)); // Dark blue for section header
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int startXPos = 72, startYPos = 120;
        canvas.drawText("User Information", startXPos, startYPos, paint);

        // Draw a light blue rectangle around the user info section
        paint.setColor(Color.argb(50, 0, 153, 204)); // Light blue transparent fill
        canvas.drawRect(startXPos, startYPos + 10, pageInfo.getPageWidth() - startXPos, startYPos + 120, paint);

        // Set text color for user info
        paint.setColor(Color.BLACK);
        paint.setTextSize(16f);
        startYPos += 30;
        for (int i = 0; i < 5; i++) {
            canvas.drawText(infoArr[i], startXPos + 10, startYPos, paint);
            canvas.drawText(": " + userInfo[i], startXPos + 100, startYPos, paint);
            startYPos += 25;
        }

        // Section: Detection Result Paragraph
        paint.setTextSize(18f);
        paint.setColor(Color.rgb(0, 102, 153)); // Dark cyan for section header
        startYPos += 50;
        canvas.drawText("Aircraft Detection Details", startXPos, startYPos, paint);

        // Draw a brief paragraph describing the result
        paint.setTextSize(14f);
        paint.setColor(Color.DKGRAY);
        String paragraph = "This report presents the detection of aircraft based on the processed image. "
                + "The advanced model analyzes the uploaded image to determine the type and position of the detected aircraft. "
                + "The accuracy of detection and real-time performance provide an insightful understanding of aviation monitoring.";
        int paragraphStartY = startYPos + 30;
        int paragraphEndY = paragraphStartY + 100;
        canvas.drawText(paragraph, startXPos, paragraphStartY, paint);

        // Section: Captured Image
        paint.setTextSize(18f);
        paint.setColor(Color.rgb(0, 153, 0)); // Green for the image section header
        startYPos = paragraphEndY + 50;
        canvas.drawText("Captured/Uploaded Image", pageInfo.getPageWidth() / 2, startYPos, paint);

        // Draw image border with rounded corners
        int imageTop = startYPos + 20;
        Rect imageRect = new Rect(startXPos, imageTop, pageInfo.getPageWidth() - startXPos, imageTop + 300);
        linePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0)); // Dashed border for the image
        canvas.drawRect(imageRect, linePaint);

        // Draw the image
        if (image != null) {
            canvas.drawBitmap(image, null, imageRect, null);
        } else {
            paint.setColor(Color.RED);
            paint.setTextSize(16f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No Image Available", pageInfo.getPageWidth() / 2, imageTop + 150, paint);
        }

        // Divider Line
        linePaint.setPathEffect(null);
        paint.setColor(Color.GRAY);
        canvas.drawLine(startXPos, imageRect.bottom + 30, pageInfo.getPageWidth() - startXPos, imageRect.bottom + 30, linePaint);

        // Footer
        paint.setTextSize(12f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        int footerYPos = pageInfo.getPageHeight() - 100;
        canvas.drawText("Prepared by: Tapos Chandra Das", startXPos, footerYPos, paint);
        canvas.drawText("Computer Science & Engineering, University of Chittagong", startXPos, footerYPos + 20, paint);

        // Finish page
        pdfDocument.finishPage(page1);

        // Save the PDF document
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Aircraft_Report.pdf");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
            outputStream.close();
            Toast.makeText(mlpage.this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mlpage.this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }


}